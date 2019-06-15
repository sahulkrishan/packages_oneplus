package com.android.settingslib.net;

import android.content.Context;
import android.util.SparseArray;

public class UidDetailProvider {
    public static final int OTHER_USER_RANGE_START = -2000;
    private static final String TAG = "DataUsage";
    private final Context mContext;
    private final SparseArray<UidDetail> mUidDetailCache = new SparseArray();

    public static int buildKeyForUser(int userHandle) {
        return -2000 - userHandle;
    }

    public static boolean isKeyForUser(int key) {
        return key <= OTHER_USER_RANGE_START;
    }

    public static int getUserIdForKey(int key) {
        return -2000 - key;
    }

    public UidDetailProvider(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void clearCache() {
        synchronized (this.mUidDetailCache) {
            this.mUidDetailCache.clear();
        }
    }

    public UidDetail getUidDetail(int uid, boolean blocking) {
        UidDetail detail;
        synchronized (this.mUidDetailCache) {
            detail = (UidDetail) this.mUidDetailCache.get(uid);
        }
        if (detail != null) {
            return detail;
        }
        if (!blocking) {
            return null;
        }
        detail = buildUidDetail(uid);
        synchronized (this.mUidDetailCache) {
            this.mUidDetailCache.put(uid, detail);
        }
        return detail;
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x0141  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0141  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0141  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0141  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0141  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0141  */
    private com.android.settingslib.net.UidDetail buildUidDetail(int r18) {
        /*
        r17 = this;
        r1 = r17;
        r2 = r18;
        r0 = r1.mContext;
        r3 = r0.getResources();
        r0 = r1.mContext;
        r4 = r0.getPackageManager();
        r0 = new com.android.settingslib.net.UidDetail;
        r0.<init>();
        r5 = r0;
        r0 = r4.getNameForUid(r2);
        r5.label = r0;
        r0 = r4.getDefaultActivityIcon();
        r5.icon = r0;
        r0 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        if (r2 == r0) goto L_0x017b;
    L_0x0026:
        switch(r2) {
            case -5: goto L_0x0160;
            case -4: goto L_0x0148;
            default: goto L_0x0029;
        };
    L_0x0029:
        r0 = r1.mContext;
        r6 = "user";
        r0 = r0.getSystemService(r6);
        r6 = r0;
        r6 = (android.os.UserManager) r6;
        r0 = isKeyForUser(r18);
        if (r0 == 0) goto L_0x0055;
    L_0x003a:
        r0 = getUserIdForKey(r18);
        r7 = r6.getUserInfo(r0);
        if (r7 == 0) goto L_0x0055;
    L_0x0044:
        r8 = r1.mContext;
        r8 = com.android.settingslib.Utils.getUserLabel(r8, r7);
        r5.label = r8;
        r8 = r1.mContext;
        r8 = com.android.settingslib.Utils.getUserIcon(r8, r6, r7);
        r5.icon = r8;
        return r5;
    L_0x0055:
        r7 = r4.getPackagesForUid(r2);
        r0 = 0;
        if (r7 == 0) goto L_0x005e;
    L_0x005c:
        r8 = r7.length;
        goto L_0x005f;
    L_0x005e:
        r8 = r0;
    L_0x005f:
        r9 = android.os.UserHandle.getUserId(r18);	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r10 = new android.os.UserHandle;	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r10.<init>(r9);	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r11 = android.app.AppGlobals.getPackageManager();	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r12 = 1;
        if (r8 != r12) goto L_0x009e;
    L_0x006f:
        r12 = r7[r0];	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r0 = r11.getApplicationInfo(r12, r0, r9);	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        if (r0 == 0) goto L_0x0090;
    L_0x0077:
        r12 = r0.loadLabel(r4);	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r12 = r12.toString();	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r5.label = r12;	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r12 = r0.loadIcon(r4);	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r13 = new android.os.UserHandle;	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r13.<init>(r9);	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r12 = r6.getBadgedIconForUser(r12, r13);	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r5.icon = r12;	 Catch:{ NameNotFoundException -> 0x0099, RemoteException -> 0x0094 }
        r16 = r7;
        goto L_0x00f8;
    L_0x0094:
        r0 = move-exception;
        r16 = r7;
        goto L_0x0108;
    L_0x0099:
        r0 = move-exception;
        r16 = r7;
        goto L_0x0122;
    L_0x009e:
        if (r8 <= r12) goto L_0x00f6;
    L_0x00a0:
        r12 = new java.lang.CharSequence[r8];	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r5.detailLabels = r12;	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r12 = new java.lang.CharSequence[r8];	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r5.detailContentDescriptions = r12;	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r12 = r0;
    L_0x00a9:
        if (r12 >= r8) goto L_0x00f6;
    L_0x00ab:
        r13 = r7[r12];	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r14 = r4.getPackageInfo(r13, r0);	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r15 = r11.getApplicationInfo(r13, r0, r9);	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        if (r15 == 0) goto L_0x00ee;
    L_0x00b7:
        r0 = r5.detailLabels;	 Catch:{ NameNotFoundException -> 0x011f, RemoteException -> 0x0105 }
        r16 = r7;
        r7 = r15.loadLabel(r4);	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r7 = r7.toString();	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0[r12] = r7;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0 = r5.detailContentDescriptions;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r7 = r5.detailLabels;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r7 = r7[r12];	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r7 = r6.getBadgedLabelForUser(r7, r10);	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0[r12] = r7;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0 = r14.sharedUserLabel;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        if (r0 == 0) goto L_0x00f0;
    L_0x00d5:
        r0 = r14.sharedUserLabel;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r7 = r14.applicationInfo;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0 = r4.getText(r13, r0, r7);	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0 = r0.toString();	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r5.label = r0;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0 = r15.loadIcon(r4);	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0 = r6.getBadgedIconForUser(r0, r10);	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r5.icon = r0;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        goto L_0x00f0;
    L_0x00ee:
        r16 = r7;
    L_0x00f0:
        r12 = r12 + 1;
        r7 = r16;
        r0 = 0;
        goto L_0x00a9;
    L_0x00f6:
        r16 = r7;
    L_0x00f8:
        r0 = r5.label;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r0 = r6.getBadgedLabelForUser(r0, r10);	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        r5.contentDescription = r0;	 Catch:{ NameNotFoundException -> 0x0103, RemoteException -> 0x0101 }
        goto L_0x0138;
    L_0x0101:
        r0 = move-exception;
        goto L_0x0108;
    L_0x0103:
        r0 = move-exception;
        goto L_0x0122;
    L_0x0105:
        r0 = move-exception;
        r16 = r7;
    L_0x0108:
        r7 = "DataUsage";
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Error while building UI detail for uid ";
        r9.append(r10);
        r9.append(r2);
        r9 = r9.toString();
        android.util.Log.w(r7, r9, r0);
        goto L_0x0139;
    L_0x011f:
        r0 = move-exception;
        r16 = r7;
    L_0x0122:
        r7 = "DataUsage";
        r9 = new java.lang.StringBuilder;
        r9.<init>();
        r10 = "Error while building UI detail for uid ";
        r9.append(r10);
        r9.append(r2);
        r9 = r9.toString();
        android.util.Log.w(r7, r9, r0);
    L_0x0139:
        r0 = r5.label;
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 == 0) goto L_0x0147;
    L_0x0141:
        r0 = java.lang.Integer.toString(r18);
        r5.label = r0;
    L_0x0147:
        return r5;
    L_0x0148:
        r0 = android.os.UserManager.supportsMultipleUsers();
        if (r0 == 0) goto L_0x0151;
    L_0x014e:
        r0 = com.android.settingslib.R.string.data_usage_uninstalled_apps_users;
        goto L_0x0153;
    L_0x0151:
        r0 = com.android.settingslib.R.string.data_usage_uninstalled_apps;
    L_0x0153:
        r0 = r3.getString(r0);
        r5.label = r0;
        r0 = r4.getDefaultActivityIcon();
        r5.icon = r0;
        return r5;
    L_0x0160:
        r0 = r1.mContext;
        r6 = "connectivity";
        r0 = r0.getSystemService(r6);
        r0 = (android.net.ConnectivityManager) r0;
        r6 = com.android.settingslib.Utils.getTetheringLabel(r0);
        r6 = r3.getString(r6);
        r5.label = r6;
        r6 = r4.getDefaultActivityIcon();
        r5.icon = r6;
        return r5;
    L_0x017b:
        r0 = com.android.settingslib.R.string.process_kernel_label;
        r0 = r3.getString(r0);
        r5.label = r0;
        r0 = r4.getDefaultActivityIcon();
        r5.icon = r0;
        return r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.net.UidDetailProvider.buildUidDetail(int):com.android.settingslib.net.UidDetail");
    }
}
