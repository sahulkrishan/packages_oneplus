package com.android.settings.bluetooth;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BluetoothDetailsButtonsController$AbsgPn9bfqFfvfi3BgeGPbSW3X0 implements OnClickListener {
    private final /* synthetic */ BluetoothDetailsButtonsController f$0;

    public /* synthetic */ -$$Lambda$BluetoothDetailsButtonsController$AbsgPn9bfqFfvfi3BgeGPbSW3X0(BluetoothDetailsButtonsController bluetoothDetailsButtonsController) {
        this.f$0 = bluetoothDetailsButtonsController;
    }

    public final void onClick(View view) {
        this.f$0.mCachedDevice.disconnect();
    }
}
