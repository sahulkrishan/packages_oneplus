package com.android.settings.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class AppPermissions {
    private static final String TAG = "AppPermissions";
    private final Context mContext;
    private final ArrayMap<String, PermissionGroup> mGroups = new ArrayMap();
    private final PackageInfo mPackageInfo;

    private static final class Permission {
        private boolean granted;
        private final boolean runtime;

        public Permission(boolean runtime, boolean granted) {
            this.runtime = runtime;
            this.granted = granted;
        }
    }

    private static final class PermissionGroup {
        private boolean mHasRuntimePermissions;
        private final ArrayMap<String, Permission> mPermissions;

        private PermissionGroup() {
            this.mPermissions = new ArrayMap();
        }

        public boolean hasRuntimePermissions() {
            return this.mHasRuntimePermissions;
        }

        public boolean areRuntimePermissionsGranted() {
            int permissionCount = this.mPermissions.size();
            for (int i = 0; i < permissionCount; i++) {
                Permission permission = (Permission) this.mPermissions.valueAt(i);
                if (permission.runtime && !permission.granted) {
                    return false;
                }
            }
            return true;
        }

        public List<Permission> getPermissions() {
            return new ArrayList(this.mPermissions.values());
        }

        /* Access modifiers changed, original: 0000 */
        public void addPermission(Permission permission, String permName) {
            this.mPermissions.put(permName, permission);
            if (permission.runtime) {
                this.mHasRuntimePermissions = true;
            }
        }
    }

    public AppPermissions(Context context, String packageName) {
        this.mContext = context;
        this.mPackageInfo = getPackageInfo(packageName);
        refresh();
    }

    private PackageInfo getPackageInfo(String packageName) {
        try {
            return this.mContext.getPackageManager().getPackageInfo(packageName, 4096);
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to find ");
            stringBuilder.append(packageName);
            Log.e(str, stringBuilder.toString(), e);
            return null;
        }
    }

    public void refresh() {
        if (this.mPackageInfo != null) {
            loadPermissionGroups();
        }
    }

    public int getPermissionCount() {
        return this.mGroups.size();
    }

    public int getGrantedPermissionsCount() {
        int ct = 0;
        for (int i = 0; i < this.mGroups.size(); i++) {
            if (((PermissionGroup) this.mGroups.valueAt(i)).areRuntimePermissionsGranted()) {
                ct++;
            }
        }
        return ct;
    }

    private void loadPermissionGroups() {
        this.mGroups.clear();
        if (this.mPackageInfo.requestedPermissions != null) {
            boolean appSupportsRuntimePermissions = appSupportsRuntime(this.mPackageInfo.applicationInfo);
            int i = 0;
            while (true) {
                boolean granted = true;
                if (i >= this.mPackageInfo.requestedPermissions.length) {
                    break;
                }
                String requestedPerm = this.mPackageInfo.requestedPermissions[i];
                try {
                    PermissionInfo permInfo = this.mContext.getPackageManager().getPermissionInfo(requestedPerm, 0);
                    String permName = permInfo.name;
                    String groupName = permInfo.group != null ? permInfo.group : permName;
                    PermissionGroup group = (PermissionGroup) this.mGroups.get(groupName);
                    if (group == null) {
                        group = new PermissionGroup();
                        this.mGroups.put(groupName, group);
                    }
                    boolean runtime = appSupportsRuntimePermissions && permInfo.protectionLevel == 1;
                    if ((this.mPackageInfo.requestedPermissionsFlags[i] & 2) == 0) {
                        granted = false;
                    }
                    group.addPermission(new Permission(runtime, granted), permName);
                } catch (NameNotFoundException e) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unknown permission: ");
                    stringBuilder.append(requestedPerm);
                    Log.w(str, stringBuilder.toString());
                }
                i++;
            }
            for (int i2 = this.mGroups.size() - 1; i2 >= 0; i2--) {
                if (!((PermissionGroup) this.mGroups.valueAt(i2)).mHasRuntimePermissions) {
                    this.mGroups.removeAt(i2);
                }
            }
        }
    }

    public static boolean appSupportsRuntime(ApplicationInfo info) {
        return info.targetSdkVersion > 22;
    }
}
