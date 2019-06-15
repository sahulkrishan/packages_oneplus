package com.android.settings.notification;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

public class SuppressorHelper {
    private static final String TAG = "SuppressorHelper";

    public static String getSuppressionText(Context context, ComponentName suppressor) {
        if (suppressor == null) {
            return null;
        }
        return context.getString(17040364, new Object[]{getSuppressorCaption(context, suppressor)});
    }

    @VisibleForTesting
    static String getSuppressorCaption(Context context, ComponentName suppressor) {
        PackageManager pm = context.getPackageManager();
        try {
            ServiceInfo info = pm.getServiceInfo(suppressor, null);
            if (info != null) {
                CharSequence seq = info.loadLabel(pm);
                if (seq != null) {
                    String str = seq.toString().trim();
                    if (str.length() > 0) {
                        return str;
                    }
                }
            }
        } catch (Throwable e) {
            Log.w(TAG, "Error loading suppressor caption", e);
        }
        return suppressor.getPackageName();
    }
}
