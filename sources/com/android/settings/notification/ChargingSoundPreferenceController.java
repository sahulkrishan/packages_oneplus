package com.android.settings.notification;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ChargingSoundPreferenceController extends SettingPrefController {
    private static final String KEY_CHARGING_SOUNDS = "charging_sounds";

    public ChargingSoundPreferenceController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        this.mPreference = new SettingPref(1, KEY_CHARGING_SOUNDS, "charging_sounds_enabled", 1, new int[0]);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_charging_sounds);
    }
}
