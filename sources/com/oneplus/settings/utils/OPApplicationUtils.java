package com.oneplus.settings.utils;

import com.android.settings.R;
import com.oneplus.settings.SettingsBaseApplication;
import java.util.Arrays;
import java.util.List;

public class OPApplicationUtils {
    public static boolean isOnePlusH2UninstallationApp(String pkgName) {
        List<String> appLists = Arrays.asList(SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_uninstallation_app_list_H2));
        if (appLists == null) {
            return false;
        }
        return appLists.contains(pkgName);
    }

    public static boolean isOnePlusO2UninstallationApp(String pkgName) {
        List<String> appLists = Arrays.asList(SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_uninstallation_app_list_O2));
        if (appLists == null) {
            return false;
        }
        return appLists.contains(pkgName);
    }
}
