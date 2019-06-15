package com.android.settingslib.wifi;

import com.android.settingslib.wifi.WifiTracker.WifiListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$evcvquoPxZkPmBIit31UXvhXEJk implements Runnable {
    private final /* synthetic */ WifiListener f$0;

    public /* synthetic */ -$$Lambda$evcvquoPxZkPmBIit31UXvhXEJk(WifiListener wifiListener) {
        this.f$0 = wifiListener;
    }

    public final void run() {
        this.f$0.onAccessPointsChanged();
    }
}
