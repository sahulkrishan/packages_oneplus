package com.android.settings.fuelgauge;

import com.android.settings.fuelgauge.BatteryInfo.Callback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BatteryHistoryPreference$OfN0YWKsw9YRrCqoEdP8dybAPU0 implements Callback {
    private final /* synthetic */ BatteryHistoryPreference f$0;

    public /* synthetic */ -$$Lambda$BatteryHistoryPreference$OfN0YWKsw9YRrCqoEdP8dybAPU0(BatteryHistoryPreference batteryHistoryPreference) {
        this.f$0 = batteryHistoryPreference;
    }

    public final void onBatteryInfoLoaded(BatteryInfo batteryInfo) {
        BatteryHistoryPreference.lambda$setStats$0(this.f$0, batteryInfo);
    }
}
