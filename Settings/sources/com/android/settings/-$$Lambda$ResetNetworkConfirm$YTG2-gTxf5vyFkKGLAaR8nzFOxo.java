package com.android.settings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ResetNetworkConfirm$YTG2-gTxf5vyFkKGLAaR8nzFOxo implements OnDismissListener {
    private final /* synthetic */ ResetNetworkConfirm f$0;

    public /* synthetic */ -$$Lambda$ResetNetworkConfirm$YTG2-gTxf5vyFkKGLAaR8nzFOxo(ResetNetworkConfirm resetNetworkConfirm) {
        this.f$0 = resetNetworkConfirm;
    }

    public final void onDismiss(DialogInterface dialogInterface) {
        this.f$0.getActivity().finish();
    }
}
