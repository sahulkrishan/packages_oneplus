package com.android.settings.enterprise;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;

public class AlwaysOnVpnCurrentUserPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_ALWAYS_ON_VPN_PRIMARY_USER = "always_on_vpn_primary_user";
    private final EnterprisePrivacyFeatureProvider mFeatureProvider;

    public AlwaysOnVpnCurrentUserPreferenceController(Context context) {
        super(context);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context);
    }

    public void updateState(Preference preference) {
        int i;
        if (this.mFeatureProvider.isInCompMode()) {
            i = R.string.enterprise_privacy_always_on_vpn_personal;
        } else {
            i = R.string.enterprise_privacy_always_on_vpn_device;
        }
        preference.setTitle(i);
    }

    public boolean isAvailable() {
        return this.mFeatureProvider.isAlwaysOnVpnSetInCurrentUser();
    }

    public String getPreferenceKey() {
        return KEY_ALWAYS_ON_VPN_PRIMARY_USER;
    }
}
