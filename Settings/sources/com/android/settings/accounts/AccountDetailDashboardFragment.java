package com.android.settings.accounts;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.drawer.Tile;
import java.util.ArrayList;
import java.util.List;

public class AccountDetailDashboardFragment extends DashboardFragment {
    private static final String EXTRA_ACCOUNT_NAME = "extra.accountName";
    public static final String KEY_ACCOUNT = "account";
    public static final String KEY_ACCOUNT_LABEL = "account_label";
    public static final String KEY_ACCOUNT_TITLE_RES = "account_title_res";
    public static final String KEY_ACCOUNT_TYPE = "account_type";
    public static final String KEY_USER_HANDLE = "user_handle";
    private static final String METADATA_IA_ACCOUNT = "com.android.settings.ia.account";
    private static final String TAG = "AccountDetailDashboard";
    @VisibleForTesting
    Account mAccount;
    private String mAccountLabel;
    private AccountSyncPreferenceController mAccountSynController;
    @VisibleForTesting
    String mAccountType;
    private RemoveAccountPreferenceController mRemoveAccountController;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setPreferenceComparisonCallback(null);
        Bundle args = getArguments();
        Activity activity = getActivity();
        UserHandle userHandle = Utils.getSecureTargetUser(activity.getActivityToken(), (UserManager) getSystemService("user"), args, activity.getIntent().getExtras());
        if (args != null) {
            if (args.containsKey("account")) {
                this.mAccount = (Account) args.getParcelable("account");
            }
            if (args.containsKey(KEY_ACCOUNT_LABEL)) {
                this.mAccountLabel = args.getString(KEY_ACCOUNT_LABEL);
            }
            if (args.containsKey(KEY_ACCOUNT_TYPE)) {
                this.mAccountType = args.getString(KEY_ACCOUNT_TYPE);
            }
        }
        this.mAccountSynController.init(this.mAccount, userHandle);
        this.mRemoveAccountController.init(this.mAccount, userHandle);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mAccountLabel != null) {
            getActivity().setTitle(this.mAccountLabel);
        }
        updateUi();
    }

    public int getMetricsCategory() {
        return 8;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getHelpResource() {
        return R.string.help_url_account_detail;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.account_type_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mAccountSynController = new AccountSyncPreferenceController(context);
        controllers.add(this.mAccountSynController);
        this.mRemoveAccountController = new RemoveAccountPreferenceController(context, this);
        controllers.add(this.mRemoveAccountController);
        controllers.add(new AccountHeaderPreferenceController(context, getLifecycle(), getActivity(), this, getArguments()));
        return controllers;
    }

    /* Access modifiers changed, original: protected */
    public boolean displayTile(Tile tile) {
        if (this.mAccountType == null) {
            return false;
        }
        Bundle metadata = tile.metaData;
        if (metadata == null) {
            return false;
        }
        boolean display = this.mAccountType.equals(metadata.getString(METADATA_IA_ACCOUNT));
        if (display && tile.intent != null) {
            tile.intent.putExtra(EXTRA_ACCOUNT_NAME, this.mAccount.name);
        }
        return display;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateUi() {
        Context context = getContext();
        UserHandle userHandle = null;
        Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_USER_HANDLE)) {
            userHandle = (UserHandle) args.getParcelable(KEY_USER_HANDLE);
        }
        AccountTypePreferenceLoader accountTypePreferenceLoader = new AccountTypePreferenceLoader(this, new AuthenticatorHelper(context, userHandle, null), userHandle);
        PreferenceScreen prefs = accountTypePreferenceLoader.addPreferencesForType(this.mAccountType, getPreferenceScreen());
        if (prefs != null) {
            accountTypePreferenceLoader.updatePreferenceIntents(prefs, this.mAccountType, this.mAccount);
        }
    }
}
