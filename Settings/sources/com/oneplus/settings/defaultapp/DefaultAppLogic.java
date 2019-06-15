package com.oneplus.settings.defaultapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.oneplus.settings.defaultapp.apptype.DefaultAppTypeInfo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultAppLogic {
    private static final byte[] initLock = new byte[0];
    private static DefaultAppLogic instance;
    private final String TAG = "DefaultAppLogic";
    private final Context mContext;
    private Map<String, List<String>> mExcludedMap;
    private String[] mKeys;
    private String[] mValues;

    private DefaultAppLogic(Context context) {
        this.mContext = context;
        this.mKeys = DefaultAppConstants.DEFAULTAPP_VALUE_LIST_KEY;
        this.mValues = DefaultAppUtils.getDefaultAppValueList();
        initExcludedMap();
    }

    public static DefaultAppLogic getInstance(Context context) {
        if (instance == null) {
            synchronized (initLock) {
                if (instance == null) {
                    instance = new DefaultAppLogic(context);
                }
            }
        }
        return instance;
    }

    private void initExcludedMap() {
        this.mExcludedMap = new HashMap();
        List<String> excludedGalleryList = new ArrayList();
        excludedGalleryList.add("com.android.documentsui");
        this.mExcludedMap.put(this.mKeys[1], excludedGalleryList);
    }

    public void initDefaultAppSettings() {
        initDefaultAppSettings(false);
    }

    public void initDefaultAppSettings(boolean forceReset) {
        if (forceReset || !DataHelper.isDefaultAppInited(this.mContext)) {
            for (int i = 0; i < this.mKeys.length; i++) {
                String appType = this.mKeys[i];
                String app = getPmDefaultAppPackageName(appType);
                if (app == null || "android".equals(app)) {
                    DefaultAppUtils.resetDefaultApp(this.mContext, this.mKeys[i]);
                } else {
                    List appInfoList = getAppInfoList(appType);
                    List packageList = getAppPackageNameList(appType, appInfoList);
                    setDefaultAppPosition(appType, appInfoList, packageList, getDefaultAppPosition(packageList, app));
                }
            }
            DataHelper.setDefaultAppInited(this.mContext);
        }
    }

    private boolean isAppExist(String appType, List<String> packageNameList, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        List<String> excludedList = (List) this.mExcludedMap.get(appType);
        if (excludedList != null && excludedList.contains(packageName)) {
            return true;
        }
        for (int index = 0; index < packageNameList.size(); index++) {
            if (packageName.equals(packageNameList.get(index))) {
                return true;
            }
        }
        return false;
    }

    public List<DefaultAppActivityInfo> getAppInfoList(String appType) {
        List<DefaultAppActivityInfo> appInfoList = new ArrayList();
        List<Intent> intentList = DefaultAppUtils.create(this.mContext, appType).getAppIntent();
        PackageManager packageManager = this.mContext.getPackageManager();
        for (Intent intent : intentList) {
            DefaultAppActivityInfo appActivityInfo = new DefaultAppActivityInfo();
            for (ResolveInfo resolveInfo : packageManager.queryIntentActivities(intent, 131072)) {
                appActivityInfo.addActivityInfo(resolveInfo.activityInfo);
            }
            appInfoList.add(appActivityInfo);
        }
        return appInfoList;
    }

    public List<String> getAppPackageNameList(String appType, List<DefaultAppActivityInfo> appInfoList) {
        List<String> packageNameList = new ArrayList();
        for (DefaultAppActivityInfo appActivityInfo : appInfoList) {
            for (ActivityInfo activityInfo : appActivityInfo.getActivityInfo()) {
                if (!isAppExist(appType, packageNameList, activityInfo.packageName)) {
                    packageNameList.add(activityInfo.packageName);
                }
            }
        }
        return packageNameList;
    }

    private void updateRelatedDefaultApp(String appType, String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            int i = 0;
            while (i < this.mKeys.length) {
                if (!this.mKeys[i].equals(appType) && packageName.equals(this.mValues[i])) {
                    resetDefaultApp(this.mKeys[i]);
                }
                i++;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void resetDefaultApp(String appType) {
        List appInfoList = getAppInfoList(appType);
        List packageNameList = getAppPackageNameList(appType, appInfoList);
        setDefaultAppPosition(appType, appInfoList, packageNameList, getSystemDefaultAppPosition(appType, packageNameList));
    }

    public void setDefaultAppPosition(String appType, List<DefaultAppActivityInfo> appInfoList, List<String> packageNameList, String defaultPackageName) {
        if (getDefaultAppPosition(appType, (List) packageNameList) == -1) {
            for (String packageName : packageNameList) {
                if (!defaultPackageName.equals(packageName)) {
                    setDefaultApp(appType, appInfoList, packageNameList, packageName);
                }
            }
        }
        setDefaultApp(appType, appInfoList, packageNameList, defaultPackageName);
    }

    public void setDefaultApp(String appType, List<DefaultAppActivityInfo> appInfoList, List<String> packageNameList, String defaultPackageName) {
        Exception e;
        StringBuilder stringBuilder;
        String str = appType;
        List list = packageNameList;
        String str2 = defaultPackageName;
        PackageManager packageManager = this.mContext.getPackageManager();
        int prePosition = getDefaultAppPosition(str, list);
        DataHelper.setDefaultAppPackageName(this.mContext, str, str2);
        if (prePosition != -1) {
            packageManager.clearPackagePreferredActivities((String) list.get(prePosition));
            updateRelatedDefaultApp(str, (String) list.get(prePosition));
        }
        DefaultAppTypeInfo appTypeInfo = DefaultAppUtils.create(this.mContext, str);
        if (appTypeInfo != null) {
            List<IntentFilter> filterList = appTypeInfo.getAppFilter();
            List<Integer> matchList = appTypeInfo.getAppMatchParam();
            int index = 0;
            while (true) {
                int index2 = index;
                int prePosition2;
                DefaultAppTypeInfo appTypeInfo2;
                if (index2 < filterList.size()) {
                    List<String> list2;
                    List<ActivityInfo> activityInfoList = ((DefaultAppActivityInfo) appInfoList.get(index2)).getActivityInfo();
                    ComponentName[] arrayOfComponentName = new ComponentName[activityInfoList.size()];
                    int i = activityInfoList.size() - 1;
                    ComponentName component = null;
                    while (true) {
                        index = i;
                        if (index < 0) {
                            break;
                        }
                        ActivityInfo activityInfo = (ActivityInfo) activityInfoList.get(index);
                        prePosition2 = prePosition;
                        prePosition = activityInfo.packageName;
                        appTypeInfo2 = appTypeInfo;
                        String className = activityInfo.name;
                        ComponentName componentName = new ComponentName(prePosition, className);
                        arrayOfComponentName[index] = componentName;
                        if (!TextUtils.isEmpty(prePosition) && prePosition.equals(str2)) {
                            component = new ComponentName(prePosition, className);
                        }
                        i = index - 1;
                        prePosition = prePosition2;
                        appTypeInfo = appTypeInfo2;
                        list2 = packageNameList;
                    }
                    prePosition2 = prePosition;
                    appTypeInfo2 = appTypeInfo;
                    if (component != null) {
                        packageManager.addPreferredActivity((IntentFilter) filterList.get(index2), ((Integer) matchList.get(index2)).intValue(), arrayOfComponentName, component);
                        if (str.equals("op_default_app_browser")) {
                            try {
                                Class<?> uh = Class.forName(UserHandle.class.getName());
                                try {
                                    Method method = uh.getMethod("getCallingUserId", new Class[0]);
                                    prePosition = ((Integer) method.invoke(uh, new Object[0])).intValue();
                                    Class<?> c = packageManager.getClass();
                                    Method setDefaultBrowserPackageName = "setDefaultBrowserPackageName";
                                    Class[] clsArr = new Class[2];
                                    try {
                                        clsArr[0] = String.class;
                                        clsArr[1] = Integer.TYPE;
                                        setDefaultBrowserPackageName = c.getDeclaredMethod(setDefaultBrowserPackageName, clsArr);
                                        Object[] objArr = new Object[2];
                                        try {
                                            objArr[0] = component.getPackageName();
                                            objArr[1] = Integer.valueOf(prePosition);
                                            invoke(packageManager, setDefaultBrowserPackageName, objArr);
                                        } catch (Exception e2) {
                                            e = e2;
                                        }
                                    } catch (Exception e3) {
                                        e = e3;
                                        i = 0;
                                        stringBuilder = new StringBuilder();
                                        stringBuilder.append("setDefaultAppPosition: ");
                                        stringBuilder.append(e);
                                        Log.e("DefaultAppLogic", stringBuilder.toString());
                                        index = index2 + 1;
                                        prePosition = prePosition2;
                                        appTypeInfo = appTypeInfo2;
                                        str = appType;
                                        list2 = packageNameList;
                                    }
                                } catch (Exception e4) {
                                    e = e4;
                                    i = 0;
                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append("setDefaultAppPosition: ");
                                    stringBuilder.append(e);
                                    Log.e("DefaultAppLogic", stringBuilder.toString());
                                    index = index2 + 1;
                                    prePosition = prePosition2;
                                    appTypeInfo = appTypeInfo2;
                                    str = appType;
                                    list2 = packageNameList;
                                }
                            } catch (Exception e5) {
                                e = e5;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append("setDefaultAppPosition: ");
                                stringBuilder.append(e);
                                Log.e("DefaultAppLogic", stringBuilder.toString());
                                index = index2 + 1;
                                prePosition = prePosition2;
                                appTypeInfo = appTypeInfo2;
                                str = appType;
                                list2 = packageNameList;
                            }
                            index = index2 + 1;
                            prePosition = prePosition2;
                            appTypeInfo = appTypeInfo2;
                            str = appType;
                            list2 = packageNameList;
                        }
                    }
                    index = index2 + 1;
                    prePosition = prePosition2;
                    appTypeInfo = appTypeInfo2;
                    str = appType;
                    list2 = packageNameList;
                } else {
                    List<DefaultAppActivityInfo> list3 = appInfoList;
                    prePosition2 = prePosition;
                    appTypeInfo2 = appTypeInfo;
                    return;
                }
            }
        }
    }

    public void setDefaultAppPosition(String appType, List<DefaultAppActivityInfo> appInfoList, List<String> packageNameList, int position) {
        if (position >= 0 && position < packageNameList.size()) {
            setDefaultAppPosition(appType, (List) appInfoList, (List) packageNameList, (String) packageNameList.get(position));
        }
    }

    public Object invoke(PackageManager c, Method m, Object... args) {
        if (c == null || m == null) {
            return null;
        }
        try {
            return m.invoke(c, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public int getDefaultAppPosition(String appType, List<String> packageNameList, String defaultPackageName) {
        for (int index = 0; index < packageNameList.size(); index++) {
            if (((String) packageNameList.get(index)).equals(defaultPackageName)) {
                return index;
            }
        }
        return -1;
    }

    public int getDefaultAppPosition(String appType, List<String> packageNameList) {
        return getDefaultAppPosition((List) packageNameList, DataHelper.getDefaultAppPackageName(this.mContext, appType));
    }

    public int getDefaultAppPosition(List<String> packageNameList, String defaultPackageName) {
        for (int index = 0; index < packageNameList.size(); index++) {
            if (((String) packageNameList.get(index)).equals(defaultPackageName)) {
                return index;
            }
        }
        return -1;
    }

    public int getSystemDefaultAppPosition(String appType, List<String> packageNameList) {
        String systemDefaultPackageName = DefaultAppUtils.getSystemDefaultPackageName(this.mContext, appType);
        for (int index = 0; index < packageNameList.size(); index++) {
            if (((String) packageNameList.get(index)).equals(systemDefaultPackageName)) {
                return index;
            }
        }
        return -1;
    }

    public String getDefaultAppName(String appType) {
        return DefaultAppUtils.getDefaultAppName(this.mContext, appType);
    }

    public String getDefaultAppPackageName(String appType) {
        return DefaultAppUtils.getDefaultAppPackageName(this.mContext, appType);
    }

    public String getPmDefaultAppPackageName(String appType) {
        List<Intent> intentList = DefaultAppUtils.create(this.mContext, appType).getAppIntent();
        PackageManager packageManager = this.mContext.getPackageManager();
        Set<String> defaultApp = new LinkedHashSet();
        for (Intent intent : intentList) {
            ResolveInfo r = packageManager.resolveActivity(intent, 65536);
            if (r != null) {
                defaultApp.add(r.activityInfo.packageName);
            }
        }
        List<String> excludedList = (List) this.mExcludedMap.get(appType);
        defaultApp.remove("android");
        Iterator<String> iterator = defaultApp.iterator();
        while (iterator.hasNext()) {
            String pkg = (String) iterator.next();
            if (excludedList != null && excludedList.contains(pkg)) {
                iterator.remove();
            }
        }
        if (defaultApp.size() < 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getDefaultAppPackageName appType:");
            stringBuilder.append(appType);
            stringBuilder.append(" error defaultApp.size != 1");
            Log.d("DefaultAppLogic", stringBuilder.toString());
            return null;
        }
        String pkg0 = defaultApp.toArray()[0];
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("getPmDefaultAppPackageName appType:");
        stringBuilder2.append(appType);
        stringBuilder2.append(", defaultApp pkg:");
        stringBuilder2.append(pkg0);
        Log.d("DefaultAppLogic", stringBuilder2.toString());
        return pkg0;
    }
}
