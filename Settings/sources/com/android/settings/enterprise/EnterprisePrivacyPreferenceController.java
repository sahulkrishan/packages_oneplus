package com.android.settings.enterprise;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;

public class EnterprisePrivacyPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_ENTERPRISE_PRIVACY = "enterprise_privacy";
    private final EnterprisePrivacyFeatureProvider mFeatureProvider;

    public EnterprisePrivacyPreferenceController(Context context) {
        super(context);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context);
    }

    public void updateState(Preference preference) {
        if (preference != null) {
            if (this.mFeatureProvider.getDeviceOwnerOrganizationName() == null) {
                preference.setSummary((int) R.string.enterprise_privacy_settings_summary_generic);
            } else {
                preference.setSummary(this.mContext.getResources().getString(R.string.enterprise_privacy_settings_summary_with_name, new Object[]{organizationName}));
            }
        }
    }

    public boolean isAvailable() {
        return this.mFeatureProvider.hasDeviceOwner();
    }

    public String getPreferenceKey() {
        return KEY_ENTERPRISE_PRIVACY;
    }
}
