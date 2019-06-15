package com.android.settings.development;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class SelectDebugAppPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin, OnActivityResultListener {
    private static final String DEBUG_APP_KEY = "debug_app";
    private final DevelopmentSettingsDashboardFragment mFragment;
    private final PackageManagerWrapper mPackageManager = new PackageManagerWrapper(this.mContext.getPackageManager());

    public SelectDebugAppPreferenceController(Context context, DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        this.mFragment = fragment;
    }

    public String getPreferenceKey() {
        return DEBUG_APP_KEY;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!DEBUG_APP_KEY.equals(preference.getKey())) {
            return false;
        }
        Intent intent = getActivityStartIntent();
        intent.putExtra(AppPicker.EXTRA_DEBUGGABLE, true);
        this.mFragment.startActivityForResult(intent, 1);
        return true;
    }

    public void updateState(Preference preference) {
        updatePreferenceSummary();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || resultCode != -1) {
            return false;
        }
        Global.putString(this.mContext.getContentResolver(), DEBUG_APP_KEY, data.getAction());
        updatePreferenceSummary();
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        this.mPreference.setSummary(this.mContext.getResources().getString(R.string.debug_app_not_set));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Intent getActivityStartIntent() {
        return new Intent(this.mContext, AppPicker.class);
    }

    private void updatePreferenceSummary() {
        String debugApp = Global.getString(this.mContext.getContentResolver(), DEBUG_APP_KEY);
        if (debugApp == null || debugApp.length() <= 0) {
            this.mPreference.setSummary(this.mContext.getResources().getString(R.string.debug_app_not_set));
            return;
        }
        this.mPreference.setSummary(this.mContext.getResources().getString(R.string.debug_app_set, new Object[]{getAppLabel(debugApp)}));
    }

    private String getAppLabel(String debugApp) {
        try {
            CharSequence lab = this.mPackageManager.getApplicationLabel(this.mPackageManager.getApplicationInfo(debugApp, 512));
            return lab != null ? lab.toString() : debugApp;
        } catch (NameNotFoundException e) {
            return debugApp;
        }
    }
}
