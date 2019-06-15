package com.android.settings.datausage;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService.Stub;
import android.net.INetworkStatsSession;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import java.util.List;

public final class DataUsageUtils {
    private static final String ETHERNET = "ethernet";
    static final boolean TEST_RADIOS = false;
    static final String TEST_RADIOS_PROP = "test.radios";

    private DataUsageUtils() {
    }

    public static CharSequence formatDataUsage(Context context, long byteValue) {
        BytesResult res = Formatter.formatBytes(context.getResources(), byteValue, 8);
        return BidiFormatter.getInstance().unicodeWrap(context.getString(17039924, new Object[]{res.value, res.units}));
    }

    public static boolean hasEthernet(Context context) {
        boolean hasEthernet = ConnectivityManager.from(context).isNetworkSupported(true);
        try {
            long ethernetBytes;
            INetworkStatsSession statsSession = Stub.asInterface(ServiceManager.getService("netstats")).openSession();
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

    public static boolean hasMobileData(Context context) {
        ConnectivityManager connectivityManager = ConnectivityManager.from(context);
        if (connectivityManager == null || !connectivityManager.isNetworkSupported(0)) {
            return false;
        }
        return true;
    }

    public static boolean hasWifiRadio(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
        return connectivityManager != null && connectivityManager.isNetworkSupported(1);
    }

    public static boolean hasSim(Context context) {
        int simState = ((TelephonyManager) context.getSystemService(TelephonyManager.class)).getSimState();
        return (simState == 1 || simState == 0) ? false : true;
    }

    public static int getDefaultSubscriptionId(Context context) {
        SubscriptionManager subManager = SubscriptionManager.from(context);
        if (subManager == null) {
            return -1;
        }
        SubscriptionInfo subscriptionInfo = subManager.getDefaultDataSubscriptionInfo();
        if (subscriptionInfo == null) {
            List<SubscriptionInfo> list = subManager.getAllSubscriptionInfoList();
            if (list.size() == 0) {
                return -1;
            }
            subscriptionInfo = (SubscriptionInfo) list.get(0);
        }
        return subscriptionInfo.getSubscriptionId();
    }

    static NetworkTemplate getDefaultTemplate(Context context, int defaultSubId) {
        if (hasMobileData(context) && defaultSubId != -1) {
            TelephonyManager telephonyManager = TelephonyManager.from(context);
            return NetworkTemplate.normalize(NetworkTemplate.buildTemplateMobileAll(telephonyManager.getSubscriberId(defaultSubId)), telephonyManager.getMergedSubscriberIds());
        } else if (hasWifiRadio(context)) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        } else {
            return NetworkTemplate.buildTemplateEthernet();
        }
    }
}
