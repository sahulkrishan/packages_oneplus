package com.android.settings.applications;

import android.content.Intent;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.appinfo.AppInfoDashboardFragment;

public class InstalledAppDetailsTop extends SettingsActivity {
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, AppInfoDashboardFragment.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return AppInfoDashboardFragment.class.getName().equals(fragmentName);
    }
}
