package com.oneplus.settings.utils;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPPreferenceDividerLine extends AbstractPreferenceController implements LifecycleObserver {
    private static final String KEY_PREFERENCE_DIVIDER_LINE = "preference_divider_line";
    private Preference mPreference;

    public OPPreferenceDividerLine(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_PREFERENCE_DIVIDER_LINE;
    }
}
