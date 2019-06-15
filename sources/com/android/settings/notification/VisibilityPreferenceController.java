package com.android.settings.notification;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.RestrictedListPreference;
import com.android.settings.RestrictedListPreference.RestrictedItem;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.ArrayList;

public class VisibilityPreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_VISIBILITY_OVERRIDE = "visibility_override";
    private static final String TAG = "ChannelVisPrefContr";
    private LockPatternUtils mLockPatternUtils;

    public VisibilityPreferenceController(Context context, LockPatternUtils utils, NotificationBackend backend) {
        super(context, backend);
        this.mLockPatternUtils = utils;
    }

    public String getPreferenceKey() {
        return KEY_VISIBILITY_OVERRIDE;
    }

    public boolean isAvailable() {
        boolean z = false;
        if (!super.isAvailable() || this.mChannel == null || this.mAppRow.banned) {
            return false;
        }
        if (checkCanBeVisible(2) && isLockScreenSecure()) {
            z = true;
        }
        return z;
    }

    public void updateState(Preference preference) {
        if (this.mChannel != null && this.mAppRow != null) {
            String summaryShowEntry;
            String summaryShowEntryValue;
            RestrictedListPreference pref = (RestrictedListPreference) preference;
            ArrayList<CharSequence> entries = new ArrayList();
            ArrayList<CharSequence> values = new ArrayList();
            pref.clearRestrictedItems();
            if (getLockscreenNotificationsEnabled() && getLockscreenAllowPrivateNotifications()) {
                summaryShowEntry = this.mContext.getString(R.string.lock_screen_notifications_summary_show);
                summaryShowEntryValue = Integer.toString(NotificationManagerCompat.IMPORTANCE_UNSPECIFIED);
                entries.add(summaryShowEntry);
                values.add(summaryShowEntryValue);
                setRestrictedIfNotificationFeaturesDisabled(pref, summaryShowEntry, summaryShowEntryValue, 12);
            }
            if (getLockscreenNotificationsEnabled()) {
                summaryShowEntry = this.mContext.getString(R.string.lock_screen_notifications_summary_hide);
                summaryShowEntryValue = Integer.toString(null);
                entries.add(summaryShowEntry);
                values.add(summaryShowEntryValue);
                setRestrictedIfNotificationFeaturesDisabled(pref, summaryShowEntry, summaryShowEntryValue, 4);
            }
            entries.add(this.mContext.getString(R.string.lock_screen_notifications_summary_disable));
            values.add(Integer.toString(-1));
            pref.setEntries((CharSequence[]) entries.toArray(new CharSequence[entries.size()]));
            pref.setEntryValues((CharSequence[]) values.toArray(new CharSequence[values.size()]));
            if (this.mChannel.getLockscreenVisibility() == NotificationManagerCompat.IMPORTANCE_UNSPECIFIED) {
                pref.setValue(Integer.toString(getGlobalVisibility()));
            } else {
                pref.setValue(Integer.toString(this.mChannel.getLockscreenVisibility()));
            }
            pref.setSummary("%s");
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mChannel != null) {
            int sensitive = Integer.parseInt((String) newValue);
            if (sensitive == getGlobalVisibility()) {
                sensitive = NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;
            }
            this.mChannel.setLockscreenVisibility(sensitive);
            this.mChannel.lockFields(2);
            saveChannel();
        }
        return true;
    }

    private void setRestrictedIfNotificationFeaturesDisabled(RestrictedListPreference pref, CharSequence entry, CharSequence entryValue, int keyguardNotificationFeatures) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(this.mContext, keyguardNotificationFeatures, this.mAppRow.userId);
        if (admin != null) {
            pref.addRestrictedItem(new RestrictedItem(entry, entryValue, admin));
        }
    }

    private int getGlobalVisibility() {
        if (!getLockscreenNotificationsEnabled()) {
            return -1;
        }
        if (getLockscreenAllowPrivateNotifications()) {
            return NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;
        }
        return 0;
    }

    private boolean getLockscreenNotificationsEnabled() {
        return Secure.getInt(this.mContext.getContentResolver(), "lock_screen_show_notifications", 0) != 0;
    }

    private boolean getLockscreenAllowPrivateNotifications() {
        return Secure.getInt(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0) != 0;
    }

    /* Access modifiers changed, original: protected */
    public boolean isLockScreenSecure() {
        boolean lockscreenSecure = this.mLockPatternUtils.isSecure(UserHandle.myUserId());
        UserInfo parentUser = this.mUm.getProfileParent(UserHandle.myUserId());
        if (parentUser != null) {
            return lockscreenSecure | this.mLockPatternUtils.isSecure(parentUser.id);
        }
        return lockscreenSecure;
    }
}
