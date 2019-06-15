package com.android.settings.widget;

import android.support.v7.widget.RecyclerView;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$HighlightablePreferenceGroupAdapter$Xc5BA2nCks8YuSzn7LsPZS7EmPA implements Runnable {
    private final /* synthetic */ HighlightablePreferenceGroupAdapter f$0;
    private final /* synthetic */ RecyclerView f$1;

    public /* synthetic */ -$$Lambda$HighlightablePreferenceGroupAdapter$Xc5BA2nCks8YuSzn7LsPZS7EmPA(HighlightablePreferenceGroupAdapter highlightablePreferenceGroupAdapter, RecyclerView recyclerView) {
        this.f$0 = highlightablePreferenceGroupAdapter;
        this.f$1 = recyclerView;
    }

    public final void run() {
        HighlightablePreferenceGroupAdapter.lambda$requestHighlight$0(this.f$0, this.f$1);
    }
}
