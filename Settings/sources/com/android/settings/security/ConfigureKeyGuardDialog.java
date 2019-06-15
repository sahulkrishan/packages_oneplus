package com.android.settings.security;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.PointerIconCompat;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.password.ChooseLockGeneric.ChooseLockGenericFragment;

public class ConfigureKeyGuardDialog extends InstrumentedDialogFragment implements OnClickListener, OnDismissListener {
    public static final String TAG = "ConfigureKeyGuardDialog";
    private boolean mConfigureConfirmed;

    public int getMetricsCategory() {
        return PointerIconCompat.TYPE_ALIAS;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setTitle(17039380).setMessage(R.string.credentials_configure_lock_screen_hint).setPositiveButton(R.string.credentials_configure_lock_screen_button, this).setNegativeButton(17039360, this).create();
    }

    public void onClick(DialogInterface dialog, int button) {
        this.mConfigureConfirmed = button == -1;
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mConfigureConfirmed) {
            this.mConfigureConfirmed = false;
            startPasswordSetup();
            return;
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void startPasswordSetup() {
        Intent intent = new Intent("android.app.action.SET_NEW_PASSWORD");
        intent.putExtra(ChooseLockGenericFragment.MINIMUM_QUALITY_KEY, 65536);
        startActivity(intent);
    }
}
