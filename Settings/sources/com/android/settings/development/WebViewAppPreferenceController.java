package com.android.settings.development;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.webview.WebViewUpdateServiceWrapper;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class WebViewAppPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private static final String TAG = "WebViewAppPrefCtrl";
    private static final String WEBVIEW_APP_KEY = "select_webview_provider";
    private final PackageManagerWrapper mPackageManager;
    private final WebViewUpdateServiceWrapper mWebViewUpdateServiceWrapper = new WebViewUpdateServiceWrapper();

    public WebViewAppPreferenceController(Context context) {
        super(context);
        this.mPackageManager = new PackageManagerWrapper(context.getPackageManager());
    }

    public String getPreferenceKey() {
        return WEBVIEW_APP_KEY;
    }

    public void updateState(Preference preference) {
        CharSequence defaultAppLabel = getDefaultAppLabel();
        if (TextUtils.isEmpty(defaultAppLabel)) {
            Log.d(TAG, "No default app");
            this.mPreference.setSummary((int) R.string.app_list_preference_none);
            return;
        }
        this.mPreference.setSummary(defaultAppLabel);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public DefaultAppInfo getDefaultAppInfo() {
        PackageInfo currentPackage = this.mWebViewUpdateServiceWrapper.getCurrentWebViewPackage();
        return new DefaultAppInfo(this.mContext, this.mPackageManager, currentPackage == null ? null : currentPackage.applicationInfo);
    }

    private CharSequence getDefaultAppLabel() {
        return getDefaultAppInfo().loadLabel();
    }
}
