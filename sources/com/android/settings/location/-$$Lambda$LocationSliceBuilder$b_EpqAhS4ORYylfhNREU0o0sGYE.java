package com.android.settings.location;

import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$LocationSliceBuilder$b_EpqAhS4ORYylfhNREU0o0sGYE implements Consumer {
    private final /* synthetic */ String f$0;
    private final /* synthetic */ IconCompat f$1;
    private final /* synthetic */ SliceAction f$2;

    public /* synthetic */ -$$Lambda$LocationSliceBuilder$b_EpqAhS4ORYylfhNREU0o0sGYE(String str, IconCompat iconCompat, SliceAction sliceAction) {
        this.f$0 = str;
        this.f$1 = iconCompat;
        this.f$2 = sliceAction;
    }

    public final void accept(Object obj) {
        ((RowBuilder) obj).setTitle(this.f$0).setTitleItem(this.f$1, 0).setPrimaryAction(this.f$2);
    }
}
