package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class BluetoothDelayReportsPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final String BLUETOOTH_DISABLE_AVDTP_DELAY_REPORTS_PROPERTY = "persist.bluetooth.disabledelayreports";
    private static final String BLUETOOTH_DISABLE_AVDTP_DELAY_REPORT_KEY = "bluetooth_disable_avdtp_delay_reports";

    public BluetoothDelayReportsPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return BLUETOOTH_DISABLE_AVDTP_DELAY_REPORT_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(BLUETOOTH_DISABLE_AVDTP_DELAY_REPORTS_PROPERTY, ((Boolean) newValue).booleanValue() ? "true" : "false");
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean(BLUETOOTH_DISABLE_AVDTP_DELAY_REPORTS_PROPERTY, false));
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        ((SwitchPreference) this.mPreference).setChecked(false);
        SystemProperties.set(BLUETOOTH_DISABLE_AVDTP_DELAY_REPORTS_PROPERTY, "false");
    }
}
