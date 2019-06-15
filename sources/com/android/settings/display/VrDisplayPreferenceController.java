package com.android.settings.display;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class VrDisplayPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_VR_DISPLAY_PREF = "vr_display_pref";

    public VrDisplayPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.vr.high_performance");
    }

    public String getPreferenceKey() {
        return KEY_VR_DISPLAY_PREF;
    }

    public void updateState(Preference preference) {
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser()) == 0) {
            preference.setSummary((int) R.string.display_vr_pref_low_persistence);
        } else {
            preference.setSummary((int) R.string.display_vr_pref_off);
        }
    }
}
