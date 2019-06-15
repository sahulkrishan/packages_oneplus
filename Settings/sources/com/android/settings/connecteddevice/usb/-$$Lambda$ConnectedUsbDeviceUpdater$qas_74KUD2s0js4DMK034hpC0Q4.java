package com.android.settings.connecteddevice.usb;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ConnectedUsbDeviceUpdater$qas_74KUD2s0js4DMK034hpC0Q4 implements OnPreferenceClickListener {
    private final /* synthetic */ ConnectedUsbDeviceUpdater f$0;

    public /* synthetic */ -$$Lambda$ConnectedUsbDeviceUpdater$qas_74KUD2s0js4DMK034hpC0Q4(ConnectedUsbDeviceUpdater connectedUsbDeviceUpdater) {
        this.f$0 = connectedUsbDeviceUpdater;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return new SubSettingLauncher(this.f$0.mFragment.getContext()).setDestination(UsbDetailsFragment.class.getName()).setTitle((int) R.string.device_details_title).setSourceMetricsCategory(this.f$0.mFragment.getMetricsCategory()).launch();
    }
}
