package com.oneplus.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import com.android.settings.core.BasePreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class OPVideoEnhancerSwitchPreferenceController extends BasePreferenceController implements OnPreferenceChangeListener {
    private static final String KEY_VIDEO_ENHANCER_SWITCH = "video_enhancer_switch";

    public OPVideoEnhancerSwitchPreferenceController(Context context) {
        super(context, KEY_VIDEO_ENHANCER_SWITCH);
    }

    public int getAvailabilityStatus() {
        return OPUtils.isSupportVideoEnhancer() ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_VIDEO_ENHANCER_SWITCH);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        ((SwitchPreference) preference).setChecked(SystemProperties.getBoolean("persist.sys.oem.vendor.media.vpp.enable", false));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isEnabled = ((Boolean) newValue).booleanValue();
        SystemProperties.set("persist.sys.oem.vendor.media.vpp.enable", isEnabled ? "true" : "false");
        OPUtils.sendAppTracker("video_enhancer", isEnabled ? "1" : "0");
        return true;
    }
}
