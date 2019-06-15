package com.android.settings.fuelgauge;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ButtonActionDialogFragment extends InstrumentedDialogFragment implements OnClickListener {
    private static final String ARG_ID = "id";
    @VisibleForTesting
    int mId;

    interface AppButtonsDialogListener {
        void handleDialogClick(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DialogType {
        public static final int DISABLE = 0;
        public static final int FORCE_STOP = 2;
        public static final int SPECIAL_DISABLE = 1;
    }

    public static ButtonActionDialogFragment newInstance(int id) {
        ButtonActionDialogFragment dialogFragment = new ButtonActionDialogFragment();
        Bundle args = new Bundle(1);
        args.putInt("id", id);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public int getMetricsCategory() {
        return 558;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mId = getArguments().getInt("id");
        Dialog dialog = createDialog(this.mId);
        if (dialog != null) {
            return dialog;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("unknown id ");
        stringBuilder.append(this.mId);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public void onClick(DialogInterface dialog, int which) {
        ((AppButtonsDialogListener) getTargetFragment()).handleDialogClick(this.mId);
    }

    private AlertDialog createDialog(int id) {
        Context context = getContext();
        switch (id) {
            case 0:
            case 1:
                return new Builder(context).setMessage(R.string.app_disable_dlg_text).setPositiveButton(R.string.app_disable_dlg_positive, this).setNegativeButton(R.string.dlg_cancel, null).create();
            case 2:
                return new Builder(context).setTitle(R.string.force_stop_dlg_title).setMessage(R.string.force_stop_dlg_text).setPositiveButton(R.string.dlg_ok, this).setNegativeButton(R.string.dlg_cancel, null).create();
            default:
                return null;
        }
    }
}
