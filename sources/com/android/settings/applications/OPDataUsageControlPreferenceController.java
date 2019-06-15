package com.android.settings.applications;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.oneplus.settings.utils.OPUtils;

public class OPDataUsageControlPreferenceController extends BasePreferenceController implements LifecycleObserver {
    private static final String KEY_DATA_USAGE_CONTROL = "data_usage_control";
    private Preference mPreference;

    public OPDataUsageControlPreferenceController(Context context) {
        super(context, KEY_DATA_USAGE_CONTROL);
    }

    public int getAvailabilityStatus() {
        return !OPUtils.isGuestMode() ? 0 : 3;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_DATA_USAGE_CONTROL;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_DATA_USAGE_CONTROL.equals(preference.getKey())) {
            return false;
        }
        try {
            this.mContext.startActivity(new Intent("com.oneplus.security.action.NETWORK_Restrict"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
}
