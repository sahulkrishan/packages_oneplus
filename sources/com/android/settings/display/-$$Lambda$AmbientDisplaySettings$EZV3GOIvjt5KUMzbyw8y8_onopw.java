package com.android.settings.display;

import com.android.settings.display.AmbientDisplayAlwaysOnPreferenceController.OnPreferenceChangedCallback;

/* compiled from: lambda */
public final /* synthetic */ class -$$Lambda$AmbientDisplaySettings$EZV3GOIvjt5KUMzbyw8y8_onopw implements OnPreferenceChangedCallback {
    private final /* synthetic */ AmbientDisplaySettings f$0;

    public /* synthetic */ -$$Lambda$AmbientDisplaySettings$EZV3GOIvjt5KUMzbyw8y8_onopw(AmbientDisplaySettings ambientDisplaySettings) {
        this.f$0 = ambientDisplaySettings;
    }

    public final void onPreferenceChanged() {
        this.f$0.updatePreferenceStates();
    }
}
