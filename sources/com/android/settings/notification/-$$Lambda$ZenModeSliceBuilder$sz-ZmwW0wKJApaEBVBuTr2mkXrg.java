package com.android.settings.notification;

import android.support.v4.util.Consumer;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ZenModeSliceBuilder$sz-ZmwW0wKJApaEBVBuTr2mkXrg implements Consumer {
    private final /* synthetic */ CharSequence f$0;
    private final /* synthetic */ SliceAction f$1;
    private final /* synthetic */ SliceAction f$2;

    public /* synthetic */ -$$Lambda$ZenModeSliceBuilder$sz-ZmwW0wKJApaEBVBuTr2mkXrg(CharSequence charSequence, SliceAction sliceAction, SliceAction sliceAction2) {
        this.f$0 = charSequence;
        this.f$1 = sliceAction;
        this.f$2 = sliceAction2;
    }

    public final void accept(Object obj) {
        ((RowBuilder) obj).setTitle(this.f$0).addEndItem(this.f$1).setPrimaryAction(this.f$2);
    }
}
