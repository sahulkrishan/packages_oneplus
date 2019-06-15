package com.android.settings.notification;

import android.app.Dialog;
import android.os.Bundle;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.notification.EnableZenModeDialog;

public class SettingsEnableZenModeDialog extends InstrumentedDialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new EnableZenModeDialog(getContext()).createDialog();
    }

    public int getMetricsCategory() {
        return 1286;
    }
}
