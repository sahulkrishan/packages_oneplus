package com.oneplus.settings.product;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class OPMemoryInfoController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY = "oneplus_memory_capacity";
    private Context mContext;

    public OPMemoryInfoController(Context context) {
        super(context);
        this.mContext = context;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public void updateState(Preference preference) {
        try {
            preference.setSummary(OPUtils.getTotalMemory());
        } catch (RuntimeException e) {
            preference.setSummary(this.mContext.getResources().getString(R.string.device_info_default));
        }
    }

    public boolean isAvailable() {
        return false;
    }
}
