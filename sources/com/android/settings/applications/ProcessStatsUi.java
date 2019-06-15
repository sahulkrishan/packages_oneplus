package com.android.settings.applications;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.util.TimeUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.applications.ProcStatsData.MemInfo;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProcessStatsUi extends ProcessStatsBase {
    public static final int[] BACKGROUND_AND_SYSTEM_PROC_STATES = new int[]{0, 2, 3, 4, 8, 5, 6, 7, 9};
    public static final int[] CACHED_PROC_STATES = new int[]{11, 12, 13};
    static final boolean DEBUG = false;
    public static final int[] FOREGROUND_PROC_STATES = new int[]{1};
    private static final String KEY_APP_LIST = "app_list";
    private static final int MENU_SHOW_AVG = 1;
    private static final int MENU_SHOW_MAX = 2;
    static final String TAG = "ProcessStatsUi";
    static final Comparator<ProcStatsPackageEntry> sMaxPackageEntryCompare = new Comparator<ProcStatsPackageEntry>() {
        public int compare(ProcStatsPackageEntry lhs, ProcStatsPackageEntry rhs) {
            double rhsMax = (double) Math.max(rhs.mMaxBgMem, rhs.mMaxRunMem);
            double lhsMax = (double) Math.max(lhs.mMaxBgMem, lhs.mMaxRunMem);
            if (lhsMax == rhsMax) {
                return 0;
            }
            return lhsMax < rhsMax ? 1 : -1;
        }
    };
    static final Comparator<ProcStatsPackageEntry> sPackageEntryCompare = new Comparator<ProcStatsPackageEntry>() {
        public int compare(ProcStatsPackageEntry lhs, ProcStatsPackageEntry rhs) {
            double rhsWeight = Math.max(rhs.mRunWeight, rhs.mBgWeight);
            double lhsWeight = Math.max(lhs.mRunWeight, lhs.mBgWeight);
            if (lhsWeight == rhsWeight) {
                return 0;
            }
            return lhsWeight < rhsWeight ? 1 : -1;
        }
    };
    private PreferenceGroup mAppListGroup;
    private MenuItem mMenuAvg;
    private MenuItem mMenuMax;
    private PackageManager mPm;
    private boolean mShowMax;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPm = getActivity().getPackageManager();
        addPreferencesFromResource(R.xml.process_stats_ui);
        this.mAppListGroup = (PreferenceGroup) findPreference(KEY_APP_LIST);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.mMenuAvg = menu.add(0, 1, 0, R.string.sort_avg_use);
        this.mMenuMax = menu.add(0, 2, 0, R.string.sort_max_use);
        updateMenu();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
            case 2:
                this.mShowMax ^= 1;
                refreshUi();
                updateMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateMenu() {
        this.mMenuMax.setVisible(this.mShowMax ^ 1);
        this.mMenuAvg.setVisible(this.mShowMax);
    }

    public int getMetricsCategory() {
        return 23;
    }

    public int getHelpResource() {
        return R.string.help_uri_process_stats_apps;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof ProcessStatsPreference)) {
            return false;
        }
        ProcessStatsPreference pgp = (ProcessStatsPreference) preference;
        ProcessStatsBase.launchMemoryDetail((SettingsActivity) getActivity(), this.mStatsManager.getMemInfo(), pgp.getEntry(), true);
        return super.onPreferenceTreeClick(preference);
    }

    public static String makeDuration(long time) {
        StringBuilder sb = new StringBuilder(32);
        TimeUtils.formatDuration(time, sb);
        return sb.toString();
    }

    public void refreshUi() {
        int i;
        double d;
        this.mAppListGroup.removeAll();
        int i2 = 0;
        this.mAppListGroup.setOrderingAsAdded(false);
        PreferenceGroup preferenceGroup = this.mAppListGroup;
        if (this.mShowMax) {
            i = R.string.maximum_memory_use;
        } else {
            i = R.string.average_memory_use;
        }
        preferenceGroup.setTitle(i);
        Context context = getActivity();
        MemInfo memInfo = this.mStatsManager.getMemInfo();
        List<ProcStatsPackageEntry> pkgEntries = this.mStatsManager.getEntries();
        int N = pkgEntries.size();
        for (int i3 = 0; i3 < N; i3++) {
            ((ProcStatsPackageEntry) pkgEntries.get(i3)).updateMetrics();
        }
        Collections.sort(pkgEntries, this.mShowMax ? sMaxPackageEntryCompare : sPackageEntryCompare);
        if (this.mShowMax) {
            d = memInfo.realTotalRam;
        } else {
            d = memInfo.usedWeight * memInfo.weightToRam;
        }
        double maxMemory = d;
        while (i2 < pkgEntries.size()) {
            ProcStatsPackageEntry pkg = (ProcStatsPackageEntry) pkgEntries.get(i2);
            ProcessStatsPreference pref = new ProcessStatsPreference(getPrefContext());
            pkg.retrieveUiData(context, this.mPm);
            pref.init(pkg, this.mPm, maxMemory, memInfo.weightToRam, memInfo.totalScale, this.mShowMax ^ 1);
            pref.setOrder(i2);
            this.mAppListGroup.addPreference(pref);
            i2++;
        }
    }
}
