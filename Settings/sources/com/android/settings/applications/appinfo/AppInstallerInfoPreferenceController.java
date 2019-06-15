package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.applications.AppStoreUtil;
import com.android.settingslib.applications.AppUtils;

public class AppInstallerInfoPreferenceController extends AppInfoPreferenceControllerBase {
    private CharSequence mInstallerLabel;
    private String mInstallerPackage;
    private String mPackageName;

    public AppInstallerInfoPreferenceController(Context context, String key) {
        super(context, key);
    }

    public int getAvailabilityStatus() {
        int i = 3;
        if (Utils.isManagedProfile(UserManager.get(this.mContext))) {
            return 3;
        }
        if (this.mInstallerLabel != null) {
            i = 0;
        }
        return i;
    }

    public void updateState(Preference preference) {
        int detailsStringId;
        if (AppUtils.isInstant(this.mParent.getPackageInfo().applicationInfo)) {
            detailsStringId = R.string.instant_app_details_summary;
        } else {
            detailsStringId = R.string.app_install_details_summary;
        }
        preference.setSummary(this.mContext.getString(detailsStringId, new Object[]{this.mInstallerLabel}));
        Intent intent = AppStoreUtil.getAppStoreLink(this.mContext, this.mInstallerPackage, this.mPackageName);
        if (intent != null) {
            preference.setIntent(intent);
        } else {
            preference.setEnabled(false);
        }
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
        this.mInstallerPackage = AppStoreUtil.getInstallerPackageName(this.mContext, this.mPackageName);
        this.mInstallerLabel = Utils.getApplicationLabel(this.mContext, this.mInstallerPackage);
    }
}
