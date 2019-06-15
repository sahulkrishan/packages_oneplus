package com.android.settings.display;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class CameraGesturePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener {
    private static final String KEY_CAMERA_GESTURE = "camera_gesture";

    public CameraGesturePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return KEY_CAMERA_GESTURE;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) preference;
        if (Secure.getInt(this.mContext.getContentResolver(), "camera_gesture_disabled", 0) == 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    public boolean isAvailable() {
        if (!(this.mContext.getResources().getInteger(17694753) != -1) || SystemProperties.getBoolean("gesture.disable_camera_launch", false)) {
            return false;
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Secure.putInt(this.mContext.getContentResolver(), "camera_gesture_disabled", ((Boolean) newValue).booleanValue() ^ 1);
        return true;
    }
}
