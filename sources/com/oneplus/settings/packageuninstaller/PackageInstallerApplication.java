package com.oneplus.settings.packageuninstaller;

import android.app.Application;
import android.content.pm.PackageItemInfo;

public class PackageInstallerApplication extends Application {
    public void onCreate() {
        super.onCreate();
        PackageItemInfo.setForceSafeLabels(true);
    }
}
