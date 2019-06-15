package com.android.settings.notification;

import android.app.INotificationManager;
import android.app.INotificationManager.Stub;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.graphics.drawable.Drawable;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.service.notification.NotifyingApp;
import android.support.v4.app.NotificationManagerCompat;
import android.util.IconDrawableFactory;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationBackend {
    private static final String TAG = "NotificationBackend";
    static INotificationManager sINM = Stub.asInterface(ServiceManager.getService("notification"));

    static class Row {
        public String section;

        Row() {
        }
    }

    public static class AppRow extends Row {
        public boolean banned;
        public int blockedChannelCount;
        public int channelCount;
        public boolean first;
        public Drawable icon;
        public CharSequence label;
        public String lockedChannelId;
        public boolean lockedImportance;
        public String pkg;
        public Intent settingsIntent;
        public boolean showBadge;
        public boolean systemApp;
        public int uid;
        public int userId;
    }

    public AppRow loadAppRow(Context context, PackageManager pm, ApplicationInfo app) {
        AppRow row = new AppRow();
        row.pkg = app.packageName;
        row.uid = app.uid;
        try {
            row.label = app.loadLabel(pm);
        } catch (Throwable t) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error loading application label for ");
            stringBuilder.append(row.pkg);
            Log.e(str, stringBuilder.toString(), t);
            row.label = row.pkg;
        }
        row.icon = IconDrawableFactory.newInstance(context).getBadgedIcon(app);
        row.banned = getNotificationsBanned(row.pkg, row.uid);
        row.showBadge = canShowBadge(row.pkg, row.uid);
        row.userId = UserHandle.getUserId(row.uid);
        row.blockedChannelCount = getBlockedChannelCount(row.pkg, row.uid);
        row.channelCount = getChannelCount(row.pkg, row.uid);
        return row;
    }

    public AppRow loadAppRow(Context context, PackageManager pm, PackageInfo app) {
        AppRow row = loadAppRow(context, pm, app.applicationInfo);
        recordCanBeBlocked(context, pm, app, row);
        return row;
    }

    /* Access modifiers changed, original: 0000 */
    public void recordCanBeBlocked(Context context, PackageManager pm, PackageInfo app, AppRow row) {
        row.systemApp = Utils.isSystemPackage(context.getResources(), pm, app);
        String[] nonBlockablePkgs = context.getResources().getStringArray(17236024);
        String[] oneplusnonBlockablePkgs = context.getResources().getStringArray(84017172);
        String[] allnonBlockablePkgs = (String[]) Arrays.copyOf(nonBlockablePkgs, nonBlockablePkgs.length + oneplusnonBlockablePkgs.length);
        System.arraycopy(oneplusnonBlockablePkgs, 0, allnonBlockablePkgs, nonBlockablePkgs.length, oneplusnonBlockablePkgs.length);
        markAppRowWithBlockables(allnonBlockablePkgs, row, app.packageName);
    }

    @VisibleForTesting
    static void markAppRowWithBlockables(String[] nonBlockablePkgs, AppRow row, String packageName) {
        if (nonBlockablePkgs != null) {
            int N = nonBlockablePkgs.length;
            for (int i = 0; i < N; i++) {
                String pkg = nonBlockablePkgs[i];
                if (pkg != null) {
                    if (pkg.contains(":")) {
                        if (packageName.equals(pkg.split(":", 2)[0])) {
                            row.lockedChannelId = pkg.split(":", 2)[1];
                        }
                    } else if (packageName.equals(nonBlockablePkgs[i])) {
                        row.lockedImportance = true;
                        row.systemApp = true;
                    }
                }
            }
        }
    }

    public boolean isSystemApp(Context context, ApplicationInfo app) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(app.packageName, 64);
            AppRow row = new AppRow();
            recordCanBeBlocked(context, context.getPackageManager(), info, row);
            return row.systemApp;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getNotificationsBanned(String pkg, int uid) {
        boolean z = false;
        try {
            if (!sINM.areNotificationsEnabledForPackage(pkg, uid)) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public boolean setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) {
        try {
            if (onlyHasDefaultChannel(pkg, uid)) {
                NotificationChannel defaultChannel = getChannel(pkg, uid, "miscellaneous");
                defaultChannel.setImportance(enabled ? NotificationManagerCompat.IMPORTANCE_UNSPECIFIED : 0);
                updateChannel(pkg, uid, defaultChannel);
            }
            sINM.setNotificationsEnabledForPackage(pkg, uid, enabled);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public boolean canShowBadge(String pkg, int uid) {
        try {
            return sINM.canShowBadge(pkg, uid);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public boolean setShowBadge(String pkg, int uid, boolean showBadge) {
        try {
            sINM.setShowBadge(pkg, uid, showBadge);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public NotificationChannel getChannel(String pkg, int uid, String channelId) {
        if (channelId == null) {
            return null;
        }
        try {
            return sINM.getNotificationChannelForPackage(pkg, uid, channelId, true);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return null;
        }
    }

    public NotificationChannelGroup getGroup(String pkg, int uid, String groupId) {
        if (groupId == null) {
            return null;
        }
        try {
            return sINM.getNotificationChannelGroupForPackage(groupId, pkg, uid);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return null;
        }
    }

    public ParceledListSlice<NotificationChannelGroup> getGroups(String pkg, int uid) {
        try {
            return sINM.getNotificationChannelGroupsForPackage(pkg, uid, false);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return ParceledListSlice.emptyList();
        }
    }

    public void updateChannel(String pkg, int uid, NotificationChannel channel) {
        try {
            sINM.updateNotificationChannelForPackage(pkg, uid, channel);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
        }
    }

    public void updateChannelGroup(String pkg, int uid, NotificationChannelGroup group) {
        try {
            sINM.updateNotificationChannelGroupForPackage(pkg, uid, group);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
        }
    }

    public int getDeletedChannelCount(String pkg, int uid) {
        try {
            return sINM.getDeletedChannelCount(pkg, uid);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return 0;
        }
    }

    public int getBlockedChannelCount(String pkg, int uid) {
        try {
            return sINM.getBlockedChannelCount(pkg, uid);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return 0;
        }
    }

    public boolean onlyHasDefaultChannel(String pkg, int uid) {
        try {
            return sINM.onlyHasDefaultChannel(pkg, uid);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return false;
        }
    }

    public int getChannelCount(String pkg, int uid) {
        try {
            return sINM.getNumNotificationChannelsForPackage(pkg, uid, false);
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return 0;
        }
    }

    public List<NotifyingApp> getRecentApps() {
        try {
            return sINM.getRecentNotifyingAppsForUser(UserHandle.myUserId()).getList();
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return new ArrayList();
        }
    }

    public boolean setLedEnabled(String pkg, boolean ledDisabled) {
        try {
            sINM.setNotificationLedStatus(pkg, ledDisabled);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Error calling setLedDisabled", e);
            return false;
        }
    }

    public boolean getLedEnabled(String pkg) {
        try {
            return sINM.isNotificationLedEnabled(pkg);
        } catch (Exception e) {
            Log.w(TAG, "Error calling setLedDisabled", e);
            return false;
        }
    }

    public int getBlockedAppCount() {
        try {
            return sINM.getBlockedAppCount(UserHandle.myUserId());
        } catch (Exception e) {
            Log.w(TAG, "Error calling NoMan", e);
            return 0;
        }
    }
}
