package com.android.settings.wifi.tether;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController.OnTetherConfigUpdateListener;

public class WifiTetherSecurityPreferenceController extends WifiTetherBasePreferenceController {
    private static final String PREF_KEY = "wifi_tether_security";
    private final String[] mSecurityEntries = this.mContext.getResources().getStringArray(R.array.wifi_tether_security);
    private int mSecurityValue;

    public WifiTetherSecurityPreferenceController(Context context, OnTetherConfigUpdateListener listener) {
        super(context, listener, PREF_KEY);
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }

    public void updateDisplay() {
        WifiConfiguration config = this.mWifiManager.getWifiApConfiguration();
        if (config == null || config.getAuthType() != 0) {
            this.mSecurityValue = 4;
        } else {
            this.mSecurityValue = 0;
        }
        ListPreference preference = this.mPreference;
        preference.setSummary(getSummaryForSecurityType(this.mSecurityValue));
        preference.setValue(String.valueOf(this.mSecurityValue));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mSecurityValue = Integer.parseInt((String) newValue);
        preference.setSummary(getSummaryForSecurityType(this.mSecurityValue));
        this.mListener.onTetherConfigUpdated();
        return true;
    }

    public int getSecurityType() {
        return this.mSecurityValue;
    }

    private String getSummaryForSecurityType(int securityType) {
        if (securityType == 0) {
            return this.mSecurityEntries[1];
        }
        return this.mSecurityEntries[0];
    }
}
