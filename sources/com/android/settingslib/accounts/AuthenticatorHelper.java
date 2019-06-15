package com.android.settingslib.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class AuthenticatorHelper extends BroadcastReceiver {
    private static final String ONEPLUS_ACCOUNT_TYPE = "com.oneplus.account";
    private static final String TAG = "AuthenticatorHelper";
    private final Map<String, Drawable> mAccTypeIconCache = new HashMap();
    private final HashMap<String, ArrayList<String>> mAccountTypeToAuthorities = new HashMap();
    private final Context mContext;
    private final ArrayList<String> mEnabledAccountTypes = new ArrayList();
    private final OnAccountsUpdateListener mListener;
    private boolean mListeningToAccountUpdates;
    private final Map<String, AuthenticatorDescription> mTypeToAuthDescription = new HashMap();
    private final UserHandle mUserHandle;

    public interface OnAccountsUpdateListener {
        void onAccountsUpdate(UserHandle userHandle);
    }

    public AuthenticatorHelper(Context context, UserHandle userHandle, OnAccountsUpdateListener listener) {
        this.mContext = context;
        this.mUserHandle = userHandle;
        this.mListener = listener;
        onAccountsUpdated(null);
    }

    public String[] getEnabledAccountTypes() {
        return (String[]) this.mEnabledAccountTypes.toArray(new String[this.mEnabledAccountTypes.size()]);
    }

    public void preloadDrawableForType(final Context context, final String accountType) {
        new AsyncTask<Void, Void, Void>() {
            /* Access modifiers changed, original: protected|varargs */
            public Void doInBackground(Void... params) {
                AuthenticatorHelper.this.getDrawableForType(context, accountType);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    /* JADX WARNING: Missing block: B:10:0x001d, code skipped:
            if (r6.mTypeToAuthDescription.containsKey(r8) == false) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:12:?, code skipped:
            r1 = (android.accounts.AuthenticatorDescription) r6.mTypeToAuthDescription.get(r8);
            r0 = r6.mContext.getPackageManager().getUserBadgedIcon(r7.createPackageContextAsUser(r1.packageName, 0, r6.mUserHandle).getDrawable(r1.iconId), r6.mUserHandle);
            r3 = r6.mAccTypeIconCache;
     */
    /* JADX WARNING: Missing block: B:13:0x0045, code skipped:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:15:?, code skipped:
            r6.mAccTypeIconCache.put(r8, r0);
     */
    /* JADX WARNING: Missing block: B:16:0x004b, code skipped:
            monitor-exit(r3);
     */
    public android.graphics.drawable.Drawable getDrawableForType(android.content.Context r7, java.lang.String r8) {
        /*
        r6 = this;
        r0 = 0;
        r1 = r6.mAccTypeIconCache;
        monitor-enter(r1);
        r2 = r6.mAccTypeIconCache;	 Catch:{ all -> 0x005c }
        r2 = r2.containsKey(r8);	 Catch:{ all -> 0x005c }
        if (r2 == 0) goto L_0x0016;
    L_0x000c:
        r2 = r6.mAccTypeIconCache;	 Catch:{ all -> 0x005c }
        r2 = r2.get(r8);	 Catch:{ all -> 0x005c }
        r2 = (android.graphics.drawable.Drawable) r2;	 Catch:{ all -> 0x005c }
        monitor-exit(r1);	 Catch:{ all -> 0x005c }
        return r2;
    L_0x0016:
        monitor-exit(r1);	 Catch:{ all -> 0x005c }
        r1 = r6.mTypeToAuthDescription;
        r1 = r1.containsKey(r8);
        if (r1 == 0) goto L_0x0051;
    L_0x001f:
        r1 = r6.mTypeToAuthDescription;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r1 = r1.get(r8);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r1 = (android.accounts.AuthenticatorDescription) r1;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r2 = r1.packageName;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r3 = 0;
        r4 = r6.mUserHandle;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r2 = r7.createPackageContextAsUser(r2, r3, r4);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r3 = r6.mContext;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r3 = r3.getPackageManager();	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r4 = r1.iconId;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r4 = r2.getDrawable(r4);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r5 = r6.mUserHandle;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r3 = r3.getUserBadgedIcon(r4, r5);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r0 = r3;
        r3 = r6.mAccTypeIconCache;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        monitor-enter(r3);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
        r4 = r6.mAccTypeIconCache;	 Catch:{ all -> 0x004d }
        r4.put(r8, r0);	 Catch:{ all -> 0x004d }
        monitor-exit(r3);	 Catch:{ all -> 0x004d }
        goto L_0x0051;
    L_0x004d:
        r4 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x004d }
        throw r4;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0050, NameNotFoundException | NotFoundException -> 0x0050 }
    L_0x0050:
        r1 = move-exception;
    L_0x0051:
        if (r0 != 0) goto L_0x005b;
    L_0x0053:
        r1 = r7.getPackageManager();
        r0 = r1.getDefaultActivityIcon();
    L_0x005b:
        return r0;
    L_0x005c:
        r2 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x005c }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.accounts.AuthenticatorHelper.getDrawableForType(android.content.Context, java.lang.String):android.graphics.drawable.Drawable");
    }

    public CharSequence getLabelForType(Context context, String accountType) {
        String str;
        StringBuilder stringBuilder;
        if (!this.mTypeToAuthDescription.containsKey(accountType)) {
            return null;
        }
        try {
            AuthenticatorDescription desc = (AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType);
            return context.createPackageContextAsUser(desc.packageName, 0, this.mUserHandle).getResources().getText(desc.labelId);
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
            stringBuilder.append("No label icon for account type ");
            stringBuilder.append(accountType);
            Log.w(str, stringBuilder.toString());
            return null;
        }
    }

    public String getPackageForType(String accountType) {
        if (this.mTypeToAuthDescription.containsKey(accountType)) {
            return ((AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType)).packageName;
        }
        return null;
    }

    public int getLabelIdForType(String accountType) {
        if (this.mTypeToAuthDescription.containsKey(accountType)) {
            return ((AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType)).labelId;
        }
        return -1;
    }

    public void updateAuthDescriptions(Context context) {
        AuthenticatorDescription[] authDescs = AccountManager.get(context).getAuthenticatorTypesAsUser(this.mUserHandle.getIdentifier());
        for (int i = 0; i < authDescs.length; i++) {
            this.mTypeToAuthDescription.put(authDescs[i].type, authDescs[i]);
        }
    }

    public boolean containsAccountType(String accountType) {
        return this.mTypeToAuthDescription.containsKey(accountType);
    }

    public AuthenticatorDescription getAccountTypeDescription(String accountType) {
        return (AuthenticatorDescription) this.mTypeToAuthDescription.get(accountType);
    }

    public boolean hasAccountPreferences(String accountType) {
        if (containsAccountType(accountType)) {
            AuthenticatorDescription desc = getAccountTypeDescription(accountType);
            if (!(desc == null || desc.accountPreferencesId == 0)) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public void onAccountsUpdated(Account[] accounts) {
        updateAuthDescriptions(this.mContext);
        if (accounts == null) {
            accounts = AccountManager.get(this.mContext).getAccountsAsUser(this.mUserHandle.getIdentifier());
        }
        this.mEnabledAccountTypes.clear();
        this.mAccTypeIconCache.clear();
        for (Account account : accounts) {
            if (ONEPLUS_ACCOUNT_TYPE.equals(account.type)) {
                Log.v(TAG, "Ignore OnePlus account entry point");
            } else if (!this.mEnabledAccountTypes.contains(account.type)) {
                this.mEnabledAccountTypes.add(account.type);
            }
        }
        buildAccountTypeToAuthoritiesMap();
        if (this.mListeningToAccountUpdates) {
            this.mListener.onAccountsUpdate(this.mUserHandle);
        }
    }

    public void onReceive(Context context, Intent intent) {
        onAccountsUpdated(AccountManager.get(this.mContext).getAccountsAsUser(this.mUserHandle.getIdentifier()));
    }

    public void listenToAccountUpdates() {
        if (!this.mListeningToAccountUpdates) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.accounts.LOGIN_ACCOUNTS_CHANGED");
            intentFilter.addAction("android.intent.action.DEVICE_STORAGE_OK");
            this.mContext.registerReceiverAsUser(this, this.mUserHandle, intentFilter, null, null);
            this.mListeningToAccountUpdates = true;
        }
    }

    public void stopListeningToAccountUpdates() {
        if (this.mListeningToAccountUpdates) {
            this.mContext.unregisterReceiver(this);
            this.mListeningToAccountUpdates = false;
        }
    }

    public ArrayList<String> getAuthoritiesForAccountType(String type) {
        return (ArrayList) this.mAccountTypeToAuthorities.get(type);
    }

    private void buildAccountTypeToAuthoritiesMap() {
        this.mAccountTypeToAuthorities.clear();
        for (SyncAdapterType sa : ContentResolver.getSyncAdapterTypesAsUser(this.mUserHandle.getIdentifier())) {
            ArrayList<String> authorities = (ArrayList) this.mAccountTypeToAuthorities.get(sa.accountType);
            if (authorities == null) {
                authorities = new ArrayList();
                this.mAccountTypeToAuthorities.put(sa.accountType, authorities);
            }
            if (Log.isLoggable(TAG, 2)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Added authority ");
                stringBuilder.append(sa.authority);
                stringBuilder.append(" to accountType ");
                stringBuilder.append(sa.accountType);
                Log.v(str, stringBuilder.toString());
            }
            authorities.add(sa.authority);
        }
    }
}
