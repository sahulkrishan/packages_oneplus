package com.android.settingslib.development;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.core.AbstractPreferenceController;

public abstract class DeveloperOptionsPreferenceController extends AbstractPreferenceController {
    protected Preference mPreference;

    public DeveloperOptionsPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public void onDeveloperOptionsEnabled() {
        if (isAvailable()) {
            onDeveloperOptionsSwitchEnabled();
        }
    }

    public void onDeveloperOptionsDisabled() {
        if (isAvailable()) {
            onDeveloperOptionsSwitchDisabled();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchEnabled() {
        this.mPreference.setEnabled(true);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        this.mPreference.setEnabled(false);
    }
}
