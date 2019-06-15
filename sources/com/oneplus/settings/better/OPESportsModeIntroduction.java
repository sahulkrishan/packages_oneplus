package com.oneplus.settings.better;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.telephony.TelephonyManager;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;

public class OPESportsModeIntroduction extends SettingsPreferenceFragment {
    private static final String KEY_ONEPLUS_E_SPORTS_MODE_NETWORK_INTRODUCTION = "oneplus_e_sports_mode_network_introduction";
    private Preference mNetworkPre;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_esport_mode_introduction);
        initNetworkPreference();
    }

    private void initNetworkPreference() {
        this.mNetworkPre = findPreference(KEY_ONEPLUS_E_SPORTS_MODE_NETWORK_INTRODUCTION);
        if (this.mNetworkPre != null) {
            if (isSupportDualLTEProject() && isDualSimCard()) {
                this.mNetworkPre.setSummary(getActivity().getString(R.string.oneplus_e_sports_mode_network_introduction_dual4g_summary));
            } else {
                this.mNetworkPre.setSummary(getActivity().getString(R.string.oneplus_e_sports_mode_network_introduction_summary));
            }
        }
    }

    private boolean isDualSimCard() {
        if (((TelephonyManager) getActivity().getSystemService("phone")).getPhoneCount() == 2) {
            return true;
        }
        return false;
    }

    public static boolean isSupportDualLTEProject() {
        return SettingsBaseApplication.mApplication.getResources().getBoolean(17956947);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
