package com.android.settingslib.wifi;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiTracker$WifiListenerExecutor$PZBvWEzpVHhaI95PbZNbzEgAH1I implements Runnable {
    private final /* synthetic */ WifiListenerExecutor f$0;
    private final /* synthetic */ int f$1;

    public /* synthetic */ -$$Lambda$WifiTracker$WifiListenerExecutor$PZBvWEzpVHhaI95PbZNbzEgAH1I(WifiListenerExecutor wifiListenerExecutor, int i) {
        this.f$0 = wifiListenerExecutor;
        this.f$1 = i;
    }

    public final void run() {
        this.f$0.mDelegatee.onWifiStateChanged(this.f$1);
    }
}
