package com.oneplus.settings.packageuninstaller;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.VersionedPackage;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;
import com.android.settings.R;

public class UninstallerActivity extends Activity {
    private static final String TAG = "UninstallerActivity";
    private static final String UNINSTALLING_CHANNEL = "uninstalling";
    private DialogInfo mDialogInfo;
    private String mPackageName;

    public static class DialogInfo {
        public ActivityInfo activityInfo;
        public boolean allUsers;
        public ApplicationInfo appInfo;
        public IBinder callback;
        public UserHandle user;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(null);
        try {
            int callingUid = ActivityManager.getService().getLaunchedFromUid(getActivityToken());
            String callingPackage = getPackageNameForUid(callingUid);
            String str;
            StringBuilder stringBuilder;
            if (callingPackage == null) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Package not found for originating uid ");
                stringBuilder.append(callingUid);
                Log.e(str, stringBuilder.toString());
                setResult(1);
                finish();
            } else if (((AppOpsManager) getSystemService("appops")).noteOpNoThrow("android:request_delete_packages", callingUid, callingPackage) != 0) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Install from uid ");
                stringBuilder2.append(callingUid);
                stringBuilder2.append(" disallowed by AppOps");
                Log.e(str2, stringBuilder2.toString());
                setResult(1);
                finish();
            } else if (PackageUtil.getMaxTargetSdkVersionForUid(this, callingUid) < 28 || AppGlobals.getPackageManager().checkUidPermission("android.permission.REQUEST_DELETE_PACKAGES", callingUid) == 0 || AppGlobals.getPackageManager().checkUidPermission("android.permission.DELETE_PACKAGES", callingUid) == 0) {
                Intent intent = getIntent();
                Uri packageUri = intent.getData();
                if (packageUri == null) {
                    Log.e(TAG, "No package URI in intent");
                    showAppNotFound();
                    return;
                }
                this.mPackageName = packageUri.getEncodedSchemeSpecificPart();
                if (this.mPackageName == null) {
                    callingPackage = TAG;
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("Invalid package name in URI: ");
                    stringBuilder3.append(packageUri);
                    Log.e(callingPackage, stringBuilder3.toString());
                    showAppNotFound();
                    return;
                }
                IPackageManager pm = Stub.asInterface(ServiceManager.getService("package"));
                this.mDialogInfo = new DialogInfo();
                this.mDialogInfo.allUsers = intent.getBooleanExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
                if (!this.mDialogInfo.allUsers || UserManager.get(this).isAdminUser()) {
                    this.mDialogInfo.user = (UserHandle) intent.getParcelableExtra("android.intent.extra.USER");
                    if (this.mDialogInfo.user == null) {
                        this.mDialogInfo.user = Process.myUserHandle();
                    } else if (!((UserManager) getSystemService("user")).getUserProfiles().contains(this.mDialogInfo.user)) {
                        String str3 = TAG;
                        StringBuilder stringBuilder4 = new StringBuilder();
                        stringBuilder4.append("User ");
                        stringBuilder4.append(Process.myUserHandle());
                        stringBuilder4.append(" can't request uninstall for user ");
                        stringBuilder4.append(this.mDialogInfo.user);
                        Log.e(str3, stringBuilder4.toString());
                        showUserIsNotAllowed();
                        return;
                    }
                    this.mDialogInfo.callback = intent.getIBinderExtra("android.content.pm.extra.CALLBACK");
                    try {
                        this.mDialogInfo.appInfo = pm.getApplicationInfo(this.mPackageName, 4194304, this.mDialogInfo.user.getIdentifier());
                    } catch (RemoteException e) {
                        Log.e(TAG, "Unable to get packageName. Package manager is dead?");
                    }
                    if (this.mDialogInfo.appInfo == null) {
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Invalid packageName: ");
                        stringBuilder.append(this.mPackageName);
                        Log.e(str, stringBuilder.toString());
                        showAppNotFound();
                        return;
                    }
                    str = packageUri.getFragment();
                    if (str != null) {
                        try {
                            this.mDialogInfo.activityInfo = pm.getActivityInfo(new ComponentName(this.mPackageName, str), 0, this.mDialogInfo.user.getIdentifier());
                        } catch (RemoteException e2) {
                            Log.e(TAG, "Unable to get className. Package manager is dead?");
                        }
                    }
                    showConfirmationDialog();
                    return;
                }
                Log.e(TAG, "Only admin user can request uninstall for all users");
                showUserIsNotAllowed();
            } else {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Uid ");
                stringBuilder.append(callingUid);
                stringBuilder.append(" does not have ");
                stringBuilder.append("android.permission.REQUEST_DELETE_PACKAGES");
                stringBuilder.append(" or ");
                stringBuilder.append("android.permission.DELETE_PACKAGES");
                Log.e(str, stringBuilder.toString());
                setResult(1);
                finish();
            }
        } catch (RemoteException e3) {
            Log.e(TAG, "Could not determine the launching uid.");
            setResult(1);
            finish();
        }
    }

    public DialogInfo getDialogInfo() {
        return this.mDialogInfo;
    }

    private void showConfirmationDialog() {
        if (isTv()) {
            showContentFragment(new UninstallAlertFragment(), 0, 0);
        } else {
            showDialogFragment(new UninstallAlertDialogFragment(), 0, 0);
        }
    }

    private void showAppNotFound() {
        if (isTv()) {
            showContentFragment(new ErrorFragment(), R.string.app_not_found_dlg_title, R.string.app_not_found_dlg_text);
        } else {
            showDialogFragment(new ErrorDialogFragment(), R.string.app_not_found_dlg_title, R.string.app_not_found_dlg_text);
        }
    }

    private void showUserIsNotAllowed() {
        if (isTv()) {
            showContentFragment(new ErrorFragment(), R.string.user_is_not_allowed_dlg_title, R.string.user_is_not_allowed_dlg_text);
        } else {
            showDialogFragment(new ErrorDialogFragment(), 0, R.string.user_is_not_allowed_dlg_text);
        }
    }

    private void showGenericError() {
        if (isTv()) {
            showContentFragment(new ErrorFragment(), R.string.generic_error_dlg_title, R.string.generic_error_dlg_text);
        } else {
            showDialogFragment(new ErrorDialogFragment(), 0, R.string.generic_error_dlg_text);
        }
    }

    private boolean isTv() {
        return (getResources().getConfiguration().uiMode & 15) == 4;
    }

    private void showContentFragment(@NonNull Fragment fragment, @StringRes int title, @StringRes int text) {
        Bundle args = new Bundle();
        args.putInt("com.android.packageinstaller.arg.title", title);
        args.putInt("com.android.packageinstaller.arg.text", text);
        fragment.setArguments(args);
        getFragmentManager().beginTransaction().replace(16908290, fragment).commit();
    }

    private void showDialogFragment(@NonNull DialogFragment fragment, @StringRes int title, @StringRes int text) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        Bundle args = new Bundle();
        if (title != 0) {
            args.putInt("com.android.packageinstaller.arg.title", title);
        }
        args.putInt("com.android.packageinstaller.arg.text", text);
        fragment.setArguments(args);
        fragment.show(ft, "dialog");
    }

    public void startUninstallProgress() {
        boolean z = false;
        boolean returnResult = getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false);
        CharSequence label = this.mDialogInfo.appInfo.loadSafeLabel(getPackageManager());
        if (isTv()) {
            Intent newIntent = new Intent("android.intent.action.VIEW");
            newIntent.putExtra("android.intent.extra.USER", this.mDialogInfo.user);
            newIntent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", this.mDialogInfo.allUsers);
            newIntent.putExtra("android.content.pm.extra.CALLBACK", this.mDialogInfo.callback);
            newIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO, this.mDialogInfo.appInfo);
            if (returnResult) {
                newIntent.putExtra("android.intent.extra.RETURN_RESULT", true);
                newIntent.addFlags(33554432);
            }
            newIntent.setClass(this, UninstallAppProgress.class);
            startActivity(newIntent);
        } else {
            try {
                int uninstallId = UninstallEventReceiver.getNewId(this);
                Intent broadcastIntent = new Intent(this, OPUninstallFinish.class);
                broadcastIntent.setFlags(268435456);
                broadcastIntent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", this.mDialogInfo.allUsers);
                broadcastIntent.putExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO, this.mDialogInfo.appInfo);
                broadcastIntent.putExtra("com.android.packageinstaller.extra.APP_LABEL", label);
                broadcastIntent.putExtra("com.android.packageinstaller.extra.UNINSTALL_ID", uninstallId);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uninstallId, broadcastIntent, 134217728);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(new NotificationChannel(UNINSTALLING_CHANNEL, getString(R.string.uninstalling_notification_channel), 1));
                notificationManager.notify(uninstallId, new Builder(this, UNINSTALLING_CHANNEL).setSmallIcon(R.drawable.ic_remove_24dp).setProgress(0, 1, true).setContentTitle(getString(R.string.uninstalling, new Object[]{label})).setOngoing(true).build());
                try {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Uninstalling extras=");
                    stringBuilder.append(broadcastIntent.getExtras());
                    Log.i(str, stringBuilder.toString());
                    IPackageInstaller packageInstaller = ActivityThread.getPackageManager().getPackageInstaller();
                    VersionedPackage versionedPackage = new VersionedPackage(this.mDialogInfo.appInfo.packageName, -1);
                    String packageName = getPackageName();
                    if (this.mDialogInfo.allUsers) {
                        z = true;
                    }
                    packageInstaller.uninstall(versionedPackage, packageName, z, pendingIntent.getIntentSender(), this.mDialogInfo.user.getIdentifier());
                } catch (Exception e) {
                    notificationManager.cancel(uninstallId);
                    Log.e(TAG, "Cannot start uninstall", e);
                    showGenericError();
                }
            } catch (OutOfIdsException e2) {
                OutOfIdsException outOfIdsException = e2;
                showGenericError();
            }
        }
    }

    public void dispatchAborted() {
        if (this.mDialogInfo != null && this.mDialogInfo.callback != null) {
            try {
                IPackageDeleteObserver2.Stub.asInterface(this.mDialogInfo.callback).onPackageDeleted(this.mPackageName, -5, "Cancelled by user");
            } catch (RemoteException e) {
            }
        }
    }

    private String getPackageNameForUid(int sourceUid) {
        String[] packagesForUid = getPackageManager().getPackagesForUid(sourceUid);
        if (packagesForUid == null) {
            return null;
        }
        return packagesForUid[0];
    }
}
