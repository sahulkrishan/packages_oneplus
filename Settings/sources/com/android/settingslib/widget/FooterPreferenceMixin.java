package com.android.settingslib.widget;

import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.SetPreferenceScreen;

public class FooterPreferenceMixin implements LifecycleObserver, SetPreferenceScreen {
    private FooterPreference mFooterPreference;
    private final PreferenceFragment mFragment;

    public FooterPreferenceMixin(PreferenceFragment fragment, Lifecycle lifecycle) {
        this.mFragment = fragment;
        lifecycle.addObserver(this);
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (this.mFooterPreference != null) {
            preferenceScreen.addPreference(this.mFooterPreference);
        }
    }

    public FooterPreference createFooterPreference() {
        PreferenceScreen screen = this.mFragment.getPreferenceScreen();
        if (!(this.mFooterPreference == null || screen == null)) {
            screen.removePreference(this.mFooterPreference);
        }
        this.mFooterPreference = new FooterPreference(getPrefContext());
        if (screen != null) {
            screen.addPreference(this.mFooterPreference);
        }
        return this.mFooterPreference;
    }

    private Context getPrefContext() {
        return this.mFragment.getPreferenceManager().getContext();
    }

    public boolean hasFooter() {
        return this.mFooterPreference != null;
    }
}
