package com.android.settings.fuelgauge;

import android.os.BatteryStats;
import com.android.settings.fuelgauge.BatteryInfo.Callback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BatteryHistoryChart$O1ddbx0WxFm_LlbjYiwVyWtFoUA implements Callback {
    private final /* synthetic */ BatteryHistoryChart f$0;
    private final /* synthetic */ BatteryStats f$1;

    public /* synthetic */ -$$Lambda$BatteryHistoryChart$O1ddbx0WxFm_LlbjYiwVyWtFoUA(BatteryHistoryChart batteryHistoryChart, BatteryStats batteryStats) {
        this.f$0 = batteryHistoryChart;
        this.f$1 = batteryStats;
    }

    public final void onBatteryInfoLoaded(BatteryInfo batteryInfo) {
        BatteryHistoryChart.lambda$setStats$0(this.f$0, this.f$1, batteryInfo);
    }
}
