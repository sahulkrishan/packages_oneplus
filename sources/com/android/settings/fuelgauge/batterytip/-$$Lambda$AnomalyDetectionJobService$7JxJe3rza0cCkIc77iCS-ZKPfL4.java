package com.android.settings.fuelgauge.batterytip;

import android.app.job.JobParameters;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AnomalyDetectionJobService$7JxJe3rza0cCkIc77iCS-ZKPfL4 implements Runnable {
    private final /* synthetic */ AnomalyDetectionJobService f$0;
    private final /* synthetic */ JobParameters f$1;

    public /* synthetic */ -$$Lambda$AnomalyDetectionJobService$7JxJe3rza0cCkIc77iCS-ZKPfL4(AnomalyDetectionJobService anomalyDetectionJobService, JobParameters jobParameters) {
        this.f$0 = anomalyDetectionJobService;
        this.f$1 = jobParameters;
    }

    public final void run() {
        AnomalyDetectionJobService.lambda$onStartJob$0(this.f$0, this.f$1);
    }
}
