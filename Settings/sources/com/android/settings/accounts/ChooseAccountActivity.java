package com.android.settings.accounts;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.CharSequences;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.enterprise.EnterprisePrivacyFeatureProvider;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.widget.FooterPreference;
import com.google.android.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class ChooseAccountActivity extends SettingsPreferenceFragment {
    private static final String TAG = "ChooseAccountActivity";
    private HashMap<String, ArrayList<String>> mAccountTypeToAuthorities = null;
    public HashSet<String> mAccountTypesFilter;
    private PreferenceGroup mAddAccountGroup;
    private AuthenticatorDescription[] mAuthDescs;
    private String[] mAuthorities;
    private FooterPreference mEnterpriseDisclosurePreference = null;
    private EnterprisePrivacyFeatureProvider mFeatureProvider;
    private final ArrayList<ProviderEntry> mProviderList = new ArrayList();
    private Map<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap();
    private UserManager mUm;
    private UserHandle mUserHandle;

    private static class ProviderEntry implements Comparable<ProviderEntry> {
        private final CharSequence name;
        private final String type;

        ProviderEntry(CharSequence providerName, String accountType) {
            this.name = providerName;
            this.type = accountType;
        }

        public int compareTo(ProviderEntry another) {
            if (this.name == null) {
                return -1;
            }
            if (another.name == null) {
                return 1;
            }
            return CharSequences.compareToIgnoreCase(this.name, another.name);
        }
    }

    public int getMetricsCategory() {
        return 10;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Activity activity = getActivity();
        this.mFeatureProvider = FeatureFactory.getFactory(activity).getEnterprisePrivacyFeatureProvider(activity);
        addPreferencesFromResource(R.xml.add_account_settings);
        this.mAuthorities = getIntent().getStringArrayExtra(AccountPreferenceBase.AUTHORITIES_FILTER_KEY);
        String[] accountTypesFilter = getIntent().getStringArrayExtra(AccountPreferenceBase.ACCOUNT_TYPES_FILTER_KEY);
        if (accountTypesFilter != null) {
            this.mAccountTypesFilter = new HashSet();
            for (String accountType : accountTypesFilter) {
                this.mAccountTypesFilter.add(accountType);
            }
        }
        this.mAddAccountGroup = getPreferenceScreen();
        this.mUm = UserManager.get(getContext());
        this.mUserHandle = Utils.getSecureTargetUser(getActivity().getActivityToken(), this.mUm, null, getIntent().getExtras());
        updateAuthDescriptions();
    }

    private void updateAuthDescriptions() {
        this.mAuthDescs = AccountManager.get(getContext()).getAuthenticatorTypesAsUser(this.mUserHandle.getIdentifier());
        for (int i = 0; i < this.mAuthDescs.length; i++) {
            this.mTypeToAuthDescription.put(this.mAuthDescs[i].type, this.mAuthDescs[i]);
        }
        onAuthDescriptionsUpdated();
    }

    private void onAuthDescriptionsUpdated() {
        String str;
        for (String accountType : this.mAuthDescs) {
            String accountType2 = accountType2.type;
            CharSequence providerName = getLabelForType(accountType2);
            ArrayList<String> accountAuths = getAuthoritiesForAccountType(accountType2);
            boolean addAccountPref = true;
            if (this.mAuthorities != null && this.mAuthorities.length > 0 && accountAuths != null) {
                addAccountPref = false;
                for (Object contains : this.mAuthorities) {
                    if (accountAuths.contains(contains)) {
                        addAccountPref = true;
                        break;
                    }
                }
            }
            if (!(!addAccountPref || this.mAccountTypesFilter == null || this.mAccountTypesFilter.contains(accountType2))) {
                addAccountPref = false;
            }
            if (addAccountPref) {
                if (!"com.oneplus.account".equals(accountType2)) {
                    this.mProviderList.add(new ProviderEntry(providerName, accountType2));
                }
            } else if (Log.isLoggable(TAG, 2)) {
                str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Skipped pref ");
                stringBuilder.append(providerName);
                stringBuilder.append(": has no authority we need");
                Log.v(str, stringBuilder.toString());
            }
        }
        Context context = getPreferenceScreen().getContext();
        if (this.mProviderList.size() == 1) {
            EnforcedAdmin admin = RestrictedLockUtils.checkIfAccountManagementDisabled(context, ((ProviderEntry) this.mProviderList.get(0)).type, this.mUserHandle.getIdentifier());
            if (admin != null) {
                setResult(0, RestrictedLockUtils.getShowAdminSupportDetailsIntent(context, admin));
                finish();
                return;
            }
            finishWithAccountType(((ProviderEntry) this.mProviderList.get(0)).type);
        } else if (this.mProviderList.size() > 0) {
            Collections.sort(this.mProviderList);
            this.mAddAccountGroup.removeAll();
            Iterator it = this.mProviderList.iterator();
            while (it.hasNext()) {
                ProviderEntry pref = (ProviderEntry) it.next();
                ProviderPreference p = new ProviderPreference(getPreferenceScreen().getContext(), pref.type, getDrawableForType(pref.type), pref.name);
                p.checkAccountManagementAndSetDisabled(this.mUserHandle.getIdentifier());
                if (!(TextUtils.isEmpty(pref.name) || pref.name == null)) {
                    this.mAddAccountGroup.addPreference(p);
                }
            }
            addEnterpriseDisclosure();
        } else {
            if (Log.isLoggable(TAG, 2)) {
                StringBuilder auths = new StringBuilder();
                for (String a : this.mAuthorities) {
                    auths.append(a);
                    auths.append(' ');
                }
                str = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("No providers found for authorities: ");
                stringBuilder2.append(auths);
                Log.v(str, stringBuilder2.toString());
            }
            setResult(0);
            finish();
        }
    }

    private void addEnterpriseDisclosure() {
        CharSequence disclosure = this.mFeatureProvider.getDeviceOwnerDisclosure();
        if (disclosure != null) {
            if (this.mEnterpriseDisclosurePreference == null) {
                this.mEnterpriseDisclosurePreference = this.mFooterPreferenceMixin.createFooterPreference();
                this.mEnterpriseDisclosurePreference.setSelectable(false);
            }
            this.mEnterpriseDisclosurePreference.setTitle(disclosure);
            this.mAddAccountGroup.addPreference(this.mEnterpriseDisclosurePreference);
        }
    }

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        if (this.mAccountTypeToAuthorities == null) {
            this.mAccountTypeToAuthorities = Maps.newHashMap();
            for (SyncAdapterType sa : ContentResolver.getSyncAdapterTypesAsUser(this.mUserHandle.getIdentifier())) {
                ArrayList<String> authorities = (ArrayList) this.mAccountTypeToAuthorities.get(sa.accountType);
                if (authorities == null) {
                    authorities = new ArrayList();
                    this.mAccountTypeToAuthorities.put(sa.accountType, authorities);
                }
                if (Log.isLoggable(TAG, 2)) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("added authority ");
                    stringBuilder.append(sa.authority);
                    stringBuilder.append(" to accountType ");
                    stringBuilder.append(sa.accountType);
                    Log.d(str, stringBuilder.toString());
                }
                authorities.add(sa.authority);
            }
        }
        return (ArrayList) this.mAccountTypeToAuthorities.get(type);
    }

    /* Access modifiers changed, original: protected */
    public Drawable getDrawableForType(String accountType) {
        String str;
        StringBuilder stringBuilder;
        Drawable icon = null;
        if (this.mTypeToAuthDescription.containsKey(accountType)) {
            try {
                AuthenticatorDescription desc = (AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType);
                icon = getPackageManager().getUserBadgedIcon(getActivity().createPackageContextAsUser(desc.packageName, 0, this.mUserHandle).getDrawable(desc.iconId), this.mUserHandle);
            } catch (NameNotFoundException e) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("No icon name for account type ");
                stringBuilder.append(accountType);
                Log.w(str, stringBuilder.toString());
            } catch (NotFoundException e2) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("No icon resource for account type ");
                stringBuilder.append(accountType);
                Log.w(str, stringBuilder.toString());
            }
        }
        if (icon != null) {
            return icon;
        }
        return getPackageManager().getDefaultActivityIcon();
    }

    /* Access modifiers changed, original: protected */
    public CharSequence getLabelForType(String accountType) {
        String str;
        StringBuilder stringBuilder;
        if (!this.mTypeToAuthDescription.containsKey(accountType)) {
            return null;
        }
        try {
            AuthenticatorDescription desc = (AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType);
            return getActivity().createPackageContextAsUser(desc.packageName, 0, this.mUserHandle).getResources().getText(desc.labelId);
        } catch (NameNotFoundException e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("No label name for account type ");
            stringBuilder.append(accountType);
            Log.w(str, stringBuilder.toString());
            return null;
        } catch (NotFoundException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("No label resource for account type ");
            stringBuilder.append(accountType);
            Log.w(str, stringBuilder.toString());
            return null;
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof ProviderPreference) {
            ProviderPreference pref = (ProviderPreference) preference;
            if (Log.isLoggable(TAG, 2)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Attempting to add account of type ");
                stringBuilder.append(pref.getAccountType());
                Log.v(str, stringBuilder.toString());
            }
            finishWithAccountType(pref.getAccountType());
        }
        return true;
    }

    private void finishWithAccountType(String accountType) {
        Intent intent = new Intent();
        intent.putExtra("selected_account", accountType);
        intent.putExtra("android.intent.extra.USER", this.mUserHandle);
        setResult(-1, intent);
        finish();
    }
}
