package com.android.settings.widget;

import android.view.View;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$HighlightablePreferenceGroupAdapter$CKVsKq8Jy7vb9RpitMwq8ps1ehE implements Runnable {
    private final /* synthetic */ HighlightablePreferenceGroupAdapter f$0;
    private final /* synthetic */ View f$1;

    public /* synthetic */ -$$Lambda$HighlightablePreferenceGroupAdapter$CKVsKq8Jy7vb9RpitMwq8ps1ehE(HighlightablePreferenceGroupAdapter highlightablePreferenceGroupAdapter, View view) {
        this.f$0 = highlightablePreferenceGroupAdapter;
        this.f$1 = view;
    }

    public final void run() {
        HighlightablePreferenceGroupAdapter.lambda$requestRemoveHighlightDelayed$1(this.f$0, this.f$1);
    }
}
