package com.oneplus.settings.aboutphone;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class OPBaseBandPreferenceController extends BasePreferenceController implements LifecycleObserver {
    static final String BASEBAND_PROPERTY = "gsm.version.baseband";
    private static final String KEY_BASEBAND_VERSION = "baseband_version";
    private Context mContext;
    private Preference mPreference;

    public OPBaseBandPreferenceController(Context context) {
        super(context, KEY_BASEBAND_VERSION);
        this.mContext = context;
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
        this.mPreference.setSummary(SystemProperties.get(BASEBAND_PROPERTY, this.mContext.getString(R.string.device_info_default)));
    }

    public String getPreferenceKey() {
        return KEY_BASEBAND_VERSION;
    }
}
