package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.widget.SummaryUpdater;
import com.android.settings.widget.SummaryUpdater.OnSummaryChangeListener;
import com.android.settingslib.wifi.WifiStatusTracker;

public final class WifiSummaryUpdater extends SummaryUpdater {
    private static final IntentFilter INTENT_FILTER = new IntentFilter();
    private final BroadcastReceiver mReceiver;
    private final WifiStatusTracker mWifiTracker;

    static {
        INTENT_FILTER.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        INTENT_FILTER.addAction("android.net.wifi.STATE_CHANGE");
        INTENT_FILTER.addAction("android.net.wifi.RSSI_CHANGED");
    }

    public WifiSummaryUpdater(Context context, OnSummaryChangeListener listener) {
        this(context, listener, null);
    }

    @VisibleForTesting
    public WifiSummaryUpdater(Context context, OnSummaryChangeListener listener, WifiStatusTracker wifiTracker) {
        WifiStatusTracker wifiStatusTracker;
        super(context, listener);
        if (wifiTracker != null) {
            wifiStatusTracker = wifiTracker;
        } else {
            WifiStatusTracker wifiStatusTracker2 = new WifiStatusTracker(context, (WifiManager) context.getSystemService(WifiManager.class), (NetworkScoreManager) context.getSystemService(NetworkScoreManager.class), (ConnectivityManager) context.getSystemService(ConnectivityManager.class), new -$$Lambda$WifiSummaryUpdater$5w1MXX8MJfsbMZcSIHVb0vJmaww(this));
        }
        this.mWifiTracker = wifiStatusTracker;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WifiSummaryUpdater.this.mWifiTracker.handleBroadcast(intent);
                WifiSummaryUpdater.this.notifyChangeIfNeeded();
            }
        };
    }

    public void register(boolean register) {
        if (register) {
            this.mContext.registerReceiver(this.mReceiver, INTENT_FILTER);
        } else {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        this.mWifiTracker.setListening(register);
    }

    public String getSummary() {
        if (!this.mWifiTracker.enabled) {
            return this.mContext.getString(R.string.switch_off_text);
        }
        if (!this.mWifiTracker.connected) {
            return this.mContext.getString(R.string.disconnected);
        }
        String ssid = WifiInfo.removeDoubleQuotes(this.mWifiTracker.ssid);
        if (TextUtils.isEmpty(this.mWifiTracker.statusLabel)) {
            return ssid;
        }
        return this.mContext.getResources().getString(R.string.preference_summary_default_combination, new Object[]{ssid, this.mWifiTracker.statusLabel});
    }
}
