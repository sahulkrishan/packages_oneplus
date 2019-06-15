package com.android.settings.system;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class ResetPreferenceController extends BasePreferenceController {
    public ResetPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_reset_dashboard)) {
            return 0;
        }
        return 2;
    }
}
