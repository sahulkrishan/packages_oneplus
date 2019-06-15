package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class TetheringHardwareAccelPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 1;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 0;
    private static final String TETHERING_HARDWARE_OFFLOAD = "tethering_hardware_offload";

    public TetheringHardwareAccelPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return TETHERING_HARDWARE_OFFLOAD;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), "tether_offload_disabled", ((Boolean) newValue).booleanValue() ^ 1);
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Global.getInt(this.mContext.getContentResolver(), "tether_offload_disabled", 0) != 1) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Global.putInt(this.mContext.getContentResolver(), "tether_offload_disabled", 1);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
