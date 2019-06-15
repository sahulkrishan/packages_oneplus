package com.android.settings.notification;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class DockingSoundPreferenceController extends SettingPrefController {
    private static final String KEY_DOCKING_SOUNDS = "docking_sounds";

    public DockingSoundPreferenceController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        this.mPreference = new SettingPref(1, KEY_DOCKING_SOUNDS, "dock_sounds_enabled", 1, new int[0]) {
            public boolean isApplicable(Context context) {
                return context.getResources().getBoolean(R.bool.has_dock_settings);
            }
        };
    }
}
