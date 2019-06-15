package com.android.settings.wfd;

import android.app.Activity;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProvider;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiDisplaySettings$FSGRkDMrB620EgLXH7J2ShDkw60 implements SummaryProviderFactory {
    public static final /* synthetic */ -$$Lambda$WifiDisplaySettings$FSGRkDMrB620EgLXH7J2ShDkw60 INSTANCE = new -$$Lambda$WifiDisplaySettings$FSGRkDMrB620EgLXH7J2ShDkw60();

    private /* synthetic */ -$$Lambda$WifiDisplaySettings$FSGRkDMrB620EgLXH7J2ShDkw60() {
    }

    public final SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
        return new SummaryProvider(activity, summaryLoader);
    }
}
