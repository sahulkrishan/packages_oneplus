package com.android.settings.notification;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedSwitchPreference;

public class BadgePreferenceController extends NotificationPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_BADGE = "badge";
    private static final int SYSTEM_WIDE_OFF = 0;
    private static final int SYSTEM_WIDE_ON = 1;
    private static final String TAG = "BadgePrefContr";

    public BadgePreferenceController(Context context, NotificationBackend backend) {
        super(context, backend);
    }

    public String getPreferenceKey() {
        return KEY_BADGE;
    }

    public boolean isAvailable() {
        if (!super.isAvailable()) {
            return false;
        }
        if ((this.mAppRow == null && this.mChannel == null) || Secure.getInt(this.mContext.getContentResolver(), "notification_badging", 1) == 0) {
            return false;
        }
        if (this.mChannel == null || isDefaultChannel()) {
            return true;
        }
        return this.mAppRow.showBadge;
    }

    public void updateState(Preference preference) {
        if (this.mAppRow != null) {
            RestrictedSwitchPreference pref = (RestrictedSwitchPreference) preference;
            pref.setDisabledByAdmin(this.mAdmin);
            if (this.mChannel != null) {
                pref.setChecked(this.mChannel.canShowBadge());
                boolean z = isChannelConfigurable() && !pref.isDisabledByAdmin();
                pref.setEnabled(z);
                return;
            }
            pref.setChecked(this.mAppRow.showBadge);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean showBadge = ((Boolean) newValue).booleanValue();
        if (this.mChannel != null) {
            this.mChannel.setShowBadge(showBadge);
            saveChannel();
        } else if (this.mAppRow != null) {
            this.mAppRow.showBadge = showBadge;
            this.mBackend.setShowBadge(this.mAppRow.pkg, this.mAppRow.uid, showBadge);
        }
        return true;
    }
}
