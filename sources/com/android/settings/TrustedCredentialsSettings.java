package com.android.settings;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.net.http.SslCertificate;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.IKeyChainService;
import android.security.KeyChain;
import android.security.KeyChain.KeyChainConnection;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.TrustedCredentialsDialogBuilder.DelegateInterface;
import com.android.settings.core.InstrumentedFragment;
import com.oneplus.settings.utils.OPUtils;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntConsumer;

public class TrustedCredentialsSettings extends InstrumentedFragment implements DelegateInterface {
    public static final String ARG_SHOW_NEW_FOR_USER = "ARG_SHOW_NEW_FOR_USER";
    private static final int REQUEST_CONFIRM_CREDENTIALS = 1;
    private static final String SAVED_CONFIRMED_CREDENTIAL_USERS = "ConfirmedCredentialUsers";
    private static final String SAVED_CONFIRMING_CREDENTIAL_USER = "ConfirmingCredentialUser";
    private static final String TAG = "TrustedCredentialsSettings";
    private static final String USER_ACTION = "com.android.settings.TRUSTED_CREDENTIALS_USER";
    private Set<AliasLoader> mAliasLoaders = new ArraySet(2);
    private AliasOperation mAliasOperation;
    private ArraySet<Integer> mConfirmedCredentialUsers;
    private IntConsumer mConfirmingCredentialListener;
    private int mConfirmingCredentialUser;
    private ArrayList<GroupAdapter> mGroupAdapters = new ArrayList(2);
    @GuardedBy("mKeyChainConnectionByProfileId")
    private final SparseArray<KeyChainConnection> mKeyChainConnectionByProfileId = new SparseArray();
    private KeyguardManager mKeyguardManager;
    private TabHost mTabHost;
    private int mTrustAllCaUserId;
    private UserManager mUserManager;
    private BroadcastReceiver mWorkProfileChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.MANAGED_PROFILE_AVAILABLE".equals(action) || "android.intent.action.MANAGED_PROFILE_UNAVAILABLE".equals(action) || "android.intent.action.MANAGED_PROFILE_UNLOCKED".equals(action)) {
                Iterator it = TrustedCredentialsSettings.this.mGroupAdapters.iterator();
                while (it.hasNext()) {
                    ((GroupAdapter) it.next()).load();
                }
            }
        }
    };

    private class AdapterData {
        private final GroupAdapter mAdapter;
        private final SparseArray<List<CertHolder>> mCertHoldersByUserId;
        private final Tab mTab;

        private class AliasLoader extends AsyncTask<Void, Integer, SparseArray<List<CertHolder>>> {
            private View mContentView;
            private Context mContext;
            private ProgressBar mProgressBar;

            public AliasLoader() {
                this.mContext = TrustedCredentialsSettings.this.getActivity();
                TrustedCredentialsSettings.this.mAliasLoaders.add(this);
                for (UserHandle profile : TrustedCredentialsSettings.this.mUserManager.getUserProfiles()) {
                    AdapterData.this.mCertHoldersByUserId.put(profile.getIdentifier(), new ArrayList());
                }
            }

            private boolean shouldSkipProfile(UserHandle userHandle) {
                return TrustedCredentialsSettings.this.mUserManager.isQuietModeEnabled(userHandle) || !TrustedCredentialsSettings.this.mUserManager.isUserUnlocked(userHandle.getIdentifier());
            }

            /* Access modifiers changed, original: protected */
            public void onPreExecute() {
                View content = TrustedCredentialsSettings.this.mTabHost.getTabContentView();
                this.mProgressBar = (ProgressBar) content.findViewById(AdapterData.this.mTab.mProgress);
                this.mContentView = content.findViewById(AdapterData.this.mTab.mContentView);
                this.mProgressBar.setVisibility(0);
                this.mContentView.setVisibility(8);
            }

            /* Access modifiers changed, original: protected|varargs */
            public SparseArray<List<CertHolder>> doInBackground(Void... params) {
                SparseArray<List<CertHolder>> certHoldersByProfile = new SparseArray();
                try {
                    synchronized (TrustedCredentialsSettings.this.mKeyChainConnectionByProfileId) {
                        int i;
                        UserHandle profile;
                        int profileId;
                        SparseArray sparseArray;
                        int n;
                        SparseArray<List<String>> aliasesByProfileId;
                        List<UserHandle> profiles = TrustedCredentialsSettings.this.mUserManager.getUserProfiles();
                        int n2 = profiles.size();
                        SparseArray<List<String>> aliasesByProfileId2 = new SparseArray(n2);
                        int progress = 0;
                        int max = 0;
                        for (i = 0; i < n2; i++) {
                            profile = (UserHandle) profiles.get(i);
                            profileId = profile.getIdentifier();
                            if (!shouldSkipProfile(profile)) {
                                KeyChainConnection keyChainConnection = KeyChain.bindAsUser(this.mContext, profile);
                                TrustedCredentialsSettings.this.mKeyChainConnectionByProfileId.put(profileId, keyChainConnection);
                                List<String> aliases = AdapterData.this.mTab.getAliases(keyChainConnection.getService());
                                if (isCancelled()) {
                                    sparseArray = new SparseArray();
                                    return sparseArray;
                                }
                                max += aliases.size();
                                aliasesByProfileId2.put(profileId, aliases);
                            }
                        }
                        i = 0;
                        while (i < n2) {
                            profile = (UserHandle) profiles.get(i);
                            profileId = profile.getIdentifier();
                            List<String> aliases2 = (List) aliasesByProfileId2.get(profileId);
                            if (isCancelled()) {
                                sparseArray = new SparseArray();
                                return sparseArray;
                            }
                            List<UserHandle> profiles2;
                            KeyChainConnection keyChainConnection2 = (KeyChainConnection) TrustedCredentialsSettings.this.mKeyChainConnectionByProfileId.get(profileId);
                            UserHandle userHandle;
                            KeyChainConnection keyChainConnection3;
                            List<String> aliasesByProfileId3;
                            if (shouldSkipProfile(profile) || aliases2 == null) {
                                profiles2 = profiles;
                                n = n2;
                                aliasesByProfileId = aliasesByProfileId2;
                                userHandle = profile;
                                keyChainConnection3 = keyChainConnection2;
                            } else if (keyChainConnection2 == null) {
                                profiles2 = profiles;
                                n = n2;
                                aliasesByProfileId = aliasesByProfileId2;
                                userHandle = profile;
                                keyChainConnection3 = keyChainConnection2;
                                aliasesByProfileId3 = aliases2;
                            } else {
                                int aliasMax;
                                int aliasMax2;
                                List<CertHolder> certHolders;
                                IKeyChainService service;
                                IKeyChainService service2 = keyChainConnection2.getService();
                                List<CertHolder> certHolders2 = new ArrayList(max);
                                int aliasMax3 = aliases2.size();
                                int progress2 = progress;
                                progress = 0;
                                while (true) {
                                    aliasMax = aliasMax3;
                                    if (progress >= aliasMax) {
                                        break;
                                    }
                                    profiles2 = profiles;
                                    n = n2;
                                    String alias = (String) aliases2.get(progress);
                                    profiles = service2.getEncodedCaCertificate(alias, true);
                                    X509Certificate cert = KeyChain.toCertificate(profiles);
                                    byte[] encodedCertificate = profiles;
                                    aliasesByProfileId = aliasesByProfileId2;
                                    aliasMax2 = aliasMax;
                                    userHandle = profile;
                                    certHolders = certHolders2;
                                    service = service2;
                                    keyChainConnection3 = keyChainConnection2;
                                    GroupAdapter access$2800 = AdapterData.this.mAdapter;
                                    aliasesByProfileId3 = aliases2;
                                    certHolders.add(new CertHolder(service2, access$2800, AdapterData.this.mTab, alias, cert, profileId, null));
                                    profiles = new Integer[2];
                                    aliasMax = progress2 + 1;
                                    profiles[0] = Integer.valueOf(aliasMax);
                                    profiles[1] = Integer.valueOf(max);
                                    publishProgress(profiles);
                                    progress++;
                                    aliases2 = aliasesByProfileId3;
                                    progress2 = aliasMax;
                                    certHolders2 = certHolders;
                                    service2 = service;
                                    keyChainConnection2 = keyChainConnection3;
                                    profiles = profiles2;
                                    n2 = n;
                                    aliasesByProfileId2 = aliasesByProfileId;
                                    aliasMax3 = aliasMax2;
                                    profile = userHandle;
                                }
                                profiles2 = profiles;
                                n = n2;
                                aliasesByProfileId = aliasesByProfileId2;
                                aliasMax2 = aliasMax;
                                userHandle = profile;
                                certHolders = certHolders2;
                                service = service2;
                                keyChainConnection3 = keyChainConnection2;
                                aliasesByProfileId3 = aliases2;
                                Collections.sort(certHolders);
                                certHoldersByProfile.put(profileId, certHolders);
                                progress = progress2;
                                i++;
                                profiles = profiles2;
                                n2 = n;
                                aliasesByProfileId2 = aliasesByProfileId;
                            }
                            certHoldersByProfile.put(profileId, new ArrayList(0));
                            i++;
                            profiles = profiles2;
                            n2 = n;
                            aliasesByProfileId2 = aliasesByProfileId;
                        }
                        n = n2;
                        aliasesByProfileId = aliasesByProfileId2;
                        return certHoldersByProfile;
                    }
                } catch (RemoteException e) {
                    Log.e(TrustedCredentialsSettings.TAG, "Remote exception while loading aliases.", e);
                    return new SparseArray();
                } catch (InterruptedException e2) {
                    Log.e(TrustedCredentialsSettings.TAG, "InterruptedException while loading aliases.", e2);
                    return new SparseArray();
                }
            }

            /* Access modifiers changed, original: protected|varargs */
            public void onProgressUpdate(Integer... progressAndMax) {
                int progress = progressAndMax[0].intValue();
                int max = progressAndMax[1].intValue();
                if (max != this.mProgressBar.getMax()) {
                    this.mProgressBar.setMax(max);
                }
                this.mProgressBar.setProgress(progress);
            }

            /* Access modifiers changed, original: protected */
            public void onPostExecute(SparseArray<List<CertHolder>> certHolders) {
                AdapterData.this.mCertHoldersByUserId.clear();
                int n = certHolders.size();
                for (int i = 0; i < n; i++) {
                    AdapterData.this.mCertHoldersByUserId.put(certHolders.keyAt(i), (List) certHolders.valueAt(i));
                }
                AdapterData.this.mAdapter.notifyDataSetChanged();
                this.mProgressBar.setVisibility(8);
                this.mContentView.setVisibility(0);
                this.mProgressBar.setProgress(0);
                TrustedCredentialsSettings.this.mAliasLoaders.remove(this);
                showTrustAllCaDialogIfNeeded();
            }

            private boolean isUserTabAndTrustAllCertMode() {
                return TrustedCredentialsSettings.this.isTrustAllCaCertModeInProgress() && AdapterData.this.mTab == Tab.USER;
            }

            private void showTrustAllCaDialogIfNeeded() {
                if (isUserTabAndTrustAllCertMode()) {
                    List<CertHolder> certHolders = (List) AdapterData.this.mCertHoldersByUserId.get(TrustedCredentialsSettings.this.mTrustAllCaUserId);
                    if (certHolders != null) {
                        List<CertHolder> unapprovedUserCertHolders = new ArrayList();
                        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService(DevicePolicyManager.class);
                        for (CertHolder cert : certHolders) {
                            if (!(cert == null || dpm.isCaCertApproved(cert.mAlias, TrustedCredentialsSettings.this.mTrustAllCaUserId))) {
                                unapprovedUserCertHolders.add(cert);
                            }
                        }
                        if (unapprovedUserCertHolders.size() == 0) {
                            String str = TrustedCredentialsSettings.TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("no cert is pending approval for user ");
                            stringBuilder.append(TrustedCredentialsSettings.this.mTrustAllCaUserId);
                            Log.w(str, stringBuilder.toString());
                            return;
                        }
                        TrustedCredentialsSettings.this.showTrustAllCaDialog(unapprovedUserCertHolders);
                    }
                }
            }
        }

        /* synthetic */ AdapterData(TrustedCredentialsSettings x0, Tab x1, GroupAdapter x2, AnonymousClass1 x3) {
            this(x1, x2);
        }

        private AdapterData(Tab tab, GroupAdapter adapter) {
            this.mCertHoldersByUserId = new SparseArray();
            this.mAdapter = adapter;
            this.mTab = tab;
        }

        public void remove(CertHolder certHolder) {
            if (this.mCertHoldersByUserId != null) {
                List<CertHolder> certs = (List) this.mCertHoldersByUserId.get(certHolder.mProfileId);
                if (certs != null) {
                    certs.remove(certHolder);
                }
            }
        }
    }

    private class AliasOperation extends AsyncTask<Void, Void, Boolean> {
        private final CertHolder mCertHolder;

        /* synthetic */ AliasOperation(TrustedCredentialsSettings x0, CertHolder x1, AnonymousClass1 x2) {
            this(x1);
        }

        private AliasOperation(CertHolder certHolder) {
            this.mCertHolder = certHolder;
            TrustedCredentialsSettings.this.mAliasOperation = this;
        }

        /* Access modifiers changed, original: protected|varargs */
        public Boolean doInBackground(Void... params) {
            try {
                synchronized (TrustedCredentialsSettings.this.mKeyChainConnectionByProfileId) {
                    IKeyChainService service = ((KeyChainConnection) TrustedCredentialsSettings.this.mKeyChainConnectionByProfileId.get(this.mCertHolder.mProfileId)).getService();
                    if (this.mCertHolder.mDeleted) {
                        service.installCaCertificate(this.mCertHolder.mX509Cert.getEncoded());
                        Boolean valueOf = Boolean.valueOf(true);
                        return valueOf;
                    }
                    Boolean valueOf2 = Boolean.valueOf(service.deleteCaCertificate(this.mCertHolder.mAlias));
                    return valueOf2;
                }
            } catch (RemoteException | IllegalStateException | SecurityException | CertificateEncodingException e) {
                String str = TrustedCredentialsSettings.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Error while toggling alias ");
                stringBuilder.append(this.mCertHolder.mAlias);
                Log.w(str, stringBuilder.toString(), e);
                return Boolean.valueOf(false);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Boolean ok) {
            if (ok.booleanValue()) {
                if (this.mCertHolder.mTab.mSwitch) {
                    this.mCertHolder.mDeleted = this.mCertHolder.mDeleted ^ 1;
                } else {
                    this.mCertHolder.mAdapter.remove(this.mCertHolder);
                }
                this.mCertHolder.mAdapter.notifyDataSetChanged();
            } else {
                this.mCertHolder.mAdapter.load();
            }
            TrustedCredentialsSettings.this.mAliasOperation = null;
        }
    }

    static class CertHolder implements Comparable<CertHolder> {
        private final GroupAdapter mAdapter;
        private final String mAlias;
        private boolean mDeleted;
        public int mProfileId;
        private final IKeyChainService mService;
        private final SslCertificate mSslCert;
        private final String mSubjectPrimary;
        private final String mSubjectSecondary;
        private final Tab mTab;
        private final X509Certificate mX509Cert;

        /* synthetic */ CertHolder(IKeyChainService x0, GroupAdapter x1, Tab x2, String x3, X509Certificate x4, int x5, AnonymousClass1 x6) {
            this(x0, x1, x2, x3, x4, x5);
        }

        private CertHolder(IKeyChainService service, GroupAdapter adapter, Tab tab, String alias, X509Certificate x509Cert, int profileId) {
            this.mProfileId = profileId;
            this.mService = service;
            this.mAdapter = adapter;
            this.mTab = tab;
            this.mAlias = alias;
            this.mX509Cert = x509Cert;
            this.mSslCert = new SslCertificate(x509Cert);
            String cn = this.mSslCert.getIssuedTo().getCName();
            String o = this.mSslCert.getIssuedTo().getOName();
            String ou = this.mSslCert.getIssuedTo().getUName();
            if (o.isEmpty()) {
                if (cn.isEmpty()) {
                    this.mSubjectPrimary = this.mSslCert.getIssuedTo().getDName();
                    this.mSubjectSecondary = "";
                } else {
                    this.mSubjectPrimary = cn;
                    this.mSubjectSecondary = "";
                }
            } else if (cn.isEmpty()) {
                this.mSubjectPrimary = o;
                this.mSubjectSecondary = ou;
            } else {
                this.mSubjectPrimary = o;
                this.mSubjectSecondary = cn;
            }
            try {
                this.mDeleted = this.mTab.deleted(this.mService, this.mAlias);
            } catch (RemoteException e) {
                String str = TrustedCredentialsSettings.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Remote exception while checking if alias ");
                stringBuilder.append(this.mAlias);
                stringBuilder.append(" is deleted.");
                Log.e(str, stringBuilder.toString(), e);
                this.mDeleted = false;
            }
        }

        public int compareTo(CertHolder o) {
            int primary = this.mSubjectPrimary.compareToIgnoreCase(o.mSubjectPrimary);
            if (primary != 0) {
                return primary;
            }
            return this.mSubjectSecondary.compareToIgnoreCase(o.mSubjectSecondary);
        }

        public boolean equals(Object o) {
            if (!(o instanceof CertHolder)) {
                return false;
            }
            return this.mAlias.equals(((CertHolder) o).mAlias);
        }

        public int hashCode() {
            return this.mAlias.hashCode();
        }

        public int getUserId() {
            return this.mProfileId;
        }

        public String getAlias() {
            return this.mAlias;
        }

        public boolean isSystemCert() {
            return this.mTab == Tab.SYSTEM;
        }

        public boolean isDeleted() {
            return this.mDeleted;
        }
    }

    private class ChildAdapter extends BaseAdapter implements OnClickListener, OnItemClickListener {
        private final int[] EMPTY_STATE_SET;
        private final int[] GROUP_EXPANDED_STATE_SET;
        private final LayoutParams HIDE_CONTAINER_LAYOUT_PARAMS;
        private final LayoutParams HIDE_LIST_LAYOUT_PARAMS;
        private final LayoutParams SHOW_LAYOUT_PARAMS;
        private LinearLayout mContainerView;
        private final int mGroupPosition;
        private ViewGroup mHeaderView;
        private ImageView mIndicatorView;
        private boolean mIsListExpanded;
        private ListView mListView;
        private final DataSetObserver mObserver;
        private final GroupAdapter mParent;

        /* synthetic */ ChildAdapter(TrustedCredentialsSettings x0, GroupAdapter x1, int x2, AnonymousClass1 x3) {
            this(x1, x2);
        }

        private ChildAdapter(GroupAdapter parent, int groupPosition) {
            this.GROUP_EXPANDED_STATE_SET = new int[]{16842920};
            this.EMPTY_STATE_SET = new int[0];
            this.HIDE_CONTAINER_LAYOUT_PARAMS = new LayoutParams(-1, -2, 0.0f);
            this.HIDE_LIST_LAYOUT_PARAMS = new LayoutParams(-1, 0);
            this.SHOW_LAYOUT_PARAMS = new LayoutParams(-1, -1, 1.0f);
            this.mObserver = new DataSetObserver() {
                public void onChanged() {
                    super.onChanged();
                    super.notifyDataSetChanged();
                }

                public void onInvalidated() {
                    super.onInvalidated();
                    super.notifyDataSetInvalidated();
                }
            };
            this.mIsListExpanded = true;
            this.mParent = parent;
            this.mGroupPosition = groupPosition;
            this.mParent.registerDataSetObserver(this.mObserver);
        }

        public int getCount() {
            return this.mParent.getChildrenCount(this.mGroupPosition);
        }

        public CertHolder getItem(int position) {
            return this.mParent.getChild(this.mGroupPosition, position);
        }

        public long getItemId(int position) {
            return this.mParent.getChildId(this.mGroupPosition, position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return this.mParent.getChildView(this.mGroupPosition, position, false, convertView, parent);
        }

        public void notifyDataSetChanged() {
            this.mParent.notifyDataSetChanged();
        }

        public void notifyDataSetInvalidated() {
            this.mParent.notifyDataSetInvalidated();
        }

        public void onClick(View view) {
            boolean z = checkGroupExpandableAndStartWarningActivity() && !this.mIsListExpanded;
            this.mIsListExpanded = z;
            refreshViews();
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            TrustedCredentialsSettings.this.showCertDialog(getItem(pos));
        }

        public void setContainerView(LinearLayout containerView) {
            this.mContainerView = containerView;
            this.mListView = (ListView) this.mContainerView.findViewById(R.id.cert_list);
            this.mListView.setAdapter(this);
            this.mListView.setOnItemClickListener(this);
            this.mListView.setItemsCanFocus(true);
            this.mHeaderView = (ViewGroup) this.mContainerView.findViewById(R.id.header_view);
            this.mHeaderView.setOnClickListener(this);
            this.mIndicatorView = (ImageView) this.mHeaderView.findViewById(R.id.group_indicator);
            this.mIndicatorView.setImageDrawable(getGroupIndicator());
            FrameLayout headerContentContainer = (FrameLayout) this.mHeaderView.findViewById(R.id.header_content_container);
            headerContentContainer.addView(this.mParent.getGroupView(this.mGroupPosition, true, null, headerContentContainer));
        }

        public void showHeader(boolean showHeader) {
            this.mHeaderView.setVisibility(showHeader ? 0 : 8);
        }

        public void showDivider(boolean showDivider) {
            this.mHeaderView.findViewById(R.id.header_divider).setVisibility(showDivider ? 0 : 8);
        }

        public void setExpandIfAvailable(boolean expanded) {
            boolean z = false;
            if (expanded && this.mParent.checkGroupExpandableAndStartWarningActivity(this.mGroupPosition, false)) {
                z = true;
            }
            this.mIsListExpanded = z;
            refreshViews();
        }

        private boolean checkGroupExpandableAndStartWarningActivity() {
            return this.mParent.checkGroupExpandableAndStartWarningActivity(this.mGroupPosition);
        }

        private void refreshViews() {
            int[] iArr;
            ViewGroup.LayoutParams layoutParams;
            ImageView imageView = this.mIndicatorView;
            if (this.mIsListExpanded) {
                iArr = this.GROUP_EXPANDED_STATE_SET;
            } else {
                iArr = this.EMPTY_STATE_SET;
            }
            imageView.setImageState(iArr, false);
            ListView listView = this.mListView;
            if (this.mIsListExpanded) {
                layoutParams = this.SHOW_LAYOUT_PARAMS;
            } else {
                layoutParams = this.HIDE_LIST_LAYOUT_PARAMS;
            }
            listView.setLayoutParams(layoutParams);
            LinearLayout linearLayout = this.mContainerView;
            if (this.mIsListExpanded) {
                layoutParams = this.SHOW_LAYOUT_PARAMS;
            } else {
                layoutParams = this.HIDE_CONTAINER_LAYOUT_PARAMS;
            }
            linearLayout.setLayoutParams(layoutParams);
        }

        private Drawable getGroupIndicator() {
            TypedArray a = TrustedCredentialsSettings.this.getActivity().obtainStyledAttributes(null, R.styleable.ExpandableListView, 16842863, 0);
            Drawable groupIndicator = a.getDrawable(0);
            a.recycle();
            return groupIndicator;
        }
    }

    private class GroupAdapter extends BaseExpandableListAdapter implements OnGroupClickListener, OnChildClickListener, OnClickListener {
        private final AdapterData mData;

        private class ViewHolder {
            private TextView mSubjectPrimaryView;
            private TextView mSubjectSecondaryView;
            private Switch mSwitch;

            private ViewHolder() {
            }

            /* synthetic */ ViewHolder(GroupAdapter x0, AnonymousClass1 x1) {
                this();
            }
        }

        /* synthetic */ GroupAdapter(TrustedCredentialsSettings x0, Tab x1, AnonymousClass1 x2) {
            this(x1);
        }

        private GroupAdapter(Tab tab) {
            this.mData = new AdapterData(TrustedCredentialsSettings.this, tab, this, null);
            load();
        }

        public int getGroupCount() {
            return this.mData.mCertHoldersByUserId.size();
        }

        public int getChildrenCount(int groupPosition) {
            List<CertHolder> certHolders = (List) this.mData.mCertHoldersByUserId.valueAt(groupPosition);
            if (certHolders != null) {
                return certHolders.size();
            }
            return 0;
        }

        public UserHandle getGroup(int groupPosition) {
            return new UserHandle(this.mData.mCertHoldersByUserId.keyAt(groupPosition));
        }

        public CertHolder getChild(int groupPosition, int childPosition) {
            return (CertHolder) ((List) this.mData.mCertHoldersByUserId.get(getUserIdByGroup(groupPosition))).get(childPosition);
        }

        public long getGroupId(int groupPosition) {
            return (long) getUserIdByGroup(groupPosition);
        }

        private int getUserIdByGroup(int groupPosition) {
            return this.mData.mCertHoldersByUserId.keyAt(groupPosition);
        }

        public UserInfo getUserInfoByGroup(int groupPosition) {
            return TrustedCredentialsSettings.this.mUserManager.getUserInfo(getUserIdByGroup(groupPosition));
        }

        public long getChildId(int groupPosition, int childPosition) {
            return (long) childPosition;
        }

        public boolean hasStableIds() {
            return false;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = Utils.inflateCategoryHeader((LayoutInflater) TrustedCredentialsSettings.this.getActivity().getSystemService("layout_inflater"), parent);
            }
            TextView title = (TextView) convertView.findViewById(16908310);
            if (getUserInfoByGroup(groupPosition).isManagedProfile()) {
                title.setText(R.string.category_work);
            } else {
                title.setText(R.string.category_personal);
            }
            title.setTextAlignment(6);
            return convertView;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            return getViewForCertificate(getChild(groupPosition, childPosition), this.mData.mTab, convertView, parent);
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
            TrustedCredentialsSettings.this.showCertDialog(getChild(groupPosition, childPosition));
            return true;
        }

        public void onClick(View view) {
            TrustedCredentialsSettings.this.removeOrInstallCert((CertHolder) view.getTag());
        }

        public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
            return checkGroupExpandableAndStartWarningActivity(groupPosition) ^ 1;
        }

        public void load() {
            AdapterData adapterData = this.mData;
            Objects.requireNonNull(adapterData);
            new AliasLoader().execute(new Void[0]);
        }

        public void remove(CertHolder certHolder) {
            this.mData.remove(certHolder);
        }

        public void setExpandableListView(ExpandableListView lv) {
            lv.setAdapter(this);
            lv.setOnGroupClickListener(this);
            lv.setOnChildClickListener(this);
            lv.setVisibility(0);
        }

        public ChildAdapter getChildAdapter(int groupPosition) {
            return new ChildAdapter(TrustedCredentialsSettings.this, this, groupPosition, null);
        }

        public boolean checkGroupExpandableAndStartWarningActivity(int groupPosition) {
            return checkGroupExpandableAndStartWarningActivity(groupPosition, true);
        }

        public boolean checkGroupExpandableAndStartWarningActivity(int groupPosition, boolean startActivity) {
            UserHandle groupUser = getGroup(groupPosition);
            int groupUserId = groupUser.getIdentifier();
            if (TrustedCredentialsSettings.this.mUserManager.isQuietModeEnabled(groupUser)) {
                Intent intent = UnlaunchableAppActivity.createInQuietModeDialogIntent(groupUserId);
                if (startActivity) {
                    TrustedCredentialsSettings.this.getActivity().startActivity(intent);
                }
                return false;
            } else if (TrustedCredentialsSettings.this.mUserManager.isUserUnlocked(groupUser) || !new LockPatternUtils(TrustedCredentialsSettings.this.getActivity()).isSeparateProfileChallengeEnabled(groupUserId)) {
                return true;
            } else {
                if (startActivity) {
                    TrustedCredentialsSettings.this.startConfirmCredential(groupUserId);
                }
                return false;
            }
        }

        private View getViewForCertificate(CertHolder certHolder, Tab mTab, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(this, null);
                convertView = LayoutInflater.from(TrustedCredentialsSettings.this.getActivity()).inflate(R.layout.trusted_credential, parent, false);
                convertView.setTag(holder);
                holder.mSubjectPrimaryView = (TextView) convertView.findViewById(R.id.trusted_credential_subject_primary);
                holder.mSubjectSecondaryView = (TextView) convertView.findViewById(R.id.trusted_credential_subject_secondary);
                holder.mSwitch = (Switch) convertView.findViewById(R.id.trusted_credential_status);
                holder.mSwitch.setOnClickListener(this);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mSubjectPrimaryView.setText(certHolder.mSubjectPrimary);
            holder.mSubjectSecondaryView.setText(certHolder.mSubjectSecondary);
            if (mTab.mSwitch) {
                holder.mSwitch.setChecked(certHolder.mDeleted ^ 1);
                holder.mSwitch.setEnabled(TrustedCredentialsSettings.this.mUserManager.hasUserRestriction("no_config_credentials", new UserHandle(certHolder.mProfileId)) ^ 1);
                holder.mSwitch.setVisibility(0);
                holder.mSwitch.setTag(certHolder);
            }
            return convertView;
        }
    }

    private enum Tab {
        SYSTEM("system", R.string.trusted_credentials_system_tab, R.id.system_tab, R.id.system_progress, R.id.system_content, true),
        USER("user", R.string.trusted_credentials_user_tab, R.id.user_tab, R.id.user_progress, R.id.user_content, false);
        
        private final int mContentView;
        private final int mLabel;
        private final int mProgress;
        private final boolean mSwitch;
        private final String mTag;
        private final int mView;

        private Tab(String tag, int label, int view, int progress, int contentView, boolean withSwitch) {
            this.mTag = tag;
            this.mLabel = label;
            this.mView = view;
            this.mProgress = progress;
            this.mContentView = contentView;
            this.mSwitch = withSwitch;
        }

        private List<String> getAliases(IKeyChainService service) throws RemoteException {
            switch (this) {
                case SYSTEM:
                    return service.getSystemCaAliases().getList();
                case USER:
                    return service.getUserCaAliases().getList();
                default:
                    throw new AssertionError();
            }
        }

        private boolean deleted(IKeyChainService service, String alias) throws RemoteException {
            switch (this) {
                case SYSTEM:
                    return service.containsCaAlias(alias) ^ 1;
                case USER:
                    return false;
                default:
                    throw new AssertionError();
            }
        }
    }

    public int getMetricsCategory() {
        return 92;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        this.mUserManager = (UserManager) activity.getSystemService("user");
        this.mKeyguardManager = (KeyguardManager) activity.getSystemService("keyguard");
        this.mTrustAllCaUserId = activity.getIntent().getIntExtra(ARG_SHOW_NEW_FOR_USER, -10000);
        this.mConfirmedCredentialUsers = new ArraySet(2);
        this.mConfirmingCredentialUser = -10000;
        if (savedInstanceState != null) {
            this.mConfirmingCredentialUser = savedInstanceState.getInt(SAVED_CONFIRMING_CREDENTIAL_USER, -10000);
            ArrayList<Integer> users = savedInstanceState.getIntegerArrayList(SAVED_CONFIRMED_CREDENTIAL_USERS);
            if (users != null) {
                this.mConfirmedCredentialUsers.addAll(users);
            }
        }
        this.mConfirmingCredentialListener = null;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        filter.addAction("android.intent.action.MANAGED_PROFILE_UNLOCKED");
        activity.registerReceiver(this.mWorkProfileChangedReceiver, filter);
        activity.setTitle(R.string.trusted_credentials);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(SAVED_CONFIRMED_CREDENTIAL_USERS, new ArrayList(this.mConfirmedCredentialUsers));
        outState.putInt(SAVED_CONFIRMING_CREDENTIAL_USER, this.mConfirmingCredentialUser);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        this.mTabHost = (TabHost) inflater.inflate(R.layout.trusted_credentials, parent, false);
        this.mTabHost.setup();
        addTab(Tab.SYSTEM);
        addTab(Tab.USER);
        if (getActivity().getIntent() != null && USER_ACTION.equals(getActivity().getIntent().getAction())) {
            this.mTabHost.setCurrentTabByTag(Tab.USER.mTag);
        }
        return this.mTabHost;
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(this.mWorkProfileChangedReceiver);
        for (AliasLoader aliasLoader : this.mAliasLoaders) {
            aliasLoader.cancel(true);
        }
        this.mAliasLoaders.clear();
        this.mGroupAdapters.clear();
        if (this.mAliasOperation != null) {
            this.mAliasOperation.cancel(true);
            this.mAliasOperation = null;
        }
        closeKeyChainConnections();
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            int userId = this.mConfirmingCredentialUser;
            IntConsumer listener = this.mConfirmingCredentialListener;
            this.mConfirmingCredentialUser = -10000;
            this.mConfirmingCredentialListener = null;
            if (resultCode == -1) {
                this.mConfirmedCredentialUsers.add(Integer.valueOf(userId));
                if (listener != null) {
                    listener.accept(userId);
                }
            }
        }
    }

    private void closeKeyChainConnections() {
        synchronized (this.mKeyChainConnectionByProfileId) {
            int n = this.mKeyChainConnectionByProfileId.size();
            for (int i = 0; i < n; i++) {
                ((KeyChainConnection) this.mKeyChainConnectionByProfileId.valueAt(i)).close();
            }
            this.mKeyChainConnectionByProfileId.clear();
        }
    }

    private void addTab(Tab tab) {
        this.mTabHost.addTab(this.mTabHost.newTabSpec(tab.mTag).setIndicator(getActivity().getString(tab.mLabel)).setContent(tab.mView));
        GroupAdapter groupAdapter = new GroupAdapter(this, tab, null);
        this.mGroupAdapters.add(groupAdapter);
        int profilesSize = groupAdapter.getGroupCount();
        if (OPUtils.hasMultiAppProfiles(this.mUserManager)) {
            profilesSize--;
        }
        ViewGroup contentView = (ViewGroup) this.mTabHost.findViewById(tab.mContentView);
        contentView.getLayoutTransition().enableTransitionType(4);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i = 0; i < groupAdapter.getGroupCount(); i++) {
            boolean isWork = groupAdapter.getUserInfoByGroup(i).isManagedProfile();
            if (groupAdapter.getUserInfoByGroup(i).id != 999) {
                ChildAdapter adapter = groupAdapter.getChildAdapter(i);
                LinearLayout containerView = (LinearLayout) inflater.inflate(R.layout.trusted_credential_list_container, contentView, false);
                adapter.setContainerView(containerView);
                boolean z = true;
                adapter.showHeader(profilesSize > 1);
                adapter.showDivider(isWork);
                if (profilesSize > 2 && isWork) {
                    z = false;
                }
                adapter.setExpandIfAvailable(z);
                if (isWork) {
                    contentView.addView(containerView);
                } else {
                    contentView.addView(containerView, 0);
                }
            }
        }
    }

    private boolean startConfirmCredential(int userId) {
        Intent newIntent = this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null, userId);
        if (newIntent == null) {
            return false;
        }
        this.mConfirmingCredentialUser = userId;
        startActivityForResult(newIntent, 1);
        return true;
    }

    private boolean isTrustAllCaCertModeInProgress() {
        return this.mTrustAllCaUserId != -10000;
    }

    private void showTrustAllCaDialog(List<CertHolder> unapprovedCertHolders) {
        new TrustedCredentialsDialogBuilder(getActivity(), this).setCertHolders((CertHolder[]) unapprovedCertHolders.toArray(new CertHolder[unapprovedCertHolders.size()])).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                TrustedCredentialsSettings.this.getActivity().getIntent().removeExtra(TrustedCredentialsSettings.ARG_SHOW_NEW_FOR_USER);
                TrustedCredentialsSettings.this.mTrustAllCaUserId = -10000;
            }
        }).show();
    }

    private void showCertDialog(CertHolder certHolder) {
        new TrustedCredentialsDialogBuilder(getActivity(), this).setCertHolder(certHolder).show();
    }

    public List<X509Certificate> getX509CertsFromCertHolder(CertHolder certHolder) {
        List<X509Certificate> certificates = null;
        try {
            synchronized (this.mKeyChainConnectionByProfileId) {
                IKeyChainService service = ((KeyChainConnection) this.mKeyChainConnectionByProfileId.get(certHolder.mProfileId)).getService();
                List<String> chain = service.getCaCertificateChainAliases(certHolder.mAlias, true);
                int n = chain.size();
                certificates = new ArrayList(n);
                for (int i = 0; i < n; i++) {
                    certificates.add(KeyChain.toCertificate(service.getEncodedCaCertificate((String) chain.get(i), true)));
                }
            }
        } catch (RemoteException ex) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("RemoteException while retrieving certificate chain for root ");
            stringBuilder.append(certHolder.mAlias);
            Log.e(str, stringBuilder.toString(), ex);
        }
        return certificates;
    }

    public void removeOrInstallCert(CertHolder certHolder) {
        new AliasOperation(this, certHolder, null).execute(new Void[0]);
    }

    public boolean startConfirmCredentialIfNotConfirmed(int userId, IntConsumer onCredentialConfirmedListener) {
        if (this.mConfirmedCredentialUsers.contains(Integer.valueOf(userId))) {
            return false;
        }
        boolean result = startConfirmCredential(userId);
        if (result) {
            this.mConfirmingCredentialListener = onCredentialConfirmedListener;
        }
        return result;
    }
}
