package com.android.settings.dashboard;

import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import java.util.function.Predicate;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$DashboardFragment$S-iRpeKDC_3jmfXOTbVaWpa8f5Y implements Predicate {
    public static final /* synthetic */ -$$Lambda$DashboardFragment$S-iRpeKDC_3jmfXOTbVaWpa8f5Y INSTANCE = new -$$Lambda$DashboardFragment$S-iRpeKDC_3jmfXOTbVaWpa8f5Y();

    private /* synthetic */ -$$Lambda$DashboardFragment$S-iRpeKDC_3jmfXOTbVaWpa8f5Y() {
    }

    public final boolean test(Object obj) {
        return (((BasePreferenceController) obj) instanceof LifecycleObserver);
    }
}
