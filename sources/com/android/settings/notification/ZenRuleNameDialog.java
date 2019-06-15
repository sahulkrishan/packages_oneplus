package com.android.settings.notification;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class ZenRuleNameDialog extends InstrumentedDialogFragment {
    private static final String EXTRA_CONDITION_ID = "extra_zen_condition_id";
    private static final String EXTRA_ZEN_RULE_NAME = "zen_rule_name";
    protected static final String TAG = "ZenRuleNameDialog";
    protected static PositiveClickListener mPositiveClickListener;

    public interface PositiveClickListener {
        void onOk(String str, Fragment fragment);
    }

    public int getMetricsCategory() {
        return 1269;
    }

    public static void show(Fragment parent, String ruleName, Uri conditionId, PositiveClickListener listener) {
        Bundle args = new Bundle();
        args.putString(EXTRA_ZEN_RULE_NAME, ruleName);
        args.putParcelable(EXTRA_CONDITION_ID, conditionId);
        mPositiveClickListener = listener;
        ZenRuleNameDialog dialog = new ZenRuleNameDialog();
        dialog.setArguments(args);
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        Uri conditionId = (Uri) arguments.getParcelable(EXTRA_CONDITION_ID);
        String ruleName = arguments.getString(EXTRA_ZEN_RULE_NAME);
        final boolean isNew = ruleName == null;
        final String originalRuleName = ruleName;
        Context context = getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.zen_rule_name, null, false);
        final EditText editText = (EditText) v.findViewById(R.id.zen_mode_rule_name);
        if (!isNew) {
            editText.setText(ruleName);
            editText.setSelection(editText.getText().length());
        }
        editText.setSelectAllOnFocus(true);
        return new Builder(context).setTitle(getTitleResource(conditionId, isNew)).setView(v).setPositiveButton(isNew ? R.string.zen_mode_add : R.string.okay, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String newName = ZenRuleNameDialog.this.trimmedText(editText);
                if (!TextUtils.isEmpty(newName)) {
                    if (isNew || originalRuleName == null || !originalRuleName.equals(newName)) {
                        ZenRuleNameDialog.mPositiveClickListener.onOk(newName, ZenRuleNameDialog.this.getTargetFragment());
                    }
                }
            }
        }).setNegativeButton(R.string.cancel, null).create();
    }

    private String trimmedText(EditText editText) {
        return editText.getText() == null ? null : editText.getText().toString().trim();
    }

    private int getTitleResource(Uri conditionId, boolean isNew) {
        boolean isEvent = ZenModeConfig.isValidEventConditionId(conditionId);
        boolean isTime = ZenModeConfig.isValidScheduleConditionId(conditionId);
        if (!isNew) {
            return R.string.zen_mode_rule_name;
        }
        if (isEvent) {
            return R.string.zen_mode_add_event_rule;
        }
        if (isTime) {
            return R.string.zen_mode_add_time_rule;
        }
        return R.string.zen_mode_rule_name;
    }
}
