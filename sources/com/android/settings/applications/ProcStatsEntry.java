package com.android.settings.applications;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.app.procstats.ProcessState;
import com.android.internal.app.procstats.ProcessStats.ProcessDataCollection;
import com.android.internal.app.procstats.ServiceState;
import java.util.ArrayList;
import java.util.List;

public final class ProcStatsEntry implements Parcelable {
    public static final Creator<ProcStatsEntry> CREATOR = new Creator<ProcStatsEntry>() {
        public ProcStatsEntry createFromParcel(Parcel in) {
            return new ProcStatsEntry(in);
        }

        public ProcStatsEntry[] newArray(int size) {
            return new ProcStatsEntry[size];
        }
    };
    private static boolean DEBUG = false;
    private static final String TAG = "ProcStatsEntry";
    final long mAvgBgMem;
    final long mAvgRunMem;
    String mBestTargetPackage;
    final long mBgDuration;
    final double mBgWeight;
    public CharSequence mLabel;
    final long mMaxBgMem;
    final long mMaxRunMem;
    final String mName;
    final String mPackage;
    final ArrayList<String> mPackages = new ArrayList();
    final long mRunDuration;
    final double mRunWeight;
    ArrayMap<String, ArrayList<Service>> mServices = new ArrayMap(1);
    final int mUid;

    public static final class Service implements Parcelable {
        public static final Creator<Service> CREATOR = new Creator<Service>() {
            public Service createFromParcel(Parcel in) {
                return new Service(in);
            }

            public Service[] newArray(int size) {
                return new Service[size];
            }
        };
        final long mDuration;
        final String mName;
        final String mPackage;
        final String mProcess;

        public Service(ServiceState service) {
            this.mPackage = service.getPackage();
            this.mName = service.getName();
            this.mProcess = service.getProcessName();
            this.mDuration = service.dumpTime(null, null, 0, -1, 0, 0);
        }

