package com.android.settings.development;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class WifiVerboseLoggingPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;
    private static final String WIFI_VERBOSE_LOGGING_KEY = "wifi_verbose_logging";
    private final WifiManager mWifiManager;

    public WifiVerboseLoggingPreferenceController(Context context) {
        super(context);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    public String getPreferenceKey() {
        return WIFI_VERBOSE_LOGGING_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mWifiManager.enableVerboseLogging(((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(this.mWifiManager.getVerboseLoggingLevel() > 0);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        this.mWifiManager.enableVerboseLogging(0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
