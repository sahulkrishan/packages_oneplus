package com.android.settings.development;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class WifiCoverageExtendPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String WIFI_COVERAGE_EXTEND_KEY = "wifi_coverage_extend";
    private final WifiManager mWifiManager;

    public WifiCoverageExtendPreferenceController(Context context) {
        super(context);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
    }

    public String getPreferenceKey() {
        return WIFI_COVERAGE_EXTEND_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mWifiManager.enableWifiCoverageExtendFeature(((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(this.mWifiManager.isWifiCoverageExtendFeatureEnabled());
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        this.mWifiManager.enableWifiCoverageExtendFeature(false);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
