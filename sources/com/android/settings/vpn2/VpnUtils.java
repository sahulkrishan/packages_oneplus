package com.android.settings.vpn2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.security.KeyStore;
import android.util.Log;
import com.android.internal.net.VpnConfig;

public class VpnUtils {
    private static final String TAG = "VpnUtils";

    public static String getLockdownVpn() {
        byte[] value = KeyStore.getInstance().get("LOCKDOWN_VPN");
        return value == null ? null : new String(value);
    }

    public static void clearLockdownVpn(Context context) {
        KeyStore.getInstance().delete("LOCKDOWN_VPN");
        getConnectivityManager(context).updateLockdownVpn();
    }

    public static void setLockdownVpn(Context context, String lockdownKey) {
        KeyStore.getInstance().put("LOCKDOWN_VPN", lockdownKey.getBytes(), -1, 0);
        getConnectivityManager(context).updateLockdownVpn();
    }

    public static boolean isVpnLockdown(String key) {
        return key.equals(getLockdownVpn());
    }

    public static boolean isAnyLockdownActive(Context context) {
        int userId = context.getUserId();
        boolean z = true;
        if (getLockdownVpn() != null) {
            return true;
        }
        if (getConnectivityManager(context).getAlwaysOnVpnPackageForUser(userId) == null || Secure.getIntForUser(context.getContentResolver(), "always_on_vpn_lockdown", 0, userId) == 0) {
            z = false;
        }
        return z;
    }

    public static boolean isVpnActive(Context context) throws RemoteException {
        return getIConnectivityManager().getVpnConfig(context.getUserId()) != null;
    }

    public static String getConnectedPackage(IConnectivityManager service, int userId) throws RemoteException {
        VpnConfig config = service.getVpnConfig(userId);
        return config != null ? config.user : null;
    }

    private static ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
    }

    private static IConnectivityManager getIConnectivityManager() {
        return Stub.asInterface(ServiceManager.getService("connectivity"));
    }

    public static boolean isAlwaysOnVpnSet(ConnectivityManager cm, int userId) {
        return cm.getAlwaysOnVpnPackageForUser(userId) != null;
    }

    public static boolean disconnectLegacyVpn(Context context) {
        try {
            int userId = context.getUserId();
            IConnectivityManager connectivityService = getIConnectivityManager();
            if (connectivityService.getLegacyVpnInfo(userId) != null) {
                clearLockdownVpn(context);
                connectivityService.prepareVpn(null, "[Legacy VPN]", userId);
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Legacy VPN could not be disconnected", e);
        }
        return false;
    }
}
