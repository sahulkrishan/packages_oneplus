package com.android.settings.notification;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$NotificationAccessConfirmationActivity$hd7i7CSD_dVpjvK__hXE8eDM2I0 implements OnClickListener {
    private final /* synthetic */ NotificationAccessConfirmationActivity f$0;

    public /* synthetic */ -$$Lambda$NotificationAccessConfirmationActivity$hd7i7CSD_dVpjvK__hXE8eDM2I0(NotificationAccessConfirmationActivity notificationAccessConfirmationActivity) {
        this.f$0 = notificationAccessConfirmationActivity;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.cancel();
    }
}
