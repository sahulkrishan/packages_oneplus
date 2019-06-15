package com.android.settings.display;

import android.app.UiModeManager;
import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class NightModePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String TAG = "NightModePrefContr";

    public NightModePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return false;
    }

    public String getPreferenceKey() {
        return KEY_NIGHT_MODE;
    }

    public void displayPreference(PreferenceScreen screen) {
        if (isAvailable()) {
            ListPreference mNightModePreference = (ListPreference) screen.findPreference(KEY_NIGHT_MODE);
            if (mNightModePreference != null) {
                mNightModePreference.setValue(String.valueOf(((UiModeManager) this.mContext.getSystemService("uimode")).getNightMode()));
                mNightModePreference.setOnPreferenceChangeListener(this);
            }
            return;
        }
        setVisible(screen, KEY_NIGHT_MODE, false);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            ((UiModeManager) this.mContext.getSystemService("uimode")).setNightMode(Integer.parseInt((String) newValue));
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist night mode setting", e);
            return false;
        }
    }
}
