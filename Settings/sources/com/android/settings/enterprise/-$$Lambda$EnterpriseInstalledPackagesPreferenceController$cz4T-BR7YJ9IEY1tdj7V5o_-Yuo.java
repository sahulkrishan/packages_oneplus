package com.android.settings.enterprise;

import com.android.settings.applications.ApplicationFeatureProvider.NumberOfAppsCallback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$EnterpriseInstalledPackagesPreferenceController$cz4T-BR7YJ9IEY1tdj7V5o_-Yuo implements NumberOfAppsCallback {
    private final /* synthetic */ Boolean[] f$0;

    public /* synthetic */ -$$Lambda$EnterpriseInstalledPackagesPreferenceController$cz4T-BR7YJ9IEY1tdj7V5o_-Yuo(Boolean[] boolArr) {
        this.f$0 = boolArr;
    }

    public final void onNumberOfAppsResult(int i) {
        EnterpriseInstalledPackagesPreferenceController.lambda$isAvailable$1(this.f$0, i);
    }
}
