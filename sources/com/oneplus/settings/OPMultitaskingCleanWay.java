package com.oneplus.settings;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.System;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;

public class OPMultitaskingCleanWay extends SettingsPreferenceFragment implements OnClickListener {
    private static final String KEY_DEEP_CLEAR_WAY = "op_deep_clear_way";
    private static final String KEY_NORMAL_CLEAR_WAY = "op_normal_clear_way";
    private static final int METRICSLOGGER_MULTITASKING_CLEARWAY_VALUE = 1262;
    private Context mContext;
    private RadioButtonPreference mDeepClearWayButton;
    private RadioButtonPreference mNormalClearWayButton;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_multitasking_clean_way);
        this.mContext = SettingsBaseApplication.mApplication;
        this.mNormalClearWayButton = (RadioButtonPreference) findPreference(KEY_NORMAL_CLEAR_WAY);
        this.mDeepClearWayButton = (RadioButtonPreference) findPreference(KEY_DEEP_CLEAR_WAY);
        this.mNormalClearWayButton.setOnClickListener(this);
        this.mDeepClearWayButton.setOnClickListener(this);
    }

    public void onResume() {
        if (!(this.mNormalClearWayButton == null || this.mDeepClearWayButton == null)) {
            boolean z = false;
            int value = System.getInt(this.mContext.getContentResolver(), "oem_clear_way", 0);
            this.mNormalClearWayButton.setChecked(value == 0);
            RadioButtonPreference radioButtonPreference = this.mDeepClearWayButton;
            if (value == 1) {
                z = true;
            }
            radioButtonPreference.setChecked(z);
        }
        super.onResume();
    }

    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        if (emiter == this.mNormalClearWayButton) {
            this.mNormalClearWayButton.setChecked(true);
            this.mDeepClearWayButton.setChecked(false);
            System.putInt(this.mContext.getContentResolver(), "oem_clear_way", 0);
        } else if (emiter == this.mDeepClearWayButton) {
            this.mNormalClearWayButton.setChecked(false);
            this.mDeepClearWayButton.setChecked(true);
            System.putInt(this.mContext.getContentResolver(), "oem_clear_way", 1);
        }
    }

    public int getMetricsCategory() {
        return METRICSLOGGER_MULTITASKING_CLEARWAY_VALUE;
    }
}
