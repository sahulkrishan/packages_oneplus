package com.android.settings;

import android.util.Log;

public class SubSettings extends SettingsActivity {
    public boolean onNavigateUp() {
        finish();
        return true;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Launching fragment ");
        stringBuilder.append(fragmentName);
        Log.d("SubSettings", stringBuilder.toString());
        return true;
    }
}
