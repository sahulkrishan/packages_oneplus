package com.android.settings.development;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class FreeformWindowsPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String ENABLE_FREEFORM_SUPPORT_KEY = "enable_freeform_support";
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    @VisibleForTesting
    static final String USER_BUILD_TYPE = "user";

    public FreeformWindowsPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return TextUtils.equals(USER_BUILD_TYPE, getBuildType()) ^ 1;
    }

    public String getPreferenceKey() {
        return "enable_freeform_support";
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "enable_freeform_support", ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Global.getInt(this.mContext.getContentResolver(), "enable_freeform_support", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Global.putInt(this.mContext.getContentResolver(), "enable_freeform_support", 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getBuildType() {
        return Build.TYPE;
    }
}
