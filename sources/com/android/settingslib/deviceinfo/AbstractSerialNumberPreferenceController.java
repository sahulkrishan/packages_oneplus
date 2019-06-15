package com.android.settingslib.deviceinfo;

import android.content.Context;
import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settingslib.core.AbstractPreferenceController;

public class AbstractSerialNumberPreferenceController extends AbstractPreferenceController {
    @VisibleForTesting
    static final String KEY_SERIAL_NUMBER = "serial_number";
    private final String mSerialNumber;

    public AbstractSerialNumberPreferenceController(Context context) {
        this(context, Build.getSerial());
    }

    @VisibleForTesting
    AbstractSerialNumberPreferenceController(Context context, String serialNumber) {
        super(context);
        this.mSerialNumber = serialNumber;
    }

    public boolean isAvailable() {
        return TextUtils.isEmpty(this.mSerialNumber) ^ 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(KEY_SERIAL_NUMBER);
        if (pref != null) {
            pref.setSummary(this.mSerialNumber);
        }
    }

    public String getPreferenceKey() {
        return KEY_SERIAL_NUMBER;
    }
}
