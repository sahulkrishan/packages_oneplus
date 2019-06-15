package com.android.settings.applications.appinfo;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.applications.AppInfoWithHeader;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateWriteSettingsBridge;
import com.android.settings.applications.AppStateWriteSettingsBridge.WriteSettingsState;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class WriteSettingsDetails extends AppInfoWithHeader implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final int[] APP_OPS_OP_CODE = new int[]{23};
    private static final String KEY_APP_OPS_PREFERENCE_SCREEN = "app_ops_preference_screen";
    private static final String KEY_APP_OPS_SETTINGS_SWITCH = "app_ops_settings_switch";
    private static final String LOG_TAG = "WriteSettingsDetails";
    private AppStateWriteSettingsBridge mAppBridge;
    private AppOpsManager mAppOpsManager;
    private Intent mSettingsIntent;
    private SwitchPreference mSwitchPref;
    private WriteSettingsState mWriteSettingsState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mAppBridge = new AppStateWriteSettingsBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        addPreferencesFromResource(R.xml.write_system_settings_permissions_details);
        this.mSwitchPref = (SwitchPreference) findPreference(KEY_APP_OPS_SETTINGS_SWITCH);
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
        if (!(this.mWriteSettingsState == null || ((Boolean) newValue).booleanValue() == this.mWriteSettingsState.isPermissible())) {
            setCanWriteSettings(this.mWriteSettingsState.isPermissible() ^ 1);
            refreshUi();
        }
        return true;
    }

    private void setCanWriteSettings(boolean newState) {
        logSpecialPermissionChange(newState, this.mPackageName);
        this.mAppOpsManager.setMode(23, this.mPackageInfo.applicationInfo.uid, this.mPackageName, newState ? 0 : 2);
    }

    /* Access modifiers changed, original: 0000 */
    public void logSpecialPermissionChange(boolean newState, String packageName) {
        int logCategory;
        if (newState) {
            logCategory = 774;
        } else {
            logCategory = 775;
        }
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), logCategory, packageName, new Pair[0]);
    }

    private boolean canWriteSettings(String pkgName) {
        if (this.mAppOpsManager.noteOpNoThrow(23, this.mPackageInfo.applicationInfo.uid, pkgName) == 0) {
            return true;
        }
        return false;
    }

    /* Access modifiers changed, original: protected */
    public boolean refreshUi() {
        this.mWriteSettingsState = this.mAppBridge.getWriteSettingsInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        this.mSwitchPref.setChecked(this.mWriteSettingsState.isPermissible());
        this.mSwitchPref.setEnabled(this.mWriteSettingsState.permissionDeclared);
        ResolveInfo resolveInfo = this.mPm.resolveActivityAsUser(this.mSettingsIntent, 128, this.mUserId);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    public int getMetricsCategory() {
        return 221;
    }

    public static CharSequence getSummary(Context context, AppEntry entry) {
        WriteSettingsState state;
        if (entry.extraInfo instanceof WriteSettingsState) {
            state = entry.extraInfo;
        } else if (entry.extraInfo instanceof PermissionState) {
            state = new WriteSettingsState((PermissionState) entry.extraInfo);
        } else {
            state = new AppStateWriteSettingsBridge(context, null, null).getWriteSettingsInfo(entry.info.packageName, entry.info.uid);
        }
        return getSummary(context, state);
    }

    public static CharSequence getSummary(Context context, WriteSettingsState writeSettingsState) {
        int i;
        if (writeSettingsState.isPermissible()) {
            i = R.string.app_permission_summary_allowed;
        } else {
            i = R.string.app_permission_summary_not_allowed;
        }
        return context.getString(i);
    }
}
