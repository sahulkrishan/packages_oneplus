package com.oneplus.settings.system;

import android.content.Context;
import android.os.UserManager;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class OPUssSystemUpdateController extends BasePreferenceController {
    private Context mContext;
    private final UserManager mUm;

    public OPUssSystemUpdateController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        this.mContext = context;
        this.mUm = UserManager.get(context);
    }

    public int getAvailabilityStatus() {
        return isNeedAvailable() ? 0 : 2;
    }

    public boolean isNeedAvailable() {
        return !this.mContext.getResources().getBoolean(R.bool.config_use_gota) && OPUtils.isSupportUss() && this.mUm.isAdminUser();
    }
}
