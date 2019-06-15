package com.android.settings.applications.appinfo;

import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AppActionButtonPreferenceController$Ww2IUjWxdICZ6sY_1SuD__XEpOY implements OnClickListener {
    private final /* synthetic */ AppActionButtonPreferenceController f$0;

    public /* synthetic */ -$$Lambda$AppActionButtonPreferenceController$Ww2IUjWxdICZ6sY_1SuD__XEpOY(AppActionButtonPreferenceController appActionButtonPreferenceController) {
        this.f$0 = appActionButtonPreferenceController;
    }

    public final void onClick(View view) {
        this.f$0.mParent.handleUninstallButtonClick();
    }
}
