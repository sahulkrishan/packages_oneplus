package com.android.settings.slices;

import android.app.PendingIntent;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SliceBuilderUtils$NVqOOBEdIdirrSxUZFgCZXRQ1vA implements Consumer {
    private final /* synthetic */ SliceData f$0;
    private final /* synthetic */ CharSequence f$1;
    private final /* synthetic */ PendingIntent f$2;
    private final /* synthetic */ IconCompat f$3;

    public /* synthetic */ -$$Lambda$SliceBuilderUtils$NVqOOBEdIdirrSxUZFgCZXRQ1vA(SliceData sliceData, CharSequence charSequence, PendingIntent pendingIntent, IconCompat iconCompat) {
        this.f$0 = sliceData;
        this.f$1 = charSequence;
        this.f$2 = pendingIntent;
        this.f$3 = iconCompat;
    }

    public final void accept(Object obj) {
        ((RowBuilder) obj).setTitle(this.f$0.getTitle()).setSubtitle(this.f$1).setPrimaryAction(new SliceAction(this.f$2, this.f$3, this.f$0.getTitle()));
    }
}
