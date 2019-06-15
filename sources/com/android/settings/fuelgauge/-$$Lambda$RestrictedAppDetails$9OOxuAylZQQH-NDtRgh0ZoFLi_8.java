package com.android.settings.fuelgauge;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.fuelgauge.batterytip.AppInfo;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RestrictedAppDetails$9OOxuAylZQQH-NDtRgh0ZoFLi_8 implements OnPreferenceChangeListener {
    private final /* synthetic */ RestrictedAppDetails f$0;
    private final /* synthetic */ AppInfo f$1;

    public /* synthetic */ -$$Lambda$RestrictedAppDetails$9OOxuAylZQQH-NDtRgh0ZoFLi_8(RestrictedAppDetails restrictedAppDetails, AppInfo appInfo) {
        this.f$0 = restrictedAppDetails;
        this.f$1 = appInfo;
    }

    public final boolean onPreferenceChange(Preference preference, Object obj) {
        return RestrictedAppDetails.lambda$refreshUi$0(this.f$0, this.f$1, preference, obj);
    }
}
