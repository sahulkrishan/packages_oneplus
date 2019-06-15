package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BluetoothDeviceNoNamePreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_KEY = "bluetooth_show_devices_without_names";
    @VisibleForTesting
    static final String BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY = "persist.bluetooth.showdeviceswithoutnames";

    public BluetoothDeviceNoNamePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY, ((Boolean) newValue).booleanValue() ? "true" : "false");
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean(BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY, true));
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set(BLUETOOTH_SHOW_DEVICES_WITHOUT_NAMES_PROPERTY, "false");
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
