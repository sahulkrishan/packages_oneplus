package com.android.settings.development;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.Formatter;
import android.util.Log;
import com.android.settings.applications.ProcStatsData;
import com.android.settings.applications.ProcStatsData.MemInfo;
import com.android.settings.applications.ProcessStatsBase;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.utils.ThreadUtils;

public class MemoryUsagePreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private static final String MEMORY_USAGE_KEY = "memory";
    private ProcStatsData mProcStatsData;

    public MemoryUsagePreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return MEMORY_USAGE_KEY;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mProcStatsData = getProcStatsData();
        setDuration();
    }

    public void updateState(Preference preference) {
        ThreadUtils.postOnBackgroundThread(new -$$Lambda$MemoryUsagePreferenceController$2UovDioLDVLRpJrL4IsFsRdoZts(this));
    }

    public static /* synthetic */ void lambda$updateState$1(MemoryUsagePreferenceController memoryUsagePreferenceController) {
        Log.d(PreferenceControllerMixin.TAG, "postOnBackgroundThread updateState start");
        memoryUsagePreferenceController.mProcStatsData.refreshStats(true);
        MemInfo memInfo = memoryUsagePreferenceController.mProcStatsData.getMemInfo();
        ThreadUtils.postOnMainThread(new -$$Lambda$MemoryUsagePreferenceController$jVfwyLcntt7OQNk4ZzyeXShgglc(memoryUsagePreferenceController, Formatter.formatShortFileSize(memoryUsagePreferenceController.mContext, (long) memInfo.realUsedRam), Formatter.formatShortFileSize(memoryUsagePreferenceController.mContext, (long) memInfo.realTotalRam)));
        Log.d(PreferenceControllerMixin.TAG, "postOnBackgroundThread updateState end");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setDuration() {
        this.mProcStatsData.setDuration(ProcessStatsBase.sDurations[0]);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public ProcStatsData getProcStatsData() {
        return new ProcStatsData(this.mContext, false);
    }
}
