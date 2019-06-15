package com.android.settings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$RestrictedSettingsFragment$LUdTuWQX3d8kcdKiPapl2FlA0-c implements OnDismissListener {
    private final /* synthetic */ RestrictedSettingsFragment f$0;

    public /* synthetic */ -$$Lambda$RestrictedSettingsFragment$LUdTuWQX3d8kcdKiPapl2FlA0-c(RestrictedSettingsFragment restrictedSettingsFragment) {
        this.f$0 = restrictedSettingsFragment;
    }

    public final void onDismiss(DialogInterface dialogInterface) {
        this.f$0.getActivity().finish();
    }
}
