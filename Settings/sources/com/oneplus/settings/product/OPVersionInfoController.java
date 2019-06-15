package com.oneplus.settings.product;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class OPVersionInfoController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY = "oneplus_oos_version";
    private Context mContext;

    public OPVersionInfoController(Context context) {
        super(context);
        this.mContext = context;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public void updateState(Preference preference) {
        if (OpFeatures.isSupport(new int[]{1})) {
            preference.setTitle(this.mContext.getResources().getString(R.string.oneplus_oxygen_version));
            preference.setSummary(SystemProperties.get("ro.oxygen.version", this.mContext.getResources().getString(R.string.device_info_default)).replace("O2", "O₂"));
            return;
        }
        preference.setTitle(this.mContext.getResources().getString(R.string.oneplus_hydrogen_version).replace("H2", "H₂"));
        preference.setSummary(SystemProperties.get("ro.rom.version", this.mContext.getResources().getString(R.string.device_info_default)).replace("H2", "H₂"));
    }

    public boolean isAvailable() {
        return false;
    }
}
