package com.android.settings.dashboard;

import android.graphics.drawable.Icon;
import android.support.v7.preference.Preference;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardFeatureProviderImpl$GREAS10FflfW9_XoMVZ4GOVTVF8 implements Runnable {
    private final /* synthetic */ Preference f$0;
    private final /* synthetic */ Icon f$1;

    public /* synthetic */ -$$Lambda$DashboardFeatureProviderImpl$GREAS10FflfW9_XoMVZ4GOVTVF8(Preference preference, Icon icon) {
        this.f$0 = preference;
        this.f$1 = icon;
    }

    public final void run() {
        this.f$0.setIcon(this.f$1.loadDrawable(this.f$0.getContext()));
    }
}
