package com.android.settings.gestures;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.widget.VideoPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;

public abstract class GesturePreferenceController extends TogglePreferenceController implements OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause, OnCreate, OnSaveInstanceState {
    @VisibleForTesting
    static final String KEY_VIDEO_PAUSED = "key_video_paused";
    @VisibleForTesting
    boolean mVideoPaused;
    private VideoPreference mVideoPreference;

    public abstract String getVideoPrefKey();

    public GesturePreferenceController(Context context, String key) {
        super(context, key);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mVideoPreference = (VideoPreference) screen.findPreference(getVideoPrefKey());
        }
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            preference.setEnabled(canHandleClicks());
        }
    }

    public CharSequence getSummary() {
        return this.mContext.getText(isChecked() ? R.string.gesture_setting_on : R.string.gesture_setting_off);
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
    public boolean canHandleClicks() {
        return true;
    }
}
