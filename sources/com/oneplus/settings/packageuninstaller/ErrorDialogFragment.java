package com.oneplus.settings.packageuninstaller;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ErrorDialogFragment extends DialogFragment {
    public static final String TEXT = "com.android.packageinstaller.arg.text";
    public static final String TITLE = "com.android.packageinstaller.arg.title";

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder b = new Builder(getActivity()).setMessage(getArguments().getInt("com.android.packageinstaller.arg.text")).setPositiveButton(17039370, null);
        if (getArguments().containsKey("com.android.packageinstaller.arg.title")) {
            b.setTitle(getArguments().getInt("com.android.packageinstaller.arg.title"));
        }
        return b.create();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (isAdded()) {
            if (getActivity() instanceof UninstallerActivity) {
                ((UninstallerActivity) getActivity()).dispatchAborted();
            }
            getActivity().setResult(1);
            getActivity().finish();
        }
    }
}
