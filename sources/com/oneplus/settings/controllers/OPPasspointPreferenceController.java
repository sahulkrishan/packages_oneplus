package com.oneplus.settings.controllers;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.OpFeatures;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;

public class OPPasspointPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnPause {
    private static final String KEY_ONEPLUS_PASSPOINT = "oneplus_passpoint";

    public OPPasspointPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_ONEPLUS_PASSPOINT);
        lifecycle.addObserver(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public int getAvailabilityStatus() {
        int i = 2;
        if (OPUtils.isSupportUss()) {
            WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
            if (!(wifiManager == null || !wifiManager.isSupportPasspoint() || OPUtils.isGuestMode())) {
                i = 0;
            }
            return i;
        }
        if (OpFeatures.isSupport(new int[]{85}) && !OPUtils.isGuestMode()) {
            i = 0;
        }
        return i;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_ONEPLUS_PASSPOINT) || !(preference instanceof SwitchPreference)) {
            return false;
        }
        System.putInt(this.mContext.getContentResolver(), KEY_ONEPLUS_PASSPOINT, ((SwitchPreference) preference).isChecked());
        return true;
    }

    public String getPreferenceKey() {
        return KEY_ONEPLUS_PASSPOINT;
    }

    public void updateState(Preference preference) {
        if (preference instanceof SwitchPreference) {
            SwitchPreference enableSwitch = (SwitchPreference) preference;
            boolean z = true;
            if (System.getInt(this.mContext.getContentResolver(), KEY_ONEPLUS_PASSPOINT, 0) != 1) {
                z = false;
            }
            enableSwitch.setChecked(z);
        }
    }
}
