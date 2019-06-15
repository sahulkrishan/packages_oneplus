package com.android.settings.security;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$UnificationConfirmationDialog$-wYUc2a9Y89ehsHG44vpFDdnSk8 implements OnClickListener {
    private final /* synthetic */ boolean f$0;
    private final /* synthetic */ SecuritySettings f$1;

    public /* synthetic */ -$$Lambda$UnificationConfirmationDialog$-wYUc2a9Y89ehsHG44vpFDdnSk8(boolean z, SecuritySettings securitySettings) {
        this.f$0 = z;
        this.f$1 = securitySettings;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        UnificationConfirmationDialog.lambda$onCreateDialog$0(this.f$0, this.f$1, dialogInterface, i);
    }
}
