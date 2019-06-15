package com.android.settings.development;

import android.content.Context;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class DisableAutomaticUpdatesPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    @VisibleForTesting
    static final int DISABLE_UPDATES_SETTING = 1;
    @VisibleForTesting
    static final int ENABLE_UPDATES_SETTING = 0;
    private static final String OTA_DISABLE_AUTOMATIC_UPDATE_KEY = "ota_disable_automatic_update";

    public DisableAutomaticUpdatesPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return OTA_DISABLE_AUTOMATIC_UPDATE_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Global.putInt(this.mContext.getContentResolver(), OTA_DISABLE_AUTOMATIC_UPDATE_KEY, ((Boolean) newValue).booleanValue() ^ 1);
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Global.getInt(this.mContext.getContentResolver(), OTA_DISABLE_AUTOMATIC_UPDATE_KEY, 0) != 1) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Global.putInt(this.mContext.getContentResolver(), OTA_DISABLE_AUTOMATIC_UPDATE_KEY, 1);
        ((SwitchPreference) this.mPreference).setChecked(false);
    }
}
