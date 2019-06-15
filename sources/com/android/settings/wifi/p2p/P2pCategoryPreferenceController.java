package com.android.settings.wifi.p2p;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public abstract class P2pCategoryPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    protected PreferenceGroup mCategory;

    public P2pCategoryPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mCategory = (PreferenceGroup) screen.findPreference(getPreferenceKey());
    }

    public void removeAllChildren() {
        if (this.mCategory != null) {
            this.mCategory.removeAll();
            this.mCategory.setVisible(false);
        }
    }

    public void addChild(Preference child) {
        if (this.mCategory != null) {
            this.mCategory.addPreference(child);
            this.mCategory.setVisible(true);
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.mCategory != null) {
            this.mCategory.setEnabled(enabled);
        }
    }
}
