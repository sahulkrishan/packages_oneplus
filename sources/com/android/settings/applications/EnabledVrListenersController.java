package com.android.settings.applications;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class EnabledVrListenersController extends BasePreferenceController {
    @VisibleForTesting
    static final String KEY_ENABLED_VR_LISTENERS = "enabled_vr_listeners";

    public EnabledVrListenersController(Context context) {
        super(context, KEY_ENABLED_VR_LISTENERS);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_enabled_vr_listeners)) {
            return 0;
        }
        return 2;
    }
}
