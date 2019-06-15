package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.BatteryStats;
import android.os.BatteryStats.HistoryItem;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;
import android.util.SparseIntArray;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settings.R;
import com.android.settings.graph.UsageView;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.Utils;
import com.android.settingslib.utils.PowerUtil;
import com.android.settingslib.utils.StringUtil;

public class BatteryInfo {
    private static final String LOG_TAG = "BatteryInfo";
    public long averageTimeToDischarge = -1;
    public int batteryLevel;
    public String batteryPercentString;
    public CharSequence chargeLabel;
    public boolean discharging = true;
    private boolean mCharging;
    private BatteryStats mStats;
    public CharSequence remainingLabel;
    public long remainingTimeUs = 0;
    public String statusLabel;
    private long timePeriod;

    public interface BatteryDataParser {
        void onDataGap();

        void onDataPoint(long j, HistoryItem historyItem);

        void onParsingDone();

        void onParsingStarted(long j, long j2);
    }

    public interface Callback {
        void onBatteryInfoLoaded(BatteryInfo batteryInfo);
    }

    public void bindHistory(final UsageView view, BatteryDataParser... parsers) {
        final Context context = view.getContext();
        BatteryDataParser parser = new BatteryDataParser() {
            byte lastLevel;
            int lastTime = -1;
            SparseIntArray points = new SparseIntArray();
            long startTime;

            public void onParsingStarted(long startTime, long endTime) {
                this.startTime = startTime;
                BatteryInfo.this.timePeriod = endTime - startTime;
                view.clearPaths();
                view.configureGraph((int) BatteryInfo.this.timePeriod, 100);
            }

            public void onDataPoint(long time, HistoryItem record) {
                this.lastTime = (int) time;
                this.lastLevel = record.batteryLevel;
                this.points.put(this.lastTime, this.lastLevel);
            }

            public void onDataGap() {
                if (this.points.size() > 1) {
                    view.addPath(this.points);
                }
                this.points.clear();
            }

            public void onParsingDone() {
                onDataGap();
                if (BatteryInfo.this.remainingTimeUs != 0) {
                    PowerUsageFeatureProvider provider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
                    if (!BatteryInfo.this.mCharging && provider.isEnhancedBatteryPredictionEnabled(context)) {
                        this.points = provider.getEnhancedBatteryPredictionCurve(context, this.startTime);
                    } else if (this.lastTime >= 0) {
                        int i;
                        this.points.put(this.lastTime, this.lastLevel);
                        SparseIntArray sparseIntArray = this.points;
                        int access$000 = (int) (BatteryInfo.this.timePeriod + PowerUtil.convertUsToMs(BatteryInfo.this.remainingTimeUs));
                        if (BatteryInfo.this.mCharging) {
                            i = 100;
                        } else {
                            i = 0;
                        }
                        sparseIntArray.put(access$000, i);
                    }
                }
                if (this.points != null && this.points.size() > 0) {
                    view.configureGraph(this.points.keyAt(this.points.size() - 1), 100);
                    view.addProjectedPath(this.points);
                }
            }
        };
        BatteryDataParser[] parserList = new BatteryDataParser[(parsers.length + 1)];
        for (int i = 0; i < parsers.length; i++) {
            parserList[i] = parsers[i];
        }
        parserList[parsers.length] = parser;
        parse(this.mStats, parserList);
        String timeString = context.getString(R.string.charge_length_format, new Object[]{Formatter.formatShortElapsedTime(context, this.timePeriod)});
        String remaining = "";
        if (this.remainingTimeUs != 0) {
            remaining = context.getString(R.string.remaining_length_format, new Object[]{Formatter.formatShortElapsedTime(context, this.remainingTimeUs / 1000)});
        }
        view.setBottomLabels(new CharSequence[]{timeString, remaining});
    }

    public static void getBatteryInfo(Context context, Callback callback) {
        getBatteryInfo(context, callback, false);
    }

    public static void getBatteryInfo(Context context, Callback callback, boolean shortString) {
        long startTime = System.currentTimeMillis();
        BatteryStatsHelper statsHelper = new BatteryStatsHelper(context, true);
        statsHelper.create((Bundle) null);
        BatteryUtils.logRuntime(LOG_TAG, "time to make batteryStatsHelper", startTime);
        getBatteryInfo(context, callback, statsHelper, shortString);
    }

    public static void getBatteryInfo(Context context, Callback callback, BatteryStatsHelper statsHelper, boolean shortString) {
        long startTime = System.currentTimeMillis();
        BatteryStats stats = statsHelper.getStats();
        BatteryUtils.logRuntime(LOG_TAG, "time for getStats", startTime);
        getBatteryInfo(context, callback, stats, shortString);
    }

