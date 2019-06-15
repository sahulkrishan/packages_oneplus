package com.android.settings.inputmethod;

import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.inputmethod.PhysicalKeyboardFragment.HardKeyboardDeviceInfo;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$PhysicalKeyboardFragment$GzAuWQoIrNRWOGdhye1KALY7EFw implements OnPreferenceClickListener {
    private final /* synthetic */ PhysicalKeyboardFragment f$0;
    private final /* synthetic */ HardKeyboardDeviceInfo f$1;

    public /* synthetic */ -$$Lambda$PhysicalKeyboardFragment$GzAuWQoIrNRWOGdhye1KALY7EFw(PhysicalKeyboardFragment physicalKeyboardFragment, HardKeyboardDeviceInfo hardKeyboardDeviceInfo) {
        this.f$0 = physicalKeyboardFragment;
        this.f$1 = hardKeyboardDeviceInfo;
    }

    public final boolean onPreferenceClick(Preference preference) {
        return this.f$0.showKeyboardLayoutDialog(this.f$1.mDeviceIdentifier);
    }
}
