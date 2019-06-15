package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public abstract class GlobalSettingSwitchPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final int SETTING_VALUE_OFF = 0;
    private static final int SETTING_VALUE_ON = 1;
    private final int mDefault;
    private final int mOff;
    private final int mOn;
    private final String mSettingsKey;

    public GlobalSettingSwitchPreferenceController(Context context, String globalSettingsKey) {
        this(context, globalSettingsKey, 1, 0, 0);
    }

    public GlobalSettingSwitchPreferenceController(Context context, String globalSettingsKey, int valueOn, int valueOff, int valueDefault) {
        super(context);
        this.mSettingsKey = globalSettingsKey;
        this.mOn = valueOn;
        this.mOff = valueOff;
        this.mDefault = valueDefault;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), this.mSettingsKey, ((Boolean) newValue).booleanValue() ? this.mOn : this.mOff);
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(Global.getInt(this.mContext.getContentResolver(), this.mSettingsKey, this.mDefault) != this.mOff);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Global.putInt(this.mContext.getContentResolver(), this.mSettingsKey, this.mOff);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
