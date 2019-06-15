package com.android.settings.wifi.tether;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController.OnTetherConfigUpdateListener;
import com.oneplus.settings.utils.OPUtils;

public class OPWifiTetherDeviceManagerController extends WifiTetherBasePreferenceController {
    public static final String PREF_KEY = "connected_device_manager";
    private static final String TAG = "OPWifiTetherDeviceManagerController";

    public OPWifiTetherDeviceManagerController(Context context, OnTetherConfigUpdateListener listener) {
        super(context, listener, PREF_KEY);
    }

    public void updateDisplay() {
        this.mPreference.setVisible(isAvailable());
    }

    public int getAvailabilityStatus() {
        boolean state = OPUtils.isAppPakExist(this.mContext, "com.oneplus.wifiapsettings") && this.mWifiManager != null && this.mWifiManager.getWifiApState() == 13;
        if (state) {
            return 0;
        }
        return 1;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!PREF_KEY.equals(preference.getKey())) {
            return false;
        }
        OPUtils.isAppPakExist(this.mContext, "com.oneplus.wifiapsettings");
        try {
            this.mContext.startActivity(new Intent("android.oem.intent.action.OPWIFIAP_SETTINGS"));
        } catch (ActivityNotFoundException e) {
        }
        return true;
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }
}
