package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class ShowFirstCrashDialogPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    private static final String SHOW_FIRST_CRASH_DIALOG_KEY = "show_first_crash_dialog";

    public ShowFirstCrashDialogPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return SHOW_FIRST_CRASH_DIALOG_KEY;
    }

    public boolean isAvailable() {
        return Global.getInt(this.mContext.getContentResolver(), SHOW_FIRST_CRASH_DIALOG_KEY, 0) == 0;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "show_first_crash_dialog_dev_option", ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Secure.getInt(this.mContext.getContentResolver(), "show_first_crash_dialog_dev_option", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Secure.putInt(this.mContext.getContentResolver(), "show_first_crash_dialog_dev_option", 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
