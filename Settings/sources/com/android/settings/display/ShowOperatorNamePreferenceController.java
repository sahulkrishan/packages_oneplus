package com.android.settings.display;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class ShowOperatorNamePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_SHOW_OPERATOR_NAME = "show_operator_name";

    public ShowOperatorNamePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_showOperatorNameInStatusBar);
    }

    public String getPreferenceKey() {
        return KEY_SHOW_OPERATOR_NAME;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), KEY_SHOW_OPERATOR_NAME, ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = true;
        SwitchPreference switchPreference = (SwitchPreference) preference;
        if (Secure.getInt(this.mContext.getContentResolver(), KEY_SHOW_OPERATOR_NAME, 1) == 0) {
            z = false;
        }
        switchPreference.setChecked(z);
    }
}
