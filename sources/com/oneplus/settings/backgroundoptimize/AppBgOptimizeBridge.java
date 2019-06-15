package com.oneplus.settings.backgroundoptimize;

import android.content.Context;
import android.content.pm.PackageManager;
import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.ArrayList;
import java.util.Map;

public class AppBgOptimizeBridge extends AppStateBaseBridge {
    public static final AppFilter FILTER_APP_BG_All = new AppFilter() {
        public void init() {
        }

        /* JADX WARNING: Missing block: B:11:0x0028, code skipped:
            return false;
     */
        public boolean filterApp(com.android.settingslib.applications.ApplicationsState.AppEntry r4) {
            /*
            r3 = this;
            r0 = 0;
            if (r4 == 0) goto L_0x0028;
        L_0x0003:
            r1 = r4.info;
            r1 = r1.uid;
            r1 = android.os.UserHandle.getUserId(r1);
            r2 = 999; // 0x3e7 float:1.4E-42 double:4.936E-321;
            if (r1 != r2) goto L_0x0010;
        L_0x000f:
            goto L_0x0028;
        L_0x0010:
            r1 = com.oneplus.settings.utils.OPUtils.bgServiceApplist;
            r2 = r4.info;
            r2 = r2.packageName;
            r1 = r1.contains(r2);
            if (r1 == 0) goto L_0x001d;
        L_0x001c:
            return r0;
        L_0x001d:
            r1 = r4.info;
            r1 = r1.flags;
            r2 = 1;
            r1 = r1 & r2;
            if (r1 != 0) goto L_0x0027;
        L_0x0025:
            r0 = r2;
        L_0x0027:
            return r0;
        L_0x0028:
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.backgroundoptimize.AppBgOptimizeBridge$AnonymousClass1.filterApp(com.android.settingslib.applications.ApplicationsState$AppEntry):boolean");
        }
    };
    public static final AppFilter FILTER_APP_BG_NOT_OPTIMIZE = new AppFilter() {
        public void init() {
        }

        /* JADX WARNING: Missing block: B:15:0x0033, code skipped:
            return false;
     */
        public boolean filterApp(com.android.settingslib.applications.ApplicationsState.AppEntry r5) {
            /*
            r4 = this;
            r0 = 0;
            if (r5 == 0) goto L_0x0033;
        L_0x0003:
            r1 = r5.extraInfo;
            if (r1 == 0) goto L_0x0033;
        L_0x0007:
            r1 = r5.extraInfo;
            r1 = r1 instanceof com.oneplus.settings.backgroundoptimize.AppControlMode;
            if (r1 == 0) goto L_0x0033;
        L_0x000d:
            r1 = r5.info;
            r1 = r1.uid;
            r1 = android.os.UserHandle.getUserId(r1);
            r2 = 999; // 0x3e7 float:1.4E-42 double:4.936E-321;
            if (r1 != r2) goto L_0x001a;
        L_0x0019:
            goto L_0x0033;
        L_0x001a:
            r1 = com.oneplus.settings.utils.OPUtils.bgServiceApplist;
            r2 = r5.info;
            r2 = r2.packageName;
            r1 = r1.contains(r2);
            if (r1 == 0) goto L_0x0027;
        L_0x0026:
            return r0;
        L_0x0027:
            r1 = r5.extraInfo;
            r1 = (com.oneplus.settings.backgroundoptimize.AppControlMode) r1;
            r2 = r1.value;
            r3 = 1;
            if (r3 != r2) goto L_0x0032;
        L_0x0030:
            r0 = r3;
        L_0x0032:
            return r0;
        L_0x0033:
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.backgroundoptimize.AppBgOptimizeBridge$AnonymousClass2.filterApp(com.android.settingslib.applications.ApplicationsState$AppEntry):boolean");
        }
    };
    private final Context mContext;
    private final BgOActivityManager mManager = BgOActivityManager.getInstance(this.mContext);
    private final PackageManager mPm = this.mContext.getPackageManager();

    public AppBgOptimizeBridge(Context context, ApplicationsState appState, Callback callback) {
        super(appState, callback);
        this.mContext = context;
    }

    /* Access modifiers changed, original: protected */
    public void loadAllExtraInfo() {
        ArrayList<AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        int i = 0;
        Map<String, AppControlMode> map = this.mManager.getAllAppControlModesMap(0);
        while (i < N) {
            AppEntry app = (AppEntry) apps.get(i);
            app.extraInfo = map.get(app.info.packageName);
            i++;
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = new AppControlMode(pkg, 0, this.mManager.getAppControlMode(pkg, 0));
    }
}
