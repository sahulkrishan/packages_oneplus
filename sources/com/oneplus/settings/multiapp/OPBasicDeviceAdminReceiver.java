package com.oneplus.settings.multiapp;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OPBasicDeviceAdminReceiver extends DeviceAdminReceiver {
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        Log.e(OPMultiAppListSettings.TAG, "onProfileProvisioningComplete");
        enableProfile(context);
    }

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), OPBasicDeviceAdminReceiver.class);
    }

    private void enableProfile(Context context) {
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService("device_policy");
        ComponentName componentName = getComponentName(context);
        manager.setProfileName(componentName, OPMultiAppListSettings.PROFILE_NAME);
        manager.setProfileEnabled(componentName);
    }
}
