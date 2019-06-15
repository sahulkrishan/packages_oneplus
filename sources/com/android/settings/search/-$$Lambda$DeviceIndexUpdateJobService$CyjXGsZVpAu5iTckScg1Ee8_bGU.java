package com.android.settings.search;

import android.app.job.JobParameters;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DeviceIndexUpdateJobService$CyjXGsZVpAu5iTckScg1Ee8_bGU implements Runnable {
    private final /* synthetic */ DeviceIndexUpdateJobService f$0;
    private final /* synthetic */ JobParameters f$1;

    public /* synthetic */ -$$Lambda$DeviceIndexUpdateJobService$CyjXGsZVpAu5iTckScg1Ee8_bGU(DeviceIndexUpdateJobService deviceIndexUpdateJobService, JobParameters jobParameters) {
        this.f$0 = deviceIndexUpdateJobService;
        this.f$1 = jobParameters;
    }

    public final void run() {
        this.f$0.updateIndex(this.f$1);
    }
}
