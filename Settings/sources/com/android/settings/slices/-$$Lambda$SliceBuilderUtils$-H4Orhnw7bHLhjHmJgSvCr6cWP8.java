package com.android.settings.slices;

import android.app.PendingIntent;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SliceBuilderUtils$-H4Orhnw7bHLhjHmJgSvCr6cWP8 implements Consumer {
    private final /* synthetic */ SliceData f$0;
    private final /* synthetic */ CharSequence f$1;
    private final /* synthetic */ PendingIntent f$2;
    private final /* synthetic */ IconCompat f$3;
    private final /* synthetic */ SliceAction f$4;

    public /* synthetic */ -$$Lambda$SliceBuilderUtils$-H4Orhnw7bHLhjHmJgSvCr6cWP8(SliceData sliceData, CharSequence charSequence, PendingIntent pendingIntent, IconCompat iconCompat, SliceAction sliceAction) {
        this.f$0 = sliceData;
        this.f$1 = charSequence;
        this.f$2 = pendingIntent;
        this.f$3 = iconCompat;
        this.f$4 = sliceAction;
    }

    public final void accept(Object obj) {
        ((RowBuilder) obj).setTitle(this.f$0.getTitle()).setSubtitle(this.f$1).setPrimaryAction(new SliceAction(this.f$2, this.f$3, this.f$0.getTitle())).addEndItem(this.f$4);
    }
}
