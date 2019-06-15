package com.android.settings.wifi.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class WifiP2pPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {
    private static final String KEY_WIFI_DIRECT = "wifi_direct";
    private final IntentFilter mFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
    @VisibleForTesting
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            WifiP2pPreferenceController.this.togglePreferences();
        }
    };
    private Preference mWifiDirectPref;
    private final WifiManager mWifiManager;

    public WifiP2pPreferenceController(Context context, Lifecycle lifecycle, WifiManager wifiManager) {
        super(context);
        this.mWifiManager = wifiManager;
        lifecycle.addObserver(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mWifiDirectPref = screen.findPreference(KEY_WIFI_DIRECT);
        togglePreferences();
    }

    public void onResume() {
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
    }

    public void onPause() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_WIFI_DIRECT;
    }

    private void togglePreferences() {
        if (this.mWifiDirectPref != null) {
            this.mWifiDirectPref.setEnabled(this.mWifiManager.isWifiEnabled());
        }
    }
}
