package com.android.settings.applications;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.os.RemoteException;
import android.os.UserHandle;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public abstract class AppWithAdminGrantedPermissionsCounter extends AppCounter {
    private final DevicePolicyManager mDevicePolicyManager;
    private final IPackageManager mPackageManagerService;
    private final String[] mPermissions;

    public AppWithAdminGrantedPermissionsCounter(Context context, String[] permissions, PackageManagerWrapper packageManager, IPackageManager packageManagerService, DevicePolicyManager devicePolicyManager) {
        super(context, packageManager);
        this.mPermissions = permissions;
        this.mPackageManagerService = packageManagerService;
        this.mDevicePolicyManager = devicePolicyManager;
    }

    /* Access modifiers changed, original: protected */
    public boolean includeInCount(ApplicationInfo info) {
        return includeInCount(this.mPermissions, this.mDevicePolicyManager, this.mPm, this.mPackageManagerService, info);
    }

    public static boolean includeInCount(String[] permissions, DevicePolicyManager devicePolicyManager, PackageManagerWrapper packageManager, IPackageManager packageManagerService, ApplicationInfo info) {
        if (info.targetSdkVersion >= 23) {
            for (String permission : permissions) {
                if (devicePolicyManager.getPermissionGrantState(null, info.packageName, permission) == 1) {
                    return true;
                }
            }
            return false;
        } else if (packageManager.getInstallReason(info.packageName, new UserHandle(UserHandle.getUserId(info.uid))) != 1) {
            return false;
        } else {
            try {
                for (String permission2 : permissions) {
                    if (packageManagerService.checkUidPermission(permission2, info.uid) == 0) {
                        return true;
                    }
                }
            } catch (RemoteException e) {
            }
            return false;
        }
    }
}
