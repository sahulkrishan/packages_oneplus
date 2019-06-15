package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class CameraLaserSensorPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final String BUILD_TYPE = "ro.build.type";
    @VisibleForTesting
    static final int DISABLED = 2;
    @VisibleForTesting
    static final int ENABLED = 0;
    @VisibleForTesting
    static final String ENG_BUILD = "eng";
    private static final String KEY_CAMERA_LASER_SENSOR_SWITCH = "camera_laser_sensor_switch";
    @VisibleForTesting
    static final String PROPERTY_CAMERA_LASER_SENSOR = "persist.camera.stats.disablehaf";
    @VisibleForTesting
    static final String USERDEBUG_BUILD = "userdebug";
    @VisibleForTesting
    static final String USER_BUILD = "user";

    public CameraLaserSensorPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_camera_laser_sensor);
    }

    public String getPreferenceKey() {
        return KEY_CAMERA_LASER_SENSOR_SWITCH;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(PROPERTY_CAMERA_LASER_SENSOR, Integer.toString(((Boolean) newValue).booleanValue() ? null : 2));
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(isLaserSensorEnabled());
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set(PROPERTY_CAMERA_LASER_SENSOR, Integer.toString(2));
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    private boolean isLaserSensorEnabled() {
        return TextUtils.equals(Integer.toString(0), SystemProperties.get(PROPERTY_CAMERA_LASER_SENSOR, Integer.toString(0)));
    }
}
