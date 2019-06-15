package com.android.settings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$MasterClearConfirm$weRgiuD2TQnm7jx9NX_-qHWwsHU implements OnDismissListener {
    private final /* synthetic */ MasterClearConfirm f$0;

    public /* synthetic */ -$$Lambda$MasterClearConfirm$weRgiuD2TQnm7jx9NX_-qHWwsHU(MasterClearConfirm masterClearConfirm) {
        this.f$0 = masterClearConfirm;
    }

    public final void onDismiss(DialogInterface dialogInterface) {
        this.f$0.getActivity().finish();
    }
}
