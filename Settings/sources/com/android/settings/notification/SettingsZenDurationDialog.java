package com.android.settings.notification;

import android.app.Dialog;
import android.os.Bundle;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.notification.ZenDurationDialog;

public class SettingsZenDurationDialog extends InstrumentedDialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new ZenDurationDialog(getContext()).createDialog();
    }

    public int getMetricsCategory() {
        return 1341;
    }
}
