package com.android.settings.notification;

import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.v7.preference.PreferenceScreen;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.notification.VolumeSeekBarPreference.Callback;

public abstract class VolumeSeekBarPreferenceController extends AdjustVolumeRestrictedPreferenceController implements LifecycleObserver {
    protected AudioHelper mHelper;
    protected VolumeSeekBarPreference mPreference;
    protected Callback mVolumePreferenceCallback;

    public abstract int getAudioStream();

    public abstract int getMuteIcon();

    public VolumeSeekBarPreferenceController(Context context, String key) {
        super(context, key);
        setAudioHelper(new AudioHelper(context));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setAudioHelper(AudioHelper helper) {
        this.mHelper = helper;
    }

    public void setCallback(Callback callback) {
        this.mVolumePreferenceCallback = callback;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            this.mPreference = (VolumeSeekBarPreference) screen.findPreference(getPreferenceKey());
            this.mPreference.setCallback(this.mVolumePreferenceCallback);
            this.mPreference.setStream(getAudioStream());
            this.mPreference.setMuteIcon(getMuteIcon());
        }
    }

    @OnLifecycleEvent(Event.ON_RESUME)
    public void onResume() {
        if (this.mPreference != null) {
            this.mPreference.onActivityResume();
        }
    }

    @OnLifecycleEvent(Event.ON_PAUSE)
    public void onPause() {
        if (this.mPreference != null) {
            this.mPreference.onActivityPause();
        }
    }

    public int getSliderPosition() {
        if (this.mPreference != null) {
            return this.mPreference.getProgress();
        }
        return this.mHelper.getStreamVolume(getAudioStream());
    }

    public boolean setSliderPosition(int position) {
        if (this.mPreference != null) {
            this.mPreference.setProgress(position);
        }
        return this.mHelper.setStreamVolume(getAudioStream(), position);
    }

    public int getMaxSteps() {
        if (this.mPreference != null) {
            return this.mPreference.getMax();
        }
        return this.mHelper.getMaxVolume(getAudioStream());
    }
}
