package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.applications.AppStateInstallAppsBridge;

public class ExternalSourceDetailPreferenceController extends AppInfoPreferenceControllerBase {
    private String mPackageName;

    public ExternalSourceDetailPreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        int i = 3;
        if (Utils.isManagedProfile(UserManager.get(this.mContext))) {
            return 3;
        }
        if (isPotentialAppSource()) {
            i = 0;
        }
        return i;
    }

    public void updateState(Preference preference) {
        preference.setSummary(getPreferenceSummary());
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return ExternalSourcesDetails.class;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public CharSequence getPreferenceSummary() {
        return ExternalSourcesDetails.getPreferenceSummary(this.mContext, this.mParent.getAppEntry());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isPotentialAppSource() {
        PackageInfo packageInfo = this.mParent.getPackageInfo();
        if (packageInfo == null) {
            return false;
        }
        return new AppStateInstallAppsBridge(this.mContext, null, null).createInstallAppsStateFor(this.mPackageName, packageInfo.applicationInfo.uid).isPotentialAppSource();
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }
}
