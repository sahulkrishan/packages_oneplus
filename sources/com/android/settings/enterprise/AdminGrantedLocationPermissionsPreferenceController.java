package com.android.settings.enterprise;

import android.content.Context;

public class AdminGrantedLocationPermissionsPreferenceController extends AdminGrantedPermissionsPreferenceControllerBase {
    private static final String KEY_ENTERPRISE_PRIVACY_NUMBER_LOCATION_ACCESS_PACKAGES = "enterprise_privacy_number_location_access_packages";

    public AdminGrantedLocationPermissionsPreferenceController(Context context, boolean async) {
        super(context, async, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"});
    }

    public String getPreferenceKey() {
        return KEY_ENTERPRISE_PRIVACY_NUMBER_LOCATION_ACCESS_PACKAGES;
    }
}
