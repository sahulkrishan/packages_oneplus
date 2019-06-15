package com.android.settings.enterprise;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;

public class ImePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_INPUT_METHOD = "input_method";
    private final EnterprisePrivacyFeatureProvider mFeatureProvider;

    public ImePreferenceController(Context context) {
        super(context);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context);
    }

    public void updateState(Preference preference) {
        preference.setSummary(this.mContext.getResources().getString(R.string.enterprise_privacy_input_method_name, new Object[]{this.mFeatureProvider.getImeLabelIfOwnerSet()}));
    }

    public boolean isAvailable() {
        return this.mFeatureProvider.getImeLabelIfOwnerSet() != null;
    }

    public String getPreferenceKey() {
        return KEY_INPUT_METHOD;
    }
}
