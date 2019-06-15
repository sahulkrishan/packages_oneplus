package com.android.settings.network;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$VpnPreferenceController$iDQ0RgxaDkCLoaHHZ6-UO2xSI_c implements Runnable {
    private final /* synthetic */ VpnPreferenceController f$0;
    private final /* synthetic */ String f$1;

    public /* synthetic */ -$$Lambda$VpnPreferenceController$iDQ0RgxaDkCLoaHHZ6-UO2xSI_c(VpnPreferenceController vpnPreferenceController, String str) {
        this.f$0 = vpnPreferenceController;
        this.f$1 = str;
    }

    public final void run() {
        this.f$0.mPreference.setSummary((CharSequence) this.f$1);
    }
}
