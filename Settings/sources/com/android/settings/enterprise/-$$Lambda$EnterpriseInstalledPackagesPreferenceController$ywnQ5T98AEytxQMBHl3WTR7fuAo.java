package com.android.settings.enterprise;

import android.support.v7.preference.Preference;
import com.android.settings.applications.ApplicationFeatureProvider.NumberOfAppsCallback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$EnterpriseInstalledPackagesPreferenceController$ywnQ5T98AEytxQMBHl3WTR7fuAo implements NumberOfAppsCallback {
    private final /* synthetic */ EnterpriseInstalledPackagesPreferenceController f$0;
    private final /* synthetic */ Preference f$1;

    public /* synthetic */ -$$Lambda$EnterpriseInstalledPackagesPreferenceController$ywnQ5T98AEytxQMBHl3WTR7fuAo(EnterpriseInstalledPackagesPreferenceController enterpriseInstalledPackagesPreferenceController, Preference preference) {
        this.f$0 = enterpriseInstalledPackagesPreferenceController;
        this.f$1 = preference;
    }

    public final void onNumberOfAppsResult(int i) {
        EnterpriseInstalledPackagesPreferenceController.lambda$updateState$0(this.f$0, this.f$1, i);
    }
}
