package com.android.settingslib.inputmethod;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$InputMethodPreference$HH5dtwzFZv06UNDXJAO6Cyx4kxo implements OnClickListener {
    private final /* synthetic */ InputMethodPreference f$0;

    public /* synthetic */ -$$Lambda$InputMethodPreference$HH5dtwzFZv06UNDXJAO6Cyx4kxo(InputMethodPreference inputMethodPreference) {
        this.f$0 = inputMethodPreference;
    }

    public final void onClick(DialogInterface dialogInterface, int i) {
        this.f$0.setCheckedInternal(false);
    }
}
