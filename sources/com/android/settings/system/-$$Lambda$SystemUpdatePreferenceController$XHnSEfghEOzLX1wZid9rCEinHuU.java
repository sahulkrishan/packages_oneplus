package com.android.settings.system;

import java.util.concurrent.Callable;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$SystemUpdatePreferenceController$XHnSEfghEOzLX1wZid9rCEinHuU implements Callable {
    private final /* synthetic */ SystemUpdatePreferenceController f$0;

    public /* synthetic */ -$$Lambda$SystemUpdatePreferenceController$XHnSEfghEOzLX1wZid9rCEinHuU(SystemUpdatePreferenceController systemUpdatePreferenceController) {
        this.f$0 = systemUpdatePreferenceController;
    }

    public final Object call() {
        return this.f$0.mUpdateManager.retrieveSystemUpdateInfo();
    }
}
