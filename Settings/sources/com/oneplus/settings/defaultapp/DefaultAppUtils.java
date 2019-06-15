package com.oneplus.settings.defaultapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.android.settings.R;
import com.oneplus.settings.defaultapp.apptype.DefaultAppTypeCamera;
import com.oneplus.settings.defaultapp.apptype.DefaultAppTypeEmail;
import com.oneplus.settings.defaultapp.apptype.DefaultAppTypeGallery;
import com.oneplus.settings.defaultapp.apptype.DefaultAppTypeInfo;
import com.oneplus.settings.defaultapp.apptype.DefaultAppTypeMusic;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public class DefaultAppUtils {
    public static final String TAG = "DefaultAppUtils";

    public static String getKeyTypeString(int type) {
        return DefaultAppConstants.DEFAULTAPP_VALUE_LIST_KEY[type];
    }

    public static int getKeyTypeInt(String type) {
        String[] mKey = DefaultAppConstants.DEFAULTAPP_VALUE_LIST_KEY;
        for (int i = 0; i < mKey.length; i++) {
            if (mKey[i].equals(type)) {
                return i;
            }
        }
        return 0;
    }

    public static DefaultAppTypeInfo create(Context ctx, String type) {
        String[] mKey = DefaultAppConstants.DEFAULTAPP_VALUE_LIST_KEY;
        int pos = 0;
        for (int i = 0; i < mKey.length; i++) {
            if (mKey[i].equals(type)) {
                pos = i;
                break;
            }
        }
        return create(ctx, pos);
    }

    public static DefaultAppTypeInfo create(Context ctx, int type) {
        switch (type) {
            case 0:
                return new DefaultAppTypeCamera();
            case 1:
                return new DefaultAppTypeGallery();
            case 2:
                return new DefaultAppTypeMusic();
            case 3:
                return new DefaultAppTypeEmail();
            default:
                return null;
        }
    }

    public static String[] getDefaultAppValueList() {
        if (OPUtils.isO2()) {
            return DefaultAppConstants.DEFAULTAPP_VALUE_LIST_O2OS;
        }
        return DefaultAppConstants.DEFAULTAPP_VALUE_LIST_H2OS;
    }

    public static String getSystemDefaultPackageName(Context ctx, String appType) {
        String[] mKey = DefaultAppConstants.DEFAULTAPP_VALUE_LIST_KEY;
        String[] mValue = getDefaultAppValueList();
        for (int i = 0; i < mKey.length; i++) {
            if (mKey[i].equals(appType)) {
                return mValue[i];
            }
        }
        return null;
    }

    public static String getDefaultAppName(Context context, String appType) {
        String packageName = DataHelper.getDefaultAppPackageName(context, appType);
        String systemDefaultPackageName = getSystemDefaultPackageName(context, appType);
        boolean isAppExist = isAppExist(context, packageName);
        if (TextUtils.isEmpty(packageName) || !isAppExist) {
            return null;
        }
        if (packageName.equals(systemDefaultPackageName)) {
            return context.getString(R.string.system_default_app);
        }
        return queryAppName(context, packageName);
    }

    public static String getDefaultAppPackageName(Context context, String appType) {
        String packageName = DataHelper.getDefaultAppPackageName(context, appType);
        boolean isAppExist = isAppExist(context, packageName);
        if (TextUtils.isEmpty(packageName) || !isAppExist) {
            return null;
        }
        return packageName;
    }

    public static boolean isAppExist(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName, 128);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String queryAppName(Context context, String packageName) {
        String appName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            return String.valueOf(packageManager.getApplicationInfo(packageName, 128).loadLabel(packageManager));
        } catch (Exception e) {
            e.printStackTrace();
            return appName;
        }
    }

    public static void resetDefaultApp(Context context, String appType) {
        DefaultAppLogic logic = DefaultAppLogic.getInstance(context);
        List appInfoList = logic.getAppInfoList(appType);
        List packageNameList = logic.getAppPackageNameList(appType, appInfoList);
        logic.setDefaultAppPosition(appType, appInfoList, packageNameList, logic.getDefaultAppPosition(appType, packageNameList, getSystemDefaultPackageName(context, appType)));
    }

    public static void updateDefaultApp(Context ctx) {
        DefaultAppLogic logic = DefaultAppLogic.getInstance(ctx);
        for (String appType : DefaultAppConstants.DEFAULTAPP_VALUE_LIST_KEY) {
            String packageName = DataHelper.getDefaultAppPackageName(ctx, appType);
            List appInfoList = logic.getAppInfoList(appType);
            List packageNameList = logic.getAppPackageNameList(appType, appInfoList);
            if (packageName != null && packageNameList.contains(packageName)) {
                logic.setDefaultAppPosition(appType, appInfoList, packageNameList, logic.getDefaultAppPosition(appType, packageNameList, packageName));
            }
        }
    }

    public static void clearDefaultApp(Context ctx, String packageName) {
        PackageManager packageManager = ctx.getPackageManager();
        boolean needClear = false;
        for (String appType : DefaultAppConstants.DEFAULTAPP_VALUE_LIST_KEY) {
            String p = DataHelper.getDefaultAppPackageName(ctx, appType);
            if (p != null && p.equals(packageName)) {
                needClear = true;
                DataHelper.setDefaultAppPackageName(ctx, appType, "");
            }
        }
        if (needClear) {
            packageManager.clearPackagePreferredActivities(packageName);
        }
    }
}
