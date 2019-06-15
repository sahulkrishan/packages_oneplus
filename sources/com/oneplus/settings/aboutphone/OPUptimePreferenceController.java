package com.oneplus.settings.aboutphone;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.deviceinfo.AbstractUptimePreferenceController;

public class OPUptimePreferenceController extends AbstractUptimePreferenceController implements PreferenceControllerMixin {
    public OPUptimePreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
    }

    public boolean isAvailable() {
        return true;
    }
}
