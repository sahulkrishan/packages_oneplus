package com.android.settings.wifi.calling;

import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiCallingSliceHelper$zbtZymRgbM5OtQTuVraAeUKJDfQ implements Consumer {
    private final /* synthetic */ WifiCallingSliceHelper f$0;
    private final /* synthetic */ String f$1;
    private final /* synthetic */ boolean f$2;
    private final /* synthetic */ IconCompat f$3;

    public /* synthetic */ -$$Lambda$WifiCallingSliceHelper$zbtZymRgbM5OtQTuVraAeUKJDfQ(WifiCallingSliceHelper wifiCallingSliceHelper, String str, boolean z, IconCompat iconCompat) {
        this.f$0 = wifiCallingSliceHelper;
        this.f$1 = str;
        this.f$2 = z;
        this.f$3 = iconCompat;
    }

    public final void accept(Object obj) {
        ((RowBuilder) obj).setTitle(this.f$1).addEndItem(new SliceAction(this.f$0.getBroadcastIntent(WifiCallingSliceHelper.ACTION_WIFI_CALLING_CHANGED), null, this.f$2)).setPrimaryAction(new SliceAction(this.f$0.getActivityIntent(WifiCallingSliceHelper.ACTION_WIFI_CALLING_SETTINGS_ACTIVITY), this.f$3, (CharSequence) this.f$1));
    }
}
