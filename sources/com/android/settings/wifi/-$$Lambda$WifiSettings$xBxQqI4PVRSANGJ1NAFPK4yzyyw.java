package com.android.settings.wifi;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiSettings$xBxQqI4PVRSANGJ1NAFPK4yzyyw implements OnPreferenceClickListener {
    private final /* synthetic */ WifiSettings f$0;
    private final /* synthetic */ ConnectedAccessPointPreference f$1;

    public /* synthetic */ -$$Lambda$WifiSettings$xBxQqI4PVRSANGJ1NAFPK4yzyyw(WifiSettings wifiSettings, ConnectedAccessPointPreference connectedAccessPointPreference) {
        this.f$0 = wifiSettings;
        this.f$1 = connectedAccessPointPreference;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return WifiSettings.lambda$addConnectedAccessPointPreference$2(this.f$0, this.f$1, preference);
    }
}
