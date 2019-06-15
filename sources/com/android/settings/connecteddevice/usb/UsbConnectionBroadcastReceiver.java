package com.android.settings.connecteddevice.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbPortStatus;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class UsbConnectionBroadcastReceiver extends BroadcastReceiver implements LifecycleObserver, OnResume, OnPause {
    private boolean mConnected;
    private Context mContext;
    private int mDataRole = 0;
    private long mFunctions = 0;
    private boolean mListeningToUsbEvents;
    private int mPowerRole = 0;
    private UsbBackend mUsbBackend;
    private UsbConnectionListener mUsbConnectionListener;

    interface UsbConnectionListener {
        void onUsbConnectionChanged(boolean z, long j, int i, int i2);
    }

    public UsbConnectionBroadcastReceiver(Context context, UsbConnectionListener usbConnectionListener, UsbBackend backend) {
        this.mContext = context;
        this.mUsbConnectionListener = usbConnectionListener;
        this.mUsbBackend = backend;
    }

    public void onReceive(Context context, Intent intent) {
        if ("android.hardware.usb.action.USB_STATE".equals(intent.getAction())) {
            boolean z = intent.getExtras().getBoolean("connected") || intent.getExtras().getBoolean("host_connected");
            this.mConnected = z;
            if (this.mConnected) {
                long functions = 0;
                if (intent.getExtras().getBoolean("mtp") && intent.getExtras().getBoolean("unlocked", false)) {
                    functions = 0 | 4;
                }
                if (intent.getExtras().getBoolean("ptp") && intent.getExtras().getBoolean("unlocked", false)) {
                    functions |= 16;
                }
                if (intent.getExtras().getBoolean("midi")) {
                    functions |= 8;
                }
                if (intent.getExtras().getBoolean("rndis")) {
                    functions |= 32;
                }
                this.mFunctions = functions;
                this.mDataRole = this.mUsbBackend.getDataRole();
                this.mPowerRole = this.mUsbBackend.getPowerRole();
            }
        } else if ("android.hardware.usb.action.USB_PORT_CHANGED".equals(intent.getAction())) {
            UsbPortStatus portStatus = (UsbPortStatus) intent.getExtras().getParcelable("portStatus");
            if (portStatus != null) {
                this.mDataRole = portStatus.getCurrentDataRole();
                this.mPowerRole = portStatus.getCurrentPowerRole();
            }
        }
        if (this.mUsbConnectionListener != null) {
            this.mUsbConnectionListener.onUsbConnectionChanged(this.mConnected, this.mFunctions, this.mPowerRole, this.mDataRole);
        }
    }

    public void register() {
        if (!this.mListeningToUsbEvents) {
            this.mConnected = false;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.hardware.usb.action.USB_STATE");
            intentFilter.addAction("android.hardware.usb.action.USB_PORT_CHANGED");
            Intent intent = this.mContext.registerReceiver(this, intentFilter);
            if (intent != null) {
                onReceive(this.mContext, intent);
            }
            this.mListeningToUsbEvents = true;
        }
    }

    public void unregister() {
        if (this.mListeningToUsbEvents) {
            this.mContext.unregisterReceiver(this);
            this.mListeningToUsbEvents = false;
        }
    }

    public boolean isConnected() {
        return this.mConnected;
    }

    public void onResume() {
        register();
    }

    public void onPause() {
        unregister();
    }
}
