package com.android.settings.fuelgauge;

import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.Settings.BgOptimizeAppListActivity;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.fuelgauge.PowerWhitelistBackend;
import com.oneplus.settings.backgroundoptimize.BgOActivityManager;

public class BatteryOptimizationPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_BACKGROUND_ACTIVITY = "battery_optimization";
    private PowerWhitelistBackend mBackend;
    private DashboardFragment mFragment;
    private String mPackageName;
    private SettingsActivity mSettingsActivity;

    public BatteryOptimizationPreferenceController(SettingsActivity settingsActivity, DashboardFragment fragment, String packageName) {
        super(settingsActivity);
        this.mFragment = fragment;
        this.mSettingsActivity = settingsActivity;
        this.mPackageName = packageName;
        this.mBackend = PowerWhitelistBackend.getInstance(this.mSettingsActivity);
    }

    @VisibleForTesting
    BatteryOptimizationPreferenceController(SettingsActivity settingsActivity, DashboardFragment fragment, String packageName, PowerWhitelistBackend backend) {
        super(settingsActivity);
        this.mFragment = fragment;
        this.mSettingsActivity = settingsActivity;
        this.mPackageName = packageName;
        this.mBackend = backend;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = true;
        if (1 != BgOActivityManager.getInstance(this.mContext).getAppControlMode(this.mPackageName, 0)) {
            z = false;
        }
        preference.setSummary(z ? R.string.high_power_on : R.string.high_power_off);
    }

    public String getPreferenceKey() {
        return KEY_BACKGROUND_ACTIVITY;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_BACKGROUND_ACTIVITY.equals(preference.getKey())) {
            return false;
        }
        Bundle args = new Bundle();
        args.putString(ManageApplications.EXTRA_CLASSNAME, BgOptimizeAppListActivity.class.getName());
        new SubSettingLauncher(this.mSettingsActivity).setDestination(ManageApplications.class.getName()).setArguments(args).setTitle((int) R.string.high_power_apps).setSourceMetricsCategory(this.mFragment.getMetricsCategory()).launch("com.android.settings.action.BACKGROUND_OPTIMIZE");
        return true;
    }
}
