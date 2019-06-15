package com.oneplus.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.core.BasePreferenceController;

public class OPCarrierConfigVersionPreferenceController extends BasePreferenceController {
    private static final String ACTION_STARTUP = "oneplus.intent.action.CARRIER_CONFIG_VERSION";
    private static final String KEY_CARRIER_CONFIG = "carrier_config_version";
    private static final String PROPERTY_CONFIG_VERSION = "op_carrier_config_version";
    private Context mContext;

    public OPCarrierConfigVersionPreferenceController(Context context) {
        super(context, KEY_CARRIER_CONFIG);
        this.mContext = context;
    }

    public int getAvailabilityStatus() {
        return TextUtils.isEmpty(getLatestVersionCode());
    }

    public String getPreferenceKey() {
        return KEY_CARRIER_CONFIG;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        screen.findPreference(getPreferenceKey()).setSummary(getLatestVersionCode());
    }

    public void updateState(Preference preference) {
        preference.setSummary(getLatestVersionCode());
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_CARRIER_CONFIG)) {
            return false;
        }
        try {
            this.mContext.startActivity(new Intent(ACTION_STARTUP));
        } catch (ActivityNotFoundException exception) {
            exception.printStackTrace();
        }
        return true;
    }

    private String getLatestVersionCode() {
        String versionCode = System.getString(this.mContext.getContentResolver(), PROPERTY_CONFIG_VERSION);
        return versionCode == null ? "" : versionCode;
    }
}
