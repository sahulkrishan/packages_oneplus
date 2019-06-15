package com.android.settings.wifi.tether;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.BidiFormatter;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.wifi.tether.WifiTetherSoftApManager.WifiTetherSoftApCallback;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class WifiTetherPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop {
    private static final IntentFilter AIRPLANE_INTENT_FILTER = new IntentFilter("android.intent.action.AIRPLANE_MODE");
    private static final int ID_NULL = -1;
    private static final String WIFI_TETHER_SETTINGS = "wifi_tether";
    private final ConnectivityManager mConnectivityManager;
    private final Lifecycle mLifecycle;
    @VisibleForTesting
    Preference mPreference;
    private final BroadcastReceiver mReceiver;
    private int mSoftApState;
    private final WifiManager mWifiManager;
    private final String[] mWifiRegexs;
    @VisibleForTesting
    WifiTetherSoftApManager mWifiTetherSoftApManager;

    public WifiTetherPreferenceController(Context context, Lifecycle lifecycle) {
        this(context, lifecycle, true);
    }

    @VisibleForTesting
    WifiTetherPreferenceController(Context context, Lifecycle lifecycle, boolean initSoftApManager) {
        super(context);
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                    WifiTetherPreferenceController.this.clearSummaryForAirplaneMode(R.string.wifi_hotspot_off_subtext);
                }
            }
        };
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mWifiRegexs = this.mConnectivityManager.getTetherableWifiRegexs();
        this.mLifecycle = lifecycle;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        if (initSoftApManager) {
            initWifiTetherSoftApManager();
        }
    }

    public boolean isAvailable() {
        return (this.mWifiRegexs == null || this.mWifiRegexs.length == 0 || Utils.isMonkeyRunning()) ? false : true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(WIFI_TETHER_SETTINGS);
        if (this.mPreference != null) {
        }
    }

    public String getPreferenceKey() {
        return WIFI_TETHER_SETTINGS;
    }

    public void onStart() {
        if (this.mPreference != null) {
            this.mContext.registerReceiver(this.mReceiver, AIRPLANE_INTENT_FILTER);
            clearSummaryForAirplaneMode();
            if (this.mWifiTetherSoftApManager != null) {
                this.mWifiTetherSoftApManager.registerSoftApCallback();
            }
        }
    }

    public void onStop() {
        if (this.mPreference != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            if (this.mWifiTetherSoftApManager != null) {
                this.mWifiTetherSoftApManager.unRegisterSoftApCallback();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initWifiTetherSoftApManager() {
        this.mWifiTetherSoftApManager = new WifiTetherSoftApManager(this.mWifiManager, new WifiTetherSoftApCallback() {
            public void onStateChanged(int state, int failureReason) {
                WifiTetherPreferenceController.this.mSoftApState = state;
                WifiTetherPreferenceController.this.handleWifiApStateChanged(state, failureReason);
            }

            public void onNumClientsChanged(int numClients) {
                if (WifiTetherPreferenceController.this.mPreference != null && WifiTetherPreferenceController.this.mSoftApState == 13) {
                    String extendWifiSummary;
                    if (WifiTetherPreferenceController.this.mWifiManager.isExtendingWifi()) {
                        extendWifiSummary = "Extending Wifi-Coverage: ";
                    } else {
                        extendWifiSummary = "";
                    }
                    Preference preference = WifiTetherPreferenceController.this.mPreference;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(extendWifiSummary);
                    stringBuilder.append(WifiTetherPreferenceController.this.mContext.getResources().getQuantityString(R.plurals.wifi_tether_connected_summary, numClients, new Object[]{Integer.valueOf(numClients)}));
                    preference.setSummary(stringBuilder.toString());
                }
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void handleWifiApStateChanged(int state, int reason) {
        switch (state) {
            case 10:
                this.mPreference.setSummary((int) R.string.wifi_tether_stopping);
                return;
            case 11:
                this.mPreference.setSummary((int) R.string.wifi_hotspot_off_subtext);
                clearSummaryForAirplaneMode();
                return;
            case 12:
                this.mPreference.setSummary((int) R.string.wifi_tether_starting);
                return;
            case 13:
                updateConfigSummary(this.mWifiManager.getWifiApConfiguration());
                return;
            default:
                if (reason == 1) {
                    this.mPreference.setSummary((int) R.string.wifi_sap_no_channel_error);
                } else {
                    this.mPreference.setSummary((int) R.string.wifi_error);
                }
                clearSummaryForAirplaneMode();
                return;
        }
    }

    private void updateConfigSummary(WifiConfiguration wifiConfig) {
        String str;
        String s = this.mContext.getString(17041179);
        Preference preference = this.mPreference;
        Context context = this.mContext;
        Object[] objArr = new Object[1];
        BidiFormatter instance = BidiFormatter.getInstance();
        if (wifiConfig == null) {
            str = s;
        } else {
            str = wifiConfig.SSID;
        }
        objArr[0] = instance.unicodeWrap(str);
        preference.setSummary(context.getString(R.string.wifi_tether_enabled_subtext, objArr));
    }

    private void clearSummaryForAirplaneMode() {
        clearSummaryForAirplaneMode(-1);
    }

    private void clearSummaryForAirplaneMode(int defaultId) {
        boolean z = false;
        if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            z = true;
        }
        if (z) {
            this.mPreference.setSummary((int) R.string.wifi_tether_disabled_by_airplane);
        } else if (defaultId != -1) {
            this.mPreference.setSummary(defaultId);
        }
    }
}
