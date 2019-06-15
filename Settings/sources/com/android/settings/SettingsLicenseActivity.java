package com.android.settings;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.users.RestrictedProfileSettings;
import com.android.settingslib.license.LicenseHtmlLoader;
import java.io.File;

public class SettingsLicenseActivity extends Activity implements LoaderCallbacks<File> {
    private static final String DEFAULT_LICENSE_PATH = "/system/etc/NOTICE.html.gz";
    private static final int LOADER_ID_LICENSE_HTML_LOADER = 0;
    private static final String PROPERTY_LICENSE_PATH = "ro.config.license_path";
    private static final String TAG = "SettingsLicenseActivity";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String licenseHtmlPath = SystemProperties.get(PROPERTY_LICENSE_PATH, DEFAULT_LICENSE_PATH);
        if (isFilePathValid(licenseHtmlPath)) {
            showSelectedFile(licenseHtmlPath);
        } else {
            showHtmlFromDefaultXmlFiles();
        }
    }

    public Loader<File> onCreateLoader(int id, Bundle args) {
        return new LicenseHtmlLoader(this);
    }

    public void onLoadFinished(Loader<File> loader, File generatedHtmlFile) {
        showGeneratedHtmlFile(generatedHtmlFile);
    }

    public void onLoaderReset(Loader<File> loader) {
    }

    private void showHtmlFromDefaultXmlFiles() {
        getLoaderManager().initLoader(0, Bundle.EMPTY, this);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Uri getUriFromGeneratedHtmlFile(File generatedHtmlFile) {
        return FileProvider.getUriForFile(this, RestrictedProfileSettings.FILE_PROVIDER_AUTHORITY, generatedHtmlFile);
    }

    private void showGeneratedHtmlFile(File generatedHtmlFile) {
        if (generatedHtmlFile != null) {
            showHtmlFromUri(getUriFromGeneratedHtmlFile(generatedHtmlFile));
            return;
        }
        Log.e(TAG, "Failed to generate.");
        showErrorAndFinish();
    }

    private void showSelectedFile(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "The system property for the license file is empty");
            showErrorAndFinish();
            return;
        }
        File file = new File(path);
        if (isFileValid(file)) {
            showHtmlFromUri(Uri.fromFile(file));
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("License file ");
        stringBuilder.append(path);
        stringBuilder.append(" does not exist");
        Log.e(str, stringBuilder.toString());
        showErrorAndFinish();
    }

    private void showHtmlFromUri(Uri uri) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        intent.putExtra("android.intent.extra.TITLE", getString(R.string.settings_license_activity_title));
        if ("content".equals(uri.getScheme())) {
            intent.addFlags(1);
        }
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
        Toast.makeText(this, R.string.settings_license_activity_unavailable, 1).show();
        finish();
    }

    private boolean isFilePathValid(String path) {
        return !TextUtils.isEmpty(path) && isFileValid(new File(path));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isFileValid(File file) {
        return file.exists() && file.length() != 0;
    }
}
