package com.android.settings.network;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$NetworkDashboardFragment$ezC2Ol_SOf4CDiS8HjkkdWzGu_s implements OnClickListener {
    private final /* synthetic */ MobilePlanPreferenceController f$0;

    public /* synthetic */ -$$Lambda$NetworkDashboardFragment$ezC2Ol_SOf4CDiS8HjkkdWzGu_s(MobilePlanPreferenceController mobilePlanPreferenceController) {
        this.f$0 = mobilePlanPreferenceController;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.setMobilePlanDialogMessage(null);
    }
}
