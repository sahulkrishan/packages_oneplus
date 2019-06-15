package com.android.settingslib.applications;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.IconDrawableFactory;
import com.android.settingslib.widget.CandidateInfo;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class DefaultAppInfo extends CandidateInfo {
    public final ComponentName componentName;
    private final Context mContext;
    protected final PackageManagerWrapper mPm;
    public final PackageItemInfo packageItemInfo;
    public final String summary;
    public final int userId;

    public DefaultAppInfo(Context context, PackageManagerWrapper pm, int uid, ComponentName cn) {
        this(context, pm, uid, cn, null, true);
    }

    public DefaultAppInfo(Context context, PackageManagerWrapper pm, PackageItemInfo info) {
        this(context, pm, info, null, true);
    }

    public DefaultAppInfo(Context context, PackageManagerWrapper pm, int uid, ComponentName cn, String summary, boolean enabled) {
        super(enabled);
        this.mContext = context;
        this.mPm = pm;
        this.packageItemInfo = null;
        this.userId = uid;
        this.componentName = cn;
        this.summary = summary;
    }

    public DefaultAppInfo(Context context, PackageManagerWrapper pm, PackageItemInfo info, String summary, boolean enabled) {
        super(enabled);
        this.mContext = context;
        this.mPm = pm;
        this.userId = UserHandle.myUserId();
        this.packageItemInfo = info;
        this.componentName = null;
        this.summary = summary;
    }

    public CharSequence loadLabel() {
        if (this.componentName != null) {
            try {
                ComponentInfo componentInfo = getComponentInfo();
                if (componentInfo != null) {
                    return componentInfo.loadLabel(this.mPm.getPackageManager());
                }
                return this.mPm.getApplicationInfoAsUser(this.componentName.getPackageName(), 0, this.userId).loadLabel(this.mPm.getPackageManager());
            } catch (NameNotFoundException e) {
                return null;
            }
        } else if (this.packageItemInfo != null) {
            return this.packageItemInfo.loadLabel(this.mPm.getPackageManager());
        } else {
            return null;
        }
    }

    public Drawable loadIcon() {
        IconDrawableFactory factory = IconDrawableFactory.newInstance(this.mContext);
        if (this.componentName != null) {
            try {
                ComponentInfo componentInfo = getComponentInfo();
                ApplicationInfo appInfo = this.mPm.getApplicationInfoAsUser(this.componentName.getPackageName(), 0, this.userId);
                if (componentInfo != null) {
                    return factory.getBadgedIcon(componentInfo, appInfo, this.userId);
                }
                return factory.getBadgedIcon(appInfo);
            } catch (NameNotFoundException e) {
                return null;
            }
        } else if (this.packageItemInfo == null) {
            return null;
        } else {
            try {
                return factory.getBadgedIcon(this.packageItemInfo, this.mPm.getApplicationInfoAsUser(this.packageItemInfo.packageName, 0, this.userId), this.userId);
            } catch (NameNotFoundException e2) {
                return null;
            }
        }
    }

    public String getKey() {
        if (this.componentName != null) {
            return this.componentName.flattenToString();
        }
        if (this.packageItemInfo != null) {
            return this.packageItemInfo.packageName;
        }
        return null;
    }

    private ComponentInfo getComponentInfo() {
        try {
            ComponentInfo componentInfo = AppGlobals.getPackageManager().getActivityInfo(this.componentName, 0, this.userId);
            if (componentInfo == null) {
                componentInfo = AppGlobals.getPackageManager().getServiceInfo(this.componentName, 0, this.userId);
            }
            return componentInfo;
        } catch (RemoteException e) {
            return null;
        }
    }
}
