package com.oneplus.settings;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnDestroy;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.settings.widget.OPVideoPreference;

public class OPVideoEnhancerPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause, OnDestroy {
    @VisibleForTesting
    static final String KEY_VIDEO_SOURCE = "video_source";
    private OPVideoPreference mVideoPreference;

    public OPVideoEnhancerPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_VIDEO_SOURCE);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public int getAvailabilityStatus() {
        return OPUtils.isSupportVideoEnhancer() ? 0 : 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mVideoPreference = (OPVideoPreference) screen.findPreference(KEY_VIDEO_SOURCE);
    }

    public void onPause() {
        if (this.mVideoPreference != null) {
            this.mVideoPreference.setVideoPaused();
        }
    }

    public void onDestroy() {
        if (this.mVideoPreference != null) {
            this.mVideoPreference.release();
        }
    }

    public void onResume() {
        if (this.mVideoPreference != null) {
            this.mVideoPreference.setVideoResume();
        }
    }
}
