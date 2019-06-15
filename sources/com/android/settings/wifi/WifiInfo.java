package com.android.settings.wifi;

import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class WifiInfo extends SettingsPreferenceFragment {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.testing_wifi_settings);
    }

    public int getMetricsCategory() {
        return 89;
    }
}
