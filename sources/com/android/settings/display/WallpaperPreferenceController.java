package com.android.settings.display;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;

public class WallpaperPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    public static final String KEY_WALLPAPER = "wallpaper";
    private static final String TAG = "WallpaperPrefController";
    private final String mWallpaperClass = this.mContext.getString(R.string.config_wallpaper_picker_class);
    private final String mWallpaperPackage = this.mContext.getString(R.string.config_wallpaper_picker_package);

    public WallpaperPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        boolean z = false;
        if (TextUtils.isEmpty(this.mWallpaperPackage) || TextUtils.isEmpty(this.mWallpaperClass)) {
            Log.e(TAG, "No Wallpaper picker specified!");
            return false;
        }
        ComponentName componentName = new ComponentName(this.mWallpaperPackage, this.mWallpaperClass);
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent();
        intent.setComponent(componentName);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        if (!(resolveInfos == null || resolveInfos.size() == 0)) {
            z = true;
        }
        return z;
    }

    public String getPreferenceKey() {
        return KEY_WALLPAPER;
    }

    public void updateState(Preference preference) {
        disablePreferenceIfManaged((RestrictedPreference) preference);
    }

    private void disablePreferenceIfManaged(RestrictedPreference pref) {
        String restriction = "no_set_wallpaper";
        if (pref != null) {
            pref.setDisabledByAdmin(null);
            if (RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_set_wallpaper", UserHandle.myUserId())) {
                pref.setEnabled(false);
            } else {
                pref.checkRestrictionAndSetDisabled("no_set_wallpaper");
            }
        }
    }
}
