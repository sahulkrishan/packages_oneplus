package com.android.settings.notification;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.accounts.AccountRestrictionHelper;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;

public class EmergencyBroadcastPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private AccountRestrictionHelper mHelper;
    private PackageManager mPm;
    private final String mPrefKey;
    private UserManager mUserManager;

    public EmergencyBroadcastPreferenceController(Context context, String prefKey) {
        this(context, new AccountRestrictionHelper(context), prefKey);
    }

    @VisibleForTesting(otherwise = 5)
    EmergencyBroadcastPreferenceController(Context context, AccountRestrictionHelper helper, String prefKey) {
        super(context);
        this.mPrefKey = prefKey;
        this.mHelper = helper;
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mPm = this.mContext.getPackageManager();
    }

    public void updateState(Preference preference) {
        if (preference instanceof RestrictedPreference) {
            ((RestrictedPreference) preference).checkRestrictionAndSetDisabled("no_config_cell_broadcasts");
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    public String getPreferenceKey() {
        return this.mPrefKey;
    }

    public boolean isAvailable() {
        return this.mUserManager.isAdminUser() && isCellBroadcastAppLinkEnabled() && !this.mHelper.hasBaseUserRestriction("no_config_cell_broadcasts", UserHandle.myUserId());
    }

    private boolean isCellBroadcastAppLinkEnabled() {
        boolean enabled = this.mContext.getResources().getBoolean(17956915);
        if (!enabled) {
            return enabled;
        }
        try {
            if (this.mPm.getApplicationEnabledSetting("com.android.cellbroadcastreceiver") == 2) {
                return false;
            }
            return enabled;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
