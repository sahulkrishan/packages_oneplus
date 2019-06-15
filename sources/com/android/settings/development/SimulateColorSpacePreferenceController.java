package com.android.settings.development;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class SimulateColorSpacePreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    private static final String SIMULATE_COLOR_SPACE = "simulate_color_space";

    public SimulateColorSpacePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return SIMULATE_COLOR_SPACE;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeSimulateColorSpace(newValue);
        return true;
    }

    public void updateState(Preference preference) {
        updateSimulateColorSpace();
    }

    public void onDeveloperOptionsDisabled() {
        super.onDeveloperOptionsDisabled();
        if (usingDevelopmentColorSpace()) {
            writeSimulateColorSpace(Integer.valueOf(-1));
        }
    }

    private void updateSimulateColorSpace() {
        ContentResolver cr = this.mContext.getContentResolver();
        ListPreference listPreference = this.mPreference;
        if (Secure.getInt(cr, "accessibility_display_daltonizer_enabled", 0) != 0) {
            String mode = Integer.toString(Secure.getInt(cr, "accessibility_display_daltonizer", -1));
            listPreference.setValue(mode);
            if (listPreference.findIndexOfValue(mode) < 0) {
                listPreference.setSummary(this.mContext.getResources().getString(R.string.daltonizer_type_overridden, new Object[]{this.mContext.getResources().getString(R.string.accessibility_display_daltonizer_preference_title)}));
                return;
            } else {
                listPreference.setSummary("%s");
                return;
            }
        }
        listPreference.setValue(Integer.toString(-1));
    }

    private void writeSimulateColorSpace(Object value) {
        ContentResolver cr = this.mContext.getContentResolver();
        int newMode = Integer.parseInt(value.toString());
        if (newMode < 0) {
            Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 0);
            return;
        }
        Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 1);
        Secure.putInt(cr, "accessibility_display_daltonizer", newMode);
    }

    private boolean usingDevelopmentColorSpace() {
        ContentResolver cr = this.mContext.getContentResolver();
        if (Secure.getInt(cr, "accessibility_display_daltonizer_enabled", 0) != 0) {
            if (((ListPreference) this.mPreference).findIndexOfValue(Integer.toString(Secure.getInt(cr, "accessibility_display_daltonizer", -1))) >= 0) {
                return true;
            }
        }
        return false;
    }
}
