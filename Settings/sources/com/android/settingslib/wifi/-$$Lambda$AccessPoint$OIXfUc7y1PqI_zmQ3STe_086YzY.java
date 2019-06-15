package com.android.settingslib.wifi;

import java.util.Iterator;
import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AccessPoint$OIXfUc7y1PqI_zmQ3STe_086YzY implements Consumer {
    private final /* synthetic */ long f$0;
    private final /* synthetic */ Iterator f$1;

    public /* synthetic */ -$$Lambda$AccessPoint$OIXfUc7y1PqI_zmQ3STe_086YzY(long j, Iterator it) {
        this.f$0 = j;
        this.f$1 = it;
    }

    public final void accept(Object obj) {
        AccessPoint.lambda$updateScores$0(this.f$0, this.f$1, (TimestampedScoredNetwork) obj);
    }
}
