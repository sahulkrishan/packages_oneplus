package com.android.settings.security;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class UnificationConfirmationDialog extends InstrumentedDialogFragment {
    private static final String EXTRA_COMPLIANT = "compliant";
    static final String TAG_UNIFICATION_DIALOG = "unification_dialog";

    public static UnificationConfirmationDialog newInstance(boolean compliant) {
        UnificationConfirmationDialog dialog = new UnificationConfirmationDialog();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_COMPLIANT, compliant);
        dialog.setArguments(args);
        return dialog;
    }

    public void show(SecuritySettings host) {
        FragmentManager manager = host.getChildFragmentManager();
        if (manager.findFragmentByTag(TAG_UNIFICATION_DIALOG) == null) {
            show(manager, TAG_UNIFICATION_DIALOG);
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int i;
        SecuritySettings parentFragment = (SecuritySettings) getParentFragment();
        boolean compliant = getArguments().getBoolean(EXTRA_COMPLIANT);
        Builder title = new Builder(getActivity()).setTitle(R.string.lock_settings_profile_unification_dialog_title);
        if (compliant) {
            i = R.string.lock_settings_profile_unification_dialog_body;
        } else {
            i = R.string.lock_settings_profile_unification_dialog_uncompliant_body;
        }
        title = title.setMessage(i);
        if (compliant) {
            i = R.string.lock_settings_profile_unification_dialog_confirm;
        } else {
            i = R.string.lock_settings_profile_unification_dialog_uncompliant_confirm;
        }
        return title.setPositiveButton(i, new -$$Lambda$UnificationConfirmationDialog$-wYUc2a9Y89ehsHG44vpFDdnSk8(compliant, parentFragment)).setNegativeButton(R.string.cancel, null).create();
    }

    static /* synthetic */ void lambda$onCreateDialog$0(boolean compliant, SecuritySettings parentFragment, DialogInterface dialog, int whichButton) {
        if (compliant) {
            parentFragment.launchConfirmDeviceLockForUnification();
        } else {
            parentFragment.unifyUncompliantLocks();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        ((SecuritySettings) getParentFragment()).updateUnificationPreference();
    }

    public int getMetricsCategory() {
        return 532;
    }
}
