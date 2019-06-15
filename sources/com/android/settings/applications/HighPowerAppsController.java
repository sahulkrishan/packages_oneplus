package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.Settings.BgOptimizeAppListActivity;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settings.core.BasePreferenceController;

public class HighPowerAppsController extends BasePreferenceController {
    @VisibleForTesting
    static final String KEY_HIGH_POWER_APPS = "high_power_apps";

    public HighPowerAppsController(Context context) {
        super(context, KEY_HIGH_POWER_APPS);
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(KEY_HIGH_POWER_APPS, preference.getKey())) {
            return false;
        }
        Intent intent = new Intent("com.android.settings.action.BACKGROUND_OPTIMIZE");
        intent.putExtra(ManageApplications.EXTRA_CLASSNAME, BgOptimizeAppListActivity.class.getName());
        this.mContext.startActivity(intent);
        return true;
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_high_power_apps)) {
            return 0;
        }
        return 2;
    }
}
