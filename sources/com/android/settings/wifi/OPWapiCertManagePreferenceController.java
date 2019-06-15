package com.android.settings.wifi;

import android.content.Context;
import android.util.OpFeatures;
import com.android.settingslib.core.AbstractPreferenceController;

public class OPWapiCertManagePreferenceController extends AbstractPreferenceController {
    private static final String KEY_WAPI_CERT_MANAGE = "wapi_cert_manage";

    public OPWapiCertManagePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return 1 ^ OpFeatures.isSupport(new int[]{1});
    }

    public String getPreferenceKey() {
        return KEY_WAPI_CERT_MANAGE;
    }
}
