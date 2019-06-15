package com.oneplus.settings.defaultapp.controller;

import android.content.Context;
import com.oneplus.settings.defaultapp.DefaultAppUtils;

public class DefaultMailController extends DefaultBasePreferenceController {
    public DefaultMailController(Context context) {
        super(context);
    }

    /* Access modifiers changed, original: protected */
    public String getType() {
        return DefaultAppUtils.getKeyTypeString(3);
    }
}
