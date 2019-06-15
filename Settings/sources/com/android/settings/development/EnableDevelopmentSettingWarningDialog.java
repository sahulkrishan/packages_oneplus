package com.android.settings.development;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class EnableDevelopmentSettingWarningDialog extends InstrumentedDialogFragment implements OnClickListener {
    public static final String TAG = "EnableDevSettingDlg";

    public static void show(DevelopmentSettingsDashboardFragment host) {
        EnableDevelopmentSettingWarningDialog dialog = new EnableDevelopmentSettingWarningDialog();
        dialog.setTargetFragment(host, 0);
        FragmentManager manager = host.getActivity().getFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            dialog.show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1219;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setMessage(R.string.dev_settings_warning_message).setTitle(R.string.dev_settings_warning_title).setPositiveButton(17039379, this).setNegativeButton(17039369, this).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        DevelopmentSettingsDashboardFragment host = (DevelopmentSettingsDashboardFragment) getTargetFragment();
        if (which == -1) {
            host.onEnableDevelopmentOptionsConfirmed();
        } else {
            host.onEnableDevelopmentOptionsRejected();
        }
    }
}
