package com.android.settings.network;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.utils.ThreadUtils;

public class VpnPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    private static final NetworkRequest REQUEST = new Builder().removeCapability(15).removeCapability(13).removeCapability(14).build();
    private static final String TAG = "VpnPreferenceController";
    private final ConnectivityManager mConnectivityManager;
    private final IConnectivityManager mConnectivityManagerService;
    private final NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onAvailable(Network network) {
            VpnPreferenceController.this.updateSummary();
        }

        public void onLost(Network network) {
            VpnPreferenceController.this.updateSummary();
        }
    };
    private Preference mPreference;
    private final String mToggleable;
    private final UserManager mUserManager;

    public VpnPreferenceController(Context context) {
        super(context);
        this.mToggleable = Global.getString(context.getContentResolver(), "airplane_mode_toggleable_radios");
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mConnectivityManagerService = Stub.asInterface(ServiceManager.getService("connectivity"));
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(KEY_VPN_SETTINGS);
        if ((this.mToggleable == null || !this.mToggleable.contains("wifi")) && this.mPreference != null) {
            this.mPreference.setDependency("airplane_mode");
        }
    }

    public boolean isAvailable() {
        return RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_config_vpn", UserHandle.myUserId()) ^ 1;
    }

    public String getPreferenceKey() {
        return KEY_VPN_SETTINGS;
    }

    public void onPause() {
        if (isAvailable()) {
            this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
        }
    }

    public void onResume() {
        if (isAvailable()) {
            this.mConnectivityManager.registerNetworkCallback(REQUEST, this.mNetworkCallback);
            updateSummary();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting(otherwise = 2)
    public void updateSummary() {
        if (this.mPreference != null) {
            SparseArray<VpnConfig> vpns = new SparseArray();
            try {
                int uid;
                String summary;
                for (UserInfo user : this.mUserManager.getUsers()) {
                    VpnConfig cfg = this.mConnectivityManagerService.getVpnConfig(user.id);
                    if (cfg != null) {
                        if (cfg.legacy) {
                            LegacyVpnInfo legacyVpn = this.mConnectivityManagerService.getLegacyVpnInfo(user.id);
                            if (legacyVpn != null) {
                                if (legacyVpn.state != 3) {
                                }
                            }
                        }
                        vpns.put(user.id, cfg);
                    }
                }
                UserInfo userInfo = this.mUserManager.getUserInfo(UserHandle.myUserId());
                if (userInfo.isRestricted()) {
                    uid = userInfo.restrictedProfileParentId;
                } else {
                    uid = userInfo.id;
                }
                VpnConfig vpn = (VpnConfig) vpns.get(uid);
                if (vpn == null) {
                    summary = this.mContext.getString(R.string.vpn_disconnected_summary);
                } else {
                    summary = getNameForVpnConfig(vpn, UserHandle.of(uid));
                }
                ThreadUtils.postOnMainThread(new -$$Lambda$VpnPreferenceController$iDQ0RgxaDkCLoaHHZ6-UO2xSI_c(this, summary));
            } catch (RemoteException rme) {
                Log.e(TAG, "Unable to list active VPNs", rme);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getNameForVpnConfig(VpnConfig cfg, UserHandle user) {
        if (cfg.legacy) {
            return this.mContext.getString(R.string.wifi_display_status_connected);
        }
        String vpnPackage = cfg.user;
        try {
            return VpnConfig.getVpnLabel(this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user), vpnPackage).toString();
        } catch (NameNotFoundException nnfe) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Package ");
            stringBuilder.append(vpnPackage);
            stringBuilder.append(" is not present");
            Log.e(str, stringBuilder.toString(), nnfe);
            return null;
        }
    }
}
