package com.android.settings.users;

import android.app.Activity;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProvider;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$UserSettings$Eg6plZiaX7G7UUvF4Q46lU8PMRw implements SummaryProviderFactory {
    public static final /* synthetic */ -$$Lambda$UserSettings$Eg6plZiaX7G7UUvF4Q46lU8PMRw INSTANCE = new -$$Lambda$UserSettings$Eg6plZiaX7G7UUvF4Q46lU8PMRw();

    private /* synthetic */ -$$Lambda$UserSettings$Eg6plZiaX7G7UUvF4Q46lU8PMRw() {
    }

    public final SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
        return new SummaryProvider(activity, summaryLoader);
    }
}
