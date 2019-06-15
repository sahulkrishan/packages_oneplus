package com.android.settings.bluetooth;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BluetoothPairingDialogFragment$r7iz4I0mbAZSn1y-rbFsqcyiwC0 implements OnCheckedChangeListener {
    private final /* synthetic */ BluetoothPairingDialogFragment f$0;

    public /* synthetic */ -$$Lambda$BluetoothPairingDialogFragment$r7iz4I0mbAZSn1y-rbFsqcyiwC0(BluetoothPairingDialogFragment bluetoothPairingDialogFragment) {
        this.f$0 = bluetoothPairingDialogFragment;
    }

    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        BluetoothPairingDialogFragment.lambda$createPinEntryView$1(this.f$0, compoundButton, z);
    }
}
