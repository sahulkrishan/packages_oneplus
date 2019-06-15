package com.android.settings.fuelgauge.batterytip.detectors;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.settings.fuelgauge.batterytip.AppInfo.Builder;
import com.android.settings.fuelgauge.batterytip.BatteryDatabaseManager;
import com.android.settings.fuelgauge.batterytip.BatteryTipPolicy;
import com.android.settings.fuelgauge.batterytip.tips.AppLabelPredicate;
import com.android.settings.fuelgauge.batterytip.tips.AppRestrictionPredicate;
import com.android.settings.fuelgauge.batterytip.tips.BatteryTip;
import com.android.settings.fuelgauge.batterytip.tips.RestrictAppTip;
import com.oneplus.settings.timer.timepower.SettingsUtil;
import java.util.ArrayList;
import java.util.List;

public class RestrictAppDetector implements BatteryTipDetector {
    @VisibleForTesting
    static final boolean USE_FAKE_DATA = false;
    private AppLabelPredicate mAppLabelPredicate;
    private AppRestrictionPredicate mAppRestrictionPredicate;
    @VisibleForTesting
    BatteryDatabaseManager mBatteryDatabaseManager;
    private Context mContext;
    private BatteryTipPolicy mPolicy;

    public RestrictAppDetector(Context context, BatteryTipPolicy policy) {
        this.mContext = context;
        this.mPolicy = policy;
        this.mBatteryDatabaseManager = BatteryDatabaseManager.getInstance(context);
        this.mAppRestrictionPredicate = new AppRestrictionPredicate(context);
        this.mAppLabelPredicate = new AppLabelPredicate(context);
    }

    public BatteryTip detect() {
        int i = 2;
        if (!this.mPolicy.appRestrictionEnabled) {
            return new RestrictAppTip(2, new ArrayList());
        }
        long oneDayBeforeMs = System.currentTimeMillis() - SettingsUtil.MILLIS_OF_DAY;
        List highUsageApps = this.mBatteryDatabaseManager.queryAllAnomalies(oneDayBeforeMs, 0);
        highUsageApps.removeIf(this.mAppLabelPredicate.or(this.mAppRestrictionPredicate));
        if (!highUsageApps.isEmpty()) {
            return new RestrictAppTip(0, highUsageApps);
        }
        List autoHandledApps = this.mBatteryDatabaseManager.queryAllAnomalies(oneDayBeforeMs, 2);
        autoHandledApps.removeIf(this.mAppLabelPredicate.or(this.mAppRestrictionPredicate.negate()));
        if (!autoHandledApps.isEmpty()) {
            i = 1;
        }
        return new RestrictAppTip(i, autoHandledApps);
    }

    private BatteryTip getFakeData() {
        List highUsageApps = new ArrayList();
        highUsageApps.add(new Builder().setPackageName("com.android.settings").build());
        return new RestrictAppTip(0, highUsageApps);
    }
}
