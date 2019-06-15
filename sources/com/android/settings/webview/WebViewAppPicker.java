package com.android.settings.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.webkit.UserPackage;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultAppPickerFragment;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.List;

public class WebViewAppPicker extends DefaultAppPickerFragment {
    private WebViewUpdateServiceWrapper mWebViewUpdateServiceWrapper;

    private static class WebViewAppInfo extends DefaultAppInfo {
        public WebViewAppInfo(Context context, PackageManagerWrapper pm, PackageItemInfo packageItemInfo, String summary, boolean enabled) {
            super(context, pm, packageItemInfo, summary, enabled);
        }

        public CharSequence loadLabel() {
            String versionName = "";
            try {
                versionName = this.mPm.getPackageManager().getPackageInfo(this.packageItemInfo.packageName, 0).versionName;
            } catch (NameNotFoundException e) {
            }
            return String.format("%s %s", new Object[]{super.loadLabel(), versionName});
        }
    }

    private WebViewUpdateServiceWrapper getWebViewUpdateServiceWrapper() {
        if (this.mWebViewUpdateServiceWrapper == null) {
            setWebViewUpdateServiceWrapper(createDefaultWebViewUpdateServiceWrapper());
        }
        return this.mWebViewUpdateServiceWrapper;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (!this.mUserManager.isAdminUser()) {
            getActivity().finish();
        }
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.webview_app_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        List<DefaultAppInfo> packageInfoList = new ArrayList();
        Context context = getContext();
        WebViewUpdateServiceWrapper webViewUpdateService = getWebViewUpdateServiceWrapper();
        for (ApplicationInfo ai : webViewUpdateService.getValidWebViewApplicationInfos(context)) {
            packageInfoList.add(createDefaultAppInfo(context, this.mPm, ai, getDisabledReason(webViewUpdateService, context, ai.packageName)));
        }
        return packageInfoList;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        PackageInfo currentPackage = getWebViewUpdateServiceWrapper().getCurrentWebViewPackage();
        return currentPackage == null ? null : currentPackage.packageName;
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        return getWebViewUpdateServiceWrapper().setWebViewProvider(key);
    }

    /* Access modifiers changed, original: protected */
    public void onSelectionPerformed(boolean success) {
        if (success) {
            Activity activity = getActivity();
            Intent intent = activity == null ? null : activity.getIntent();
            if (intent != null && "android.settings.WEBVIEW_SETTINGS".equals(intent.getAction())) {
                getActivity().finish();
                return;
            }
            return;
        }
        getWebViewUpdateServiceWrapper().showInvalidChoiceToast(getActivity());
        updateCandidates();
    }

    private WebViewUpdateServiceWrapper createDefaultWebViewUpdateServiceWrapper() {
        return new WebViewUpdateServiceWrapper();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setWebViewUpdateServiceWrapper(WebViewUpdateServiceWrapper wvusWrapper) {
        this.mWebViewUpdateServiceWrapper = wvusWrapper;
    }

    public int getMetricsCategory() {
        return 405;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public DefaultAppInfo createDefaultAppInfo(Context context, PackageManagerWrapper pm, PackageItemInfo packageItemInfo, String disabledReason) {
        return new WebViewAppInfo(context, pm, packageItemInfo, disabledReason, TextUtils.isEmpty(disabledReason));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getDisabledReason(WebViewUpdateServiceWrapper webviewUpdateServiceWrapper, Context context, String packageName) {
        for (UserPackage userPackage : webviewUpdateServiceWrapper.getPackageInfosAllUsers(context, packageName)) {
            if (userPackage.getUserInfo().id != 999) {
                if (!userPackage.isInstalledPackage()) {
                    return context.getString(R.string.webview_uninstalled_for_user, new Object[]{userPackage.getUserInfo().name});
                } else if (!userPackage.isEnabledPackage()) {
                    return context.getString(R.string.webview_disabled_for_user, new Object[]{userPackage.getUserInfo().name});
                }
            }
        }
        return null;
    }
}
