package com.android.settings.notification;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class ZenDeleteRuleDialog extends InstrumentedDialogFragment {
    private static final String EXTRA_ZEN_RULE_ID = "zen_rule_id";
    private static final String EXTRA_ZEN_RULE_NAME = "zen_rule_name";
    protected static final String TAG = "ZenDeleteRuleDialog";
    protected static PositiveClickListener mPositiveClickListener;

    public interface PositiveClickListener {
        void onOk(String str);
    }

    public static void show(Fragment parent, String ruleName, String id, PositiveClickListener listener) {
        Bundle args = new Bundle();
        args.putString(EXTRA_ZEN_RULE_NAME, ruleName);
        args.putString(EXTRA_ZEN_RULE_ID, id);
        mPositiveClickListener = listener;
        ZenDeleteRuleDialog dialog = new ZenDeleteRuleDialog();
        dialog.setArguments(args);
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG);
    }

    public int getMetricsCategory() {
        return 1266;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        String ruleName = arguments.getString(EXTRA_ZEN_RULE_NAME);
        final String id = arguments.getString(EXTRA_ZEN_RULE_ID);
        AlertDialog dialog = new Builder(getContext()).setMessage(getString(R.string.zen_mode_delete_rule_confirmation, new Object[]{ruleName})).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.zen_mode_delete_rule_button, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (arguments != null) {
                    ZenDeleteRuleDialog.mPositiveClickListener.onOk(id);
                }
            }
        }).create();
        View messageView = dialog.findViewById(16908299);
        if (messageView != null) {
            messageView.setTextDirection(5);
        }
        return dialog;
    }
}
