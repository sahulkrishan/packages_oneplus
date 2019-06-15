package com.android.settings.deviceinfo;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.accounts.AccountDetailDashboardFragment;
import com.android.settings.accounts.AccountFeatureProvider;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.overlay.FeatureFactory;

public class BrandedAccountPreferenceController extends BasePreferenceController {
    private static final String KEY_PREFERENCE_TITLE = "branded_account";
    private final Account[] mAccounts = FeatureFactory.getFactory(this.mContext).getAccountFeatureProvider().getAccounts(this.mContext);

    public BrandedAccountPreferenceController(Context context) {
        super(context, KEY_PREFERENCE_TITLE);
    }

    public int getAvailabilityStatus() {
        if (this.mAccounts == null || this.mAccounts.length <= 0) {
            return 3;
        }
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        AccountFeatureProvider accountFeatureProvider = FeatureFactory.getFactory(this.mContext).getAccountFeatureProvider();
        Preference accountPreference = screen.findPreference(KEY_PREFERENCE_TITLE);
        if (accountPreference == null || !(this.mAccounts == null || this.mAccounts.length == 0)) {
            accountPreference.setSummary(this.mAccounts[0].name);
            accountPreference.setOnPreferenceClickListener(new -$$Lambda$BrandedAccountPreferenceController$rFwl4JPEzufcbKCkFgByL5d4NMI(this, accountFeatureProvider));
            return;
        }
        screen.removePreference(accountPreference);
    }

    public static /* synthetic */ boolean lambda$displayPreference$0(BrandedAccountPreferenceController brandedAccountPreferenceController, AccountFeatureProvider accountFeatureProvider, Preference preference) {
        Bundle args = new Bundle();
        args.putParcelable("account", brandedAccountPreferenceController.mAccounts[0]);
        args.putParcelable(AccountDetailDashboardFragment.KEY_USER_HANDLE, Process.myUserHandle());
        args.putString(AccountDetailDashboardFragment.KEY_ACCOUNT_TYPE, accountFeatureProvider.getAccountType());
        new SubSettingLauncher(brandedAccountPreferenceController.mContext).setDestination(AccountDetailDashboardFragment.class.getName()).setTitle((int) R.string.account_sync_title).setArguments(args).setSourceMetricsCategory(40).launch();
        return true;
    }
}
