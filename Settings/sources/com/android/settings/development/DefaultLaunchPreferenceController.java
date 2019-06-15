package com.android.settings.development;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class DefaultLaunchPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private final String mPreferenceKey;

    public DefaultLaunchPreferenceController(Context context, String preferenceKey) {
        super(context);
        this.mPreferenceKey = preferenceKey;
    }

    public String getPreferenceKey() {
        return this.mPreferenceKey;
    }
}
