package com.android.settings.enterprise;

import com.android.settings.applications.ApplicationFeatureProvider.NumberOfAppsCallback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AdminGrantedPermissionsPreferenceControllerBase$4ZAcP8cSJJvD_RXkeJP9Rdjuu0k implements NumberOfAppsCallback {
    private final /* synthetic */ Boolean[] f$0;

    public /* synthetic */ -$$Lambda$AdminGrantedPermissionsPreferenceControllerBase$4ZAcP8cSJJvD_RXkeJP9Rdjuu0k(Boolean[] boolArr) {
        this.f$0 = boolArr;
    }

    public final void onNumberOfAppsResult(int i) {
        AdminGrantedPermissionsPreferenceControllerBase.lambda$isAvailable$1(this.f$0, i);
    }
}
