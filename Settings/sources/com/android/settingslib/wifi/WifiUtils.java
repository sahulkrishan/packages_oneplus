package com.android.settingslib.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.net.wifi.WifiInfo;
import android.support.annotation.VisibleForTesting;
import com.android.settingslib.R;
import java.util.Map;

public class WifiUtils {
    public static String buildLoggingSummary(AccessPoint accessPoint, WifiConfiguration config) {
        StringBuilder stringBuilder;
        StringBuilder stringBuilder2;
        StringBuilder summary = new StringBuilder();
        WifiInfo info = accessPoint.getInfo();
        if (accessPoint.isActive() && info != null) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(" f=");
            stringBuilder.append(Integer.toString(info.getFrequency()));
            summary.append(stringBuilder.toString());
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append(" ");
        stringBuilder.append(getVisibilityStatus(accessPoint));
        summary.append(stringBuilder.toString());
        if (!(config == null || config.getNetworkSelectionStatus().isNetworkEnabled())) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(" (");
            stringBuilder.append(config.getNetworkSelectionStatus().getNetworkStatusString());
            summary.append(stringBuilder.toString());
            if (config.getNetworkSelectionStatus().getDisableTime() > 0) {
                long diff = (System.currentTimeMillis() - config.getNetworkSelectionStatus().getDisableTime()) / 1000;
                long sec = diff % 60;
                long min = (diff / 60) % 60;
                long hour = (min / 60) % 60;
                summary.append(", ");
                if (hour > 0) {
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(Long.toString(hour));
                    stringBuilder2.append("h ");
                    summary.append(stringBuilder2.toString());
                }
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append(Long.toString(min));
                stringBuilder2.append("m ");
                summary.append(stringBuilder2.toString());
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append(Long.toString(sec));
                stringBuilder2.append("s ");
                summary.append(stringBuilder2.toString());
            }
            summary.append(")");
        }
        if (config != null) {
            NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
            for (int index = 0; index < 14; index++) {
                if (networkStatus.getDisableReasonCounter(index) != 0) {
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(" ");
                    stringBuilder2.append(NetworkSelectionStatus.getNetworkDisableReasonString(index));
                    stringBuilder2.append("=");
                    stringBuilder2.append(networkStatus.getDisableReasonCounter(index));
                    summary.append(stringBuilder2.toString());
                }
            }
        }
        return summary.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:60:0x015f  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01b0  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x015f  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01b0  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x015f  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01b0  */
    @android.support.annotation.VisibleForTesting
    static java.lang.String getVisibilityStatus(com.android.settingslib.wifi.AccessPoint r21) {
        /*
        r1 = r21;
        r2 = r21.getInfo();
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r3 = r0;
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r4 = r0;
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r5 = r0;
        r0 = 0;
        r6 = r21.isActive();
        if (r6 == 0) goto L_0x00aa;
    L_0x001f:
        if (r2 == 0) goto L_0x00aa;
    L_0x0021:
        r0 = r2.getBSSID();
        if (r0 == 0) goto L_0x002f;
    L_0x0027:
        r6 = " ";
        r3.append(r6);
        r3.append(r0);
    L_0x002f:
        r6 = " rssi=";
        r3.append(r6);
        r6 = r2.getRssi();
        r3.append(r6);
        r6 = " ";
        r3.append(r6);
        r6 = " score=";
        r3.append(r6);
        r6 = r2.score;
        r3.append(r6);
        r6 = r21.getSpeed();
        if (r6 == 0) goto L_0x005c;
    L_0x0050:
        r6 = " speed=";
        r3.append(r6);
        r6 = r21.getSpeedLabel();
        r3.append(r6);
    L_0x005c:
        r6 = " tx=%.1f,";
        r7 = 1;
        r8 = new java.lang.Object[r7];
        r9 = r2.txSuccessRate;
        r9 = java.lang.Double.valueOf(r9);
        r10 = 0;
        r8[r10] = r9;
        r6 = java.lang.String.format(r6, r8);
        r3.append(r6);
        r6 = "%.1f,";
        r8 = new java.lang.Object[r7];
        r11 = r2.txRetriesRate;
        r9 = java.lang.Double.valueOf(r11);
        r8[r10] = r9;
        r6 = java.lang.String.format(r6, r8);
        r3.append(r6);
        r6 = "%.1f ";
        r8 = new java.lang.Object[r7];
        r11 = r2.txBadRate;
        r9 = java.lang.Double.valueOf(r11);
        r8[r10] = r9;
        r6 = java.lang.String.format(r6, r8);
        r3.append(r6);
        r6 = "rx=%.1f";
        r7 = new java.lang.Object[r7];
        r8 = r2.rxSuccessRate;
        r8 = java.lang.Double.valueOf(r8);
        r7[r10] = r8;
        r6 = java.lang.String.format(r6, r7);
        r3.append(r6);
    L_0x00aa:
        r6 = r0;
        r7 = android.net.wifi.WifiConfiguration.INVALID_RSSI;
        r8 = android.net.wifi.WifiConfiguration.INVALID_RSSI;
        r9 = 4;
        r10 = 0;
        r11 = 0;
        r12 = 0;
        r13 = android.os.SystemClock.elapsedRealtime();
        r0 = r21.getScanResults();
        if (r0 == 0) goto L_0x0154;
    L_0x00bd:
        r0 = r21.getScanResults();
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x0154;
    L_0x00c7:
        r0 = new java.util.HashSet;	 Catch:{ Exception -> 0x014b }
        r0.<init>();	 Catch:{ Exception -> 0x014b }
        r15 = r21.getScanResults();	 Catch:{ Exception -> 0x014b }
        r0.addAll(r15);	 Catch:{ Exception -> 0x014b }
        r15 = r0.size();	 Catch:{ Exception -> 0x014b }
        if (r15 <= 0) goto L_0x0146;
    L_0x00d9:
        r15 = r0.iterator();	 Catch:{ Exception -> 0x014b }
    L_0x00dd:
        r16 = r15.hasNext();	 Catch:{ Exception -> 0x014b }
        if (r16 == 0) goto L_0x0146;
    L_0x00e3:
        r16 = r15.next();	 Catch:{ Exception -> 0x014b }
        r16 = (android.net.wifi.ScanResult) r16;	 Catch:{ Exception -> 0x014b }
        r17 = r16;
        r18 = r0;
        r0 = r17;
        if (r0 != 0) goto L_0x00f5;
        r0 = r18;
        goto L_0x00dd;
    L_0x00f5:
        r19 = r2;
        r2 = r0.frequency;	 Catch:{ Exception -> 0x0142 }
        r20 = r9;
        r9 = 4900; // 0x1324 float:6.866E-42 double:2.421E-320;
        if (r2 < r9) goto L_0x011b;
    L_0x00ff:
        r2 = r0.frequency;	 Catch:{ Exception -> 0x0119 }
        r9 = 5900; // 0x170c float:8.268E-42 double:2.915E-320;
        if (r2 > r9) goto L_0x011b;
    L_0x0105:
        r10 = r10 + 1;
        r2 = r0.level;	 Catch:{ Exception -> 0x0119 }
        if (r2 <= r7) goto L_0x010e;
    L_0x010b:
        r2 = r0.level;	 Catch:{ Exception -> 0x0119 }
        r7 = r2;
    L_0x010e:
        r2 = 4;
        if (r10 > r2) goto L_0x013a;
    L_0x0111:
        r2 = verboseScanResultSummary(r1, r0, r6, r13);	 Catch:{ Exception -> 0x0119 }
        r5.append(r2);	 Catch:{ Exception -> 0x0119 }
        goto L_0x013a;
    L_0x0119:
        r0 = move-exception;
        goto L_0x0150;
    L_0x011b:
        r2 = r0.frequency;	 Catch:{ Exception -> 0x0119 }
        r9 = 2400; // 0x960 float:3.363E-42 double:1.186E-320;
        if (r2 < r9) goto L_0x013a;
    L_0x0121:
        r2 = r0.frequency;	 Catch:{ Exception -> 0x0119 }
        r9 = 2500; // 0x9c4 float:3.503E-42 double:1.235E-320;
        if (r2 > r9) goto L_0x013a;
    L_0x0127:
        r11 = r11 + 1;
        r2 = r0.level;	 Catch:{ Exception -> 0x0119 }
        if (r2 <= r8) goto L_0x0130;
    L_0x012d:
        r2 = r0.level;	 Catch:{ Exception -> 0x0119 }
        r8 = r2;
    L_0x0130:
        r2 = 4;
        if (r11 > r2) goto L_0x013a;
    L_0x0133:
        r2 = verboseScanResultSummary(r1, r0, r6, r13);	 Catch:{ Exception -> 0x0119 }
        r4.append(r2);	 Catch:{ Exception -> 0x0119 }
        r0 = r18;
        r2 = r19;
        r9 = r20;
        goto L_0x00dd;
    L_0x0142:
        r0 = move-exception;
        r20 = r9;
        goto L_0x0150;
    L_0x0146:
        r19 = r2;
        r20 = r9;
        goto L_0x0158;
    L_0x014b:
        r0 = move-exception;
        r19 = r2;
        r20 = r9;
    L_0x0150:
        r0.printStackTrace();
        goto L_0x0158;
    L_0x0154:
        r19 = r2;
        r20 = r9;
    L_0x0158:
        r0 = " [";
        r3.append(r0);
        if (r11 <= 0) goto L_0x0183;
    L_0x015f:
        r0 = "(";
        r3.append(r0);
        r3.append(r11);
        r0 = ")";
        r3.append(r0);
        r2 = 4;
        if (r11 <= r2) goto L_0x017c;
    L_0x016f:
        r0 = "max=";
        r3.append(r0);
        r3.append(r8);
        r0 = ",";
        r3.append(r0);
    L_0x017c:
        r0 = r4.toString();
        r3.append(r0);
    L_0x0183:
        r0 = ";";
        r3.append(r0);
        if (r10 <= 0) goto L_0x01ae;
    L_0x018a:
        r0 = "(";
        r3.append(r0);
        r3.append(r10);
        r0 = ")";
        r3.append(r0);
        r2 = 4;
        if (r10 <= r2) goto L_0x01a7;
    L_0x019a:
        r0 = "max=";
        r3.append(r0);
        r3.append(r7);
        r0 = ",";
        r3.append(r0);
    L_0x01a7:
        r0 = r5.toString();
        r3.append(r0);
    L_0x01ae:
        if (r12 <= 0) goto L_0x01b8;
    L_0x01b0:
        r0 = "!";
        r3.append(r0);
        r3.append(r12);
    L_0x01b8:
        r0 = "]";
        r3.append(r0);
        r0 = r3.toString();
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.wifi.WifiUtils.getVisibilityStatus(com.android.settingslib.wifi.AccessPoint):java.lang.String");
    }

    @VisibleForTesting
    static String verboseScanResultSummary(AccessPoint accessPoint, ScanResult result, String bssid, long nowMs) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" \n{");
        stringBuilder.append(result.BSSID);
        if (result.BSSID.equals(bssid)) {
            stringBuilder.append("*");
        }
        stringBuilder.append("=");
        stringBuilder.append(result.frequency);
        stringBuilder.append(",");
        stringBuilder.append(result.level);
        int speed = getSpecificApSpeed(result, accessPoint.getScoredNetworkCache());
        if (speed != 0) {
            stringBuilder.append(",");
            stringBuilder.append(accessPoint.getSpeedLabel(speed));
        }
        int ageSeconds = ((int) (nowMs - (result.timestamp / 1000))) / 1000;
        stringBuilder.append(",");
        stringBuilder.append(ageSeconds);
        stringBuilder.append("s");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    private static int getSpecificApSpeed(ScanResult result, Map<String, TimestampedScoredNetwork> scoredNetworkCache) {
        TimestampedScoredNetwork timedScore = (TimestampedScoredNetwork) scoredNetworkCache.get(result.BSSID);
        if (timedScore == null) {
            return 0;
        }
        return timedScore.getScore().calculateBadge(result.level);
    }

    public static String getMeteredLabel(Context context, WifiConfiguration config) {
        if (config.meteredOverride == 1 || (config.meteredHint && !isMeteredOverridden(config))) {
            return context.getString(R.string.wifi_metered_label);
        }
        return context.getString(R.string.wifi_unmetered_label);
    }

    public static boolean isMeteredOverridden(WifiConfiguration config) {
        return config.meteredOverride != 0;
    }
}
