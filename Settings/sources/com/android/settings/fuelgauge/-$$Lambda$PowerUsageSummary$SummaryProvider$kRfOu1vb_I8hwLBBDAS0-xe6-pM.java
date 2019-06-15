package com.android.settings.fuelgauge;

import com.android.settings.fuelgauge.BatteryBroadcastReceiver.OnBatteryChangedListener;
import com.android.settings.fuelgauge.BatteryInfo.Callback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$PowerUsageSummary$SummaryProvider$kRfOu1vb_I8hwLBBDAS0-xe6-pM implements OnBatteryChangedListener {
    private final /* synthetic */ SummaryProvider f$0;

    public /* synthetic */ -$$Lambda$PowerUsageSummary$SummaryProvider$kRfOu1vb_I8hwLBBDAS0-xe6-pM(SummaryProvider summaryProvider) {
        this.f$0 = summaryProvider;
    }

    public final void onBatteryChanged(int i) {
        BatteryInfo.getBatteryInfo(this.f$0.mContext, new Callback() {
            public void onBatteryInfoLoaded(BatteryInfo info) {
                SummaryProvider.this.mLoader.setSummary(SummaryProvider.this, PowerUsageSummary.getDashboardLabel(SummaryProvider.this.mContext, info));
            }
        }, true);
    }
}
