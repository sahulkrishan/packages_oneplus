package com.android.settings.notification;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.RingtonePreference;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.ringtone.OPRingtonePickerActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigureNotificationSettings extends DashboardFragment {
    @VisibleForTesting
    static final String KEY_LOCKSCREEN = "lock_screen_notifications";
    @VisibleForTesting
    static final String KEY_LOCKSCREEN_WORK_PROFILE = "lock_screen_notifications_profile";
    @VisibleForTesting
    static final String KEY_LOCKSCREEN_WORK_PROFILE_HEADER = "lock_screen_notifications_profile_header";
    private static final String KEY_NOTI_DEFAULT_RINGTONE = "notification_default_ringtone";
    @VisibleForTesting
    static final String KEY_SWIPE_DOWN = "gesture_swipe_down_fingerprint_notifications";
    private static final String KEY_ZEN_MODE = "zen_mode_notifications";
    private static final int REQUEST_CODE = 200;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.configure_notification_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ConfigureNotificationSettings.buildPreferenceControllers(context, null, null, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(ConfigureNotificationSettings.KEY_SWIPE_DOWN);
            keys.add(ConfigureNotificationSettings.KEY_LOCKSCREEN);
            keys.add(ConfigureNotificationSettings.KEY_LOCKSCREEN_WORK_PROFILE);
            keys.add(ConfigureNotificationSettings.KEY_LOCKSCREEN_WORK_PROFILE_HEADER);
            keys.add(ConfigureNotificationSettings.KEY_ZEN_MODE);
            return keys;
        }
    };
    private static final String SELECTED_PREFERENCE_KEY = "selected_preference";
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static final String TAG = "ConfigNotiSettings";
    private RingtonePreference mRequestPreference;

    static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private NotificationBackend mBackend = new NotificationBackend();
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        /* Access modifiers changed, original: protected */
        @VisibleForTesting
        public void setBackend(NotificationBackend backend) {
            this.mBackend = backend;
        }

        public void setListening(boolean listening) {
            if (listening) {
                int blockedAppCount = this.mBackend.getBlockedAppCount();
                if (blockedAppCount == 0) {
                    this.mSummaryLoader.setSummary(this, this.mContext.getText(R.string.app_notification_listing_summary_zero));
                } else {
                    this.mSummaryLoader.setSummary(this, this.mContext.getResources().getQuantityString(R.plurals.app_notification_listing_summary_others, blockedAppCount, new Object[]{Integer.valueOf(blockedAppCount)}));
                }
            }
        }
    }

    public int getMetricsCategory() {
        return 337;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.configure_notification_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        Application app;
        Activity activity = getActivity();
        if (activity != null) {
            app = activity.getApplication();
        } else {
            app = null;
        }
        return buildPreferenceControllers(context, getLifecycle(), app, this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle, Application app, Fragment host) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        PulseNotificationPreferenceController pulseController = new PulseNotificationPreferenceController(context);
        LockScreenNotificationPreferenceController lockScreenNotificationController = new LockScreenNotificationPreferenceController(context, KEY_LOCKSCREEN, KEY_LOCKSCREEN_WORK_PROFILE_HEADER, KEY_LOCKSCREEN_WORK_PROFILE);
        if (lifecycle != null) {
            lifecycle.addObserver(pulseController);
            lifecycle.addObserver(lockScreenNotificationController);
        }
        controllers.add(new RecentNotifyingAppsPreferenceController(context, new NotificationBackend(), app, host));
        controllers.add(pulseController);
        controllers.add(lockScreenNotificationController);
        controllers.add(new NotificationRingtonePreferenceController(context) {
            public String getPreferenceKey() {
                return ConfigureNotificationSettings.KEY_NOTI_DEFAULT_RINGTONE;
            }
        });
        controllers.add(new ZenModePreferenceController(context, lifecycle, KEY_ZEN_MODE));
        return controllers;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof RingtonePreference)) {
            return super.onPreferenceTreeClick(preference);
        }
        this.mRequestPreference = (RingtonePreference) preference;
        this.mRequestPreference.onPrepareRingtonePickerIntent(new Intent(getActivity(), OPRingtonePickerActivity.class));
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mRequestPreference != null) {
            this.mRequestPreference.onActivityResult(requestCode, resultCode, data);
            this.mRequestPreference = null;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mRequestPreference != null) {
            outState.putString(SELECTED_PREFERENCE_KEY, this.mRequestPreference.getKey());
        }
    }
}
