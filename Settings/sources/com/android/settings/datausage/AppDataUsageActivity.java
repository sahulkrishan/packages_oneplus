package com.android.settings.datausage;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settingslib.AppItem;

public class AppDataUsageActivity extends SettingsActivity {
    private static final boolean DEBUG = false;
    private static final String TAG = "AppDataUsageActivity";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String packageName = intent.getData().getSchemeSpecificPart();
        try {
            int uid = getPackageManager().getPackageUid(packageName, 0);
            Bundle args = new Bundle();
            AppItem appItem = new AppItem(uid);
            appItem.addUid(uid);
            args.putParcelable(AppDataUsage.ARG_APP_ITEM, appItem);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, AppDataUsage.class.getName());
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, R.string.app_data_usage);
            super.onCreate(savedInstanceState);
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("invalid package: ");
            stringBuilder.append(packageName);
            Log.w(str, stringBuilder.toString());
            try {
                super.onCreate(savedInstanceState);
            } catch (Exception e2) {
            } catch (Throwable th) {
                finish();
            }
            finish();
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return super.isValidFragment(fragmentName) || AppDataUsage.class.getName().equals(fragmentName);
    }
}
