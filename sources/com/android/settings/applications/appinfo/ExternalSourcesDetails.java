package com.android.settings.applications.appinfo;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.Settings.ManageAppExternalSourcesActivity;
import com.android.settings.applications.AppInfoWithHeader;
import com.android.settings.applications.AppStateInstallAppsBridge;
import com.android.settings.applications.AppStateInstallAppsBridge.InstallAppsState;
import com.android.settingslib.RestrictedSwitchPreference;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class ExternalSourcesDetails extends AppInfoWithHeader implements OnPreferenceChangeListener {
    private static final String KEY_EXTERNAL_SOURCE_SWITCH = "external_sources_settings_switch";
    private AppStateInstallAppsBridge mAppBridge;
    private AppOpsManager mAppOpsManager;
    private InstallAppsState mInstallAppsState;
    private RestrictedSwitchPreference mSwitchPref;
    private UserManager mUserManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mAppBridge = new AppStateInstallAppsBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mUserManager = UserManager.get(context);
        addPreferencesFromResource(R.xml.external_sources_details);
        this.mSwitchPref = (RestrictedSwitchPreference) findPreference(KEY_EXTERNAL_SOURCE_SWITCH);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean checked = ((Boolean) newValue).booleanValue();
        int i = 0;
        if (preference != this.mSwitchPref) {
            return false;
        }
        if (!(this.mInstallAppsState == null || checked == this.mInstallAppsState.canInstallApps())) {
            if (ManageAppExternalSourcesActivity.class.getName().equals(getIntent().getComponent().getClassName())) {
                if (checked) {
                    i = -1;
                }
                setResult(i);
            }
            setCanInstallApps(checked);
            refreshUi();
        }
        return true;
    }

    public static CharSequence getPreferenceSummary(Context context, AppEntry entry) {
        int userRestrictionSource = UserManager.get(context).getUserRestrictionSource("no_install_unknown_sources", UserHandle.getUserHandleForUid(entry.info.uid));
        if (userRestrictionSource != 4) {
            switch (userRestrictionSource) {
                case 1:
                    return context.getString(R.string.disabled);
                case 2:
                    break;
                default:
                    int i;
                    if (new AppStateInstallAppsBridge(context, null, null).createInstallAppsStateFor(entry.info.packageName, entry.info.uid).canInstallApps()) {
                        i = R.string.app_permission_summary_allowed;
                    } else {
                        i = R.string.app_permission_summary_not_allowed;
                    }
                    return context.getString(i);
            }
        }
        return context.getString(R.string.disabled_by_admin);
    }

    private void setCanInstallApps(boolean newState) {
        int i = 2;
        if (UserHandle.getUserId(this.mPackageInfo.applicationInfo.uid) == 0) {
            this.mAppOpsManager.setMode(73, UserHandle.getUid(999, UserHandle.getAppId(this.mPackageInfo.applicationInfo.uid)), this.mPackageName, newState ? 0 : 2);
        }
        if (UserHandle.getUserId(this.mPackageInfo.applicationInfo.uid) == 999) {
            this.mAppOpsManager.setMode(73, UserHandle.getUid(0, UserHandle.getAppId(this.mPackageInfo.applicationInfo.uid)), this.mPackageName, newState ? 0 : 2);
        }
        AppOpsManager appOpsManager = this.mAppOpsManager;
        int i2 = this.mPackageInfo.applicationInfo.uid;
        String str = this.mPackageName;
        if (newState) {
            i = 0;
        }
        appOpsManager.setMode(73, i2, str, i);
    }

    /* Access modifiers changed, original: protected */
    public boolean refreshUi() {
        if (this.mPackageInfo == null || this.mPackageInfo.applicationInfo == null) {
            return false;
        }
        if (this.mUserManager.hasBaseUserRestriction("no_install_unknown_sources", UserHandle.of(UserHandle.myUserId()))) {
            this.mSwitchPref.setChecked(false);
            this.mSwitchPref.setSummary((int) R.string.disabled);
            this.mSwitchPref.setEnabled(false);
            return true;
        }
        this.mSwitchPref.checkRestrictionAndSetDisabled("no_install_unknown_sources");
        if (this.mSwitchPref.isDisabledByAdmin()) {
            return true;
        }
        this.mInstallAppsState = this.mAppBridge.createInstallAppsStateFor(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        if (this.mInstallAppsState.isPotentialAppSource()) {
            this.mSwitchPref.setChecked(this.mInstallAppsState.canInstallApps());
            return true;
        }
        this.mSwitchPref.setEnabled(false);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    public int getMetricsCategory() {
        return 808;
    }
}
