package com.android.settings.dashboard;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RestrictedDashboardFragment$xeipMNP1JUFbhaWoStBNgM1y67g implements OnDismissListener {
    private final /* synthetic */ RestrictedDashboardFragment f$0;

    public /* synthetic */ -$$Lambda$RestrictedDashboardFragment$xeipMNP1JUFbhaWoStBNgM1y67g(RestrictedDashboardFragment restrictedDashboardFragment) {
        this.f$0 = restrictedDashboardFragment;
    }

    public final void onDismiss(DialogInterface dialogInterface) {
        this.f$0.getActivity().finish();
    }
}
