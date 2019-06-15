package com.android.settings.bluetooth;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BluetoothNameDialogFragment$pGuotXbZkr5ej_7pdbB840goZcw implements OnClickListener {
    private final /* synthetic */ BluetoothNameDialogFragment f$0;

    public /* synthetic */ -$$Lambda$BluetoothNameDialogFragment$pGuotXbZkr5ej_7pdbB840goZcw(BluetoothNameDialogFragment bluetoothNameDialogFragment) {
        this.f$0 = bluetoothNameDialogFragment;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.setDeviceName(this.f$0.mDeviceNameView.getText().toString().trim());
    }
}
