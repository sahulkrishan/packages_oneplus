package com.android.settings.applications;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class DeviceAdministratorsController extends BasePreferenceController {
    @VisibleForTesting
    static final String KEY_DEVICE_ADMIN = "device_administrators";

    public DeviceAdministratorsController(Context context) {
        super(context, KEY_DEVICE_ADMIN);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_device_administrators)) {
            return 0;
        }
        return 2;
    }
}
