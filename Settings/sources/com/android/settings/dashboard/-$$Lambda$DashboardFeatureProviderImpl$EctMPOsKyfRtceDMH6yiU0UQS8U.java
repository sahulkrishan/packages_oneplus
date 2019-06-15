package com.android.settings.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settingslib.drawer.Tile;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardFeatureProviderImpl$EctMPOsKyfRtceDMH6yiU0UQS8U implements OnPreferenceClickListener {
    private final /* synthetic */ DashboardFeatureProviderImpl f$0;
    private final /* synthetic */ Activity f$1;
    private final /* synthetic */ Tile f$2;
    private final /* synthetic */ Intent f$3;
    private final /* synthetic */ int f$4;

    public /* synthetic */ -$$Lambda$DashboardFeatureProviderImpl$EctMPOsKyfRtceDMH6yiU0UQS8U(DashboardFeatureProviderImpl dashboardFeatureProviderImpl, Activity activity, Tile tile, Intent intent, int i) {
        this.f$0 = dashboardFeatureProviderImpl;
        this.f$1 = activity;
        this.f$2 = tile;
        this.f$3 = intent;
        this.f$4 = i;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return this.f$0.launchIntentOrSelectProfile(this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
