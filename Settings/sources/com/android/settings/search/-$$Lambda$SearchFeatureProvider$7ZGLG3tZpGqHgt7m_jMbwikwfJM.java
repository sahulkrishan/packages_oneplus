package com.android.settings.search;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SearchFeatureProvider$7ZGLG3tZpGqHgt7m_jMbwikwfJM implements OnClickListener {
    private final /* synthetic */ SearchFeatureProvider f$0;
    private final /* synthetic */ Activity f$1;

    public /* synthetic */ -$$Lambda$SearchFeatureProvider$7ZGLG3tZpGqHgt7m_jMbwikwfJM(SearchFeatureProvider searchFeatureProvider, Activity activity) {
        this.f$0 = searchFeatureProvider;
        this.f$1 = activity;
    }

    public final void onClick(View view) {
        SearchFeatureProvider.lambda$initSearchToolbar$0(this.f$0, this.f$1, view);
    }
}
