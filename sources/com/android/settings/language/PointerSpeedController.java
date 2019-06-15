package com.android.settings.language;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class PointerSpeedController extends BasePreferenceController {
    @VisibleForTesting
    static final String KEY_POINTER_SPEED = "pointer_speed";

    public PointerSpeedController(Context context) {
        super(context, KEY_POINTER_SPEED);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_pointer_speed)) {
            return 0;
        }
        return 2;
    }
}
