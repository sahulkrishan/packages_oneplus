package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.core.SliderPreferenceController;
import com.android.settings.widget.SeekBarPreference;

public class NightDisplayIntensityPreferenceController extends SliderPreferenceController {
    private ColorDisplayController mController;

    public NightDisplayIntensityPreferenceController(Context context, String key) {
        super(context, key);
        this.mController = new ColorDisplayController(context);
    }

    public int getAvailabilityStatus() {
        if (!ColorDisplayController.isAvailable(this.mContext)) {
            return 2;
        }
        if (this.mController.isActivated()) {
            return 0;
        }
        return 4;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "night_display_temperature");
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        SeekBarPreference preference = (SeekBarPreference) screen.findPreference(getPreferenceKey());
        preference.setContinuousUpdates(true);
        preference.setMax(getMaxSteps());
    }

    public final void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(this.mController.isActivated());
    }

    public int getSliderPosition() {
        return convertTemperature(this.mController.getColorTemperature());
    }

    public boolean setSliderPosition(int position) {
        return this.mController.setColorTemperature(convertTemperature(position));
    }

    public int getMaxSteps() {
        return convertTemperature(this.mController.getMinimumColorTemperature());
    }

    private int convertTemperature(int temperature) {
        return this.mController.getMaximumColorTemperature() - temperature;
    }
}
