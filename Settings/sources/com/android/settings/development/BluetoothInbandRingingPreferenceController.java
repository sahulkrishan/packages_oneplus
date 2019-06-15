package com.android.settings.development;

import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BluetoothInbandRingingPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String BLUETOOTH_DISABLE_INBAND_RINGING_KEY = "bluetooth_disable_inband_ringing";
    @VisibleForTesting
    static final String BLUETOOTH_DISABLE_INBAND_RINGING_PROPERTY = "persist.bluetooth.disableinbandringing";

    public BluetoothInbandRingingPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return isInbandRingingSupported();
    }

    public String getPreferenceKey() {
        return BLUETOOTH_DISABLE_INBAND_RINGING_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(BLUETOOTH_DISABLE_INBAND_RINGING_PROPERTY, ((Boolean) newValue).booleanValue() ? "true" : "false");
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean(BLUETOOTH_DISABLE_INBAND_RINGING_PROPERTY, false));
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        ((SwitchPreference) this.mPreference).setChecked(false);
        SystemProperties.set(BLUETOOTH_DISABLE_INBAND_RINGING_PROPERTY, "false");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isInbandRingingSupported() {
        return BluetoothHeadset.isInbandRingingSupported(this.mContext);
    }
}
