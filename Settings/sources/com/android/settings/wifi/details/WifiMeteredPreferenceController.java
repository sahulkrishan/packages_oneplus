package com.android.settings.wifi.details;

import android.app.backup.BackupManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class WifiMeteredPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener, LifecycleObserver, OnPause, OnResume {
    private static final String KEY_WIFI_METERED = "metered";
    private DropDownPreference mDropDownPreference;
    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Object obj = (action.hashCode() == 1625920338 && action.equals("android.net.wifi.CONFIGURED_NETWORKS_CHANGE")) ? null : -1;
            if (obj == null) {
                WifiConfiguration wifiConfiguration = (WifiConfiguration) intent.getParcelableExtra("wifiConfiguration");
                WifiMeteredPreferenceController.this.mDropDownPreference.setValue(Integer.toString(wifiConfiguration.meteredOverride));
                WifiMeteredPreferenceController.this.updateSummary(WifiMeteredPreferenceController.this.mDropDownPreference, wifiConfiguration.meteredOverride);
            }
        }
    };
    private WifiConfiguration mWifiConfiguration;
    private WifiManager mWifiManager;

    public WifiMeteredPreferenceController(Context context, WifiConfiguration wifiConfiguration, Lifecycle lifecycle) {
        super(context, KEY_WIFI_METERED);
        this.mWifiConfiguration = wifiConfiguration;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        lifecycle.addObserver(this);
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mDropDownPreference = (DropDownPreference) screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_WIFI_METERED;
    }

    public void onResume() {
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
    }

    public void onPause() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public void updateState(Preference preference) {
        DropDownPreference dropDownPreference = (DropDownPreference) preference;
        int meteredOverride = getMeteredOverride();
        dropDownPreference.setValue(Integer.toString(meteredOverride));
        updateSummary(dropDownPreference, meteredOverride);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mWifiConfiguration != null) {
            this.mWifiConfiguration.meteredOverride = Integer.parseInt((String) newValue);
        }
        this.mWifiManager.updateNetwork(this.mWifiConfiguration);
        BackupManager.dataChanged("com.android.providers.settings");
        updateSummary((DropDownPreference) preference, getMeteredOverride());
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getMeteredOverride() {
        if (this.mWifiConfiguration != null) {
            return this.mWifiConfiguration.meteredOverride;
        }
        return 0;
    }

    private void updateSummary(DropDownPreference preference, int meteredOverride) {
        preference.setSummary(preference.getEntries()[meteredOverride]);
    }
}
