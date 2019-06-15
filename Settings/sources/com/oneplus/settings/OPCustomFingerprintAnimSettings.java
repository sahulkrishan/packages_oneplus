package com.oneplus.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.utils.OPUtils;

public class OPCustomFingerprintAnimSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener {
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_custom_fingerprint_anim_settings);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
