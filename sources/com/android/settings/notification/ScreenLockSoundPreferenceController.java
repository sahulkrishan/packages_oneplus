package com.android.settings.notification;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ScreenLockSoundPreferenceController extends SettingPrefController {
    private static final String KEY_SCREEN_LOCKING_SOUNDS = "screen_locking_sounds";

    public ScreenLockSoundPreferenceController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        this.mPreference = new SettingPref(2, KEY_SCREEN_LOCKING_SOUNDS, "lockscreen_sounds_enabled", 1, new int[0]);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_screen_locking_sounds);
    }
}
