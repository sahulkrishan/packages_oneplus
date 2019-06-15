package com.android.settings.notification;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class TouchSoundPreferenceController extends SettingPrefController {
    private static final String KEY_TOUCH_SOUNDS = "touch_sounds";

    public TouchSoundPreferenceController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        this.mPreference = new SettingPref(2, KEY_TOUCH_SOUNDS, "sound_effects_enabled", 1, new int[0]) {
            /* Access modifiers changed, original: protected */
            public boolean setSetting(final Context context, final int value) {
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        AudioManager am = (AudioManager) context.getSystemService("audio");
                        if (value != 0) {
                            am.loadSoundEffects();
                        } else {
                            am.unloadSoundEffects();
                        }
                    }
                });
                return super.setSetting(context, value);
            }
        };
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_touch_sounds);
    }
}
