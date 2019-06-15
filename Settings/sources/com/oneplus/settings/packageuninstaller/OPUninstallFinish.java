package com.oneplus.settings.packageuninstaller;

import android.app.Notification.Action;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.IDevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.content.pm.UserInfo;
import android.graphics.drawable.Icon;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.oneplus.settings.utils.OPConstants;
import java.util.Iterator;
import java.util.List;

public class OPUninstallFinish extends BroadcastReceiver {
    static final String EXTRA_APP_LABEL = "com.android.packageinstaller.extra.APP_LABEL";
    static final String EXTRA_UNINSTALL_ID = "com.android.packageinstaller.extra.UNINSTALL_ID";
    private static final String LOG_TAG = OPUninstallFinish.class.getSimpleName();
    private static final String UNINSTALL_FAILURE_CHANNEL = "uninstall failure";

    public void onReceive(Context context, Intent intent) {
        RemoteException e;
        NotificationChannel uninstallFailureChannel;
        Context context2 = context;
        Intent intent2 = intent;
        int returnCode = intent2.getIntExtra("android.content.pm.extra.STATUS", 0);
        String str = LOG_TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Uninstall finished extras=");
        stringBuilder.append(intent.getExtras());
        Log.i(str, stringBuilder.toString());
        if (returnCode == -1) {
            context2.startActivity((Intent) intent2.getParcelableExtra("android.intent.extra.INTENT"));
            return;
        }
        int uninstallId = intent2.getIntExtra(EXTRA_UNINSTALL_ID, 0);
        ApplicationInfo appInfo = (ApplicationInfo) intent2.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        String appLabel = intent2.getStringExtra(EXTRA_APP_LABEL);
        boolean allUsers = intent2.getBooleanExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
        NotificationManager notificationManager = (NotificationManager) context2.getSystemService(NotificationManager.class);
        UserManager userManager = (UserManager) context2.getSystemService(UserManager.class);
        NotificationChannel uninstallFailureChannel2 = new NotificationChannel(UNINSTALL_FAILURE_CHANNEL, context2.getString(R.string.uninstall_failure_notification_channel), 3);
        notificationManager.createNotificationChannel(uninstallFailureChannel2);
        Builder uninstallFailedNotification = new Builder(context2, UNINSTALL_FAILURE_CHANNEL);
        if (returnCode != 0) {
            StringBuilder stringBuilder2;
            if (returnCode != 2) {
                str = LOG_TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Uninstall failed for ");
                stringBuilder2.append(appInfo.packageName);
                stringBuilder2.append(" with code ");
                stringBuilder2.append(returnCode);
                Log.d(str, stringBuilder2.toString());
            } else {
                int legacyStatus = intent2.getIntExtra("android.content.pm.extra.LEGACY_STATUS", 0);
                int i;
                String str2;
                StringBuilder stringBuilder3;
                if (legacyStatus == -4) {
                    IPackageManager packageManager = Stub.asInterface(ServiceManager.getService("package"));
                    List<UserInfo> users = userManager.getUsers();
                    uninstallFailureChannel2 = -10000;
                    int i2 = 0;
                    while (true) {
                        i = i2;
                        List<UserInfo> list;
                        if (i >= users.size()) {
                            list = users;
                            NotificationChannel notificationChannel = uninstallFailureChannel2;
                            break;
                        }
                        UserInfo user = (UserInfo) users.get(i);
                        int blockingUserId;
                        IPackageManager iPackageManager;
                        try {
                            list = users;
                            blockingUserId = uninstallFailureChannel2;
                            UserInfo users2 = user;
                            try {
                                if (packageManager.getBlockUninstallForUser(appInfo.packageName, users2.id)) {
                                    uninstallFailureChannel2 = users2.id;
                                    iPackageManager = packageManager;
                                    break;
                                }
                                iPackageManager = packageManager;
                                i2 = i + 1;
                                users = list;
                                uninstallFailureChannel2 = blockingUserId;
                                packageManager = iPackageManager;
                            } catch (RemoteException e2) {
                                e = e2;
                                iPackageManager = packageManager;
                                Log.e(LOG_TAG, "Failed to talk to package manager", e);
                                i2 = i + 1;
                                users = list;
                                uninstallFailureChannel2 = blockingUserId;
                                packageManager = iPackageManager;
                            }
                        } catch (RemoteException e3) {
                            e = e3;
                            list = users;
                            blockingUserId = uninstallFailureChannel2;
                            iPackageManager = packageManager;
                            Log.e(LOG_TAG, "Failed to talk to package manager", e);
                            i2 = i + 1;
                            users = list;
                            uninstallFailureChannel2 = blockingUserId;
                            packageManager = iPackageManager;
                        }
                    }
                    if (isProfileOfOrSame(userManager, UserHandle.myUserId(), uninstallFailureChannel2)) {
                        addDeviceManagerButton(context2, uninstallFailedNotification);
                    } else {
                        addManageUsersButton(context2, uninstallFailedNotification);
                    }
                    if (uninstallFailureChannel2 == -10000) {
                        str2 = LOG_TAG;
                        stringBuilder3 = new StringBuilder();
                        stringBuilder3.append("Uninstall failed for ");
                        stringBuilder3.append(appInfo.packageName);
                        stringBuilder3.append(" with code ");
                        stringBuilder3.append(returnCode);
                        stringBuilder3.append(" no blocking user");
                        Log.d(str2, stringBuilder3.toString());
                    } else if (uninstallFailureChannel2 == null) {
                        setBigText(uninstallFailedNotification, context2.getString(R.string.uninstall_blocked_device_owner));
                    } else if (allUsers) {
                        setBigText(uninstallFailedNotification, context2.getString(R.string.uninstall_all_blocked_profile_owner));
                    } else {
                        setBigText(uninstallFailedNotification, context2.getString(R.string.uninstall_blocked_profile_owner));
                    }
                } else if (legacyStatus != -2) {
                    str = LOG_TAG;
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Uninstall blocked for ");
                    stringBuilder2.append(appInfo.packageName);
                    stringBuilder2.append(" with legacy code ");
                    stringBuilder2.append(legacyStatus);
                    Log.d(str, stringBuilder2.toString());
                } else {
                    UserInfo otherBlockingUser;
                    IDevicePolicyManager dpm = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"));
                    i = UserHandle.myUserId();
                    Iterator user2 = userManager.getUsers().iterator();
                    while (user2.hasNext()) {
                        Iterator it = user2;
                        UserInfo user3 = (UserInfo) user2.next();
                        if (isProfileOfOrSame(userManager, i, user3.id)) {
                            user2 = it;
                        } else {
                            try {
                                uninstallFailureChannel = uninstallFailureChannel2;
                                try {
                                    if (dpm.packageHasActiveAdmins(appInfo.packageName, user3.id)) {
                                        otherBlockingUser = user3;
                                        break;
                                    }
                                } catch (RemoteException e4) {
                                    e = e4;
                                    Log.e(LOG_TAG, "Failed to talk to package manager", e);
                                    user2 = it;
                                    uninstallFailureChannel2 = uninstallFailureChannel;
                                }
                            } catch (RemoteException e5) {
                                e = e5;
                                uninstallFailureChannel = uninstallFailureChannel2;
                                Log.e(LOG_TAG, "Failed to talk to package manager", e);
                                user2 = it;
                                uninstallFailureChannel2 = uninstallFailureChannel;
                            }
                            user2 = it;
                            uninstallFailureChannel2 = uninstallFailureChannel;
                        }
                    }
                    otherBlockingUser = null;
                    if (otherBlockingUser == null) {
                        str2 = LOG_TAG;
                        uninstallFailureChannel2 = new StringBuilder();
                        uninstallFailureChannel2.append("Uninstall failed because ");
                        uninstallFailureChannel2.append(appInfo.packageName);
                        uninstallFailureChannel2.append(" is a device admin");
                        Log.d(str2, uninstallFailureChannel2.toString());
                        addDeviceManagerButton(context2, uninstallFailedNotification);
                        setBigText(uninstallFailedNotification, context2.getString(R.string.uninstall_failed_device_policy_manager));
                    } else {
                        str2 = LOG_TAG;
                        stringBuilder3 = new StringBuilder();
                        stringBuilder3.append("Uninstall failed because ");
                        stringBuilder3.append(appInfo.packageName);
                        stringBuilder3.append(" is a device admin of user ");
                        stringBuilder3.append(otherBlockingUser);
                        Log.d(str2, stringBuilder3.toString());
                        setBigText(uninstallFailedNotification, String.format(context2.getString(R.string.uninstall_failed_device_policy_manager_of_user), new Object[]{otherBlockingUser.name}));
                    }
                }
                uninstallFailedNotification.setContentTitle(context2.getString(R.string.uninstall_failed_app, new Object[]{appLabel}));
                uninstallFailedNotification.setOngoing(false);
                uninstallFailedNotification.setSmallIcon(R.drawable.ic_error);
                notificationManager.notify(uninstallId, uninstallFailedNotification.build());
                return;
            }
            uninstallFailedNotification.setContentTitle(context2.getString(R.string.uninstall_failed_app, new Object[]{appLabel}));
            uninstallFailedNotification.setOngoing(false);
            uninstallFailedNotification.setSmallIcon(R.drawable.ic_error);
            notificationManager.notify(uninstallId, uninstallFailedNotification.build());
            return;
        }
        notificationManager.cancel(uninstallId);
        notifyPackageRemoved(context2, appInfo.packageName);
        Toast.makeText(context2, context2.getString(R.string.uninstall_done_app, new Object[]{appLabel}), 1).show();
    }

