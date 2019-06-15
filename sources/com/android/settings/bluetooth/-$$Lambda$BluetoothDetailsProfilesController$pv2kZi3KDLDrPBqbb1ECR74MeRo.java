package com.android.settings.bluetooth;

import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settingslib.bluetooth.A2dpProfile;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BluetoothDetailsProfilesController$pv2kZi3KDLDrPBqbb1ECR74MeRo implements OnPreferenceClickListener {
    private final /* synthetic */ BluetoothDetailsProfilesController f$0;
    private final /* synthetic */ A2dpProfile f$1;

    public /* synthetic */ -$$Lambda$BluetoothDetailsProfilesController$pv2kZi3KDLDrPBqbb1ECR74MeRo(BluetoothDetailsProfilesController bluetoothDetailsProfilesController, A2dpProfile a2dpProfile) {
        this.f$0 = bluetoothDetailsProfilesController;
        this.f$1 = a2dpProfile;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return this.f$1.setHighQualityAudioEnabled(this.f$0.mCachedDevice.getDevice(), ((SwitchPreference) preference).isChecked());
    }
}
