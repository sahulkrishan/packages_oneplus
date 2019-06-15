package com.android.settings.development;

import android.content.Context;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BugReportInPowerPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String KEY_BUGREPORT_IN_POWER = "bugreport_in_power";
    @VisibleForTesting
    static int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static int SETTING_VALUE_ON = 1;
    private final UserManager mUserManager;

    public BugReportInPowerPreferenceController(Context context) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public boolean isAvailable() {
        return this.mUserManager.hasUserRestriction("no_debugging_features") ^ 1;
    }

    public String getPreferenceKey() {
        return KEY_BUGREPORT_IN_POWER;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "bugreport_in_power_menu", ((Boolean) newValue).booleanValue() ? SETTING_VALUE_ON : SETTING_VALUE_OFF);
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(Secure.getInt(this.mContext.getContentResolver(), "bugreport_in_power_menu", SETTING_VALUE_OFF) != SETTING_VALUE_OFF);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Secure.putInt(this.mContext.getContentResolver(), "bugreport_in_power_menu", SETTING_VALUE_OFF);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
