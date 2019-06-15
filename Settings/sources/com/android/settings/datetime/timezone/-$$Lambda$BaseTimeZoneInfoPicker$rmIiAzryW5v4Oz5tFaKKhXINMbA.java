package com.android.settings.datetime.timezone;

import com.android.settings.datetime.timezone.BaseTimeZoneAdapter.AdapterItem;
import com.android.settings.datetime.timezone.BaseTimeZonePicker.OnListItemClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$BaseTimeZoneInfoPicker$rmIiAzryW5v4Oz5tFaKKhXINMbA implements OnListItemClickListener {
    private final /* synthetic */ BaseTimeZoneInfoPicker f$0;

    public /* synthetic */ -$$Lambda$BaseTimeZoneInfoPicker$rmIiAzryW5v4Oz5tFaKKhXINMbA(BaseTimeZoneInfoPicker baseTimeZoneInfoPicker) {
        this.f$0 = baseTimeZoneInfoPicker;
    }

    public final void onListItemClick(AdapterItem adapterItem) {
        this.f$0.onListItemClick((TimeZoneInfoItem) adapterItem);
    }
}
