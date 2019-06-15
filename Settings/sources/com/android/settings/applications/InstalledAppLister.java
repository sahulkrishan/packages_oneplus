package com.android.settings.applications;

import android.content.pm.ApplicationInfo;
import android.os.UserManager;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public abstract class InstalledAppLister extends AppLister {
    public InstalledAppLister(PackageManagerWrapper packageManager, UserManager userManager) {
        super(packageManager, userManager);
    }

    /* Access modifiers changed, original: protected */
    public boolean includeInCount(ApplicationInfo info) {
        return InstalledAppCounter.includeInCount(1, this.mPm, info);
    }
}
