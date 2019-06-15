package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class ResizableActivityPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String FORCE_RESIZABLE_KEY = "force_resizable_activities";
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;

    public ResizableActivityPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return FORCE_RESIZABLE_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), FORCE_RESIZABLE_KEY, ((Boolean) newValue).booleanValue());
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Global.getInt(this.mContext.getContentResolver(), FORCE_RESIZABLE_KEY, 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Global.putInt(this.mContext.getContentResolver(), FORCE_RESIZABLE_KEY, 0);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