    public static void getBatteryInfo(final Context context, final Callback callback, final BatteryStats stats, final boolean shortString) {
        new AsyncTask<Void, Void, BatteryInfo>() {
            /* Access modifiers changed, original: protected|varargs */
            public BatteryInfo doInBackground(Void... params) {
                long startTime = System.currentTimeMillis();
                PowerUsageFeatureProvider provider = FeatureFactory.getFactory(context).getPowerUsageFeatureProvider(context);
                long elapsedRealtimeUs = PowerUtil.convertMsToUs(SystemClock.elapsedRealtime());
                Intent batteryBroadcast = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
                boolean discharging = batteryBroadcast.getIntExtra("plugged", -1) == 0;
                if (discharging && provider != null && provider.isEnhancedBatteryPredictionEnabled(context)) {
                    Estimate estimate = provider.getEnhancedBatteryPrediction(context);
                    if (estimate != null) {
                        BatteryUtils.logRuntime(BatteryInfo.LOG_TAG, "time for enhanced BatteryInfo", startTime);
                        return BatteryInfo.getBatteryInfo(context, batteryBroadcast, stats, estimate, elapsedRealtimeUs, shortString);
                    }
                }
                long prediction = discharging ? stats.computeBatteryTimeRemaining(elapsedRealtimeUs) : 0;
                Estimate estimate2 = new Estimate(PowerUtil.convertUsToMs(prediction), false, -1);
                BatteryUtils.logRuntime(BatteryInfo.LOG_TAG, "time for regular BatteryInfo", startTime);
                return BatteryInfo.getBatteryInfo(context, batteryBroadcast, stats, estimate2, elapsedRealtimeUs, shortString);
            }

            /* Access modifiers changed, original: protected */
            public void onPostExecute(BatteryInfo batteryInfo) {
                long startTime = System.currentTimeMillis();
                callback.onBatteryInfoLoaded(batteryInfo);
                BatteryUtils.logRuntime(BatteryInfo.LOG_TAG, "time for callback", startTime);
            }
        }.execute(new Void[0]);
    }

    @WorkerThread
    public static BatteryInfo getBatteryInfoOld(Context context, Intent batteryBroadcast, BatteryStats stats, long elapsedRealtimeUs, boolean shortString) {
        return getBatteryInfo(context, batteryBroadcast, stats, new Estimate(PowerUtil.convertUsToMs(stats.computeBatteryTimeRemaining(elapsedRealtimeUs)), false, -1), elapsedRealtimeUs, shortString);
    }

    @WorkerThread
    public static BatteryInfo getBatteryInfo(Context context, Intent batteryBroadcast, BatteryStats stats, Estimate estimate, long elapsedRealtimeUs, boolean shortString) {
        Intent intent = batteryBroadcast;
        Estimate estimate2 = estimate;
        long startTime = System.currentTimeMillis();
        BatteryInfo info = new BatteryInfo();
        BatteryStats batteryStats = stats;
        info.mStats = batteryStats;
        info.batteryLevel = Utils.getBatteryLevel(batteryBroadcast);
        info.batteryPercentString = Utils.formatPercentage(info.batteryLevel);
        boolean z = false;
        if (intent.getIntExtra("plugged", 0) != 0) {
            z = true;
        }
        info.mCharging = z;
        info.averageTimeToDischarge = estimate2.averageDischargeTime;
        info.statusLabel = Utils.getBatteryStatus(context.getResources(), intent);
        if (info.mCharging) {
            boolean z2 = shortString;
            updateBatteryInfoCharging(context, intent, batteryStats, elapsedRealtimeUs, info);
        } else {
            updateBatteryInfoDischarging(context, shortString, estimate2, info);
        }
        BatteryUtils.logRuntime(LOG_TAG, "time for getBatteryInfo", startTime);
        return info;
    }

    private static void updateBatteryInfoCharging(Context context, Intent batteryBroadcast, BatteryStats stats, long elapsedRealtimeUs, BatteryInfo info) {
        Context context2 = context;
        BatteryInfo batteryInfo = info;
        Resources resources = context2.getResources();
        long chargeTime = stats.computeChargeTimeRemaining(elapsedRealtimeUs);
        int status = batteryBroadcast.getIntExtra(NotificationCompat.CATEGORY_STATUS, 1);
        batteryInfo.discharging = false;
        if (chargeTime <= 0 || status == 5) {
            CharSequence charSequence;
            String chargeStatusLabel = resources.getString(R.string.battery_info_status_charging_lower);
            batteryInfo.remainingLabel = null;
            if (batteryInfo.batteryLevel == 100) {
                charSequence = batteryInfo.batteryPercentString;
            } else {
                charSequence = resources.getString(R.string.power_charging, new Object[]{batteryInfo.batteryPercentString, chargeStatusLabel});
            }
            batteryInfo.chargeLabel = charSequence;
            return;
        }
        batteryInfo.remainingTimeUs = chargeTime;
        CharSequence timeString = StringUtil.formatElapsedTime(context2, (double) PowerUtil.convertUsToMs(batteryInfo.remainingTimeUs), false);
        batteryInfo.remainingLabel = context2.getString(R.string.power_remaining_charging_duration_only, new Object[]{timeString});
        batteryInfo.chargeLabel = context2.getString(R.string.power_charging_duration, new Object[]{batteryInfo.batteryPercentString, timeString});
    }

