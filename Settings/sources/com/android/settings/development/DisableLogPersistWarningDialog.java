package com.android.settings.development;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class DisableLogPersistWarningDialog extends InstrumentedDialogFragment implements OnClickListener, OnDismissListener {
    public static final String TAG = "DisableLogPersistDlg";

    public static void show(LogPersistDialogHost host) {
        if (host instanceof Fragment) {
            Fragment hostFragment = (Fragment) host;
            FragmentManager manager = hostFragment.getActivity().getFragmentManager();
            if (manager.findFragmentByTag(TAG) == null) {
                DisableLogPersistWarningDialog dialog = new DisableLogPersistWarningDialog();
                dialog.setTargetFragment(hostFragment, 0);
                dialog.show(manager, TAG);
            }
        }
    }

    public int getMetricsCategory() {
        return 1225;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setTitle(R.string.dev_logpersist_clear_warning_title).setMessage(R.string.dev_logpersist_clear_warning_message).setPositiveButton(17039379, this).setNegativeButton(17039369, this).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        LogPersistDialogHost host = (LogPersistDialogHost) getTargetFragment();
        if (host != null) {
            if (which == -1) {
                host.onDisableLogPersistDialogConfirmed();
            } else {
                host.onDisableLogPersistDialogRejected();
            }
        }
    }
}
