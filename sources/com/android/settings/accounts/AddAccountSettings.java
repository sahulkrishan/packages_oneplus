package com.android.settings.accounts;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.Settings.ChooseAccountActivity;
import com.android.settings.Utils;
import com.android.settings.password.ChooseLockSettingsHelper;

public class AddAccountSettings extends Activity {
    private static final int ADD_ACCOUNT_REQUEST = 2;
    private static final int CHOOSE_ACCOUNT_REQUEST = 1;
    static final String EXTRA_HAS_MULTIPLE_USERS = "hasMultipleUsers";
    static final String EXTRA_SELECTED_ACCOUNT = "selected_account";
    private static final String KEY_ADD_CALLED = "AddAccountCalled";
    private static final String KEY_CALLER_IDENTITY = "pendingIntent";
    private static final String SHOULD_NOT_RESOLVE = "SHOULDN'T RESOLVE!";
    private static final String TAG = "AddAccountSettings";
    private static final int UNLOCK_WORK_PROFILE_REQUEST = 3;
    private boolean mAddAccountCalled = false;
    private final AccountManagerCallback<Bundle> mCallback = new AccountManagerCallback<Bundle>() {
        /* JADX WARNING: Missing block: B:36:?, code skipped:
            return;
     */
        public void run(android.accounts.AccountManagerFuture<android.os.Bundle> r8) {
            /*
            r7 = this;
            r0 = 1;
            r1 = 2;
            r2 = r8.getResult();	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r2 = (android.os.Bundle) r2;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r3 = "intent";
            r3 = r2.get(r3);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r3 = (android.content.Intent) r3;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            if (r3 == 0) goto L_0x004d;
        L_0x0012:
            r0 = 0;
            r4 = new android.os.Bundle;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4.<init>();	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5 = "pendingIntent";
            r6 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r6 = r6.mPendingIntent;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4.putParcelable(r5, r6);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5 = "hasMultipleUsers";
            r6 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r6 = com.android.settings.Utils.hasMultipleUsers(r6);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4.putBoolean(r5, r6);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5 = "android.intent.extra.USER";
            r6 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r6 = r6.mUserHandle;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4.putParcelable(r5, r6);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r3.putExtras(r4);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5 = 268435456; // 0x10000000 float:2.5243549E-29 double:1.32624737E-315;
            r3.addFlags(r5);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r6 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r6 = r6.mUserHandle;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5.startActivityForResultAsUser(r3, r1, r6);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            goto L_0x006a;
        L_0x004d:
            r4 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5 = -1;
            r4.setResult(r5);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4 = r4.mPendingIntent;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            if (r4 == 0) goto L_0x006a;
        L_0x005b:
            r4 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4 = r4.mPendingIntent;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4.cancel();	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r4 = com.android.settings.accounts.AddAccountSettings.this;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5 = 0;
            r4.mPendingIntent = r5;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
        L_0x006a:
            r4 = "AddAccountSettings";
            r4 = android.util.Log.isLoggable(r4, r1);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            if (r4 == 0) goto L_0x0088;
        L_0x0072:
            r4 = "AddAccountSettings";
            r5 = new java.lang.StringBuilder;	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5.<init>();	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r6 = "account added: ";
            r5.append(r6);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5.append(r2);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            r5 = r5.toString();	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
            android.util.Log.v(r4, r5);	 Catch:{ OperationCanceledException -> 0x00d6, IOException -> 0x00b4, AuthenticatorException -> 0x0092 }
        L_0x0088:
            if (r0 == 0) goto L_0x00e9;
        L_0x008a:
            r1 = com.android.settings.accounts.AddAccountSettings.this;
            r1.finish();
            goto L_0x00e9;
        L_0x0090:
            r1 = move-exception;
            goto L_0x00ea;
        L_0x0092:
            r2 = move-exception;
            r3 = "AddAccountSettings";
            r1 = android.util.Log.isLoggable(r3, r1);	 Catch:{ all -> 0x0090 }
            if (r1 == 0) goto L_0x00b1;
        L_0x009b:
            r1 = "AddAccountSettings";
            r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0090 }
            r3.<init>();	 Catch:{ all -> 0x0090 }
            r4 = "addAccount failed: ";
            r3.append(r4);	 Catch:{ all -> 0x0090 }
            r3.append(r2);	 Catch:{ all -> 0x0090 }
            r3 = r3.toString();	 Catch:{ all -> 0x0090 }
            android.util.Log.v(r1, r3);	 Catch:{ all -> 0x0090 }
        L_0x00b1:
            if (r0 == 0) goto L_0x00e9;
        L_0x00b3:
            goto L_0x008a;
        L_0x00b4:
            r2 = move-exception;
            r3 = "AddAccountSettings";
            r1 = android.util.Log.isLoggable(r3, r1);	 Catch:{ all -> 0x0090 }
            if (r1 == 0) goto L_0x00d3;
        L_0x00bd:
            r1 = "AddAccountSettings";
            r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0090 }
            r3.<init>();	 Catch:{ all -> 0x0090 }
            r4 = "addAccount failed: ";
            r3.append(r4);	 Catch:{ all -> 0x0090 }
            r3.append(r2);	 Catch:{ all -> 0x0090 }
            r3 = r3.toString();	 Catch:{ all -> 0x0090 }
            android.util.Log.v(r1, r3);	 Catch:{ all -> 0x0090 }
        L_0x00d3:
            if (r0 == 0) goto L_0x00e9;
        L_0x00d5:
            goto L_0x008a;
        L_0x00d6:
            r2 = move-exception;
            r3 = "AddAccountSettings";
            r1 = android.util.Log.isLoggable(r3, r1);	 Catch:{ all -> 0x0090 }
            if (r1 == 0) goto L_0x00e6;
        L_0x00df:
            r1 = "AddAccountSettings";
            r3 = "addAccount was canceled";
            android.util.Log.v(r1, r3);	 Catch:{ all -> 0x0090 }
        L_0x00e6:
            if (r0 == 0) goto L_0x00e9;
        L_0x00e8:
            goto L_0x008a;
        L_0x00e9:
            return;
        L_0x00ea:
            if (r0 == 0) goto L_0x00f1;
        L_0x00ec:
            r2 = com.android.settings.accounts.AddAccountSettings.this;
            r2.finish();
        L_0x00f1:
            throw r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.accounts.AddAccountSettings$AnonymousClass1.run(android.accounts.AccountManagerFuture):void");
        }
    };
    private PendingIntent mPendingIntent;
    private UserHandle mUserHandle;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mAddAccountCalled = savedInstanceState.getBoolean(KEY_ADD_CALLED);
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "restored");
            }
        }
        UserManager um = (UserManager) getSystemService("user");
        this.mUserHandle = Utils.getSecureTargetUser(getActivityToken(), um, null, getIntent().getExtras());
        if (um.hasUserRestriction("no_modify_accounts", this.mUserHandle)) {
            Toast.makeText(this, R.string.user_cannot_add_accounts_message, 1).show();
            finish();
        } else if (this.mAddAccountCalled) {
            finish();
        } else if (Utils.startQuietModeDialogIfNecessary(this, um, this.mUserHandle.getIdentifier())) {
            finish();
        } else {
            if (um.isUserUnlocked(this.mUserHandle)) {
                requestChooseAccount();
            } else if (!new ChooseLockSettingsHelper(this).launchConfirmationActivity(3, getString(R.string.unlock_set_unlock_launch_picker_title), false, this.mUserHandle.getIdentifier())) {
                requestChooseAccount();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode != 0) {
                    addAccount(data.getStringExtra(EXTRA_SELECTED_ACCOUNT));
                    break;
                }
                if (data != null) {
                    startActivityAsUser(data, this.mUserHandle);
                }
                setResult(resultCode);
                finish();
                return;
            case 2:
                setResult(resultCode);
                if (this.mPendingIntent != null) {
                    this.mPendingIntent.cancel();
                    this.mPendingIntent = null;
                }
                finish();
                break;
            case 3:
                if (resultCode != -1) {
                    finish();
                    break;
                } else {
                    requestChooseAccount();
                    break;
                }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ADD_CALLED, this.mAddAccountCalled);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "saved");
        }
    }

    private void requestChooseAccount() {
        String[] authorities = getIntent().getStringArrayExtra(AccountPreferenceBase.AUTHORITIES_FILTER_KEY);
        String[] accountTypes = getIntent().getStringArrayExtra(AccountPreferenceBase.ACCOUNT_TYPES_FILTER_KEY);
        Intent intent = new Intent(this, ChooseAccountActivity.class);
        if (authorities != null) {
            intent.putExtra(AccountPreferenceBase.AUTHORITIES_FILTER_KEY, authorities);
        }
        if (accountTypes != null) {
            intent.putExtra(AccountPreferenceBase.ACCOUNT_TYPES_FILTER_KEY, accountTypes);
        }
        intent.putExtra("android.intent.extra.USER", this.mUserHandle);
        startActivityForResult(intent, 1);
    }

    private void addAccount(String accountType) {
        Bundle addAccountOptions = new Bundle();
        Intent identityIntent = new Intent();
        identityIntent.setComponent(new ComponentName(SHOULD_NOT_RESOLVE, SHOULD_NOT_RESOLVE));
        identityIntent.setAction(SHOULD_NOT_RESOLVE);
        identityIntent.addCategory(SHOULD_NOT_RESOLVE);
        this.mPendingIntent = PendingIntent.getBroadcast(this, 0, identityIntent, 0);
        addAccountOptions.putParcelable(KEY_CALLER_IDENTITY, this.mPendingIntent);
        addAccountOptions.putBoolean(EXTRA_HAS_MULTIPLE_USERS, Utils.hasMultipleUsers(this));
        AccountManager.get(this).addAccountAsUser(accountType, null, null, addAccountOptions, null, this.mCallback, null, this.mUserHandle);
        this.mAddAccountCalled = true;
    }
}
