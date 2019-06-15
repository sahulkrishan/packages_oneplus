package com.android.settings.gestures;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.VideoPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;

public class PreventRingingPreferenceController extends BasePreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause, OnCreate, OnSaveInstanceState {
    @VisibleForTesting
    static final String KEY_VIDEO_PAUSED = "key_video_paused";
    private static final String PREF_KEY_VIDEO = "gesture_prevent_ringing_video";
    private final String SECURE_KEY = "volume_hush_gesture";
    @VisibleForTesting
    boolean mVideoPaused;
    private VideoPreference mVideoPreference;

    public PreventRingingPreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        return this.mContext.getResources().getBoolean(17957074) ? 0 : 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mVideoPreference = (VideoPreference) screen.findPreference(getVideoPrefKey());
        }
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null && (preference instanceof ListPreference)) {
            ListPreference pref = (ListPreference) preference;
            int value = Secure.getInt(this.mContext.getContentResolver(), "volume_hush_gesture", 1);
            switch (value) {
                case 1:
                    pref.setValue(String.valueOf(value));
                    return;
                case 2:
                    pref.setValue(String.valueOf(value));
                    return;
                default:
                    pref.setValue(String.valueOf(0));
                    return;
            }
        }
    }

    public CharSequence getSummary() {
        int summary;
        switch (Secure.getInt(this.mContext.getContentResolver(), "volume_hush_gesture", 1)) {
            case 1:
                summary = R.string.prevent_ringing_option_vibrate_summary;
                break;
            case 2:
                summary = R.string.prevent_ringing_option_mute_summary;
                break;
            default:
                summary = R.string.prevent_ringing_option_none_summary;
                break;
        }
        return this.mContext.getString(summary);
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mVideoPaused = savedInstanceState.getBoolean(KEY_VIDEO_PAUSED, false);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_VIDEO_PAUSED, this.mVideoPaused);
    }

    public void onPause() {
        if (this.mVideoPreference != null) {
            this.mVideoPaused = this.mVideoPreference.isVideoPaused();
            this.mVideoPreference.onViewInvisible();
        }
    }

    public void onResume() {
        if (this.mVideoPreference != null) {
            this.mVideoPreference.onViewVisible(this.mVideoPaused);
        }
    }

    /* Access modifiers changed, original: protected */
    public String getVideoPrefKey() {
        return PREF_KEY_VIDEO;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "volume_hush_gesture", Integer.parseInt((String) newValue));
        preference.setSummary(getSummary());
        return true;
    }
}
