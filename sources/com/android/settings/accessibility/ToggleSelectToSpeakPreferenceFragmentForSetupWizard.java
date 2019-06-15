package com.android.settings.accessibility;

import android.os.Bundle;

public class ToggleSelectToSpeakPreferenceFragmentForSetupWizard extends ToggleAccessibilityServicePreferenceFragment {
    private boolean mToggleSwitchWasInitiallyChecked;

    /* Access modifiers changed, original: protected */
    public void onProcessArguments(Bundle arguments) {
        super.onProcessArguments(arguments);
        this.mToggleSwitchWasInitiallyChecked = this.mToggleSwitch.isChecked();
    }

    public int getMetricsCategory() {
        return 371;
    }

    public void onStop() {
        if (this.mToggleSwitch.isChecked() != this.mToggleSwitchWasInitiallyChecked) {
            this.mMetricsFeatureProvider.action(getContext(), 817, this.mToggleSwitch.isChecked());
        }
        super.onStop();
    }
}
