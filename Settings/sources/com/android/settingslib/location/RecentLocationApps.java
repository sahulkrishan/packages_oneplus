package com.android.settingslib.location;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.util.IconDrawableFactory;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecentLocationApps {
    @VisibleForTesting
    static final String ANDROID_SYSTEM_PACKAGE_NAME = "android";
    @VisibleForTesting
    static final int[] LOCATION_OPS = new int[]{41, 42};
    private static final long RECENT_TIME_INTERVAL_MILLIS = 86400000;
    private static final String TAG = RecentLocationApps.class.getSimpleName();
    private final Context mContext;
    private final IconDrawableFactory mDrawableFactory;
    private final PackageManager mPackageManager;

    public static class Request {
        public final CharSequence contentDescription;
        public final Drawable icon;
        public final boolean isHighBattery;
        public final CharSequence label;
        public final String packageName;
        public final long requestFinishTime;
        public final int uid;
        public final UserHandle userHandle;

        /* synthetic */ Request(String x0, int x1, UserHandle x2, Drawable x3, CharSequence x4, boolean x5, CharSequence x6, long x7, AnonymousClass1 x8) {
            this(x0, x1, x2, x3, x4, x5, x6, x7);
        }

        private Request(String packageName, UserHandle userHandle, Drawable icon, CharSequence label, boolean isHighBattery, CharSequence contentDescription, long requestFinishTime) {
            this.packageName = packageName;
            this.userHandle = userHandle;
            this.icon = icon;
            this.label = label;
            this.isHighBattery = isHighBattery;
            this.contentDescription = contentDescription;
            this.requestFinishTime = requestFinishTime;
            this.uid = -1;
        }

        private Request(String packageName, int uid, UserHandle userHandle, Drawable icon, CharSequence label, boolean isHighBattery, CharSequence contentDescription, long requestFinishTime) {
            this.packageName = packageName;
            this.userHandle = userHandle;
            this.icon = icon;
            this.label = label;
            this.isHighBattery = isHighBattery;
            this.contentDescription = contentDescription;
            this.requestFinishTime = requestFinishTime;
            this.uid = uid;
        }
    }

    public RecentLocationApps(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mDrawableFactory = IconDrawableFactory.newInstance(context);
    }

    public List<Request> getAppList() {
        List<PackageOps> appOps = ((AppOpsManager) this.mContext.getSystemService("appops")).getPackagesForOps(LOCATION_OPS);
        int appOpsCount = appOps != null ? appOps.size() : 0;
        ArrayList<Request> requests = new ArrayList(appOpsCount);
        long now = System.currentTimeMillis();
        List<UserHandle> profiles = ((UserManager) this.mContext.getSystemService("user")).getUserProfiles();
        for (int i = 0; i < appOpsCount; i++) {
            PackageOps ops = (PackageOps) appOps.get(i);
            String packageName = ops.getPackageName();
            int uid = ops.getUid();
            int userId = UserHandle.getUserId(uid);
            boolean isAndroidOs = uid == 1000 && ANDROID_SYSTEM_PACKAGE_NAME.equals(packageName);
            if (!(isAndroidOs || !profiles.contains(new UserHandle(userId)) || (userId == 999 && isSystemApplication(this.mPackageManager, packageName)))) {
                Request request = getRequestFromOps(now, ops);
                if (request != null) {
                    requests.add(request);
                }
            }
        }
        return requests;
    }

    private boolean isSystemApplication(PackageManager packageManager, String packageName) {
        boolean z = false;
        if (packageManager == null || packageName == null || packageName.length() == 0) {
            return false;
        }
        try {
            ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
            if (app != null && (app.flags & 1) > 0) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Request> getAppListSorted() {
        List<Request> requests = getAppList();
        Collections.sort(requests, Collections.reverseOrder(new Comparator<Request>() {
            public int compare(Request request1, Request request2) {
                return Long.compare(request1.requestFinishTime, request2.requestFinishTime);
            }
        }));
        return requests;
    }

    private Request getRequestFromOps(long now, PackageOps ops) {
        int userId;
        int i;
        String packageName = ops.getPackageName();
        long recentLocationCutoffTime = now - 86400000;
        boolean highBattery = false;
        boolean normalBattery = false;
        long locationRequestFinishTime = 0;
        for (OpEntry entry : ops.getOps()) {
            if (entry.isRunning() || entry.getTime() >= recentLocationCutoffTime) {
                locationRequestFinishTime = entry.getTime() + ((long) entry.getDuration());
                switch (entry.getOp()) {
                    case 41:
                        normalBattery = true;
                        break;
                    case 42:
                        highBattery = true;
                        break;
                    default:
                        break;
                }
            }
        }
        String str;
        StringBuilder stringBuilder;
        if (highBattery || normalBattery) {
            int uid = ops.getUid();
            int userId2 = UserHandle.getUserId(uid);
            Request request = null;
            try {
                ApplicationInfo appInfo = this.mPackageManager.getApplicationInfoAsUser(packageName, 128, userId2);
                if (appInfo == null) {
                    try {
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Null application info retrieved for package ");
                        stringBuilder.append(packageName);
                        stringBuilder.append(", userId ");
                        stringBuilder.append(userId2);
                        Log.w(str, stringBuilder.toString());
                        return null;
                    } catch (NameNotFoundException e) {
                        userId = userId2;
                        i = uid;
                        str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("package name not found for ");
                        stringBuilder.append(packageName);
                        stringBuilder.append(", userId ");
                        stringBuilder.append(userId);
                        Log.w(str, stringBuilder.toString());
                        return request;
                    }
                }
                UserHandle userHandle = new UserHandle(userId2);
                Drawable icon = this.mDrawableFactory.getBadgedIcon(appInfo, userId2);
                CharSequence appLabel = this.mPackageManager.getApplicationLabel(appInfo);
                CharSequence badgedAppLabel = this.mPackageManager.getUserBadgedLabel(appLabel, userHandle);
                if (appLabel.toString().contentEquals(badgedAppLabel)) {
                    badgedAppLabel = null;
                }
                CharSequence badgedAppLabel2 = badgedAppLabel;
                Request request2 = request2;
                userId = userId2;
                try {
                    request = new Request(packageName, uid, userHandle, icon, appLabel, highBattery, badgedAppLabel2, locationRequestFinishTime, null);
                } catch (NameNotFoundException e2) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("package name not found for ");
                    stringBuilder.append(packageName);
                    stringBuilder.append(", userId ");
                    stringBuilder.append(userId);
                    Log.w(str, stringBuilder.toString());
                    return request;
                }
                return request;
            } catch (NameNotFoundException e3) {
                userId = userId2;
                i = uid;
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("package name not found for ");
                stringBuilder.append(packageName);
                stringBuilder.append(", userId ");
                stringBuilder.append(userId);
                Log.w(str, stringBuilder.toString());
                return request;
            }
        }
        if (Log.isLoggable(TAG, 2)) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append(packageName);
            stringBuilder.append(" hadn't used location within the time interval.");
            Log.v(str, stringBuilder.toString());
        }
        return null;
    }
}
