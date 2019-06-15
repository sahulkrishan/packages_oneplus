package com.android.settings.deviceinfo;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class FccEquipmentIdPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_EQUIPMENT_ID = "fcc_equipment_id";
    private static final String PROPERTY_EQUIPMENT_ID = "ro.ril.fccid";

    public FccEquipmentIdPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return TextUtils.isEmpty(SystemProperties.get(PROPERTY_EQUIPMENT_ID)) ^ 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(KEY_EQUIPMENT_ID);
        if (pref != null) {
            pref.setSummary(SystemProperties.get(PROPERTY_EQUIPMENT_ID, this.mContext.getResources().getString(R.string.device_info_default)));
        }
    }

    public String getPreferenceKey() {
        return KEY_EQUIPMENT_ID;
    }
}
