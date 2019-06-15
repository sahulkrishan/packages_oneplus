package com.android.settings.wifi;

import com.android.settings.LinkifyUtils.OnClickListener;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.location.ScanningSettings;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiSettings$G0-vWzmi3g45SjhkhuPVMzYpO5w implements OnClickListener {
    private final /* synthetic */ WifiSettings f$0;

    public /* synthetic */ -$$Lambda$WifiSettings$G0-vWzmi3g45SjhkhuPVMzYpO5w(WifiSettings wifiSettings) {
        this.f$0 = wifiSettings;
    }

    public final void onClick() {
        new SubSettingLauncher(this.f$0.getContext()).setDestination(ScanningSettings.class.getName()).setTitle((int) R.string.location_scanning_screen_title).setSourceMetricsCategory(this.f$0.getMetricsCategory()).launch();
    }
}
