package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.text.BidiFormatter;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.AccessiblePreferenceCategory;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.accounts.AuthenticatorHelper;
import com.android.settingslib.accounts.AuthenticatorHelper.OnAccountsUpdateListener;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AccountPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnAccountsUpdateListener, OnPreferenceClickListener, LifecycleObserver, OnPause, OnResume {
    private static final String KEY_ONEPLUS_ACCOUNT = "ONEPLUS_ACCOUNT";
    private static final int ORDER_ACCOUNT_PROFILES = 1;
    private static final int ORDER_LAST = 1002;
    private static final int ORDER_NEXT_TO_LAST = 1001;
    private static final int ORDER_NEXT_TO_NEXT_TO_LAST = 1000;
    private static final String TAG = "AccountPrefController";
    private int mAccountProfileOrder;
    private String[] mAuthorities;
    private int mAuthoritiesCount;
    private AccountRestrictionHelper mHelper;
    private ManagedProfileBroadcastReceiver mManagedProfileBroadcastReceiver;
    private MetricsFeatureProvider mMetricsFeatureProvider;
    private SettingsPreferenceFragment mParent;
    private Preference mProfileNotAvailablePreference;
    private SparseArray<ProfileData> mProfiles;
    private UserManager mUm;

    private class ManagedProfileBroadcastReceiver extends BroadcastReceiver {
        private boolean mListeningToManagedProfileEvents;

        private ManagedProfileBroadcastReceiver() {
        }

        /* synthetic */ ManagedProfileBroadcastReceiver(AccountPreferenceController x0, AnonymousClass1 x1) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str = AccountPreferenceController.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Received broadcast: ");
            stringBuilder.append(action);
            Log.v(str, stringBuilder.toString());
            if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED") || action.equals("android.intent.action.MANAGED_PROFILE_ADDED")) {
                AccountPreferenceController.this.stopListeningToAccountUpdates();
                AccountPreferenceController.this.updateUi();
                AccountPreferenceController.this.listenToAccountUpdates();
                return;
            }
            str = AccountPreferenceController.TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot handle received broadcast: ");
            stringBuilder.append(intent.getAction());
            Log.w(str, stringBuilder.toString());
        }

        public void register(Context context) {
            if (!this.mListeningToManagedProfileEvents) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
                intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
                context.registerReceiver(this, intentFilter);
                this.mListeningToManagedProfileEvents = true;
            }
        }

        public void unregister(Context context) {
            if (this.mListeningToManagedProfileEvents) {
                context.unregisterReceiver(this);
                this.mListeningToManagedProfileEvents = false;
            }
        }
    }

    public static class ProfileData {
        public ArrayMap<String, AccountTypePreference> accountPreferences = new ArrayMap();
        public RestrictedPreference addAccountPreference;
        public AuthenticatorHelper authenticatorHelper;
        public Preference managedProfilePreference;
        public boolean pendingRemoval;
        public PreferenceGroup preferenceGroup;
        public RestrictedPreference removeWorkProfilePreference;
        public UserInfo userInfo;
    }

    public AccountPreferenceController(Context context, SettingsPreferenceFragment parent, String[] authorities) {
        this(context, parent, authorities, new AccountRestrictionHelper(context));
    }

    @VisibleForTesting
    AccountPreferenceController(Context context, SettingsPreferenceFragment parent, String[] authorities, AccountRestrictionHelper helper) {
        super(context);
        this.mProfiles = new SparseArray();
        this.mManagedProfileBroadcastReceiver = new ManagedProfileBroadcastReceiver(this, null);
        this.mAuthoritiesCount = 0;
        this.mAccountProfileOrder = 1;
        this.mUm = (UserManager) context.getSystemService("user");
        this.mAuthorities = authorities;
        this.mParent = parent;
        if (this.mAuthorities != null) {
            this.mAuthoritiesCount = this.mAuthorities.length;
        }
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(this.mContext).getMetricsFeatureProvider();
        this.mHelper = helper;
    }

    public boolean isAvailable() {
        return this.mUm.isManagedProfile() ^ 1;
    }

    public String getPreferenceKey() {
        return null;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        updateUi();
    }

    public void updateRawDataToIndex(List<SearchIndexableRaw> rawData) {
        if (isAvailable()) {
            Resources res = this.mContext.getResources();
            String screenTitle = res.getString(R.string.account_settings_title);
            List<UserInfo> profiles = this.mUm.getProfiles(UserHandle.myUserId());
            int profilesCount = profiles.size();
            for (int i = 0; i < profilesCount; i++) {
                UserInfo userInfo = (UserInfo) profiles.get(i);
                if (userInfo.isEnabled()) {
                    SearchIndexableRaw data;
                    if (!this.mHelper.hasBaseUserRestriction("no_modify_accounts", userInfo.id)) {
                        data = new SearchIndexableRaw(this.mContext);
                        data.title = res.getString(R.string.add_account_label);
                        data.screenTitle = screenTitle;
                        rawData.add(data);
                    }
                    if (userInfo.isManagedProfile() && userInfo.id != 999) {
                        if (!this.mHelper.hasBaseUserRestriction("no_remove_managed_profile", UserHandle.myUserId())) {
                            data = new SearchIndexableRaw(this.mContext);
                            data.title = res.getString(R.string.remove_managed_profile_label);
                            data.screenTitle = screenTitle;
                            rawData.add(data);
                        }
                        data = new SearchIndexableRaw(this.mContext);
                        data.title = res.getString(R.string.managed_profile_settings_title);
                        data.screenTitle = screenTitle;
                        rawData.add(data);
                    }
                }
            }
        }
    }

    public void onResume() {
        updateUi();
        this.mManagedProfileBroadcastReceiver.register(this.mContext);
        listenToAccountUpdates();
    }

    public void onPause() {
        stopListeningToAccountUpdates();
        this.mManagedProfileBroadcastReceiver.unregister(this.mContext);
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        ProfileData profileData = (ProfileData) this.mProfiles.get(userHandle.getIdentifier());
        if (profileData != null) {
            updateAccountTypes(profileData);
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Missing Settings screen for: ");
        stringBuilder.append(userHandle.getIdentifier());
        Log.w(str, stringBuilder.toString());
    }

    public boolean onPreferenceClick(Preference preference) {
        int count = this.mProfiles.size();
        int i = 0;
        while (i < count) {
            ProfileData profileData = (ProfileData) this.mProfiles.valueAt(i);
            if (preference == profileData.addAccountPreference) {
                Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
                intent.putExtra("android.intent.extra.USER", profileData.userInfo.getUserHandle());
                intent.putExtra(AccountPreferenceBase.AUTHORITIES_FILTER_KEY, this.mAuthorities);
                this.mContext.startActivity(intent);
                return true;
            } else if (preference == profileData.removeWorkProfilePreference) {
                RemoveUserFragment.newInstance(profileData.userInfo.id).show(this.mParent.getFragmentManager(), "removeUser");
                return true;
            } else if (preference == profileData.managedProfilePreference) {
                Bundle arguments = new Bundle();
                arguments.putParcelable("android.intent.extra.USER", profileData.userInfo.getUserHandle());
                new SubSettingLauncher(this.mContext).setSourceMetricsCategory(this.mParent.getMetricsCategory()).setDestination(ManagedProfileSettings.class.getName()).setTitle((int) R.string.managed_profile_settings_title).setArguments(arguments).launch();
                return true;
            } else {
                i++;
            }
        }
        return false;
    }

    private void updateUi() {
        if (isAvailable()) {
            int i;
            int size = this.mProfiles.size();
            for (i = 0; i < size; i++) {
                ((ProfileData) this.mProfiles.valueAt(i)).pendingRemoval = true;
            }
            size = 0;
            if (this.mUm.isRestrictedProfile()) {
                updateProfileUi(this.mUm.getUserInfo(UserHandle.myUserId()));
            } else {
                List<UserInfo> profiles = this.mUm.getProfiles(UserHandle.myUserId());
                int profilesCount = profiles.size();
                if (isMultiAppEnable(profiles) ? profilesCount <= 2 : profilesCount <= 1) {
                    boolean addCategory = false;
                }
                for (int i2 = 0; i2 < profilesCount; i2++) {
                    if (((UserInfo) profiles.get(i2)).id != 999) {
                        updateProfileUi((UserInfo) profiles.get(i2));
                    }
                }
            }
            cleanUpPreferences();
            i = this.mProfiles.size();
            while (size < i) {
                updateAccountTypes((ProfileData) this.mProfiles.valueAt(size));
                size++;
            }
            return;
        }
        Log.e(TAG, "We should not be showing settings for a managed profile");
    }

    private void updateProfileUi(UserInfo userInfo) {
        updateProfileUi(userInfo, true);
    }

    private void updateProfileUi(UserInfo userInfo, boolean addCategory) {
        if (this.mParent.getPreferenceManager() != null) {
            ProfileData data = (ProfileData) this.mProfiles.get(userInfo.id);
            if (data != null) {
                data.pendingRemoval = false;
                if (userInfo.isEnabled()) {
                    data.authenticatorHelper = new AuthenticatorHelper(this.mContext, userInfo.getUserHandle(), this);
                }
                return;
            }
            Context context = this.mContext;
            ProfileData profileData = new ProfileData();
            profileData.userInfo = userInfo;
            AccessiblePreferenceCategory preferenceGroup = this.mHelper.createAccessiblePreferenceCategory(this.mParent.getPreferenceManager().getContext());
            int i = this.mAccountProfileOrder;
            this.mAccountProfileOrder = i + 1;
            preferenceGroup.setOrder(i);
            if (isSingleProfile()) {
                preferenceGroup.setTitle((CharSequence) context.getString(R.string.account_for_section_header, new Object[]{BidiFormatter.getInstance().unicodeWrap(userInfo.name)}));
                preferenceGroup.setContentDescription(this.mContext.getString(R.string.account_settings));
            } else if (userInfo.isManagedProfile()) {
                preferenceGroup.setTitle((int) R.string.category_work);
                preferenceGroup.setSummary((CharSequence) getWorkGroupSummary(context, userInfo));
                preferenceGroup.setContentDescription(this.mContext.getString(R.string.accessibility_category_work, new Object[]{workGroupSummary}));
                profileData.removeWorkProfilePreference = newRemoveWorkProfilePreference();
                this.mHelper.enforceRestrictionOnPreference(profileData.removeWorkProfilePreference, "no_remove_managed_profile", UserHandle.myUserId());
                profileData.managedProfilePreference = newManagedProfileSettings();
            } else {
                preferenceGroup.setTitle((int) R.string.category_personal);
                preferenceGroup.setContentDescription(this.mContext.getString(R.string.accessibility_category_personal));
            }
            PreferenceScreen screen = this.mParent.getPreferenceScreen();
            if (screen != null && addCategory) {
                screen.addPreference(preferenceGroup);
            }
            if (addCategory) {
                profileData.preferenceGroup = preferenceGroup;
            } else {
                profileData.preferenceGroup = screen;
            }
            if (userInfo.isEnabled()) {
                profileData.authenticatorHelper = new AuthenticatorHelper(context, userInfo.getUserHandle(), this);
                profileData.addAccountPreference = newAddAccountPreference();
                this.mHelper.enforceRestrictionOnPreference(profileData.addAccountPreference, "no_modify_accounts", userInfo.id);
            }
            this.mProfiles.put(userInfo.id, profileData);
        }
    }

    private RestrictedPreference newAddAccountPreference() {
        RestrictedPreference preference = new RestrictedPreference(this.mParent.getPreferenceManager().getContext());
        preference.setTitle((int) R.string.add_account_label);
        preference.setIcon((int) R.drawable.ic_menu_add);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(1000);
        return preference;
    }

    private RestrictedPreference newRemoveWorkProfilePreference() {
        RestrictedPreference preference = new RestrictedPreference(this.mParent.getPreferenceManager().getContext());
        preference.setTitle((int) R.string.remove_managed_profile_label);
        preference.setIcon((int) R.drawable.ic_delete);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(1002);
        return preference;
    }

    private Preference newManagedProfileSettings() {
        Preference preference = new Preference(this.mParent.getPreferenceManager().getContext());
        preference.setTitle((int) R.string.managed_profile_settings_title);
        preference.setIcon((int) R.drawable.ic_settings_24dp);
        preference.setOnPreferenceClickListener(this);
        preference.setOrder(1001);
        return preference;
    }

    private String getWorkGroupSummary(Context context, UserInfo userInfo) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo adminApplicationInfo = Utils.getAdminApplicationInfo(context, userInfo.id);
        if (adminApplicationInfo == null) {
            return null;
        }
        CharSequence appLabel = packageManager.getApplicationLabel(adminApplicationInfo);
        return this.mContext.getString(R.string.managing_admin, new Object[]{appLabel});
    }

    /* Access modifiers changed, original: 0000 */
    public void cleanUpPreferences() {
        PreferenceScreen screen = this.mParent.getPreferenceScreen();
        if (screen != null) {
            for (int i = this.mProfiles.size() - 1; i >= 0; i--) {
                ProfileData data = (ProfileData) this.mProfiles.valueAt(i);
                if (data.pendingRemoval) {
                    screen.removePreference(data.preferenceGroup);
                    this.mProfiles.removeAt(i);
                }
            }
        }
    }

    private void listenToAccountUpdates() {
        int count = this.mProfiles.size();
        for (int i = 0; i < count; i++) {
            AuthenticatorHelper authenticatorHelper = ((ProfileData) this.mProfiles.valueAt(i)).authenticatorHelper;
            if (authenticatorHelper != null) {
                authenticatorHelper.listenToAccountUpdates();
            }
        }
    }

    private void stopListeningToAccountUpdates() {
        int count = this.mProfiles.size();
        for (int i = 0; i < count; i++) {
            AuthenticatorHelper authenticatorHelper = ((ProfileData) this.mProfiles.valueAt(i)).authenticatorHelper;
            if (authenticatorHelper != null) {
                authenticatorHelper.stopListeningToAccountUpdates();
            }
        }
    }

    private void updateAccountTypes(ProfileData profileData) {
        if (this.mParent.getPreferenceManager() != null && profileData.preferenceGroup.getPreferenceManager() != null) {
            int i = 0;
            if (profileData.userInfo.isEnabled()) {
                ArrayMap<String, AccountTypePreference> preferenceToRemove = new ArrayMap(profileData.accountPreferences);
                ArrayList<AccountTypePreference> preferences = getAccountTypePreferences(profileData.authenticatorHelper, profileData.userInfo.getUserHandle(), preferenceToRemove);
                int count = preferences.size();
                while (i < count) {
                    AccountTypePreference preference = (AccountTypePreference) preferences.get(i);
                    preference.setOrder(i);
                    String key = preference.getKey();
                    if (!profileData.accountPreferences.containsKey(key)) {
                        profileData.preferenceGroup.addPreference(preference);
                        profileData.accountPreferences.put(key, preference);
                    }
                    i++;
                }
                if (profileData.addAccountPreference != null) {
                    if (OPUtils.isAppExist(this.mContext, "com.oneplus.account")) {
                        PreferenceGroup screen = profileData.preferenceGroup;
                        if (screen != null && screen.findPreference(KEY_ONEPLUS_ACCOUNT) == null) {
                            Log.d(TAG, "add newAddOneplusAccountPreference");
                            screen.addPreference(newAddOneplusAccountPreference(this.mContext));
                        }
                    }
                    profileData.preferenceGroup.addPreference(profileData.addAccountPreference);
                }
                for (String key2 : preferenceToRemove.keySet()) {
                    profileData.preferenceGroup.removePreference((Preference) profileData.accountPreferences.get(key2));
                    profileData.accountPreferences.remove(key2);
                }
            } else {
                profileData.preferenceGroup.removeAll();
                if (this.mProfileNotAvailablePreference == null) {
                    this.mProfileNotAvailablePreference = new Preference(this.mParent.getPreferenceManager().getContext());
                }
                this.mProfileNotAvailablePreference.setEnabled(false);
                this.mProfileNotAvailablePreference.setIcon((int) R.drawable.empty_icon);
                this.mProfileNotAvailablePreference.setTitle(null);
                this.mProfileNotAvailablePreference.setSummary((int) R.string.managed_profile_not_available_label);
                profileData.preferenceGroup.addPreference(this.mProfileNotAvailablePreference);
            }
            if (profileData.removeWorkProfilePreference != null) {
                profileData.preferenceGroup.addPreference(profileData.removeWorkProfilePreference);
            }
            if (profileData.managedProfilePreference != null) {
                profileData.preferenceGroup.addPreference(profileData.managedProfilePreference);
            }
        }
    }

    private ArrayList<AccountTypePreference> getAccountTypePreferences(AuthenticatorHelper helper, UserHandle userHandle, ArrayMap<String, AccountTypePreference> preferenceToRemove) {
        AuthenticatorHelper authenticatorHelper = helper;
        Parcelable parcelable = userHandle;
        String[] accountTypes = helper.getEnabledAccountTypes();
        ArrayList<AccountTypePreference> accountTypePreferences = new ArrayList(accountTypes.length);
        int i = 0;
        while (i < accountTypes.length) {
            String[] accountTypes2;
            Object obj;
            String accountType = accountTypes[i];
            if (accountTypeHasAnyRequestedAuthorities(authenticatorHelper, accountType)) {
                CharSequence label = authenticatorHelper.getLabelForType(this.mContext, accountType);
                if (label != null) {
                    Account[] accounts;
                    int titleResId;
                    String titleResPackageName = authenticatorHelper.getPackageForType(accountType);
                    int titleResId2 = authenticatorHelper.getLabelIdForType(accountType);
                    Account[] accounts2 = AccountManager.get(this.mContext).getAccountsByTypeAsUser(accountType, parcelable);
                    Drawable icon = authenticatorHelper.getDrawableForType(this.mContext, accountType);
                    Context prefContext = this.mParent.getPreferenceManager().getContext();
                    int length = accounts2.length;
                    int i2 = 0;
                    while (i2 < length) {
                        int i3;
                        int i4;
                        Account account = accounts2[i2];
                        AccountTypePreference preference = (AccountTypePreference) preferenceToRemove.remove(AccountTypePreference.buildKey(account));
                        if (preference != null) {
                            accountTypePreferences.add(preference);
                            accountTypes2 = accountTypes;
                        } else {
                            ArrayList<String> auths = authenticatorHelper.getAuthoritiesForAccountType(account.type);
                            accountTypes2 = accountTypes;
                            if (AccountRestrictionHelper.showAccount(this.mAuthorities, auths) != null) {
                                accountTypes = new Bundle();
                                accountTypes.putParcelable("account", account);
                                accountTypes.putParcelable(AccountDetailDashboardFragment.KEY_USER_HANDLE, parcelable);
                                accountTypes.putString(AccountDetailDashboardFragment.KEY_ACCOUNT_TYPE, accountType);
                                accountTypes.putString(AccountDetailDashboardFragment.KEY_ACCOUNT_LABEL, label.toString());
                                accountTypes.putInt(AccountDetailDashboardFragment.KEY_ACCOUNT_TITLE_RES, titleResId2);
                                accountTypes.putParcelable("android.intent.extra.USER", parcelable);
                                Account account2 = account;
                                int metricsCategory = this.mMetricsFeatureProvider.getMetricsCategory(this.mParent);
                                i3 = i2;
                                i4 = length;
                                accounts = accounts2;
                                titleResId = titleResId2;
                                accountTypePreferences.add(new AccountTypePreference(prefContext, metricsCategory, account2, titleResPackageName, titleResId2, label, AccountDetailDashboardFragment.class.getName(), accountTypes, icon));
                                i2 = i3 + 1;
                                accountTypes = accountTypes2;
                                length = i4;
                                accounts2 = accounts;
                                titleResId2 = titleResId;
                                obj = userHandle;
                            }
                        }
                        i3 = i2;
                        i4 = length;
                        accounts = accounts2;
                        titleResId = titleResId2;
                        i2 = i3 + 1;
                        accountTypes = accountTypes2;
                        length = i4;
                        accounts2 = accounts;
                        titleResId2 = titleResId;
                        obj = userHandle;
                    }
                    accountTypes2 = accountTypes;
                    accounts = accounts2;
                    titleResId = titleResId2;
                    authenticatorHelper.preloadDrawableForType(this.mContext, accountType);
                    i++;
                    accountTypes = accountTypes2;
                    obj = userHandle;
                }
            }
            accountTypes2 = accountTypes;
            i++;
            accountTypes = accountTypes2;
            obj = userHandle;
        }
        Collections.sort(accountTypePreferences, new Comparator<AccountTypePreference>() {
            public int compare(AccountTypePreference t1, AccountTypePreference t2) {
                int result = t1.getSummary().toString().compareTo(t2.getSummary().toString());
                if (result != 0) {
                    return result;
                }
                return t1.getTitle().toString().compareTo(t2.getTitle().toString());
            }
        });
        return accountTypePreferences;
    }

    private boolean accountTypeHasAnyRequestedAuthorities(AuthenticatorHelper helper, String accountType) {
        if (this.mAuthoritiesCount == 0) {
            return true;
        }
        ArrayList<String> authoritiesForType = helper.getAuthoritiesForAccountType(accountType);
        if (authoritiesForType == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("No sync authorities for account type: ");
            stringBuilder.append(accountType);
            Log.d(str, stringBuilder.toString());
            return false;
        }
        for (int j = 0; j < this.mAuthoritiesCount; j++) {
            if (authoritiesForType.contains(this.mAuthorities[j])) {
                return true;
            }
        }
        return false;
    }

    private boolean isSingleProfile() {
        return this.mUm.isLinkedUser() || this.mUm.getProfiles(UserHandle.myUserId()).size() == 1;
    }

    private boolean isMultiAppEnable(List<UserInfo> profiles) {
        for (UserInfo profile : profiles) {
            if (profile.id == 999) {
                return true;
            }
        }
        return false;
    }

    private RestrictedPreference newAddOneplusAccountPreference(Context context) {
        RestrictedPreference preference = new RestrictedPreference(context);
        preference.setKey(KEY_ONEPLUS_ACCOUNT);
        preference.setTitle((int) R.string.add_oneplus_account_label);
        preference.setIcon((int) R.drawable.op_ic_oneplus_account_icon);
        preference.setOrder(999);
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                if (OPUtils.isAppExist(AccountPreferenceController.this.mContext, "com.oneplus.account")) {
                    AccountManager accountManager = AccountManager.get(AccountPreferenceController.this.mContext);
                    Bundle bundle = new Bundle();
                    bundle.putString("come_from", "from_settings");
                    AccountManager accountManager2 = accountManager;
                    accountManager2.addAccount("com.oneplus.account", "", null, bundle, AccountPreferenceController.this.mParent.getActivity(), null, null);
                }
                return true;
            }
        });
        return preference;
    }
}
