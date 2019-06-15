package com.android.settings.applications;

import android.content.Context;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.SliceBroadcastRelay;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.ArrayList;
import java.util.List;

public class AppStateOverlayBridge extends AppStateAppOpsBridge {
    private static final int APP_OPS_OP_CODE = 24;
    public static final AppFilter FILTER_SYSTEM_ALERT_WINDOW = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            return info.extraInfo != null;
        }
    };
    private static final String[] PM_PERMISSION = new String[]{PM_SYSTEM_ALERT_WINDOW};
    private static final String PM_SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private static final String TAG = "AppStateOverlayBridge";

    public static class OverlayState extends PermissionState {
        private static final List<String> DISABLE_PACKAGE_LIST = new ArrayList();
        public final boolean controlEnabled;

        static {
            DISABLE_PACKAGE_LIST.add(SliceBroadcastRelay.SYSTEMUI_PACKAGE);
        }

        public OverlayState(PermissionState permissionState) {
            super(permissionState.packageName, permissionState.userHandle);
            this.packageInfo = permissionState.packageInfo;
            this.appOpMode = permissionState.appOpMode;
            this.permissionDeclared = permissionState.permissionDeclared;
            this.staticPermissionGranted = permissionState.staticPermissionGranted;
            this.controlEnabled = DISABLE_PACKAGE_LIST.contains(permissionState.packageName) ^ 1;
        }
    }

    public AppStateOverlayBridge(Context context, ApplicationsState appState, Callback callback) {
        super(context, appState, callback, 24, PM_PERMISSION);
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = getOverlayInfo(pkg, uid);
    }

    public OverlayState getOverlayInfo(String pkg, int uid) {
        return new OverlayState(super.getPermissionInfo(pkg, uid));
    }

    public int getNumberOfPackagesWithPermission() {
        return super.getNumPackagesDeclaredPermission();
    }

    public int getNumberOfPackagesCanDrawOverlay() {
        return super.getNumPackagesAllowedByAppOps();
    }
}
