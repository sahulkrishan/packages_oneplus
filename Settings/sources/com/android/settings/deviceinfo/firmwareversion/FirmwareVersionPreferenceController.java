package com.android.settings.deviceinfo.firmwareversion;

import android.app.Fragment;
import android.content.Context;
import android.os.Build.VERSION;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class FirmwareVersionPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String FIRMWARE_VERSION_KEY = "firmware_version";
    private final Fragment mFragment;

    public FirmwareVersionPreferenceController(Context context, Fragment fragment) {
        super(context);
        this.mFragment = fragment;
    }

    public boolean isAvailable() {
        return false;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(getPreferenceKey());
        if (pref != null) {
            pref.setSummary(VERSION.RELEASE);
        }
    }

    public String getPreferenceKey() {
        return FIRMWARE_VERSION_KEY;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        FirmwareVersionDialogFragment.show(this.mFragment);
        return true;
    }
}
