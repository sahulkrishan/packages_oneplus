package com.oneplus.settings.displaysizeadaption;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.utils.OPUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplaySizeAdaptiongeManager {
    public static final int APP_TYPE_COMPAT_MODE = 0;
    public static final int APP_TYPE_DEFAULT_MODE = 1;
    public static final int APP_TYPE_FULL_SCREEN_MODE = 3;
    private static final int FULLSCREENAPP_TYPE = 70;
    private static final int MODE_COMPAT_VALUE = 100;
    private static final int MODE_FULL_VALUE = 102;
    private static final int MODE_NONFULL_VALUE = 101;
    private static Map<String, OPAppModel> m17819FullScreenAppMap = new HashMap();
    private static DisplaySizeAdaptiongeManager mDisplaySizeAdaptiongeManager;
    private static Map<String, OPAppModel> mFullScreenAppMap = new HashMap();
    private static Map<String, OPAppModel> mOriginalSizeAppMap = new HashMap();
    private static Map<String, OPAppModel> mTmp17819FullScreenAppMap = new HashMap();
    private static Map<String, OPAppModel> mTmpFullScreenAppMap = new HashMap();
    private static Map<String, OPAppModel> mTmpOriginalSizeAppMap = new HashMap();
    private ActivityManager mAm;
    private AppOpsManager mAppOpsManager;
    private Context mContext;
    private PackageManager mPackageManager;
    ApplicationInfo multiAppInfo = null;

    public DisplaySizeAdaptiongeManager(Context context) {
        this.mContext = context;
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mPackageManager = this.mContext.getPackageManager();
        this.mAm = (ActivityManager) this.mContext.getSystemService("activity");
    }

    public static DisplaySizeAdaptiongeManager getInstance(Context context) {
        if (mDisplaySizeAdaptiongeManager == null) {
            mDisplaySizeAdaptiongeManager = new DisplaySizeAdaptiongeManager(context);
        }
        return mDisplaySizeAdaptiongeManager;
    }

    public Map<String, OPAppModel> loadAppMap() {
        loadFullScreenApp();
        loadOriginalSizeApp();
        Map<String, OPAppModel> appMap = new HashMap();
        try {
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            if (!OPUtils.isSupportScreenCutting()) {
                mainIntent.addCategory("android.intent.category.LAUNCHER");
            }
            List<ResolveInfo> resolveInfos = this.mPackageManager.queryIntentActivities(mainIntent, 0);
            if (resolveInfos.isEmpty()) {
                return null;
            }
            for (ResolveInfo reInfo : resolveInfos) {
                String activityName = reInfo.activityInfo.name;
                String packageName = reInfo.activityInfo.packageName;
                appMap.put(packageName, new OPAppModel(packageName, (String) reInfo.loadLabel(this.mPackageManager), "", 0, false));
            }
            return appMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Drawable getBadgedIcon(PackageManager pm, ResolveInfo resolveInfo) {
        ApplicationInfo info = resolveInfo.activityInfo.applicationInfo;
        return pm.getUserBadgedIcon(pm.loadUnbadgedItemIcon(info, info), new UserHandle(UserHandle.getUserId(info.uid)));
    }

    private void loadClassAppList(int value) {
        int i = value;
        HashMap classAppMap = new HashMap();
        try {
            List<PackageOps> packageOps = this.mAppOpsManager.getPackagesForOps(new int[]{70});
            if (packageOps != null) {
                for (PackageOps packageOp : packageOps) {
                    int appUid = packageOp.getUid();
                    for (OpEntry op : packageOp.getOps()) {
                        if (op.getOp() == 70 && op.getMode() == i) {
                            classAppMap.put(packageOp.getPackageName(), Integer.valueOf(appUid));
                        }
                    }
                }
            }
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            if (!OPUtils.isSupportScreenCutting()) {
                mainIntent.addCategory("android.intent.category.LAUNCHER");
            }
            List<ResolveInfo> resolveInfos = this.mPackageManager.queryIntentActivities(mainIntent, 0);
            if (!resolveInfos.isEmpty()) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    String packageName = reInfo.activityInfo.packageName;
                    String appLabel = (String) reInfo.loadLabel(this.mPackageManager);
                    boolean isSelected = classAppMap.containsKey(packageName);
                    if (isSelected) {
                        OPAppModel appModel = new OPAppModel(packageName, appLabel, "", 0, isSelected);
                        if (i != 0) {
                            if (i != 100 || OPUtils.isSupportScreenCutting()) {
                                if (i != 1) {
                                    if (i != 101) {
                                        if (OPUtils.isSupportScreenCutting() && (i == 2 || i == 102)) {
                                            mTmpFullScreenAppMap.remove(packageName);
                                            mTmpOriginalSizeAppMap.remove(packageName);
                                            mTmp17819FullScreenAppMap.put(packageName, appModel);
                                        } else {
                                            mTmpFullScreenAppMap.remove(packageName);
                                            mTmpOriginalSizeAppMap.remove(packageName);
                                            mTmp17819FullScreenAppMap.remove(packageName);
                                        }
                                    }
                                }
                                mTmpFullScreenAppMap.put(packageName, appModel);
                                mTmpOriginalSizeAppMap.remove(packageName);
                                mTmp17819FullScreenAppMap.remove(packageName);
                            }
                        }
                        mTmpOriginalSizeAppMap.put(packageName, appModel);
                        mTmpFullScreenAppMap.remove(packageName);
                        mTmp17819FullScreenAppMap.remove(packageName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFullScreenApp() {
        loadClassAppList(1);
        loadClassAppList(101);
        loadClassAppList(2);
        loadClassAppList(102);
        mFullScreenAppMap.clear();
        m17819FullScreenAppMap.clear();
        mFullScreenAppMap = new HashMap(mTmpFullScreenAppMap);
        m17819FullScreenAppMap = new HashMap(mTmp17819FullScreenAppMap);
        mTmpFullScreenAppMap.clear();
        mTmp17819FullScreenAppMap.clear();
    }

    private void loadOriginalSizeApp() {
        loadClassAppList(0);
        loadClassAppList(100);
        mOriginalSizeAppMap.clear();
        mOriginalSizeAppMap = new HashMap(mTmpOriginalSizeAppMap);
        mTmpOriginalSizeAppMap.clear();
    }

    public void setClassApp(int uid, String packageName, int value) {
        this.mAppOpsManager.setMode(70, uid, packageName, value);
        removeTask(packageName);
        try {
            this.multiAppInfo = this.mPackageManager.getApplicationInfoByUserId(packageName, 1, 999);
        } catch (NameNotFoundException e) {
        }
        if (this.multiAppInfo != null) {
            this.mAppOpsManager.setMode(70, this.multiAppInfo.uid, packageName, value);
        }
        try {
            OPAppModel oPAppModel = new OPAppModel(packageName, this.mPackageManager.getApplicationInfo(packageName, 0).loadLabel(this.mPackageManager).toString(), "", uid, false);
            if (value != 1) {
                if (value != 101) {
                    if (value != 0) {
                        if (value != 100) {
                            if (OPUtils.isSupportScreenCutting() && (value == 102 || value == 2)) {
                                mFullScreenAppMap.remove(packageName);
                                mOriginalSizeAppMap.remove(packageName);
                                m17819FullScreenAppMap.put(packageName, oPAppModel);
                                return;
                            }
                            mFullScreenAppMap.remove(packageName);
                            mOriginalSizeAppMap.remove(packageName);
                            m17819FullScreenAppMap.remove(packageName);
                            return;
                        }
                    }
                    mOriginalSizeAppMap.put(packageName, oPAppModel);
                    mFullScreenAppMap.remove(packageName);
                    m17819FullScreenAppMap.remove(packageName);
                    return;
                }
            }
            mFullScreenAppMap.put(packageName, oPAppModel);
            mOriginalSizeAppMap.remove(packageName);
            m17819FullScreenAppMap.remove(packageName);
        } catch (NameNotFoundException e2) {
            Log.e("DisplaySizeAdaptiongeManager", e2.getMessage());
        }
    }

    public int getAppTypeValue(String packageName) {
        OPAppModel mFullScreenOPAppModel = (OPAppModel) mFullScreenAppMap.get(packageName);
        OPAppModel m17819FullScreenAppModel = (OPAppModel) m17819FullScreenAppMap.get(packageName);
        if (((OPAppModel) mOriginalSizeAppMap.get(packageName)) != null) {
            return 0;
        }
        if (mFullScreenOPAppModel != null) {
            return 1;
        }
        if (!OPUtils.isSupportScreenCutting()) {
            return 1;
        }
        if (m17819FullScreenAppModel != null) {
            return 3;
        }
        return 1;
    }

    private void removeTask(String packageName) {
        List<RecentTaskInfo> recentTaskInfos = null;
        try {
            recentTaskInfos = ActivityManager.getService().getRecentTasks(Integer.MAX_VALUE, 2, -2).getList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (recentTaskInfos != null) {
            for (RecentTaskInfo recentTaskInfo : recentTaskInfos) {
                ComponentName baseActivity;
                if (recentTaskInfo != null) {
                    baseActivity = recentTaskInfo.baseActivity;
                } else {
                    baseActivity = null;
                }
                if (!(baseActivity == null || TextUtils.isEmpty(packageName) || !packageName.equals(baseActivity.getPackageName()))) {
                    try {
                        ActivityManager.getService().removeTask(recentTaskInfo.persistentId);
                    } catch (RemoteException e2) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Failed to remove task=");
                        stringBuilder.append(recentTaskInfo.persistentId);
                        Log.w("OPNotchDisplayGuideActivity", stringBuilder.toString(), e2);
                    }
                }
            }
        }
    }
}
