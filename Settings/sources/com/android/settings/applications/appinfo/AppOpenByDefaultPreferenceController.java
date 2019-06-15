package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.os.ServiceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.AppLaunchSettings;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class AppOpenByDefaultPreferenceController extends AppInfoPreferenceControllerBase {
    private PackageManager mPackageManager;
    private IUsbManager mUsbManager = Stub.asInterface(ServiceManager.getService("usb"));

    public AppOpenByDefaultPreferenceController(Context context, String key) {
        super(context, key);
        this.mPackageManager = context.getPackageManager();
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        AppEntry appEntry = this.mParent.getAppEntry();
        if (appEntry == null || appEntry.info == null) {
            this.mPreference.setEnabled(false);
        } else if ((appEntry.info.flags & 8388608) == 0 || !appEntry.info.enabled) {
            this.mPreference.setEnabled(false);
        }
    }

    public void updateState(Preference preference) {
        PackageInfo packageInfo = this.mParent.getPackageInfo();
        if (packageInfo == null || AppUtils.isInstant(packageInfo.applicationInfo)) {
            preference.setVisible(false);
            return;
        }
        preference.setVisible(true);
        preference.setSummary(AppUtils.getLaunchByDefaultSummary(this.mParent.getAppEntry(), this.mUsbManager, this.mPackageManager, this.mContext));
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return AppLaunchSettings.class;
    }
}
