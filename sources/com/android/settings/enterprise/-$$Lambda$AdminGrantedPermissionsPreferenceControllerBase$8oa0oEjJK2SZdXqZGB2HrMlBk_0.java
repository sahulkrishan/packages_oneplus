package com.android.settings.enterprise;

import android.support.v7.preference.Preference;
import com.android.settings.applications.ApplicationFeatureProvider.NumberOfAppsCallback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AdminGrantedPermissionsPreferenceControllerBase$8oa0oEjJK2SZdXqZGB2HrMlBk_0 implements NumberOfAppsCallback {
    private final /* synthetic */ AdminGrantedPermissionsPreferenceControllerBase f$0;
    private final /* synthetic */ Preference f$1;

    public /* synthetic */ -$$Lambda$AdminGrantedPermissionsPreferenceControllerBase$8oa0oEjJK2SZdXqZGB2HrMlBk_0(AdminGrantedPermissionsPreferenceControllerBase adminGrantedPermissionsPreferenceControllerBase, Preference preference) {
        this.f$0 = adminGrantedPermissionsPreferenceControllerBase;
        this.f$1 = preference;
    }

    public final void onNumberOfAppsResult(int i) {
        AdminGrantedPermissionsPreferenceControllerBase.lambda$updateState$0(this.f$0, this.f$1, i);
    }
}
