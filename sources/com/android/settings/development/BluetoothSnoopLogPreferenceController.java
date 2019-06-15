package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BluetoothSnoopLogPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final String BLUETOOTH_BTSNOOP_ENABLE_PROPERTY = "persist.bluetooth.btsnoopenable";
    private static final String PREFERENCE_KEY = "bt_hci_snoop_log";

    public BluetoothSnoopLogPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return PREFERENCE_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(BLUETOOTH_BTSNOOP_ENABLE_PROPERTY, Boolean.toString(((Boolean) newValue).booleanValue()));
        return true;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean(BLUETOOTH_BTSNOOP_ENABLE_PROPERTY, false));
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set(BLUETOOTH_BTSNOOP_ENABLE_PROPERTY, Boolean.toString(false));
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
