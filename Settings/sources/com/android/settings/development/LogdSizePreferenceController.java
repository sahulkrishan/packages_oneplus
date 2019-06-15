package com.android.settings.development;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.AbstractLogdSizePreferenceController;

public class LogdSizePreferenceController extends AbstractLogdSizePreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    public LogdSizePreferenceController(Context context) {
        super(context);
    }

    public void updateState(Preference preference) {
        updateLogdSizeValues();
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeLogdSizeOption(null);
    }
}
