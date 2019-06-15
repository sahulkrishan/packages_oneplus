package com.android.settingslib.inputmethod;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$InputMethodPreference$FTfMqDGTv2yWgiGfPYaiYBCHriY implements OnClickListener {
    private final /* synthetic */ InputMethodPreference f$0;

    public /* synthetic */ -$$Lambda$InputMethodPreference$FTfMqDGTv2yWgiGfPYaiYBCHriY(InputMethodPreference inputMethodPreference) {
        this.f$0 = inputMethodPreference;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.setCheckedInternal(true);
    }
}
