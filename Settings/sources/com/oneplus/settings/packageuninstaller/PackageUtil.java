package com.oneplus.settings.packageuninstaller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.CallbackImpl;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PackageParserException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import java.io.File;

public class PackageUtil {
    public static final String INTENT_ATTR_APPLICATION_INFO = "com.android.packageinstaller.applicationInfo";
    public static final String INTENT_ATTR_INSTALL_STATUS = "com.android.packageinstaller.installStatus";
    public static final String INTENT_ATTR_PACKAGE_NAME = "com.android.packageinstaller.PackageName";
    public static final String INTENT_ATTR_PERMISSIONS_LIST = "com.android.packageinstaller.PermissionsList";
    private static final String LOG_TAG = PackageUtil.class.getSimpleName();
    public static final String PREFIX = "com.android.packageinstaller.";

    public static class AppSnippet {
        @Nullable
        public Drawable icon;
        @NonNull
        public CharSequence label;

        public AppSnippet(@NonNull CharSequence label, @Nullable Drawable icon) {
            this.label = label;
            this.icon = icon;
        }
    }

    public static Package getPackageInfo(Context context, File sourceFile) {
        PackageParser parser = new PackageParser();
        parser.setCallback(new CallbackImpl(context.getPackageManager()));
        try {
            return parser.parsePackage(sourceFile, 0);
        } catch (PackageParserException e) {
            return null;
        }
    }

    public static View initSnippet(View snippetView, CharSequence label, Drawable icon) {
        ((ImageView) snippetView.findViewById(R.id.app_icon)).setImageDrawable(icon);
        ((TextView) snippetView.findViewById(R.id.app_name)).setText(label);
        return snippetView;
    }

    public static View initSnippetForInstalledApp(Context pContext, ApplicationInfo appInfo, View snippetView) {
        return initSnippetForInstalledApp(pContext, appInfo, snippetView, null);
    }

    public static View initSnippetForInstalledApp(Context pContext, ApplicationInfo appInfo, View snippetView, UserHandle user) {
        PackageManager pm = pContext.getPackageManager();
        Drawable icon = appInfo.loadIcon(pm);
        if (user != null) {
            icon = pContext.getPackageManager().getUserBadgedIcon(icon, user);
        }
        return initSnippet(snippetView, appInfo.loadLabel(pm), icon);
    }

    @NonNull
    public static View initSnippetForNewApp(@NonNull Activity pContext, @NonNull AppSnippet as, int snippetId) {
        View appSnippet = pContext.findViewById(snippetId);
        if (as.icon != null) {
            ((ImageView) appSnippet.findViewById(R.id.app_icon)).setImageDrawable(as.icon);
        }
        ((TextView) appSnippet.findViewById(R.id.app_name)).setText(as.label);
        return appSnippet;
    }

    public static AppSnippet getAppSnippet(Activity pContext, ApplicationInfo appInfo, File sourceFile) {
        String archiveFilePath = sourceFile.getAbsolutePath();
        Resources pRes = pContext.getResources();
        AssetManager assmgr = new AssetManager();
        assmgr.addAssetPath(archiveFilePath);
        Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
        CharSequence label = null;
        if (appInfo.labelRes != 0) {
            try {
                label = res.getText(appInfo.labelRes);
            } catch (NotFoundException e) {
            }
        }
        if (label == null) {
            label = appInfo.nonLocalizedLabel != null ? appInfo.nonLocalizedLabel : appInfo.packageName;
        }
        Drawable icon = null;
        try {
            if (appInfo.icon != 0) {
                try {
                    icon = res.getDrawable(appInfo.icon);
                } catch (NotFoundException e2) {
                }
            }
            if (icon == null) {
                icon = pContext.getPackageManager().getDefaultActivityIcon();
            }
        } catch (OutOfMemoryError e3) {
            Log.i(LOG_TAG, "Could not load app icon", e3);
        }
        return new AppSnippet(label, icon);
    }

    static int getMaxTargetSdkVersionForUid(@NonNull Context context, int uid) {
        PackageManager pm = context.getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);
        if (packages == null) {
            return -1;
        }
        int targetSdkVersion = -1;
        for (String packageName : packages) {
            try {
                targetSdkVersion = Math.max(targetSdkVersion, pm.getApplicationInfo(packageName, 0).targetSdkVersion);
            } catch (NameNotFoundException e) {
            }
        }
        return targetSdkVersion;
    }
}
