package com.android.settings.enterprise;

import android.content.Context;

public class AdminGrantedCameraPermissionPreferenceController extends AdminGrantedPermissionsPreferenceControllerBase {
    private static final String KEY_ENTERPRISE_PRIVACY_NUMBER_CAMERA_ACCESS_PACKAGES = "enterprise_privacy_number_camera_access_packages";

    public AdminGrantedCameraPermissionPreferenceController(Context context, boolean async) {
        super(context, async, new String[]{"android.permission.CAMERA"});
    }

    public String getPreferenceKey() {
        return KEY_ENTERPRISE_PRIVACY_NUMBER_CAMERA_ACCESS_PACKAGES;
    }
}
