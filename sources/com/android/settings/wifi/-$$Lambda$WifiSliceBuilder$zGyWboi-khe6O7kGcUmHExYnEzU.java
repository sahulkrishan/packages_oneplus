package com.android.settings.wifi;

import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiSliceBuilder$zGyWboi-khe6O7kGcUmHExYnEzU implements Consumer {
    private final /* synthetic */ String f$0;
    private final /* synthetic */ CharSequence f$1;
    private final /* synthetic */ SliceAction f$2;
    private final /* synthetic */ SliceAction f$3;

    public /* synthetic */ -$$Lambda$WifiSliceBuilder$zGyWboi-khe6O7kGcUmHExYnEzU(String str, CharSequence charSequence, SliceAction sliceAction, SliceAction sliceAction2) {
        this.f$0 = str;
        this.f$1 = charSequence;
        this.f$2 = sliceAction;
        this.f$3 = sliceAction2;
    }

    public final void accept(Object obj) {
        ((RowBuilder) obj).setTitle(this.f$0).setSubtitle(this.f$1).addEndItem(this.f$2).setPrimaryAction(this.f$3);
    }
}
