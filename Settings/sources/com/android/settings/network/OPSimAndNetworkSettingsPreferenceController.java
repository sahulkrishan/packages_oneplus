package com.android.settings.network;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPSimAndNetworkSettingsPreferenceController extends BasePreferenceController implements LifecycleObserver {
    private static final String KEY_OP_SIM_CORD = "op_sim_cord";
    private final boolean mIsSecondaryUser = (this.mUserManager.isAdminUser() ^ 1);
    private Preference mPreference;
    private final UserManager mUserManager;

    public OPSimAndNetworkSettingsPreferenceController(Context context) {
        super(context, KEY_OP_SIM_CORD);
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public int getAvailabilityStatus() {
        return !isUserRestricted() ? 0 : 2;
    }

    public boolean isUserRestricted() {
        return this.mIsSecondaryUser || RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_config_mobile_networks", UserHandle.myUserId());
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_OP_SIM_CORD;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_OP_SIM_CORD.equals(preference.getKey())) {
            return false;
        }
        try {
            this.mContext.startActivity(new Intent("oneplus.intent.action.SIM_AND_NETWORK_SETTINGS"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
}
