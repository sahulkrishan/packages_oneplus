package com.oneplus.settings.apploader;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.oneplus.settings.OPOnlineConfigManager;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.gestures.OPGestureUtils;
import com.oneplus.settings.highpowerapp.PackageUtils;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OPApplicationLoader {
    public static final Comparator<OPAppModel> ALPHA_COMPARATOR = new Comparator<OPAppModel>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(OPAppModel object1, OPAppModel object2) {
            int compareResult = this.sCollator.compare(object1.getLabel(), object2.getLabel());
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = this.sCollator.compare(object1.getPkgName(), object2.getPkgName());
            if (compareResult != 0) {
                return compareResult;
            }
            return object1.getUid() - object2.getUid();
        }
    };
    public static final Uri APP_CATEGORY_URI = Uri.parse("content://net.oneplus.provider.appcategoryprovider.AppCategoryContentProvider/app_category");
    private static final String CALCULATOR_PACKAGE_NAME = "com.oneplus.calculator";
    private static final String CAMERA_PACKAGE_NAME = "com.oneplus.camera";
    private static final String CARD_PACKAGE_NAME = "com.oneplus.card";
    public static final String CATEGORY_AUTHORIY = "net.oneplus.provider.appcategoryprovider.AppCategoryContentProvider";
    private static final String CONTACS_PACKAGE_NAME = "com.android.contacts";
    public static final int DATA_LOAD_COMPLETED = 0;
    private static final String DESKCLOCK_PACKAGE_NAME = "com.oneplus.deskclock";
    private static final String FILEMANAGER_PACKAGE_NAME = "com.oneplus.filemanager";
    private static final String GALLERY_PACKAGE_NAME = "com.oneplus.gallery";
    private static final String GOOGLE_QUICK_SEARCH_BOX_PACKAGE_NAME = "com.google.android.googlequicksearchbox";
    public static final int LOAD_ALL_APP = 0;
    public static final int LOAD_ALL_APP_SORT_BY_SELECTED = 3;
    public static final int LOAD_ALL_QUICK_LAUNCH_APPS = 4;
    public static final int LOAD_ALL_QUICK_LAUNCH_SHORTCUTS = 5;
    public static final int LOAD_SELECTED_APP = 1;
    public static final int LOAD_UNSELECTED_APP = 2;
    private static final String MARKET_PACKAGE_NAME = "com.oneplus.market";
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private static final String NOTE_PACKAGE_NAME = "com.oneplus.note";
    public static final int OP_GAME_MODE_APP = 68;
    public static final int OP_READ_MODE_APP = 67;
    public static final String PACKAGENAME_CALENDAR = "com.google.android.calendar";
    public static final String PACKAGENAME_OP_CALENDAR = "com.oneplus.calendar";
    private static final String PHONE_PACKAGE_NAME = "com.android.dialer";
    private static long PROGRESS_MIN_SHOW_TIME = 500;
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String SOUNDRECORDER_PACKAGE_NAME = "com.oneplus.soundrecorder";
    private static final String TAG = "AppLockerDataController";
    private static final String WEATHER_PACKAGE_NAME = "com.oneplus.weather";
    private static final String WEATHER_PACKAGE_NAME_NET = "net.oneplus.weather";
    private static long WILL_SHOW_PROGRESS_TIME = 300;
    public static ArrayList<String> mGameAppArrayList = new ArrayList();
    private static ArrayList<String> mGameAppList = new ArrayList();
    public final Comparator<ResolveInfo> RESOLVEINFO_ALPHA_COMPARATOR = new Comparator<ResolveInfo>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(ResolveInfo object1, ResolveInfo object2) {
            int compareResult = this.sCollator.compare(object1.loadLabel(OPApplicationLoader.this.mPackageManager), object2.loadLabel(OPApplicationLoader.this.mPackageManager));
            if (compareResult != 0) {
                return compareResult;
            }
            compareResult = this.sCollator.compare(object1.activityInfo.packageName, object2.activityInfo.packageName);
            if (compareResult != 0) {
                return compareResult;
            }
            return object1.activityInfo.applicationInfo.uid - object1.activityInfo.applicationInfo.uid;
        }
    };
    private List<OPAppModel> mAllAppList = new ArrayList();
    private List<OPAppModel> mAllAppSelectedList = new ArrayList();
    private List<OPAppModel> mAllAppSortBySelectedList = new ArrayList();
    private List<OPAppModel> mAllAppUnSelectedList = new ArrayList();
    private List<OPAppModel> mAllQuickLaunchAppList = new ArrayList();
    private List<OPAppModel> mAllQuickLaunchShortcuts = new ArrayList();
    private AppOpsManager mAppOpsManager;
    private int mAppType;
    private Context mContext;
    private Handler mHandler1 = new Handler(Looper.getMainLooper());
    private boolean mHasShowProgress;
    private List<OPAppModel> mIsGameUnSelectedAppList = new ArrayList();
    private boolean mLoading = false;
    private View mLoadingContainer;
    private boolean mNeedLoadWorkProfileApps = true;
    private PackageManager mPackageManager;
    private final List<UserHandle> mProfiles;
    private List<OPAppModel> mSelectedAppList = new ArrayList();
    private Map<Integer, String> mSelectedAppMap = new HashMap();
    private Runnable mShowPromptRunnable;
    private long mShowPromptTime;
    private ExecutorService mThreadPool = Executors.newCachedThreadPool();
    private List<OPAppModel> mUnSelectedAppList = new ArrayList();
    private Map<String, Integer> mUnSelectedAppMap = new HashMap();
    private final UserManager mUserManager;

    public class GestureAppComparetor implements Comparator<OPAppModel> {
        public int compare(OPAppModel lhs, OPAppModel rhs) {
            if (lhs.getLabel().equals(rhs.getLabel())) {
                return 0;
            }
            return lhs.getLabel().compareTo(rhs.getLabel());
        }
    }

    public class SelectedAppComparetor implements Comparator<OPAppModel> {
        public int compare(OPAppModel lhs, OPAppModel rhs) {
            if (lhs.isSelected() && !rhs.isSelected()) {
                return -1;
            }
            if (lhs.isSelected() || !rhs.isSelected()) {
                return 0;
            }
            return 1;
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final void onPreExecute() {
        this.mHasShowProgress = false;
        this.mShowPromptRunnable = new Runnable() {
            public void run() {
                OPApplicationLoader.this.mHasShowProgress = true;
                if (OPApplicationLoader.this.mLoadingContainer != null) {
                    OPApplicationLoader.this.mLoadingContainer.setVisibility(0);
                }
                OPApplicationLoader.this.mShowPromptTime = System.currentTimeMillis();
            }
        };
        this.mHandler1.postDelayed(this.mShowPromptRunnable, WILL_SHOW_PROGRESS_TIME);
    }

    /* Access modifiers changed, original: protected|final */
    public final void onPostExecute() {
        if (this.mHasShowProgress) {
            long remainShowTime = PROGRESS_MIN_SHOW_TIME - (System.currentTimeMillis() - this.mShowPromptTime);
            if (remainShowTime > 0) {
                this.mHandler1.postDelayed(new Runnable() {
                    public void run() {
                        if (OPApplicationLoader.this.mLoadingContainer != null) {
                            OPApplicationLoader.this.mLoadingContainer.setVisibility(8);
                        }
                    }
                }, remainShowTime);
                return;
            } else {
                this.mHandler1.post(new Runnable() {
                    public void run() {
                        if (OPApplicationLoader.this.mLoadingContainer != null) {
                            OPApplicationLoader.this.mLoadingContainer.setVisibility(8);
                        }
                    }
                });
                return;
            }
        }
        this.mHandler1.removeCallbacks(this.mShowPromptRunnable);
    }

    public OPApplicationLoader(Context context, PackageManager packageManager) {
        this.mContext = context;
        this.mPackageManager = packageManager;
        this.mUserManager = UserManager.get(context);
        this.mProfiles = this.mUserManager.getUserProfiles();
    }

    public OPApplicationLoader(Context context, AppOpsManager appOpsManager, PackageManager packageManager) {
        this.mContext = context;
        this.mAppOpsManager = appOpsManager;
        this.mPackageManager = packageManager;
        this.mUserManager = UserManager.get(context);
        this.mProfiles = this.mUserManager.getUserProfiles();
    }

    public boolean isNeedLoadWorkProfileApps() {
        return this.mNeedLoadWorkProfileApps;
    }

    public void setNeedLoadWorkProfileApps(boolean needLoadWorkProfileApps) {
        this.mNeedLoadWorkProfileApps = needLoadWorkProfileApps;
    }

    public void setmLoadingContainer(View mView) {
        this.mLoadingContainer = mView;
    }

    public void setAppType(int type) {
        this.mAppType = type;
    }

    public void initData(final int type, final Handler handler) {
        this.mThreadPool.execute(new Runnable() {
            public void run() {
                OPApplicationLoader.this.onPreExecute();
                OPApplicationLoader.this.mLoading = true;
                if (OPApplicationLoader.this.mAppType == 68) {
                    OPApplicationLoader.this.loadGameApp();
                }
                OPApplicationLoader.this.loadAppListByType(type);
                OPApplicationLoader.this.mLoading = false;
                OPApplicationLoader.this.onPostExecute();
                handler.sendEmptyMessage(type);
            }
        });
    }

    public boolean isLoading() {
        return this.mLoading;
    }

    public Map<Integer, String> loadSelectedGameOrReadAppMap(int type) {
        List<PackageOps> packageOps = this.mAppOpsManager.getPackagesForOps(new int[]{type});
        if (this.mSelectedAppMap != null) {
            this.mSelectedAppMap.clear();
        }
        if (packageOps != null) {
            for (PackageOps packageOp : packageOps) {
                int userId = UserHandle.getUserId(packageOp.getUid());
                int appUid = packageOp.getUid();
                if (isThisUserAProfileOfCurrentUser(userId)) {
                    for (OpEntry op : packageOp.getOps()) {
                        if (op.getOp() == type && op.getMode() == 0) {
                            this.mSelectedAppMap.put(Integer.valueOf(appUid), packageOp.getPackageName());
                        }
                    }
                }
            }
        }
        return this.mSelectedAppMap;
    }

    private boolean isThisUserAProfileOfCurrentUser(int userId) {
        int profilesMax = this.mProfiles.size();
        for (int i = 0; i < profilesMax; i++) {
            if (((UserHandle) this.mProfiles.get(i)).getIdentifier() == userId) {
                return true;
            }
        }
        return false;
    }

    public void loadAppListByType(int type) {
        switch (type) {
            case 0:
                loadAllAppList();
                return;
            case 1:
                loadSelectedAppList();
                return;
            case 2:
                loadUnSelectedAppList();
                return;
            case 3:
                loadAllAppListSortBySelected(true);
                return;
            case 4:
                loadAllQuickLaunchAppList();
                return;
            case 5:
                loadAllQuickLaunchShortcuts();
                return;
            default:
                return;
        }
    }

    public List<OPAppModel> getAppListByType(int type) {
        if (type == 0) {
            return this.mAllAppList;
        }
        if (type == 1) {
            return this.mSelectedAppList;
        }
        if (type == 2) {
            return this.mUnSelectedAppList;
        }
        if (type == 5) {
            return this.mAllQuickLaunchShortcuts;
        }
        if (type == 4) {
            return this.mAllQuickLaunchAppList;
        }
        return this.mAllAppSortBySelectedList;
    }

    public List<OPAppModel> getAllAppList() {
        return this.mAllAppList;
    }

    public List<OPAppModel> getSelectedAppList() {
        return this.mSelectedAppList;
    }

    public List<OPAppModel> getUnSelectedAppList() {
        return this.mUnSelectedAppList;
    }

    public List<OPAppModel> getAllAppSortBySelectList() {
        return this.mAllAppSortBySelectedList;
    }

    private Drawable getBadgedIcon(PackageManager pm, ResolveInfo resolveInfo) {
        ApplicationInfo info = resolveInfo.activityInfo.applicationInfo;
        return pm.getUserBadgedIcon(pm.loadUnbadgedItemIcon(info, info), new UserHandle(UserHandle.getUserId(info.uid)));
    }

    public void loadAllAppListSortBySelected(boolean noSystemApp) {
        try {
            this.mAllAppSortBySelectedList.clear();
            this.mAllAppSelectedList.clear();
            this.mAllAppUnSelectedList.clear();
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            List<ResolveInfo> resolveInfos = this.mPackageManager.queryIntentActivities(mainIntent, 0);
            if (!resolveInfos.isEmpty()) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    String packageName = reInfo.activityInfo.packageName;
                    String appLabel = (String) reInfo.loadLabel(this.mPackageManager);
                    if (!noSystemApp || !PackageUtils.isSystemApplication(this.mContext, packageName)) {
                        if (multiAppPackageExcludeFilter(this.mContext, packageName)) {
                            int uid = reInfo.activityInfo.applicationInfo.uid;
                            boolean z = this.mSelectedAppMap.containsKey(Integer.valueOf(uid)) && this.mSelectedAppMap.containsValue(packageName);
                            boolean isSelected = z;
                            OPAppModel appModel = new OPAppModel(packageName, appLabel, "", uid, isSelected);
                            appModel.setAppIcon(getBadgedIcon(this.mPackageManager, reInfo));
                            if (isSelected) {
                                this.mAllAppSelectedList.add(appModel);
                            } else {
                                this.mAllAppUnSelectedList.add(appModel);
                            }
                        }
                    }
                }
                Collections.sort(this.mAllAppSelectedList, ALPHA_COMPARATOR);
                Collections.sort(this.mAllAppUnSelectedList, ALPHA_COMPARATOR);
                this.mAllAppSortBySelectedList.addAll(this.mAllAppSelectedList);
                this.mAllAppSortBySelectedList.addAll(this.mAllAppUnSelectedList);
            }
        } catch (Exception e) {
            Log.e(TAG, "some unknown error happened.");
            e.printStackTrace();
        }
    }

    public void loadAllAppList() {
        try {
            List<ResolveInfo> resolveInfos;
            this.mAllAppList.clear();
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            if (this.mNeedLoadWorkProfileApps) {
                resolveInfos = new ArrayList();
                for (UserInfo user : this.mUserManager.getProfiles(UserHandle.myUserId())) {
                    resolveInfos.addAll(this.mPackageManager.queryIntentActivitiesAsUser(mainIntent, 0, user.id));
                }
            } else {
                resolveInfos = this.mPackageManager.queryIntentActivities(mainIntent, 0);
            }
            if (!resolveInfos.isEmpty()) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    String packageName = reInfo.activityInfo.packageName;
                    String appLabel = (String) reInfo.loadLabel(this.mPackageManager);
                    if (!CAMERA_PACKAGE_NAME.equals(packageName)) {
                        String str = packageName;
                        OPAppModel appModel = new OPAppModel(str, appLabel, "", reInfo.activityInfo.applicationInfo.uid, false);
                        appModel.setAppIcon(getBadgedIcon(this.mPackageManager, reInfo));
                        this.mAllAppList.add(appModel);
                    }
                }
                Collections.sort(this.mAllAppList, ALPHA_COMPARATOR);
            }
        } catch (Exception e) {
            Log.e(TAG, "some unknown error happened.");
            e.printStackTrace();
        }
    }

    public void loadAllQuickLaunchAppList() {
        try {
            List<ResolveInfo> resolveInfos;
            this.mAllQuickLaunchAppList.clear();
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            if (this.mNeedLoadWorkProfileApps) {
                resolveInfos = new ArrayList();
                for (UserInfo user : this.mUserManager.getProfiles(UserHandle.myUserId())) {
                    resolveInfos.addAll(this.mPackageManager.queryIntentActivitiesAsUser(mainIntent, 0, user.id));
                }
            } else {
                resolveInfos = this.mPackageManager.queryIntentActivities(mainIntent, 0);
            }
            if (!resolveInfos.isEmpty()) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    OPAppModel oPAppModel = new OPAppModel(reInfo.activityInfo.packageName, (String) reInfo.loadLabel(this.mPackageManager), "", reInfo.activityInfo.applicationInfo.uid, false);
                    oPAppModel.setType(0);
                    oPAppModel.setSelected(OPUtils.isInQuickLaunchList(this.mContext, oPAppModel));
                    oPAppModel.setAppIcon(getBadgedIcon(this.mPackageManager, reInfo));
                    this.mAllQuickLaunchAppList.add(oPAppModel);
                }
                Collections.sort(this.mAllQuickLaunchAppList, ALPHA_COMPARATOR);
            }
        } catch (Exception e) {
            Log.e(TAG, "some unknown error happened.");
            e.printStackTrace();
        }
    }

    public void loadAllQuickLaunchShortcuts() {
        try {
            List<ResolveInfo> resolveInfos;
            this.mAllQuickLaunchShortcuts.clear();
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            if (this.mNeedLoadWorkProfileApps) {
                resolveInfos = new ArrayList();
                for (UserInfo user : this.mUserManager.getProfiles(UserHandle.myUserId())) {
                    resolveInfos.addAll(this.mPackageManager.queryIntentActivitiesAsUser(mainIntent, 0, user.id));
                }
            } else {
                resolveInfos = this.mPackageManager.queryIntentActivities(mainIntent, 0);
            }
            if (!resolveInfos.isEmpty()) {
                Collections.sort(resolveInfos, this.RESOLVEINFO_ALPHA_COMPARATOR);
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    String packageName = reInfo.activityInfo.packageName;
                    String appLabel = (String) reInfo.loadLabel(this.mPackageManager);
                    if (OPGestureUtils.hasShortCuts(this.mContext, packageName)) {
                        loadShortcutByPackageName(packageName, reInfo.activityInfo.applicationInfo.uid);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "some unknown error happened.");
            e.printStackTrace();
        }
    }

    private void loadShortcutByPackageName(String pkg, int uid) {
        String str = pkg;
        List<ShortcutInfo> shortcutInfo = OPGestureUtils.loadShortCuts(this.mContext, str);
        if (shortcutInfo != null) {
            int size = shortcutInfo.size();
            LauncherApps mLauncherApps = (LauncherApps) this.mContext.getSystemService("launcherapps");
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 < size) {
                    ShortcutInfo s = (ShortcutInfo) shortcutInfo.get(i2);
                    CharSequence label = s.getLongLabel();
                    if (TextUtils.isEmpty(label)) {
                        label = s.getShortLabel();
                    }
                    if (TextUtils.isEmpty(label)) {
                        label = s.getId();
                    }
                    CharSequence label2 = label;
                    if (!(OPConstants.PACKAGE_ALIPAY.equals(str) && (OPConstants.PACKAGE_ALIPAY_SCANNING_ID.equals(s.getId()) || OPConstants.PACKAGE_ALIPAY_QRCODE_ID.equals(s.getId())))) {
                        OPAppModel model = new OPAppModel(s.getPackage(), label2.toString(), s.getId(), uid, false);
                        model.setAppLabel(OPUtils.getAppLabel(this.mContext, s.getPackage()));
                        model.setType(1);
                        model.setSelected(OPUtils.isInQuickLaunchList(this.mContext, model));
                        model.setAppIcon(OPUtils.getAppIcon(this.mContext, str));
                        try {
                            model.setShortCutIcon(mLauncherApps.getShortcutIconDrawable(s, 0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        this.mAllQuickLaunchShortcuts.add(model);
                    }
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
    }

    public void loadSelectedAppList() {
        try {
            List<ResolveInfo> resolveInfos;
            this.mSelectedAppList.clear();
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            if (this.mNeedLoadWorkProfileApps) {
                resolveInfos = new ArrayList();
                for (UserInfo user : this.mUserManager.getProfiles(UserHandle.myUserId())) {
                    resolveInfos.addAll(this.mPackageManager.queryIntentActivitiesAsUser(mainIntent, 0, user.id));
                }
            } else {
                resolveInfos = this.mPackageManager.queryIntentActivities(mainIntent, 0);
            }
            if (!resolveInfos.isEmpty()) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    String packageName = reInfo.activityInfo.packageName;
                    String appLabel = (String) reInfo.loadLabel(this.mPackageManager);
                    if (!packageExcludeFilter(packageName)) {
                        int uid = reInfo.activityInfo.applicationInfo.uid;
                        boolean z = this.mSelectedAppMap.containsKey(Integer.valueOf(uid)) && this.mSelectedAppMap.containsValue(packageName);
                        boolean isSelected = z;
                        if (isSelected) {
                            OPAppModel appModel = new OPAppModel(packageName, appLabel, "", uid, isSelected);
                            appModel.setAppIcon(getBadgedIcon(this.mPackageManager, reInfo));
                            this.mSelectedAppList.add(appModel);
                        }
                    }
                }
                Collections.sort(this.mSelectedAppList, ALPHA_COMPARATOR);
            }
        } catch (Exception e) {
            Log.e(TAG, "some unknown error happened.");
            e.printStackTrace();
        }
    }

    public void loadUnSelectedAppList() {
        try {
            List<ResolveInfo> resolveInfos;
            this.mUnSelectedAppList.clear();
            Intent mainIntent = new Intent("android.intent.action.MAIN", null);
            mainIntent.addCategory("android.intent.category.LAUNCHER");
            int i = 0;
            if (this.mNeedLoadWorkProfileApps) {
                resolveInfos = new ArrayList();
                for (UserInfo user : this.mUserManager.getProfiles(UserHandle.myUserId())) {
                    resolveInfos.addAll(this.mPackageManager.queryIntentActivitiesAsUser(mainIntent, 0, user.id));
                }
            } else {
                resolveInfos = this.mPackageManager.queryIntentActivities(mainIntent, 0);
            }
            if (!resolveInfos.isEmpty()) {
                for (ResolveInfo reInfo : resolveInfos) {
                    String activityName = reInfo.activityInfo.name;
                    String packageName = reInfo.activityInfo.packageName;
                    String appLabel = (String) reInfo.loadLabel(this.mPackageManager);
                    if (!packageExcludeFilter(packageName)) {
                        int uid = reInfo.activityInfo.applicationInfo.uid;
                        boolean z = (this.mSelectedAppMap.containsKey(Integer.valueOf(uid)) && this.mSelectedAppMap.containsValue(packageName)) ? true : i;
                        boolean isUnSelected = z;
                        if (!isUnSelected) {
                            boolean z2 = true;
                            OPAppModel appModel = new OPAppModel(packageName, appLabel, "", uid, isUnSelected);
                            appModel.setAppIcon(getBadgedIcon(this.mPackageManager, reInfo));
                            if (this.mAppType == 68) {
                                if (mGameAppList.contains(packageName)) {
                                    appModel.setGameAPP(z2);
                                    this.mIsGameUnSelectedAppList.add(appModel);
                                } else {
                                    appModel.setGameAPP(false);
                                    this.mUnSelectedAppList.add(appModel);
                                }
                                i = 0;
                            } else {
                                i = 0;
                                appModel.setGameAPP(false);
                                this.mUnSelectedAppList.add(appModel);
                            }
                        }
                    }
                }
                if (this.mAppType == 68) {
                    Collections.sort(this.mIsGameUnSelectedAppList, ALPHA_COMPARATOR);
                    Collections.sort(this.mUnSelectedAppList, ALPHA_COMPARATOR);
                    this.mIsGameUnSelectedAppList.addAll(this.mUnSelectedAppList);
                    this.mUnSelectedAppList = this.mIsGameUnSelectedAppList;
                } else {
                    Collections.sort(this.mUnSelectedAppList, ALPHA_COMPARATOR);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "some unknown error happened.");
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0039, code skipped:
            if (r0 == null) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:10:0x003b, code skipped:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:15:0x0058, code skipped:
            if (r0 == null) goto L_0x005b;
     */
    /* JADX WARNING: Missing block: B:16:0x005b, code skipped:
            mGameAppList = mGameAppArrayList;
     */
    /* JADX WARNING: Missing block: B:22:?, code skipped:
            return;
     */
    private void loadGameApp() {
        /*
        r8 = this;
        r0 = 0;
        r7 = "category_id = 7";
        r1 = mGameAppArrayList;
        r1 = r1.size();
        if (r1 > 0) goto L_0x0066;
    L_0x000b:
        r1 = r8.mContext;	 Catch:{ SQLiteException -> 0x0041 }
        r1 = r1.getContentResolver();	 Catch:{ SQLiteException -> 0x0041 }
        r2 = APP_CATEGORY_URI;	 Catch:{ SQLiteException -> 0x0041 }
        r3 = 0;
        r5 = 0;
        r6 = 0;
        r4 = r7;
        r1 = r1.query(r2, r3, r4, r5, r6);	 Catch:{ SQLiteException -> 0x0041 }
        r0 = r1;
        if (r0 == 0) goto L_0x0039;
    L_0x001e:
        r1 = r0.moveToFirst();	 Catch:{ SQLiteException -> 0x0041 }
        if (r1 == 0) goto L_0x0039;
    L_0x0024:
        r1 = "package_name";
        r1 = r0.getColumnIndex(r1);	 Catch:{ SQLiteException -> 0x0041 }
        r1 = r0.getString(r1);	 Catch:{ SQLiteException -> 0x0041 }
        r2 = mGameAppArrayList;	 Catch:{ SQLiteException -> 0x0041 }
        r2.add(r1);	 Catch:{ SQLiteException -> 0x0041 }
        r1 = r0.moveToNext();	 Catch:{ SQLiteException -> 0x0041 }
        if (r1 != 0) goto L_0x0024;
    L_0x0039:
        if (r0 == 0) goto L_0x005b;
    L_0x003b:
        r0.close();
        goto L_0x005b;
    L_0x003f:
        r1 = move-exception;
        goto L_0x0060;
    L_0x0041:
        r1 = move-exception;
        r2 = "AppLockerDataController";
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x003f }
        r3.<init>();	 Catch:{ all -> 0x003f }
        r4 = "ex ";
        r3.append(r4);	 Catch:{ all -> 0x003f }
        r3.append(r1);	 Catch:{ all -> 0x003f }
        r3 = r3.toString();	 Catch:{ all -> 0x003f }
        android.util.Log.e(r2, r3);	 Catch:{ all -> 0x003f }
        if (r0 == 0) goto L_0x005b;
    L_0x005a:
        goto L_0x003b;
    L_0x005b:
        r1 = mGameAppArrayList;
        mGameAppList = r1;
        goto L_0x0066;
    L_0x0060:
        if (r0 == 0) goto L_0x0065;
    L_0x0062:
        r0.close();
    L_0x0065:
        throw r1;
    L_0x0066:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.apploader.OPApplicationLoader.loadGameApp():void");
    }

    private boolean packageExcludeFilter(String packageName) {
        boolean z = true;
        if (this.mAppType != 63) {
            boolean z2 = "com.oneplus.deskclock".equals(packageName) || MARKET_PACKAGE_NAME.equals(packageName) || "com.android.settings".equals(packageName) || "com.google.android.googlequicksearchbox".equals(packageName) || "com.android.dialer".equals(packageName) || "com.android.contacts".equals(packageName) || WEATHER_PACKAGE_NAME.equals(packageName) || WEATHER_PACKAGE_NAME_NET.equals(packageName) || "com.google.android.calendar".equals(packageName) || "com.oneplus.calendar".equals(packageName) || GALLERY_PACKAGE_NAME.equals(packageName) || FILEMANAGER_PACKAGE_NAME.equals(packageName) || CALCULATOR_PACKAGE_NAME.equals(packageName) || CARD_PACKAGE_NAME.equals(packageName);
            boolean filter = z2;
            if (this.mAppType == 68) {
                z2 = filter || "com.android.mms".equals(packageName) || "com.oneplus.note".equals(packageName);
                filter = z2;
            }
            if (this.mAppType != 67) {
                return filter;
            }
            if (!(filter || SOUNDRECORDER_PACKAGE_NAME.equals(packageName) || CAMERA_PACKAGE_NAME.equals(packageName))) {
                z = false;
            }
            return z;
        }
        if (!(false || "com.oneplus.deskclock".equals(packageName) || "com.android.settings".equals(packageName) || "com.google.android.googlequicksearchbox".equals(packageName))) {
            z = false;
        }
        return z;
    }

    private boolean multiAppPackageExcludeFilter(Context context, String pkgName) {
        return OPOnlineConfigManager.getMultiAppWhiteList().contains(pkgName);
    }
}
