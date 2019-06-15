package com.android.settings.applications.appinfo;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$InstantAppButtonsPreferenceController$2vM5nla3CEsaIUNVk7alr9UEbBA implements OnClickListener {
    private final /* synthetic */ InstantAppButtonsPreferenceController f$0;
    private final /* synthetic */ Intent f$1;

    public /* synthetic */ -$$Lambda$InstantAppButtonsPreferenceController$2vM5nla3CEsaIUNVk7alr9UEbBA(InstantAppButtonsPreferenceController instantAppButtonsPreferenceController, Intent intent) {
        this.f$0 = instantAppButtonsPreferenceController;
        this.f$1 = intent;
    }

    public final void onClick(View view) {
        this.f$0.mParent.startActivity(this.f$1);
    }
}
