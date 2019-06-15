package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.SystemPropPoker;

public class CoolColorTemperaturePreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String COLOR_TEMPERATURE_KEY = "color_temperature";
    @VisibleForTesting
    static final String COLOR_TEMPERATURE_PROPERTY = "persist.sys.debug.color_temp";

    public CoolColorTemperaturePreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_enableColorTemperature);
    }

    public String getPreferenceKey() {
        return COLOR_TEMPERATURE_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(COLOR_TEMPERATURE_PROPERTY, Boolean.toString(((Boolean) newValue).booleanValue()));
        SystemPropPoker.getInstance().poke();
        displayColorTemperatureToast();
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean(COLOR_TEMPERATURE_PROPERTY, false));
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set(COLOR_TEMPERATURE_PROPERTY, Boolean.toString(false));
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void displayColorTemperatureToast() {
        Toast.makeText(this.mContext, R.string.color_temperature_toast, 1).show();
    }
}
