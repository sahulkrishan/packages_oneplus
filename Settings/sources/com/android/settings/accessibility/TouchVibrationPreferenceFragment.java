package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.os.Vibrator;
import android.provider.Settings.System;
import com.android.settings.R;

public class TouchVibrationPreferenceFragment extends VibrationPreferenceFragment {
    public int getMetricsCategory() {
        return 1294;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_touch_vibration_settings;
    }

    /* Access modifiers changed, original: protected */
    public String getVibrationIntensitySetting() {
        return "haptic_feedback_intensity";
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultVibrationIntensity() {
        return ((Vibrator) getContext().getSystemService(Vibrator.class)).getDefaultHapticFeedbackIntensity();
    }

    /* Access modifiers changed, original: protected */
    public int getPreviewVibrationAudioAttributesUsage() {
        return 13;
    }

    public void onVibrationIntensitySelected(int intensity) {
        int i = 0;
        boolean hapticFeedbackEnabled = intensity != 0;
        ContentResolver contentResolver = getContext().getContentResolver();
        String str = "haptic_feedback_enabled";
        if (hapticFeedbackEnabled) {
            i = 1;
        }
        System.putInt(contentResolver, str, i);
    }
}
