package com.android.settings.notification;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Global;
import android.service.notification.ScheduleCalendar;
import android.service.notification.ZenModeConfig;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public abstract class AbstractZenModePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {
    protected static ZenModeConfigWrapper mZenModeConfigWrapper;
    private final String KEY;
    protected final ZenModeBackend mBackend;
    protected MetricsFeatureProvider mMetricsFeatureProvider;
    private final NotificationManager mNotificationManager;
    protected PreferenceScreen mScreen;
    @VisibleForTesting
    protected SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri ZEN_MODE_CONFIG_ETAG_URI = Global.getUriFor("zen_mode_config_etag");
        private final Uri ZEN_MODE_DURATION_URI = Global.getUriFor("zen_duration");
        private final Uri ZEN_MODE_URI = Global.getUriFor("zen_mode");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(this.ZEN_MODE_URI, false, this, -1);
            cr.registerContentObserver(this.ZEN_MODE_CONFIG_ETAG_URI, false, this, -1);
            cr.registerContentObserver(this.ZEN_MODE_DURATION_URI, false, this, -1);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri == null || this.ZEN_MODE_URI.equals(uri) || this.ZEN_MODE_CONFIG_ETAG_URI.equals(uri) || this.ZEN_MODE_DURATION_URI.equals(uri)) {
                AbstractZenModePreferenceController.this.mBackend.updatePolicy();
                AbstractZenModePreferenceController.this.mBackend.updateZenMode();
                if (AbstractZenModePreferenceController.this.mScreen != null) {
                    AbstractZenModePreferenceController.this.displayPreference(AbstractZenModePreferenceController.this.mScreen);
                }
                AbstractZenModePreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    @VisibleForTesting
    static class ZenModeConfigWrapper {
        private final Context mContext;

        public ZenModeConfigWrapper(Context context) {
            this.mContext = context;
        }

        /* Access modifiers changed, original: protected */
        public String getOwnerCaption(String owner) {
            return ZenModeConfig.getOwnerCaption(this.mContext, owner);
        }

        /* Access modifiers changed, original: protected */
        public boolean isTimeRule(Uri id) {
            return ZenModeConfig.isValidEventConditionId(id) || ZenModeConfig.isValidScheduleConditionId(id);
        }

        /* Access modifiers changed, original: protected */
        public CharSequence getFormattedTime(long time, int userHandle) {
            return ZenModeConfig.getFormattedTime(this.mContext, time, isToday(time), userHandle);
        }

        private boolean isToday(long time) {
            return ZenModeConfig.isToday(time);
        }

        /* Access modifiers changed, original: protected */
        public long parseManualRuleTime(Uri id) {
            return ZenModeConfig.tryParseCountdownConditionId(id);
        }

        /* Access modifiers changed, original: protected */
        public long parseAutomaticRuleEndTime(Uri id) {
            if (ZenModeConfig.isValidEventConditionId(id)) {
                return Long.MAX_VALUE;
            }
            if (!ZenModeConfig.isValidScheduleConditionId(id)) {
                return -1;
            }
            ScheduleCalendar schedule = ZenModeConfig.toScheduleCalendar(id);
            long endTimeMs = schedule.getNextChangeTime(System.currentTimeMillis());
            if (schedule.exitAtAlarm()) {
                long nextAlarm = AbstractZenModePreferenceController.getNextAlarm(this.mContext);
                schedule.maybeSetNextAlarm(System.currentTimeMillis(), nextAlarm);
                if (schedule.shouldExitForAlarm(endTimeMs)) {
                    return nextAlarm;
                }
            }
            return endTimeMs;
        }
    }

    public AbstractZenModePreferenceController(Context context, String key, Lifecycle lifecycle) {
        super(context);
        mZenModeConfigWrapper = new ZenModeConfigWrapper(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        this.KEY = key;
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(this.mContext).getMetricsFeatureProvider();
        this.mBackend = ZenModeBackend.getInstance(context);
    }

    public String getPreferenceKey() {
        return this.KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mScreen = screen;
        Preference pref = screen.findPreference(this.KEY);
        if (pref != null) {
            this.mSettingObserver = new SettingObserver(pref);
        }
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver());
            this.mSettingObserver.onChange(false, null);
        }
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.unregister(this.mContext.getContentResolver());
        }
    }

    /* Access modifiers changed, original: protected */
    public Policy getPolicy() {
        return this.mNotificationManager.getNotificationPolicy();
    }

    /* Access modifiers changed, original: protected */
    public ZenModeConfig getZenModeConfig() {
        return this.mNotificationManager.getZenModeConfig();
    }

    /* Access modifiers changed, original: protected */
    public int getZenMode() {
        return Global.getInt(this.mContext.getContentResolver(), "zen_mode", this.mBackend.mZenMode);
    }

    /* Access modifiers changed, original: protected */
    public int getZenDuration() {
        return Global.getInt(this.mContext.getContentResolver(), "zen_duration", 0);
    }

    private static long getNextAlarm(Context context) {
        AlarmClockInfo info = ((AlarmManager) context.getSystemService(NotificationCompat.CATEGORY_ALARM)).getNextAlarmClock(ActivityManager.getCurrentUser());
        return info != null ? info.getTriggerTime() : 0;
    }
}
