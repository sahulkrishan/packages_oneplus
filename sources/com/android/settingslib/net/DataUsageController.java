package com.android.settingslib.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsService.Stub;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkStatsHistory;
import android.net.NetworkStatsHistory.Entry;
import android.net.NetworkTemplate;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Pair;
import java.time.ZonedDateTime;
import java.util.Formatter;
import java.util.Locale;

public class DataUsageController {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int FIELDS = 10;
    private static final StringBuilder PERIOD_BUILDER = new StringBuilder(50);
    private static final Formatter PERIOD_FORMATTER = new Formatter(PERIOD_BUILDER, Locale.getDefault());
    private static final String TAG = "DataUsageController";
    private Callback mCallback;
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private NetworkNameProvider mNetworkController;
    private final NetworkPolicyManager mPolicyManager = NetworkPolicyManager.from(this.mContext);
    private INetworkStatsSession mSession;
    private final INetworkStatsService mStatsService = Stub.asInterface(ServiceManager.getService("netstats"));
    private final SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;

    public interface Callback {
        void onMobileDataEnabled(boolean z);
    }

    public static class DataUsageInfo {
        public String carrier;
        public long cycleEnd;
        public long cycleStart;
        public long limitLevel;
        public String period;
        public long startDate;
        public long usageLevel;
        public long warningLevel;
    }

    public interface NetworkNameProvider {
        String getMobileDataNetworkName();
    }

    public DataUsageController(Context context) {
        this.mContext = context;
        this.mTelephonyManager = TelephonyManager.from(context);
        this.mConnectivityManager = ConnectivityManager.from(context);
        this.mSubscriptionManager = SubscriptionManager.from(context);
    }

    public void setNetworkController(NetworkNameProvider networkController) {
        this.mNetworkController = networkController;
    }

    public long getDefaultWarningLevel() {
        return 1048576 * ((long) this.mContext.getResources().getInteger(17694956));
    }

    private INetworkStatsSession getSession() {
        if (this.mSession == null) {
            try {
                this.mSession = this.mStatsService.openSession();
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to open stats session", e);
            } catch (RuntimeException e2) {
                Log.w(TAG, "Failed to open stats session", e2);
            }
        }
        return this.mSession;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    private DataUsageInfo warn(String msg) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Failed to get data usage, ");
        stringBuilder.append(msg);
        Log.w(str, stringBuilder.toString());
        return null;
    }

    public DataUsageInfo getDataUsageInfo() {
        String subscriberId = getActiveSubscriberId(this.mContext);
        if (subscriberId == null) {
            return warn("no subscriber id");
        }
        return getDataUsageInfo(NetworkTemplate.normalize(NetworkTemplate.buildTemplateMobileAll(subscriberId), this.mTelephonyManager.getMergedSubscriberIds()));
    }

    public DataUsageInfo getWifiDataUsageInfo() {
        return getDataUsageInfo(NetworkTemplate.buildTemplateWifiWildcard());
    }

