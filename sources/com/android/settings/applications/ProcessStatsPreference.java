package com.android.settings.applications;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import com.android.settings.widget.AppPreference;

public class ProcessStatsPreference extends AppPreference {
    static final String TAG = "ProcessStatsPreference";
    private ProcStatsPackageEntry mEntry;

    public ProcessStatsPreference(Context context) {
        super(context, null);
    }

    public void init(ProcStatsPackageEntry entry, PackageManager pm, double maxMemory, double weightToRam, double totalScale, boolean avg) {
        double amount;
        ProcStatsPackageEntry procStatsPackageEntry = entry;
        this.mEntry = procStatsPackageEntry;
        String title = TextUtils.isEmpty(procStatsPackageEntry.mUiLabel) ? procStatsPackageEntry.mPackage : procStatsPackageEntry.mUiLabel;
        setTitle((CharSequence) title);
        if (TextUtils.isEmpty(title)) {
            Log.d(TAG, "PackageEntry contained no package name or uiLabel");
        }
        if (procStatsPackageEntry.mUiTargetApp != null) {
            setIcon(procStatsPackageEntry.mUiTargetApp.loadIcon(pm));
        } else {
            setIcon(pm.getDefaultActivityIcon());
        }
        boolean statsForeground = procStatsPackageEntry.mRunWeight > procStatsPackageEntry.mBgWeight;
        if (avg) {
            amount = (statsForeground ? procStatsPackageEntry.mRunWeight : procStatsPackageEntry.mBgWeight) * weightToRam;
        } else {
            amount = (((double) (statsForeground ? procStatsPackageEntry.mMaxRunMem : procStatsPackageEntry.mMaxBgMem)) * totalScale) * 1024.0d;
        }
        setSummary((CharSequence) Formatter.formatShortFileSize(getContext(), (long) amount));
        setProgress((int) ((100.0d * amount) / maxMemory));
    }

    public ProcStatsPackageEntry getEntry() {
        return this.mEntry;
    }
}
