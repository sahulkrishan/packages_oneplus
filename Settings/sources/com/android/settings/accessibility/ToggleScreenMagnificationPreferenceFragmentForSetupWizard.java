package com.android.settings.accessibility;

import android.os.Bundle;

public class ToggleScreenMagnificationPreferenceFragmentForSetupWizard extends ToggleScreenMagnificationPreferenceFragment {
    public int getMetricsCategory() {
        return 368;
    }

    public void onStop() {
        Bundle args = getArguments();
        if (!(args == null || !args.containsKey("checked") || this.mToggleSwitch.isChecked() == args.getBoolean("checked"))) {
            this.mMetricsFeatureProvider.action(getContext(), 368, this.mToggleSwitch.isChecked());
        }
        super.onStop();
    }
}
