package com.android.settings.wifi;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiConfiguration;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class WifiUtils {
    private static final int PASSWORD_MAX_LENGTH = 63;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int SSID_ASCII_MAX_LENGTH = 32;
    private static final int SSID_ASCII_MIN_LENGTH = 1;

    public static boolean isSSIDTooLong(String ssid) {
        boolean z = false;
        if (TextUtils.isEmpty(ssid)) {
            return false;
        }
        if (ssid.getBytes().length > 32) {
            z = true;
        }
        return z;
    }

    public static boolean isSSIDTooShort(String ssid) {
        boolean z = true;
        if (TextUtils.isEmpty(ssid)) {
            return true;
        }
        if (ssid.length() >= 1) {
            z = false;
        }
        return z;
    }

    public static boolean isHotspotPasswordValid(String password) {
        boolean z = false;
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        int length = password.length();
        int lengthBytes = password.getBytes().length;
        if (length >= 8 && lengthBytes <= 63) {
            z = true;
        }
        return z;
    }

    public static boolean isNetworkLockedDown(Context context, WifiConfiguration config) {
        boolean isLockdownFeatureEnabled = false;
        if (config == null) {
            return false;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService("device_policy");
        PackageManagerWrapper pm = new PackageManagerWrapper(context.getPackageManager());
        if (pm.hasSystemFeature("android.software.device_admin") && dpm == null) {
            return true;
        }
        boolean isConfigEligibleForLockdown = false;
        if (dpm != null) {
            ComponentName deviceOwner = dpm.getDeviceOwnerComponentOnAnyUser();
            if (deviceOwner != null) {
                try {
                    isConfigEligibleForLockdown = pm.getPackageUidAsUser(deviceOwner.getPackageName(), dpm.getDeviceOwnerUserId()) == config.creatorUid;
                } catch (NameNotFoundException e) {
                }
            }
        }
        if (!isConfigEligibleForLockdown) {
            return false;
        }
        if (Global.getInt(context.getContentResolver(), "wifi_device_owner_configs_lockdown", 0) != 0) {
            isLockdownFeatureEnabled = true;
        }
        return isLockdownFeatureEnabled;
    }

    public static boolean canSignIntoNetwork(NetworkCapabilities capabilities) {
        return capabilities != null && capabilities.hasCapability(17);
    }

    public static boolean isSupportDualBand() {
        String propDualband = SystemProperties.get("persist.vendor.wifi.softap.dualband", "0");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Dualband:");
        stringBuilder.append(propDualband);
        Log.i("WifiUtils", stringBuilder.toString());
        return propDualband.equals("1");
    }
}
