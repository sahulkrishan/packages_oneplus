package com.android.settings.slices;

import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SliceBuilderUtils$JGXESizo03yh-FrnCdjYorH4I8Y implements Consumer {
    private final /* synthetic */ String f$0;
    private final /* synthetic */ IconCompat f$1;
    private final /* synthetic */ CharSequence f$2;
    private final /* synthetic */ SliceAction f$3;

    public /* synthetic */ -$$Lambda$SliceBuilderUtils$JGXESizo03yh-FrnCdjYorH4I8Y(String str, IconCompat iconCompat, CharSequence charSequence, SliceAction sliceAction) {
        this.f$0 = str;
        this.f$1 = iconCompat;
        this.f$2 = charSequence;
        this.f$3 = sliceAction;
    }

    public final void accept(Object obj) {
        ((RowBuilder) obj).setTitle(this.f$0).setTitleItem(this.f$1).setSubtitle(this.f$2).setPrimaryAction(this.f$3);
    }
}
