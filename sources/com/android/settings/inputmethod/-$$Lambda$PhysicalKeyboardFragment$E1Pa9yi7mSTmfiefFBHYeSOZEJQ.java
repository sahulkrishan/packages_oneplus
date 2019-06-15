package com.android.settings.inputmethod;

import com.android.settings.inputmethod.PhysicalKeyboardFragment.HardKeyboardDeviceInfo;
import java.text.Collator;
import java.util.Comparator;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$PhysicalKeyboardFragment$E1Pa9yi7mSTmfiefFBHYeSOZEJQ implements Comparator {
    private final /* synthetic */ Collator f$0;

    public /* synthetic */ -$$Lambda$PhysicalKeyboardFragment$E1Pa9yi7mSTmfiefFBHYeSOZEJQ(Collator collator) {
        this.f$0 = collator;
    }

    public final int compare(Object obj, Object obj2) {
        return PhysicalKeyboardFragment.lambda$getHardKeyboards$3(this.f$0, (HardKeyboardDeviceInfo) obj, (HardKeyboardDeviceInfo) obj2);
    }
}
