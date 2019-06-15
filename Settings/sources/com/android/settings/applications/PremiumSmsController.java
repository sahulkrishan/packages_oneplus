package com.android.settings.applications;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class PremiumSmsController extends BasePreferenceController {
    @VisibleForTesting
    static final String KEY_PREMIUM_SMS = "premium_sms";

    public PremiumSmsController(Context context) {
        super(context, KEY_PREMIUM_SMS);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_premium_sms)) {
            return 0;
        }
        return 2;
    }
}
