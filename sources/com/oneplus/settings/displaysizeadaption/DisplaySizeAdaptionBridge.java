package com.oneplus.settings.displaysizeadaption;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisplaySizeAdaptionBridge extends AppStateBaseBridge {
    private static final String BROWSER_H2_PACKAGE_NAME = "com.nearme.browser";
    private static final String BROWSER_PACKAGE_NAME = "com.android.browser";
    private static final String CALENDAR_PACKAGE_NAME = "com.google.android.calendar";
    private static final String CHROME_PACKAGE_NAME = "com.android.chrome";
    private static final String DOWNLOADS_PACKAGE_NAME = "com.android.documentsui";
    private static final String DRIVE_PACKAGE_NAME = "com.google.android.apps.docs";
    private static final String DUO_PACKAGE_NAME = "com.google.android.apps.tachyon";
    public static final AppFilter FILTER_APP_All = new AppFilter() {
        public void init() {
            DisplaySizeAdaptionBridge.resolveInfoList = DisplaySizeAdaptionBridge.getLauncherApp();
        }

        public boolean filterApp(AppEntry appEntry) {
            boolean z = false;
            if (appEntry == null) {
                return false;
            }
            if (OPUtils.isSupportScreenCutting()) {
                if (((UserHandle.getUserId(appEntry.info.uid) != 999 && (appEntry.info.flags & 1) == 0 && DisplaySizeAdaptionBridge.isLauncherApp(appEntry.info.packageName)) || DisplaySizeAdaptionBridge.packageExcludeFilter(appEntry.info.packageName)) && appEntry.info.targetSdkVersion < 28) {
                    z = true;
                }
                return z;
            }
            if (UserHandle.getUserId(appEntry.info.uid) != 999 && (appEntry.info.flags & 1) == 0 && DisplaySizeAdaptionBridge.isLauncherApp(appEntry.info.packageName)) {
                z = true;
            }
            return z;
        }
    };
    public static final AppFilter FILTER_APP_DEFAULT = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry appEntry) {
            boolean z = false;
            if (appEntry == null || appEntry.extraInfo == null) {
                return false;
            }
            OPAppModel acm = appEntry.extraInfo;
            if (OPUtils.isSupportScreenCutting()) {
                if (((UserHandle.getUserId(appEntry.info.uid) != 999 && (appEntry.info.flags & 1) == 0 && DisplaySizeAdaptionBridge.isLauncherApp(appEntry.info.packageName)) || DisplaySizeAdaptionBridge.packageExcludeFilter(appEntry.info.packageName)) && DisplaySizeAdaptionBridge.mManager.getAppTypeValue(acm.getPkgName()) == 3 && appEntry.info.targetSdkVersion < 28) {
                    z = true;
                }
                return z;
            }
            if (UserHandle.getUserId(appEntry.info.uid) != 999 && (appEntry.info.flags & 1) == 0 && DisplaySizeAdaptionBridge.mManager.getAppTypeValue(acm.getPkgName()) == 3) {
                z = true;
            }
            return z;
        }
    };
    public static final AppFilter FILTER_APP_FULL_SCREEN = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry appEntry) {
            boolean z = false;
            if (appEntry == null || appEntry.extraInfo == null) {
                return false;
            }
            OPAppModel acm = appEntry.extraInfo;
            if (OPUtils.isSupportScreenCutting()) {
                if (((UserHandle.getUserId(appEntry.info.uid) != 999 && (appEntry.info.flags & 1) == 0 && DisplaySizeAdaptionBridge.isLauncherApp(appEntry.info.packageName)) || DisplaySizeAdaptionBridge.packageExcludeFilter(appEntry.info.packageName)) && DisplaySizeAdaptionBridge.mManager.getAppTypeValue(acm.getPkgName()) == 1 && appEntry.info.targetSdkVersion < 28) {
                    z = true;
                }
                return z;
            }
            if (UserHandle.getUserId(appEntry.info.uid) != 999 && (appEntry.info.flags & 1) == 0 && DisplaySizeAdaptionBridge.mManager.getAppTypeValue(acm.getPkgName()) == 1) {
                z = true;
            }
            return z;
        }
    };
    public static final AppFilter FILTER_APP_ORIGINAL_SIZE = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry appEntry) {
            boolean z = false;
            if (appEntry == null || appEntry.extraInfo == null) {
                return false;
            }
            OPAppModel acm = appEntry.extraInfo;
            if (UserHandle.getUserId(appEntry.info.uid) != 999 && (appEntry.info.flags & 1) == 0 && DisplaySizeAdaptionBridge.mManager.getAppTypeValue(acm.getPkgName()) == 0) {
                z = true;
            }
            return z;
        }
    };
    private static final String GMAIL_PACKAGE_NAME = "com.google.android.gm";
    private static final String GOOGLEQUICKSEARCHBOX_PACKAGE_NAME = "com.google.android.googlequicksearchbox";
    private static final String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.walletnfcrel";
    private static final String MAPS_PACKAGE_NAME = "com.google.android.apps.maps";
    private static final String MUSIC_PACKAGE_NAME = "com.google.android.music";
    private static final String PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos";
    private static final String PLAY_STORE_PACKAGE_NAME = "com.android.vending";
    private static final String VIDEOS_PACKAGE_NAME = "com.google.android.videos";
    private static final String YOUTUBE_PACKAGE_NAME = "com.google.android.youtube";
    private static final DisplaySizeAdaptiongeManager mManager = DisplaySizeAdaptiongeManager.getInstance(SettingsBaseApplication.mApplication);
    private static List<ResolveInfo> resolveInfoList;
    private AppOpsManager mAppOpsManager = ((AppOpsManager) this.mContext.getSystemService("appops"));
    private final Context mContext;
    private CharSequence mLabel;
    private final PackageManager mPm = this.mContext.getPackageManager();

    public DisplaySizeAdaptionBridge(Context context, ApplicationsState appState, Callback callback) {
        super(appState, callback);
        this.mContext = context;
    }

    /* Access modifiers changed, original: protected */
    public void loadAllExtraInfo() {
        ArrayList<AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        Map<String, OPAppModel> map = mManager.loadAppMap();
        if (map != null) {
            for (int i = 0; i < N; i++) {
                AppEntry app = (AppEntry) apps.get(i);
                app.extraInfo = map.get(app.info.packageName);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String pkg, int uid) {
        try {
            app.extraInfo = new OPAppModel(pkg, this.mPm.getApplicationInfo(pkg, 0).loadLabel(this.mPm).toString(), "", this.mPm.getApplicationInfo(pkg, 0).uid, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean packageExcludeFilter(String packageName) {
        boolean z = "com.google.android.calendar".equals(packageName) || "com.android.chrome".equals(packageName) || DOWNLOADS_PACKAGE_NAME.equals(packageName) || DRIVE_PACKAGE_NAME.equals(packageName) || DUO_PACKAGE_NAME.equals(packageName) || GMAIL_PACKAGE_NAME.equals(packageName) || "com.google.android.googlequicksearchbox".equals(packageName) || GOOGLE_PAY_PACKAGE_NAME.equals(packageName) || "com.google.android.apps.maps".equals(packageName) || PHOTOS_PACKAGE_NAME.equals(packageName) || VIDEOS_PACKAGE_NAME.equals(packageName) || "com.google.android.music".equals(packageName) || PLAY_STORE_PACKAGE_NAME.equals(packageName) || "com.google.android.youtube".equals(packageName) || "com.android.browser".equals(packageName) || BROWSER_H2_PACKAGE_NAME.equals(packageName);
        return z;
    }

    private static List<ResolveInfo> getLauncherApp() {
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        return SettingsBaseApplication.mApplication.getPackageManager().queryIntentActivities(mainIntent, 0);
    }

    private static boolean isLauncherApp(String packageName) {
        for (int i = 0; i < resolveInfoList.size(); i++) {
            if (packageName.equals(((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
}
