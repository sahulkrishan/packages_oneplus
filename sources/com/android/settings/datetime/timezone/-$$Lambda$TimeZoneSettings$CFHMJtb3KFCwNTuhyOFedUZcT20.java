package com.android.settings.datetime.timezone;

import com.android.settings.datetime.timezone.model.TimeZoneData;
import com.android.settings.datetime.timezone.model.TimeZoneDataLoader.OnDataReadyCallback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$TimeZoneSettings$CFHMJtb3KFCwNTuhyOFedUZcT20 implements OnDataReadyCallback {
    private final /* synthetic */ TimeZoneSettings f$0;

    public /* synthetic */ -$$Lambda$TimeZoneSettings$CFHMJtb3KFCwNTuhyOFedUZcT20(TimeZoneSettings timeZoneSettings) {
        this.f$0 = timeZoneSettings;
    }

    public final void onTimeZoneDataReady(TimeZoneData timeZoneData) {
        this.f$0.onTimeZoneDataReady(timeZoneData);
    }
}
