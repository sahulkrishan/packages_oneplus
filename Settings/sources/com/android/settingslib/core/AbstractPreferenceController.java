package com.android.settingslib.core;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;

public abstract class AbstractPreferenceController {
    protected final Context mContext;

    public abstract String getPreferenceKey();

    public abstract boolean isAvailable();

    public AbstractPreferenceController(Context context) {
        this.mContext = context;
    }

    public void displayPreference(PreferenceScreen screen) {
        String prefKey = getPreferenceKey();
        if (isAvailable()) {
            setVisible(screen, prefKey, true);
            if (this instanceof OnPreferenceChangeListener) {
                screen.findPreference(prefKey).setOnPreferenceChangeListener((OnPreferenceChangeListener) this);
                return;
            }
            return;
        }
        setVisible(screen, prefKey, false);
    }

    public void updateState(Preference preference) {
        refreshSummary(preference);
    }

    /* Access modifiers changed, original: protected */
    public void refreshSummary(Preference preference) {
        if (preference != null) {
            CharSequence summary = getSummary();
            if (summary != null) {
                preference.setSummary(summary);
            }
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        return false;
    }

    /* Access modifiers changed, original: protected|final */
    public final void setVisible(PreferenceGroup group, String key, boolean isVisible) {
        Preference pref = group.findPreference(key);
        if (pref != null) {
            pref.setVisible(isVisible);
        }
    }

    public CharSequence getSummary() {
        return null;
    }
}
