package com.android.settings;

import android.os.Bundle;
import android.os.UserManager;
import android.support.v7.preference.PreferenceScreen;

public class TestingSettings extends SettingsPreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.testing_settings);
        if (!UserManager.get(getContext()).isAdminUser()) {
            getPreferenceScreen().removePreference((PreferenceScreen) findPreference("radio_info_settings"));
        }
    }

    public int getMetricsCategory() {
        return 89;
    }
}
