package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.os.UserHandle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.location.LocationSettings;
import com.android.settings.utils.LocalClassLoaderContextThemeWrapper;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.core.instrumentation.Instrumentable;

public class AccountTypePreferenceLoader {
    private static final String ACCOUNT_KEY = "account";
    private static final String LAUNCHING_LOCATION_SETTINGS = "com.android.settings.accounts.LAUNCHING_LOCATION_SETTINGS";
    private static final String TAG = "AccountTypePrefLoader";
    private AuthenticatorHelper mAuthenticatorHelper;
    private PreferenceFragment mFragment;
    private UserHandle mUserHandle;

    private class FragmentStarter implements OnPreferenceClickListener {
        private final String mClass;
        private final int mTitleRes;

        public FragmentStarter(String className, int title) {
            this.mClass = className;
            this.mTitleRes = title;
        }

        public boolean onPreferenceClick(Preference preference) {
            int metricsCategory;
            if (AccountTypePreferenceLoader.this.mFragment instanceof Instrumentable) {
                metricsCategory = ((Instrumentable) AccountTypePreferenceLoader.this.mFragment).getMetricsCategory();
            } else {
                metricsCategory = 0;
            }
            new SubSettingLauncher(preference.getContext()).setTitle(this.mTitleRes).setDestination(this.mClass).setSourceMetricsCategory(metricsCategory).launch();
            if (this.mClass.equals(LocationSettings.class.getName())) {
                AccountTypePreferenceLoader.this.mFragment.getActivity().sendBroadcast(new Intent(AccountTypePreferenceLoader.LAUNCHING_LOCATION_SETTINGS), "android.permission.WRITE_SECURE_SETTINGS");
            }
            return true;
        }
    }

    public AccountTypePreferenceLoader(PreferenceFragment fragment, AuthenticatorHelper authenticatorHelper, UserHandle userHandle) {
        this.mFragment = fragment;
        this.mAuthenticatorHelper = authenticatorHelper;
        this.mUserHandle = userHandle;
    }

    public PreferenceScreen addPreferencesForType(String accountType, PreferenceScreen parent) {
        String str;
        StringBuilder stringBuilder;
        if (!this.mAuthenticatorHelper.containsAccountType(accountType)) {
            return null;
        }
        try {
            AuthenticatorDescription desc = this.mAuthenticatorHelper.getAccountTypeDescription(accountType);
            if (desc == null || desc.accountPreferencesId == 0) {
                return null;
            }
            Context targetCtx = this.mFragment.getActivity().createPackageContextAsUser(desc.packageName, 0, this.mUserHandle);
            Theme baseTheme = this.mFragment.getResources().newTheme();
            baseTheme.applyStyle(R.style.f958Theme.SettingsBase, true);
            Context themedCtx = new LocalClassLoaderContextThemeWrapper(getClass(), targetCtx, 0);
            themedCtx.getTheme().setTo(baseTheme);
            return this.mFragment.getPreferenceManager().inflateFromResource(themedCtx, desc.accountPreferencesId, parent);
        } catch (NameNotFoundException e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Couldn't load preferences.xml file from ");
            stringBuilder.append(null.packageName);
            Log.w(str, stringBuilder.toString());
            return null;
        } catch (NotFoundException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Couldn't load preferences.xml file from ");
            stringBuilder.append(null.packageName);
            Log.w(str, stringBuilder.toString());
            return null;
        }
    }

    public void updatePreferenceIntents(PreferenceGroup prefs, final String acccountType, Account account) {
        final PackageManager pm = this.mFragment.getActivity().getPackageManager();
        int i = 0;
        while (i < prefs.getPreferenceCount()) {
            Preference pref = prefs.getPreference(i);
            if (pref instanceof PreferenceGroup) {
                updatePreferenceIntents((PreferenceGroup) pref, acccountType, account);
            }
            Intent intent = pref.getIntent();
            if (intent != null) {
                if (TextUtils.equals(intent.getAction(), "android.settings.LOCATION_SOURCE_SETTINGS")) {
                    pref.setOnPreferenceClickListener(new FragmentStarter(LocationSettings.class.getName(), R.string.location_settings_title));
                } else if (pm.resolveActivityAsUser(intent, 65536, this.mUserHandle.getIdentifier()) == null) {
                    prefs.removePreference(pref);
                } else {
                    intent.putExtra("account", account);
                    intent.setFlags(intent.getFlags() | 268435456);
                    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            Intent prefIntent = preference.getIntent();
                            if (AccountTypePreferenceLoader.this.isSafeIntent(pm, prefIntent, acccountType)) {
                                AccountTypePreferenceLoader.this.mFragment.getActivity().startActivityAsUser(prefIntent, AccountTypePreferenceLoader.this.mUserHandle);
                            } else {
                                String str = AccountTypePreferenceLoader.TAG;
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append("Refusing to launch authenticator intent becauseit exploits Settings permissions: ");
                                stringBuilder.append(prefIntent);
                                Log.e(str, stringBuilder.toString());
                            }
                            return true;
                        }
                    });
                }
            }
            i++;
        }
    }

    private boolean isSafeIntent(PackageManager pm, Intent intent, String acccountType) {
        AuthenticatorDescription authDesc = this.mAuthenticatorHelper.getAccountTypeDescription(acccountType);
        boolean z = false;
        ResolveInfo resolveInfo = pm.resolveActivityAsUser(intent, 0, this.mUserHandle.getIdentifier());
        if (resolveInfo == null) {
            return false;
        }
        ActivityInfo resolvedActivityInfo = resolveInfo.activityInfo;
        ApplicationInfo resolvedAppInfo = resolvedActivityInfo.applicationInfo;
        try {
            if (resolvedActivityInfo.exported && (resolvedActivityInfo.permission == null || pm.checkPermission(resolvedActivityInfo.permission, authDesc.packageName) == 0)) {
                return true;
            }
            if (resolvedAppInfo.uid == pm.getApplicationInfo(authDesc.packageName, 0).uid) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Intent considered unsafe due to exception.", e);
            return false;
        }
    }
}
