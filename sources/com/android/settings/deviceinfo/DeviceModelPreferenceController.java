package com.android.settings.deviceinfo;

import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;

public class DeviceModelPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_DEVICE_MODEL = "device_model";
    private final Fragment mHost;

    public DeviceModelPreferenceController(Context context, Fragment host) {
        super(context);
        this.mHost = host;
    }

    public boolean isAvailable() {
        return false;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(KEY_DEVICE_MODEL);
        if (pref != null) {
            pref.setSummary(this.mContext.getResources().getString(R.string.model_summary, new Object[]{getDeviceModel()}));
        }
    }

    public String getPreferenceKey() {
        return KEY_DEVICE_MODEL;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_DEVICE_MODEL)) {
            return false;
        }
        HardwareInfoDialogFragment.newInstance().show(this.mHost.getFragmentManager(), HardwareInfoDialogFragment.TAG);
        return true;
    }

    public static String getDeviceModel() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Build.MODEL);
        stringBuilder.append(DeviceInfoUtils.getMsvSuffix());
        return stringBuilder.toString();
    }
}
