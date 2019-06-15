package com.android.settings.notification;

import android.content.Context;
import android.content.res.Resources;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class EmergencyTonePreferenceController extends SettingPrefController {
    private static final int DEFAULT_EMERGENCY_TONE = 0;
    private static final int EMERGENCY_TONE_ALERT = 1;
    private static final int EMERGENCY_TONE_SILENT = 0;
    private static final int EMERGENCY_TONE_VIBRATE = 2;
    private static final String KEY_EMERGENCY_TONE = "emergency_tone";

    public EmergencyTonePreferenceController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        this.mPreference = new SettingPref(1, KEY_EMERGENCY_TONE, KEY_EMERGENCY_TONE, 0, 1, 2, 0) {
            public boolean isApplicable(Context context) {
                context.getSystemService("phone");
                return false;
            }

            /* Access modifiers changed, original: protected */
            public String getCaption(Resources res, int value) {
                switch (value) {
                    case 0:
                        return res.getString(R.string.emergency_tone_silent);
                    case 1:
                        return res.getString(R.string.emergency_tone_alert);
                    case 2:
                        return res.getString(R.string.emergency_tone_vibrate);
                    default:
                        throw new IllegalArgumentException();
                }
            }
        };
    }
}
