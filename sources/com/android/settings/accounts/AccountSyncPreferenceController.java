package com.android.settings.accounts;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncAdapterType;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener;
import com.android.settingslib.core.AbstractPreferenceController;

public class AccountSyncPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnAccountsUpdateListener {
    private static final String KEY_ACCOUNT_SYNC = "account_sync";
    private static final String TAG = "AccountSyncController";
    private Account mAccount;
    private Preference mPreference;
    private UserHandle mUserHandle;

    public AccountSyncPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_ACCOUNT_SYNC.equals(preference.getKey())) {
            return false;
        }
        Bundle args = new Bundle();
        args.putParcelable("account", this.mAccount);
        args.putParcelable("android.intent.extra.USER", this.mUserHandle);
        new SubSettingLauncher(this.mContext).setDestination(AccountSyncSettings.class.getName()).setArguments(args).setSourceMetricsCategory(8).setTitle((int) R.string.account_sync_title).launch();
        return true;
    }

    public String getPreferenceKey() {
        return KEY_ACCOUNT_SYNC;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public void updateState(Preference preference) {
        updateSummary(preference);
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        updateSummary(this.mPreference);
    }

    public void init(Account account, UserHandle userHandle) {
        this.mAccount = account;
        this.mUserHandle = userHandle;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateSummary(Preference preference) {
        if (this.mAccount != null) {
            int userId = this.mUserHandle.getIdentifier();
            SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(userId);
            int total = 0;
            int enabled = 0;
            if (syncAdapters != null) {
                for (SyncAdapterType sa : syncAdapters) {
                    if (sa.accountType.equals(this.mAccount.type) && sa.isUserVisible() && ContentResolver.getIsSyncableAsUser(this.mAccount, sa.authority, userId) > 0) {
                        total++;
                        boolean syncEnabled = ContentResolver.getSyncAutomaticallyAsUser(this.mAccount, sa.authority, userId);
                        if ((ContentResolver.getMasterSyncAutomaticallyAsUser(userId) ^ true) || syncEnabled) {
                            enabled++;
                        }
                    }
                }
            }
            if (enabled == 0) {
                preference.setSummary((int) R.string.account_sync_summary_all_off);
            } else if (enabled == total) {
                preference.setSummary((int) R.string.account_sync_summary_all_on);
            } else {
                preference.setSummary(this.mContext.getString(R.string.account_sync_summary_some_on, new Object[]{Integer.valueOf(enabled), Integer.valueOf(total)}));
            }
        }
    }
}
