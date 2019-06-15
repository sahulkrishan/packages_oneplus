package com.android.settings.dashboard;

import android.support.v7.preference.Preference;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardFeatureProviderImpl$f6w3zqqhleyaUiHJCm70VP43jfI implements Runnable {
    private final /* synthetic */ Preference f$0;
    private final /* synthetic */ String f$1;

    public /* synthetic */ -$$Lambda$DashboardFeatureProviderImpl$f6w3zqqhleyaUiHJCm70VP43jfI(Preference preference, String str) {
        this.f$0 = preference;
        this.f$1 = str;
    }

    public final void run() {
        this.f$0.setSummary((CharSequence) this.f$1);
    }
}
