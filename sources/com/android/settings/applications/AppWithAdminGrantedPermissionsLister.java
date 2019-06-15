package com.android.settings.applications;

import android.app.admin.DevicePolicyManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.os.UserManager;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public abstract class AppWithAdminGrantedPermissionsLister extends AppLister {
    private final DevicePolicyManager mDevicePolicyManager;
    private final IPackageManager mPackageManagerService;
    private final String[] mPermissions;

    public AppWithAdminGrantedPermissionsLister(String[] permissions, PackageManagerWrapper packageManager, IPackageManager packageManagerService, DevicePolicyManager devicePolicyManager, UserManager userManager) {
        super(packageManager, userManager);
        this.mPermissions = permissions;
        this.mPackageManagerService = packageManagerService;
        this.mDevicePolicyManager = devicePolicyManager;
    }

    /* Access modifiers changed, original: protected */
    public boolean includeInCount(ApplicationInfo info) {
        return AppWithAdminGrantedPermissionsCounter.includeInCount(this.mPermissions, this.mDevicePolicyManager, this.mPm, this.mPackageManagerService, info);
    }
}
