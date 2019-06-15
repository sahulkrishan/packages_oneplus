package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.Intent;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class RegulatoryInfoPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final Intent INTENT_PROBE = new Intent("android.settings.SHOW_REGULATORY_INFO");
    private static final String KEY_REGULATORY_INFO = "regulatory_info";
    private static final String ONEPLUS_A5000 = "ONEPLUS A5000";
    private static final String ONEPLUS_A5010 = "ONEPLUS A5010";
    private static final String ONEPLUS_A6000 = "ONEPLUS A6000";
    private static final String ONEPLUS_A6003 = "ONEPLUS A6003";

    public RegulatoryInfoPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return false;
    }

    public String getPreferenceKey() {
        return KEY_REGULATORY_INFO;
    }
}
