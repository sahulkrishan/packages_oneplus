package com.android.settings.gestures;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

public class DoubleTapScreenPreferenceController extends GesturePreferenceController {
    private static final String PREF_KEY_VIDEO = "gesture_double_tap_screen_video";
    private final int OFF = 0;
    private final int ON = 1;
    private final String SECURE_KEY = "doze_pulse_on_double_tap";
    private AmbientDisplayConfiguration mAmbientConfig;
    private final String mDoubleTapScreenPrefKey;
    private final int mUserId = UserHandle.myUserId();

    public DoubleTapScreenPreferenceController(Context context, String key) {
        super(context, key);
        this.mDoubleTapScreenPrefKey = key;
    }

    public DoubleTapScreenPreferenceController setConfig(AmbientDisplayConfiguration config) {
        this.mAmbientConfig = config;
        return this;
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        return isSuggestionComplete(new AmbientDisplayConfiguration(context), prefs);
    }

    @VisibleForTesting
    static boolean isSuggestionComplete(AmbientDisplayConfiguration config, SharedPreferences prefs) {
        if (!config.pulseOnDoubleTapAvailable() || prefs.getBoolean(DoubleTapScreenSettings.PREF_KEY_SUGGESTION_COMPLETE, false)) {
            return true;
        }
        return false;
    }

    public int getAvailabilityStatus() {
        if (this.mAmbientConfig == null) {
            this.mAmbientConfig = new AmbientDisplayConfiguration(this.mContext);
        }
        if (!this.mAmbientConfig.doubleTapSensorAvailable()) {
            return 2;
        }
        if (this.mAmbientConfig.ambientDisplayAvailable()) {
            return 0;
        }
        return 4;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_double_tap_screen");
    }

    public boolean setChecked(boolean isChecked) {
        return Secure.putInt(this.mContext.getContentResolver(), "doze_pulse_on_double_tap", isChecked);
    }

    /* Access modifiers changed, original: protected */
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public boolean isChecked() {
        return this.mAmbientConfig.pulseOnDoubleTapEnabled(this.mUserId);
    }

    public ResultPayload getResultPayload() {
        return new InlineSwitchPayload("doze_pulse_on_double_tap", 2, 1, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, DoubleTapScreenSettings.class.getName(), this.mDoubleTapScreenPrefKey, this.mContext.getString(R.string.display_settings)), isAvailable(), 1);
    }

    /* Access modifiers changed, original: protected */
    public boolean canHandleClicks() {
        return this.mAmbientConfig.alwaysOnEnabled(this.mUserId) ^ 1;
    }
}
