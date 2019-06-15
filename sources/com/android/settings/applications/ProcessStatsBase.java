package com.android.settings.applications;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.android.internal.app.procstats.ProcessStats;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.applications.ProcStatsData.MemInfo;
import com.android.settings.core.SubSettingLauncher;
import com.oneplus.settings.timer.timepower.SettingsUtil;

public abstract class ProcessStatsBase extends SettingsPreferenceFragment implements OnItemSelectedListener {
    protected static final String ARG_DURATION_INDEX = "duration_index";
    protected static final String ARG_TRANSFER_STATS = "transfer_stats";
    private static final String DURATION = "duration";
    private static final long DURATION_QUANTUM = ProcessStats.COMMIT_PERIOD;
    protected static final int NUM_DURATIONS = 4;
    protected static int[] sDurationLabels = new int[]{R.string.menu_duration_3h, R.string.menu_duration_6h, R.string.menu_duration_12h, R.string.menu_duration_1d};
    public static long[] sDurations = new long[]{10800000 - (DURATION_QUANTUM / 2), 21600000 - (DURATION_QUANTUM / 2), 43200000 - (DURATION_QUANTUM / 2), SettingsUtil.MILLIS_OF_DAY - (DURATION_QUANTUM / 2)};
    protected int mDurationIndex;
    private ArrayAdapter<String> mFilterAdapter;
    private Spinner mFilterSpinner;
    private ViewGroup mSpinnerHeader;
    protected ProcStatsData mStatsManager;

    public abstract void refreshUi();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle args = getArguments();
        Activity activity = getActivity();
        boolean z = icicle != null || (args != null && args.getBoolean(ARG_TRANSFER_STATS, false));
        this.mStatsManager = new ProcStatsData(activity, z);
        int i = icicle != null ? icicle.getInt(ARG_DURATION_INDEX) : args != null ? args.getInt(ARG_DURATION_INDEX) : 0;
        this.mDurationIndex = i;
        this.mStatsManager.setDuration(icicle != null ? icicle.getLong(DURATION, sDurations[0]) : sDurations[0]);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DURATION, this.mStatsManager.getDuration());
        outState.putInt(ARG_DURATION_INDEX, this.mDurationIndex);
    }

    public void onResume() {
        super.onResume();
        this.mStatsManager.refreshStats(false);
        refreshUi();
    }

    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isChangingConfigurations()) {
            this.mStatsManager.xferStats();
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mSpinnerHeader = (ViewGroup) setPinnedHeaderView((int) R.layout.apps_filter_spinner);
        this.mFilterSpinner = (Spinner) this.mSpinnerHeader.findViewById(R.id.filter_spinner);
        this.mFilterAdapter = new ArrayAdapter(this.mFilterSpinner.getContext(), R.layout.filter_spinner_item);
        this.mFilterAdapter.setDropDownViewResource(17367049);
        for (int i = 0; i < 4; i++) {
            this.mFilterAdapter.add(getString(sDurationLabels[i]));
        }
        this.mFilterSpinner.setAdapter(this.mFilterAdapter);
        this.mFilterSpinner.setSelection(this.mDurationIndex);
        this.mFilterSpinner.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        this.mDurationIndex = position;
        this.mStatsManager.setDuration(sDurations[position]);
        refreshUi();
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
        this.mFilterSpinner.setSelection(0);
    }

    public static void launchMemoryDetail(SettingsActivity activity, MemInfo memInfo, ProcStatsPackageEntry entry, boolean includeAppInfo) {
        Bundle args = new Bundle();
        args.putParcelable(ProcessStatsDetail.EXTRA_PACKAGE_ENTRY, entry);
        args.putDouble(ProcessStatsDetail.EXTRA_WEIGHT_TO_RAM, memInfo.weightToRam);
        args.putLong(ProcessStatsDetail.EXTRA_TOTAL_TIME, memInfo.memTotalTime);
        args.putDouble(ProcessStatsDetail.EXTRA_MAX_MEMORY_USAGE, memInfo.usedWeight * memInfo.weightToRam);
        args.putDouble(ProcessStatsDetail.EXTRA_TOTAL_SCALE, memInfo.totalScale);
        new SubSettingLauncher(activity).setDestination(ProcessStatsDetail.class.getName()).setTitle((int) R.string.memory_usage).setArguments(args).setSourceMetricsCategory(0).launch();
    }
}
