package com.android.settings.wifi.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class P2pThisDevicePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private Preference mPreference;

    public P2pThisDevicePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return "p2p_this_device";
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public void setEnabled(boolean enabled) {
        if (this.mPreference != null) {
            this.mPreference.setEnabled(enabled);
        }
    }

    public void updateDeviceName(WifiP2pDevice thisDevice) {
        String mDeviceName = System.getString(this.mContext.getContentResolver(), "oem_oneplus_devicename");
        CharSequence mDeviceName2 = OPUtils.resetDeviceNameIfInvalid(this.mContext);
        if (mDeviceName2.length() > 32) {
            mDeviceName2 = mDeviceName2.substring(0, 31);
            System.putString(this.mContext.getContentResolver(), "oem_oneplus_devicename", mDeviceName2);
        }
        this.mPreference.setTitle(mDeviceName2);
        if (!thisDevice.deviceName.equals(mDeviceName2)) {
            WifiP2pManager mWifiP2pManager = (WifiP2pManager) this.mContext.getSystemService("wifip2p");
            Channel mChannel = mWifiP2pManager.initialize(this.mContext, this.mContext.getMainLooper(), null);
            if (mWifiP2pManager != null) {
                mWifiP2pManager.setDeviceName(mChannel, mDeviceName2, null);
            }
        }
    }
}
