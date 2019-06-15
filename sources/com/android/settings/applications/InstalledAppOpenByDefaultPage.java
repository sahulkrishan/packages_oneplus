package com.android.settings.applications;

import android.content.Intent;
import com.android.settings.SettingsActivity;

public class InstalledAppOpenByDefaultPage extends SettingsActivity {
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, AppLaunchSettings.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return AppLaunchSettings.class.getName().equals(fragmentName);
    }
}
