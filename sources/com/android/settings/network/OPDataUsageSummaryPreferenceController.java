package com.android.settings.network;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.oneplus.settings.utils.OPConstants;

public class OPDataUsageSummaryPreferenceController extends BasePreferenceController implements LifecycleObserver {
    private static final String KEY_DATA_USAGE_SUMMARY = "data_usage_summary";
    private Preference mPreference;

    public OPDataUsageSummaryPreferenceController(Context context) {
        super(context, KEY_DATA_USAGE_SUMMARY);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_DATA_USAGE_SUMMARY;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_DATA_USAGE_SUMMARY.equals(preference.getKey())) {
            return false;
        }
        try {
            this.mContext.startActivity(new Intent(OPConstants.OP_USAGE_DATA_SUMMARY_ACTION));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
}
