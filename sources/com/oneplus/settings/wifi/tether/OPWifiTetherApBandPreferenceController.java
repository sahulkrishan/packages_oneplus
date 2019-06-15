package com.oneplus.settings.wifi.tether;

import android.content.Context;
import android.content.res.Resources;
import android.icu.text.ListFormatter;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.wifi.WifiUtils;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController.OnTetherConfigUpdateListener;
import com.oneplus.settings.widget.OPHotspotApBandSelectionPreference;

public class OPWifiTetherApBandPreferenceController extends WifiTetherBasePreferenceController {
    public static final String[] BAND_VALUES = new String[]{String.valueOf(0), String.valueOf(1)};
    private static final String PREF_KEY = "wifi_tether_network_ap_band_single_select";
    private static final String TAG = "OPWifiTetherApBandPref";
    private final String[] mBandEntries;
    private int mBandIndex;
    private final String[] mBandSummaries;

    public OPWifiTetherApBandPreferenceController(Context context, OnTetherConfigUpdateListener listener) {
        super(context, listener, PREF_KEY);
        Resources res = this.mContext.getResources();
        this.mBandEntries = res.getStringArray(R.array.wifi_ap_band_config_full);
        this.mBandSummaries = res.getStringArray(R.array.wifi_ap_band_summary_full);
    }

    public int getAvailabilityStatus() {
        if (WifiUtils.isSupportDualBand()) {
            return 1;
        }
        return super.getAvailabilityStatus();
    }

    public void updateDisplay() {
        WifiConfiguration config = this.mWifiManager.getWifiApConfiguration();
        int tempBandIndex = this.mBandIndex;
        String str;
        StringBuilder stringBuilder;
        if (config == null) {
            this.mBandIndex = 0;
            Log.d(TAG, "Updating band index to 0 because no config");
        } else if (is5GhzBandSupported()) {
            this.mBandIndex = config.apBand;
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Updating band index to ");
            stringBuilder.append(this.mBandIndex);
            Log.d(str, stringBuilder.toString());
        } else {
            config.apBand = 0;
            this.mWifiManager.setWifiApConfiguration(config);
            this.mBandIndex = config.apBand;
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("5Ghz not supported, updating band index to ");
            stringBuilder.append(this.mBandIndex);
            Log.d(str, stringBuilder.toString());
        }
        OPHotspotApBandSelectionPreference preference = this.mPreference;
        if (this.mBandIndex >= this.mBandEntries.length) {
            this.mBandIndex = tempBandIndex;
        }
        if (is5GhzBandSupported()) {
            preference.setExistingConfigValue(config.apBand);
            preference.setSummary((CharSequence) getConfigSummary());
            return;
        }
        preference.setEnabled(false);
        preference.setSummary((int) R.string.wifi_ap_choose_2G);
    }

    /* Access modifiers changed, original: 0000 */
    public String getConfigSummary() {
        if (this.mBandIndex == -1) {
            return ListFormatter.getInstance().format((Object[]) this.mBandSummaries);
        }
        return this.mBandSummaries[this.mBandIndex];
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mBandIndex = ((Integer) newValue).intValue();
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Band preference changed, updating band index to ");
        stringBuilder.append(this.mBandIndex);
        Log.d(str, stringBuilder.toString());
        preference.setSummary(getConfigSummary());
        this.mListener.onTetherConfigUpdated();
        return true;
    }

    private boolean is5GhzBandSupported() {
        String countryCode = this.mWifiManager.getCountryCode();
        if (!this.mWifiManager.isDualBandSupported() || countryCode == null) {
            return false;
        }
        return true;
    }

    public int getBandIndex() {
        return this.mBandIndex;
    }
}
