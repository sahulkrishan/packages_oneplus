package com.android.settings.fuelgauge.batterytip;

import android.app.job.JobParameters;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AnomalyCleanupJobService$Wvu3W97OjsnNVurAIkZXTma9fMg implements Runnable {
    private final /* synthetic */ AnomalyCleanupJobService f$0;
    private final /* synthetic */ BatteryDatabaseManager f$1;
    private final /* synthetic */ BatteryTipPolicy f$2;
    private final /* synthetic */ JobParameters f$3;

    public /* synthetic */ -$$Lambda$AnomalyCleanupJobService$Wvu3W97OjsnNVurAIkZXTma9fMg(AnomalyCleanupJobService anomalyCleanupJobService, BatteryDatabaseManager batteryDatabaseManager, BatteryTipPolicy batteryTipPolicy, JobParameters jobParameters) {
        this.f$0 = anomalyCleanupJobService;
        this.f$1 = batteryDatabaseManager;
        this.f$2 = batteryTipPolicy;
        this.f$3 = jobParameters;
    }

    public final void run() {
        AnomalyCleanupJobService.lambda$onStartJob$0(this.f$0, this.f$1, this.f$2, this.f$3);
    }
}
