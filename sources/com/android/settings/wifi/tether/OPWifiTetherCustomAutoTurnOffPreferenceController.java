package com.android.settings.wifi.tether;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class OPWifiTetherCustomAutoTurnOffPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    private static final int CUSTOM_AUTO_TURN_OFF_10_MIN_VALUE = 2;
    private static final int CUSTOM_AUTO_TURN_OFF_15_MIN_VALUE = 3;
    private static final int CUSTOM_AUTO_TURN_OFF_5_MIN_VALUE = 1;
    private static final int CUSTOM_AUTO_TURN_OFF_ALWAYS_VALUE = 0;
    private static final String PREF_KEY = "wifi_tether_custom_auto_turn_off";
    private final String[] mCustomEntries = this.mContext.getResources().getStringArray(R.array.wifi_tether_custom_auto_turn_off_summary);

    public OPWifiTetherCustomAutoTurnOffPreferenceController(Context context) {
        super(context, PREF_KEY);
    }

    public int getAvailabilityStatus() {
        return OpFeatures.isSupport(new int[]{85}) ? 0 : 2;
    }

    public String getPreferenceKey() {
        return PREF_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ListPreference preference = (ListPreference) screen.findPreference(getPreferenceKey());
        int value = Global.getInt(this.mContext.getContentResolver(), "soft_ap_timeout_enabled", 2);
        preference.setSummary(getSummaryForDisplay(value));
        preference.setValue(String.valueOf(value));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int mValue = Integer.parseInt((String) newValue);
        preference.setSummary(getSummaryForDisplay(mValue));
        Global.putInt(this.mContext.getContentResolver(), "soft_ap_timeout_enabled", mValue);
        return true;
    }

    private String getSummaryForDisplay(int value) {
        if (value == 1) {
            return this.mCustomEntries[0];
        }
        if (value == 2) {
            return this.mCustomEntries[1];
        }
        if (value == 3) {
            return this.mCustomEntries[2];
        }
        if (value == 0) {
            return this.mCustomEntries[3];
        }
        return this.mCustomEntries[0];
    }
}
