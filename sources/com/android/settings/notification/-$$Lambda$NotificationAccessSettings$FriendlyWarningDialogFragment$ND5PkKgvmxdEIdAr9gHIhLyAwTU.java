package com.android.settings.notification;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$NotificationAccessSettings$FriendlyWarningDialogFragment$ND5PkKgvmxdEIdAr9gHIhLyAwTU implements OnClickListener {
    private final /* synthetic */ NotificationAccessSettings f$0;
    private final /* synthetic */ ComponentName f$1;

    public /* synthetic */ -$$Lambda$NotificationAccessSettings$FriendlyWarningDialogFragment$ND5PkKgvmxdEIdAr9gHIhLyAwTU(NotificationAccessSettings notificationAccessSettings, ComponentName componentName) {
        this.f$0 = notificationAccessSettings;
        this.f$1 = componentName;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        NotificationAccessSettings.disable(this.f$0, this.f$1);
    }
}
