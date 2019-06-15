package com.android.settings.fuelgauge;

import com.android.settings.fuelgauge.BatteryInfo.Callback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BatteryHistoryDetail$ZIvw_m8MPrnAuz9tJSzFmSFxa_8 implements Callback {
    private final /* synthetic */ BatteryHistoryDetail f$0;

    public /* synthetic */ -$$Lambda$BatteryHistoryDetail$ZIvw_m8MPrnAuz9tJSzFmSFxa_8(BatteryHistoryDetail batteryHistoryDetail) {
        this.f$0 = batteryHistoryDetail;
    }

    public final void onBatteryInfoLoaded(BatteryInfo batteryInfo) {
        BatteryHistoryDetail.lambda$updateEverything$0(this.f$0, batteryInfo);
    }
}
