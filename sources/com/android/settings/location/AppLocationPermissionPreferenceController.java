package com.android.settings.location;

import android.content.Context;
import android.provider.Settings.Global;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class AppLocationPermissionPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_APP_LEVEL_PERMISSIONS = "app_level_permissions";

    public AppLocationPermissionPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return KEY_APP_LEVEL_PERMISSIONS;
    }

    public boolean isAvailable() {
        return Global.getInt(this.mContext.getContentResolver(), "location_settings_link_to_permissions_enabled", 1) == 1;
    }
}
