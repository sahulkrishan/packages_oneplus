package com.android.settings.display;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.dream.DreamSettings;
import com.android.settingslib.core.AbstractPreferenceController;

public class ScreenSaverPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_SCREEN_SAVER = "screensaver";

    public ScreenSaverPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(17956946);
    }

    public String getPreferenceKey() {
        return KEY_SCREEN_SAVER;
    }

    public void updateState(Preference preference) {
        preference.setSummary(DreamSettings.getSummaryTextWithDreamName(this.mContext));
    }
}