    private static void updateBatteryInfoDischarging(Context context, boolean shortString, Estimate estimate, BatteryInfo info) {
        long drainTimeUs = PowerUtil.convertMsToUs(estimate.estimateMillis);
        if (drainTimeUs > 0) {
            info.remainingTimeUs = drainTimeUs;
            long convertUsToMs = PowerUtil.convertUsToMs(drainTimeUs);
            boolean z = false;
            boolean z2 = estimate.isBasedOnUsage && !shortString;
            info.remainingLabel = PowerUtil.getBatteryRemainingStringFormatted(context, convertUsToMs, null, z2);
            long convertUsToMs2 = PowerUtil.convertUsToMs(drainTimeUs);
            String str = info.batteryPercentString;
            if (estimate.isBasedOnUsage && !shortString) {
                z = true;
            }
            info.chargeLabel = PowerUtil.getBatteryRemainingStringFormatted(context, convertUsToMs2, str, z);
            return;
        }
        info.remainingLabel = null;
        info.chargeLabel = info.batteryPercentString;
    }

    /* JADX WARNING: Removed duplicated region for block: B:61:0x0120  */
    public static void parse(android.os.BatteryStats r44, com.android.settings.fuelgauge.BatteryInfo.BatteryDataParser... r45) {
        /*
        r0 = r44;
        r1 = r45;
        r2 = 0;
        r4 = 0;
        r6 = 0;
        r8 = 0;
        r10 = r2;
        r12 = 0;
        r14 = 0;
        r16 = 0;
        r17 = 0;
        r18 = 1;
        r19 = r44.startIteratingHistoryLocked();
        r20 = 0;
        r22 = r2;
        r3 = 5;
        if (r19 == 0) goto L_0x0073;
    L_0x0022:
        r2 = new android.os.BatteryStats$HistoryItem;
        r2.<init>();
    L_0x0027:
        r19 = r0.getNextHistoryLocked(r2);
        if (r19 == 0) goto L_0x0073;
    L_0x002d:
        r17 = r17 + 1;
        if (r18 == 0) goto L_0x0035;
    L_0x0031:
        r18 = 0;
        r6 = r2.time;
    L_0x0035:
        r24 = r4;
        r4 = r2.cmd;
        if (r4 == r3) goto L_0x0040;
    L_0x003b:
        r4 = r2.cmd;
        r5 = 7;
        if (r4 != r5) goto L_0x0066;
    L_0x0040:
        r4 = r2.currentTime;
        r26 = 15552000000; // 0x39ef8b000 float:-2.6330813E-20 double:7.683708924E-314;
        r26 = r12 + r26;
        r4 = (r4 > r26 ? 1 : (r4 == r26 ? 0 : -1));
        if (r4 > 0) goto L_0x0058;
    L_0x004d:
        r4 = r2.time;
        r26 = 300000; // 0x493e0 float:4.2039E-40 double:1.482197E-318;
        r26 = r6 + r26;
        r4 = (r4 > r26 ? 1 : (r4 == r26 ? 0 : -1));
        if (r4 >= 0) goto L_0x005a;
    L_0x0058:
        r22 = 0;
    L_0x005a:
        r12 = r2.currentTime;
        r14 = r2.time;
        r4 = (r22 > r20 ? 1 : (r22 == r20 ? 0 : -1));
        if (r4 != 0) goto L_0x0066;
    L_0x0062:
        r4 = r14 - r6;
        r22 = r12 - r4;
    L_0x0066:
        r4 = r2.isDeltaData();
        if (r4 == 0) goto L_0x0070;
    L_0x006c:
        r16 = r17;
        r8 = r2.time;
    L_0x0070:
        r4 = r24;
        goto L_0x0027;
    L_0x0073:
        r24 = r4;
        r4 = r22;
        r44.finishIteratingHistoryLocked();
        r22 = r12 + r8;
        r28 = r4;
        r3 = r22 - r14;
        r2 = 0;
        r5 = r16;
        r19 = 0;
        r22 = r19;
    L_0x0087:
        r30 = r22;
        r31 = r2;
        r2 = r1.length;
        r32 = r8;
        r8 = r30;
        if (r8 >= r2) goto L_0x00a4;
    L_0x0092:
        r2 = r1[r8];
        r34 = r10;
        r9 = r28;
        r2.onParsingStarted(r9, r3);
        r22 = r8 + 1;
        r2 = r31;
        r8 = r32;
        r10 = r34;
        goto L_0x0087;
    L_0x00a4:
        r34 = r10;
        r9 = r28;
        r2 = (r3 > r9 ? 1 : (r3 == r9 ? 0 : -1));
        if (r2 <= 0) goto L_0x0152;
    L_0x00ac:
        r2 = r44.startIteratingHistoryLocked();
        if (r2 == 0) goto L_0x0152;
    L_0x00b2:
        r2 = new android.os.BatteryStats$HistoryItem;
        r2.<init>();
        r8 = r31;
    L_0x00b9:
        r11 = r0.getNextHistoryLocked(r2);
        if (r11 == 0) goto L_0x0149;
    L_0x00bf:
        if (r8 >= r5) goto L_0x0149;
    L_0x00c1:
        r11 = r2.isDeltaData();
        if (r11 == 0) goto L_0x00f2;
    L_0x00c7:
        r36 = r3;
        r3 = r2.time;
        r3 = r3 - r14;
        r34 = r34 + r3;
        r3 = r2.time;
        r14 = r34 - r9;
        r11 = (r14 > r20 ? 1 : (r14 == r20 ? 0 : -1));
        if (r11 >= 0) goto L_0x00d8;
    L_0x00d6:
        r14 = 0;
    L_0x00d8:
        r11 = r19;
    L_0x00da:
        r38 = r3;
        r3 = r1.length;
        if (r11 >= r3) goto L_0x00e9;
    L_0x00df:
        r3 = r1[r11];
        r3.onDataPoint(r14, r2);
        r11 = r11 + 1;
        r3 = r38;
        goto L_0x00da;
        r40 = r5;
        r41 = r12;
        r14 = r38;
    L_0x00f0:
        r11 = 5;
        goto L_0x013f;
    L_0x00f2:
        r36 = r3;
        r3 = r34;
        r11 = r2.cmd;
        r40 = r5;
        r5 = 5;
        if (r11 == r5) goto L_0x0106;
    L_0x00fd:
        r5 = r2.cmd;
        r11 = 7;
        if (r5 != r11) goto L_0x0103;
    L_0x0102:
        goto L_0x0107;
    L_0x0103:
        r41 = r12;
        goto L_0x011b;
    L_0x0106:
        r11 = 7;
    L_0x0107:
        r41 = r12;
        r11 = r2.currentTime;
        r5 = (r11 > r9 ? 1 : (r11 == r9 ? 0 : -1));
        if (r5 < 0) goto L_0x0114;
    L_0x010f:
        r11 = r2.currentTime;
    L_0x0111:
        r34 = r11;
        goto L_0x0119;
    L_0x0114:
        r11 = r2.time;
        r11 = r11 - r6;
        r11 = r11 + r9;
        goto L_0x0111;
    L_0x0119:
        r14 = r2.time;
    L_0x011b:
        r5 = r2.cmd;
        r11 = 6;
        if (r5 == r11) goto L_0x00f0;
    L_0x0120:
        r5 = r2.cmd;
        r11 = 5;
        if (r5 != r11) goto L_0x0132;
    L_0x0125:
        r12 = r3 - r34;
        r12 = java.lang.Math.abs(r12);
        r22 = 3600000; // 0x36ee80 float:5.044674E-39 double:1.7786363E-317;
        r5 = (r12 > r22 ? 1 : (r12 == r22 ? 0 : -1));
        if (r5 <= 0) goto L_0x013f;
    L_0x0132:
        r5 = r19;
    L_0x0134:
        r12 = r1.length;
        if (r5 >= r12) goto L_0x013f;
    L_0x0137:
        r12 = r1[r5];
        r12.onDataGap();
        r5 = r5 + 1;
        goto L_0x0134;
    L_0x013f:
        r8 = r8 + 1;
        r3 = r36;
        r5 = r40;
        r12 = r41;
        goto L_0x00b9;
    L_0x0149:
        r36 = r3;
        r40 = r5;
        r41 = r12;
        r31 = r8;
        goto L_0x0158;
    L_0x0152:
        r36 = r3;
        r40 = r5;
        r41 = r12;
    L_0x0158:
        r44.finishIteratingHistoryLocked();
    L_0x015c:
        r2 = r19;
        r3 = r1.length;
        if (r2 >= r3) goto L_0x0169;
    L_0x0161:
        r3 = r1[r2];
        r3.onParsingDone();
        r19 = r2 + 1;
        goto L_0x015c;
    L_0x0169:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.BatteryInfo.parse(android.os.BatteryStats, com.android.settings.fuelgauge.BatteryInfo$BatteryDataParser[]):void");
    }
}
