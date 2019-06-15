package com.android.settings.datetime.timezone;

import com.android.settings.datetime.timezone.model.TimeZoneData;
import com.android.settings.datetime.timezone.model.TimeZoneDataLoader.OnDataReadyCallback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MBKbnic3yruONZHLQGUj0vAB5hk implements OnDataReadyCallback {
    private final /* synthetic */ BaseTimeZonePicker f$0;

    public /* synthetic */ -$$Lambda$MBKbnic3yruONZHLQGUj0vAB5hk(BaseTimeZonePicker baseTimeZonePicker) {
        this.f$0 = baseTimeZonePicker;
    }

    public final void onTimeZoneDataReady(TimeZoneData timeZoneData) {
        this.f$0.onTimeZoneDataReady(timeZoneData);
    }
}
