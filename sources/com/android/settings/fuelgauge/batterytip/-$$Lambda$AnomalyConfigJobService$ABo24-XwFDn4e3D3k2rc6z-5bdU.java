package com.android.settings.fuelgauge.batterytip;

import android.app.job.JobParameters;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AnomalyConfigJobService$ABo24-XwFDn4e3D3k2rc6z-5bdU implements Runnable {
    private final /* synthetic */ AnomalyConfigJobService f$0;
    private final /* synthetic */ JobParameters f$1;

    public /* synthetic */ -$$Lambda$AnomalyConfigJobService$ABo24-XwFDn4e3D3k2rc6z-5bdU(AnomalyConfigJobService anomalyConfigJobService, JobParameters jobParameters) {
        this.f$0 = anomalyConfigJobService;
        this.f$1 = jobParameters;
    }

    public final void run() {
        AnomalyConfigJobService.lambda$onStartJob$0(this.f$0, this.f$1);
    }
}
