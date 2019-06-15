package com.android.settings.fuelgauge;

import android.content.Intent;
import android.util.Log;
import com.android.settings.utils.VoiceSettingsActivity;
import com.android.settingslib.fuelgauge.BatterySaverUtils;

public class BatterySaverModeVoiceActivity extends VoiceSettingsActivity {
    private static final String TAG = "BatterySaverModeVoiceActivity";

    /* Access modifiers changed, original: protected */
    public boolean onVoiceSettingInteraction(Intent intent) {
        if (!intent.hasExtra("android.settings.extra.battery_saver_mode_enabled")) {
            Log.v(TAG, "Missing battery saver mode extra");
        } else if (BatterySaverUtils.setPowerSaveMode(this, intent.getBooleanExtra("android.settings.extra.battery_saver_mode_enabled", false), true)) {
            notifySuccess(null);
        } else {
            Log.v(TAG, "Unable to set power mode");
            notifyFailure(null);
        }
        return true;
    }
}
