package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

public class AmbientDisplayAlwaysOnPreferenceController extends TogglePreferenceController {
    private static final int MY_USER = UserHandle.myUserId();
    private final int OFF = 0;
    private final int ON = 1;
    private OnPreferenceChangedCallback mCallback;
    private AmbientDisplayConfiguration mConfig;

    public interface OnPreferenceChangedCallback {
        void onPreferenceChanged();
    }

    public AmbientDisplayAlwaysOnPreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        if (this.mConfig == null) {
            this.mConfig = new AmbientDisplayConfiguration(this.mContext);
        }
        return isAvailable(this.mConfig) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), AmbientDisplaySettings.KEY_AMBIENT_DISPLAY_ALWAYS_ON);
    }

    public boolean isChecked() {
        return this.mConfig.alwaysOnEnabled(MY_USER);
    }

    public boolean setChecked(boolean isChecked) {
        Secure.putInt(this.mContext.getContentResolver(), "doze_always_on", isChecked);
        if (this.mCallback != null) {
            this.mCallback.onPreferenceChanged();
        }
        return true;
    }

    public AmbientDisplayAlwaysOnPreferenceController setConfig(AmbientDisplayConfiguration config) {
        this.mConfig = config;
        return this;
    }

    public AmbientDisplayAlwaysOnPreferenceController setCallback(OnPreferenceChangedCallback callback) {
        this.mCallback = callback;
        return this;
    }

    public static boolean isAlwaysOnEnabled(AmbientDisplayConfiguration config) {
        return config.alwaysOnEnabled(MY_USER);
    }

    public static boolean isAvailable(AmbientDisplayConfiguration config) {
        return config.alwaysOnAvailableForUser(MY_USER);
    }

    public static boolean accessibilityInversionEnabled(AmbientDisplayConfiguration config) {
        return config.accessibilityInversionEnabled(MY_USER);
    }

    public ResultPayload getResultPayload() {
        return new InlineSwitchPayload("doze_always_on", 2, 1, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, AmbientDisplaySettings.class.getName(), getPreferenceKey(), this.mContext.getString(R.string.ambient_display_screen_title)), isAvailable(), 1);
    }
}
