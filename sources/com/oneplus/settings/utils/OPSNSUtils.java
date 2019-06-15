package com.oneplus.settings.utils;

import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.OpFeatures;
import com.android.settings.R;
import java.io.PrintStream;

public class OPSNSUtils {
    public static String getOpeName(Context context, int solt) {
        if (context == null) {
            return null;
        }
        String simOpe = null;
        SubscriptionInfo subinfo = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(solt);
        if (subinfo != null) {
            simOpe = TelephonyManager.getDefault().getSimOperator(subinfo.getSubscriptionId());
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("simOpe = ");
        stringBuilder.append(simOpe);
        stringBuilder.append(", slot = ");
        stringBuilder.append(solt);
        logd(stringBuilder.toString());
        if (simOpe != null && (simOpe.startsWith("46000") || simOpe.startsWith("46002") || simOpe.startsWith("46007"))) {
            return context.getResources().getString(R.string.op_china_mobile);
        }
        if (simOpe != null && (simOpe.startsWith("46001") || simOpe.startsWith("46009"))) {
            return context.getResources().getString(R.string.op_china_unicom);
        }
        if (simOpe != null && (simOpe.startsWith("46003") || simOpe.startsWith("46006") || simOpe.startsWith("46011"))) {
            return context.getResources().getString(R.string.op_china_telecom);
        }
        String netOpName = null;
        if (subinfo != null) {
            netOpName = TelephonyManager.getDefault().getNetworkOperatorName(subinfo.getSubscriptionId());
        }
        if (netOpName != null) {
            return netOpName;
        }
        return null;
    }

    public static String getSimName(Context context, int solt) {
        return getSimName(context, solt, true);
    }

    public static String getSimName(Context context, int solt, boolean shouldDefault) {
        if (context == null) {
            return null;
        }
        String mSlotName = null;
        SubscriptionInfo subinfo = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(solt);
        if (subinfo != null) {
            if (subinfo.getDisplayName() != null) {
                mSlotName = subinfo.getDisplayName().toString();
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mSlotName = ");
            stringBuilder.append(mSlotName);
            logd(stringBuilder.toString());
        }
        PrintStream printStream = System.out;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("zhuyang--mSlotName:");
        stringBuilder2.append(mSlotName);
        stringBuilder2.append(" shouldDefault:");
        stringBuilder2.append(shouldDefault);
        stringBuilder2.append(" solt:");
        stringBuilder2.append(solt);
        printStream.println(stringBuilder2.toString());
        if (TextUtils.isEmpty(mSlotName) && shouldDefault) {
            if (OpFeatures.isSupport(new int[]{85})) {
                mSlotName = context.getResources().getString(R.string.op_sim_card_with_slotid, new Object[]{""});
            } else {
                mSlotName = context.getResources().getString(R.string.op_sim_card_with_slotid, new Object[]{Integer.valueOf(solt + 1)});
            }
        }
        return mSlotName;
    }

    private static void logd(String msg) {
        Log.d("OPSNSUtils", msg);
    }

    public static int findSlotIdBySubId(int subId) {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        for (int i = 0; i < phoneCount; i++) {
            int[] subIdArr = SubscriptionManager.getSubId(i);
            if (subIdArr != null && subIdArr.length > 0 && subId == subIdArr[0]) {
                return i;
            }
        }
        return 0;
    }

    public static int findSubIdBySlotId(int slotId) {
        int[] subIds = SubscriptionManager.getSubId(slotId);
        if (subIds != null) {
            return subIds[0];
        }
        return -1;
    }

    public static String getLocalString(Context context, String originalString) {
        if (context == null) {
            return originalString;
        }
        String[] origNames = context.getResources().getStringArray(R.array.op_origin_carrier_names);
        String[] localNames = context.getResources().getStringArray(R.array.op_locale_carrier_names);
        for (int i = 0; i < origNames.length; i++) {
            if (origNames[i].equalsIgnoreCase(originalString)) {
                return context.getResources().getString(context.getResources().getIdentifier(localNames[i], "string", context.getPackageName()));
            }
        }
        return originalString;
    }
}
