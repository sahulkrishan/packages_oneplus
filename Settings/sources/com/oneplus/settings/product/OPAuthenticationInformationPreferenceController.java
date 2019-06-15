package com.oneplus.settings.product;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class OPAuthenticationInformationPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_AUTHENTICATION_INFORMATION = "oneplus_authentication_information";
    private static final String ONEPLUS_A5000 = "ONEPLUS A5000";
    private static final String ONEPLUS_A5010 = "ONEPLUS A5010";
    private static final String ONEPLUS_A6000 = "ONEPLUS A6000";
    private static final String ONEPLUS_A6003 = "ONEPLUS A6003";
    private Context mContext;

    public OPAuthenticationInformationPreferenceController(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isAvailable() {
        return false;
    }

    public String getPreferenceKey() {
        return KEY_AUTHENTICATION_INFORMATION;
    }
}
