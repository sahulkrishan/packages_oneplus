package com.android.settings.bluetooth;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ConnectedBluetoothDeviceUpdater$cbcA1LEPXmJVOc_WhespijdA8X8 implements OnPreferenceClickListener {
    private final /* synthetic */ ConnectedBluetoothDeviceUpdater f$0;

    public /* synthetic */ -$$Lambda$ConnectedBluetoothDeviceUpdater$cbcA1LEPXmJVOc_WhespijdA8X8(ConnectedBluetoothDeviceUpdater connectedBluetoothDeviceUpdater) {
        this.f$0 = connectedBluetoothDeviceUpdater;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return this.f$0.launchDeviceDetails(preference);
    }
}
