package com.android.settings.users;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.security.OwnerInfoPreferenceController.OwnerInfoCallback;

public class OwnerInfoSettings extends InstrumentedDialogFragment implements OnClickListener {
    private static final String TAG_OWNER_INFO = "ownerInfo";
    private LockPatternUtils mLockPatternUtils;
    private EditText mOwnerInfo;
    private int mUserId;
    private View mView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUserId = UserHandle.myUserId();
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mView = LayoutInflater.from(getActivity()).inflate(R.layout.ownerinfo, null);
        initView();
        return new Builder(getActivity()).setTitle(R.string.owner_info_settings_title).setView(this.mView).setPositiveButton(R.string.save, this).setNegativeButton(R.string.cancel, this).show();
    }

    private void initView() {
        String info = this.mLockPatternUtils.getOwnerInfo(this.mUserId);
        this.mOwnerInfo = (EditText) this.mView.findViewById(R.id.owner_info_edit_text);
        this.mOwnerInfo.requestFocus();
        if (!TextUtils.isEmpty(info)) {
            this.mOwnerInfo.setText(info);
            this.mOwnerInfo.setSelection(info.length());
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            String info = this.mOwnerInfo.getText().toString();
            this.mLockPatternUtils.setOwnerInfoEnabled(TextUtils.isEmpty(info) ^ 1, this.mUserId);
            this.mLockPatternUtils.setOwnerInfo(info, this.mUserId);
            if (getTargetFragment() instanceof OwnerInfoCallback) {
                ((OwnerInfoCallback) getTargetFragment()).onOwnerInfoUpdated();
            }
        }
    }

    public static void show(Fragment parent) {
        if (parent.isAdded()) {
            OwnerInfoSettings dialog = new OwnerInfoSettings();
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), TAG_OWNER_INFO);
        }
    }

    public int getMetricsCategory() {
        return 531;
    }
}
