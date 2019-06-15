package com.android.settings.applications;

import android.content.Context;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;

public class AppStateUsageBridge extends AppStateAppOpsBridge {
    private static final int APP_OPS_OP_CODE = 43;
    public static final AppFilter FILTER_APP_USAGE = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            return info.extraInfo != null;
        }
    };
    private static final String[] PM_PERMISSION = new String[]{PM_USAGE_STATS};
    private static final String PM_USAGE_STATS = "android.permission.PACKAGE_USAGE_STATS";
    private static final String TAG = "AppStateUsageBridge";

    public static class UsageState extends PermissionState {
        public UsageState(PermissionState permissionState) {
            super(permissionState.packageName, permissionState.userHandle);
            this.packageInfo = permissionState.packageInfo;
            this.appOpMode = permissionState.appOpMode;
            this.permissionDeclared = permissionState.permissionDeclared;
            this.staticPermissionGranted = permissionState.staticPermissionGranted;
        }
    }

    public AppStateUsageBridge(Context context, ApplicationsState appState, Callback callback) {
        super(context, appState, callback, 43, PM_PERMISSION);
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = getUsageInfo(pkg, uid);
    }

    public UsageState getUsageInfo(String pkg, int uid) {
        return new UsageState(super.getPermissionInfo(pkg, uid));
    }
}
