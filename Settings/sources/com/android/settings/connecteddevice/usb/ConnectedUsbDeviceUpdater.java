package com.android.settings.connecteddevice.usb;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.dashboard.DashboardFragment;

public class ConnectedUsbDeviceUpdater {
    private DevicePreferenceCallback mDevicePreferenceCallback;
    private DashboardFragment mFragment;
    private UsbBackend mUsbBackend;
    @VisibleForTesting
    UsbConnectionListener mUsbConnectionListener;
    @VisibleForTesting
    Preference mUsbPreference;
    @VisibleForTesting
    UsbConnectionBroadcastReceiver mUsbReceiver;

    public static /* synthetic */ void lambda$new$0(ConnectedUsbDeviceUpdater connectedUsbDeviceUpdater, boolean connected, long functions, int powerRole, int dataRole) {
        if (connected) {
            connectedUsbDeviceUpdater.mUsbPreference.setSummary(getSummary(connectedUsbDeviceUpdater.mUsbBackend.getCurrentFunctions(), connectedUsbDeviceUpdater.mUsbBackend.getPowerRole()));
            connectedUsbDeviceUpdater.mDevicePreferenceCallback.onDeviceAdded(connectedUsbDeviceUpdater.mUsbPreference);
            return;
        }
        connectedUsbDeviceUpdater.mDevicePreferenceCallback.onDeviceRemoved(connectedUsbDeviceUpdater.mUsbPreference);
    }

    public ConnectedUsbDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback) {
        this(context, fragment, devicePreferenceCallback, new UsbBackend(context));
    }

    @VisibleForTesting
    ConnectedUsbDeviceUpdater(Context context, DashboardFragment fragment, DevicePreferenceCallback devicePreferenceCallback, UsbBackend usbBackend) {
        this.mUsbConnectionListener = new -$$Lambda$ConnectedUsbDeviceUpdater$8_8ZhYJMgn-zGVqi-7esENaXwOM(this);
        this.mFragment = fragment;
        this.mDevicePreferenceCallback = devicePreferenceCallback;
        this.mUsbBackend = usbBackend;
        this.mUsbReceiver = new UsbConnectionBroadcastReceiver(context, this.mUsbConnectionListener, this.mUsbBackend);
    }

    public void registerCallback() {
        this.mUsbReceiver.register();
    }

    public void unregisterCallback() {
        this.mUsbReceiver.unregister();
    }

    public void initUsbPreference(Context context) {
        this.mUsbPreference = new Preference(context, null);
        this.mUsbPreference.setTitle((int) R.string.usb_pref);
        this.mUsbPreference.setIcon((int) R.drawable.ic_usb);
        this.mUsbPreference.setOnPreferenceClickListener(new -$$Lambda$ConnectedUsbDeviceUpdater$qas_74KUD2s0js4DMK034hpC0Q4(this));
        forceUpdate();
    }

    private void forceUpdate() {
        this.mUsbReceiver.register();
    }

    public static int getSummary(long functions, int power) {
        switch (power) {
            case 1:
                if (functions == 4) {
                    return R.string.usb_summary_file_transfers_power;
                }
                if (functions == 32) {
                    return R.string.usb_summary_tether_power;
                }
                if (functions == 16) {
                    return R.string.usb_summary_photo_transfers_power;
                }
                if (functions == 8) {
                    return R.string.usb_summary_MIDI_power;
                }
                return R.string.usb_summary_power_only;
            case 2:
                if (functions == 4) {
                    return R.string.usb_summary_file_transfers;
                }
                if (functions == 32) {
                    return R.string.usb_summary_tether;
                }
                if (functions == 16) {
                    return R.string.usb_summary_photo_transfers;
                }
                if (functions == 8) {
                    return R.string.usb_summary_MIDI;
                }
                return R.string.usb_summary_charging_only;
            default:
                return R.string.usb_summary_charging_only;
        }
    }
}
