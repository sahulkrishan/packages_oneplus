package com.android.settings.accessibility;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

public class NotificationVibrationIntensityPreferenceController extends VibrationIntensityPreferenceController {
    @VisibleForTesting
    static final String PREF_KEY = "notification_vibration_preference_screen";

    public NotificationVibrationIntensityPreferenceController(Context context) {
        super(context, PREF_KEY, "notification_vibration_intensity");
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public int getDefaultIntensity() {
        return this.mVibrator.getDefaultNotificationVibrationIntensity();
    }
}
