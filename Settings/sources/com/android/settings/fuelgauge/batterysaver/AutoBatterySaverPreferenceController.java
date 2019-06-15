package com.android.settings.fuelgauge.batterysaver;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.fuelgauge.BatterySaverUtils;

public class AutoBatterySaverPreferenceController extends TogglePreferenceController implements OnPreferenceChangeListener {
    static final int DEFAULT_TRIGGER_LEVEL = 0;
    @VisibleForTesting
    static final String KEY_AUTO_BATTERY_SAVER = "auto_battery_saver";
    private final int mDefaultTriggerLevelForOn = this.mContext.getResources().getInteger(17694806);

    public AutoBatterySaverPreferenceController(Context context) {
        super(context, KEY_AUTO_BATTERY_SAVER);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public boolean isChecked() {
        return Global.getInt(this.mContext.getContentResolver(), "low_power_trigger_level", 0) != 0;
    }

    public boolean setChecked(boolean isChecked) {
        BatterySaverUtils.setAutoBatterySaverTriggerLevel(this.mContext, isChecked ? this.mDefaultTriggerLevelForOn : 0);
        return true;
    }
}
