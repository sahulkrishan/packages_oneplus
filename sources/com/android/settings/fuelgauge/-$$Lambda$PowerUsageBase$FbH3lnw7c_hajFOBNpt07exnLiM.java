package com.android.settings.fuelgauge;

import com.android.settings.fuelgauge.BatteryBroadcastReceiver.OnBatteryChangedListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$PowerUsageBase$FbH3lnw7c_hajFOBNpt07exnLiM implements OnBatteryChangedListener {
    private final /* synthetic */ PowerUsageBase f$0;

    public /* synthetic */ -$$Lambda$PowerUsageBase$FbH3lnw7c_hajFOBNpt07exnLiM(PowerUsageBase powerUsageBase) {
        this.f$0 = powerUsageBase;
    }

    public final void onBatteryChanged(int i) {
        this.f$0.restartBatteryStatsLoader(i);
    }
}
