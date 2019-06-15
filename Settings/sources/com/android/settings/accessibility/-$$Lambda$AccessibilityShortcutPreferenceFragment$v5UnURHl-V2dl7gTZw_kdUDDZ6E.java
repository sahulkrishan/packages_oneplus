package com.android.settings.accessibility;

import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AccessibilityShortcutPreferenceFragment$v5UnURHl-V2dl7gTZw_kdUDDZ6E implements OnPreferenceChangeListener {
    private final /* synthetic */ AccessibilityShortcutPreferenceFragment f$0;

    public /* synthetic */ -$$Lambda$AccessibilityShortcutPreferenceFragment$v5UnURHl-V2dl7gTZw_kdUDDZ6E(AccessibilityShortcutPreferenceFragment accessibilityShortcutPreferenceFragment) {
        this.f$0 = accessibilityShortcutPreferenceFragment;
    }

    public final boolean onPreferenceChange(Preference preference, Object obj) {
        return Secure.putInt(this.f$0.getContentResolver(), AccessibilityShortcutPreferenceFragment.ON_LOCK_SCREEN_KEY, ((Boolean) obj).booleanValue());
    }
}