        public Service(Parcel in) {
            this.mPackage = in.readString();
            this.mName = in.readString();
            this.mProcess = in.readString();
            this.mDuration = in.readLong();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mPackage);
            dest.writeString(this.mName);
            dest.writeString(this.mProcess);
            dest.writeLong(this.mDuration);
        }
    }

    public ProcStatsEntry(ProcessState proc, String packageName, ProcessDataCollection tmpBgTotals, ProcessDataCollection tmpRunTotals, boolean useUss) {
        proc.computeProcessData(tmpBgTotals, 0);
        proc.computeProcessData(tmpRunTotals, 0);
        this.mPackage = proc.getPackage();
        this.mUid = proc.getUid();
        this.mName = proc.getName();
        this.mPackages.add(packageName);
        this.mBgDuration = tmpBgTotals.totalTime;
        this.mAvgBgMem = useUss ? tmpBgTotals.avgUss : tmpBgTotals.avgPss;
        this.mMaxBgMem = useUss ? tmpBgTotals.maxUss : tmpBgTotals.maxPss;
        this.mBgWeight = ((double) this.mAvgBgMem) * ((double) this.mBgDuration);
        this.mRunDuration = tmpRunTotals.totalTime;
        this.mAvgRunMem = useUss ? tmpRunTotals.avgUss : tmpRunTotals.avgPss;
        this.mMaxRunMem = useUss ? tmpRunTotals.maxUss : tmpRunTotals.maxPss;
        this.mRunWeight = ((double) this.mAvgRunMem) * ((double) this.mRunDuration);
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("New proc entry ");
            stringBuilder.append(proc.getName());
            stringBuilder.append(": dur=");
            stringBuilder.append(this.mBgDuration);
            stringBuilder.append(" avgpss=");
            stringBuilder.append(this.mAvgBgMem);
            stringBuilder.append(" weight=");
            stringBuilder.append(this.mBgWeight);
            Log.d(str, stringBuilder.toString());
        }
    }

    public ProcStatsEntry(String pkgName, int uid, String procName, long duration, long mem, long memDuration) {
        this.mPackage = pkgName;
        this.mUid = uid;
        this.mName = procName;
        this.mRunDuration = duration;
        this.mBgDuration = duration;
        this.mMaxRunMem = mem;
        this.mAvgRunMem = mem;
        this.mMaxBgMem = mem;
        this.mAvgBgMem = mem;
        double d = ((double) memDuration) * ((double) mem);
        this.mRunWeight = d;
        this.mBgWeight = d;
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("New proc entry ");
            stringBuilder.append(procName);
            stringBuilder.append(": dur=");
            stringBuilder.append(this.mBgDuration);
            stringBuilder.append(" avgpss=");
            stringBuilder.append(this.mAvgBgMem);
            stringBuilder.append(" weight=");
            stringBuilder.append(this.mBgWeight);
            Log.d(str, stringBuilder.toString());
        }
    }

    public ProcStatsEntry(Parcel in) {
        this.mPackage = in.readString();
        this.mUid = in.readInt();
        this.mName = in.readString();
        in.readStringList(this.mPackages);
        this.mBgDuration = in.readLong();
        this.mAvgBgMem = in.readLong();
        this.mMaxBgMem = in.readLong();
        this.mBgWeight = in.readDouble();
        this.mRunDuration = in.readLong();
        this.mAvgRunMem = in.readLong();
        this.mMaxRunMem = in.readLong();
        this.mRunWeight = in.readDouble();
        this.mBestTargetPackage = in.readString();
        int N = in.readInt();
        if (N > 0) {
            this.mServices.ensureCapacity(N);
            for (int i = 0; i < N; i++) {
                String key = in.readString();
                ArrayList<Service> value = new ArrayList();
                in.readTypedList(value, Service.CREATOR);
                this.mServices.append(key, value);
            }
        }
    }

    public void addPackage(String packageName) {
        this.mPackages.add(packageName);
    }

    /* JADX WARNING: Removed duplicated region for block: B:110:0x0411  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x03dc  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x0477 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x044f  */
    public void evaluateTargetPackage(android.content.pm.PackageManager r24, com.android.internal.app.procstats.ProcessStats r25, com.android.internal.app.procstats.ProcessStats.ProcessDataCollection r26, com.android.internal.app.procstats.ProcessStats.ProcessDataCollection r27, java.util.Comparator<com.android.settings.applications.ProcStatsEntry> r28, boolean r29) {
        /*
        r23 = this;
        r1 = r23;
        r0 = 0;
        r1.mBestTargetPackage = r0;
        r0 = r1.mPackages;
        r0 = r0.size();
        r2 = 1;
        r3 = 0;
        if (r0 != r2) goto L_0x0046;
    L_0x000f:
        r0 = DEBUG;
        if (r0 == 0) goto L_0x003b;
    L_0x0013:
        r0 = "ProcStatsEntry";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r4 = "Eval pkg of ";
        r2.append(r4);
        r4 = r1.mName;
        r2.append(r4);
        r4 = ": single pkg ";
        r2.append(r4);
        r4 = r1.mPackages;
        r4 = r4.get(r3);
        r4 = (java.lang.String) r4;
        r2.append(r4);
        r2 = r2.toString();
        android.util.Log.d(r0, r2);
    L_0x003b:
        r0 = r1.mPackages;
        r0 = r0.get(r3);
        r0 = (java.lang.String) r0;
        r1.mBestTargetPackage = r0;
        return;
    L_0x0046:
        r0 = r3;
    L_0x0047:
        r4 = r1.mPackages;
        r4 = r4.size();
        if (r0 >= r4) goto L_0x006b;
    L_0x004f:
        r4 = "android";
        r5 = r1.mPackages;
        r5 = r5.get(r0);
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x0068;
    L_0x005d:
        r2 = r1.mPackages;
        r2 = r2.get(r0);
        r2 = (java.lang.String) r2;
        r1.mBestTargetPackage = r2;
        return;
    L_0x0068:
        r0 = r0 + 1;
        goto L_0x0047;
    L_0x006b:
        r0 = new java.util.ArrayList;
        r0.<init>();
        r4 = r0;
        r0 = r3;
    L_0x0072:
        r5 = r1.mPackages;
        r5 = r5.size();
        if (r0 >= r5) goto L_0x0155;
    L_0x007a:
        r5 = r25;
        r6 = r5.mPackages;
        r7 = r1.mPackages;
        r7 = r7.get(r0);
        r7 = (java.lang.String) r7;
        r8 = r1.mUid;
        r6 = r6.get(r7, r8);
        r6 = (android.util.LongSparseArray) r6;
        r7 = r3;
    L_0x008f:
        r8 = r6.size();
        if (r7 >= r8) goto L_0x0150;
    L_0x0095:
        r8 = r6.valueAt(r7);
        r8 = (com.android.internal.app.procstats.ProcessStats.PackageState) r8;
        r9 = DEBUG;
        if (r9 == 0) goto L_0x00c4;
    L_0x009f:
        r9 = "ProcStatsEntry";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "Eval pkg of ";
        r10.append(r11);
        r11 = r1.mName;
        r10.append(r11);
        r11 = ", pkg ";
        r10.append(r11);
        r10.append(r8);
        r11 = ":";
        r10.append(r11);
        r10 = r10.toString();
        android.util.Log.d(r9, r10);
    L_0x00c4:
        if (r8 != 0) goto L_0x00f9;
    L_0x00c6:
        r9 = "ProcStatsEntry";
        r10 = new java.lang.StringBuilder;
        r10.<init>();
        r11 = "No package state found for ";
        r10.append(r11);
        r11 = r1.mPackages;
        r11 = r11.get(r0);
        r11 = (java.lang.String) r11;
        r10.append(r11);
        r11 = "/";
        r10.append(r11);
        r11 = r1.mUid;
        r10.append(r11);
        r11 = " in process ";
        r10.append(r11);
        r11 = r1.mName;
        r10.append(r11);
        r10 = r10.toString();
        android.util.Log.w(r9, r10);
        goto L_0x014b;
    L_0x00f9:
        r9 = r8.mProcesses;
        r10 = r1.mName;
        r9 = r9.get(r10);
        r9 = (com.android.internal.app.procstats.ProcessState) r9;
        if (r9 != 0) goto L_0x0138;
    L_0x0105:
        r10 = "ProcStatsEntry";
        r11 = new java.lang.StringBuilder;
        r11.<init>();
        r12 = "No process ";
        r11.append(r12);
        r12 = r1.mName;
        r11.append(r12);
        r12 = " found in package state ";
        r11.append(r12);
        r12 = r1.mPackages;
        r12 = r12.get(r0);
        r12 = (java.lang.String) r12;
        r11.append(r12);
        r12 = "/";
        r11.append(r12);
        r12 = r1.mUid;
        r11.append(r12);
        r11 = r11.toString();
        android.util.Log.w(r10, r11);
        goto L_0x014b;
    L_0x0138:
        r15 = new com.android.settings.applications.ProcStatsEntry;
        r12 = r8.mPackageName;
        r10 = r15;
        r11 = r9;
        r13 = r26;
        r14 = r27;
        r3 = r15;
        r15 = r29;
        r10.<init>(r11, r12, r13, r14, r15);
        r4.add(r3);
    L_0x014b:
        r7 = r7 + 1;
        r3 = 0;
        goto L_0x008f;
    L_0x0150:
        r0 = r0 + 1;
        r3 = 0;
        goto L_0x0072;
    L_0x0155:
        r5 = r25;
        r0 = r4.size();
        if (r0 <= r2) goto L_0x0497;
    L_0x015d:
        r3 = r28;
        java.util.Collections.sort(r4, r3);
        r6 = 0;
        r0 = r4.get(r6);
        r0 = (com.android.settings.applications.ProcStatsEntry) r0;
        r6 = r0.mRunWeight;
        r0 = r4.get(r2);
        r0 = (com.android.settings.applications.ProcStatsEntry) r0;
        r8 = r0.mRunWeight;
        r10 = 4613937818241073152; // 0x4008000000000000 float:0.0 double:3.0;
        r8 = r8 * r10;
        r0 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1));
        if (r0 <= 0) goto L_0x01e3;
    L_0x017a:
        r0 = DEBUG;
        if (r0 == 0) goto L_0x01d7;
    L_0x017e:
        r0 = "ProcStatsEntry";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "Eval pkg of ";
        r6.append(r7);
        r7 = r1.mName;
        r6.append(r7);
        r7 = ": best pkg ";
        r6.append(r7);
        r7 = 0;
        r8 = r4.get(r7);
        r8 = (com.android.settings.applications.ProcStatsEntry) r8;
        r8 = r8.mPackage;
        r6.append(r8);
        r8 = " weight ";
        r6.append(r8);
        r8 = r4.get(r7);
        r8 = (com.android.settings.applications.ProcStatsEntry) r8;
        r7 = r8.mRunWeight;
        r6.append(r7);
        r7 = " better than ";
        r6.append(r7);
        r7 = r4.get(r2);
        r7 = (com.android.settings.applications.ProcStatsEntry) r7;
        r7 = r7.mPackage;
        r6.append(r7);
        r7 = " weight ";
        r6.append(r7);
        r2 = r4.get(r2);
        r2 = (com.android.settings.applications.ProcStatsEntry) r2;
        r7 = r2.mRunWeight;
        r6.append(r7);
        r2 = r6.toString();
        android.util.Log.d(r0, r2);
    L_0x01d7:
        r2 = 0;
        r0 = r4.get(r2);
        r0 = (com.android.settings.applications.ProcStatsEntry) r0;
        r0 = r0.mPackage;
        r1.mBestTargetPackage = r0;
        return;
    L_0x01e3:
        r2 = 0;
        r0 = r4.get(r2);
        r0 = (com.android.settings.applications.ProcStatsEntry) r0;
        r6 = r0.mRunWeight;
        r8 = -1;
        r0 = 0;
        r2 = r0;
        r0 = 0;
    L_0x01f1:
        r10 = r0;
        r0 = r4.size();
        if (r10 >= r0) goto L_0x0481;
    L_0x01f8:
        r0 = r4.get(r10);
        r11 = r0;
        r11 = (com.android.settings.applications.ProcStatsEntry) r11;
        r12 = r11.mRunWeight;
        r14 = 4611686018427387904; // 0x4000000000000000 float:0.0 double:2.0;
        r14 = r6 / r14;
        r0 = (r12 > r14 ? 1 : (r12 == r14 ? 0 : -1));
        if (r0 >= 0) goto L_0x0240;
    L_0x0209:
        r0 = DEBUG;
        if (r0 == 0) goto L_0x02ff;
    L_0x020d:
        r0 = "ProcStatsEntry";
        r12 = new java.lang.StringBuilder;
        r12.<init>();
        r13 = "Eval pkg of ";
        r12.append(r13);
        r13 = r1.mName;
        r12.append(r13);
        r13 = ": pkg ";
        r12.append(r13);
        r13 = r11.mPackage;
        r12.append(r13);
        r13 = " weight ";
        r12.append(r13);
        r13 = r11.mRunWeight;
        r12.append(r13);
        r13 = " too small";
        r12.append(r13);
        r12 = r12.toString();
        android.util.Log.d(r0, r12);
        goto L_0x02ff;
    L_0x0240:
        r0 = r11.mPackage;	 Catch:{ NameNotFoundException -> 0x0448 }
        r12 = r24;
        r13 = 0;
        r0 = r12.getApplicationInfo(r0, r13);	 Catch:{ NameNotFoundException -> 0x0448 }
        r13 = r0.icon;	 Catch:{ NameNotFoundException -> 0x0448 }
        if (r13 != 0) goto L_0x027f;
    L_0x024d:
        r13 = DEBUG;	 Catch:{ NameNotFoundException -> 0x027a }
        if (r13 == 0) goto L_0x0278;
    L_0x0251:
        r13 = "ProcStatsEntry";
        r14 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x027a }
        r14.<init>();	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = "Eval pkg of ";
        r14.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = r1.mName;	 Catch:{ NameNotFoundException -> 0x027a }
        r14.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = ": pkg ";
        r14.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = r11.mPackage;	 Catch:{ NameNotFoundException -> 0x027a }
        r14.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = " has no icon";
        r14.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r14 = r14.toString();	 Catch:{ NameNotFoundException -> 0x027a }
        android.util.Log.d(r13, r14);	 Catch:{ NameNotFoundException -> 0x027a }
    L_0x0278:
        goto L_0x02ff;
    L_0x027a:
        r0 = move-exception;
        r19 = r6;
        goto L_0x044b;
    L_0x027f:
        r13 = r0.flags;	 Catch:{ NameNotFoundException -> 0x0448 }
        r13 = r13 & 8;
        if (r13 == 0) goto L_0x0303;
    L_0x0285:
        r13 = r11.mRunDuration;	 Catch:{ NameNotFoundException -> 0x027a }
        if (r2 == 0) goto L_0x02cc;
    L_0x0289:
        r15 = (r13 > r8 ? 1 : (r13 == r8 ? 0 : -1));
        if (r15 <= 0) goto L_0x0290;
    L_0x028d:
        r16 = r0;
        goto L_0x02ce;
    L_0x0290:
        r15 = DEBUG;	 Catch:{ NameNotFoundException -> 0x027a }
        if (r15 == 0) goto L_0x02c9;
    L_0x0294:
        r15 = "ProcStatsEntry";
        r16 = r0;
        r0 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x027a }
        r0.<init>();	 Catch:{ NameNotFoundException -> 0x027a }
        r3 = "Eval pkg of ";
        r0.append(r3);	 Catch:{ NameNotFoundException -> 0x027a }
        r3 = r1.mName;	 Catch:{ NameNotFoundException -> 0x027a }
        r0.append(r3);	 Catch:{ NameNotFoundException -> 0x027a }
        r3 = ": pkg ";
        r0.append(r3);	 Catch:{ NameNotFoundException -> 0x027a }
        r3 = r11.mPackage;	 Catch:{ NameNotFoundException -> 0x027a }
        r0.append(r3);	 Catch:{ NameNotFoundException -> 0x027a }
        r3 = " pers run time ";
        r0.append(r3);	 Catch:{ NameNotFoundException -> 0x027a }
        r0.append(r13);	 Catch:{ NameNotFoundException -> 0x027a }
        r3 = " not as good as last ";
        r0.append(r3);	 Catch:{ NameNotFoundException -> 0x027a }
        r0.append(r8);	 Catch:{ NameNotFoundException -> 0x027a }
        r0 = r0.toString();	 Catch:{ NameNotFoundException -> 0x027a }
        android.util.Log.d(r15, r0);	 Catch:{ NameNotFoundException -> 0x027a }
        goto L_0x02fe;
    L_0x02c9:
        r16 = r0;
        goto L_0x02fe;
    L_0x02cc:
        r16 = r0;
    L_0x02ce:
        r0 = DEBUG;	 Catch:{ NameNotFoundException -> 0x027a }
        if (r0 == 0) goto L_0x02fc;
    L_0x02d2:
        r0 = "ProcStatsEntry";
        r3 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x027a }
        r3.<init>();	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = "Eval pkg of ";
        r3.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = r1.mName;	 Catch:{ NameNotFoundException -> 0x027a }
        r3.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = ": pkg ";
        r3.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = r11.mPackage;	 Catch:{ NameNotFoundException -> 0x027a }
        r3.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r15 = " new best pers run time ";
        r3.append(r15);	 Catch:{ NameNotFoundException -> 0x027a }
        r3.append(r13);	 Catch:{ NameNotFoundException -> 0x027a }
        r3 = r3.toString();	 Catch:{ NameNotFoundException -> 0x027a }
        android.util.Log.d(r0, r3);	 Catch:{ NameNotFoundException -> 0x027a }
    L_0x02fc:
        r8 = r13;
        r2 = 1;
    L_0x02ff:
        r19 = r6;
        goto L_0x0477;
    L_0x0303:
        r16 = r0;
        if (r2 == 0) goto L_0x0333;
    L_0x0307:
        r0 = DEBUG;	 Catch:{ NameNotFoundException -> 0x027a }
        if (r0 == 0) goto L_0x0332;
    L_0x030b:
        r0 = "ProcStatsEntry";
        r3 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException -> 0x027a }
        r3.<init>();	 Catch:{ NameNotFoundException -> 0x027a }
        r13 = "Eval pkg of ";
        r3.append(r13);	 Catch:{ NameNotFoundException -> 0x027a }
        r13 = r1.mName;	 Catch:{ NameNotFoundException -> 0x027a }
        r3.append(r13);	 Catch:{ NameNotFoundException -> 0x027a }
        r13 = ": pkg ";
        r3.append(r13);	 Catch:{ NameNotFoundException -> 0x027a }
        r13 = r11.mPackage;	 Catch:{ NameNotFoundException -> 0x027a }
        r3.append(r13);	 Catch:{ NameNotFoundException -> 0x027a }
        r13 = " is not persistent";
        r3.append(r13);	 Catch:{ NameNotFoundException -> 0x027a }
        r3 = r3.toString();	 Catch:{ NameNotFoundException -> 0x027a }
        android.util.Log.d(r0, r3);	 Catch:{ NameNotFoundException -> 0x027a }
    L_0x0332:
        goto L_0x02ff;
        r0 = 0;
        r3 = 0;
        r13 = r1.mServices;
        r13 = r13.size();
    L_0x033c:
        if (r3 >= r13) goto L_0x0362;
    L_0x033e:
        r14 = r1.mServices;
        r14 = r14.valueAt(r3);
        r14 = (java.util.ArrayList) r14;
        r15 = 0;
        r16 = r14.get(r15);
        r15 = r16;
        r15 = (com.android.settings.applications.ProcStatsEntry.Service) r15;
        r15 = r15.mPackage;
        r17 = r0;
        r0 = r11.mPackage;
        r0 = r15.equals(r0);
        if (r0 == 0) goto L_0x035d;
    L_0x035b:
        r0 = r14;
        goto L_0x0364;
    L_0x035d:
        r3 = r3 + 1;
        r0 = r17;
        goto L_0x033c;
    L_0x0362:
        r17 = r0;
    L_0x0364:
        r13 = 0;
        if (r0 == 0) goto L_0x03d0;
    L_0x0368:
        r3 = 0;
        r15 = r0.size();
    L_0x036d:
        if (r3 >= r15) goto L_0x03d0;
    L_0x036f:
        r16 = r0.get(r3);
        r18 = r0;
        r0 = r16;
        r0 = (com.android.settings.applications.ProcStatsEntry.Service) r0;
        r19 = r6;
        r5 = r0.mDuration;
        r5 = (r5 > r13 ? 1 : (r5 == r13 ? 0 : -1));
        if (r5 <= 0) goto L_0x03c3;
    L_0x0381:
        r5 = DEBUG;
        if (r5 == 0) goto L_0x03be;
    L_0x0385:
        r5 = "ProcStatsEntry";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "Eval pkg of ";
        r6.append(r7);
        r7 = r1.mName;
        r6.append(r7);
        r7 = ": pkg ";
        r6.append(r7);
        r7 = r11.mPackage;
        r6.append(r7);
        r7 = " service ";
        r6.append(r7);
        r7 = r0.mName;
        r6.append(r7);
        r7 = " run time is ";
        r6.append(r7);
        r21 = r13;
        r12 = r0.mDuration;
        r6.append(r12);
        r6 = r6.toString();
        android.util.Log.d(r5, r6);
        goto L_0x03c0;
    L_0x03be:
        r21 = r13;
    L_0x03c0:
        r13 = r0.mDuration;
        goto L_0x03d8;
    L_0x03c3:
        r21 = r13;
        r3 = r3 + 1;
        r0 = r18;
        r6 = r19;
        r5 = r25;
        r12 = r24;
        goto L_0x036d;
    L_0x03d0:
        r18 = r0;
        r19 = r6;
        r21 = r13;
        r13 = r21;
    L_0x03d8:
        r0 = (r13 > r8 ? 1 : (r13 == r8 ? 0 : -1));
        if (r0 <= 0) goto L_0x0411;
    L_0x03dc:
        r0 = DEBUG;
        if (r0 == 0) goto L_0x040a;
    L_0x03e0:
        r0 = "ProcStatsEntry";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r5 = "Eval pkg of ";
        r3.append(r5);
        r5 = r1.mName;
        r3.append(r5);
        r5 = ": pkg ";
        r3.append(r5);
        r5 = r11.mPackage;
        r3.append(r5);
        r5 = " new best run time ";
        r3.append(r5);
        r3.append(r13);
        r3 = r3.toString();
        android.util.Log.d(r0, r3);
    L_0x040a:
        r0 = r11.mPackage;
        r1.mBestTargetPackage = r0;
        r5 = r13;
        r8 = r5;
        goto L_0x0477;
    L_0x0411:
        r0 = DEBUG;
        if (r0 == 0) goto L_0x0477;
    L_0x0415:
        r0 = "ProcStatsEntry";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r5 = "Eval pkg of ";
        r3.append(r5);
        r5 = r1.mName;
        r3.append(r5);
        r5 = ": pkg ";
        r3.append(r5);
        r5 = r11.mPackage;
        r3.append(r5);
        r5 = " run time ";
        r3.append(r5);
        r3.append(r13);
        r5 = " not as good as last ";
        r3.append(r5);
        r3.append(r8);
        r3 = r3.toString();
        android.util.Log.d(r0, r3);
        goto L_0x0477;
    L_0x0448:
        r0 = move-exception;
        r19 = r6;
    L_0x044b:
        r3 = DEBUG;
        if (r3 == 0) goto L_0x0476;
    L_0x044f:
        r3 = "ProcStatsEntry";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Eval pkg of ";
        r5.append(r6);
        r6 = r1.mName;
        r5.append(r6);
        r6 = ": pkg ";
        r5.append(r6);
        r6 = r11.mPackage;
        r5.append(r6);
        r6 = " failed finding app info";
        r5.append(r6);
        r5 = r5.toString();
        android.util.Log.d(r3, r5);
    L_0x0477:
        r0 = r10 + 1;
        r6 = r19;
        r3 = r28;
        r5 = r25;
        goto L_0x01f1;
    L_0x0481:
        r19 = r6;
        r0 = r1.mBestTargetPackage;
        r0 = android.text.TextUtils.isEmpty(r0);
        if (r0 == 0) goto L_0x0496;
    L_0x048b:
        r3 = 0;
        r0 = r4.get(r3);
        r0 = (com.android.settings.applications.ProcStatsEntry) r0;
        r0 = r0.mPackage;
        r1.mBestTargetPackage = r0;
    L_0x0496:
        goto L_0x04a8;
    L_0x0497:
        r0 = r4.size();
        if (r0 != r2) goto L_0x04a8;
    L_0x049d:
        r2 = 0;
        r0 = r4.get(r2);
        r0 = (com.android.settings.applications.ProcStatsEntry) r0;
        r0 = r0.mPackage;
        r1.mBestTargetPackage = r0;
    L_0x04a8:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.ProcStatsEntry.evaluateTargetPackage(android.content.pm.PackageManager, com.android.internal.app.procstats.ProcessStats, com.android.internal.app.procstats.ProcessStats$ProcessDataCollection, com.android.internal.app.procstats.ProcessStats$ProcessDataCollection, java.util.Comparator, boolean):void");
    }

    public void addService(ServiceState svc) {
        ArrayList<Service> services = (ArrayList) this.mServices.get(svc.getPackage());
        if (services == null) {
            services = new ArrayList();
            this.mServices.put(svc.getPackage(), services);
        }
        services.add(new Service(svc));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackage);
        dest.writeInt(this.mUid);
        dest.writeString(this.mName);
        dest.writeStringList(this.mPackages);
        dest.writeLong(this.mBgDuration);
        dest.writeLong(this.mAvgBgMem);
        dest.writeLong(this.mMaxBgMem);
        dest.writeDouble(this.mBgWeight);
        dest.writeLong(this.mRunDuration);
        dest.writeLong(this.mAvgRunMem);
        dest.writeLong(this.mMaxRunMem);
        dest.writeDouble(this.mRunWeight);
        dest.writeString(this.mBestTargetPackage);
        int N = this.mServices.size();
        dest.writeInt(N);
        for (int i = 0; i < N; i++) {
            dest.writeString((String) this.mServices.keyAt(i));
            dest.writeTypedList((List) this.mServices.valueAt(i));
        }
    }

    public int getUid() {
        return this.mUid;
    }
}
