package com.android.settings.notification;

import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.RestrictedListPreference;
import com.android.settings.RestrictedListPreference.RestrictedItem;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import java.util.ArrayList;

public class LockScreenNotificationPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause {
    private static final String TAG = "LockScreenNotifPref";
    private RestrictedListPreference mLockscreen;
    private RestrictedListPreference mLockscreenProfile;
    private int mLockscreenSelectedValue;
    private int mLockscreenSelectedValueProfile;
    private final int mProfileUserId;
    private final boolean mSecure;
    private final boolean mSecureProfile;
    private final String mSettingKey;
    private SettingObserver mSettingObserver;
    private final String mWorkSettingCategoryKey;
    private final String mWorkSettingKey;

    class SettingObserver extends ContentObserver {
        private final Uri LOCK_SCREEN_PRIVATE_URI = Secure.getUriFor("lock_screen_allow_private_notifications");
        private final Uri LOCK_SCREEN_SHOW_URI = Secure.getUriFor("lock_screen_show_notifications");

        public SettingObserver() {
            super(new Handler());
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.LOCK_SCREEN_PRIVATE_URI, false, this);
                cr.registerContentObserver(this.LOCK_SCREEN_SHOW_URI, false, this);
                return;
            }
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.LOCK_SCREEN_PRIVATE_URI.equals(uri) || this.LOCK_SCREEN_SHOW_URI.equals(uri)) {
                LockScreenNotificationPreferenceController.this.updateLockscreenNotifications();
                if (LockScreenNotificationPreferenceController.this.mProfileUserId != -10000) {
                    LockScreenNotificationPreferenceController.this.updateLockscreenNotificationsForProfile();
                }
            }
        }
    }

    public LockScreenNotificationPreferenceController(Context context) {
        this(context, null, null, null);
    }

    public LockScreenNotificationPreferenceController(Context context, String settingKey, String workSettingCategoryKey, String workSettingKey) {
        super(context);
        this.mSettingKey = settingKey;
        this.mWorkSettingCategoryKey = workSettingCategoryKey;
        this.mWorkSettingKey = workSettingKey;
        this.mProfileUserId = Utils.getManagedProfileId(UserManager.get(context), UserHandle.myUserId());
        LockPatternUtils utils = FeatureFactory.getFactory(context).getSecurityFeatureProvider().getLockPatternUtils(context);
        this.mSecure = utils.isSecure(UserHandle.myUserId());
        boolean z = (this.mProfileUserId == -10000 || this.mProfileUserId == 999 || !utils.isSecure(this.mProfileUserId)) ? false : true;
        this.mSecureProfile = z;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mLockscreen = (RestrictedListPreference) screen.findPreference(this.mSettingKey);
        if (this.mLockscreen == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Preference not found: ");
            stringBuilder.append(this.mSettingKey);
            Log.i(str, stringBuilder.toString());
            return;
        }
        if (this.mProfileUserId == -10000 || this.mProfileUserId == 999) {
            setVisible(screen, this.mWorkSettingKey, false);
            setVisible(screen, this.mWorkSettingCategoryKey, false);
        } else {
            this.mLockscreenProfile = (RestrictedListPreference) screen.findPreference(this.mWorkSettingKey);
            this.mLockscreenProfile.setRequiresActiveUnlockedProfile(true);
            this.mLockscreenProfile.setProfileUserId(this.mProfileUserId);
        }
        this.mSettingObserver = new SettingObserver();
        initLockScreenNotificationPrefDisplay();
        initLockscreenNotificationPrefForProfile();
    }

    private void initLockScreenNotificationPrefDisplay() {
        ArrayList<CharSequence> entries = new ArrayList();
        ArrayList<CharSequence> values = new ArrayList();
        String summaryShowEntry = this.mContext.getString(R.string.lock_screen_notifications_summary_show);
        String summaryShowEntryValue = Integer.toString(R.string.lock_screen_notifications_summary_show);
        entries.add(summaryShowEntry);
        values.add(summaryShowEntryValue);
        setRestrictedIfNotificationFeaturesDisabled(summaryShowEntry, summaryShowEntryValue, 12);
        if (this.mSecure) {
            String summaryHideEntry = this.mContext.getString(R.string.lock_screen_notifications_summary_hide);
            String summaryHideEntryValue = Integer.toString(R.string.lock_screen_notifications_summary_hide);
            entries.add(summaryHideEntry);
            values.add(summaryHideEntryValue);
            setRestrictedIfNotificationFeaturesDisabled(summaryHideEntry, summaryHideEntryValue, 4);
        }
        entries.add(this.mContext.getString(R.string.lock_screen_notifications_summary_disable));
        values.add(Integer.toString(R.string.lock_screen_notifications_summary_disable));
        this.mLockscreen.setEntries((CharSequence[]) entries.toArray(new CharSequence[entries.size()]));
        this.mLockscreen.setEntryValues((CharSequence[]) values.toArray(new CharSequence[values.size()]));
        updateLockscreenNotifications();
        if (this.mLockscreen.getEntries().length > 1) {
            this.mLockscreen.setOnPreferenceChangeListener(this);
        } else {
            this.mLockscreen.setEnabled(false);
        }
    }

    private void initLockscreenNotificationPrefForProfile() {
        if (this.mLockscreenProfile == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Preference not found: ");
            stringBuilder.append(this.mWorkSettingKey);
            Log.i(str, stringBuilder.toString());
            return;
        }
        ArrayList<CharSequence> entries = new ArrayList();
        ArrayList<CharSequence> values = new ArrayList();
        String summaryShowEntry = this.mContext.getString(R.string.lock_screen_notifications_summary_show_profile);
        String summaryShowEntryValue = Integer.toString(R.string.lock_screen_notifications_summary_show_profile);
        entries.add(summaryShowEntry);
        values.add(summaryShowEntryValue);
        setRestrictedIfNotificationFeaturesDisabled(summaryShowEntry, summaryShowEntryValue, 12);
        if (this.mSecureProfile) {
            String summaryHideEntry = this.mContext.getString(R.string.lock_screen_notifications_summary_hide_profile);
            String summaryHideEntryValue = Integer.toString(R.string.lock_screen_notifications_summary_hide_profile);
            entries.add(summaryHideEntry);
            values.add(summaryHideEntryValue);
            setRestrictedIfNotificationFeaturesDisabled(summaryHideEntry, summaryHideEntryValue, 4);
        }
        this.mLockscreenProfile.setEntries((CharSequence[]) entries.toArray(new CharSequence[entries.size()]));
        this.mLockscreenProfile.setEntryValues((CharSequence[]) values.toArray(new CharSequence[values.size()]));
        updateLockscreenNotificationsForProfile();
        if (this.mLockscreenProfile.getEntries().length > 1) {
            this.mLockscreenProfile.setOnPreferenceChangeListener(this);
        } else {
            this.mLockscreenProfile.setEnabled(false);
        }
    }

    public String getPreferenceKey() {
        return null;
    }

    public boolean isAvailable() {
        return false;
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), true);
        }
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        int i = 0;
        int val;
        boolean show;
        if (TextUtils.equals(this.mWorkSettingKey, key)) {
            val = Integer.parseInt((String) newValue);
            if (val == this.mLockscreenSelectedValueProfile) {
                return false;
            }
            show = val == R.string.lock_screen_notifications_summary_show_profile;
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = "lock_screen_allow_private_notifications";
            if (show) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, this.mProfileUserId);
            this.mLockscreenSelectedValueProfile = val;
            return true;
        } else if (!TextUtils.equals(this.mSettingKey, key)) {
            return false;
        } else {
            val = Integer.parseInt((String) newValue);
            if (val == this.mLockscreenSelectedValue) {
                return false;
            }
            show = val != R.string.lock_screen_notifications_summary_disable;
            boolean show2 = val == R.string.lock_screen_notifications_summary_show;
            Secure.putInt(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", show2 ? 1 : 0);
            Secure.putInt(this.mContext.getContentResolver(), "lock_screen_show_notifications", show ? 1 : 0);
            Secure.putIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", show2 ? 1 : 0, 999);
            ContentResolver contentResolver2 = this.mContext.getContentResolver();
            String str2 = "lock_screen_show_notifications";
            if (show) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver2, str2, i, 999);
            this.mLockscreenSelectedValue = val;
            return true;
        }
    }

    private void setRestrictedIfNotificationFeaturesDisabled(CharSequence entry, CharSequence entryValue, int keyguardNotificationFeatures) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this.mContext, keyguardNotificationFeatures, UserHandle.myUserId());
        if (!(admin == null || this.mLockscreen == null)) {
            this.mLockscreen.addRestrictedItem(new RestrictedItem(entry, entryValue, admin));
        }
        if (this.mProfileUserId != -10000) {
            EnforcedAdmin profileAdmin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this.mContext, keyguardNotificationFeatures, this.mProfileUserId);
            if (profileAdmin != null && this.mLockscreenProfile != null) {
                this.mLockscreenProfile.addRestrictedItem(new RestrictedItem(entry, entryValue, profileAdmin));
            }
        }
    }

    public static int getSummaryResource(Context context) {
        boolean enabled = getLockscreenNotificationsEnabled(context);
        boolean allowPrivate = !FeatureFactory.getFactory(context).getSecurityFeatureProvider().getLockPatternUtils(context).isSecure(UserHandle.myUserId()) || getAllowPrivateNotifications(context, UserHandle.myUserId());
        if (!enabled) {
            return R.string.lock_screen_notifications_summary_disable;
        }
        if (allowPrivate) {
            return R.string.lock_screen_notifications_summary_show;
        }
        return R.string.lock_screen_notifications_summary_hide;
    }

    private void updateLockscreenNotifications() {
        if (this.mLockscreen != null) {
            this.mLockscreenSelectedValue = getSummaryResource(this.mContext);
            this.mLockscreen.setSummary("%s");
            this.mLockscreen.setValue(Integer.toString(this.mLockscreenSelectedValue));
        }
    }

    private boolean adminAllowsUnredactedNotifications(int userId) {
        return (((DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class)).getKeyguardDisabledFeatures(null, userId) & 8) == 0;
    }

    private void updateLockscreenNotificationsForProfile() {
        if (this.mProfileUserId != -10000 && this.mLockscreenProfile != null) {
            int i;
            boolean allowPrivate = adminAllowsUnredactedNotifications(this.mProfileUserId) && (!this.mSecureProfile || getAllowPrivateNotifications(this.mContext, this.mProfileUserId));
            this.mLockscreenProfile.setSummary("%s");
            if (allowPrivate) {
                i = R.string.lock_screen_notifications_summary_show_profile;
            } else {
                i = R.string.lock_screen_notifications_summary_hide_profile;
            }
            this.mLockscreenSelectedValueProfile = i;
            this.mLockscreenProfile.setValue(Integer.toString(this.mLockscreenSelectedValueProfile));
        }
    }

    private static boolean getLockscreenNotificationsEnabled(Context context) {
        return Secure.getInt(context.getContentResolver(), "lock_screen_show_notifications", 0) != 0;
    }

    private static boolean getAllowPrivateNotifications(Context context, int userId) {
        return Secure.getIntForUser(context.getContentResolver(), "lock_screen_allow_private_notifications", 0, userId) != 0;
    }
}
