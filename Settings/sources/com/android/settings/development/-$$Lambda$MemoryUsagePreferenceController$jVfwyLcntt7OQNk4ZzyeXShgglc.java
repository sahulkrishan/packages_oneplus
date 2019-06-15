package com.android.settings.development;

import com.android.settings.R;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MemoryUsagePreferenceController$jVfwyLcntt7OQNk4ZzyeXShgglc implements Runnable {
    private final /* synthetic */ MemoryUsagePreferenceController f$0;
    private final /* synthetic */ String f$1;
    private final /* synthetic */ String f$2;

    public /* synthetic */ -$$Lambda$MemoryUsagePreferenceController$jVfwyLcntt7OQNk4ZzyeXShgglc(MemoryUsagePreferenceController memoryUsagePreferenceController, String str, String str2) {
        this.f$0 = memoryUsagePreferenceController;
        this.f$1 = str;
        this.f$2 = str2;
    }

    public final void run() {
        this.f$0.mPreference.setSummary(this.f$0.mContext.getString(R.string.memory_summary, new Object[]{this.f$1, this.f$2}));
    }
}
