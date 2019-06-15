package com.android.setupwizardlib.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.AnyRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import java.util.List;

public class Partner {
    private static final String ACTION_PARTNER_CUSTOMIZATION = "com.android.setupwizard.action.PARTNER_CUSTOMIZATION";
    private static final String TAG = "(SUW) Partner";
    private static Partner sPartner;
    private static boolean sSearched = false;
    private final String mPackageName;
    private final Resources mResources;

    public static class ResourceEntry {
        public int id;
        public boolean isOverlay;
        public Resources resources;

        ResourceEntry(Resources resources, int id, boolean isOverlay) {
            this.resources = resources;
            this.id = id;
            this.isOverlay = isOverlay;
        }
    }

    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        ResourceEntry entry = getResourceEntry(context, id);
        return entry.resources.getDrawable(entry.id);
    }

    public static String getString(Context context, @StringRes int id) {
        ResourceEntry entry = getResourceEntry(context, id);
        return entry.resources.getString(entry.id);
    }

    public static int getColor(Context context, @ColorRes int id) {
        ResourceEntry resourceEntry = getResourceEntry(context, id);
        return resourceEntry.resources.getColor(resourceEntry.id);
    }

    public static CharSequence getText(Context context, @StringRes int id) {
        ResourceEntry entry = getResourceEntry(context, id);
        return entry.resources.getText(entry.id);
    }

    public static ResourceEntry getResourceEntry(Context context, @AnyRes int id) {
        Partner partner = get(context);
        if (partner != null) {
            Resources ourResources = context.getResources();
            int partnerId = partner.getIdentifier(ourResources.getResourceEntryName(id), ourResources.getResourceTypeName(id));
            if (partnerId != 0) {
                return new ResourceEntry(partner.mResources, partnerId, true);
            }
        }
        return new ResourceEntry(context.getResources(), id, false);
    }

    public static synchronized Partner get(Context context) {
        Partner partner;
        synchronized (Partner.class) {
            if (!sSearched) {
                PackageManager pm = context.getPackageManager();
                Intent intent = new Intent(ACTION_PARTNER_CUSTOMIZATION);
                List<ResolveInfo> receivers;
                if (VERSION.SDK_INT >= 24) {
                    receivers = pm.queryBroadcastReceivers(intent, 1835008);
                } else {
                    receivers = pm.queryBroadcastReceivers(intent, 0);
                }
                for (ResolveInfo info : receivers) {
                    if (info.activityInfo != null) {
                        ApplicationInfo appInfo = info.activityInfo.applicationInfo;
                        if ((appInfo.flags & 1) != 0) {
                            try {
                                sPartner = new Partner(appInfo.packageName, pm.getResourcesForApplication(appInfo));
                                break;
                            } catch (NameNotFoundException e) {
                                String str = TAG;
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Failed to find resources for ");
                                stringBuilder.append(appInfo.packageName);
                                Log.w(str, stringBuilder.toString());
                            }
                        }
                    }
                }
                sSearched = true;
            }
            partner = sPartner;
        }
        return partner;
    }

    @VisibleForTesting
    public static synchronized void resetForTesting() {
        synchronized (Partner.class) {
            sSearched = false;
            sPartner = null;
        }
    }

    private Partner(String packageName, Resources res) {
        this.mPackageName = packageName;
        this.mResources = res;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public Resources getResources() {
        return this.mResources;
    }

    public int getIdentifier(String name, String defType) {
        return this.mResources.getIdentifier(name, defType, this.mPackageName);
    }
}
