package com.android.settingslib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION;
import android.support.annotation.VisibleForTesting;
import android.system.Os;
import android.system.StructUtsname;
import android.telephony.PhoneNumberUtils;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceInfoUtils {
    private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";
    private static final String TAG = "DeviceInfoUtils";

    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            String readLine = reader.readLine();
            return readLine;
        } finally {
            reader.close();
        }
    }

    public static String getFormattedKernelVersion(Context context) {
        return formatKernelVersion(context, Os.uname());
    }

    @VisibleForTesting
    static String formatKernelVersion(Context context, StructUtsname uname) {
        if (uname == null) {
            return context.getString(R.string.status_unavailable);
        }
        String VERSION_REGEX = "(#\\d+) (?:.*?)?((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)";
        Matcher m = Pattern.compile("(#\\d+) (?:.*?)?((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)").matcher(uname.version);
        if (m.matches()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(uname.release);
            stringBuilder.append("\n");
            stringBuilder.append(m.group(1));
            stringBuilder.append(" ");
            stringBuilder.append(m.group(2));
            return stringBuilder.toString();
        }
        String str = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Regex did not match on uname version ");
        stringBuilder2.append(uname.version);
        Log.e(str, stringBuilder2.toString());
        return context.getString(R.string.status_unavailable);
    }

    public static String getMsvSuffix() {
        try {
            if (Long.parseLong(readLine(FILENAME_MSV), 16) == 0) {
                return " (ENGINEERING)";
            }
        } catch (IOException | NumberFormatException e) {
        }
        return "";
    }

    public static String getFeedbackReporterPackage(Context context) {
        String feedbackReporter = context.getResources().getString(R.string.oem_preferred_feedback_reporter);
        if (TextUtils.isEmpty(feedbackReporter)) {
            return feedbackReporter;
        }
        Intent intent = new Intent("android.intent.action.BUG_REPORT");
        PackageManager pm = context.getPackageManager();
        for (ResolveInfo info : pm.queryIntentActivities(intent, 64)) {
            if (!(info.activityInfo == null || TextUtils.isEmpty(info.activityInfo.packageName))) {
                try {
                    if ((pm.getApplicationInfo(info.activityInfo.packageName, 0).flags & 1) != 0 && TextUtils.equals(info.activityInfo.packageName, feedbackReporter)) {
                        return feedbackReporter;
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        return null;
    }

    public static String getSecurityPatch() {
        String patch = VERSION.SECURITY_PATCH;
        if ("".equals(patch)) {
            return null;
        }
        try {
            patch = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy"), new SimpleDateFormat("yyyy-MM-dd").parse(patch)).toString();
        } catch (ParseException e) {
        }
        return patch;
    }

    public static String getFormattedPhoneNumber(Context context, SubscriptionInfo subscriptionInfo) {
        if (subscriptionInfo == null) {
            return null;
        }
        String rawNumber = ((TelephonyManager) context.getSystemService("phone")).getLine1Number(subscriptionInfo.getSubscriptionId());
        if (TextUtils.isEmpty(rawNumber)) {
            return null;
        }
        return PhoneNumberUtils.formatNumber(rawNumber);
    }

    public static String getFormattedPhoneNumbers(Context context, List<SubscriptionInfo> subscriptionInfo) {
        StringBuilder sb = new StringBuilder();
        if (subscriptionInfo != null) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            int count = subscriptionInfo.size();
            for (int i = 0; i < count; i++) {
                String rawNumber = telephonyManager.getLine1Number(((SubscriptionInfo) subscriptionInfo.get(i)).getSubscriptionId());
                if (!TextUtils.isEmpty(rawNumber)) {
                    sb.append(PhoneNumberUtils.formatNumber(rawNumber));
                    if (i < count - 1) {
                        sb.append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }
}
