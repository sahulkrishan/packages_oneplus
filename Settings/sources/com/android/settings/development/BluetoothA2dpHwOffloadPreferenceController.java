package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BluetoothA2dpHwOffloadPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    static final String A2DP_OFFLOAD_DISABLED_PROPERTY = "persist.bluetooth.a2dp_offload.disabled";
    static final String A2DP_OFFLOAD_SUPPORTED_PROPERTY = "ro.bluetooth.a2dp_offload.supported";
    private static final String PREFERENCE_KEY = "bluetooth_disable_a2dp_hw_offload";
    private final DevelopmentSettingsDashboardFragment mFragment;

    public BluetoothA2dpHwOffloadPreferenceController(Context context, DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        this.mFragment = fragment;
    }

    public String getPreferenceKey() {
        return PREFERENCE_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        BluetoothA2dpHwOffloadRebootDialog.show(this.mFragment, this);
        return false;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (SystemProperties.getBoolean(A2DP_OFFLOAD_SUPPORTED_PROPERTY, false)) {
            ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean(A2DP_OFFLOAD_DISABLED_PROPERTY, false));
            return;
        }
        this.mPreference.setEnabled(false);
        ((SwitchPreference) this.mPreference).setChecked(true);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        if (SystemProperties.getBoolean(A2DP_OFFLOAD_SUPPORTED_PROPERTY, false)) {
            ((SwitchPreference) this.mPreference).setChecked(false);
            SystemProperties.set(A2DP_OFFLOAD_DISABLED_PROPERTY, "false");
            return;
        }
        ((SwitchPreference) this.mPreference).setChecked(true);
        SystemProperties.set(A2DP_OFFLOAD_DISABLED_PROPERTY, "true");
    }

    public void onA2dpHwDialogConfirmed() {
        SystemProperties.set(A2DP_OFFLOAD_DISABLED_PROPERTY, Boolean.toString(SystemProperties.getBoolean(A2DP_OFFLOAD_DISABLED_PROPERTY, false) ^ 1));
    }
}
