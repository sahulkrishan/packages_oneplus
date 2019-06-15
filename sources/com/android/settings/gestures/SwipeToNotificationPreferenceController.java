package com.android.settings.gestures;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.android.settings.Utils;

public class SwipeToNotificationPreferenceController extends GesturePreferenceController {
    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String PREF_KEY_VIDEO = "gesture_swipe_down_fingerprint_video";
    private static final String SECURE_KEY = "system_navigation_keys_enabled";

    public SwipeToNotificationPreferenceController(Context context, String key) {
        super(context, key);
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        if (!isGestureAvailable(context) || prefs.getBoolean(SwipeToNotificationSettings.PREF_KEY_SUGGESTION_COMPLETE, false)) {
            return true;
        }
        return false;
    }

    private static boolean isGestureAvailable(Context context) {
        return Utils.hasFingerprintHardware(context) && context.getResources().getBoolean(17957051);
    }

    /* Access modifiers changed, original: protected */
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public int getAvailabilityStatus() {
        return isAvailable(this.mContext) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_swipe_down_fingerprint");
    }

    public boolean setChecked(boolean isChecked) {
        setSwipeToNotification(this.mContext, isChecked);
        return true;
    }

    public boolean isChecked() {
        return isSwipeToNotificationOn(this.mContext);
    }

    public static boolean isSwipeToNotificationOn(Context context) {
        return Secure.getInt(context.getContentResolver(), SECURE_KEY, 0) == 1;
    }

    public static boolean setSwipeToNotification(Context context, boolean isEnabled) {
        return Secure.putInt(context.getContentResolver(), SECURE_KEY, isEnabled);
    }

    public static boolean isAvailable(Context context) {
        return isGestureAvailable(context);
    }
}
