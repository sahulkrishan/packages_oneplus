package com.android.settings.widget;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class MasterSwitchController extends SwitchWidgetController implements OnPreferenceChangeListener {
    private final MasterSwitchPreference mPreference;

    public MasterSwitchController(MasterSwitchPreference preference) {
        this.mPreference = preference;
    }

    public void updateTitle(boolean isChecked) {
    }

    public void startListening() {
        this.mPreference.setOnPreferenceChangeListener(this);
    }

    public void stopListening() {
        this.mPreference.setOnPreferenceChangeListener(null);
    }

    public void setChecked(boolean checked) {
        this.mPreference.setChecked(checked);
    }

    public boolean isChecked() {
        return this.mPreference.isChecked();
    }

    public void setEnabled(boolean enabled) {
        this.mPreference.setSwitchEnabled(enabled);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mListener != null) {
            return this.mListener.onSwitchToggled(((Boolean) newValue).booleanValue());
        }
        return false;
    }

    public void setDisabledByAdmin(EnforcedAdmin admin) {
        this.mPreference.setDisabledByAdmin(admin);
    }
}
