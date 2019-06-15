package com.android.settings.applications;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AppStorageSettings$DjRyx_XFfzsxe3o1nZS2usao_fc implements OnClickListener {
    private final /* synthetic */ AppStorageSettings f$0;

    public /* synthetic */ -$$Lambda$AppStorageSettings$DjRyx_XFfzsxe3o1nZS2usao_fc(AppStorageSettings appStorageSettings) {
        this.f$0 = appStorageSettings;
    }

    public final void onClick(View view) {
        this.f$0.handleClearCacheClick();
    }
}
