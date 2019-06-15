package com.oneplus.settings.controllers;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPWiFiCallingPreferenceController extends BasePreferenceController implements LifecycleObserver {
    private static final String KEY_WIFI_CALLING = "wifi_calling";
    private Preference mPreference;

    public OPWiFiCallingPreferenceController(Context context) {
        super(context, "wifi_calling");
    }

    public int getAvailabilityStatus() {
        return 3;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return "wifi_calling";
    }
}
