package com.android.settings.bluetooth;

import com.android.settings.widget.GearPreference;
import com.android.settings.widget.GearPreference.OnGearClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BluetoothDeviceUpdater$9cHgqnTeqRHSfH6f9TvykmwcB28 implements OnGearClickListener {
    private final /* synthetic */ BluetoothDeviceUpdater f$0;

    public /* synthetic */ -$$Lambda$BluetoothDeviceUpdater$9cHgqnTeqRHSfH6f9TvykmwcB28(BluetoothDeviceUpdater bluetoothDeviceUpdater) {
        this.f$0 = bluetoothDeviceUpdater;
    }

    public final void onGearClick(GearPreference gearPreference) {
        this.f$0.launchDeviceDetails(gearPreference);
    }
}
