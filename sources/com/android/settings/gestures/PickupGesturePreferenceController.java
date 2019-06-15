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

public class PickupGesturePreferenceController extends GesturePreferenceController {
    private static final String PREF_KEY_VIDEO = "gesture_pick_up_video";
    private final int OFF = 0;
    private final int ON = 1;
    private final String SECURE_KEY = "doze_pulse_on_pick_up";
    private AmbientDisplayConfiguration mAmbientConfig;
    private final String mPickUpPrefKey;
    private final int mUserId = UserHandle.myUserId();

    public PickupGesturePreferenceController(Context context, String key) {
        super(context, key);
        this.mPickUpPrefKey = key;
    }

    public PickupGesturePreferenceController setConfig(AmbientDisplayConfiguration config) {
        this.mAmbientConfig = config;
        return this;
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        AmbientDisplayConfiguration ambientConfig = new AmbientDisplayConfiguration(context);
        if (prefs.getBoolean(PickupGestureSettings.PREF_KEY_SUGGESTION_COMPLETE, false) || !ambientConfig.pulseOnPickupAvailable()) {
            return true;
        }
        return false;
    }

    public int getAvailabilityStatus() {
        if (this.mAmbientConfig == null) {
            this.mAmbientConfig = new AmbientDisplayConfiguration(this.mContext);
        }
        if (!this.mAmbientConfig.dozePulsePickupSensorAvailable()) {
            return 2;
        }
        if (this.mAmbientConfig.ambientDisplayAvailable()) {
            return 0;
        }
        return 4;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_pick_up");
    }

    /* Access modifiers changed, original: protected */
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public boolean isChecked() {
        return this.mAmbientConfig.pulseOnPickupEnabled(this.mUserId);
    }

    public String getPreferenceKey() {
        return this.mPickUpPrefKey;
    }

    public boolean setChecked(boolean isChecked) {
        return Secure.putInt(this.mContext.getContentResolver(), "doze_pulse_on_pick_up", isChecked);
    }

    public boolean canHandleClicks() {
        return pulseOnPickupCanBeModified();
    }

    public ResultPayload getResultPayload() {
        return new InlineSwitchPayload("doze_pulse_on_pick_up", 2, 1, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, PickupGestureSettings.class.getName(), this.mPickUpPrefKey, this.mContext.getString(R.string.display_settings)), isAvailable(), 1);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean pulseOnPickupCanBeModified() {
        return this.mAmbientConfig.pulseOnPickupCanBeModified(this.mUserId);
    }
}
