package com.android.settings.inputmethod;

import com.android.settingslib.inputmethod.InputMethodPreference;
import java.text.Collator;
import java.util.Comparator;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AvailableVirtualKeyboardFragment$jwIjaxSxVSRnK0I3ZX1KVHtd2wk implements Comparator {
    private final /* synthetic */ Collator f$0;

    public /* synthetic */ -$$Lambda$AvailableVirtualKeyboardFragment$jwIjaxSxVSRnK0I3ZX1KVHtd2wk(Collator collator) {
        this.f$0 = collator;
    }

    public final int compare(Object obj, Object obj2) {
        return ((InputMethodPreference) obj).compareTo((InputMethodPreference) obj2, this.f$0);
    }
}
