package com.android.settings.dashboard;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

class DashboardTilePlaceholderPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_PLACEHOLDER = "dashboard_tile_placeholder";
    private int mOrder = Integer.MAX_VALUE;

    public DashboardTilePlaceholderPreferenceController(Context context) {
        super(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref != null) {
            this.mOrder = pref.getOrder();
            screen.removePreference(pref);
        }
    }

    public boolean isAvailable() {
        return false;
    }

    public String getPreferenceKey() {
        return KEY_PLACEHOLDER;
    }

    public int getOrder() {
        return this.mOrder;
    }
}
