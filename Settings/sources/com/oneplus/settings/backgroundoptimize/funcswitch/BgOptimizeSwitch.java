package com.oneplus.settings.backgroundoptimize.funcswitch;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.backgroundoptimize.BgOActivityManager;
import com.oneplus.settings.utils.OPUtils;

public class BgOptimizeSwitch extends SettingsPreferenceFragment {
    private static final String KEY_SLEEP_STANDBY = "sleep_standby";
    private static final String OPTIMAL_POWER_SAVE_MODE_ENABLED = "optimal_power_save_mode_enabled";
    private static final String PREF_BG_OPTIMIZE_SWITCH = "bg_optimize_switch";
    private Context mContext;
    private SwitchPreference mSleepStandBySwitchPreference;
    SwitchPreference switchPreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.bg_optimize_prefs);
        this.mContext = getActivity();
        initData();
    }

    private void initData() {
        this.switchPreference = (SwitchPreference) findPreference(PREF_BG_OPTIMIZE_SWITCH);
        boolean z = true;
        if (this.switchPreference != null) {
            this.switchPreference.setChecked(1 == BgOActivityManager.getInstance(getPrefContext()).getAppControlState(0));
        }
        this.mSleepStandBySwitchPreference = (SwitchPreference) findPreference(KEY_SLEEP_STANDBY);
        int value = System.getIntForUser(getContentResolver(), OPTIMAL_POWER_SAVE_MODE_ENABLED, 0, -2);
        if (!OPUtils.isSupportSleepStandby()) {
            this.mSleepStandBySwitchPreference.setVisible(false);
        }
        if (this.mSleepStandBySwitchPreference != null) {
            SwitchPreference switchPreference = this.mSleepStandBySwitchPreference;
            if (value <= 0) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    private void confirmDeepOptimization() {
        OnClickListener onClickListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    BgOActivityManager.getInstance(BgOptimizeSwitch.this.getPrefContext()).setAppControlState(0, 0);
                    OPUtils.sendAppTracker("battery_deep", 0);
                } else if (which == -2) {
                    BgOptimizeSwitch.this.switchPreference.setChecked(true);
                }
            }
        };
        new Builder(this.mContext).setTitle(R.string.oneplus_deep_optimization_title).setMessage(R.string.oneplus_deep_optimization_summary).setPositiveButton(17039370, onClickListener).setNegativeButton(17039360, onClickListener).setCancelable(false).create().show();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (PREF_BG_OPTIMIZE_SWITCH.equals(preference.getKey())) {
            if (this.switchPreference.isChecked()) {
                BgOActivityManager.getInstance(getPrefContext()).setAppControlState(0, 1);
                OPUtils.sendAppTracker("battery_deep", 1);
            } else {
                confirmDeepOptimization();
            }
            return true;
        } else if (!KEY_SLEEP_STANDBY.equals(preference.getKey())) {
            return super.onPreferenceTreeClick(preference);
        } else {
            System.putIntForUser(getContentResolver(), OPTIMAL_POWER_SAVE_MODE_ENABLED, this.mSleepStandBySwitchPreference.isChecked(), -2);
            return true;
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
