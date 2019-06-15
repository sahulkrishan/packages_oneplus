package android.support.v17.leanback.system;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Build.VERSION;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.widget.ShadowOverlayContainer;

public class Settings {
    private static final String ACTION_PARTNER_CUSTOMIZATION = "android.support.v17.leanback.action.PARTNER_CUSTOMIZATION";
    private static final boolean DEBUG = false;
    public static final String OUTLINE_CLIPPING_DISABLED = "OUTLINE_CLIPPING_DISABLED";
    public static final String PREFER_STATIC_SHADOWS = "PREFER_STATIC_SHADOWS";
    private static final String TAG = "Settings";
    private static Settings sInstance;
    private boolean mOutlineClippingDisabled;
    private boolean mPreferStaticShadows;

    static class Customizations {
        String mPackageName;
        Resources mResources;

        public Customizations(Resources resources, String packageName) {
            this.mResources = resources;
            this.mPackageName = packageName;
        }

        public boolean getBoolean(String resourceName, boolean defaultValue) {
            int resId = this.mResources.getIdentifier(resourceName, "bool", this.mPackageName);
            return resId > 0 ? this.mResources.getBoolean(resId) : defaultValue;
        }
    }

    public static Settings getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Settings(context);
        }
        return sInstance;
    }

    private Settings(Context context) {
        generateSetting(getCustomizations(context));
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public boolean preferStaticShadows() {
        return this.mPreferStaticShadows;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public boolean isOutlineClippingDisabled() {
        return this.mOutlineClippingDisabled;
    }

    public boolean getBoolean(String key) {
        return getOrSetBoolean(key, false, false);
    }

    public void setBoolean(String key, boolean value) {
        getOrSetBoolean(key, true, value);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean getOrSetBoolean(String key, boolean set, boolean value) {
        boolean z;
        if (key.compareTo(PREFER_STATIC_SHADOWS) == 0) {
            if (set) {
                this.mPreferStaticShadows = value;
                z = value;
            } else {
                z = this.mPreferStaticShadows;
            }
            return z;
        } else if (key.compareTo(OUTLINE_CLIPPING_DISABLED) == 0) {
            if (set) {
                this.mOutlineClippingDisabled = value;
                z = value;
            } else {
                z = this.mOutlineClippingDisabled;
            }
            return z;
        } else {
            throw new IllegalArgumentException("Invalid key");
        }
    }

    private void generateSetting(Customizations customizations) {
        if (ShadowOverlayContainer.supportsDynamicShadow()) {
            this.mPreferStaticShadows = false;
            if (customizations != null) {
                this.mPreferStaticShadows = customizations.getBoolean("leanback_prefer_static_shadows", this.mPreferStaticShadows);
            }
        } else {
            this.mPreferStaticShadows = true;
        }
        if (VERSION.SDK_INT >= 21) {
            this.mOutlineClippingDisabled = false;
            if (customizations != null) {
                this.mOutlineClippingDisabled = customizations.getBoolean("leanback_outline_clipping_disabled", this.mOutlineClippingDisabled);
                return;
            }
            return;
        }
        this.mOutlineClippingDisabled = true;
    }

    private Customizations getCustomizations(Context context) {
        PackageManager pm = context.getPackageManager();
        Resources resources = null;
        String packageName = null;
        for (ResolveInfo info : pm.queryBroadcastReceivers(new Intent(ACTION_PARTNER_CUSTOMIZATION), 0)) {
            packageName = info.activityInfo.packageName;
            if (packageName != null && isSystemApp(info)) {
                try {
                    resources = pm.getResourcesForApplication(packageName);
                } catch (NameNotFoundException e) {
                }
            }
            if (resources != null) {
                break;
            }
        }
        return resources == null ? null : new Customizations(resources, packageName);
    }

    private static boolean isSystemApp(ResolveInfo info) {
        return (info.activityInfo == null || (info.activityInfo.applicationInfo.flags & 1) == 0) ? false : true;
    }
}
