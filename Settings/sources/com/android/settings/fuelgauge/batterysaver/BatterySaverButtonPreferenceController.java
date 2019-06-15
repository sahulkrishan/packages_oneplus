package com.android.settings.fuelgauge.batterysaver;

import android.content.Context;
import android.os.PowerManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.fuelgauge.BatterySaverReceiver;
import com.android.settings.fuelgauge.BatterySaverReceiver.BatterySaverListener;
import com.android.settings.widget.TwoStateButtonPreference;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;
import com.android.settingslib.fuelgauge.BatterySaverUtils;

public class BatterySaverButtonPreferenceController extends TogglePreferenceController implements LifecycleObserver, OnStart, OnStop, BatterySaverListener {
    private final BatterySaverReceiver mBatterySaverReceiver;
    private final PowerManager mPowerManager;
    private TwoStateButtonPreference mPreference;

    public BatterySaverButtonPreferenceController(Context context, String key) {
        super(context, key);
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mBatterySaverReceiver = new BatterySaverReceiver(context);
        this.mBatterySaverReceiver.setBatterySaverListener(this);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public boolean isSliceable() {
        return true;
    }

    public void onStart() {
        this.mBatterySaverReceiver.setListening(true);
    }

    public void onStop() {
        this.mBatterySaverReceiver.setListening(false);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (TwoStateButtonPreference) screen.findPreference(getPreferenceKey());
    }

    public boolean isChecked() {
        return this.mPowerManager.isPowerSaveMode();
    }

    public boolean setChecked(boolean stateOn) {
        return BatterySaverUtils.setPowerSaveMode(this.mContext, stateOn, false);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (this.mPreference != null) {
            this.mPreference.setChecked(isChecked());
        }
    }

    public void onPowerSaveModeChanged() {
        boolean isChecked = isChecked();
        if (this.mPreference != null && this.mPreference.isChecked() != isChecked) {
            this.mPreference.setChecked(isChecked);
        }
    }

    public void onBatteryChanged(boolean pluggedIn) {
        if (this.mPreference != null) {
            this.mPreference.setButtonEnabled(pluggedIn ^ 1);
        }
    }
}
