package com.android.settingslib.inputmethod;

import android.content.Context;
import android.support.v14.preference.SwitchPreference;

public class SwitchWithNoTextPreference extends SwitchPreference {
    private static final String EMPTY_TEXT = "";

    public SwitchWithNoTextPreference(Context context) {
        super(context);
        setSwitchTextOn((CharSequence) "");
        setSwitchTextOff((CharSequence) "");
    }
}
