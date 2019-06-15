package com.android.settings.development;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPGetLogsPreferenceController extends AbstractPreferenceController implements LifecycleObserver {
    private static final String KEY_GET_LOGS = "getlogs";
    private Preference mPreference;

    public OPGetLogsPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public String getPreferenceKey() {
        return KEY_GET_LOGS;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_GET_LOGS.equals(preference.getKey())) {
            return false;
        }
        try {
            Intent intent = new Intent("com.oem.oemlogkit.startlog");
            intent.setFlags(805306368);
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
        return true;
    }
}
