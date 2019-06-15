package com.android.settings.webview;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.UserPackage;
import android.webkit.WebViewFactory;
import android.webkit.WebViewProviderInfo;
import android.widget.Toast;
import com.android.settings.R;
import java.util.ArrayList;
import java.util.List;

public class WebViewUpdateServiceWrapper {
    static final int PACKAGE_FLAGS = 4194304;
    private static final String TAG = "WVUSWrapper";

    public PackageInfo getCurrentWebViewPackage() {
        try {
            return WebViewFactory.getUpdateService().getCurrentWebViewPackage();
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public List<ApplicationInfo> getValidWebViewApplicationInfos(Context context) {
        WebViewProviderInfo[] providers = null;
        try {
            providers = WebViewFactory.getUpdateService().getValidWebViewPackages();
        } catch (RemoteException e) {
        }
        List<ApplicationInfo> pkgs = new ArrayList();
        for (WebViewProviderInfo provider : providers) {
            try {
                pkgs.add(context.getPackageManager().getApplicationInfo(provider.packageName, 4194304));
            } catch (NameNotFoundException e2) {
            }
        }
        return pkgs;
    }

    public boolean setWebViewProvider(String packageName) {
        try {
            return packageName.equals(WebViewFactory.getUpdateService().changeProviderAndSetting(packageName));
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("RemoteException when trying to change provider to ");
            stringBuilder.append(packageName);
            Log.e(str, stringBuilder.toString(), e);
            return false;
        }
    }

    public List<UserPackage> getPackageInfosAllUsers(Context context, String packageName) {
        return UserPackage.getPackageInfosAllUsers(context, packageName, 4194304);
    }

    public void showInvalidChoiceToast(Context context) {
        Toast.makeText(context, R.string.select_webview_provider_toast_text, 0).show();
    }
}
