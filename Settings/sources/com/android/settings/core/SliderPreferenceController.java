package com.android.settings.core;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.widget.SeekBarPreference;

public abstract class SliderPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    public abstract int getMaxSteps();

    public abstract int getSliderPosition();

    public abstract boolean setSliderPosition(int i);

    public SliderPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return setSliderPosition(((Integer) newValue).intValue());
    }

    public void updateState(Preference preference) {
        if (preference instanceof SeekBarPreference) {
            ((SeekBarPreference) preference).setProgress(getSliderPosition());
        } else if (preference instanceof android.support.v7.preference.SeekBarPreference) {
            ((android.support.v7.preference.SeekBarPreference) preference).setValue(getSliderPosition());
        }
    }

    public int getSliceType() {
        return 2;
    }
}
