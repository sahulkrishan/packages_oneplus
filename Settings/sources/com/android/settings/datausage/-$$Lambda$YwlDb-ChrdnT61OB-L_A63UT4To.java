package com.android.settings.datausage;

import android.app.Activity;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProvider;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$YwlDb-ChrdnT61OB-L_A63UT4To implements SummaryProviderFactory {
    public static final /* synthetic */ -$$Lambda$YwlDb-ChrdnT61OB-L_A63UT4To INSTANCE = new -$$Lambda$YwlDb-ChrdnT61OB-L_A63UT4To();

    private /* synthetic */ -$$Lambda$YwlDb-ChrdnT61OB-L_A63UT4To() {
    }

    public final SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
        return new SummaryProvider(activity, summaryLoader);
    }
}
