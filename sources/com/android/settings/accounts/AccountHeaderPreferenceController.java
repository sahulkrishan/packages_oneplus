package com.android.settings.accounts;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AccountHeaderPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume {
    private static final String KEY_ACCOUNT_HEADER = "account_header";
    private final Account mAccount;
    private final Activity mActivity;
    private LayoutPreference mHeaderPreference;
    private final PreferenceFragment mHost;
    private final UserHandle mUserHandle;

    public AccountHeaderPreferenceController(Context context, Lifecycle lifecycle, Activity activity, PreferenceFragment host, Bundle args) {
        super(context);
        this.mActivity = activity;
        this.mHost = host;
        if (args == null || !args.containsKey("account")) {
            this.mAccount = null;
        } else {
            this.mAccount = (Account) args.getParcelable("account");
        }
        if (args == null || !args.containsKey(AccountDetailDashboardFragment.KEY_USER_HANDLE)) {
            this.mUserHandle = null;
        } else {
            this.mUserHandle = (UserHandle) args.getParcelable(AccountDetailDashboardFragment.KEY_USER_HANDLE);
        }
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return (this.mAccount == null || this.mUserHandle == null) ? false : true;
    }

    public String getPreferenceKey() {
        return KEY_ACCOUNT_HEADER;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mHeaderPreference = (LayoutPreference) screen.findPreference(KEY_ACCOUNT_HEADER);
    }

    public void onResume() {
        EntityHeaderController.newInstance(this.mActivity, this.mHost, this.mHeaderPreference.findViewById(R.id.entity_header)).setLabel(this.mAccount.name).setIcon(new AuthenticatorHelper(this.mContext, this.mUserHandle, null).getDrawableForType(this.mContext, this.mAccount.type)).done(this.mActivity, true);
    }
}
