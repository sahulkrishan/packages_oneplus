package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Global;
import android.support.v4.text.BidiFormatter;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class WifiInfoPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {
    private static final String KEY_CURRENT_IP_ADDRESS = "current_ip_address";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.LINK_CONFIGURATION_CHANGED") || action.equals("android.net.wifi.STATE_CHANGE")) {
                WifiInfoPreferenceController.this.updateWifiInfo();
            }
        }
    };
    private Preference mWifiIpAddressPref;
    private Preference mWifiMacAddressPref;
    private final WifiManager mWifiManager;

    public WifiInfoPreferenceController(Context context, Lifecycle lifecycle, WifiManager wifiManager) {
        super(context);
        this.mWifiManager = wifiManager;
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.LINK_CONFIGURATION_CHANGED");
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        lifecycle.addObserver(this);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return null;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mWifiMacAddressPref = screen.findPreference(KEY_MAC_ADDRESS);
        this.mWifiMacAddressPref.setSelectable(false);
        this.mWifiIpAddressPref = screen.findPreference("current_ip_address");
        this.mWifiIpAddressPref.setSelectable(false);
    }

    public void onResume() {
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
        updateWifiInfo();
    }

    public void onPause() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public void updateWifiInfo() {
        if (this.mWifiMacAddressPref != null) {
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            int macRandomizationMode = Global.getInt(this.mContext.getContentResolver(), "wifi_connected_mac_randomization_enabled", 0);
            CharSequence macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
            if (TextUtils.isEmpty(macAddress)) {
                this.mWifiMacAddressPref.setSummary((int) R.string.status_unavailable);
            } else if (macRandomizationMode == 1 && "02:00:00:00:00:00".equals(macAddress)) {
                this.mWifiMacAddressPref.setSummary((int) R.string.wifi_status_mac_randomized);
            } else {
                this.mWifiMacAddressPref.setSummary(macAddress);
            }
        }
        if (this.mWifiIpAddressPref != null) {
            CharSequence string;
            String ipAddress = Utils.getWifiIpAddresses(this.mContext);
            Preference preference = this.mWifiIpAddressPref;
            if (ipAddress == null) {
                string = this.mContext.getString(R.string.status_unavailable);
            } else {
                string = BidiFormatter.getInstance().unicodeWrap(ipAddress);
            }
            preference.setSummary(string);
        }
    }
}
