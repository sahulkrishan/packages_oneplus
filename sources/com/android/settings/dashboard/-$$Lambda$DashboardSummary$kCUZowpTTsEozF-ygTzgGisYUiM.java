package com.android.settings.dashboard;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardSummary$kCUZowpTTsEozF-ygTzgGisYUiM implements Runnable {
    private final /* synthetic */ DashboardSummary f$0;

    public /* synthetic */ -$$Lambda$DashboardSummary$kCUZowpTTsEozF-ygTzgGisYUiM(DashboardSummary dashboardSummary) {
        this.f$0 = dashboardSummary;
    }

    public final void run() {
        this.f$0.mAdapter.setCategory(this.f$0.mStagingCategory);
    }
}