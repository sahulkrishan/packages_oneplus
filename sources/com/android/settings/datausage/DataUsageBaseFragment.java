package com.android.settings.datausage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.INetworkManagementService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.datausage.TemplatePreference.NetworkServices;
import com.android.settingslib.NetworkPolicyEditor;

public abstract class DataUsageBaseFragment extends DashboardFragment {
    private static final String ETHERNET = "ethernet";
    private static final String TAG = "DataUsageBase";
    protected final NetworkServices services = new NetworkServices();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getContext();
        this.services.mNetworkService = Stub.asInterface(ServiceManager.getService("network_management"));
        this.services.mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService("netstats"));
        this.services.mPolicyManager = (NetworkPolicyManager) context.getSystemService("netpolicy");
        this.services.mPolicyEditor = new NetworkPolicyEditor(this.services.mPolicyManager);
        this.services.mTelephonyManager = TelephonyManager.from(context);
        this.services.mSubscriptionManager = SubscriptionManager.from(context);
        this.services.mUserManager = UserManager.get(context);
    }

    public void onResume() {
        super.onResume();
        this.services.mPolicyEditor.read();
    }

    /* Access modifiers changed, original: protected */
    public boolean isAdmin() {
        return this.services.mUserManager.isAdminUser();
    }

    /* Access modifiers changed, original: protected */
    public boolean isMobileDataAvailable(int subId) {
        return this.services.mSubscriptionManager.getActiveSubscriptionInfo(subId) != null;
    }

    /* Access modifiers changed, original: protected */
    public boolean isNetworkPolicyModifiable(NetworkPolicy policy, int subId) {
        return policy != null && isBandwidthControlEnabled() && this.services.mUserManager.isAdminUser() && isDataEnabled(subId);
    }

    private boolean isDataEnabled(int subId) {
        if (subId == -1) {
            return true;
        }
        return this.services.mTelephonyManager.getDataEnabled(subId);
    }

    /* Access modifiers changed, original: protected */
    public boolean isBandwidthControlEnabled() {
        try {
            return this.services.mNetworkService.isBandwidthControlEnabled();
        } catch (RemoteException e) {
            Log.w(TAG, "problem talking with INetworkManagementService: ", e);
            return false;
        }
    }

    public boolean hasEthernet(Context context) {
        boolean hasEthernet = ConnectivityManager.from(context).isNetworkSupported(true);
        try {
            long ethernetBytes;
            INetworkStatsSession statsSession = this.services.mStatsService.openSession();
            if (statsSession != null) {
                ethernetBytes = statsSession.getSummaryForNetwork(NetworkTemplate.buildTemplateEthernet(), Long.MIN_VALUE, Long.MAX_VALUE).getTotalBytes();
                TrafficStats.closeQuietly(statsSession);
            } else {
                ethernetBytes = 0;
            }
            return hasEthernet && ethernetBytes > 0;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}
