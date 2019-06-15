package com.android.settings.wifi.tether;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.widget.ValidatedEditTextPreference;
import com.android.settings.widget.ValidatedEditTextPreference.Validator;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController.OnTetherConfigUpdateListener;

public class WifiTetherSSIDPreferenceController extends WifiTetherBasePreferenceController implements Validator {
    @VisibleForTesting
    static final String DEFAULT_SSID = "AndroidAP";
    private static final String PREF_KEY = "wifi_tether_network_name";
    private static final String TAG = "WifiTetherSsidPref";
    private String mSSID;
    private WifiDeviceNameTextValidator mWifiDeviceNameTextValidator = new WifiDeviceNameTextValidator();

    public WifiTetherSSIDPreferenceController(Context context, OnTetherConfigUpdateListener listener) {
        super(context, listener, PREF_KEY);
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }

    public void updateDisplay() {
        WifiConfiguration config = this.mWifiManager.getWifiApConfiguration();
        String str;
        StringBuilder stringBuilder;
        if (config != null) {
            this.mSSID = config.SSID;
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Updating SSID in Preference, ");
            stringBuilder.append(this.mSSID);
            Log.d(str, stringBuilder.toString());
        } else {
            this.mSSID = DEFAULT_SSID;
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Updating to default SSID in Preference, ");
            stringBuilder.append(this.mSSID);
            Log.d(str, stringBuilder.toString());
        }
        ((ValidatedEditTextPreference) this.mPreference).setValidator(this);
        updateSsidDisplay((EditTextPreference) this.mPreference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mSSID = (String) newValue;
        updateSsidDisplay((EditTextPreference) preference);
        this.mListener.onTetherConfigUpdated();
        return true;
    }

    public boolean isTextValid(String value) {
        return this.mWifiDeviceNameTextValidator.isTextValid(value);
    }

    public String getSSID() {
        return this.mSSID;
    }

    private void updateSsidDisplay(EditTextPreference preference) {
        preference.setText(this.mSSID);
        preference.setSummary((CharSequence) this.mSSID);
    }
}
