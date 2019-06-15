package com.android.settings.applications;

import android.view.View;
import android.view.View.OnClickListener;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AppStateNotificationBridge$3yb6PrF82n91FG3YEHY_Ccl1JyI implements OnClickListener {
    private final /* synthetic */ AppStateNotificationBridge f$0;
    private final /* synthetic */ AppEntry f$1;

    public /* synthetic */ -$$Lambda$AppStateNotificationBridge$3yb6PrF82n91FG3YEHY_Ccl1JyI(AppStateNotificationBridge appStateNotificationBridge, AppEntry appEntry) {
        this.f$0 = appStateNotificationBridge;
        this.f$1 = appEntry;
    }

    public final void onClick(View view) {
        AppStateNotificationBridge.lambda$getSwitchOnClickListener$0(this.f$0, this.f$1, view);
    }
}
