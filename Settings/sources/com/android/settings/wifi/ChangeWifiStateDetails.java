package com.android.settings.wifi;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.applications.AppInfoWithHeader;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.wifi.AppStateChangeWifiStateBridge.WifiSettingsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class ChangeWifiStateDetails extends AppInfoWithHeader implements OnPreferenceChangeListener {
    private static final String KEY_APP_OPS_SETTINGS_SWITCH = "app_ops_settings_switch";
    private static final String LOG_TAG = "ChangeWifiStateDetails";
    private AppStateChangeWifiStateBridge mAppBridge;
    private AppOpsManager mAppOpsManager;
    private SwitchPreference mSwitchPref;
    private WifiSettingsState mWifiSettingsState;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mAppBridge = new AppStateChangeWifiStateBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        addPreferencesFromResource(R.xml.change_wifi_state_details);
        this.mSwitchPref = (SwitchPreference) findPreference(KEY_APP_OPS_SETTINGS_SWITCH);
        this.mSwitchPref.setTitle((int) R.string.change_wifi_state_app_detail_switch);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    public int getMetricsCategory() {
        return 338;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mSwitchPref) {
            return false;
        }
        if (!(this.mWifiSettingsState == null || ((Boolean) newValue).booleanValue() == this.mWifiSettingsState.isPermissible())) {
            setCanChangeWifiState(this.mWifiSettingsState.isPermissible() ^ 1);
            refreshUi();
        }
        return true;
    }

    private void setCanChangeWifiState(boolean newState) {
        logSpecialPermissionChange(newState, this.mPackageName);
        this.mAppOpsManager.setMode(65, this.mPackageInfo.applicationInfo.uid, this.mPackageName, newState ? 0 : 1);
    }

    /* Access modifiers changed, original: protected */
    public void logSpecialPermissionChange(boolean newState, String packageName) {
        int logCategory;
        if (newState) {
            logCategory = 774;
        } else {
            logCategory = 775;
        }
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), logCategory, packageName, new Pair[0]);
    }

    /* Access modifiers changed, original: protected */
    public boolean refreshUi() {
        if (this.mPackageInfo == null || this.mPackageInfo.applicationInfo == null) {
            return false;
        }
        this.mWifiSettingsState = this.mAppBridge.getWifiSettingsInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        this.mSwitchPref.setChecked(this.mWifiSettingsState.isPermissible());
        this.mSwitchPref.setEnabled(this.mWifiSettingsState.permissionDeclared);
        return true;
    }

    public static CharSequence getSummary(Context context, AppEntry entry) {
        WifiSettingsState state;
        if (entry.extraInfo instanceof WifiSettingsState) {
            state = entry.extraInfo;
        } else if (entry.extraInfo instanceof PermissionState) {
            state = new WifiSettingsState((PermissionState) entry.extraInfo);
        } else {
            state = new AppStateChangeWifiStateBridge(context, null, null).getWifiSettingsInfo(entry.info.packageName, entry.info.uid);
        }
        return getSummary(context, state);
    }

    public static CharSequence getSummary(Context context, WifiSettingsState wifiSettingsState) {
        int i;
        if (wifiSettingsState.isPermissible()) {
            i = R.string.app_permission_summary_allowed;
        } else {
            i = R.string.app_permission_summary_not_allowed;
        }
        return context.getString(i);
    }
}
