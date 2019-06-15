package com.android.settings.gestures;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;

public class AssistGestureSettingsPreferenceController extends GesturePreferenceController {
    private static final int OFF = 0;
    private static final int ON = 1;
    private static final String PREF_KEY_VIDEO = "gesture_assist_video";
    private static final String SECURE_KEY_ASSIST = "assist_gesture_enabled";
    private static final String SECURE_KEY_SILENCE = "assist_gesture_silence_alerts_enabled";
    private final String mAssistGesturePrefKey;
    @VisibleForTesting
    boolean mAssistOnly;
    private final AssistGestureFeatureProvider mFeatureProvider;
    private Preference mPreference;
    private PreferenceScreen mScreen;
    private boolean mWasAvailable = isAvailable();

    public AssistGestureSettingsPreferenceController(Context context, String key) {
        super(context, key);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getAssistGestureFeatureProvider();
        this.mAssistGesturePrefKey = key;
    }

    public int getAvailabilityStatus() {
        boolean isAvailable;
        if (this.mAssistOnly) {
            isAvailable = this.mFeatureProvider.isSupported(this.mContext);
        } else {
            isAvailable = this.mFeatureProvider.isSensorAvailable(this.mContext);
        }
        return isAvailable ? 0 : 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mScreen = screen;
        this.mPreference = screen.findPreference(getPreferenceKey());
        super.displayPreference(screen);
    }

    public void onResume() {
        if (this.mWasAvailable != isAvailable()) {
            updatePreference();
            this.mWasAvailable = isAvailable();
        }
    }

    public AssistGestureSettingsPreferenceController setAssistOnly(boolean assistOnly) {
        this.mAssistOnly = assistOnly;
        return this;
    }

    private void updatePreference() {
        if (this.mPreference != null) {
            if (!isAvailable()) {
                this.mScreen.removePreference(this.mPreference);
            } else if (this.mScreen.findPreference(getPreferenceKey()) == null) {
                this.mScreen.addPreference(this.mPreference);
            }
        }
    }

    private boolean isAssistGestureEnabled() {
        return Secure.getInt(this.mContext.getContentResolver(), SECURE_KEY_ASSIST, 1) != 0;
    }

    private boolean isSilenceGestureEnabled() {
        return Secure.getInt(this.mContext.getContentResolver(), SECURE_KEY_SILENCE, 1) != 0;
    }

    public boolean setChecked(boolean isChecked) {
        return Secure.putInt(this.mContext.getContentResolver(), SECURE_KEY_ASSIST, isChecked);
    }

    /* Access modifiers changed, original: protected */
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public CharSequence getSummary() {
        boolean z = false;
        boolean isEnabled = isAssistGestureEnabled() && this.mFeatureProvider.isSupported(this.mContext);
        if (!this.mAssistOnly) {
            if (isEnabled || isSilenceGestureEnabled()) {
                z = true;
            }
            isEnabled = z;
        }
        return this.mContext.getText(isEnabled ? R.string.gesture_setting_on : R.string.gesture_setting_off);
    }

    public boolean isChecked() {
        return Secure.getInt(this.mContext.getContentResolver(), SECURE_KEY_ASSIST, 0) == 1;
    }

    public ResultPayload getResultPayload() {
        return new InlineSwitchPayload(SECURE_KEY_ASSIST, 2, 1, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, AssistGestureSettings.class.getName(), this.mAssistGesturePrefKey, this.mContext.getString(R.string.display_settings)), isAvailable(), 1);
    }
}
