package com.android.settings.dashboard;

import android.support.v7.preference.Preference;
import com.android.settingslib.drawer.Tile;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardFeatureProviderImpl$eT0JYpovsB0-eUpWXkBH1qYJv_I implements Runnable {
    private final /* synthetic */ DashboardFeatureProviderImpl f$0;
    private final /* synthetic */ Tile f$1;
    private final /* synthetic */ Preference f$2;

    public /* synthetic */ -$$Lambda$DashboardFeatureProviderImpl$eT0JYpovsB0-eUpWXkBH1qYJv_I(DashboardFeatureProviderImpl dashboardFeatureProviderImpl, Tile tile, Preference preference) {
        this.f$0 = dashboardFeatureProviderImpl;
        this.f$1 = tile;
        this.f$2 = preference;
    }

    public final void run() {
        DashboardFeatureProviderImpl.lambda$bindSummary$2(this.f$0, this.f$1, this.f$2);
    }
}
