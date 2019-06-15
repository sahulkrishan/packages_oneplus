package com.android.settingslib.inputmethod;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$InputMethodPreference$pHt4-6FWRQ9Ts6PuJy_AB14MhJc implements OnClickListener {
    private final /* synthetic */ InputMethodPreference f$0;

    public /* synthetic */ -$$Lambda$InputMethodPreference$pHt4-6FWRQ9Ts6PuJy_AB14MhJc(InputMethodPreference inputMethodPreference) {
        this.f$0 = inputMethodPreference;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        InputMethodPreference.lambda$showSecurityWarnDialog$0(this.f$0, dialogInterface, i);
    }
}
