package com.android.settings.wifi.details;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$WifiDetailPreferenceController$HDOTYXVF80U7sCZa22KqorlzriY implements OnClickListener {
    private final /* synthetic */ WifiDetailPreferenceController f$0;

    public /* synthetic */ -$$Lambda$WifiDetailPreferenceController$HDOTYXVF80U7sCZa22KqorlzriY(WifiDetailPreferenceController wifiDetailPreferenceController) {
        this.f$0 = wifiDetailPreferenceController;
    }

    public final void onClick(View view) {
        this.f$0.forgetNetwork();
    }
}
