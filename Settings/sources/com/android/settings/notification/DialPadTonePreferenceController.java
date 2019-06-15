package com.android.settings.notification;

import android.content.Context;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class DialPadTonePreferenceController extends SettingPrefController {
    private static final String KEY_DIAL_PAD_TONES = "dial_pad_tones";

    public DialPadTonePreferenceController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        this.mPreference = new SettingPref(2, KEY_DIAL_PAD_TONES, "dtmf_tone", 1, new int[0]) {
            public boolean isApplicable(Context context) {
                return Utils.isVoiceCapable(context);
            }
        };
    }
}
