package com.android.settings.wifi.tether;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;

public abstract class WifiTetherBasePreferenceController extends BasePreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    protected final ConnectivityManager mCm;
    protected final OnTetherConfigUpdateListener mListener;
    protected Preference mPreference;
    protected final WifiManager mWifiManager;
    protected final String[] mWifiRegexs = this.mCm.getTetherableWifiRegexs();

    public interface OnTetherConfigUpdateListener {
        void onTetherConfigUpdated();
    }

    public abstract void updateDisplay();

    public WifiTetherBasePreferenceController(Context context, OnTetherConfigUpdateListener listener, String preferenceKey) {
        super(context, preferenceKey);
        this.mListener = listener;
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mCm = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public int getAvailabilityStatus() {
        boolean state = (this.mWifiManager == null || this.mWifiRegexs == null || this.mWifiRegexs.length <= 0) ? false : true;
        if (state) {
            return 0;
        }
        return 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
        updateDisplay();
    }
}
