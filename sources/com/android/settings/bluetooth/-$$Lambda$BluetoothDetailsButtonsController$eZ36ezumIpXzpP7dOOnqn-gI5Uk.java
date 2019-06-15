package com.android.settings.bluetooth;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BluetoothDetailsButtonsController$eZ36ezumIpXzpP7dOOnqn-gI5Uk implements OnClickListener {
    private final /* synthetic */ BluetoothDetailsButtonsController f$0;

    public /* synthetic */ -$$Lambda$BluetoothDetailsButtonsController$eZ36ezumIpXzpP7dOOnqn-gI5Uk(BluetoothDetailsButtonsController bluetoothDetailsButtonsController) {
        this.f$0 = bluetoothDetailsButtonsController;
    }

    public final void onClick(View view) {
        this.f$0.mCachedDevice.connect(true);
    }
}
