package com.android.settings.enterprise;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;

public class ManageDeviceAdminPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_MANAGE_DEVICE_ADMIN = "manage_device_admin";
    private final EnterprisePrivacyFeatureProvider mFeatureProvider;

    public ManageDeviceAdminPreferenceController(Context context) {
        super(context);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context);
    }

    public void updateState(Preference preference) {
        CharSequence string;
        int activeAdmins = this.mFeatureProvider.getNumberOfActiveDeviceAdminsForCurrentUserAndManagedProfile();
        if (activeAdmins == 0) {
            string = this.mContext.getResources().getString(R.string.number_of_device_admins_none);
        } else {
            string = this.mContext.getResources().getQuantityString(R.plurals.number_of_device_admins, activeAdmins, new Object[]{Integer.valueOf(activeAdmins)});
        }
        preference.setSummary(string);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_manage_device_admin);
    }

    public String getPreferenceKey() {
        return KEY_MANAGE_DEVICE_ADMIN;
    }
}
