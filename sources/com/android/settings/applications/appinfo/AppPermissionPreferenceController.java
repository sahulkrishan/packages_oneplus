package com.android.settings.applications.appinfo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.icu.text.ListFormatter;
import android.os.Build.VERSION;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.util.GlobalPermissionState.PackagePermissionState;
import android.util.GlobalPermissionState.PermissionState;
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.applications.PermissionsSummaryHelper;
import com.android.settingslib.applications.PermissionsSummaryHelper.PermissionsResultCallback;
import com.oneplus.settings.permission.CustomPermissionUtils;
import com.oneplus.settings.permission.PermissionManager;
import com.oneplus.settings.permission.PermissionManager.Callback;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class AppPermissionPreferenceController extends AppInfoPreferenceControllerBase {
    private static final String EXTRA_HIDE_INFO_BUTTON = "hideInfoButton";
    private static final String TAG = "PermissionPrefControl";
    private Context mContext;
    private List<CharSequence> mCustomPermissionNameList = new ArrayList();
    private String mPackageName;
    @VisibleForTesting
    final PermissionsResultCallback mPermissionCallback = new PermissionsResultCallback() {
        public void onPermissionSummaryResult(int standardGrantedPermissionCount, int requestedPermissionCount, int additionalGrantedPermissionCount, List<CharSequence> grantedGroupLabels) {
            if (AppPermissionPreferenceController.this.mParent.getActivity() != null) {
                CharSequence summary;
                Resources res = AppPermissionPreferenceController.this.mContext.getResources();
                if (requestedPermissionCount == 0) {
                    summary = res.getString(R.string.runtime_permissions_summary_no_permissions_requested);
                    AppPermissionPreferenceController.this.mPreference.setEnabled(false);
                } else {
                    List<CharSequence> list = AppPermissionPreferenceController.mergeList(new ArrayList(grantedGroupLabels), AppPermissionPreferenceController.this.mCustomPermissionNameList);
                    if (additionalGrantedPermissionCount > 0) {
                        list.add(res.getQuantityString(R.plurals.runtime_permissions_additional_count, additionalGrantedPermissionCount, new Object[]{Integer.valueOf(additionalGrantedPermissionCount)}));
                    }
                    if (list.size() == 0) {
                        summary = res.getString(R.string.runtime_permissions_summary_no_permissions_granted);
                    } else {
                        summary = ListFormatter.getInstance().format(list);
                    }
                    AppPermissionPreferenceController.this.mPreference.setEnabled(true);
                }
                AppPermissionPreferenceController.this.mPreference.setSummary(summary);
            }
        }
    };
    private PermissionManager mPermissionManager;

    public AppPermissionPreferenceController(Context context, String key) {
        super(context, key);
        this.mContext = context;
    }

    public void updateState(Preference preference) {
        if (VERSION.IS_CTA_BUILD) {
            loadCustomPermission(this.mPackageName);
        } else {
            PermissionsSummaryHelper.getPermissionSummary(this.mContext, this.mPackageName, this.mPermissionCallback);
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!getPreferenceKey().equals(preference.getKey())) {
            return false;
        }
        startManagePermissionsActivity();
        return true;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    private void startManagePermissionsActivity() {
        Intent intent;
        if (VERSION.IS_CTA_BUILD) {
            intent = new Intent("com.oneplus.security.action.OPPERMISSION_APP");
            intent.putExtra("packageName", this.mParent.getAppEntry().info.packageName);
        } else {
            intent = new Intent("android.intent.action.MANAGE_APP_PERMISSIONS");
            intent.putExtra("android.intent.extra.PACKAGE_NAME", this.mParent.getAppEntry().info.packageName);
            intent.putExtra(EXTRA_HIDE_INFO_BUTTON, true);
        }
        try {
            Activity activity = this.mParent.getActivity();
            AppInfoDashboardFragment appInfoDashboardFragment = this.mParent;
            activity.startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "No app can handle android.intent.action.MANAGE_APP_PERMISSIONS");
        }
    }

    private void loadCustomPermission(final String packageName) {
        this.mPermissionManager = PermissionManager.get();
        this.mPermissionManager.setCallback(new Callback() {
            public void onServiceConnected() {
                String str = AppPermissionPreferenceController.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("PermissionManager queryPermissionData packageName:");
                stringBuilder.append(packageName);
                Log.d(str, stringBuilder.toString());
                AppPermissionPreferenceController.this.mPermissionManager.queryPermissionData(AppPermissionPreferenceController.this.mContext, packageName);
            }

            public void onPermissionDataObtained(PackagePermissionState permissionState) {
                AppPermissionPreferenceController.this.mCustomPermissionNameList.clear();
                if (permissionState == null || permissionState.mPermMap == null) {
                    PermissionsSummaryHelper.getPermissionSummary(AppPermissionPreferenceController.this.mContext, packageName, AppPermissionPreferenceController.this.mPermissionCallback);
                    return;
                }
                String str = AppPermissionPreferenceController.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("PermissionManager onPermissionDataObtained permissionState.mPkgName:");
                stringBuilder.append(permissionState.mPkgName);
                Log.d(str, stringBuilder.toString());
                for (Entry<String, PermissionState> entryPermState : permissionState.mPermMap.entrySet()) {
                    String permName = (String) entryPermState.getKey();
                    if (!CustomPermissionUtils.CUSTOM_PERMISSION_CONTROL_BLUETOOTH.equals(permName)) {
                        if (!CustomPermissionUtils.CUSTOM_PERMISSION_CONTROL_WIFI.equals(permName)) {
                            if (CustomPermissionUtils.isCustomPermission(permName) && ((PermissionState) entryPermState.getValue()).isGranted) {
                                String affectedGroupName = CustomPermissionUtils.getGroupForCustomPermission(permName);
                                String groupLabel = CustomPermissionUtils.getGroupLabelForCustomPermission(AppPermissionPreferenceController.this.mContext, permName);
                                AppPermissionPreferenceController.this.mCustomPermissionNameList.add(groupLabel);
                                String str2 = AppPermissionPreferenceController.TAG;
                                StringBuilder stringBuilder2 = new StringBuilder();
                                stringBuilder2.append("CustomPermissionUtils getGroupForCustomPermission affectedGroupName:");
                                stringBuilder2.append(affectedGroupName);
                                stringBuilder2.append(", permName:");
                                stringBuilder2.append(permName);
                                stringBuilder2.append(", groupLabel:");
                                stringBuilder2.append(groupLabel);
                                Log.d(str2, stringBuilder2.toString());
                            }
                        }
                    }
                }
                PermissionsSummaryHelper.getPermissionSummary(AppPermissionPreferenceController.this.mContext, packageName, AppPermissionPreferenceController.this.mPermissionCallback);
            }

            public void onPermissionDataUpdated(int result) {
                String str = AppPermissionPreferenceController.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("onPermissionDataUpdated result:");
                stringBuilder.append(result);
                Log.d(str, stringBuilder.toString());
            }
        });
        try {
            this.mPermissionManager.connectToPermissionControlService(this.mContext);
        } catch (Exception e) {
            Log.e(TAG, "connectToPermissionControlService error!");
            e.printStackTrace();
            PermissionsSummaryHelper.getPermissionSummary(this.mContext, packageName, this.mPermissionCallback);
        }
    }

    private static List<CharSequence> mergeList(List<CharSequence> list1, List<CharSequence> list2) {
        Set<CharSequence> set = new HashSet();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList(set);
    }
}
