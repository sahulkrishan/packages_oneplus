package com.android.settings.enterprise;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;

public class AlwaysOnVpnManagedProfilePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_ALWAYS_ON_VPN_MANAGED_PROFILE = "always_on_vpn_managed_profile";
    private final EnterprisePrivacyFeatureProvider mFeatureProvider;

    public AlwaysOnVpnManagedProfilePreferenceController(Context context) {
        super(context);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context);
    }

    public boolean isAvailable() {
        return this.mFeatureProvider.isAlwaysOnVpnSetInManagedProfile();
    }

    public String getPreferenceKey() {
        return KEY_ALWAYS_ON_VPN_MANAGED_PROFILE;
    }
}
