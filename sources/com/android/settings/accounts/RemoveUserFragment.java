package com.android.settings.accounts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.UserManager;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.users.UserDialogs;

public class RemoveUserFragment extends InstrumentedDialogFragment {
    private static final String ARG_USER_ID = "userId";

    static RemoveUserFragment newInstance(int userId) {
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        RemoveUserFragment fragment = new RemoveUserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int userId = getArguments().getInt("userId");
        return UserDialogs.createRemoveDialog(getActivity(), userId, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ((UserManager) RemoveUserFragment.this.getActivity().getSystemService("user")).removeUser(userId);
            }
        });
    }

    public int getMetricsCategory() {
        return 534;
    }
}
