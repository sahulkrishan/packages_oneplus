package com.android.settings.display;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.FeatureFlagUtils;
import com.android.settings.core.BasePreferenceController;

public class SystemUiThemePreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    private ListPreference mSystemUiThemePref;

    public SystemUiThemePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public int getAvailabilityStatus() {
        return FeatureFlagUtils.isEnabled(this.mContext, "settings_systemui_theme") ^ 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSystemUiThemePref = (ListPreference) screen.findPreference(getPreferenceKey());
        this.mSystemUiThemePref.setValue(Integer.toString(Secure.getInt(this.mContext.getContentResolver(), "theme_mode", 0)));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "theme_mode", Integer.parseInt((String) newValue));
        refreshSummary(preference);
        return true;
    }

    public CharSequence getSummary() {
        return this.mSystemUiThemePref.getEntries()[this.mSystemUiThemePref.findIndexOfValue(Integer.toString(Secure.getInt(this.mContext.getContentResolver(), "theme_mode", 0)))];
    }
}
