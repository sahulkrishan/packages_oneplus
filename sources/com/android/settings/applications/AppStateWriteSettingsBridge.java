package com.android.settings.applications;

import android.content.Context;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;

public class AppStateWriteSettingsBridge extends AppStateAppOpsBridge {
    private static final int APP_OPS_OP_CODE = 23;
    public static final AppFilter FILTER_WRITE_SETTINGS = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            return info.extraInfo != null;
        }
    };
    private static final String[] PM_PERMISSIONS = new String[]{PM_WRITE_SETTINGS};
    private static final String PM_WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String TAG = "AppStateWriteSettingsBridge";

    public static class WriteSettingsState extends PermissionState {
        public WriteSettingsState(PermissionState permissionState) {
            super(permissionState.packageName, permissionState.userHandle);
            this.packageInfo = permissionState.packageInfo;
            this.appOpMode = permissionState.appOpMode;
            this.permissionDeclared = permissionState.permissionDeclared;
            this.staticPermissionGranted = permissionState.staticPermissionGranted;
        }
    }

    public AppStateWriteSettingsBridge(Context context, ApplicationsState appState, Callback callback) {
        super(context, appState, callback, 23, PM_PERMISSIONS);
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = getWriteSettingsInfo(pkg, uid);
    }

    public WriteSettingsState getWriteSettingsInfo(String pkg, int uid) {
        return new WriteSettingsState(super.getPermissionInfo(pkg, uid));
    }

    public int getNumberOfPackagesWithPermission() {
        return super.getNumPackagesDeclaredPermission();
    }

    public int getNumberOfPackagesCanWriteSettings() {
        return super.getNumPackagesAllowedByAppOps();
    }
}
