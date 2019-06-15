package com.android.settings.gestures;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

public class DoubleTapPowerPreferenceController extends GesturePreferenceController {
    @VisibleForTesting
    static final int OFF = 1;
    @VisibleForTesting
    static final int ON = 0;
    private static final String PREF_KEY_VIDEO = "gesture_double_tap_power_video";
    private final String SECURE_KEY = "camera_double_tap_power_gesture_disabled";
    private final String mDoubleTapPowerKey;

    public DoubleTapPowerPreferenceController(Context context, String key) {
        super(context, key);
        this.mDoubleTapPowerKey = key;
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        if (!isGestureAvailable(context) || prefs.getBoolean(DoubleTapPowerSettings.PREF_KEY_SUGGESTION_COMPLETE, false)) {
            return true;
        }
        return false;
    }

    private static boolean isGestureAvailable(Context context) {
        return context.getResources().getBoolean(17956908);
    }

    public int getAvailabilityStatus() {
        return isGestureAvailable(this.mContext) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_double_tap_power");
    }

    /* Access modifiers changed, original: protected */
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public boolean isChecked() {
        if (Secure.getInt(this.mContext.getContentResolver(), "camera_double_tap_power_gesture_disabled", 0) == 0) {
            return true;
        }
        return false;
    }

    public boolean setChecked(boolean isChecked) {
        return Secure.putInt(this.mContext.getContentResolver(), "camera_double_tap_power_gesture_disabled", isChecked ^ 1);
    }

    public ResultPayload getResultPayload() {
        return new InlineSwitchPayload("camera_double_tap_power_gesture_disabled", 2, 0, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, DoubleTapPowerSettings.class.getName(), this.mDoubleTapPowerKey, this.mContext.getString(R.string.display_settings)), isAvailable(), 0);
    }
}
