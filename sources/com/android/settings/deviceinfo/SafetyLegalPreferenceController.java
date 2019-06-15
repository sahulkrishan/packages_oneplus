package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class SafetyLegalPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_SAFETY_LEGAL = "safetylegal";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";

    public SafetyLegalPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return TextUtils.isEmpty(SystemProperties.get(PROPERTY_URL_SAFETYLEGAL)) ^ 1;
    }

    public String getPreferenceKey() {
        return KEY_SAFETY_LEGAL;
    }
}
