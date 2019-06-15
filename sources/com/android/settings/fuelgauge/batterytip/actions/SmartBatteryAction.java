package com.android.settings.fuelgauge.batterytip.actions;

import android.app.Fragment;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.fuelgauge.SmartBatterySettings;
import com.android.settingslib.core.instrumentation.Instrumentable;

public class SmartBatteryAction extends BatteryTipAction {
    private Fragment mFragment;
    private SettingsActivity mSettingsActivity;

    public SmartBatteryAction(SettingsActivity settingsActivity, Fragment fragment) {
        super(settingsActivity.getApplicationContext());
        this.mSettingsActivity = settingsActivity;
        this.mFragment = fragment;
    }

    public void handlePositiveAction(int metricsKey) {
        int metricsCategory;
        this.mMetricsFeatureProvider.action(this.mContext, 1364, metricsKey);
        SubSettingLauncher subSettingLauncher = new SubSettingLauncher(this.mSettingsActivity);
        if (this.mFragment instanceof Instrumentable) {
            metricsCategory = ((Instrumentable) this.mFragment).getMetricsCategory();
        } else {
            metricsCategory = 0;
        }
        subSettingLauncher.setSourceMetricsCategory(metricsCategory).setDestination(SmartBatterySettings.class.getName()).setTitle((int) R.string.smart_battery_manager_title).launch();
    }
}
