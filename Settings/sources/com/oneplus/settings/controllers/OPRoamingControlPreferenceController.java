package com.oneplus.settings.controllers;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPRoamingControlPreferenceController extends BasePreferenceController implements LifecycleObserver {
    private static final String KEY_BUTTON_ONEPLUS_ROAMING = "key_button_oneplus_roaming";
    private static final String ONEPLUS_ROAMING_PACKAGE = "com.redteamobile.oneplus.roaming";
    private static final String ONEPLUS_ROAMING_PACKAGE_LAUNCHER = "com.redteamobile.oneplus.roaming.activity.MainActivity";
    private Preference mPreference;

    public OPRoamingControlPreferenceController(Context context) {
        super(context, KEY_BUTTON_ONEPLUS_ROAMING);
    }

    private boolean hasCatRoaming() {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            pm.getPackageInfo(ONEPLUS_ROAMING_PACKAGE, 1);
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(new ComponentName(ONEPLUS_ROAMING_PACKAGE, ONEPLUS_ROAMING_PACKAGE_LAUNCHER));
            return pm.queryIntentActivities(intent, 0).size() > 0;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getAvailabilityStatus() {
        return hasCatRoaming() ^ 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_BUTTON_ONEPLUS_ROAMING;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_BUTTON_ONEPLUS_ROAMING.equals(preference.getKey())) {
            return false;
        }
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setComponent(new ComponentName(ONEPLUS_ROAMING_PACKAGE, ONEPLUS_ROAMING_PACKAGE_LAUNCHER));
            intent.setFlags(268435456);
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
}
