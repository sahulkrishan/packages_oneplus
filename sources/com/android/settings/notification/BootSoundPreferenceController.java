package com.android.settings.notification;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class BootSoundPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_BOOT_SOUNDS = "boot_sounds";
    @VisibleForTesting
    static final String PROPERTY_BOOT_SOUNDS = "persist.sys.bootanim.play_sound";

    public BootSoundPreferenceController(Context context) {
        super(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            ((SwitchPreference) screen.findPreference(KEY_BOOT_SOUNDS)).setChecked(SystemProperties.getBoolean(PROPERTY_BOOT_SOUNDS, true));
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_BOOT_SOUNDS.equals(preference.getKey())) {
            SystemProperties.set(PROPERTY_BOOT_SOUNDS, ((SwitchPreference) preference).isChecked() ? "1" : "0");
        }
        return false;
    }

    public String getPreferenceKey() {
        return KEY_BOOT_SOUNDS;
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.has_boot_sounds);
    }
}
