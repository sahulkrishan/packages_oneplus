package com.android.settings.wifi.calling;

import android.app.PendingIntent;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiCallingSliceHelper$6JNBI7DQgipwzIQhGGlqsYB5PlI implements Consumer {
    private final /* synthetic */ String f$0;
    private final /* synthetic */ String f$1;
    private final /* synthetic */ PendingIntent f$2;
    private final /* synthetic */ IconCompat f$3;

    public /* synthetic */ -$$Lambda$WifiCallingSliceHelper$6JNBI7DQgipwzIQhGGlqsYB5PlI(String str, String str2, PendingIntent pendingIntent, IconCompat iconCompat) {
        this.f$0 = str;
        this.f$1 = str2;
        this.f$2 = pendingIntent;
        this.f$3 = iconCompat;
    }

    public final void accept(Object obj) {
        ((RowBuilder) obj).setTitle(this.f$0).setSubtitle(this.f$1).setPrimaryAction(new SliceAction(this.f$2, this.f$3, (CharSequence) this.f$0));
    }
}
