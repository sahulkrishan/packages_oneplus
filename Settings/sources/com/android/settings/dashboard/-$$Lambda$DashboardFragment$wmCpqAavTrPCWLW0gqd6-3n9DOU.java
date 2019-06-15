package com.android.settings.dashboard;

import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardFragment$wmCpqAavTrPCWLW0gqd6-3n9DOU implements Consumer {
    private final /* synthetic */ PreferenceScreen f$0;

    public /* synthetic */ -$$Lambda$DashboardFragment$wmCpqAavTrPCWLW0gqd6-3n9DOU(PreferenceScreen preferenceScreen) {
        this.f$0 = preferenceScreen;
    }

    public final void accept(Object obj) {
        ((AbstractPreferenceController) obj).displayPreference(this.f$0);
    }
}
