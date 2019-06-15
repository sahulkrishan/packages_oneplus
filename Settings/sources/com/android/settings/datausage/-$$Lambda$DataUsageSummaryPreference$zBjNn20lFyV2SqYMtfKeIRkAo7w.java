package com.android.settings.datausage;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DataUsageSummaryPreference$zBjNn20lFyV2SqYMtfKeIRkAo7w implements OnClickListener {
    private final /* synthetic */ DataUsageSummaryPreference f$0;

    public /* synthetic */ -$$Lambda$DataUsageSummaryPreference$zBjNn20lFyV2SqYMtfKeIRkAo7w(DataUsageSummaryPreference dataUsageSummaryPreference) {
        this.f$0 = dataUsageSummaryPreference;
    }

    public final void onClick(View view) {
        DataUsageSummaryPreference.launchWifiDataUsage(this.f$0.getContext());
    }
}
