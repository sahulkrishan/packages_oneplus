package com.android.settings.accessibility;

import android.os.Vibrator;
import com.android.settings.R;

public class NotificationVibrationPreferenceFragment extends VibrationPreferenceFragment {
    public int getMetricsCategory() {
        return 1293;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_notification_vibration_settings;
    }

    /* Access modifiers changed, original: protected */
    public String getVibrationIntensitySetting() {
        return "notification_vibration_intensity";
    }

    /* Access modifiers changed, original: protected */
    public int getPreviewVibrationAudioAttributesUsage() {
        return 5;
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultVibrationIntensity() {
        return ((Vibrator) getContext().getSystemService(Vibrator.class)).getDefaultNotificationVibrationIntensity();
    }
}
