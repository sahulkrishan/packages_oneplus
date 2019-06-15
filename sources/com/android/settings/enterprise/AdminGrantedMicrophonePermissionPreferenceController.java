package com.android.settings.enterprise;

import android.content.Context;

public class AdminGrantedMicrophonePermissionPreferenceController extends AdminGrantedPermissionsPreferenceControllerBase {
    private static final String KEY_ENTERPRISE_PRIVACY_NUMBER_MICROPHONE_ACCESS_PACKAGES = "enterprise_privacy_number_microphone_access_packages";

    public AdminGrantedMicrophonePermissionPreferenceController(Context context, boolean async) {
        super(context, async, new String[]{"android.permission.RECORD_AUDIO"});
    }

    public String getPreferenceKey() {
        return KEY_ENTERPRISE_PRIVACY_NUMBER_MICROPHONE_ACCESS_PACKAGES;
    }
}
