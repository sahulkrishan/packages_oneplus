package com.oneplus.settings.product;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class OPProductInfoPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY = "oneplus_product_info";
    private Context mContext;

    public OPProductInfoPreferenceController(Context context) {
        super(context);
        this.mContext = context;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        return false;
    }
}
