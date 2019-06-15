package com.android.settings.applications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.List;

public abstract class InstalledAppCounter extends AppCounter {
    public static final int IGNORE_INSTALL_REASON = -1;
    private final int mInstallReason;

    public InstalledAppCounter(Context context, int installReason, PackageManagerWrapper packageManager) {
        super(context, packageManager);
        this.mInstallReason = installReason;
    }

    /* Access modifiers changed, original: protected */
    public boolean includeInCount(ApplicationInfo info) {
        return includeInCount(this.mInstallReason, this.mPm, info);
    }

    public static boolean includeInCount(int installReason, PackageManagerWrapper pm, ApplicationInfo info) {
        int userId = UserHandle.getUserId(info.uid);
        boolean z = false;
        if (installReason != -1 && pm.getInstallReason(info.packageName, new UserHandle(userId)) != installReason) {
            return false;
        }
        if ((info.flags & 128) != 0 || (info.flags & 1) == 0) {
            return true;
        }
        List<ResolveInfo> intents = pm.queryIntentActivitiesAsUser(new Intent("android.intent.action.MAIN", null).addCategory("android.intent.category.LAUNCHER").setPackage(info.packageName), 786944, userId);
        if (!(intents == null || intents.size() == 0)) {
            z = true;
        }
        return z;
    }
}
