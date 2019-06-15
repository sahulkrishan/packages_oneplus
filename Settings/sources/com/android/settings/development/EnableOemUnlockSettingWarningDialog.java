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
import com.oneplus.settings.utils.OPUtils;

public class EnableOemUnlockSettingWarningDialog extends InstrumentedDialogFragment implements OnClickListener, OnDismissListener {
    public static final String TAG = "EnableOemUnlockDlg";

    public static void show(Fragment host) {
        FragmentManager manager = host.getActivity().getFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            EnableOemUnlockSettingWarningDialog dialog = new EnableOemUnlockSettingWarningDialog();
            dialog.setTargetFragment(host, 0);
            dialog.show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1220;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int messageStringResId = R.string.op_confirm_enable_oem_unlock_text;
        if (OPUtils.isOP6ModeBefore()) {
            messageStringResId = R.string.confirm_enable_oem_unlock_text;
        }
        return new Builder(getActivity()).setTitle(R.string.confirm_enable_oem_unlock_title).setMessage(messageStringResId).setPositiveButton(R.string.enable_text, this).setNegativeButton(17039360, this).create();
    }

    public void onClick(DialogInterface dialog, int which) {
        OemUnlockDialogHost host = (OemUnlockDialogHost) getTargetFragment();
        if (host != null) {
            if (which == -1) {
                host.onOemUnlockDialogConfirmed();
            } else {
                host.onOemUnlockDialogDismissed();
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        OemUnlockDialogHost host = (OemUnlockDialogHost) getTargetFragment();
        if (host != null) {
            host.onOemUnlockDialogDismissed();
        }
    }
}
