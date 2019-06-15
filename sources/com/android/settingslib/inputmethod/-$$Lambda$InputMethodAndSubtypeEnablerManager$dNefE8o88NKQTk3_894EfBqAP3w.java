package com.android.settingslib.inputmethod;

import android.support.v7.preference.Preference;
import java.util.Comparator;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$InputMethodAndSubtypeEnablerManager$dNefE8o88NKQTk3_894EfBqAP3w implements Comparator {
    private final /* synthetic */ InputMethodAndSubtypeEnablerManager f$0;

    public /* synthetic */ -$$Lambda$InputMethodAndSubtypeEnablerManager$dNefE8o88NKQTk3_894EfBqAP3w(InputMethodAndSubtypeEnablerManager inputMethodAndSubtypeEnablerManager) {
        this.f$0 = inputMethodAndSubtypeEnablerManager;
    }

    public final int compare(Object obj, Object obj2) {
        return InputMethodAndSubtypeEnablerManager.lambda$addInputMethodSubtypePreferences$0(this.f$0, (Preference) obj, (Preference) obj2);
    }
}
