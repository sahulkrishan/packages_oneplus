package com.android.settings.applications;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class DataSaverController extends BasePreferenceController {
    @VisibleForTesting
    static final String KEY_DATA_SAVER = "data_saver";

    public DataSaverController(Context context) {
        super(context, KEY_DATA_SAVER);
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_data_saver)) {
            return 0;
        }
        return 2;
    }
}
