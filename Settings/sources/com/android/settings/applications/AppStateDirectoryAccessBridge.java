package com.android.settings.applications;

import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.Set;

public class AppStateDirectoryAccessBridge extends AppStateBaseBridge {
    static final boolean DEBUG = true;
    public static final AppFilter FILTER_APP_HAS_DIRECTORY_ACCESS = new AppFilter() {
        private Set<String> mPackages;

        public void init() {
            throw new UnsupportedOperationException("Need to call constructor that takes context");
        }

        /* JADX WARNING: Missing block: B:27:0x00b0, code skipped:
            if (r2 != null) goto L_0x00b2;
     */
        /* JADX WARNING: Missing block: B:28:0x00b2, code skipped:
            if (r0 != null) goto L_0x00b4;
     */
        /* JADX WARNING: Missing block: B:30:?, code skipped:
            r2.close();
     */
        /* JADX WARNING: Missing block: B:31:0x00b8, code skipped:
            r4 = move-exception;
     */
        /* JADX WARNING: Missing block: B:32:0x00b9, code skipped:
            r0.addSuppressed(r4);
     */
        /* JADX WARNING: Missing block: B:33:0x00bd, code skipped:
            r2.close();
     */
        public void init(android.content.Context r8) {
            /*
            r7 = this;
            r0 = 0;
            r7.mPackages = r0;
            r1 = new android.net.Uri$Builder;
            r1.<init>();
            r2 = "content";
            r1 = r1.scheme(r2);
            r2 = "com.android.documentsui.scopedAccess";
            r1 = r1.authority(r2);
            r2 = "packages";
            r1 = r1.appendPath(r2);
            r2 = "*";
            r1 = r1.appendPath(r2);
            r1 = r1.build();
            r2 = r8.getContentResolver();
            r3 = android.os.storage.StorageVolume.ScopedAccessProviderContract.TABLE_PACKAGES_COLUMNS;
            r2 = r2.query(r1, r3, r0, r0);
            if (r2 != 0) goto L_0x0050;
        L_0x0030:
            r3 = "DirectoryAccessBridge";
            r4 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x004e }
            r4.<init>();	 Catch:{ Throwable -> 0x004e }
            r5 = "Didn't get cursor for ";
            r4.append(r5);	 Catch:{ Throwable -> 0x004e }
            r4.append(r1);	 Catch:{ Throwable -> 0x004e }
            r4 = r4.toString();	 Catch:{ Throwable -> 0x004e }
            android.util.Log.w(r3, r4);	 Catch:{ Throwable -> 0x004e }
            if (r2 == 0) goto L_0x004b;
        L_0x0048:
            r2.close();
        L_0x004b:
            return;
        L_0x004c:
            r3 = move-exception;
            goto L_0x00b0;
        L_0x004e:
            r0 = move-exception;
            goto L_0x00af;
        L_0x0050:
            r3 = r2.getCount();	 Catch:{ Throwable -> 0x004e }
            if (r3 != 0) goto L_0x0079;
        L_0x0056:
            r4 = "DirectoryAccessBridge";
            r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x004e }
            r5.<init>();	 Catch:{ Throwable -> 0x004e }
            r6 = "No packages anymore (was ";
            r5.append(r6);	 Catch:{ Throwable -> 0x004e }
            r6 = r7.mPackages;	 Catch:{ Throwable -> 0x004e }
            r5.append(r6);	 Catch:{ Throwable -> 0x004e }
            r6 = ")";
            r5.append(r6);	 Catch:{ Throwable -> 0x004e }
            r5 = r5.toString();	 Catch:{ Throwable -> 0x004e }
            android.util.Log.d(r4, r5);	 Catch:{ Throwable -> 0x004e }
            if (r2 == 0) goto L_0x0078;
        L_0x0075:
            r2.close();
        L_0x0078:
            return;
        L_0x0079:
            r4 = new android.util.ArraySet;	 Catch:{ Throwable -> 0x004e }
            r4.<init>(r3);	 Catch:{ Throwable -> 0x004e }
            r7.mPackages = r4;	 Catch:{ Throwable -> 0x004e }
        L_0x0080:
            r4 = r2.moveToNext();	 Catch:{ Throwable -> 0x004e }
            if (r4 == 0) goto L_0x0091;
        L_0x0086:
            r4 = r7.mPackages;	 Catch:{ Throwable -> 0x004e }
            r5 = 0;
            r5 = r2.getString(r5);	 Catch:{ Throwable -> 0x004e }
            r4.add(r5);	 Catch:{ Throwable -> 0x004e }
            goto L_0x0080;
        L_0x0091:
            r4 = "DirectoryAccessBridge";
            r5 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x004e }
            r5.<init>();	 Catch:{ Throwable -> 0x004e }
            r6 = "init(): ";
            r5.append(r6);	 Catch:{ Throwable -> 0x004e }
            r6 = r7.mPackages;	 Catch:{ Throwable -> 0x004e }
            r5.append(r6);	 Catch:{ Throwable -> 0x004e }
            r5 = r5.toString();	 Catch:{ Throwable -> 0x004e }
            android.util.Log.d(r4, r5);	 Catch:{ Throwable -> 0x004e }
            if (r2 == 0) goto L_0x00ae;
        L_0x00ab:
            r2.close();
        L_0x00ae:
            return;
        L_0x00af:
            throw r0;	 Catch:{ all -> 0x004c }
        L_0x00b0:
            if (r2 == 0) goto L_0x00c0;
        L_0x00b2:
            if (r0 == 0) goto L_0x00bd;
        L_0x00b4:
            r2.close();	 Catch:{ Throwable -> 0x00b8 }
            goto L_0x00c0;
        L_0x00b8:
            r4 = move-exception;
            r0.addSuppressed(r4);
            goto L_0x00c0;
        L_0x00bd:
            r2.close();
        L_0x00c0:
            throw r3;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.AppStateDirectoryAccessBridge$AnonymousClass1.init(android.content.Context):void");
        }

        public boolean filterApp(AppEntry info) {
            return this.mPackages != null && this.mPackages.contains(info.info.packageName);
        }
    };
    private static final String TAG = "DirectoryAccessBridge";
    static final boolean VERBOSE = true;

    public AppStateDirectoryAccessBridge(ApplicationsState appState, Callback callback) {
        super(appState, callback);
    }

    /* Access modifiers changed, original: protected */
    public void loadAllExtraInfo() {
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String pkg, int uid) {
    }
}
