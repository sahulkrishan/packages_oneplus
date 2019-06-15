package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;

public class PictureInPictureDetailPreferenceController extends AppInfoPreferenceControllerBase {
    private static final String TAG = "PicInPicDetailControl";
    private final PackageManager mPackageManager;
    private String mPackageName;

    public PictureInPictureDetailPreferenceController(Context context, String key) {
        super(context, key);
        this.mPackageManager = context.getPackageManager();
    }

    public int getAvailabilityStatus() {
        return hasPictureInPictureActivites() ? 0 : 3;
    }

    public void updateState(Preference preference) {
        preference.setSummary(getPreferenceSummary());
    }

    /* Access modifiers changed, original: protected */
    public Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return PictureInPictureDetails.class;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean hasPictureInPictureActivites() {
        PackageInfo packageInfoWithActivities = null;
        try {
            packageInfoWithActivities = this.mPackageManager.getPackageInfoAsUser(this.mPackageName, 1, UserHandle.myUserId());
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Exception while retrieving the package info of ");
            stringBuilder.append(this.mPackageName);
            Log.e(str, stringBuilder.toString(), e);
        }
        if (packageInfoWithActivities == null || !PictureInPictureSettings.checkPackageHasPictureInPictureActivities(packageInfoWithActivities.packageName, packageInfoWithActivities.activities)) {
            return false;
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getPreferenceSummary() {
        return PictureInPictureDetails.getPreferenceSummary(this.mContext, this.mParent.getPackageInfo().applicationInfo.uid, this.mPackageName);
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }
}
