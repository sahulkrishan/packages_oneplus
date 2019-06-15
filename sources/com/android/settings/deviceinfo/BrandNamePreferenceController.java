package com.android.settings.deviceinfo;

import android.content.Context;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import com.android.settings.core.BasePreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class BrandNamePreferenceController extends BasePreferenceController {
    public static final String KEY_BRAND_NAME = "brand_name";
    private Context mContext;

    public BrandNamePreferenceController(Context context) {
        super(context, KEY_BRAND_NAME);
        this.mContext = context;
    }

    public int getAvailabilityStatus() {
        return OPUtils.isSupportUss() ? 0 : 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        screen.findPreference(KEY_BRAND_NAME).setSummary(((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperatorName());
    }
}
