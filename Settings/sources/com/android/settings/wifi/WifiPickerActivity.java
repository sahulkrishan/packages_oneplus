package com.android.settings.wifi;

import android.content.Intent;
import android.support.v14.preference.PreferenceFragment;
import com.android.settings.ButtonBarHandler;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.wifi.p2p.WifiP2pSettings;

public class WifiPickerActivity extends SettingsActivity implements ButtonBarHandler {
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        if (!modIntent.hasExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT)) {
            modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, getWifiSettingsClass().getName());
            modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, R.string.wifi_select_network);
        }
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        if (WifiSettings.class.getName().equals(fragmentName) || WifiP2pSettings.class.getName().equals(fragmentName) || SavedAccessPointsWifiSettings.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public Class<? extends PreferenceFragment> getWifiSettingsClass() {
        return WifiSettings.class;
    }
}
