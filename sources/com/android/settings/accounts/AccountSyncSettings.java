package com.android.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.Utils;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccountSyncSettings extends AccountPreferenceBase {
    public static final String ACCOUNT_KEY = "account";
    private static final int CANT_DO_ONETIME_SYNC_DIALOG = 102;
    private static final int MENU_SYNC_CANCEL_ID = 2;
    private static final int MENU_SYNC_NOW_ID = 1;
    private Account mAccount;
    private TextView mErrorInfoView;
    private ArrayList<SyncAdapterType> mInvisibleAdapters = Lists.newArrayList();
    private ImageView mProviderIcon;
    private TextView mProviderId;
    private TextView mUserId;

    public /* bridge */ /* synthetic */ void updateAuthDescriptions() {
        super.updateAuthDescriptions();
    }

    public Dialog onCreateDialog(int id) {
        if (id == 102) {
            return new Builder(getActivity()).setTitle(R.string.cant_sync_dialog_title).setMessage(R.string.cant_sync_dialog_message).setPositiveButton(17039370, null).create();
        }
        return null;
    }

    public int getMetricsCategory() {
        return 9;
    }

    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId != 102) {
            return 0;
        }
        return 587;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.account_sync_settings);
        getPreferenceScreen().setOrderingAsAdded(false);
        setAccessibilityTitle();
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_sync_screen, container, false);
        ViewGroup prefs_container = (ViewGroup) view.findViewById(R.id.prefs_container);
        Utils.prepareCustomPreferencesList(container, view, prefs_container, false);
        prefs_container.addView(super.onCreateView(inflater, prefs_container, savedInstanceState));
        initializeUi(view);
        return view;
    }

    /* Access modifiers changed, original: protected */
    public void initializeUi(View rootView) {
        this.mErrorInfoView = (TextView) rootView.findViewById(R.id.sync_settings_error_info);
        this.mErrorInfoView.setVisibility(8);
        this.mUserId = (TextView) rootView.findViewById(R.id.user_id);
        this.mProviderId = (TextView) rootView.findViewById(R.id.provider_id);
        this.mProviderIcon = (ImageView) rootView.findViewById(R.id.provider_icon);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) {
            Log.e("AccountPreferenceBase", "No arguments provided when starting intent. ACCOUNT_KEY needed.");
            finish();
            return;
        }
        this.mAccount = (Account) arguments.getParcelable("account");
        StringBuilder stringBuilder;
        if (accountExists(this.mAccount)) {
            if (Log.isLoggable("AccountPreferenceBase", 2)) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Got account: ");
                stringBuilder.append(this.mAccount);
                Log.v("AccountPreferenceBase", stringBuilder.toString());
            }
            this.mUserId.setText(this.mAccount.name);
            this.mProviderId.setText(this.mAccount.type);
            return;
        }
        stringBuilder = new StringBuilder();
        stringBuilder.append("Account provided does not exist: ");
        stringBuilder.append(this.mAccount);
        Log.e("AccountPreferenceBase", stringBuilder.toString());
        finish();
    }

    private void setAccessibilityTitle() {
        int i;
        UserInfo user = ((UserManager) getSystemService("user")).getUserInfo(this.mUserHandle.getIdentifier());
        boolean isWorkProfile = user != null ? user.isManagedProfile() : false;
        CharSequence currentTitle = getActivity().getTitle();
        if (isWorkProfile) {
            i = R.string.accessibility_work_account_title;
        } else {
            i = R.string.accessibility_personal_account_title;
        }
        getActivity().setTitle(Utils.createAccessibleSequence(currentTitle, getString(i, new Object[]{currentTitle})));
    }

    public void onResume() {
        removePreference("dummy");
        this.mAuthenticatorHelper.listenToAccountUpdates();
        updateAuthDescriptions();
        onAccountsUpdate(Binder.getCallingUserHandle());
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        this.mAuthenticatorHelper.stopListeningToAccountUpdates();
    }

    private void addSyncStateSwitch(Account account, String authority, String packageName, int uid) {
        SyncStateSwitchPreference item = (SyncStateSwitchPreference) getCachedPreference(authority);
        if (item == null) {
            item = new SyncStateSwitchPreference(getPrefContext(), account, authority, packageName, uid);
            getPreferenceScreen().addPreference(item);
        } else {
            item.setup(account, authority, packageName, uid);
        }
        PackageManager packageManager = getPackageManager();
        item.setPersistent(false);
        ProviderInfo providerInfo = packageManager.resolveContentProviderAsUser(authority, 0, this.mUserHandle.getIdentifier());
        if (providerInfo != null) {
            CharSequence providerLabel = providerInfo.loadLabel(packageManager);
            if (TextUtils.isEmpty(providerLabel)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Provider needs a label for authority '");
                stringBuilder.append(authority);
                stringBuilder.append("'");
                Log.e("AccountPreferenceBase", stringBuilder.toString());
                return;
            }
            item.setTitle(providerLabel);
            item.setKey(authority);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem syncNow = menu.add(0, 1, 0, getString(R.string.sync_menu_sync_now)).setIcon(R.drawable.ic_menu_refresh_holo_dark);
        MenuItem syncCancel = menu.add(0, 2, 0, getString(R.string.sync_menu_sync_cancel)).setIcon(17301560);
        syncNow.setShowAsAction(4);
        syncCancel.setShowAsAction(4);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean z = true;
        boolean syncActive = ContentResolver.getCurrentSyncsAsUser(this.mUserHandle.getIdentifier()).isEmpty() ^ true;
        MenuItem findItem = menu.findItem(1);
        if (syncActive) {
            z = false;
        }
        findItem.setVisible(z);
        menu.findItem(2).setVisible(syncActive);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                startSyncForEnabledProviders();
                return true;
            case 2:
                cancelSyncForEnabledProviders();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            int uid = requestCode;
            int count = getPreferenceScreen().getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof SyncStateSwitchPreference) {
                    SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) preference;
                    if (syncPref.getUid() == uid) {
                        onPreferenceTreeClick(syncPref);
                        return;
                    }
                }
            }
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (getActivity() == null) {
            return false;
        }
        if (!(preference instanceof SyncStateSwitchPreference)) {
            return super.onPreferenceTreeClick(preference);
        }
        SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) preference;
        String authority = syncPref.getAuthority();
        Account account = syncPref.getAccount();
        int userId = this.mUserHandle.getIdentifier();
        String packageName = syncPref.getPackageName();
        boolean syncAutomatically = ContentResolver.getSyncAutomaticallyAsUser(account, authority, userId);
        if (!syncPref.isOneTimeSyncMode()) {
            boolean syncOn = syncPref.isChecked();
            if (syncOn == syncAutomatically || (syncOn && requestAccountAccessIfNeeded(packageName))) {
                return true;
            }
            ContentResolver.setSyncAutomaticallyAsUser(account, authority, syncOn, userId);
            if (!(ContentResolver.getMasterSyncAutomaticallyAsUser(userId) && syncOn)) {
                requestOrCancelSync(account, authority, syncOn);
            }
        } else if (requestAccountAccessIfNeeded(packageName)) {
            return true;
        } else {
            requestOrCancelSync(account, authority, true);
        }
        return true;
    }

    private boolean requestAccountAccessIfNeeded(String packageName) {
        if (packageName == null) {
            return false;
        }
        try {
            int uid = getContext().getPackageManager().getPackageUidAsUser(packageName, this.mUserHandle.getIdentifier());
            AccountManager accountManager = (AccountManager) getContext().getSystemService(AccountManager.class);
            if (!accountManager.hasAccountAccess(this.mAccount, packageName, this.mUserHandle)) {
                IntentSender intent = accountManager.createRequestAccountAccessIntentSenderAsUser(this.mAccount, packageName, this.mUserHandle);
                if (intent != null) {
                    try {
                        startIntentSenderForResult(intent, uid, null, 0, 0, 0, null);
                        return true;
                    } catch (SendIntentException e) {
                        Log.e("AccountPreferenceBase", "Error requesting account access", e);
                    }
                }
            }
            return false;
        } catch (NameNotFoundException e2) {
            Log.e("AccountPreferenceBase", "Invalid sync ", e2);
            return false;
        }
    }

    private void startSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(true);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void cancelSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(false);
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void requestOrCancelSyncForEnabledProviders(boolean startSync) {
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof SyncStateSwitchPreference) {
                SyncStateSwitchPreference syncPref = (SyncStateSwitchPreference) pref;
                if (syncPref.isChecked()) {
                    requestOrCancelSync(syncPref.getAccount(), syncPref.getAuthority(), startSync);
                }
            }
        }
        if (this.mAccount != null) {
            Iterator it = this.mInvisibleAdapters.iterator();
            while (it.hasNext()) {
                requestOrCancelSync(this.mAccount, ((SyncAdapterType) it.next()).authority, startSync);
            }
        }
    }

    private void requestOrCancelSync(Account account, String authority, boolean flag) {
        if (flag) {
            Bundle extras = new Bundle();
            extras.putBoolean("force", true);
            ContentResolver.requestSyncAsUser(account, authority, this.mUserHandle.getIdentifier(), extras);
            return;
        }
        ContentResolver.cancelSyncAsUser(account, authority, this.mUserHandle.getIdentifier());
    }

    private boolean isSyncing(List<SyncInfo> currentSyncs, Account account, String authority) {
        for (SyncInfo syncInfo : currentSyncs) {
            if (syncInfo.account.equals(account) && syncInfo.authority.equals(authority)) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void onSyncStateUpdated() {
        if (isResumed()) {
            setFeedsState();
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00bd  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d8  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0115 A:{SKIP} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0121 A:{SKIP} */
    private void setFeedsState() {
        /*
        r26 = this;
        r0 = r26;
        r1 = new java.util.Date;
        r1.<init>();
        r2 = r0.mUserHandle;
        r2 = r2.getIdentifier();
        r3 = android.content.ContentResolver.getCurrentSyncsAsUser(r2);
        r4 = 0;
        r26.updateAccountSwitches();
        r5 = 0;
        r6 = r26.getPreferenceScreen();
        r6 = r6.getPreferenceCount();
    L_0x001e:
        if (r5 >= r6) goto L_0x014d;
    L_0x0020:
        r8 = r26.getPreferenceScreen();
        r8 = r8.getPreference(r5);
        r9 = r8 instanceof com.android.settings.accounts.SyncStateSwitchPreference;
        if (r9 != 0) goto L_0x0037;
        r23 = r1;
        r18 = r3;
        r22 = r5;
        r20 = r6;
        goto L_0x0143;
    L_0x0037:
        r9 = r8;
        r9 = (com.android.settings.accounts.SyncStateSwitchPreference) r9;
        r10 = r9.getAuthority();
        r11 = r9.getAccount();
        r12 = android.content.ContentResolver.getSyncStatusAsUser(r11, r10, r2);
        r13 = android.content.ContentResolver.getSyncAutomaticallyAsUser(r11, r10, r2);
        if (r12 != 0) goto L_0x004e;
    L_0x004c:
        r14 = 0;
        goto L_0x0050;
    L_0x004e:
        r14 = r12.pending;
    L_0x0050:
        if (r12 != 0) goto L_0x0054;
    L_0x0052:
        r15 = 0;
        goto L_0x0056;
    L_0x0054:
        r15 = r12.initialize;
    L_0x0056:
        r7 = r0.isSyncing(r3, r11, r10);
        r16 = 0;
        r18 = r3;
        if (r12 == 0) goto L_0x0072;
    L_0x0060:
        r19 = r4;
        r3 = r12.lastFailureTime;
        r3 = (r3 > r16 ? 1 : (r3 == r16 ? 0 : -1));
        if (r3 == 0) goto L_0x0074;
    L_0x0068:
        r3 = 0;
        r4 = r12.getLastFailureMesgAsInt(r3);
        r3 = 1;
        if (r4 == r3) goto L_0x0074;
    L_0x0070:
        r3 = 1;
        goto L_0x0075;
    L_0x0072:
        r19 = r4;
    L_0x0074:
        r3 = 0;
    L_0x0075:
        if (r13 != 0) goto L_0x0078;
    L_0x0077:
        r3 = 0;
    L_0x0078:
        if (r3 == 0) goto L_0x0081;
    L_0x007a:
        if (r7 != 0) goto L_0x0081;
    L_0x007c:
        if (r14 != 0) goto L_0x0081;
    L_0x007e:
        r4 = 1;
        r19 = r4;
    L_0x0081:
        r4 = "AccountPreferenceBase";
        r20 = r6;
        r6 = 3;
        r4 = android.util.Log.isLoggable(r4, r6);
        if (r4 == 0) goto L_0x00bd;
    L_0x008c:
        r4 = "AccountPreferenceBase";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r21 = r8;
        r8 = "Update sync status: ";
        r6.append(r8);
        r6.append(r11);
        r8 = " ";
        r6.append(r8);
        r6.append(r10);
        r8 = " active = ";
        r6.append(r8);
        r6.append(r7);
        r8 = " pend =";
        r6.append(r8);
        r6.append(r14);
        r6 = r6.toString();
        android.util.Log.d(r4, r6);
        goto L_0x00bf;
    L_0x00bd:
        r21 = r8;
    L_0x00bf:
        if (r12 != 0) goto L_0x00c6;
    L_0x00c1:
        r22 = r5;
        r4 = r16;
        goto L_0x00ca;
    L_0x00c6:
        r22 = r5;
        r4 = r12.lastSuccessTime;
    L_0x00ca:
        if (r13 != 0) goto L_0x00d8;
    L_0x00cc:
        r6 = 2131890783; // 0x7f12125f float:1.9416268E38 double:1.0532939966E-314;
        r9.setSummary(r6);
    L_0x00d2:
        r23 = r1;
        r24 = r4;
        r1 = 0;
        goto L_0x010f;
    L_0x00d8:
        if (r7 == 0) goto L_0x00e1;
    L_0x00da:
        r6 = 2131890788; // 0x7f121264 float:1.9416278E38 double:1.053293999E-314;
        r9.setSummary(r6);
        goto L_0x00d2;
    L_0x00e1:
        r6 = (r4 > r16 ? 1 : (r4 == r16 ? 0 : -1));
        if (r6 == 0) goto L_0x0105;
    L_0x00e5:
        r1.setTime(r4);
        r6 = r0.formatSyncDate(r1);
        r8 = r26.getResources();
        r23 = r1;
        r24 = r4;
        r1 = 1;
        r4 = new java.lang.Object[r1];
        r1 = 0;
        r4[r1] = r6;
        r5 = 2131888107; // 0x7f1207eb float:1.941084E38 double:1.0532926744E-314;
        r4 = r8.getString(r5, r4);
        r9.setSummary(r4);
        goto L_0x010f;
    L_0x0105:
        r23 = r1;
        r24 = r4;
        r1 = 0;
        r4 = "";
        r9.setSummary(r4);
    L_0x010f:
        r4 = android.content.ContentResolver.getIsSyncableAsUser(r11, r10, r2);
        if (r7 == 0) goto L_0x011b;
    L_0x0115:
        if (r4 < 0) goto L_0x011b;
    L_0x0117:
        if (r15 != 0) goto L_0x011b;
    L_0x0119:
        r5 = 1;
        goto L_0x011c;
    L_0x011b:
        r5 = r1;
    L_0x011c:
        r9.setActive(r5);
        if (r14 == 0) goto L_0x0127;
    L_0x0121:
        if (r4 < 0) goto L_0x0127;
    L_0x0123:
        if (r15 != 0) goto L_0x0127;
    L_0x0125:
        r5 = 1;
        goto L_0x0128;
    L_0x0127:
        r5 = r1;
    L_0x0128:
        r9.setPending(r5);
        r9.setFailed(r3);
        r5 = android.content.ContentResolver.getMasterSyncAutomaticallyAsUser(r2);
        r6 = 1;
        r5 = r5 ^ r6;
        r9.setOneTimeSyncMode(r5);
        if (r5 != 0) goto L_0x013d;
    L_0x0139:
        if (r13 == 0) goto L_0x013c;
    L_0x013b:
        goto L_0x013d;
    L_0x013c:
        goto L_0x013e;
    L_0x013d:
        r1 = r6;
    L_0x013e:
        r9.setChecked(r1);
        r4 = r19;
    L_0x0143:
        r5 = r22 + 1;
        r3 = r18;
        r6 = r20;
        r1 = r23;
        goto L_0x001e;
    L_0x014d:
        r23 = r1;
        r18 = r3;
        r19 = r4;
        r1 = 0;
        r3 = r0.mErrorInfoView;
        if (r19 == 0) goto L_0x0159;
    L_0x0158:
        goto L_0x015c;
    L_0x0159:
        r7 = 8;
        r1 = r7;
    L_0x015c:
        r3.setVisibility(r1);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.accounts.AccountSyncSettings.setFeedsState():void");
    }

    public void onAccountsUpdate(UserHandle userHandle) {
        super.onAccountsUpdate(userHandle);
        if (accountExists(this.mAccount)) {
            updateAccountSwitches();
            onSyncStateUpdated();
            return;
        }
        finish();
    }

    private boolean accountExists(Account account) {
        if (account == null) {
            return false;
        }
        for (Account equals : AccountManager.get(getActivity()).getAccountsByTypeAsUser(account.type, this.mUserHandle)) {
            if (equals.equals(account)) {
                return true;
            }
        }
        return false;
    }

    private void updateAccountSwitches() {
        int i;
        SyncAdapterType sa;
        this.mInvisibleAdapters.clear();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypesAsUser(this.mUserHandle.getIdentifier());
        ArrayList<SyncAdapterType> authorities = new ArrayList();
        for (SyncAdapterType sa2 : syncAdapters) {
            if (sa2.accountType.equals(this.mAccount.type)) {
                if (sa2.isUserVisible()) {
                    if (Log.isLoggable("AccountPreferenceBase", 3)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("updateAccountSwitches: added authority ");
                        stringBuilder.append(sa2.authority);
                        stringBuilder.append(" to accountType ");
                        stringBuilder.append(sa2.accountType);
                        Log.d("AccountPreferenceBase", stringBuilder.toString());
                    }
                    authorities.add(sa2);
                } else {
                    this.mInvisibleAdapters.add(sa2);
                }
            }
        }
        if (Log.isLoggable("AccountPreferenceBase", 3)) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("looking for sync adapters that match account ");
            stringBuilder2.append(this.mAccount);
            Log.d("AccountPreferenceBase", stringBuilder2.toString());
        }
        cacheRemoveAllPrefs(getPreferenceScreen());
        int n = authorities.size();
        for (i = 0; i < n; i++) {
            sa2 = (SyncAdapterType) authorities.get(i);
            int syncState = ContentResolver.getIsSyncableAsUser(this.mAccount, sa2.authority, this.mUserHandle.getIdentifier());
            if (Log.isLoggable("AccountPreferenceBase", 3)) {
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("  found authority ");
                stringBuilder3.append(sa2.authority);
                stringBuilder3.append(" ");
                stringBuilder3.append(syncState);
                Log.d("AccountPreferenceBase", stringBuilder3.toString());
            }
            if (syncState > 0) {
                try {
                    addSyncStateSwitch(this.mAccount, sa2.authority, sa2.getPackageName(), getContext().getPackageManager().getPackageUidAsUser(sa2.getPackageName(), this.mUserHandle.getIdentifier()));
                } catch (NameNotFoundException e) {
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("No uid for package");
                    stringBuilder4.append(sa2.getPackageName());
                    Log.e("AccountPreferenceBase", stringBuilder4.toString(), e);
                }
            }
        }
        removeCachedPrefs(getPreferenceScreen());
    }

    /* Access modifiers changed, original: protected */
    public void onAuthDescriptionsUpdated() {
        super.onAuthDescriptionsUpdated();
        if (this.mAccount != null) {
            this.mProviderIcon.setImageDrawable(getDrawableForType(this.mAccount.type));
            this.mProviderId.setText(getLabelForType(this.mAccount.type));
        }
    }

    public int getHelpResource() {
        return R.string.help_url_accounts;
    }
}
