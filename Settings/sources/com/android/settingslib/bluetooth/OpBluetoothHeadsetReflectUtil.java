package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

public class OpBluetoothHeadsetReflectUtil {
    static Object obj = null;

    public static boolean isOpBluetoothHeadset(Context context, BluetoothDevice device) {
        return BluetoothAdapter.getDefaultAdapter().isOpBluetoothHeadset(device);
    }
}
