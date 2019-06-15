package com.android.settings.fuelgauge.batterytip;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.fuelgauge.BatteryInfo;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.fuelgauge.batterytip.detectors.EarlyWarningDetector;
import com.android.settings.fuelgauge.batterytip.detectors.HighUsageDetector;
import com.android.settings.fuelgauge.batterytip.detectors.LowBatteryDetector;
import com.android.settings.fuelgauge.batterytip.detectors.RestrictAppDetector;
import com.android.settings.fuelgauge.batterytip.detectors.SmartBatteryDetector;
import com.android.settings.fuelgauge.batterytip.detectors.SummaryDetector;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.LowBatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.SummaryTip;
import com.android.settingslib.utils.AsyncLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BatteryTipLoader extends AsyncLoader<List<BatteryTip>> {
    private static final String TAG = "BatteryTipLoader";
    private static final boolean USE_FAKE_DATA = false;
    private BatteryStatsHelper mBatteryStatsHelper;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;

    public BatteryTipLoader(Context context, BatteryStatsHelper batteryStatsHelper) {
        super(context);
        this.mBatteryStatsHelper = batteryStatsHelper;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
    }

    public List<BatteryTip> loadInBackground() {
        List<BatteryTip> tips = new ArrayList();
        BatteryTipPolicy policy = new BatteryTipPolicy(getContext());
        BatteryInfo batteryInfo = this.mBatteryUtils.getBatteryInfo(this.mBatteryStatsHelper, TAG);
        Context context = getContext();
        tips.add(new LowBatteryDetector(context, policy, batteryInfo).detect());
        tips.add(new HighUsageDetector(context, policy, this.mBatteryStatsHelper, batteryInfo.discharging).detect());
        tips.add(new SmartBatteryDetector(policy, context.getContentResolver()).detect());
        tips.add(new EarlyWarningDetector(policy, context).detect());
        tips.add(new SummaryDetector(policy, batteryInfo.averageTimeToDischarge).detect());
        tips.add(new RestrictAppDetector(context, policy).detect());
        Collections.sort(tips);
        return tips;
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(List<BatteryTip> list) {
    }

    private List<BatteryTip> getFakeData() {
        List<BatteryTip> tips = new ArrayList();
        tips.add(new SummaryTip(0, -1));
        tips.add(new LowBatteryTip(0, false, "Fake data"));
        return tips;
    }
}
