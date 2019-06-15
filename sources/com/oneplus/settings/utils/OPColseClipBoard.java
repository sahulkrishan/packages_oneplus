package com.oneplus.settings.utils;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.System;

public class OPColseClipBoard extends Activity {
    private static final String CLIPBOARD_PACKAGENAME = "com.oneplus.clipboard";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CLIPBOARD_PACKAGENAME.equals(getCallingPackage())) {
            System.putInt(getContentResolver(), "oem_quick_clipboard", 0);
        }
        finish();
    }
}
