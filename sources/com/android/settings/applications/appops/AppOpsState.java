package com.android.settings.applications.appops;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.settings.R;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AppOpsState {
    public static final OpsTemplate[] ALL_TEMPLATES = new OpsTemplate[]{LOCATION_TEMPLATE, PERSONAL_TEMPLATE, MESSAGING_TEMPLATE, MEDIA_TEMPLATE, DEVICE_TEMPLATE, RUN_IN_BACKGROUND_TEMPLATE};
    static final boolean DEBUG = false;
    public static final OpsTemplate DEVICE_TEMPLATE = new OpsTemplate(new int[]{11, 25, 13, 23, 24, 40, 46, 47, 49, 50}, new boolean[]{false, true, true, true, true, true, false, false, false, false});
    public static final Comparator<AppOpEntry> LABEL_COMPARATOR = new Comparator<AppOpEntry>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(AppOpEntry object1, AppOpEntry object2) {
            return this.sCollator.compare(object1.getAppEntry().getLabel(), object2.getAppEntry().getLabel());
        }
    };
    public static final OpsTemplate LOCATION_TEMPLATE = new OpsTemplate(new int[]{0, 1, 2, 10, 12, 41, 42}, new boolean[]{true, true, false, false, false, false, false});
    public static final OpsTemplate MEDIA_TEMPLATE = new OpsTemplate(new int[]{3, 26, 27, 28, 31, 32, 33, 34, 35, 36, 37, 38, 39, 71, 44}, new boolean[]{false, true, true, false, false, false, false, false, false, false, false, false, false, false});
    public static final OpsTemplate MESSAGING_TEMPLATE = new OpsTemplate(new int[]{14, 16, 17, 18, 19, 15, 20, 21, 22}, new boolean[]{true, true, true, true, true, true, true, true, true});
    public static final OpsTemplate PERSONAL_TEMPLATE = new OpsTemplate(new int[]{4, 5, 6, 7, 8, 9, 29, 30}, new boolean[]{true, true, true, true, true, true, false, false});
    public static final Comparator<AppOpEntry> RECENCY_COMPARATOR = new Comparator<AppOpEntry>() {
        private final Collator sCollator = Collator.getInstance();

        public int compare(AppOpEntry object1, AppOpEntry object2) {
            int i = 1;
            if (object1.getSwitchOrder() != object2.getSwitchOrder()) {
                if (object1.getSwitchOrder() < object2.getSwitchOrder()) {
                    i = -1;
                }
                return i;
            } else if (object1.isRunning() != object2.isRunning()) {
                if (object1.isRunning()) {
                    i = -1;
                }
                return i;
            } else if (object1.getTime() == object2.getTime()) {
                return this.sCollator.compare(object1.getAppEntry().getLabel(), object2.getAppEntry().getLabel());
            } else {
                if (object1.getTime() > object2.getTime()) {
                    i = -1;
                }
                return i;
            }
        }
    };
    public static final OpsTemplate RUN_IN_BACKGROUND_TEMPLATE = new OpsTemplate(new int[]{64}, new boolean[]{false});
    static final String TAG = "AppOpsState";
    final AppOpsManager mAppOps;
    final Context mContext;
    final CharSequence[] mOpLabels;
    final CharSequence[] mOpSummaries;
    final PackageManager mPm;

    public static class AppEntry {
        private final File mApkFile;
        private Drawable mIcon;
        private final ApplicationInfo mInfo;
        private String mLabel;
        private boolean mMounted;
        private final SparseArray<AppOpEntry> mOpSwitches = new SparseArray();
        private final SparseArray<OpEntry> mOps = new SparseArray();
        private final AppOpsState mState;

        public AppEntry(AppOpsState state, ApplicationInfo info) {
            this.mState = state;
            this.mInfo = info;
            this.mApkFile = new File(info.sourceDir);
        }

        public void addOp(AppOpEntry entry, OpEntry op) {
            this.mOps.put(op.getOp(), op);
            this.mOpSwitches.put(AppOpsManager.opToSwitch(op.getOp()), entry);
        }

        public boolean hasOp(int op) {
            return this.mOps.indexOfKey(op) >= 0;
        }

        public AppOpEntry getOpSwitch(int op) {
            return (AppOpEntry) this.mOpSwitches.get(AppOpsManager.opToSwitch(op));
        }

        public ApplicationInfo getApplicationInfo() {
            return this.mInfo;
        }

        public String getLabel() {
            return this.mLabel;
        }

        public Drawable getIcon() {
            if (this.mIcon == null) {
                if (this.mApkFile.exists()) {
                    this.mIcon = this.mInfo.loadIcon(this.mState.mPm);
                    return this.mIcon;
                }
                this.mMounted = false;
            } else if (this.mMounted) {
                return this.mIcon;
            } else {
                if (this.mApkFile.exists()) {
                    this.mMounted = true;
                    this.mIcon = this.mInfo.loadIcon(this.mState.mPm);
                    return this.mIcon;
                }
            }
            return this.mState.mContext.getDrawable(17301651);
        }

        public String toString() {
            return this.mLabel;
        }

        /* Access modifiers changed, original: 0000 */
        public void loadLabel(Context context) {
            if (this.mLabel != null && this.mMounted) {
                return;
            }
            if (this.mApkFile.exists()) {
                this.mMounted = true;
                CharSequence label = this.mInfo.loadLabel(context.getPackageManager());
                this.mLabel = label != null ? label.toString() : this.mInfo.packageName;
                return;
            }
            this.mMounted = false;
            this.mLabel = this.mInfo.packageName;
        }
    }

    public static class AppOpEntry {
        private final AppEntry mApp;
        private final ArrayList<OpEntry> mOps = new ArrayList();
        private int mOverriddenPrimaryMode = -1;
        private final PackageOps mPkgOps;
        private final ArrayList<OpEntry> mSwitchOps = new ArrayList();
        private final int mSwitchOrder;

        public AppOpEntry(PackageOps pkg, OpEntry op, AppEntry app, int switchOrder) {
            this.mPkgOps = pkg;
            this.mApp = app;
            this.mSwitchOrder = switchOrder;
            this.mApp.addOp(this, op);
            this.mOps.add(op);
            this.mSwitchOps.add(op);
        }

        private static void addOp(ArrayList<OpEntry> list, OpEntry op) {
            for (int i = 0; i < list.size(); i++) {
                OpEntry pos = (OpEntry) list.get(i);
                if (pos.isRunning() != op.isRunning()) {
                    if (op.isRunning()) {
                        list.add(i, op);
                        return;
                    }
                } else if (pos.getTime() < op.getTime()) {
                    list.add(i, op);
                    return;
                }
            }
            list.add(op);
        }

        public void addOp(OpEntry op) {
            this.mApp.addOp(this, op);
            addOp(this.mOps, op);
            if (this.mApp.getOpSwitch(AppOpsManager.opToSwitch(op.getOp())) == null) {
                addOp(this.mSwitchOps, op);
            }
        }

        public AppEntry getAppEntry() {
            return this.mApp;
        }

        public int getSwitchOrder() {
            return this.mSwitchOrder;
        }

        public PackageOps getPackageOps() {
            return this.mPkgOps;
        }

        public int getNumOpEntry() {
            return this.mOps.size();
        }

        public OpEntry getOpEntry(int pos) {
            return (OpEntry) this.mOps.get(pos);
        }

        public int getPrimaryOpMode() {
            return this.mOverriddenPrimaryMode >= 0 ? this.mOverriddenPrimaryMode : ((OpEntry) this.mOps.get(0)).getMode();
        }

        public void overridePrimaryOpMode(int mode) {
            this.mOverriddenPrimaryMode = mode;
        }

        private CharSequence getCombinedText(ArrayList<OpEntry> ops, CharSequence[] items) {
            int i = 0;
            if (ops.size() == 1) {
                return items[((OpEntry) ops.get(0)).getOp()];
            }
            StringBuilder builder = new StringBuilder();
            while (i < ops.size()) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(items[((OpEntry) ops.get(i)).getOp()]);
                i++;
            }
            return builder.toString();
        }

        public CharSequence getSummaryText(AppOpsState state) {
            return getCombinedText(this.mOps, state.mOpSummaries);
        }

        public CharSequence getSwitchText(AppOpsState state) {
            if (this.mSwitchOps.size() > 0) {
                return getCombinedText(this.mSwitchOps, state.mOpLabels);
            }
            return getCombinedText(this.mOps, state.mOpLabels);
        }

        public CharSequence getTimeText(Resources res, boolean showEmptyText) {
            if (isRunning()) {
                return res.getText(R.string.app_ops_running);
            }
            if (getTime() > 0) {
                return DateUtils.getRelativeTimeSpanString(getTime(), System.currentTimeMillis(), 60000, 262144);
            }
            return showEmptyText ? res.getText(R.string.app_ops_never_used) : "";
        }

        public boolean isRunning() {
            return ((OpEntry) this.mOps.get(0)).isRunning();
        }

        public long getTime() {
            return ((OpEntry) this.mOps.get(0)).getTime();
        }

        public String toString() {
            return this.mApp.getLabel();
        }
    }

    public static class OpsTemplate implements Parcelable {
        public static final Creator<OpsTemplate> CREATOR = new Creator<OpsTemplate>() {
            public OpsTemplate createFromParcel(Parcel source) {
                return new OpsTemplate(source);
            }

            public OpsTemplate[] newArray(int size) {
                return new OpsTemplate[size];
            }
        };
        public final int[] ops;
        public final boolean[] showPerms;

        public OpsTemplate(int[] _ops, boolean[] _showPerms) {
            this.ops = _ops;
            this.showPerms = _showPerms;
        }

        OpsTemplate(Parcel src) {
            this.ops = src.createIntArray();
            this.showPerms = src.createBooleanArray();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeIntArray(this.ops);
            dest.writeBooleanArray(this.showPerms);
        }
    }

    public AppOpsState(Context context) {
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mPm = context.getPackageManager();
        this.mOpSummaries = context.getResources().getTextArray(R.array.app_ops_summaries);
        this.mOpLabels = context.getResources().getTextArray(R.array.app_ops_labels);
    }

    private void addOp(List<AppOpEntry> entries, PackageOps pkgOps, AppEntry appEntry, OpEntry opEntry, boolean allowMerge, int switchOrder) {
        AppOpEntry last;
        if (allowMerge && entries.size() > 0) {
            boolean entryExe = true;
            last = (AppOpEntry) entries.get(entries.size() - 1);
            if (last.getAppEntry() == appEntry) {
                boolean lastExe = last.getTime() != 0;
                if (opEntry.getTime() == 0) {
                    entryExe = false;
                }
                if (lastExe == entryExe) {
                    last.addOp(opEntry);
                    return;
                }
            }
        }
        last = appEntry.getOpSwitch(opEntry.getOp());
        if (last != null) {
            last.addOp(opEntry);
        } else {
            entries.add(new AppOpEntry(pkgOps, opEntry, appEntry, switchOrder));
        }
    }

    public AppOpsManager getAppOpsManager() {
        return this.mAppOps;
    }

    public List<AppOpEntry> buildState(OpsTemplate tpl) {
        return buildState(tpl, 0, null, RECENCY_COMPARATOR);
    }

    private AppEntry getAppEntry(Context context, HashMap<String, AppEntry> appEntries, String packageName, ApplicationInfo appInfo) {
        AppEntry appEntry = (AppEntry) appEntries.get(packageName);
        if (appEntry == null) {
            if (appInfo == null) {
                try {
                    appInfo = this.mPm.getApplicationInfo(packageName, 4194816);
                } catch (NameNotFoundException e) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unable to find info for package ");
                    stringBuilder.append(packageName);
                    Log.w(str, stringBuilder.toString());
                    return null;
                }
            }
            appEntry = new AppEntry(this, appInfo);
            appEntry.loadLabel(context);
            appEntries.put(packageName, appEntry);
        }
        return appEntry;
    }

    public List<AppOpEntry> buildState(OpsTemplate tpl, int uid, String packageName) {
        return buildState(tpl, uid, packageName, RECENCY_COMPARATOR);
    }

    public List<AppOpEntry> buildState(OpsTemplate tpl, int uid, String packageName, Comparator<AppOpEntry> comparator) {
        int i;
        List<PackageOps> pkgs;
        int i2;
        PackageOps packageOps;
        int j;
        int i3;
        int j2;
        boolean z;
        int[] opToOrder;
        List<PackageInfo> apps;
        OpsTemplate opsTemplate = tpl;
        String str = packageName;
        Context context = this.mContext;
        HashMap<String, AppEntry> appEntries = new HashMap();
        List entries = new ArrayList();
        ArrayList<String> perms = new ArrayList();
        ArrayList<Integer> permOps = new ArrayList();
        int[] opToOrder2 = new int[85];
        int i4 = 0;
        for (i = 0; i < opsTemplate.ops.length; i++) {
            if (opsTemplate.showPerms[i]) {
                String perm = AppOpsManager.opToPermission(opsTemplate.ops[i]);
                if (!(perm == null || perms.contains(perm))) {
                    perms.add(perm);
                    permOps.add(Integer.valueOf(opsTemplate.ops[i]));
                    opToOrder2[opsTemplate.ops[i]] = i;
                }
            }
        }
        if (str != null) {
            pkgs = this.mAppOps.getOpsForPackage(uid, str, opsTemplate.ops);
        } else {
            i2 = uid;
            pkgs = this.mAppOps.getPackagesForOps(opsTemplate.ops);
        }
        List<PackageOps> pkgs2 = pkgs;
        if (pkgs2 != null) {
            i = 0;
            while (i < pkgs2.size()) {
                packageOps = (PackageOps) pkgs2.get(i);
                AppEntry appEntry = getAppEntry(context, appEntries, packageOps.getPackageName(), null);
                if (appEntry != null) {
                    j = i4;
                    while (true) {
                        int j3 = j;
                        if (j3 >= packageOps.getOps().size()) {
                            break;
                        }
                        OpEntry opEntry = (OpEntry) packageOps.getOps().get(j3);
                        boolean z2 = str == null ? true : i4;
                        if (str == null) {
                            i3 = i4;
                        } else {
                            i3 = opToOrder2[opEntry.getOp()];
                        }
                        j2 = j3;
                        PackageOps pkgOps = packageOps;
                        List<PackageOps> pkgs3 = pkgs2;
                        boolean z3 = i4;
                        z = z2;
                        opToOrder = opToOrder2;
                        addOp(entries, packageOps, appEntry, opEntry, z, i3);
                        j = j2 + 1;
                        i2 = uid;
                        z = z3;
                        opToOrder2 = opToOrder;
                        pkgs2 = pkgs3;
                        opsTemplate = tpl;
                    }
                }
                i++;
                i2 = uid;
                i4 = i4;
                opToOrder2 = opToOrder2;
                pkgs2 = pkgs2;
                opsTemplate = tpl;
            }
        }
        int i5 = i4;
        opToOrder = opToOrder2;
        if (str != null) {
            apps = new ArrayList();
            try {
                apps.add(this.mPm.getPackageInfo(str, 4096));
            } catch (NameNotFoundException e) {
            }
        } else {
            String[] permsArray = new String[perms.size()];
            perms.toArray(permsArray);
            apps = this.mPm.getPackagesHoldingPermissions(permsArray, i5);
        }
        List<PackageInfo> apps2 = apps;
        j = i5;
        while (true) {
            int i6 = j;
            if (i6 < apps2.size()) {
                PackageInfo appInfo = (PackageInfo) apps2.get(i6);
                AppEntry appEntry2 = getAppEntry(context, appEntries, appInfo.packageName, appInfo.applicationInfo);
                if (appEntry2 != null && appInfo.requestedPermissions != null) {
                    packageOps = null;
                    List<OpEntry> dummyOps = null;
                    j = i5;
                    while (true) {
                        int j4 = j;
                        if (j4 >= appInfo.requestedPermissions.length) {
                            break;
                        }
                        List<PackageInfo> apps3;
                        if (appInfo.requestedPermissionsFlags == null || (appInfo.requestedPermissionsFlags[j4] & 2) != 0) {
                            PackageOps pkgOps2;
                            j = i5;
                            while (j < perms.size()) {
                                int k;
                                AppEntry appEntry3;
                                PackageInfo appInfo2;
                                int i7;
                                apps3 = apps2;
                                if (((String) perms.get(j)).equals(appInfo.requestedPermissions[j4]) != null && appEntry2.hasOp(((Integer) permOps.get(j)).intValue()) == null) {
                                    if (dummyOps == null) {
                                        dummyOps = new ArrayList();
                                        pkgOps2 = new PackageOps(appInfo.packageName, appInfo.applicationInfo.uid, dummyOps);
                                        apps2 = dummyOps;
                                    } else {
                                        pkgOps2 = packageOps;
                                        apps2 = dummyOps;
                                    }
                                    OpEntry opEntry2 = new OpEntry(((Integer) permOps.get(j)).intValue(), 0, 0, 0, 0, -1, null);
                                    apps2.add(opEntry2);
                                    boolean z4 = str == null;
                                    if (str == null) {
                                        i3 = 0;
                                    } else {
                                        i3 = opToOrder[opEntry2.getOp()];
                                    }
                                    k = j;
                                    packageOps = pkgOps2;
                                    j2 = j4;
                                    appEntry3 = appEntry2;
                                    appInfo2 = appInfo;
                                    z = z4;
                                    i7 = i6;
                                    addOp(entries, packageOps, appEntry2, opEntry2, z, i3);
                                    dummyOps = apps2;
                                } else {
                                    k = j;
                                    j2 = j4;
                                    appEntry3 = appEntry2;
                                    appInfo2 = appInfo;
                                    i7 = i6;
                                }
                                j = k + 1;
                                i6 = i7;
                                j4 = j2;
                                appEntry2 = appEntry3;
                                apps2 = apps3;
                                appInfo = appInfo2;
                            }
                            apps3 = apps2;
                            pkgOps2 = packageOps;
                        } else {
                            apps3 = apps2;
                        }
                        j = j4 + 1;
                        i6 = i6;
                        appEntry2 = appEntry2;
                        apps2 = apps3;
                        appInfo = appInfo;
                        i5 = 0;
                    }
                }
                j = i6 + 1;
                apps2 = apps2;
                i5 = 0;
            } else {
                Collections.sort(entries, comparator);
                return entries;
            }
        }
    }
}
