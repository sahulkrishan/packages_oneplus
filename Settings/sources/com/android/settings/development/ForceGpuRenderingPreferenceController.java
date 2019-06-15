package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.SystemPropPoker;

public class ForceGpuRenderingPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String FORCE_HARDWARE_UI_KEY = "force_hw_ui";
    @VisibleForTesting
    static final String HARDWARE_UI_PROPERTY = "persist.sys.ui.hw";

    public ForceGpuRenderingPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return FORCE_HARDWARE_UI_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set(HARDWARE_UI_PROPERTY, ((Boolean) newValue).booleanValue() ? Boolean.toString(true) : Boolean.toString(false));
        SystemPropPoker.getInstance().poke();
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean(HARDWARE_UI_PROPERTY, false));
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set(HARDWARE_UI_PROPERTY, Boolean.toString(false));
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
