package com.android.settings.development;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class OemLockInfoDialog extends InstrumentedDialogFragment {
    private static final String TAG = "OemLockInfoDialog";

    public static void show(Fragment host) {
        FragmentManager manager = host.getChildFragmentManager();
        if (manager.findFragmentByTag(TAG) == null) {
            new OemLockInfoDialog().show(manager, TAG);
        }
    }

    public int getMetricsCategory() {
        return 1238;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setMessage(R.string.oem_lock_info_message).create();
    }
}
