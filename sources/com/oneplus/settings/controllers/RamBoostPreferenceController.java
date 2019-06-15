package com.oneplus.settings.controllers;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.oneplus.settings.utils.OPUtils;

public class RamBoostPreferenceController extends BasePreferenceController implements LifecycleObserver {
    public static final String KEY_RAMBOOST_SETTINGS = "op_ramboost_settings";
    private Context mContext;
    private Preference mPref;

    public RamBoostPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_RAMBOOST_SETTINGS);
        this.mContext = context;
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public String getPreferenceKey() {
        return KEY_RAMBOOST_SETTINGS;
    }

    public int getAvailabilityStatus() {
        if (OPUtils.isSupportSmartBoost()) {
            return 0;
        }
        return 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPref = screen.findPreference(getPreferenceKey());
    }
}
