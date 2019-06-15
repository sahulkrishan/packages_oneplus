package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public abstract class SecureSettingSwitchPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final int SETTING_VALUE_OFF = 0;
    private static final int SETTING_VALUE_ON = 1;
    private final String mSettingsKey;

    public SecureSettingSwitchPreferenceController(Context context, String secureSettingsKey) {
        super(context);
        this.mSettingsKey = secureSettingsKey;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), this.mSettingsKey, ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Secure.getInt(this.mContext.getContentResolver(), this.mSettingsKey, 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Secure.putInt(this.mContext.getContentResolver(), this.mSettingsKey, 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
