package com.android.settings.wifi.tether;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.SoftApCallback;
import android.os.Handler;

public class WifiTetherSoftApManager {
    private Handler mHandler;
    private SoftApCallback mSoftApCallback = new SoftApCallback() {
        public void onStateChanged(int state, int failureReason) {
            WifiTetherSoftApManager.this.mWifiTetherSoftApCallback.onStateChanged(state, failureReason);
        }

        public void onNumClientsChanged(int numClients) {
        }

        public void onStaConnected(String Macaddr, int numClients) {
            WifiTetherSoftApManager.this.mWifiTetherSoftApCallback.onNumClientsChanged(numClients);
        }

        public void onStaDisconnected(String Macaddr, int numClients) {
            WifiTetherSoftApManager.this.mWifiTetherSoftApCallback.onNumClientsChanged(numClients);
        }
    };
    private WifiManager mWifiManager;
    private WifiTetherSoftApCallback mWifiTetherSoftApCallback;

    public interface WifiTetherSoftApCallback {
        void onNumClientsChanged(int i);

        void onStateChanged(int i, int i2);
    }

    WifiTetherSoftApManager(WifiManager wifiManager, WifiTetherSoftApCallback wifiTetherSoftApCallback) {
        this.mWifiManager = wifiManager;
        this.mWifiTetherSoftApCallback = wifiTetherSoftApCallback;
        this.mHandler = new Handler();
    }

    public void registerSoftApCallback() {
        this.mWifiManager.registerSoftApCallback(this.mSoftApCallback, this.mHandler);
    }

    public void unRegisterSoftApCallback() {
        this.mWifiManager.unregisterSoftApCallback(this.mSoftApCallback);
    }
}
