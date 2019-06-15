package com.android.settings.notification;

import android.content.Context;
import android.os.Vibrator;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class VibrateOnTouchPreferenceController extends SettingPrefController {
    private static final String KEY_VIBRATE_ON_TOUCH = "vibrate_on_touch";

    public VibrateOnTouchPreferenceController(Context context, SettingsPreferenceFragment parent, Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        this.mPreference = new SettingPref(2, KEY_VIBRATE_ON_TOUCH, "haptic_feedback_enabled", 0, new int[0]) {
            public boolean isApplicable(Context context) {
                return VibrateOnTouchPreferenceController.hasHaptic(context);
            }
        };
    }

    private static boolean hasHaptic(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
        return vibrator != null && vibrator.hasVibrator();
    }
}
