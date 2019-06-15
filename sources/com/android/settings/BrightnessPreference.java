package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

public class BrightnessPreference extends Preference {
    public BrightnessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        getContext().startActivityAsUser(new Intent("com.android.intent.action.SHOW_BRIGHTNESS_DIALOG"), UserHandle.CURRENT_OR_SELF);
    }
}
