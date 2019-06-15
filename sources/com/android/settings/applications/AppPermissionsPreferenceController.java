package com.android.settings.applications;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import java.util.Set;

public class AppPermissionsPreferenceController extends BasePreferenceController {
    private static final String KEY_APP_PERMISSION_GROUPS = "manage_perms";
    private static final int NUM_PERMISSION_TO_USE = 3;
    private static final String[] PERMISSION_GROUPS = new String[]{"android.permission-group.LOCATION", "android.permission-group.MICROPHONE", "android.permission-group.CAMERA", "android.permission-group.SMS", "android.permission-group.CONTACTS", "android.permission-group.PHONE"};
    private static final String TAG = "AppPermissionPrefCtrl";
    private final PackageManager mPackageManager;

    public AppPermissionsPreferenceController(Context context) {
        super(context, KEY_APP_PERMISSION_GROUPS);
        this.mPackageManager = context.getPackageManager();
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public CharSequence getSummary() {
        Set<String> grantedPermissionGroups = getGrantedPermissionGroups(getAllPermissionsInGroups());
        int count = 0;
        CharSequence summary = null;
        for (String group : PERMISSION_GROUPS) {
            if (grantedPermissionGroups.contains(group)) {
                summary = concatSummaryText(summary, group);
                count++;
                if (count >= 3) {
                    break;
                }
            }
        }
        if (count <= 0) {
            return null;
        }
        return this.mContext.getString(R.string.app_permissions_summary, new Object[]{summary});
    }

    private Set<String> getGrantedPermissionGroups(Set<String> permissions) {
        ArraySet<String> grantedPermissionGroups = new ArraySet();
        for (PackageInfo installedPackage : this.mPackageManager.getInstalledPackages(4096)) {
            if (installedPackage.permissions != null) {
                for (PermissionInfo permissionInfo : installedPackage.permissions) {
                    if (permissions.contains(permissionInfo.name) && !grantedPermissionGroups.contains(permissionInfo.group)) {
                        grantedPermissionGroups.add(permissionInfo.group);
                    }
                }
            }
        }
        return grantedPermissionGroups;
    }

    private CharSequence concatSummaryText(CharSequence currentSummary, String permission) {
        String label = getPermissionGroupLabel(permission).toString().toLowerCase();
        if (TextUtils.isEmpty(currentSummary)) {
            return label;
        }
        return this.mContext.getString(R.string.join_many_items_middle, new Object[]{currentSummary, label});
    }

    private CharSequence getPermissionGroupLabel(String group) {
        try {
            return this.mPackageManager.getPermissionGroupInfo(group, 0).loadLabel(this.mPackageManager);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error getting permissions label.", e);
            return group;
        }
    }

    private Set<String> getAllPermissionsInGroups() {
        ArraySet<String> result = new ArraySet();
        for (String group : PERMISSION_GROUPS) {
            try {
                for (PermissionInfo permissionInfo : this.mPackageManager.queryPermissionsByGroup(group, 0)) {
                    result.add(permissionInfo.name);
                }
            } catch (NameNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error getting permissions in group ");
                stringBuilder.append(group);
                Log.e(str, stringBuilder.toString(), e);
            }
        }
        return result;
    }
}
