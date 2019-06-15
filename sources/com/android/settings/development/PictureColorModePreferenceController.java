package com.android.settings.development;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class PictureColorModePreferenceController extends DeveloperOptionsPreferenceController implements LifecycleObserver, OnResume, OnPause, PreferenceControllerMixin {
    private static final String KEY_COLOR_MODE = "picture_color_mode";
    private ColorModePreference mPreference;

    public PictureColorModePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return getColorModeDescriptionsSize() > 1 && !isWideColorGamut();
    }

    public String getPreferenceKey() {
        return KEY_COLOR_MODE;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (ColorModePreference) screen.findPreference(getPreferenceKey());
        if (this.mPreference != null) {
            this.mPreference.updateCurrentAndSupported();
        }
    }

    public void onResume() {
        if (this.mPreference != null) {
            this.mPreference.startListening();
            this.mPreference.updateCurrentAndSupported();
        }
    }

    public void onPause() {
        if (this.mPreference != null) {
            this.mPreference.stopListening();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isWideColorGamut() {
        return this.mContext.getResources().getConfiguration().isScreenWideColorGamut();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getColorModeDescriptionsSize() {
        return ColorModePreference.getColorModeDescriptions(this.mContext).size();
    }
}
