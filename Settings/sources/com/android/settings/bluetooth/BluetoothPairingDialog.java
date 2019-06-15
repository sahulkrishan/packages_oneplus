package com.android.settings.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;

public class BluetoothPairingDialog extends Activity {
    public static final String FRAGMENT_TAG = "bluetooth.pairing.fragment";
    private BluetoothPairingController mBluetoothPairingController;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(action)) {
                int bondState = intent.getIntExtra("android.bluetooth.device.extra.BOND_STATE", Integer.MIN_VALUE);
                if (bondState == 12 || bondState == 10) {
                    BluetoothPairingDialog.this.dismiss();
                }
            } else if ("android.bluetooth.device.action.PAIRING_CANCEL".equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (device == null || BluetoothPairingDialog.this.mBluetoothPairingController.deviceEquals(device)) {
                    BluetoothPairingDialog.this.dismiss();
                }
            }
        }
    };
    private boolean mReceiverRegistered = false;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBluetoothPairingController = new BluetoothPairingController(getIntent(), this);
        boolean fragmentFound = true;
        BluetoothPairingDialogFragment bluetoothFragment = (BluetoothPairingDialogFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (bluetoothFragment != null && (bluetoothFragment.isPairingControllerSet() || bluetoothFragment.isPairingDialogActivitySet())) {
            bluetoothFragment.dismiss();
            bluetoothFragment = null;
        }
        if (bluetoothFragment == null) {
            fragmentFound = false;
            bluetoothFragment = new BluetoothPairingDialogFragment();
        }
        bluetoothFragment.setPairingController(this.mBluetoothPairingController);
        bluetoothFragment.setPairingDialogActivity(this);
        if (!fragmentFound) {
            bluetoothFragment.show(getFragmentManager(), FRAGMENT_TAG);
        }
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.PAIRING_CANCEL"));
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
        this.mReceiverRegistered = true;
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.mReceiverRegistered) {
            this.mReceiverRegistered = false;
            unregisterReceiver(this.mReceiver);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }
}
