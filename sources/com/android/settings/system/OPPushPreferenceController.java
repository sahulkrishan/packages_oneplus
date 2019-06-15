package com.android.settings.system;

import android.content.Context;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class OPPushPreferenceController extends AbstractPreferenceController {
    private static final String KEY = "onepus_receive_notifications";
    private Context mContext;

    public OPPushPreferenceController(Context context) {
        super(context);
        this.mContext = context;
    }

    public boolean isAvailable() {
        if (OPUtils.isAppExist(this.mContext, OPConstants.PACKAGENAME_OP_PUSH)) {
            return true;
        }
        return false;
    }

    public String getPreferenceKey() {
        return KEY;
    }
}
