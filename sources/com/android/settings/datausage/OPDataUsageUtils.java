package com.android.settings.datausage;

import android.app.AppGlobals;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Log;
import com.google.android.collect.Maps;
import com.oneplus.settings.utils.OPSNSUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OPDataUsageUtils {
    public static final int ERROR_CODE_EXCEPTION = 2;
    public static final int ERROR_CODE_INVILIDSIMCARD = 1;
    public static final int ERROR_CODE_NO = 0;
    public static final String KEY_ACCOUNT_DAY_SIM = "key_account_day_slot_";
    private static final String KEY_DATAUSAGE_ALERT_NUMBER_SIM = "key_datausage_alert_number_sim_";
    private static final String KEY_DATAUSAGE_WARN_STATE = "key_ten_percent_low_remaining_state_sim_";
    public static final String METHOD_QUERY_ONEPLUS_DATAUSAGE = "method_query_oneplus_datausage";
    public static final String METHOD_QUERY_ONEPLUS_DATAUSAGE_REGION = "method_query_oneplus_datausage_region";
    public static final String ONEPLUS_DATAUSAGE_ACCOUNTDAY = "oneplus_datausage_accountday";
    public static final String ONEPLUS_DATAUSAGE_ERROR_CODE = "oneplus_datausage_error_code";
    public static final String ONEPLUS_DATAUSAGE_SLOTID = "oneplus_datausage_slotid";
    public static final String ONEPLUS_DATAUSAGE_TIME_END = "oneplus_datausage_time_end";
    public static final String ONEPLUS_DATAUSAGE_TIME_START = "oneplus_datausage_time_start";
    public static final String ONEPLUS_DATAUSAGE_TOTAL = "oneplus_datausage_total";
    public static final String ONEPLUS_DATAUSAGE_USED = "oneplus_datausage_used";
    public static final String ONEPLUS_DATAUSAGE_WARN_STATE = "oneplus_datausage_warn_state";
    public static final String ONEPLUS_DATAUSAGE_WARN_VALUE = "oneplus_datausage_warn_value";
    public static final String ONEPLUS_SECURITY_URI = "content://com.oneplus.security.database.SafeProvider";

    public static final int getAccountDay(Context context, int subId) {
        int day = context.getContentResolver();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(KEY_ACCOUNT_DAY_SIM);
        stringBuilder.append(subId);
        return System.getInt(day, stringBuilder.toString(), 0);
    }

    public static final long getDataWarnBytes(Context context, int subId) {
        long data = context.getContentResolver();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(KEY_DATAUSAGE_ALERT_NUMBER_SIM);
        stringBuilder.append(subId);
        return System.getLong(data, stringBuilder.toString(), 0);
    }

    public static final int getDataWarnState(Context context, int subId) {
        int state = context.getContentResolver();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(KEY_DATAUSAGE_WARN_STATE);
        stringBuilder.append(subId);
        return System.getInt(state, stringBuilder.toString(), 0);
    }

    public static long[] getDataUsageSectionTimeMillByAccountDay(Context context, int subId) {
        if (subId != -1) {
            return getOneplusDataUsageRegion(context, OPSNSUtils.findSlotIdBySubId(subId));
        }
        return getOneplusDataUsageRegion(context, -1);
    }

    public static List<ApplicationInfo> getApplicationInfoByUid(Context context, int uid) {
        List<ApplicationInfo> apps = new ArrayList();
        String[] packageNames = context.getPackageManager().getPackagesForUid(uid);
        int length = packageNames != null ? packageNames.length : 0;
        try {
            int userId = UserHandle.getUserId(uid);
            IPackageManager ipm = AppGlobals.getPackageManager();
            for (int i = 0; i < length; i++) {
                ApplicationInfo appInfo = ipm.getApplicationInfo(packageNames[i], 0, userId);
                if (appInfo != null) {
                    apps.add(appInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apps;
    }

    public static Map<String, Object> getOneplusDataUsage(Context context, int slotId) {
        Exception e;
        Bundle bundle = new Bundle();
        bundle.putInt(ONEPLUS_DATAUSAGE_SLOTID, slotId);
        try {
            Bundle bundle2 = context.getContentResolver().call(Uri.parse(ONEPLUS_SECURITY_URI), METHOD_QUERY_ONEPLUS_DATAUSAGE, null, bundle);
            if (bundle2 != null) {
                int errorCode = bundle2.getInt(ONEPLUS_DATAUSAGE_ERROR_CODE);
                int accountDay = bundle2.getInt(ONEPLUS_DATAUSAGE_ACCOUNTDAY);
                long startTime = bundle2.getLong(ONEPLUS_DATAUSAGE_TIME_START);
                long endTime = bundle2.getLong(ONEPLUS_DATAUSAGE_TIME_END);
                long total = bundle2.getLong(ONEPLUS_DATAUSAGE_TOTAL);
                long used = bundle2.getLong(ONEPLUS_DATAUSAGE_USED);
                boolean warnState = bundle2.getBoolean(ONEPLUS_DATAUSAGE_WARN_STATE);
                long warnValue = bundle2.getLong(ONEPLUS_DATAUSAGE_WARN_VALUE);
                Map<String, Object> ret = Maps.newHashMap();
                ret.put(ONEPLUS_DATAUSAGE_ERROR_CODE, Integer.valueOf(errorCode));
                ret.put(ONEPLUS_DATAUSAGE_ACCOUNTDAY, Integer.valueOf(accountDay));
                ret.put(ONEPLUS_DATAUSAGE_TOTAL, Long.valueOf(total));
                ret.put(ONEPLUS_DATAUSAGE_USED, Long.valueOf(used));
                ret.put(ONEPLUS_DATAUSAGE_TIME_START, Long.valueOf(startTime));
                ret.put(ONEPLUS_DATAUSAGE_TIME_END, Long.valueOf(endTime));
                ret.put(ONEPLUS_DATAUSAGE_WARN_STATE, Boolean.valueOf(warnState));
                try {
                    ret.put(ONEPLUS_DATAUSAGE_WARN_VALUE, Long.valueOf(warnValue));
                    return ret;
                } catch (Exception e2) {
                    e = e2;
                    Log.e("OPDataUsageUtils", "getOneplusDataUsage error");
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        } catch (Exception e3) {
            e = e3;
            Bundle bundle3 = bundle;
            Log.e("OPDataUsageUtils", "getOneplusDataUsage error");
            e.printStackTrace();
            return null;
        }
    }

    public static long[] getOneplusDataUsageRegion(Context context, int slotId) {
        Bundle bundle = new Bundle();
        bundle.putInt(ONEPLUS_DATAUSAGE_SLOTID, slotId);
        try {
            Bundle bundle2 = context.getContentResolver().call(Uri.parse(ONEPLUS_SECURITY_URI), METHOD_QUERY_ONEPLUS_DATAUSAGE_REGION, null, bundle);
            if (!(bundle2 == null || bundle2.getInt(ONEPLUS_DATAUSAGE_ERROR_CODE) == 2)) {
                long startTime = bundle2.getLong(ONEPLUS_DATAUSAGE_TIME_START);
                long endTime = bundle2.getLong(ONEPLUS_DATAUSAGE_TIME_END);
                return new long[]{startTime, endTime};
            }
        } catch (Exception e) {
            Log.e("OPDataUsageUtils", "getOneplusDataUsage error");
            e.printStackTrace();
        }
        return new long[]{0, System.currentTimeMillis()};
    }
}
