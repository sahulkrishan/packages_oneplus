package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import com.android.internal.app.ProcessMap;
import com.android.internal.app.procstats.DumpUtils;
import com.android.internal.app.procstats.IProcessStats;
import com.android.internal.app.procstats.IProcessStats.Stub;
import com.android.internal.app.procstats.ProcessState;
import com.android.internal.app.procstats.ProcessStats;
import com.android.internal.app.procstats.ProcessStats.PackageState;
import com.android.internal.app.procstats.ProcessStats.ProcessDataCollection;
import com.android.internal.app.procstats.ProcessStats.TotalMemoryUseCollection;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.util.MemInfoReader;
import com.android.settings.R;
import com.android.settings.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProcStatsData {
    private static final boolean DEBUG = false;
    private static final String TAG = "ProcStatsManager";
    static final Comparator<ProcStatsEntry> sEntryCompare = new Comparator<ProcStatsEntry>() {
        public int compare(ProcStatsEntry lhs, ProcStatsEntry rhs) {
            if (lhs.mRunWeight < rhs.mRunWeight) {
                return 1;
            }
            if (lhs.mRunWeight > rhs.mRunWeight) {
                return -1;
            }
            if (lhs.mRunDuration < rhs.mRunDuration) {
                return 1;
            }
            if (lhs.mRunDuration > rhs.mRunDuration) {
                return -1;
            }
            return 0;
        }
    };
    private static ProcessStats sStatsXfer;
    private Context mContext;
    private long mDuration;
    private MemInfo mMemInfo;
    private int[] mMemStates = ProcessStats.ALL_MEM_ADJ;
    private PackageManager mPm;
    private IProcessStats mProcessStats = Stub.asInterface(ServiceManager.getService("procstats"));
    private int[] mStates = ProcessStats.BACKGROUND_PROC_STATES;
    private ProcessStats mStats;
    private boolean mUseUss;
    private long memTotalTime;
    private ArrayList<ProcStatsPackageEntry> pkgEntries;

    public static class MemInfo {
        long baseCacheRam;
        double freeWeight;
        double[] mMemStateWeights;
        long memTotalTime;
        public double realFreeRam;
        public double realTotalRam;
        public double realUsedRam;
        double totalRam;
        double totalScale;
        double usedWeight;
        double weightToRam;

        /* synthetic */ MemInfo(Context x0, TotalMemoryUseCollection x1, long x2, AnonymousClass1 x3) {
            this(x0, x1, x2);
        }

        public double getWeightToRam() {
            return this.weightToRam;
        }

        private MemInfo(Context context, TotalMemoryUseCollection totalMem, long memTotalTime) {
            this.mMemStateWeights = new double[14];
            this.memTotalTime = memTotalTime;
            calculateWeightInfo(context, totalMem, memTotalTime);
            double usedRam = (this.usedWeight * 1024.0d) / ((double) memTotalTime);
            double freeRam = (this.freeWeight * 1024.0d) / ((double) memTotalTime);
            this.totalRam = usedRam + freeRam;
            this.totalScale = this.realTotalRam / this.totalRam;
            this.weightToRam = (this.totalScale / ((double) memTotalTime)) * 1024.0d;
            this.realUsedRam = this.totalScale * usedRam;
            this.realFreeRam = this.totalScale * freeRam;
            MemoryInfo memInfo = new MemoryInfo();
            ((ActivityManager) context.getSystemService("activity")).getMemoryInfo(memInfo);
            if (((double) memInfo.hiddenAppThreshold) >= this.realFreeRam) {
                this.realUsedRam = freeRam;
                this.realFreeRam = 0.0d;
                this.baseCacheRam = (long) this.realFreeRam;
                return;
            }
            this.realUsedRam += (double) memInfo.hiddenAppThreshold;
            this.realFreeRam -= (double) memInfo.hiddenAppThreshold;
            this.baseCacheRam = memInfo.hiddenAppThreshold;
        }

        private void calculateWeightInfo(Context context, TotalMemoryUseCollection totalMem, long memTotalTime) {
            MemInfoReader memReader = new MemInfoReader();
            memReader.readMemInfo();
            this.realTotalRam = (double) memReader.getTotalSize();
            this.freeWeight = totalMem.sysMemFreeWeight + totalMem.sysMemCachedWeight;
            this.usedWeight = totalMem.sysMemKernelWeight + totalMem.sysMemNativeWeight;
            if (!totalMem.hasSwappedOutPss) {
                this.usedWeight += totalMem.sysMemZRamWeight;
            }
            for (int i = 0; i < 14; i++) {
                if (i == 6) {
                    this.mMemStateWeights[i] = 0.0d;
                } else {
                    this.mMemStateWeights[i] = totalMem.processStateWeight[i];
                    if (i >= 9) {
                        this.freeWeight += totalMem.processStateWeight[i];
                    } else {
                        this.usedWeight += totalMem.processStateWeight[i];
                    }
                }
            }
        }
    }

    public ProcStatsData(Context context, boolean useXfer) {
        this.mContext = context;
        this.mPm = context.getPackageManager();
        if (useXfer) {
            this.mStats = sStatsXfer;
        }
    }

    public void setTotalTime(int totalTime) {
        this.memTotalTime = (long) totalTime;
    }

    public void xferStats() {
        sStatsXfer = this.mStats;
    }

    public void setMemStates(int[] memStates) {
        this.mMemStates = memStates;
        refreshStats(false);
    }

    public void setStats(int[] stats) {
        this.mStates = stats;
        refreshStats(false);
    }

    public int getMemState() {
        int factor = this.mStats.mMemFactor;
        if (factor == -1) {
            return 0;
        }
        if (factor >= 4) {
            factor -= 4;
        }
        return factor;
    }

    public MemInfo getMemInfo() {
        return this.mMemInfo;
    }

    public long getElapsedTime() {
        return this.mStats.mTimePeriodEndRealtime - this.mStats.mTimePeriodStartRealtime;
    }

    public void setDuration(long duration) {
        if (duration != this.mDuration) {
            this.mDuration = duration;
            refreshStats(true);
        }
    }

    public long getDuration() {
        return this.mDuration;
    }

    public List<ProcStatsPackageEntry> getEntries() {
        return this.pkgEntries;
    }

    public void refreshStats(boolean forceLoad) {
        if (this.mStats == null || forceLoad) {
            load();
        }
        this.pkgEntries = new ArrayList();
        long now = SystemClock.uptimeMillis();
        this.memTotalTime = DumpUtils.dumpSingleTime(null, null, this.mStats.mMemFactorDurations, this.mStats.mMemFactor, this.mStats.mStartTime, now);
        TotalMemoryUseCollection totalMem = new TotalMemoryUseCollection(ProcessStats.ALL_SCREEN_ADJ, this.mMemStates);
        this.mStats.computeTotalMemoryUse(totalMem, now);
        this.mMemInfo = new MemInfo(this.mContext, totalMem, this.memTotalTime, null);
        ProcessDataCollection bgTotals = new ProcessDataCollection(ProcessStats.ALL_SCREEN_ADJ, this.mMemStates, this.mStates);
        ProcessDataCollection runTotals = new ProcessDataCollection(ProcessStats.ALL_SCREEN_ADJ, this.mMemStates, ProcessStats.NON_CACHED_PROC_STATES);
        createPkgMap(getProcs(bgTotals, runTotals), bgTotals, runTotals);
        if (totalMem.sysMemZRamWeight > 0.0d && !totalMem.hasSwappedOutPss) {
            distributeZRam(totalMem.sysMemZRamWeight);
        }
        this.pkgEntries.add(createOsEntry(bgTotals, runTotals, totalMem, this.mMemInfo.baseCacheRam));
    }

    private void createPkgMap(ArrayList<ProcStatsEntry> procEntries, ProcessDataCollection bgTotals, ProcessDataCollection runTotals) {
        ArrayMap<String, ProcStatsPackageEntry> pkgMap = new ArrayMap();
        for (int i = procEntries.size() - 1; i >= 0; i--) {
            ProcStatsEntry proc = (ProcStatsEntry) procEntries.get(i);
            proc.evaluateTargetPackage(this.mPm, this.mStats, bgTotals, runTotals, sEntryCompare, this.mUseUss);
            ProcStatsPackageEntry pkg = (ProcStatsPackageEntry) pkgMap.get(proc.mBestTargetPackage);
            if (pkg == null) {
                pkg = new ProcStatsPackageEntry(proc.mBestTargetPackage, this.memTotalTime);
                pkgMap.put(proc.mBestTargetPackage, pkg);
                this.pkgEntries.add(pkg);
            }
            pkg.addEntry(proc);
        }
    }

    private void distributeZRam(double zramWeight) {
        int i;
        long zramMem = (long) (zramWeight / ((double) this.memTotalTime));
        long totalTime = 0;
        for (i = this.pkgEntries.size() - 1; i >= 0; i--) {
            ProcStatsPackageEntry entry = (ProcStatsPackageEntry) this.pkgEntries.get(i);
            for (int j = entry.mEntries.size() - 1; j >= 0; j--) {
                totalTime += ((ProcStatsEntry) entry.mEntries.get(j)).mRunDuration;
            }
        }
        for (i = this.pkgEntries.size() - 1; i >= 0 && totalTime > 0; i--) {
            ProcStatsPackageEntry entry2 = (ProcStatsPackageEntry) this.pkgEntries.get(i);
            long pkgRunTime = 0;
            long maxRunTime = 0;
            for (int j2 = entry2.mEntries.size() - 1; j2 >= 0; j2--) {
                ProcStatsEntry proc = (ProcStatsEntry) entry2.mEntries.get(j2);
                pkgRunTime += proc.mRunDuration;
                if (proc.mRunDuration > maxRunTime) {
                    maxRunTime = proc.mRunDuration;
                }
            }
            long pkgZRam = (zramMem * pkgRunTime) / totalTime;
            if (pkgZRam > 0) {
                zramMem -= pkgZRam;
                totalTime -= pkgRunTime;
                long zramMem2 = zramMem;
                ProcStatsEntry procEntry = new ProcStatsEntry(entry2.mPackage, 0, this.mContext.getString(2131889964), maxRunTime, pkgZRam, this.memTotalTime);
                PackageManager packageManager = this.mPm;
                procEntry.evaluateTargetPackage(packageManager, this.mStats, null, null, sEntryCompare, this.mUseUss);
                entry2.addEntry(procEntry);
                zramMem = zramMem2;
            }
        }
    }

    private ProcStatsPackageEntry createOsEntry(ProcessDataCollection bgTotals, ProcessDataCollection runTotals, TotalMemoryUseCollection totalMem, long baseCacheRam) {
        long mem;
        ProcStatsEntry osEntry;
        PackageManager packageManager;
        TotalMemoryUseCollection totalMemoryUseCollection = totalMem;
        ProcStatsPackageEntry osPkg = new ProcStatsPackageEntry(Utils.OS_PKG, this.memTotalTime);
        if (totalMemoryUseCollection.sysMemNativeWeight > 0.0d) {
            mem = 0;
            if (this.memTotalTime != 0) {
                mem = (long) (totalMemoryUseCollection.sysMemNativeWeight / ((double) this.memTotalTime));
            }
            osEntry = new ProcStatsEntry(Utils.OS_PKG, 0, this.mContext.getString(R.string.process_stats_os_native), this.memTotalTime, mem, this.memTotalTime);
            packageManager = this.mPm;
            osEntry.evaluateTargetPackage(packageManager, this.mStats, bgTotals, runTotals, sEntryCompare, this.mUseUss);
            osPkg.addEntry(osEntry);
        }
        if (totalMemoryUseCollection.sysMemKernelWeight > 0.0d) {
            mem = 0;
            if (this.memTotalTime != 0) {
                mem = (long) (totalMemoryUseCollection.sysMemKernelWeight / ((double) this.memTotalTime));
            }
            osEntry = new ProcStatsEntry(Utils.OS_PKG, 0, this.mContext.getString(R.string.process_stats_os_kernel), this.memTotalTime, mem, this.memTotalTime);
            packageManager = this.mPm;
            osEntry.evaluateTargetPackage(packageManager, this.mStats, bgTotals, runTotals, sEntryCompare, this.mUseUss);
            osPkg.addEntry(osEntry);
        }
        if (baseCacheRam > 0) {
            ProcStatsEntry procStatsEntry = new ProcStatsEntry(Utils.OS_PKG, 0, this.mContext.getString(R.string.process_stats_os_cache), this.memTotalTime, baseCacheRam / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID, this.memTotalTime);
            PackageManager packageManager2 = this.mPm;
            procStatsEntry.evaluateTargetPackage(packageManager2, this.mStats, bgTotals, runTotals, sEntryCompare, this.mUseUss);
            osPkg.addEntry(procStatsEntry);
        }
        return osPkg;
    }

    private ArrayList<ProcStatsEntry> getProcs(ProcessDataCollection bgTotals, ProcessDataCollection runTotals) {
        int ipkg;
        int iproc;
        ProcStatsData procStatsData = this;
        ArrayList<ProcStatsEntry> procEntries = new ArrayList();
        ProcessMap<ProcStatsEntry> entriesMap = new ProcessMap();
        int N = procStatsData.mStats.mPackages.getMap().size();
        for (ipkg = 0; ipkg < N; ipkg++) {
            SparseArray<LongSparseArray<PackageState>> pkgUids = (SparseArray) procStatsData.mStats.mPackages.getMap().valueAt(ipkg);
            for (int iu = 0; iu < pkgUids.size(); iu++) {
                LongSparseArray<PackageState> vpkgs = (LongSparseArray) pkgUids.valueAt(iu);
                for (int iv = 0; iv < vpkgs.size(); iv++) {
                    PackageState st = (PackageState) vpkgs.valueAt(iv);
                    iproc = 0;
                    while (iproc < st.mProcesses.size()) {
                        int N2;
                        ProcessState pkgProc = (ProcessState) st.mProcesses.valueAt(iproc);
                        ProcessState proc = (ProcessState) procStatsData.mStats.mProcesses.get(pkgProc.getName(), pkgProc.getUid());
                        if (proc == null) {
                            N2 = N;
                        } else {
                            ProcStatsEntry ent = (ProcStatsEntry) entriesMap.get(proc.getName(), proc.getUid());
                            if (ent == null) {
                                N2 = N;
                                N = ent;
                                ProcessState processState = proc;
                                N = new ProcStatsEntry(processState, st.mPackageName, bgTotals, runTotals, procStatsData.mUseUss);
                                if (N.mRunWeight > 0.0d) {
                                    entriesMap.put(proc.getName(), proc.getUid(), N);
                                    procEntries.add(N);
                                }
                            } else {
                                N2 = N;
                                ent.addPackage(st.mPackageName);
                            }
                        }
                        iproc++;
                        N = N2;
                    }
                }
            }
        }
        ipkg = 0;
        N = procStatsData.mStats.mPackages.getMap().size();
        while (ipkg < N) {
            SparseArray<LongSparseArray<PackageState>> uids = (SparseArray) procStatsData.mStats.mPackages.getMap().valueAt(ipkg);
            for (int iu2 = 0; iu2 < uids.size(); iu2++) {
                LongSparseArray<PackageState> vpkgs2 = (LongSparseArray) uids.valueAt(iu2);
                for (int iv2 = 0; iv2 < vpkgs2.size(); iv2++) {
                    PackageState ps = (PackageState) vpkgs2.valueAt(iv2);
                    iproc = ps.mServices.size();
                    for (int is = 0; is < iproc; is++) {
                        ServiceState ss = (ServiceState) ps.mServices.valueAt(is);
                        if (ss.getProcessName() != null) {
                            ProcStatsEntry ent2 = (ProcStatsEntry) entriesMap.get(ss.getProcessName(), uids.keyAt(iu2));
                            if (ent2 != null) {
                                ent2.addService(ss);
                            } else {
                                String str = TAG;
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("No process ");
                                stringBuilder.append(ss.getProcessName());
                                stringBuilder.append("/");
                                stringBuilder.append(uids.keyAt(iu2));
                                stringBuilder.append(" for service ");
                                stringBuilder.append(ss.getName());
                                Log.w(str, stringBuilder.toString());
                            }
                        }
                    }
                }
            }
            ipkg++;
            procStatsData = this;
        }
        return procEntries;
    }

    private void load() {
        try {
            ParcelFileDescriptor pfd = this.mProcessStats.getStatsOverTime(this.mDuration);
            this.mStats = new ProcessStats(false);
            InputStream is = new AutoCloseInputStream(pfd);
            this.mStats.read(is);
            try {
                is.close();
            } catch (IOException e) {
            }
            if (this.mStats.mReadError != null) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failure reading process stats: ");
                stringBuilder.append(this.mStats.mReadError);
                Log.w(str, stringBuilder.toString());
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "RemoteException:", e2);
        }
    }
}
