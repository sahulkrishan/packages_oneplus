package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.development.SystemPropPoker;

public class HardwareLayersUpdatesPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String SHOW_HW_LAYERS_UPDATES_KEY = "show_hw_layers_updates";

    public HardwareLayersUpdatesPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return SHOW_HW_LAYERS_UPDATES_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SystemProperties.set("debug.hwui.show_layers_updates", ((Boolean) newValue).booleanValue() ? "true" : null);
        SystemPropPoker.getInstance().poke();
        return true;
    }

    public void updateState(Preference preference) {
        ((SwitchPreference) this.mPreference).setChecked(SystemProperties.getBoolean("debug.hwui.show_layers_updates", false));
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        SystemProperties.set("debug.hwui.show_layers_updates", null);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
