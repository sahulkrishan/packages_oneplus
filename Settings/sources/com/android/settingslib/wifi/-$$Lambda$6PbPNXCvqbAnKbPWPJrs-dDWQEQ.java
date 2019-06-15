package com.android.settingslib.wifi;

import com.android.settingslib.wifi.WifiTracker.WifiListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$6PbPNXCvqbAnKbPWPJrs-dDWQEQ implements Runnable {
    private final /* synthetic */ WifiListener f$0;

    public /* synthetic */ -$$Lambda$6PbPNXCvqbAnKbPWPJrs-dDWQEQ(WifiListener wifiListener) {
        this.f$0 = wifiListener;
    }

    public final void run() {
        this.f$0.onConnectedChanged();
    }
}
