package com.android.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

public class ManualDisplayActivity extends Activity {
    private static final String DEFAULT_MANUAL_PATH = "/system/etc/MANUAL.html.gz";
    private static final String PROPERTY_MANUAL_PATH = "ro.config.manual_path";
    private static final String TAG = "SettingsManualActivity";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getResources().getBoolean(R.bool.config_show_manual)) {
            finish();
        }
        String path = SystemProperties.get(PROPERTY_MANUAL_PATH, DEFAULT_MANUAL_PATH);
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "The system property for the manual is empty");
            showErrorAndFinish();
            return;
        }
        File file = new File(path);
        if (!file.exists() || file.length() == 0) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Manual file ");
            stringBuilder.append(path);
            stringBuilder.append(" does not exist");
            Log.e(str, stringBuilder.toString());
            showErrorAndFinish();
            return;
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(file), "text/html");
        intent.putExtra("android.intent.extra.TITLE", getString(R.string.settings_manual_activity_title));
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setPackage("com.android.htmlviewer");
        try {
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to find viewer", e);
            showErrorAndFinish();
        }
    }

    private void showErrorAndFinish() {
        Toast.makeText(this, R.string.settings_manual_activity_unavailable, 1).show();
        finish();
    }
}