    private void notifyPackageRemoved(Context context, String pkgName) {
        Intent intent = new Intent(OPConstants.ONEPLUS_ACTION_PACKAGE_REMOVED);
        intent.putExtra("package_name", pkgName);
        intent.setFlags(285212672);
        intent.setPackage("com.android.settings");
        context.sendBroadcast(intent);
    }

    private boolean isProfileOfOrSame(@NonNull UserManager userManager, int userId, int profileId) {
        boolean z = true;
        if (userId == profileId) {
            return true;
        }
        UserInfo parentUser = userManager.getProfileParent(profileId);
        if (parentUser == null || parentUser.id != userId) {
            z = false;
        }
        return z;
    }

    private void setBigText(@NonNull Builder builder, @NonNull CharSequence text) {
        builder.setStyle(new BigTextStyle().bigText(text));
    }

    private void addManageUsersButton(@NonNull Context context, @NonNull Builder builder) {
        Intent intent = new Intent("android.settings.USER_SETTINGS");
        intent.setFlags(1342177280);
        builder.addAction(new Action.Builder(Icon.createWithResource(context, R.drawable.ic_settings_multiuser), context.getString(R.string.manage_users), PendingIntent.getActivity(context, 0, intent, 134217728)).build());
    }

    private void addDeviceManagerButton(@NonNull Context context, @NonNull Builder builder) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$DeviceAdminSettingsActivity");
        intent.setFlags(1342177280);
        builder.addAction(new Action.Builder(Icon.createWithResource(context, R.drawable.ic_lock), context.getString(R.string.manage_device_administrators), PendingIntent.getActivity(context, 0, intent, 134217728)).build());
    }
}
