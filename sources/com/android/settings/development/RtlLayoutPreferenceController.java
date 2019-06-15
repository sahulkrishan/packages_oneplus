package com.android.settings.development;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.internal.app.LocalePicker;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class RtlLayoutPreferenceController extends DeveloperOptionsPreferenceController implements OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String FORCE_RTL_LAYOUT_KEY = "force_rtl_layout_all_locales";
    @VisibleForTesting
    static final int SETTING_VALUE_OFF = 0;
    @VisibleForTesting
    static final int SETTING_VALUE_ON = 1;

    public RtlLayoutPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return FORCE_RTL_LAYOUT_KEY;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeToForceRtlLayoutSetting(((Boolean) newValue).booleanValue());
        updateLocales();
        return true;
    }

    public void updateState(Preference preference) {
        boolean z = false;
        SwitchPreference switchPreference = (SwitchPreference) this.mPreference;
        if (Global.getInt(this.mContext.getContentResolver(), "debug.force_rtl", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeToForceRtlLayoutSetting(false);
        updateLocales();
        ((SwitchPreference) this.mPreference).setChecked(false);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateLocales() {
        LocalePicker.updateLocales(this.mContext.getResources().getConfiguration().getLocales());
    }

    private void writeToForceRtlLayoutSetting(boolean isEnabled) {
        String num;
        Global.putInt(this.mContext.getContentResolver(), "debug.force_rtl", isEnabled);
        String str = "debug.force_rtl";
        if (isEnabled) {
            num = Integer.toString(1);
        } else {
            num = Integer.toString(0);
        }
        SystemProperties.set(str, num);
    }
}
