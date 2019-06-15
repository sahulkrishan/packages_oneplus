package com.android.settings.deviceinfo;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;

public class KernelVersionPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_KERNEL_VERSION = "kernel_version";

    public KernelVersionPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(DeviceInfoUtils.getFormattedKernelVersion(this.mContext));
    }

    public String getPreferenceKey() {
        return KEY_KERNEL_VERSION;
    }
}
