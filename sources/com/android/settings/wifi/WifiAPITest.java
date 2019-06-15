package com.android.settings.wifi;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.widget.EditText;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class WifiAPITest extends SettingsPreferenceFragment implements OnPreferenceClickListener {
    private static final String KEY_DISABLE_NETWORK = "disable_network";
    private static final String KEY_DISCONNECT = "disconnect";
    private static final String KEY_ENABLE_NETWORK = "enable_network";
    private static final String TAG = "WifiAPITest";
    private Preference mWifiDisableNetwork;
    private Preference mWifiDisconnect;
    private Preference mWifiEnableNetwork;
    private WifiManager mWifiManager;
    private int netid;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.layout.wifi_api_test);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        this.mWifiDisconnect = preferenceScreen.findPreference(KEY_DISCONNECT);
        this.mWifiDisconnect.setOnPreferenceClickListener(this);
        this.mWifiDisableNetwork = preferenceScreen.findPreference(KEY_DISABLE_NETWORK);
        this.mWifiDisableNetwork.setOnPreferenceClickListener(this);
        this.mWifiEnableNetwork = preferenceScreen.findPreference(KEY_ENABLE_NETWORK);
        this.mWifiEnableNetwork.setOnPreferenceClickListener(this);
    }

    public int getMetricsCategory() {
        return 89;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        return false;
    }

    public boolean onPreferenceClick(Preference pref) {
        Builder alert;
        final EditText input;
        if (pref == this.mWifiDisconnect) {
            this.mWifiManager.disconnect();
        } else if (pref == this.mWifiDisableNetwork) {
            alert = new Builder(getContext());
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            input = new EditText(getPrefContext());
            alert.setView(input);
            alert.setPositiveButton("Ok", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    try {
                        WifiAPITest.this.netid = Integer.parseInt(input.getText().toString());
                        WifiAPITest.this.mWifiManager.disableNetwork(WifiAPITest.this.netid);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            });
            alert.setNegativeButton("Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        } else if (pref == this.mWifiEnableNetwork) {
            alert = new Builder(getContext());
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            input = new EditText(getPrefContext());
            alert.setView(input);
            alert.setPositiveButton("Ok", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    WifiAPITest.this.netid = Integer.parseInt(input.getText().toString());
                    WifiAPITest.this.mWifiManager.enableNetwork(WifiAPITest.this.netid, false);
                }
            });
            alert.setNegativeButton("Cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            alert.show();
        }
        return true;
    }
}
