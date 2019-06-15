package com.android.settings.fuelgauge;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.Settings.BgOptimizeAppListActivity;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.oneplus.settings.utils.OPUtils;

public class OPPowerOptimizePreferenceController extends AbstractPreferenceController implements LifecycleObserver {
    private static final String KEY_POWER_OPTIMIZE = "op_power_optimize";
    private Preference mPreference;

    public OPPowerOptimizePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return OPUtils.isGuestMode() ^ 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_POWER_OPTIMIZE;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_POWER_OPTIMIZE.equals(preference.getKey())) {
            return false;
        }
        try {
            Intent intent = new Intent("com.android.settings.action.BACKGROUND_OPTIMIZE");
            intent.putExtra(ManageApplications.EXTRA_CLASSNAME, BgOptimizeAppListActivity.class.getName());
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
}
