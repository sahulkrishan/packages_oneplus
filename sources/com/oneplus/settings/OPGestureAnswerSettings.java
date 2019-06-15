package com.oneplus.settings;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.utils.OPUtils;

public class OPGestureAnswerSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String GESTURE_TO_ANSWER_CALL = "gesture_answercall_switch";
    private static final String OPGUEST_ANSWER_CALL = "opguest_answer_call";
    private AnimationDrawable mAnimationDrawable;
    private SwitchPreference mGestureAnswerCall;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_gesture_answercall_settings);
        this.mGestureAnswerCall = (SwitchPreference) findPreference(GESTURE_TO_ANSWER_CALL);
        this.mGestureAnswerCall.setChecked(isGestureAnswerOn());
        this.mGestureAnswerCall.setOnPreferenceChangeListener(this);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public boolean onPreferenceChange(Preference pref, Object objValue) {
        if (pref != this.mGestureAnswerCall) {
            return false;
        }
        setGestureAnswerOn(((Boolean) objValue).booleanValue());
        return true;
    }

    private void setGestureAnswerOn(boolean enable) {
        Global.putInt(getContentResolver(), OPGUEST_ANSWER_CALL, enable);
    }

    private boolean isGestureAnswerOn() {
        return Global.getInt(getContentResolver(), OPGUEST_ANSWER_CALL, 0) == 1;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
