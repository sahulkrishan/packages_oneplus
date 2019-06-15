package com.android.settings.wifi.tether;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.widget.ValidatedEditTextPreference;
import com.android.settings.widget.ValidatedEditTextPreference.Validator;
import com.android.settings.wifi.WifiUtils;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController.OnTetherConfigUpdateListener;
import java.util.UUID;

public class WifiTetherPasswordPreferenceController extends WifiTetherBasePreferenceController implements Validator {
    private static final String PREF_KEY = "wifi_tether_network_password";
    private String mPassword;

    public WifiTetherPasswordPreferenceController(Context context, OnTetherConfigUpdateListener listener) {
        super(context, listener, PREF_KEY);
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }

    public void updateDisplay() {
        WifiConfiguration config = this.mWifiManager.getWifiApConfiguration();
        if (config == null || (config.getAuthType() == 4 && TextUtils.isEmpty(config.preSharedKey))) {
            this.mPassword = generateRandomPassword();
        } else {
            this.mPassword = config.preSharedKey;
        }
        ((ValidatedEditTextPreference) this.mPreference).setValidator(this);
        ((ValidatedEditTextPreference) this.mPreference).setIsPassword(false);
        ((ValidatedEditTextPreference) this.mPreference).setIsSummaryPassword(true);
        updatePasswordDisplay((EditTextPreference) this.mPreference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        this.mPassword = (String) newValue;
        updatePasswordDisplay((EditTextPreference) this.mPreference);
        this.mListener.onTetherConfigUpdated();
        return true;
    }

    public String getPasswordValidated(int securityType) {
        if (securityType == 0) {
            return "";
        }
        if (!isTextValid(this.mPassword)) {
            this.mPassword = generateRandomPassword();
            updatePasswordDisplay((EditTextPreference) this.mPreference);
        }
        return this.mPassword;
    }

    public void updateVisibility(int securityType) {
        this.mPreference.setVisible(securityType != 0);
    }

    public boolean isTextValid(String value) {
        return WifiUtils.isHotspotPasswordValid(value);
    }

    private static String generateRandomPassword() {
        String randomUUID = UUID.randomUUID().toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(randomUUID.substring(0, 8));
        stringBuilder.append(randomUUID.substring(9, 13));
        return stringBuilder.toString();
    }

    private void updatePasswordDisplay(EditTextPreference preference) {
        ValidatedEditTextPreference pref = (ValidatedEditTextPreference) preference;
        pref.setText(this.mPassword);
        if (TextUtils.isEmpty(this.mPassword)) {
            pref.setIsSummaryPassword(false);
            pref.setSummary((int) R.string.wifi_hotspot_no_password_subtext);
            pref.setVisible(false);
            return;
        }
        pref.setIsSummaryPassword(true);
        pref.setSummary((CharSequence) this.mPassword);
        pref.setVisible(true);
    }
}
