package com.android.settings.applications;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import com.android.settings.R;
import com.android.settings.SummaryPreference;
import com.android.settings.applications.ProcStatsData.MemInfo;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settingslib.Utils;

public class ProcessStatsSummary extends ProcessStatsBase implements OnPreferenceClickListener {
    private static final String KEY_APP_LIST = "apps_list";
    private static final String KEY_AVERAGY_USED = "average_used";
    private static final String KEY_FREE = "free";
    private static final String KEY_PERFORMANCE = "performance";
    private static final String KEY_STATUS_HEADER = "status_header";
    private static final String KEY_TOTAL_MEMORY = "total_memory";
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private Preference mAppListPreference;
    private Preference mAverageUsed;
    private Preference mFree;
    private Preference mPerformance;
    private SummaryPreference mSummaryPref;
    private Preference mTotalMemory;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                ProcStatsData statsManager = new ProcStatsData(this.mContext, false);
                statsManager.setDuration(ProcessStatsBase.sDurations[0]);
                MemInfo memInfo = statsManager.getMemInfo();
                String usedResult = Formatter.formatShortFileSize(this.mContext, (long) memInfo.realUsedRam);
                String totalResult = Formatter.formatShortFileSize(this.mContext, (long) memInfo.realTotalRam);
                this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.memory_summary, new Object[]{usedResult, totalResult}));
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.process_stats_summary);
        this.mSummaryPref = (SummaryPreference) findPreference(KEY_STATUS_HEADER);
        this.mPerformance = findPreference(KEY_PERFORMANCE);
        this.mTotalMemory = findPreference(KEY_TOTAL_MEMORY);
        this.mAverageUsed = findPreference(KEY_AVERAGY_USED);
        this.mFree = findPreference(KEY_FREE);
        this.mAppListPreference = findPreference(KEY_APP_LIST);
        this.mAppListPreference.setOnPreferenceClickListener(this);
    }

    public void refreshUi() {
        CharSequence memString;
        Context context = getContext();
        MemInfo memInfo = this.mStatsManager.getMemInfo();
        double usedRam = memInfo.realUsedRam;
        double totalRam = memInfo.realTotalRam;
        double freeRam = memInfo.realFreeRam;
        BytesResult usedResult = Formatter.formatBytes(context.getResources(), (long) usedRam, 1);
        CharSequence totalString = Formatter.formatShortFileSize(context, (long) totalRam);
        CharSequence freeString = Formatter.formatShortFileSize(context, (long) freeRam);
        CharSequence[] memStatesStr = getResources().getTextArray(R.array.ram_states);
        int memState = this.mStatsManager.getMemState();
        if (memState < 0 || memState >= memStatesStr.length - 1) {
            memString = memStatesStr[memStatesStr.length - 1];
        } else {
            memString = memStatesStr[memState];
        }
        this.mSummaryPref.setAmount(usedResult.value);
        this.mSummaryPref.setUnits(usedResult.units);
        float usedRatio = (float) (usedRam / (freeRam + usedRam));
        this.mSummaryPref.setRatios(usedRatio, 0.0f, 1.0f - usedRatio);
        this.mPerformance.setSummary(memString);
        this.mTotalMemory.setSummary(totalString);
        this.mAverageUsed.setSummary(Utils.formatPercentage((long) usedRam, (long) (long) totalRam));
        this.mFree.setSummary(freeString);
        String durationString = getString(sDurationLabels[this.mDurationIndex]);
        int numApps = this.mStatsManager.getEntries().size();
        this.mAppListPreference.setSummary(getResources().getQuantityString(R.plurals.memory_usage_apps_summary, numApps, new Object[]{Integer.valueOf(numApps), durationString}));
    }

    public int getMetricsCategory() {
        return 202;
    }

    public int getHelpResource() {
        return R.string.help_uri_process_stats_summary;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != this.mAppListPreference) {
            return false;
        }
        Bundle args = new Bundle();
        args.putBoolean("transfer_stats", true);
        args.putInt("duration_index", this.mDurationIndex);
        this.mStatsManager.xferStats();
        new SubSettingLauncher(getContext()).setDestination(ProcessStatsUi.class.getName()).setTitle((int) R.string.memory_usage_apps).setArguments(args).setSourceMetricsCategory(getMetricsCategory()).launch();
        return true;
    }
}
