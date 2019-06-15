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

public class EnableAdbWarningDialog extends InstrumentedDialogFragment implements OnClickListener, OnDismissListener {
    public static final String TAG = "EnableAdbDialog";

    public static void show(Fragment host) {
        FragmentManager manager = host.getActivity().getFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            EnableAdbWarningDialog dialog = new EnableAdbWarningDialog();
            dialog.setTargetFragment(host, 0);
            dialog.show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1222;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setTitle(R.string.adb_warning_title).setMessage(R.string.adb_warning_message).setPositiveButton(17039379, this).setNegativeButton(17039369, this).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        AdbDialogHost host = (AdbDialogHost) getTargetFragment();
        if (host != null) {
            if (which == -1) {
                host.onEnableAdbDialogConfirmed();
            } else {
                host.onEnableAdbDialogDismissed();
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        AdbDialogHost host = (AdbDialogHost) getTargetFragment();
        if (host != null) {
            host.onEnableAdbDialogDismissed();
        }
    }
}
