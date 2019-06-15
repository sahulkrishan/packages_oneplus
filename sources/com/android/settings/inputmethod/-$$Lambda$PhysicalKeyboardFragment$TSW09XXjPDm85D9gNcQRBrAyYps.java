package com.android.settings.inputmethod;

import java.util.List;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$PhysicalKeyboardFragment$TSW09XXjPDm85D9gNcQRBrAyYps implements Runnable {
    private final /* synthetic */ PhysicalKeyboardFragment f$0;
    private final /* synthetic */ List f$1;

    public /* synthetic */ -$$Lambda$PhysicalKeyboardFragment$TSW09XXjPDm85D9gNcQRBrAyYps(PhysicalKeyboardFragment physicalKeyboardFragment, List list) {
        this.f$0 = physicalKeyboardFragment;
        this.f$1 = list;
    }

    public final void run() {
        this.f$0.updateHardKeyboards(this.f$1);
    }
}
