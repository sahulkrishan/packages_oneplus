package com.android.settings.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import com.android.settingslib.bluetooth.A2dpProfile;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DeviceProfilesSettings$qBNrFA8-Smm3qyHXDkezO-CS7tQ implements OnClickListener {
    private final /* synthetic */ A2dpProfile f$0;
    private final /* synthetic */ BluetoothDevice f$1;
    private final /* synthetic */ CheckBox f$2;

    public /* synthetic */ -$$Lambda$DeviceProfilesSettings$qBNrFA8-Smm3qyHXDkezO-CS7tQ(A2dpProfile a2dpProfile, BluetoothDevice bluetoothDevice, CheckBox checkBox) {
        this.f$0 = a2dpProfile;
        this.f$1 = bluetoothDevice;
        this.f$2 = checkBox;
    }

    public final void onClick(View view) {
        this.f$0.setHighQualityAudioEnabled(this.f$1, this.f$2.isChecked());
    }
}
