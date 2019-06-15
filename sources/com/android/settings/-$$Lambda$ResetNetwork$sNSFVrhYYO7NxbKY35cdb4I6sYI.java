package com.android.settings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ResetNetwork$sNSFVrhYYO7NxbKY35cdb4I6sYI implements OnDismissListener {
    private final /* synthetic */ ResetNetwork f$0;

    public /* synthetic */ -$$Lambda$ResetNetwork$sNSFVrhYYO7NxbKY35cdb4I6sYI(ResetNetwork resetNetwork) {
        this.f$0 = resetNetwork;
    }

    public final void onDismiss(DialogInterface dialogInterface) {
        this.f$0.getActivity().finish();
    }
}
