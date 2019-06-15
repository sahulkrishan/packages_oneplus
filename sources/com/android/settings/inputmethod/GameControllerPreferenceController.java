package com.android.settings.inputmethod;

import android.content.Context;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.InputDevice;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class GameControllerPreferenceController extends TogglePreferenceController implements PreferenceControllerMixin, InputDeviceListener, LifecycleObserver, OnResume, OnPause {
    private final InputManager mIm;
    private Preference mPreference;

    public GameControllerPreferenceController(Context context, String key) {
        super(context, key);
        this.mIm = (InputManager) context.getSystemService("input");
    }

    public void onResume() {
        this.mIm.registerInputDeviceListener(this, null);
    }

    public void onPause() {
        this.mIm.unregisterInputDeviceListener(this);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public int getAvailabilityStatus() {
        if (!this.mContext.getResources().getBoolean(R.bool.config_show_vibrate_input_devices)) {
            return 2;
        }
        for (int deviceId : this.mIm.getInputDeviceIds()) {
            InputDevice device = this.mIm.getInputDevice(deviceId);
            if (device != null && !device.isVirtual() && device.getVibrator().hasVibrator()) {
                return 0;
            }
        }
        return 1;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference != null) {
            this.mPreference.setVisible(isAvailable());
        }
    }

    public boolean isChecked() {
        return System.getInt(this.mContext.getContentResolver(), "vibrate_input_devices", 1) > 0;
    }

    public boolean setChecked(boolean isChecked) {
        return System.putInt(this.mContext.getContentResolver(), "vibrate_input_devices", isChecked);
    }

    public void onInputDeviceAdded(int deviceId) {
        updateState(this.mPreference);
    }

    public void onInputDeviceRemoved(int deviceId) {
        updateState(this.mPreference);
    }

    public void onInputDeviceChanged(int deviceId) {
        updateState(this.mPreference);
    }
}
