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

public class ClearAdbKeysWarningDialog extends InstrumentedDialogFragment implements OnClickListener, OnDismissListener {
    public static final String TAG = "ClearAdbKeysDlg";

    public static void show(Fragment host) {
        FragmentManager manager = host.getActivity().getFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            ClearAdbKeysWarningDialog dialog = new ClearAdbKeysWarningDialog();
            dialog.setTargetFragment(host, 0);
            dialog.show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1223;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setMessage(R.string.adb_keys_warning_message).setPositiveButton(17039370, this).setNegativeButton(17039360, null).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        AdbClearKeysDialogHost host = (AdbClearKeysDialogHost) getTargetFragment();
        if (host != null) {
            host.onAdbClearKeysDialogConfirmed();
        }
    }
}
