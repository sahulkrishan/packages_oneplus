package com.android.settings.applications.appinfo;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.util.Pair;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.AppInfoWithHeader;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateOverlayBridge;
import com.android.settings.applications.AppStateOverlayBridge.OverlayState;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.oneplus.settings.SettingsBaseApplication;

public class DrawOverlayDetails extends AppInfoWithHeader implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final int[] APP_OPS_OP_CODE = new int[]{24};
    private static final String CAR_MODE_PACKAGE_NAME = "com.oneplus.carmode";
    private static final String DIALER_PACKAGE_NAME = "com.android.dialer";
    private static final String KEY_APP_OPS_SETTINGS_SWITCH = "app_ops_settings_switch";
    private static final String LOG_TAG = "DrawOverlayDetails";
    private static final String SPEECHASSIST_PACKAGE_NAME = "com.oneplus.speechassist";
    private AppOpsManager mAppOpsManager;
    private AppStateOverlayBridge mOverlayBridge;
    private OverlayState mOverlayState;
    private Intent mSettingsIntent;
    private SwitchPreference mSwitchPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getActivity();
        this.mOverlayBridge = new AppStateOverlayBridge(context, this.mState, null);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        addPreferencesFromResource(R.xml.draw_overlay_permissions_details);
        this.mSwitchPref = (SwitchPreference) findPreference(KEY_APP_OPS_SETTINGS_SWITCH);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
        this.mSettingsIntent = new Intent("android.intent.action.MAIN").setAction("android.settings.action.MANAGE_OVERLAY_PERMISSION");
    }

    public void onResume() {
        super.onResume();
        getActivity().getWindow().addPrivateFlags(524288);
    }

    public void onPause() {
        super.onPause();
        Window window = getActivity().getWindow();
        LayoutParams attrs = window.getAttributes();
        attrs.privateFlags &= -524289;
        window.setAttributes(attrs);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mOverlayBridge.release();
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mSwitchPref) {
            return false;
        }
        if (!(this.mOverlayState == null || ((Boolean) newValue).booleanValue() == this.mOverlayState.isPermissible())) {
            setCanDrawOverlay(this.mOverlayState.isPermissible() ^ 1);
            refreshUi();
        }
        return true;
    }

    private void setCanDrawOverlay(boolean newState) {
        logSpecialPermissionChange(newState, this.mPackageName);
        this.mAppOpsManager.setMode(24, this.mPackageInfo.applicationInfo.uid, this.mPackageName, newState ? 0 : 2);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void logSpecialPermissionChange(boolean newState, String packageName) {
        int logCategory;
        if (newState) {
            logCategory = 770;
        } else {
            logCategory = 771;
        }
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), logCategory, packageName, new Pair[0]);
    }

    /* Access modifiers changed, original: protected */
    public boolean refreshUi() {
        if (!(this.mPackageInfo != null || this.mAppEntry == null || this.mAppEntry.info == null)) {
            try {
                this.mPackageInfo = this.mPm.getPackageInfoAsUser(this.mAppEntry.info.packageName, 134222336, this.mUserId);
            } catch (NameNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Exception when retrieving package:");
                stringBuilder.append(this.mAppEntry.info.packageName);
                Log.e(str, stringBuilder.toString(), e);
            }
        }
        if (!(this.mPackageInfo == null || this.mPackageInfo.applicationInfo == null)) {
            this.mOverlayState = this.mOverlayBridge.getOverlayInfo(this.mPackageName, this.mPackageInfo.applicationInfo.uid);
        }
        if (this.mOverlayState != null) {
            this.mSwitchPref.setChecked(this.mOverlayState.isPermissible());
            SwitchPreference switchPreference = this.mSwitchPref;
            boolean z = this.mOverlayState.permissionDeclared && this.mOverlayState.controlEnabled;
            switchPreference.setEnabled(z);
        }
        ResolveInfo resolveInfo = this.mPm.resolveActivityAsUser(this.mSettingsIntent, 128, this.mUserId);
        boolean isCardMode = Secure.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "oneplus_carmode_switch", 1) == 1;
        if (CAR_MODE_PACKAGE_NAME.equals(this.mPackageName) || "com.android.dialer".equals(this.mPackageName) || (SPEECHASSIST_PACKAGE_NAME.equals(this.mPackageName) && isCardMode)) {
            this.mSwitchPref.setEnabled(false);
        }
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
        OverlayState state;
        if (entry.extraInfo instanceof OverlayState) {
            state = entry.extraInfo;
        } else if (entry.extraInfo instanceof PermissionState) {
            state = new OverlayState((PermissionState) entry.extraInfo);
        } else {
            state = new AppStateOverlayBridge(context, null, null).getOverlayInfo(entry.info.packageName, entry.info.uid);
        }
        return getSummary(context, state);
    }

    public static CharSequence getSummary(Context context, OverlayState overlayState) {
        return context.getString(overlayState.isPermissible() ? R.string.app_permission_summary_allowed : R.string.app_permission_summary_not_allowed);
    }
}
