package com.android.settings.utils;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$ManagedServiceSettings$ScaryWarningDialogFragment$GfuRaJIB12V_MS8RLGOsdgpO8G0 implements OnClickListener {
    private final /* synthetic */ ManagedServiceSettings f$0;
    private final /* synthetic */ ComponentName f$1;

    public /* synthetic */ -$$Lambda$ManagedServiceSettings$ScaryWarningDialogFragment$GfuRaJIB12V_MS8RLGOsdgpO8G0(ManagedServiceSettings managedServiceSettings, ComponentName componentName) {
        this.f$0 = managedServiceSettings;
        this.f$1 = componentName;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.enable(this.f$1);
    }
}
