package com.android.settingslib.deviceinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public abstract class AbstractWifiMacAddressPreferenceController extends AbstractConnectivityPreferenceController {
    private static final String[] CONNECTIVITY_INTENTS = new String[]{"android.net.conn.CONNECTIVITY_CHANGE", "android.net.wifi.LINK_CONFIGURATION_CHANGED", "android.net.wifi.STATE_CHANGE"};
    @VisibleForTesting
    static final String KEY_WIFI_MAC_ADDRESS = "wifi_mac_address";
    private Preference mWifiMacAddress;
    private final WifiManager mWifiManager;

    public AbstractWifiMacAddressPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
        this.mWifiManager = (WifiManager) context.getSystemService(WifiManager.class);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_WIFI_MAC_ADDRESS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mWifiMacAddress = screen.findPreference(KEY_WIFI_MAC_ADDRESS);
        updateConnectivity();
    }

    /* Access modifiers changed, original: protected */
    public String[] getConnectivityIntents() {
        return CONNECTIVITY_INTENTS;
    }

    /* Access modifiers changed, original: protected */
    @SuppressLint({"HardwareIds"})
    public void updateConnectivity() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        int macRandomizationMode = Global.getInt(this.mContext.getContentResolver(), "wifi_connected_mac_randomization_enabled", 0);
        CharSequence macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        if (TextUtils.isEmpty(macAddress)) {
            this.mWifiMacAddress.setSummary(R.string.status_unavailable);
        } else if (macRandomizationMode == 1 && "02:00:00:00:00:00".equals(macAddress)) {
            this.mWifiMacAddress.setSummary(R.string.wifi_status_mac_randomized);
        } else {
            this.mWifiMacAddress.setSummary(macAddress);
        }
    }
}
