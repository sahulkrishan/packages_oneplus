package com.android.settings.applications.defaultapps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.GearPreference;
import com.android.settingslib.TwoTargetPreference;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public abstract class DefaultAppPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String TAG = "DefaultAppPrefControl";
    protected final PackageManagerWrapper mPackageManager;
    protected int mUserId = UserHandle.myUserId();
    protected final UserManager mUserManager;

    public abstract DefaultAppInfo getDefaultAppInfo();

    public DefaultAppPreferenceController(Context context) {
        super(context);
        this.mPackageManager = new PackageManagerWrapper(context.getPackageManager());
        this.mUserManager = (UserManager) context.getSystemService("user");
    }

    public void updateState(Preference preference) {
        DefaultAppInfo app = getDefaultAppInfo();
        CharSequence defaultAppLabel = getDefaultAppLabel();
        if (preference instanceof TwoTargetPreference) {
            ((TwoTargetPreference) preference).setIconSize(1);
        }
        if (TextUtils.isEmpty(defaultAppLabel)) {
            Log.d(TAG, "No default app");
            preference.setSummary((int) R.string.app_list_preference_none);
            preference.setIcon(null);
        } else {
            preference.setSummary(defaultAppLabel);
            Utils.setSafeIcon(preference, getDefaultAppIcon());
        }
        mayUpdateGearIcon(app, preference);
    }

    private void mayUpdateGearIcon(DefaultAppInfo app, Preference preference) {
        if (preference instanceof GearPreference) {
            Intent settingIntent = getSettingIntent(app);
            if (settingIntent != null) {
                ComponentName mComponentName = settingIntent.getComponent();
                if (mComponentName != null) {
                    if (this.mPackageManager.getPackageManager().getComponentEnabledSetting(mComponentName) == 1) {
                        ((GearPreference) preference).setOnGearClickListener(new -$$Lambda$DefaultAppPreferenceController$P93yGe3NhKzPqeqQwHkMaXpVB1M(this, settingIntent));
                    } else {
                        ((GearPreference) preference).setOnGearClickListener(null);
                    }
                } else {
                    return;
                }
            }
            ((GearPreference) preference).setOnGearClickListener(null);
        }
    }

    /* Access modifiers changed, original: protected */
    public Intent getSettingIntent(DefaultAppInfo info) {
        return null;
    }

    public Drawable getDefaultAppIcon() {
        if (!isAvailable()) {
            return null;
        }
        DefaultAppInfo app = getDefaultAppInfo();
        if (app != null) {
            return app.loadIcon();
        }
        return null;
    }

    public CharSequence getDefaultAppLabel() {
        if (!isAvailable()) {
            return null;
        }
        DefaultAppInfo app = getDefaultAppInfo();
        if (app != null) {
            return app.loadLabel();
        }
        return null;
    }
}
