package com.android.settings.fuelgauge.batterytip.detectors;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.fuelgauge.BatteryInfo;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.fuelgauge.batterytip.AppInfo;
import com.android.settings.fuelgauge.batterytip.AppInfo.Builder;
import com.android.settings.fuelgauge.batterytip.BatteryTipPolicy;
import com.android.settings.fuelgauge.batterytip.HighUsageDataParser;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.HighUsageTip;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HighUsageDetector implements BatteryTipDetector {
    private BatteryStatsHelper mBatteryStatsHelper;
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    @VisibleForTesting
    HighUsageDataParser mDataParser;
    @VisibleForTesting
    boolean mDischarging;
    private List<AppInfo> mHighUsageAppList = new ArrayList();
    private BatteryTipPolicy mPolicy;

    public HighUsageDetector(Context context, BatteryTipPolicy policy, BatteryStatsHelper batteryStatsHelper, boolean discharging) {
        this.mPolicy = policy;
        this.mBatteryStatsHelper = batteryStatsHelper;
        this.mBatteryUtils = BatteryUtils.getInstance(context);
        this.mDataParser = new HighUsageDataParser(this.mPolicy.highUsagePeriodMs, this.mPolicy.highUsageBatteryDraining);
        this.mDischarging = discharging;
    }

    public BatteryTip detect() {
        long lastFullChargeTimeMs = this.mBatteryUtils.calculateLastFullChargeTime(this.mBatteryStatsHelper, System.currentTimeMillis());
        if (this.mPolicy.highUsageEnabled && this.mDischarging) {
            parseBatteryData();
            if (this.mDataParser.isDeviceHeavilyUsed() || this.mPolicy.testHighUsageTip) {
                List<BatterySipper> batterySippers = this.mBatteryStatsHelper.getUsageList();
                int size = batterySippers.size();
                for (int i = 0; i < size; i++) {
                    BatterySipper batterySipper = (BatterySipper) batterySippers.get(i);
                    if (!this.mBatteryUtils.shouldHideSipper(batterySipper)) {
                        long foregroundTimeMs = this.mBatteryUtils.getProcessTimeMs(1, batterySipper.uidObj, 0);
                        if (foregroundTimeMs >= 60000) {
                            this.mHighUsageAppList.add(new Builder().setUid(batterySipper.getUid()).setPackageName(this.mBatteryUtils.getPackageName(batterySipper.getUid())).setScreenOnTimeMs(foregroundTimeMs).build());
                        }
                    }
                }
                if (this.mPolicy.testHighUsageTip && this.mHighUsageAppList.isEmpty()) {
                    this.mHighUsageAppList.add(new Builder().setPackageName("com.android.settings").setScreenOnTimeMs(TimeUnit.HOURS.toMillis(3)).build());
                }
                Collections.sort(this.mHighUsageAppList, Collections.reverseOrder());
                this.mHighUsageAppList = this.mHighUsageAppList.subList(0, Math.min(this.mPolicy.highUsageAppCount, this.mHighUsageAppList.size()));
            }
        }
        return new HighUsageTip(lastFullChargeTimeMs, this.mHighUsageAppList);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void parseBatteryData() {
        BatteryInfo.parse(this.mBatteryStatsHelper.getStats(), this.mDataParser);
    }
}
