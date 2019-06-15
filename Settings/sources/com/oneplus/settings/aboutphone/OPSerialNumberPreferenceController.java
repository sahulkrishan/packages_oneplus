package com.oneplus.settings.aboutphone;

import android.content.Context;
import android.os.Build;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPSerialNumberPreferenceController extends BasePreferenceController implements LifecycleObserver {
    private static final String KEY_SERIAL_NUMBER = "serial_number";
    private Preference mPreference;

    public OPSerialNumberPreferenceController(Context context) {
        super(context, KEY_SERIAL_NUMBER);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
        this.mPreference.setSummary(Build.getSerial());
    }

    public String getPreferenceKey() {
        return KEY_SERIAL_NUMBER;
    }
}
