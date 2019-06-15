package com.oneplus.settings.defaultapp.controller;

import android.content.Context;
import com.oneplus.settings.defaultapp.DefaultAppUtils;

public class DefaultCameraController extends DefaultBasePreferenceController {
    public DefaultCameraController(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public String getType() {
        return DefaultAppUtils.getKeyTypeString(0);
    }
}
