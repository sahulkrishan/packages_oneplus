package com.android.settings.applications.manageapplications;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.NetworkPolicyManager;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telecom.DefaultDialerManager;
import android.util.Log;
import android.util.OpFeatures;
import android.webkit.IWebViewUpdateService;
import com.android.internal.telephony.SmsApplication;
import com.android.settings.R;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.defaultapp.DefaultAppLogic;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class ResetAppsHelper implements OnClickListener, OnDismissListener {
    public static final String DEFAULT_BROWSER_HYDROGEN = "com.android.browser";
    public static final String DEFAULT_BROWSER_OXYGEN = "com.android.chrome";
    private static final String EXTRA_RESET_DIALOG = "resetDialog";
    private static final String PKG_DEFAULT_DIALER = "com.android.dialer";
    private static final String PKG_DEFAULT_LAUNCHER = "net.oneplus.launcher/net.oneplus.launcher.Launcher";
    private static final String PKG_DEFAULT_MMS = "com.android.mms";
    private final AppOpsManager mAom;
    private final Context mContext;
    private final IPackageManager mIPm = Stub.asInterface(ServiceManager.getService("package"));
    private final INotificationManager mNm = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
    private final NetworkPolicyManager mNpm;
    private final PackageManager mPm;
    private AlertDialog mResetDialog;
    private final IWebViewUpdateService mWvus = IWebViewUpdateService.Stub.asInterface(ServiceManager.getService("webviewupdate"));

    public ResetAppsHelper(Context context) {
        this.mContext = context;
        this.mPm = context.getPackageManager();
        this.mNpm = NetworkPolicyManager.from(context);
        this.mAom = (AppOpsManager) context.getSystemService("appops");
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getBoolean(EXTRA_RESET_DIALOG)) {
            buildResetDialog();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mResetDialog != null) {
            outState.putBoolean(EXTRA_RESET_DIALOG, true);
        }
    }

    public void stop() {
        if (this.mResetDialog != null) {
            this.mResetDialog.dismiss();
            this.mResetDialog = null;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void buildResetDialog() {
        if (this.mResetDialog == null) {
            this.mResetDialog = new Builder(this.mContext).setTitle(R.string.reset_app_preferences_title).setMessage(R.string.reset_app_preferences_desc).setPositiveButton(R.string.reset_app_preferences_button, this).setNegativeButton(R.string.cancel, null).setOnDismissListener(this).show();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mResetDialog == dialog) {
            this.mResetDialog = null;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mResetDialog == dialog) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    List<ApplicationInfo> apps = ResetAppsHelper.this.mPm.getInstalledApplications(512);
                    for (int i = 0; i < apps.size(); i++) {
                        ApplicationInfo app = (ApplicationInfo) apps.get(i);
                        try {
                            NotificationChannel channel = ResetAppsHelper.this.mNm.getNotificationChannelForPackage(app.packageName, app.uid, "miscellaneous", true);
                            if (channel != null && (ResetAppsHelper.this.mNm.onlyHasDefaultChannel(app.packageName, app.uid) || "miscellaneous".equals(channel.getId()))) {
                                channel.setImportance(3);
                                ResetAppsHelper.this.mNm.updateNotificationChannelForPackage(app.packageName, app.uid, channel);
                            }
                            ResetAppsHelper.this.mNm.setNotificationsEnabledForPackage(app.packageName, app.uid, true);
                            if (OpFeatures.isSupport(new int[]{28}) && ResetAppsHelper.this.mAom.checkOp(69, app.uid, app.packageName) == 0) {
                                int parallelUid = UserHandle.getUid(999, app.uid);
                                NotificationChannel parallelChannel = ResetAppsHelper.this.mNm.getNotificationChannelForPackage(app.packageName, parallelUid, "miscellaneous", true);
                                if (parallelChannel != null && (ResetAppsHelper.this.mNm.onlyHasDefaultChannel(app.packageName, parallelUid) || "miscellaneous".equals(parallelChannel.getId()))) {
                                    parallelChannel.setImportance(3);
                                    ResetAppsHelper.this.mNm.updateNotificationChannelForPackage(app.packageName, parallelUid, parallelChannel);
                                }
                                ResetAppsHelper.this.mNm.setNotificationsEnabledForPackage(app.packageName, parallelUid, true);
                            }
                        } catch (RemoteException e) {
                        }
                        if (!(app.enabled || ResetAppsHelper.this.mPm.getApplicationEnabledSetting(app.packageName) != 3 || ResetAppsHelper.this.isNonEnableableFallback(app.packageName))) {
                            ResetAppsHelper.this.mPm.setApplicationEnabledSetting(app.packageName, 0, 1);
                        }
                    }
                    try {
                        ResetAppsHelper.this.mIPm.resetApplicationPreferences(UserHandle.myUserId());
                        if (OpFeatures.isSupport(new int[]{28}) && UserHandle.myUserId() == 0) {
                            ResetAppsHelper.this.mIPm.resetApplicationPreferences(999);
                        }
                    } catch (RemoteException e2) {
                    }
                    ResetAppsHelper.this.mAom.resetAllModes();
                    int[] restrictedUids = ResetAppsHelper.this.mNpm.getUidsWithPolicy(1);
                    int currentUserId = ActivityManager.getCurrentUser();
                    for (int uid : restrictedUids) {
                        if (UserHandle.getUserId(uid) == currentUserId) {
                            ResetAppsHelper.this.mNpm.setUidPolicy(uid, 0);
                        }
                        if (OpFeatures.isSupport(new int[]{28}) && currentUserId == 0 && UserHandle.getUserId(uid) == 999) {
                            ResetAppsHelper.this.mNpm.setUidPolicy(uid, 0);
                        }
                    }
                    if (VERSION.IS_CTA_BUILD) {
                        Intent intent = new Intent("com.oneplus.cta.permission.RESET");
                        intent.setClassName("com.oneplus.permissionutil", "com.oneplus.permissionutil.ResetReceiver");
                        ResetAppsHelper.this.mContext.sendBroadcast(intent);
                    }
                    ResetAppsHelper.this.resetDefaultApps(ResetAppsHelper.this.mContext);
                }
            });
        }
    }

    private boolean isNonEnableableFallback(String packageName) {
        try {
            return this.mWvus.isFallbackPackage(packageName);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetDefaultApps(Context context) {
        try {
            int userId = UserHandle.myUserId();
            PackageManager mPm = context.getPackageManager();
            DefaultAppLogic.getInstance(SettingsBaseApplication.mApplication).initDefaultAppSettings(true);
            resetDefaultBrowser(userId, mPm);
            SmsApplication.setDefaultApplication("com.android.mms", context);
            DefaultDialerManager.setDefaultDialerApplication(context, "com.android.dialer", userId);
            IntentFilter mHomeFilter = new IntentFilter("android.intent.action.MAIN");
            mHomeFilter.addCategory("android.intent.category.HOME");
            mHomeFilter.addCategory("android.intent.category.DEFAULT");
            ArrayList<ComponentName> mAllHomeComponents = new ArrayList();
            ArrayList<ResolveInfo> homeActivities = new ArrayList();
            ComponentName currentDefaultHome = mPm.getHomeActivities(homeActivities);
            for (int i = 0; i < homeActivities.size(); i++) {
                ActivityInfo info = ((ResolveInfo) homeActivities.get(i)).activityInfo;
                mAllHomeComponents.add(new ComponentName(info.packageName, info.name));
            }
            mPm.replacePreferredActivity(mHomeFilter, 1048576, (ComponentName[]) mAllHomeComponents.toArray(new ComponentName[0]), ComponentName.unflattenFromString(PKG_DEFAULT_LAUNCHER));
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("reset default app exception.");
            stringBuilder.append(e.getMessage());
            Log.e("ResetAppsHelper", stringBuilder.toString());
        }
    }

    public static String resetDefaultBrowser(int userId, PackageManager mPm) {
        String defaultBrowser = OPUtils.isO2() ? DEFAULT_BROWSER_OXYGEN : DEFAULT_BROWSER_HYDROGEN;
        boolean result = mPm.setDefaultBrowserPackageNameAsUser(defaultBrowser, userId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("resetDefaultBrowser result:");
        stringBuilder.append(result);
        stringBuilder.append(",defaultBrowser:");
        stringBuilder.append(defaultBrowser);
        Log.d("ResetAppsHelper", stringBuilder.toString());
        if (result) {
            return defaultBrowser;
        }
        return null;
    }
}
