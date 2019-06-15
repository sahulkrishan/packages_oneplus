package com.android.settings.applications.appinfo;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.AppInfoWithHeader;
import com.android.settings.overlay.FeatureFactory;

public class PictureInPictureDetails extends AppInfoWithHeader implements OnPreferenceChangeListener {
    private static final String KEY_APP_OPS_SETTINGS_SWITCH = "app_ops_settings_switch";
    private static final String LOG_TAG = "PictureInPictureDetails";
    private SwitchPreference mSwitchPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.picture_in_picture_permissions_details);
        this.mSwitchPref = (SwitchPreference) findPreference(KEY_APP_OPS_SETTINGS_SWITCH);
        this.mSwitchPref.setTitle((int) R.string.picture_in_picture_app_detail_switch);
        this.mSwitchPref.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference != this.mSwitchPref) {
            return false;
        }
        logSpecialPermissionChange(((Boolean) newValue).booleanValue(), this.mPackageName);
        setEnterPipStateForPackage(getActivity(), this.mPackageInfo.applicationInfo.uid, this.mPackageName, ((Boolean) newValue).booleanValue());
        return true;
    }

    /* Access modifiers changed, original: protected */
    public boolean refreshUi() {
        this.mSwitchPref.setChecked(getEnterPipStateForPackage(getActivity(), this.mPackageInfo.applicationInfo.uid, this.mPackageName));
        return true;
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    public int getMetricsCategory() {
        return 812;
    }

    static void setEnterPipStateForPackage(Context context, int uid, String packageName, boolean value) {
        ((AppOpsManager) context.getSystemService(AppOpsManager.class)).setMode(74, uid, packageName, value ? 0 : 2);
    }

    static boolean getEnterPipStateForPackage(Context context, int uid, String packageName) {
        return ((AppOpsManager) context.getSystemService(AppOpsManager.class)).checkOpNoThrow(74, uid, packageName) == 0;
    }

    public static int getPreferenceSummary(Context context, int uid, String packageName) {
        if (getEnterPipStateForPackage(context, uid, packageName)) {
            return R.string.app_permission_summary_allowed;
        }
        return R.string.app_permission_summary_not_allowed;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void logSpecialPermissionChange(boolean newState, String packageName) {
        int logCategory;
        if (newState) {
            logCategory = 813;
        } else {
            logCategory = 814;
        }
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), logCategory, packageName, new Pair[0]);
    }
}
