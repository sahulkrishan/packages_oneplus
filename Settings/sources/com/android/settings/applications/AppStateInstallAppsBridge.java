package com.android.settings.applications;

import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.List;

public class AppStateInstallAppsBridge extends AppStateBaseBridge {
    public static final AppFilter FILTER_APP_SOURCES = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            if (info.extraInfo == null || !(info.extraInfo instanceof InstallAppsState) || UserHandle.getUserId(info.info.uid) == 999) {
                return false;
            }
            return info.extraInfo.isPotentialAppSource();
        }
    };
    private static final String TAG = AppStateInstallAppsBridge.class.getSimpleName();
    private final AppOpsManager mAppOpsManager;
    private final IPackageManager mIpm = AppGlobals.getPackageManager();

    public static class InstallAppsState {
        int appOpMode = 3;
        boolean isSystemApp = false;
        boolean permissionGranted;
        boolean permissionRequested;

        public boolean canInstallApps() {
            if (this.appOpMode == 3) {
                return this.permissionGranted;
            }
            return this.appOpMode == 0;
        }

        public boolean isPotentialAppSource() {
            boolean z = false;
            if (this.isSystemApp) {
                return false;
            }
            if (this.appOpMode != 3 || this.permissionRequested) {
                z = true;
            }
            return z;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[permissionGranted: ");
            stringBuilder.append(this.permissionGranted);
            StringBuilder sb = new StringBuilder(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", permissionRequested: ");
            stringBuilder.append(this.permissionRequested);
            sb.append(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(", appOpMode: ");
            stringBuilder.append(this.appOpMode);
            sb.append(stringBuilder.toString());
            sb.append("]");
            return sb.toString();
        }
    }

    public AppStateInstallAppsBridge(Context context, ApplicationsState appState, Callback callback) {
        super(appState, callback);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String packageName, int uid) {
        app.extraInfo = createInstallAppsStateFor(packageName, uid);
    }

    /* Access modifiers changed, original: protected */
    public void loadAllExtraInfo() {
        List<AppEntry> allApps = this.mAppSession.getAllApps();
        for (int i = 0; i < allApps.size(); i++) {
            AppEntry currentEntry = (AppEntry) allApps.get(i);
            updateExtraInfo(currentEntry, currentEntry.info.packageName, currentEntry.info.uid);
        }
    }

    private boolean hasRequestedAppOpPermission(String permission, String packageName) {
        try {
            return ArrayUtils.contains(this.mIpm.getAppOpPermissionPackages(permission), packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "PackageManager dead. Cannot get permission info");
            return false;
        }
    }

    private boolean hasPermission(String permission, int uid) {
        boolean z = false;
        try {
            if (this.mIpm.checkUidPermission(permission, uid) == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "PackageManager dead. Cannot get permission info");
            return false;
        }
    }

    private int getAppOpMode(int appOpCode, int uid, String packageName) {
        return this.mAppOpsManager.checkOpNoThrow(appOpCode, uid, packageName);
    }

    private boolean isSystemApp(int uid) {
        return UserHandle.isApp(uid) ^ 1;
    }

    public InstallAppsState createInstallAppsStateFor(String packageName, int uid) {
        InstallAppsState appState = new InstallAppsState();
        appState.permissionRequested = hasRequestedAppOpPermission("android.permission.REQUEST_INSTALL_PACKAGES", packageName);
        appState.permissionGranted = hasPermission("android.permission.REQUEST_INSTALL_PACKAGES", uid);
        appState.appOpMode = getAppOpMode(73, uid, packageName);
        if (UserHandle.getUserId(uid) == 0) {
            int parellelAppUId = UserHandle.getUid(999, UserHandle.getAppId(uid));
            int parellelAppMode = getAppOpMode(73, parellelAppUId, packageName);
            if (appState.appOpMode != parellelAppMode) {
                if (appState.appOpMode == 3) {
                    this.mAppOpsManager.setMode(73, uid, packageName, parellelAppMode);
                    appState.appOpMode = getAppOpMode(73, uid, packageName);
                } else {
                    this.mAppOpsManager.setMode(73, parellelAppUId, packageName, appState.appOpMode);
                }
            }
        }
        appState.isSystemApp = isSystemApp(uid);
        return appState;
    }
}
