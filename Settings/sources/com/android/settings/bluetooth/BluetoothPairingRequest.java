package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.UserHandle;

public final class BluetoothPairingRequest extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            Intent pairingIntent = BluetoothPairingService.getPairingDialogIntent(context, intent);
            PowerManager powerManager = (PowerManager) context.getSystemService("power");
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            String deviceName = null;
            String deviceAddress = device != null ? device.getAddress() : null;
            if (device != null) {
                deviceName = device.getName();
            }
            boolean shouldShowDialog = LocalBluetoothPreferences.shouldShowDialogInForeground(context, deviceAddress, deviceName);
            if (powerManager.isInteractive() && shouldShowDialog) {
                context.startActivityAsUser(pairingIntent, UserHandle.CURRENT);
            } else {
                intent.setClass(context, BluetoothPairingService.class);
                context.startServiceAsUser(intent, UserHandle.CURRENT);
            }
        }
    }
}
