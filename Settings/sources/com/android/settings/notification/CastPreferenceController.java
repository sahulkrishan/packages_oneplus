package com.android.settings.notification;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class CastPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_WIFI_DISPLAY = "wifi_display";

    public CastPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return KEY_WIFI_DISPLAY;
    }

    public boolean isAvailable() {
        return true;
    }
}
