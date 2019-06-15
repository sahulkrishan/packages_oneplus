package com.android.settings.accessibility;

import android.widget.Switch;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AccessibilityShortcutPreferenceFragment$B1JGpZUcoOdF9ofKXLGiPDgZ6Bo implements OnSwitchChangeListener {
    private final /* synthetic */ AccessibilityShortcutPreferenceFragment f$0;

    public /* synthetic */ -$$Lambda$AccessibilityShortcutPreferenceFragment$B1JGpZUcoOdF9ofKXLGiPDgZ6Bo(AccessibilityShortcutPreferenceFragment accessibilityShortcutPreferenceFragment) {
        this.f$0 = accessibilityShortcutPreferenceFragment;
    }

    public final void onSwitchChanged(Switch switchR, boolean z) {
        AccessibilityShortcutPreferenceFragment.lambda$onInstallSwitchBarToggleSwitch$1(this.f$0, switchR, z);
    }
}
