package com.android.settings.enterprise;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;

public class GlobalHttpProxyPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_GLOBAL_HTTP_PROXY = "global_http_proxy";
    private final EnterprisePrivacyFeatureProvider mFeatureProvider;

    public GlobalHttpProxyPreferenceController(Context context) {
        super(context);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context);
    }

    public boolean isAvailable() {
        return this.mFeatureProvider.isGlobalHttpProxySet();
    }

    public String getPreferenceKey() {
        return KEY_GLOBAL_HTTP_PROXY;
    }
}
