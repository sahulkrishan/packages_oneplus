package com.android.settings.deviceinfo;

import android.content.Context;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.deviceinfo.AbstractImsStatusPreferenceController;

public class ImsStatusPreferenceController extends AbstractImsStatusPreferenceController implements PreferenceControllerMixin {
    public ImsStatusPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
    }
}
