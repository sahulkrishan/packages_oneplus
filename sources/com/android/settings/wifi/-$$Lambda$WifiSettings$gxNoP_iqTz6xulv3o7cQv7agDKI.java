package com.android.settings.wifi;

import com.android.settings.wifi.ConnectedAccessPointPreference.OnGearClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiSettings$gxNoP_iqTz6xulv3o7cQv7agDKI implements OnGearClickListener {
    private final /* synthetic */ WifiSettings f$0;
    private final /* synthetic */ ConnectedAccessPointPreference f$1;

    public /* synthetic */ -$$Lambda$WifiSettings$gxNoP_iqTz6xulv3o7cQv7agDKI(WifiSettings wifiSettings, ConnectedAccessPointPreference connectedAccessPointPreference) {
        this.f$0 = wifiSettings;
        this.f$1 = connectedAccessPointPreference;
    }

    public final void onGearClick(ConnectedAccessPointPreference connectedAccessPointPreference) {
        WifiSettings.lambda$addConnectedAccessPointPreference$3(this.f$0, this.f$1, connectedAccessPointPreference);
    }
}
