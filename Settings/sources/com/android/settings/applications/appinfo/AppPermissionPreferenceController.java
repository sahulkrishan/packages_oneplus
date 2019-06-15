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
import android.util.Log;
import com.android.settings.R;
import com.android.settingslib.applications.PermissionsSummaryHelper;
import com.android.settingslib.applications.PermissionsSummaryHelper.PermissionsResultCallback;
import java.util.ArrayList;
import java.util.List;

public class AppPermissionPreferenceController extends AppInfoPreferenceControllerBase {
    private static final String EXTRA_HIDE_INFO_BUTTON = "hideInfoButton";
    private static final String TAG = "PermissionPrefControl";
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
                    ArrayList<CharSequence> list = new ArrayList(grantedGroupLabels);
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

    public AppPermissionPreferenceController(Context context, String key) {
        super(context, key);
    }

    public void updateState(Preference preference) {
        PermissionsSummaryHelper.getPermissionSummary(this.mContext, this.mPackageName, this.mPermissionCallback);
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
}
