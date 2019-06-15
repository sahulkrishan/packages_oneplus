package com.android.settings.datausage;

import android.net.NetworkPolicy;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;

public class DataUsageInfoController {
    public void updateDataLimit(DataUsageInfo info, NetworkPolicy policy) {
        if (info != null && policy != null) {
            if (policy.warningBytes >= 0) {
                info.warningLevel = policy.warningBytes;
            }
            if (policy.limitBytes >= 0) {
                info.limitLevel = policy.limitBytes;
            }
        }
    }

    public long getSummaryLimit(DataUsageInfo info) {
        long limit = info.limitLevel;
        if (limit <= 0) {
            limit = info.warningLevel;
        }
        if (info.usageLevel > limit) {
            return info.usageLevel;
        }
        return limit;
    }
}
