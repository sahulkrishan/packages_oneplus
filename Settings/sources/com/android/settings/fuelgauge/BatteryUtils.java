package com.android.settings.fuelgauge;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.BatteryStats;
import android.os.BatteryStats.Timer;
import android.os.BatteryStats.Uid;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.support.v17.leanback.media.MediaPlayerGlue;
import android.util.Log;
import android.util.SparseLongArray;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatterySipper.DrainType;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.fuelgauge.batterytip.AnomalyInfo;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.fuelgauge.PowerWhitelistBackend;
import com.android.settingslib.utils.PowerUtil;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BatteryUtils {
    private static final int MIN_POWER_THRESHOLD_MILLI_AMP = 5;
    public static final int SDK_NULL = -1;
    private static final int SECONDS_IN_HOUR = 3600;
    private static final String TAG = "BatteryUtils";
    public static final int UID_NULL = -1;
    private static BatteryUtils sInstance;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private PackageManager mPackageManager;
    @VisibleForTesting
    PowerUsageFeatureProvider mPowerUsageFeatureProvider;

    @Retention(RetentionPolicy.SOURCE)
    public @interface StatusType {
        public static final int ALL = 3;
        public static final int BACKGROUND = 2;
        public static final int FOREGROUND = 1;
        public static final int SCREEN_USAGE = 0;
    }

    public static BatteryUtils getInstance(Context context) {
        if (sInstance == null || sInstance.isDataCorrupted()) {
            sInstance = new BatteryUtils(context);
        }
        return sInstance;
    }

    @VisibleForTesting
    BatteryUtils(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPackageManager = context.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mPowerUsageFeatureProvider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
    }

    public long getProcessTimeMs(int type, @Nullable Uid uid, int which) {
        if (uid == null) {
            return 0;
        }
        switch (type) {
            case 0:
                return getScreenUsageTimeMs(uid, which);
            case 1:
                return getProcessForegroundTimeMs(uid, which);
            case 2:
                return getProcessBackgroundTimeMs(uid, which);
            case 3:
                return getProcessForegroundTimeMs(uid, which) + getProcessBackgroundTimeMs(uid, which);
            default:
                return 0;
        }
    }

    private long getScreenUsageTimeMs(Uid uid, int which, long rawRealTimeUs) {
        int[] foregroundTypes = new int[1];
        int i = 0;
        foregroundTypes[0] = 0;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("package: ");
        stringBuilder.append(this.mPackageManager.getNameForUid(uid.getUid()));
        Log.v(str, stringBuilder.toString());
        long timeUs = 0;
        int length = foregroundTypes.length;
        while (i < length) {
            int type = foregroundTypes[i];
            long localTime = uid.getProcessStateTime(type, rawRealTimeUs, which);
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("type: ");
            stringBuilder2.append(type);
            stringBuilder2.append(" time(us): ");
            stringBuilder2.append(localTime);
            Log.v(str2, stringBuilder2.toString());
            timeUs += localTime;
            i++;
        }
        String str3 = TAG;
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("foreground time(us): ");
        stringBuilder3.append(timeUs);
        Log.v(str3, stringBuilder3.toString());
        return PowerUtil.convertUsToMs(Math.min(timeUs, getForegroundActivityTotalTimeUs(uid, rawRealTimeUs)));
    }

    private long getScreenUsageTimeMs(Uid uid, int which) {
        return getScreenUsageTimeMs(uid, which, PowerUtil.convertMsToUs(SystemClock.elapsedRealtime()));
    }

    private long getProcessBackgroundTimeMs(Uid uid, int which) {
        long timeUs = uid.getProcessStateTime(3, PowerUtil.convertMsToUs(SystemClock.elapsedRealtime()), which);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("package: ");
        stringBuilder.append(this.mPackageManager.getNameForUid(uid.getUid()));
        Log.v(str, stringBuilder.toString());
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("background time(us): ");
        stringBuilder.append(timeUs);
        Log.v(str, stringBuilder.toString());
        return PowerUtil.convertUsToMs(timeUs);
    }

    private long getProcessForegroundTimeMs(Uid uid, int which) {
        long rawRealTimeUs = PowerUtil.convertMsToUs(SystemClock.elapsedRealtime());
        return getScreenUsageTimeMs(uid, which, rawRealTimeUs) + PowerUtil.convertUsToMs(getForegroundServiceTotalTimeUs(uid, rawRealTimeUs));
    }

    public double removeHiddenBatterySippers(List<BatterySipper> sippers) {
        double proportionalSmearPowerMah = 0.0d;
        BatterySipper screenSipper = null;
        for (int i = sippers.size() - 1; i >= 0; i--) {
            BatterySipper sipper = (BatterySipper) sippers.get(i);
            if (shouldHideSipper(sipper)) {
                sippers.remove(i);
                if (!(sipper.drainType == DrainType.OVERCOUNTED || sipper.drainType == DrainType.SCREEN || sipper.drainType == DrainType.UNACCOUNTED || sipper.drainType == DrainType.BLUETOOTH || sipper.drainType == DrainType.WIFI || sipper.drainType == DrainType.IDLE)) {
                    proportionalSmearPowerMah += sipper.totalPowerMah;
                }
            }
            if (sipper.drainType == DrainType.SCREEN) {
                screenSipper = sipper;
            }
        }
        smearScreenBatterySipper(sippers, screenSipper);
        return proportionalSmearPowerMah;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void smearScreenBatterySipper(List<BatterySipper> sippers, BatterySipper screenSipper) {
        List list = sippers;
        BatterySipper batterySipper = screenSipper;
        long totalActivityTimeMs = 0;
        SparseLongArray activityTimeArray = new SparseLongArray();
        int size = sippers.size();
        for (int i = 0; i < size; i++) {
            Uid uid = ((BatterySipper) list.get(i)).uidObj;
            if (uid != null) {
                long timeMs = getProcessTimeMs(0, uid, 0);
                activityTimeArray.put(uid.getUid(), timeMs);
                totalActivityTimeMs += timeMs;
            }
        }
        if (totalActivityTimeMs >= 600000) {
            if (batterySipper == null) {
                Log.e(TAG, "screen sipper is null even when app screen time is not zero");
                return;
            }
            double screenPowerMah = batterySipper.totalPowerMah;
            int i2 = 0;
            int size2 = sippers.size();
            while (i2 < size2) {
                BatterySipper sipper = (BatterySipper) list.get(i2);
                sipper.totalPowerMah += (((double) activityTimeArray.get(sipper.getUid(), 0)) * screenPowerMah) / ((double) totalActivityTimeMs);
                i2++;
                List<BatterySipper> list2 = sippers;
                batterySipper = screenSipper;
            }
        }
    }

    public boolean shouldHideSipper(BatterySipper sipper) {
        DrainType drainType = sipper.drainType;
        return drainType == DrainType.IDLE || drainType == DrainType.CELL || drainType == DrainType.SCREEN || drainType == DrainType.UNACCOUNTED || drainType == DrainType.OVERCOUNTED || drainType == DrainType.BLUETOOTH || drainType == DrainType.WIFI || sipper.totalPowerMah * 3600.0d < 5.0d || this.mPowerUsageFeatureProvider.isTypeService(sipper) || this.mPowerUsageFeatureProvider.isTypeSystem(sipper);
    }

    public double calculateBatteryPercent(double powerUsageMah, double totalPowerMah, double hiddenPowerMah, int dischargeAmount) {
        if (totalPowerMah == 0.0d) {
            return 0.0d;
        }
        return (powerUsageMah / (totalPowerMah - hiddenPowerMah)) * ((double) dischargeAmount);
    }

    public long calculateRunningTimeBasedOnStatsType(BatteryStatsHelper batteryStatsHelper, int statsType) {
        return PowerUtil.convertUsToMs(batteryStatsHelper.getStats().computeBatteryRealtime(PowerUtil.convertMsToUs(SystemClock.elapsedRealtime()), statsType));
    }

    public String getPackageName(int uid) {
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        return ArrayUtils.isEmpty(packageNames) ? null : packageNames[0];
    }

    public int getTargetSdkVersion(String packageName) {
        try {
            return this.mPackageManager.getApplicationInfo(packageName, 128).targetSdkVersion;
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find package: ");
            stringBuilder.append(packageName);
            Log.e(str, stringBuilder.toString(), e);
            return -1;
        }
    }

    public boolean isBackgroundRestrictionEnabled(int targetSdkVersion, int uid, String packageName) {
        boolean z = true;
        if (targetSdkVersion >= 26) {
            return true;
        }
        int mode = this.mAppOpsManager.checkOpNoThrow(64, uid, packageName);
        if (!(mode == 1 || mode == 2)) {
            z = false;
        }
        return z;
    }

    public void sortUsageList(List<BatterySipper> usageList) {
        Collections.sort(usageList, new Comparator<BatterySipper>() {
            public int compare(BatterySipper a, BatterySipper b) {
                return Double.compare(b.totalPowerMah, a.totalPowerMah);
            }
        });
    }

    public long calculateLastFullChargeTime(BatteryStatsHelper batteryStatsHelper, long currentTimeMs) {
        return currentTimeMs - batteryStatsHelper.getStats().getStartClockTime();
    }

    public long calculateScreenUsageTime(BatteryStatsHelper batteryStatsHelper) {
        BatterySipper sipper = findBatterySipperByType(batteryStatsHelper.getUsageList(), DrainType.SCREEN);
        return sipper != null ? sipper.usageTimeMs : 0;
    }

    public static void logRuntime(String tag, String message, long startTime) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(message);
        stringBuilder.append(": ");
        stringBuilder.append(System.currentTimeMillis() - startTime);
        stringBuilder.append("ms");
        Log.d(tag, stringBuilder.toString());
    }

    public int getPackageUid(String packageName) {
        int i = -1;
        if (packageName != null) {
            try {
                i = this.mPackageManager.getPackageUid(packageName, 128);
            } catch (NameNotFoundException e) {
                return -1;
            }
        }
        return i;
    }

    @StringRes
    public int getSummaryResIdFromAnomalyType(int type) {
        switch (type) {
            case 0:
                return R.string.battery_abnormal_wakelock_summary;
            case 1:
                return R.string.battery_abnormal_wakeup_alarm_summary;
            case 2:
                return R.string.battery_abnormal_location_summary;
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Incorrect anomaly type: ");
                stringBuilder.append(type);
                throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public void setForceAppStandby(int uid, String packageName, int mode) {
        if (isPreOApp(packageName)) {
            this.mAppOpsManager.setMode(64, uid, packageName, mode);
        }
        this.mAppOpsManager.setMode(78, uid, packageName, mode);
    }

    public boolean isForceAppStandbyEnabled(int uid, String packageName) {
        return this.mAppOpsManager.checkOpNoThrow(78, uid, packageName) == 1;
    }

    public void initBatteryStatsHelper(BatteryStatsHelper statsHelper, Bundle bundle, UserManager userManager) {
        statsHelper.create(bundle);
        statsHelper.clearStats();
        statsHelper.refreshStats(0, userManager.getUserProfiles());
    }

    @WorkerThread
    public BatteryInfo getBatteryInfo(BatteryStatsHelper statsHelper, String tag) {
        Estimate enhancedBatteryPrediction;
        long startTime = System.currentTimeMillis();
        Intent batteryBroadcast = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        long elapsedRealtimeUs = PowerUtil.convertMsToUs(SystemClock.elapsedRealtime());
        BatteryStats stats = statsHelper.getStats();
        if (this.mPowerUsageFeatureProvider == null || !this.mPowerUsageFeatureProvider.isEnhancedBatteryPredictionEnabled(this.mContext)) {
            Estimate estimate = new Estimate(PowerUtil.convertUsToMs(stats.computeBatteryTimeRemaining(elapsedRealtimeUs)), false, -1);
        } else {
            enhancedBatteryPrediction = this.mPowerUsageFeatureProvider.getEnhancedBatteryPrediction(this.mContext);
        }
        Estimate estimate2 = enhancedBatteryPrediction;
        logRuntime(tag, "BatteryInfoLoader post query", startTime);
        BatteryInfo batteryInfo = BatteryInfo.getBatteryInfo(this.mContext, batteryBroadcast, stats, estimate2, elapsedRealtimeUs, false);
        logRuntime(tag, "BatteryInfoLoader.loadInBackground", startTime);
        return batteryInfo;
    }

    public BatterySipper findBatterySipperByType(List<BatterySipper> usageList, DrainType type) {
        int size = usageList.size();
        for (int i = 0; i < size; i++) {
            BatterySipper sipper = (BatterySipper) usageList.get(i);
            if (sipper.drainType == type) {
                return sipper;
            }
        }
        return null;
    }

    private boolean isDataCorrupted() {
        return this.mPackageManager == null || this.mAppOpsManager == null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public long getForegroundActivityTotalTimeUs(Uid uid, long rawRealtimeUs) {
        Timer timer = uid.getForegroundActivityTimer();
        if (timer != null) {
            return timer.getTotalTimeLocked(rawRealtimeUs, 0);
        }
        return 0;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public long getForegroundServiceTotalTimeUs(Uid uid, long rawRealtimeUs) {
        Timer timer = uid.getForegroundServiceTimer();
        if (timer != null) {
            return timer.getTotalTimeLocked(rawRealtimeUs, 0);
        }
        return 0;
    }

    public boolean isPreOApp(String packageName) {
        boolean z = false;
        try {
            if (this.mPackageManager.getApplicationInfo(packageName, 128).targetSdkVersion < 26) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find package: ");
            stringBuilder.append(packageName);
            Log.e(str, stringBuilder.toString(), e);
            return false;
        }
    }

    public boolean isPreOApp(String[] packageNames) {
        if (ArrayUtils.isEmpty(packageNames)) {
            return false;
        }
        for (String packageName : packageNames) {
            if (isPreOApp(packageName)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldHideAnomaly(PowerWhitelistBackend powerWhitelistBackend, int uid, AnomalyInfo anomalyInfo) {
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        boolean z = true;
        if (ArrayUtils.isEmpty(packageNames)) {
            return true;
        }
        if (!(isSystemUid(uid) || powerWhitelistBackend.isWhitelisted(packageNames) || ((isSystemApp(this.mPackageManager, packageNames) && !hasLauncherEntry(packageNames)) || (isExcessiveBackgroundAnomaly(anomalyInfo) && !isPreOApp(packageNames))))) {
            z = false;
        }
        return z;
    }

    private boolean isExcessiveBackgroundAnomaly(AnomalyInfo anomalyInfo) {
        return anomalyInfo.anomalyType.intValue() == 4;
    }

    private boolean isSystemUid(int uid) {
        int appUid = UserHandle.getAppId(uid);
        return appUid >= 0 && appUid < MediaPlayerGlue.FAST_FORWARD_REWIND_STEP;
    }

    private boolean isSystemApp(PackageManager packageManager, String[] packageNames) {
        int length = packageNames.length;
        int i = 0;
        while (i < length) {
            String packageName = packageNames[i];
            try {
                if ((packageManager.getApplicationInfo(packageName, 0).flags & 1) != 0) {
                    return true;
                }
                i++;
            } catch (NameNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Package not found: ");
                stringBuilder.append(packageName);
                Log.e(str, stringBuilder.toString(), e);
            }
        }
        return false;
    }

    private boolean hasLauncherEntry(String[] packageNames) {
        Intent launchIntent = new Intent("android.intent.action.MAIN", null);
        launchIntent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> resolveInfos = this.mPackageManager.queryIntentActivities(launchIntent, 1835520);
        int size = resolveInfos.size();
        for (int i = 0; i < size; i++) {
            if (ArrayUtils.contains(packageNames, ((ResolveInfo) resolveInfos.get(i)).activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    public long getAppLongVersionCode(String packageName) {
        try {
            return this.mPackageManager.getPackageInfo(packageName, 0).getLongVersionCode();
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot find package: ");
            stringBuilder.append(packageName);
            Log.e(str, stringBuilder.toString(), e);
            return -1;
        }
    }
}
