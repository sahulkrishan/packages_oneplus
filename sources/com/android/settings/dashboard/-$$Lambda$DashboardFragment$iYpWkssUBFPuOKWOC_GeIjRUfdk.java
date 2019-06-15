package com.android.settings.dashboard;

import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import java.util.function.Consumer;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardFragment$iYpWkssUBFPuOKWOC_GeIjRUfdk implements Consumer {
    private final /* synthetic */ Lifecycle f$0;

    public /* synthetic */ -$$Lambda$DashboardFragment$iYpWkssUBFPuOKWOC_GeIjRUfdk(Lifecycle lifecycle) {
        this.f$0 = lifecycle;
    }

    public final void accept(Object obj) {
        this.f$0.addObserver((LifecycleObserver) ((BasePreferenceController) obj));
    }
}
