package com.android.settings.wifi;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiSummaryUpdater$5w1MXX8MJfsbMZcSIHVb0vJmaww implements Runnable {
    private final /* synthetic */ WifiSummaryUpdater f$0;

    public /* synthetic */ -$$Lambda$WifiSummaryUpdater$5w1MXX8MJfsbMZcSIHVb0vJmaww(WifiSummaryUpdater wifiSummaryUpdater) {
        this.f$0 = wifiSummaryUpdater;
    }

    public final void run() {
        this.f$0.notifyChangeIfNeeded();
    }
}
