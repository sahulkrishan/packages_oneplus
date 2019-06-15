package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.core.AbstractPreferenceController;
import java.io.IOException;

public class RemoveAccountPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnClickListener {
    private static final String KEY_REMOVE_ACCOUNT = "remove_account";
    private Account mAccount;
    private Fragment mParentFragment;
    private UserHandle mUserHandle;

    public static class ConfirmRemoveAccountDialog extends InstrumentedDialogFragment implements DialogInterface.OnClickListener {
        private static final String KEY_ACCOUNT = "account";
        private static final String REMOVE_ACCOUNT_DIALOG = "confirmRemoveAccount";
        private Account mAccount;
        private UserHandle mUserHandle;

        public static ConfirmRemoveAccountDialog show(Fragment parent, Account account, UserHandle userHandle) {
            if (!parent.isAdded()) {
                return null;
            }
            ConfirmRemoveAccountDialog dialog = new ConfirmRemoveAccountDialog();
            Bundle bundle = new Bundle();
            bundle.putParcelable("account", account);
            bundle.putParcelable("android.intent.extra.USER", userHandle);
            dialog.setArguments(bundle);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), REMOVE_ACCOUNT_DIALOG);
            return dialog;
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle arguments = getArguments();
            this.mAccount = (Account) arguments.getParcelable("account");
            this.mUserHandle = (UserHandle) arguments.getParcelable("android.intent.extra.USER");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getActivity()).setTitle(R.string.really_remove_account_title).setMessage(R.string.really_remove_account_message).setNegativeButton(17039360, null).setPositiveButton(R.string.remove_account_label, this).create();
        }

        public int getMetricsCategory() {
            return 585;
        }

        public void onClick(DialogInterface dialog, int which) {
            Activity activity = getTargetFragment().getActivity();
            AccountManager.get(activity).removeAccountAsUser(this.mAccount, activity, new AccountManagerCallback<Bundle>() {
                public void run(AccountManagerFuture<Bundle> future) {
                    boolean failed = true;
                    try {
                        if (((Bundle) future.getResult()).getBoolean("booleanResult")) {
                            failed = false;
                        }
                    } catch (AuthenticatorException | OperationCanceledException | IOException e) {
                    }
                    Activity activity = ConfirmRemoveAccountDialog.this.getTargetFragment().getActivity();
                    if (failed && activity != null && !activity.isFinishing()) {
                        RemoveAccountFailureDialog.show(ConfirmRemoveAccountDialog.this.getTargetFragment());
                    } else if (activity != null) {
                        activity.finish();
                    }
                }
            }, null, this.mUserHandle);
        }
    }

    public static class RemoveAccountFailureDialog extends InstrumentedDialogFragment {
        private static final String FAILED_REMOVAL_DIALOG = "removeAccountFailed";

        public static void show(Fragment parent) {
            if (parent.isAdded()) {
                RemoveAccountFailureDialog dialog = new RemoveAccountFailureDialog();
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), FAILED_REMOVAL_DIALOG);
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getActivity()).setTitle(R.string.really_remove_account_title).setMessage(R.string.remove_account_failed).setPositiveButton(17039370, null).create();
        }

        public int getMetricsCategory() {
            return 586;
        }
    }

    public RemoveAccountPreferenceController(Context context, Fragment parent) {
        super(context);
        this.mParentFragment = parent;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ((Button) ((LayoutPreference) screen.findPreference(KEY_REMOVE_ACCOUNT)).findViewById(R.id.button)).setOnClickListener(this);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_REMOVE_ACCOUNT;
    }

    public void onClick(View v) {
        if (this.mUserHandle != null) {
            EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_modify_accounts", this.mUserHandle.getIdentifier());
            if (admin != null) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, admin);
                return;
            }
        }
        ConfirmRemoveAccountDialog.show(this.mParentFragment, this.mAccount, this.mUserHandle);
    }

    public void init(Account account, UserHandle userHandle) {
        this.mAccount = account;
        this.mUserHandle = userHandle;
    }
}
