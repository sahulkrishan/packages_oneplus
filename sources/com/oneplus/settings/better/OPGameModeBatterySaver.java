package com.oneplus.settings.better;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;

public class OPGameModeBatterySaver extends SettingsPreferenceFragment implements OnClickListener {
    private static final String BATTERY_SAVER_CLOSE_VALUE = "0_0";
    private static final String BATTERY_SAVER_HIGH_VALUE = "56_30";
    private static final String BATTERY_SAVER_LIGHT_VALUE = "56_0";
    public static final String GAME_MODE_BATTERY_SAVER = "game_mode_battery_saver";
    private static final String KEY_battery_saver_close = "battery_saver_close";
    private static final String KEY_battery_saver_high = "battery_saver_high";
    private static final String KEY_battery_saver_light = "battery_saver_light";
    private RadioButtonPreference mBatterySaveCloseButton;
    private RadioButtonPreference mBatterySaveHighButton;
    private RadioButtonPreference mBatterySaveLightButton;
    private Context mContext;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_game_mode_battery_saver);
        this.mContext = SettingsBaseApplication.mApplication;
        this.mBatterySaveCloseButton = (RadioButtonPreference) findPreference(KEY_battery_saver_close);
        this.mBatterySaveLightButton = (RadioButtonPreference) findPreference(KEY_battery_saver_light);
        this.mBatterySaveHighButton = (RadioButtonPreference) findPreference(KEY_battery_saver_high);
        this.mBatterySaveCloseButton.setOnClickListener(this);
        this.mBatterySaveLightButton.setOnClickListener(this);
        this.mBatterySaveHighButton.setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();
        if (this.mBatterySaveCloseButton != null && this.mBatterySaveLightButton != null && this.mBatterySaveHighButton != null) {
            String value = System.getStringForUser(getContentResolver(), "game_mode_battery_saver", -2);
            RadioButtonPreference radioButtonPreference = this.mBatterySaveCloseButton;
            boolean z = BATTERY_SAVER_CLOSE_VALUE.equalsIgnoreCase(value) || TextUtils.isEmpty(value);
            radioButtonPreference.setChecked(z);
            this.mBatterySaveLightButton.setChecked(BATTERY_SAVER_LIGHT_VALUE.equalsIgnoreCase(value));
            this.mBatterySaveHighButton.setChecked(BATTERY_SAVER_HIGH_VALUE.equalsIgnoreCase(value));
        }
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if (emiter == this.mBatterySaveCloseButton) {
            this.mBatterySaveCloseButton.setChecked(true);
            this.mBatterySaveLightButton.setChecked(false);
            this.mBatterySaveHighButton.setChecked(false);
            System.putStringForUser(getContentResolver(), "game_mode_battery_saver", BATTERY_SAVER_CLOSE_VALUE, -2);
            OPUtils.sendAppTracker("game_mode_battery_saver", BATTERY_SAVER_CLOSE_VALUE);
        } else if (emiter == this.mBatterySaveLightButton) {
            this.mBatterySaveCloseButton.setChecked(false);
            this.mBatterySaveLightButton.setChecked(true);
            this.mBatterySaveHighButton.setChecked(false);
            System.putStringForUser(getContentResolver(), "game_mode_battery_saver", BATTERY_SAVER_LIGHT_VALUE, -2);
            OPUtils.sendAppTracker("game_mode_battery_saver", BATTERY_SAVER_LIGHT_VALUE);
        } else if (emiter == this.mBatterySaveHighButton) {
            this.mBatterySaveCloseButton.setChecked(false);
            this.mBatterySaveLightButton.setChecked(false);
            this.mBatterySaveHighButton.setChecked(true);
            System.putStringForUser(getContentResolver(), "game_mode_battery_saver", BATTERY_SAVER_HIGH_VALUE, -2);
            OPUtils.sendAppTracker("game_mode_battery_saver", BATTERY_SAVER_HIGH_VALUE);
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
