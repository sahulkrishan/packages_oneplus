package com.android.settings.deletionhelper;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import com.android.settings.R;

public class ActivationWarningFragment extends DialogFragment {
    public static final String TAG = "ActivationWarningFragment";

    public static ActivationWarningFragment newInstance() {
        return new ActivationWarningFragment();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setMessage(R.string.automatic_storage_manager_activation_warning).setPositiveButton(17039370, null).create();
    }
}
