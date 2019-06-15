package com.android.settings.inputmethod;

import com.android.settingslib.inputmethod.InputMethodPreference;
import java.text.Collator;
import java.util.Comparator;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$VirtualKeyboardFragment$3eczHKaadmVH3sZXf9rlrdYqLjw implements Comparator {
    private final /* synthetic */ Collator f$0;

    public /* synthetic */ -$$Lambda$VirtualKeyboardFragment$3eczHKaadmVH3sZXf9rlrdYqLjw(Collator collator) {
        this.f$0 = collator;
    }

    public final int compare(Object obj, Object obj2) {
        return ((InputMethodPreference) obj).compareTo((InputMethodPreference) obj2, this.f$0);
    }
}
