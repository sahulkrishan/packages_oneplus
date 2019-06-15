package com.android.settings.wifi;

import android.content.Context;
import com.android.internal.util.ArrayUtils;
import com.android.settings.applications.AppStateAppOpsBridge;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;

public class AppStateChangeWifiStateBridge extends AppStateAppOpsBridge {
    private static final int APP_OPS_OP_CODE = 65;
    public static final AppFilter FILTER_CHANGE_WIFI_STATE = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            if (info == null || info.extraInfo == null) {
                return false;
            }
            WifiSettingsState wifiSettingsState = info.extraInfo;
            if (wifiSettingsState.packageInfo == null || !ArrayUtils.contains(wifiSettingsState.packageInfo.requestedPermissions, AppStateChangeWifiStateBridge.PM_NETWORK_SETTINGS)) {
                return wifiSettingsState.permissionDeclared;
            }
            return false;
        }
    };
    private static final String PM_CHANGE_WIFI_STATE = "android.permission.CHANGE_WIFI_STATE";
    private static final String PM_NETWORK_SETTINGS = "android.permission.NETWORK_SETTINGS";
    private static final String[] PM_PERMISSIONS = new String[]{PM_CHANGE_WIFI_STATE};
    private static final String TAG = "AppStateChangeWifiStateBridge";

    public static class WifiSettingsState extends PermissionState {
        public WifiSettingsState(PermissionState permissionState) {
            super(permissionState.packageName, permissionState.userHandle);
            this.packageInfo = permissionState.packageInfo;
            this.appOpMode = permissionState.appOpMode;
            this.permissionDeclared = permissionState.permissionDeclared;
            this.staticPermissionGranted = permissionState.staticPermissionGranted;
        }
    }

    public AppStateChangeWifiStateBridge(Context context, ApplicationsState appState, Callback callback) {
        super(context, appState, callback, 65, PM_PERMISSIONS);
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = getWifiSettingsInfo(pkg, uid);
    }

    /* Access modifiers changed, original: protected */
    public void loadAllExtraInfo() {
        for (AppEntry entry : this.mAppSession.getAllApps()) {
            updateExtraInfo(entry, entry.info.packageName, entry.info.uid);
        }
    }

    public WifiSettingsState getWifiSettingsInfo(String pkg, int uid) {
        return new WifiSettingsState(super.getPermissionInfo(pkg, uid));
    }
}
