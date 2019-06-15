package com.android.settings.datausage;

import android.content.Context;
import android.net.INetworkPolicyListener;
import android.net.INetworkPolicyListener.Stub;
import android.net.NetworkPolicyManager;
import android.os.RemoteException;
import android.util.Pair;
import android.util.SparseIntArray;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.utils.ThreadUtils;
import java.util.ArrayList;

public class DataSaverBackend {
    private static final String TAG = "DataSaverBackend";
    private boolean mBlacklistInitialized;
    private final Context mContext;
    private final ArrayList<Listener> mListeners = new ArrayList();
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final INetworkPolicyListener mPolicyListener = new Stub() {
        public void onUidRulesChanged(int uid, int uidRules) throws RemoteException {
        }

        public void onUidPoliciesChanged(int uid, int uidPolicies) {
            ThreadUtils.postOnMainThread(new -$$Lambda$DataSaverBackend$1$ZKxRzvcfxNqOKXdNkDOsoxL7i9I(this, uid, uidPolicies));
        }

        public void onMeteredIfacesChanged(String[] strings) throws RemoteException {
        }

        public void onRestrictBackgroundChanged(boolean isDataSaving) throws RemoteException {
            ThreadUtils.postOnMainThread(new -$$Lambda$DataSaverBackend$1$1851XOwRm2qYDEpp81v4WIVwIHs(this, isDataSaving));
        }

        public void onSubscriptionOverride(int subId, int overrideMask, int overrideValue) {
        }
    };
    private final NetworkPolicyManager mPolicyManager;
    private SparseIntArray mUidPolicies = new SparseIntArray();
    private boolean mWhitelistInitialized;

    public interface Listener {
        void onBlacklistStatusChanged(int i, boolean z);

        void onDataSaverChanged(boolean z);

        void onWhitelistStatusChanged(int i, boolean z);
    }

    public DataSaverBackend(Context context) {
        this.mContext = context;
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
        this.mPolicyManager = NetworkPolicyManager.from(context);
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
        if (this.mListeners.size() == 1) {
            this.mPolicyManager.registerListener(this.mPolicyListener);
        }
        listener.onDataSaverChanged(isDataSaverEnabled());
    }

    public void remListener(Listener listener) {
        this.mListeners.remove(listener);
        if (this.mListeners.size() == 0) {
            this.mPolicyManager.unregisterListener(this.mPolicyListener);
        }
    }

    public boolean isDataSaverEnabled() {
        return this.mPolicyManager.getRestrictBackground();
    }

    public void setDataSaverEnabled(boolean enabled) {
        this.mPolicyManager.setRestrictBackground(enabled);
        this.mMetricsFeatureProvider.action(this.mContext, 394, (int) enabled);
    }

    public void refreshWhitelist() {
        loadWhitelist();
    }

    public void setIsWhitelisted(int uid, String packageName, boolean whitelisted) {
        int policy = whitelisted ? 4 : 0;
        this.mPolicyManager.setUidPolicy(uid, policy);
        this.mUidPolicies.put(uid, policy);
        if (whitelisted) {
            this.mMetricsFeatureProvider.action(this.mContext, 395, packageName, new Pair[0]);
        }
    }

    public boolean isWhitelisted(int uid) {
        loadWhitelist();
        return this.mUidPolicies.get(uid, 0) == 4;
    }

    public int getWhitelistedCount() {
        int count = 0;
        loadWhitelist();
        for (int i = 0; i < this.mUidPolicies.size(); i++) {
            if (this.mUidPolicies.valueAt(i) == 4) {
                count++;
            }
        }
        return count;
    }

    private void loadWhitelist() {
        this.mUidPolicies.clear();
        for (int uid : this.mPolicyManager.getUidsWithPolicy(4)) {
            this.mUidPolicies.put(uid, 4);
        }
    }

    public void refreshBlacklist() {
        loadBlacklist();
    }

    public void setIsBlacklisted(int uid, String packageName, boolean blacklisted) {
        boolean policy = blacklisted;
        this.mPolicyManager.setUidPolicy(uid, policy);
        this.mUidPolicies.put(uid, policy);
        if (blacklisted) {
            this.mMetricsFeatureProvider.action(this.mContext, 396, packageName, new Pair[0]);
        }
    }

    public boolean isBlacklisted(int uid) {
        loadBlacklist();
        return this.mUidPolicies.get(uid, 0) == 1;
    }

    private void loadBlacklist() {
        for (int uid : this.mPolicyManager.getUidsWithPolicy(1)) {
            this.mUidPolicies.put(uid, 1);
        }
    }

    private void handleRestrictBackgroundChanged(boolean isDataSaving) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((Listener) this.mListeners.get(i)).onDataSaverChanged(isDataSaving);
        }
    }

    private void handleWhitelistChanged(int uid, boolean isWhitelisted) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((Listener) this.mListeners.get(i)).onWhitelistStatusChanged(uid, isWhitelisted);
        }
    }

    private void handleBlacklistChanged(int uid, boolean isBlacklisted) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            ((Listener) this.mListeners.get(i)).onBlacklistStatusChanged(uid, isBlacklisted);
        }
    }

    private void handleUidPoliciesChanged(int uid, int newPolicy) {
        loadWhitelist();
        loadBlacklist();
        boolean isBlacklisted = false;
        int oldPolicy = this.mUidPolicies.get(uid, 0);
        if (newPolicy == 0) {
            this.mUidPolicies.delete(uid);
        } else {
            this.mUidPolicies.put(uid, newPolicy);
        }
        boolean wasWhitelisted = oldPolicy == 4;
        boolean wasBlacklisted = oldPolicy == 1;
        boolean isWhitelisted = newPolicy == 4;
        if (newPolicy == 1) {
            isBlacklisted = true;
        }
        if (wasWhitelisted != isWhitelisted) {
            handleWhitelistChanged(uid, isWhitelisted);
        }
        if (wasBlacklisted != isBlacklisted) {
            handleBlacklistChanged(uid, isBlacklisted);
        }
    }
}
