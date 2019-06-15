package com.android.settings.datausage;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DataUsageSummaryPreference$1NKWVGupHVFnsudApVgFBRMGUJg implements OnClickListener {
    private final /* synthetic */ DataUsageSummaryPreference f$0;

    public /* synthetic */ -$$Lambda$DataUsageSummaryPreference$1NKWVGupHVFnsudApVgFBRMGUJg(DataUsageSummaryPreference dataUsageSummaryPreference) {
        this.f$0 = dataUsageSummaryPreference;
    }

    public final void onClick(View view) {
        this.f$0.getContext().startActivity(this.f$0.mLaunchIntent);
    }
}
