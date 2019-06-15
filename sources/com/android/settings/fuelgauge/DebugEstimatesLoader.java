package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryStats;
import android.os.SystemClock;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.utils.AsyncLoader;
import com.android.settingslib.utils.PowerUtil;
import java.util.ArrayList;
import java.util.List;

public class DebugEstimatesLoader extends AsyncLoader<List<BatteryInfo>> {
    private BatteryStatsHelper mStatsHelper;

    public DebugEstimatesLoader(Context context, BatteryStatsHelper statsHelper) {
        super(context);
        this.mStatsHelper = statsHelper;
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(List<BatteryInfo> list) {
    }

    public List<BatteryInfo> loadInBackground() {
        Estimate estimate;
        Context context = getContext();
        PowerUsageFeatureProvider powerUsageFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
        long elapsedRealtimeUs = PowerUtil.convertMsToUs(SystemClock.elapsedRealtime());
        Intent batteryBroadcast = getContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        BatteryStats stats = this.mStatsHelper.getStats();
        BatteryInfo oldinfo = BatteryInfo.getBatteryInfoOld(getContext(), batteryBroadcast, stats, elapsedRealtimeUs, false);
        Estimate estimate2 = powerUsageFeatureProvider.getEnhancedBatteryPrediction(context);
        if (estimate2 == null) {
            Estimate estimate3 = new Estimate(0, false, -1);
            estimate2 = estimate3;
        } else {
            estimate = estimate2;
        }
        BatteryInfo newInfo = BatteryInfo.getBatteryInfo(getContext(), batteryBroadcast, stats, estimate, elapsedRealtimeUs, false);
        List<BatteryInfo> infos = new ArrayList();
        infos.add(oldinfo);
        infos.add(newInfo);
        return infos;
    }
}
