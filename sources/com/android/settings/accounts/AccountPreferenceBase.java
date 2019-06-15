package com.android.settings.accounts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener;
import java.text.DateFormat;
import java.util.Date;

abstract class AccountPreferenceBase extends SettingsPreferenceFragment implements OnAccountsUpdateListener {
    public static final String ACCOUNT_TYPES_FILTER_KEY = "account_types";
    public static final String AUTHORITIES_FILTER_KEY = "authorities";
    protected static final String TAG = "AccountPreferenceBase";
    protected static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    protected AccountTypePreferenceLoader mAccountTypePreferenceLoader;
    protected AuthenticatorHelper mAuthenticatorHelper;
    private DateFormat mDateFormat;
    private Object mStatusChangeListenerHandle;
    private SyncStatusObserver mSyncStatusObserver = new -$$Lambda$AccountPreferenceBase$duCjsGZhZVNysJ2Rj1t7N9PkFAY(this);
    private DateFormat mTimeFormat;
    private UserManager mUm;
    protected UserHandle mUserHandle;

    AccountPreferenceBase() {
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mUm = (UserManager) getSystemService("user");
        Activity activity = getActivity();
        this.mUserHandle = Utils.getSecureTargetUser(activity.getActivityToken(), this.mUm, getArguments(), activity.getIntent().getExtras());
        this.mAuthenticatorHelper = new AuthenticatorHelper(activity, this.mUserHandle, this);
        this.mAccountTypePreferenceLoader = new AccountTypePreferenceLoader(this, this.mAuthenticatorHelper, this.mUserHandle);
    }

    public void onAccountsUpdate(UserHandle userHandle) {
    }

    /* Access modifiers changed, original: protected */
    public void onAuthDescriptionsUpdated() {
    }

    /* Access modifiers changed, original: protected */
    public void onSyncStateUpdated() {
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        this.mDateFormat = android.text.format.DateFormat.getDateFormat(activity);
        this.mTimeFormat = android.text.format.DateFormat.getTimeFormat(activity);
    }

    public void onResume() {
        super.onResume();
        this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(13, this.mSyncStatusObserver);
        onSyncStateUpdated();
    }

    public void onPause() {
        super.onPause();
        ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
    }

    public void updateAuthDescriptions() {
        this.mAuthenticatorHelper.updateAuthDescriptions(getActivity());
        onAuthDescriptionsUpdated();
    }

    /* Access modifiers changed, original: protected */
    public Drawable getDrawableForType(String accountType) {
        return this.mAuthenticatorHelper.getDrawableForType(getActivity(), accountType);
    }

    /* Access modifiers changed, original: protected */
    public CharSequence getLabelForType(String accountType) {
        return this.mAuthenticatorHelper.getLabelForType(getActivity(), accountType);
    }

    /* Access modifiers changed, original: protected */
    public String formatSyncDate(Date date) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.mDateFormat.format(date));
        stringBuilder.append(" ");
        stringBuilder.append(this.mTimeFormat.format(date));
        return stringBuilder.toString();
    }
}
