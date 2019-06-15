package com.oneplus.settings.aboutphone;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPKernelVersionPreferenceController extends BasePreferenceController implements LifecycleObserver {
    private static final String KEY_KERNEL_VERSION = "kernel_version";
    private Context mContext;
    private Preference mPreference;

    public OPKernelVersionPreferenceController(Context context) {
        super(context, KEY_KERNEL_VERSION);
        this.mContext = context;
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
        this.mPreference.setSummary(DeviceInfoUtils.getFormattedKernelVersion(this.mContext));
    }

    public String getPreferenceKey() {
        return KEY_KERNEL_VERSION;
    }
}
