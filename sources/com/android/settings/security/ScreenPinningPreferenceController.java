package com.android.settings.security;

import android.content.Context;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class ScreenPinningPreferenceController extends BasePreferenceController {
    private static final String KEY_SCREEN_PINNING = "screen_pinning_settings";

    public ScreenPinningPreferenceController(Context context) {
        super(context, KEY_SCREEN_PINNING);
    }

    public int getAvailabilityStatus() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_screen_pinning_settings) ? 0 : 2;
    }

    public void updateState(Preference preference) {
        if (preference == null) {
            return;
        }
        if (System.getInt(this.mContext.getContentResolver(), "op_navigation_bar_type", 1) == 3) {
            preference.setEnabled(false);
            preference.setSummary(this.mContext.getResources().getString(R.string.oneplus_fullscreen_disable_this_feature));
            return;
        }
        preference.setSummary(getSummary());
        preference.setEnabled(true);
    }

    public CharSequence getSummary() {
        if (System.getInt(this.mContext.getContentResolver(), "lock_to_app_enabled", 0) != 0) {
            return this.mContext.getText(R.string.switch_on_text);
        }
        return this.mContext.getText(R.string.switch_off_text);
    }
}
