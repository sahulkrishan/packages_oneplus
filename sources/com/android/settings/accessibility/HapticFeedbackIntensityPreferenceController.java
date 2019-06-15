package com.android.settings.accessibility;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

public class HapticFeedbackIntensityPreferenceController extends VibrationIntensityPreferenceController {
    @VisibleForTesting
    static final String PREF_KEY = "touch_vibration_preference_screen";

    public HapticFeedbackIntensityPreferenceController(Context context) {
        super(context, PREF_KEY, "haptic_feedback_intensity");
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultIntensity() {
        return this.mVibrator.getDefaultHapticFeedbackIntensity();
    }
}
