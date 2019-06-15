package com.android.settings.inputmethod;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.SettingsActivity;

public class InputMethodAndSubtypeEnablerActivity extends SettingsActivity {
    private static final String FRAGMENT_NAME = InputMethodAndSubtypeEnabler.class.getName();

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    public boolean onNavigateUp() {
        finish();
        return true;
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        if (!modIntent.hasExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT)) {
            modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, FRAGMENT_NAME);
        }
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return FRAGMENT_NAME.equals(fragmentName);
    }
}