    public DataUsageInfo getDataUsageInfo(NetworkTemplate template) {
        INetworkStatsSession iNetworkStatsSession;
        DataUsageController dataUsageController = this;
        INetworkStatsSession session = getSession();
        if (session == null) {
            return dataUsageController.warn("no stats session");
        }
        NetworkPolicy policy = findNetworkPolicy(template);
        NetworkPolicy networkPolicy;
        try {
            long start;
            long end;
            NetworkStatsHistory history = session.getHistoryForNetwork(template, 10);
            long now = System.currentTimeMillis();
            if (policy != null) {
                try {
                    Pair<ZonedDateTime, ZonedDateTime> cycle = (Pair) NetworkPolicyManager.cycleIterator(policy).next();
                    start = ((ZonedDateTime) cycle.first).toInstant().toEpochMilli();
                    end = ((ZonedDateTime) cycle.second).toInstant().toEpochMilli();
                } catch (RemoteException e) {
                    iNetworkStatsSession = session;
                    networkPolicy = policy;
                    return dataUsageController.warn("remote call failed");
                }
            }
            end = now;
            start = now - 2419200000L;
            long start2 = start;
            long callStart = System.currentTimeMillis();
            NetworkPolicy policy2 = policy;
            long start3 = start2;
            long end2 = end;
            try {
                Entry entry = history.getValues(start2, end, now, null);
                long callEnd = System.currentTimeMillis();
                if (DEBUG) {
                    try {
                        String str = TAG;
                        r11 = new Object[5];
                        r11[3] = Long.valueOf(callEnd - callStart);
                        r11[4] = historyEntryToString(entry);
                        Log.d(str, String.format("history call from %s to %s now=%s took %sms: %s", r11));
                    } catch (RemoteException e2) {
                        networkPolicy = policy2;
                        dataUsageController = this;
                    }
                } else {
                    long j = now;
                }
                if (entry == null) {
                    try {
                        end = end2;
                        dataUsageController = this;
                    } catch (RemoteException e3) {
                        dataUsageController = this;
                        return dataUsageController.warn("remote call failed");
                    }
                    try {
                        return dataUsageController.warn("no entry data");
                    } catch (RemoteException e4) {
                        return dataUsageController.warn("remote call failed");
                    }
                }
                end = end2;
                dataUsageController = this;
                try {
                    long totalBytes = entry.rxBytes + entry.txBytes;
                    DataUsageInfo usage = new DataUsageInfo();
                    usage.startDate = start3;
                    usage.usageLevel = totalBytes;
                    usage.period = dataUsageController.formatDateRange(start3, end);
                    usage.cycleStart = start3;
                    usage.cycleEnd = end;
                    if (policy2 != null) {
                        networkPolicy = policy2;
                        try {
                            usage.limitLevel = networkPolicy.limitBytes > 0 ? networkPolicy.limitBytes : 0;
                            usage.warningLevel = networkPolicy.warningBytes > 0 ? networkPolicy.warningBytes : 0;
                        } catch (RemoteException e5) {
                            return dataUsageController.warn("remote call failed");
                        }
                    }
                    long j2 = start3;
                    networkPolicy = policy2;
                    usage.warningLevel = getDefaultWarningLevel();
                    if (dataUsageController.mNetworkController != null) {
                        usage.carrier = dataUsageController.mNetworkController.getMobileDataNetworkName();
                    }
                    return usage;
                } catch (RemoteException e6) {
                    networkPolicy = policy2;
                    return dataUsageController.warn("remote call failed");
                }
            } catch (RemoteException e7) {
                networkPolicy = policy2;
                dataUsageController = this;
                return dataUsageController.warn("remote call failed");
            }
        } catch (RemoteException e8) {
            iNetworkStatsSession = session;
            networkPolicy = policy;
            return dataUsageController.warn("remote call failed");
        }
    }

    private NetworkPolicy findNetworkPolicy(NetworkTemplate template) {
        if (this.mPolicyManager == null || template == null) {
            return null;
        }
        NetworkPolicy[] policies = this.mPolicyManager.getNetworkPolicies();
        if (policies == null) {
            return null;
        }
        for (NetworkPolicy policy : policies) {
            if (policy != null && template.equals(policy.template)) {
                return policy;
            }
        }
        return null;
    }

    private static String historyEntryToString(Entry entry) {
        if (entry == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder("Entry[");
        stringBuilder.append("bucketDuration=");
        stringBuilder.append(entry.bucketDuration);
        stringBuilder.append(",bucketStart=");
        stringBuilder.append(entry.bucketStart);
        stringBuilder.append(",activeTime=");
        stringBuilder.append(entry.activeTime);
        stringBuilder.append(",rxBytes=");
        stringBuilder.append(entry.rxBytes);
        stringBuilder.append(",rxPackets=");
        stringBuilder.append(entry.rxPackets);
        stringBuilder.append(",txBytes=");
        stringBuilder.append(entry.txBytes);
        stringBuilder.append(",txPackets=");
        stringBuilder.append(entry.txPackets);
        stringBuilder.append(",operations=");
        stringBuilder.append(entry.operations);
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public void setMobileDataEnabled(boolean enabled) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setMobileDataEnabled: enabled=");
        stringBuilder.append(enabled);
        Log.d(str, stringBuilder.toString());
        this.mTelephonyManager.setDataEnabled(enabled);
        if (this.mCallback != null) {
            this.mCallback.onMobileDataEnabled(enabled);
        }
    }

    public boolean isMobileDataSupported() {
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Esports SIM id=");
            stringBuilder.append(this.mSubscriptionManager.getDefaultDataPhoneId());
            Log.d(str, stringBuilder.toString());
        }
        if (this.mConnectivityManager.isNetworkSupported(0) && this.mTelephonyManager.getSimState(this.mSubscriptionManager.getDefaultDataPhoneId()) == 5) {
            return true;
        }
        return false;
    }

    public boolean isMobileDataEnabled() {
        return this.mTelephonyManager.getDataEnabled();
    }

    private static String getActiveSubscriberId(Context context) {
        return TelephonyManager.from(context).getSubscriberId(SubscriptionManager.getDefaultDataSubscriptionId());
    }

    private String formatDateRange(long start, long end) {
        Throwable th;
        synchronized (PERIOD_BUILDER) {
            try {
                PERIOD_BUILDER.setLength(0);
                String formatter = DateUtils.formatDateRange(this.mContext, PERIOD_FORMATTER, start, end, 65552, null).toString();
                return formatter;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }
}
