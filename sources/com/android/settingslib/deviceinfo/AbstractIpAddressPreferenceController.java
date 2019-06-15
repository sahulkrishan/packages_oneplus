package com.android.settingslib.deviceinfo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settingslib.R;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.net.InetAddress;
import java.util.Iterator;

public abstract class AbstractIpAddressPreferenceController extends AbstractConnectivityPreferenceController {
    private static final String[] CONNECTIVITY_INTENTS = new String[]{"android.net.conn.CONNECTIVITY_CHANGE", "android.net.wifi.LINK_CONFIGURATION_CHANGED", "android.net.wifi.STATE_CHANGE"};
    @VisibleForTesting
    static final String KEY_IP_ADDRESS = "wifi_ip_address";
    private final ConnectivityManager mCM;
    private Preference mIpAddress;

    public AbstractIpAddressPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, lifecycle);
        this.mCM = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_IP_ADDRESS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mIpAddress = screen.findPreference(KEY_IP_ADDRESS);
        updateConnectivity();
    }

    /* Access modifiers changed, original: protected */
    public String[] getConnectivityIntents() {
        return CONNECTIVITY_INTENTS;
    }

    /* Access modifiers changed, original: protected */
    public void updateConnectivity() {
        CharSequence ipAddress = getDefaultIpAddresses(this.mCM);
        if (ipAddress != null) {
            this.mIpAddress.setSummary(ipAddress);
        } else {
            this.mIpAddress.setSummary(R.string.status_unavailable);
        }
    }

    private static String getDefaultIpAddresses(ConnectivityManager cm) {
        return formatIpAddresses(cm.getActiveLinkProperties());
    }

    private static String formatIpAddresses(LinkProperties prop) {
        if (prop == null) {
            return null;
        }
        Iterator<InetAddress> iter = prop.getAllAddresses().iterator();
        if (!iter.hasNext()) {
            return null;
        }
        StringBuilder addresses = new StringBuilder();
        while (iter.hasNext()) {
            addresses.append(((InetAddress) iter.next()).getHostAddress());
            if (iter.hasNext()) {
                addresses.append("\n");
            }
        }
        return addresses.toString();
    }
}
