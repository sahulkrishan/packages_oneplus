package com.android.settings.applications;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.AppStateUsageBridge.UsageState;
import com.android.settings.overlay.FeatureFactory;

public class UsageAccessDetails extends AppInfoWithHeader implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String KEY_APP_OPS_PREFERENCE_SCREEN = "app_ops_preference_screen";
    private static final String KEY_APP_OPS_SETTINGS_DESC = "app_ops_settings_description";
    private static final String KEY_APP_OPS_SETTINGS_SWITCH = "app_ops_settings_switch";
    private AppOpsManager mAppOpsManager;
    private DevicePolicyManager mDpm;
    private Intent mSettingsIntent;
    private SwitchPreference mSwitchPref;
    private AppStateUsageBridge mUsageBridge;
    private Preference mUsageDesc;
    private UsageState mUsageState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mUsageBridge = new AppStateUsageBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mDpm = (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class);
        addPreferencesFromResource(R.xml.app_ops_permissions_details);
        this.mSwitchPref = (SwitchPreference) findPreference(KEY_APP_OPS_SETTINGS_SWITCH);
        this.mUsageDesc = findPreference(KEY_APP_OPS_SETTINGS_DESC);
        getPreferenceScreen().setTitle((int) R.string.usage_access);
        this.mSwitchPref.setTitle((int) R.string.permit_usage_access);
        this.mUsageDesc.setSummary((int) R.string.usage_access_description);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
        this.mSettingsIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.USAGE_ACCESS_CONFIG").setPackage(this.mPackageName);
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mSwitchPref) {
            return false;
        }
        if (!(this.mUsageState == null || ((Boolean) newValue).booleanValue() == this.mUsageState.isPermissible())) {
            if (this.mUsageState.isPermissible() && this.mDpm.isProfileOwnerApp(this.mPackageName)) {
                new Builder(getContext()).setIcon(17302338).setTitle(17039380).setMessage(R.string.work_profile_usage_access_warning).setPositiveButton(R.string.okay, null).show();
            }
            setHasAccess(this.mUsageState.isPermissible() ^ 1);
            refreshUi();
        }
        return true;
    }

    private void setHasAccess(boolean newState) {
        logSpecialPermissionChange(newState, this.mPackageName);
        this.mAppOpsManager.setMode(43, this.mPackageInfo.applicationInfo.uid, this.mPackageName, newState ^ 1);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void logSpecialPermissionChange(boolean newState, String packageName) {
        int logCategory;
        if (newState) {
            logCategory = 783;
        } else {
            logCategory = 784;
        }
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), logCategory, packageName, new Pair[0]);
    }

    /* Access modifiers changed, original: protected */
    public boolean refreshUi() {
        retrieveAppEntry();
        if (this.mAppEntry == null || this.mPackageInfo == null) {
            return false;
        }
        this.mUsageState = this.mUsageBridge.getUsageInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        this.mSwitchPref.setChecked(this.mUsageState.isPermissible());
        this.mSwitchPref.setEnabled(this.mUsageState.permissionDeclared);
        ResolveInfo resolveInfo = this.mPm.resolveActivityAsUser(this.mSettingsIntent, 128, this.mUserId);
        if (resolveInfo != null) {
            Bundle metaData = resolveInfo.activityInfo.metaData;
            this.mSettingsIntent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
            if (metaData != null && metaData.containsKey("android.settings.metadata.USAGE_ACCESS_REASON")) {
                this.mSwitchPref.setSummary((CharSequence) metaData.getString("android.settings.metadata.USAGE_ACCESS_REASON"));
            }
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    public int getMetricsCategory() {
        return 183;
    }
}
