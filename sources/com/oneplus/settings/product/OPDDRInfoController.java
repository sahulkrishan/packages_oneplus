package com.oneplus.settings.product;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class OPDDRInfoController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY = "oneplus_ddr_memory_capacity";
    private Context mContext;

    public OPDDRInfoController(Context context) {
        super(context);
        this.mContext = context;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public void updateState(Preference preference) {
        try {
            preference.setSummary(OPUtils.showROMStorage(this.mContext));
        } catch (RuntimeException e) {
            preference.setSummary(this.mContext.getResources().getString(R.string.device_info_default));
        }
    }

    public boolean isAvailable() {
        return false;
    }
}
