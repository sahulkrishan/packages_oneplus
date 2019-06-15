package com.android.settings.bluetooth;

public class BluetoothLengthDeviceNameFilter extends Utf8ByteLengthFilter {
    private static final int BLUETOOTH_NAME_MAX_LENGTH_BYTES = 248;

    public BluetoothLengthDeviceNameFilter() {
        super(BLUETOOTH_NAME_MAX_LENGTH_BYTES);
    }
}
