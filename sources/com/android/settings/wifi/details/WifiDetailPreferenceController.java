package com.android.settings.wifi.details;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.RouteInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ActionListener;
import android.os.Handler;
import android.support.v4.text.BidiFormatter;
import android.support.v4.view.PointerIconCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.ActionButtonPreference;
import com.android.settings.widget.EntityHeaderController;
import com.android.settings.wifi.WifiDetailPreference;
import com.android.settings.wifi.WifiDialog;
import com.android.settings.wifi.WifiDialog.WifiDialogListener;
import com.android.settings.wifi.WifiUtils;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.wifi.AccessPoint;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class WifiDetailPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, WifiDialogListener, LifecycleObserver, OnPause, OnResume {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    @VisibleForTesting
    static final String KEY_BUTTONS_PREF = "buttons";
    @VisibleForTesting
    static final String KEY_DNS_PREF = "dns";
    @VisibleForTesting
    static final String KEY_FREQUENCY_PREF = "frequency";
    @VisibleForTesting
    static final String KEY_GATEWAY_PREF = "gateway";
    @VisibleForTesting
    static final String KEY_HEADER = "connection_header";
    @VisibleForTesting
    static final String KEY_IPV6_ADDRESSES_PREF = "ipv6_addresses";
    @VisibleForTesting
    static final String KEY_IPV6_CATEGORY = "ipv6_category";
    @VisibleForTesting
    static final String KEY_IP_ADDRESS_PREF = "ip_address";
    @VisibleForTesting
    static final String KEY_LINK_SPEED = "link_speed";
    @VisibleForTesting
    static final String KEY_MAC_ADDRESS_PREF = "mac_address";
    @VisibleForTesting
    static final String KEY_SECURITY_PREF = "security";
    @VisibleForTesting
    static final String KEY_SIGNAL_STRENGTH_PREF = "signal_strength";
    @VisibleForTesting
    static final String KEY_SUBNET_MASK_PREF = "subnet_mask";
    private static final String TAG = "WifiDetailsPrefCtrl";
    private AccessPoint mAccessPoint;
    private ActionButtonPreference mButtonsPref;
    private final ConnectivityManager mConnectivityManager;
    private WifiDetailPreference mDnsPref;
    private EntityHeaderController mEntityHeaderController;
    private final IntentFilter mFilter;
    private final Fragment mFragment;
    private WifiDetailPreference mFrequencyPref;
    private WifiDetailPreference mGatewayPref;
    private final Handler mHandler;
    private final IconInjector mIconInjector;
    private WifiDetailPreference mIpAddressPref;
    private Preference mIpv6AddressPref;
    private PreferenceCategory mIpv6Category;
    private LinkProperties mLinkProperties;
    private WifiDetailPreference mLinkSpeedPref;
    private WifiDetailPreference mMacAddressPref;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private Network mNetwork;
    private final NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onLinkPropertiesChanged(Network network, LinkProperties lp) {
            if (network.equals(WifiDetailPreferenceController.this.mNetwork) && !lp.equals(WifiDetailPreferenceController.this.mLinkProperties)) {
                WifiDetailPreferenceController.this.mLinkProperties = lp;
                WifiDetailPreferenceController.this.updateIpLayerInfo();
            }
        }

        private boolean hasCapabilityChanged(NetworkCapabilities nc, int cap) {
            boolean z = true;
            if (WifiDetailPreferenceController.this.mNetworkCapabilities == null) {
                return true;
            }
            if (WifiDetailPreferenceController.this.mNetworkCapabilities.hasCapability(cap) == nc.hasCapability(cap)) {
                z = false;
            }
            return z;
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities nc) {
            if (network.equals(WifiDetailPreferenceController.this.mNetwork) && !nc.equals(WifiDetailPreferenceController.this.mNetworkCapabilities)) {
                if (hasCapabilityChanged(nc, 16) || hasCapabilityChanged(nc, 17)) {
                    WifiDetailPreferenceController.this.refreshNetworkState();
                }
                WifiDetailPreferenceController.this.mNetworkCapabilities = nc;
                WifiDetailPreferenceController.this.updateIpLayerInfo();
            }
        }

        public void onLost(Network network) {
            if (network.equals(WifiDetailPreferenceController.this.mNetwork)) {
                WifiDetailPreferenceController.this.exitActivity();
            }
        }
    };
    private NetworkCapabilities mNetworkCapabilities;
    private NetworkInfo mNetworkInfo;
    private final NetworkRequest mNetworkRequest = new Builder().clearCapabilities().addTransportType(1).build();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Removed duplicated region for block: B:24:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:24:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:24:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        public void onReceive(android.content.Context r5, android.content.Intent r6) {
            /*
            r4 = this;
            r0 = r6.getAction();
            r1 = r0.hashCode();
            r2 = -385684331; // 0xffffffffe902ec95 float:-9.892349E24 double:NaN;
            r3 = 0;
            if (r1 == r2) goto L_0x002d;
        L_0x000e:
            r2 = -343630553; // 0xffffffffeb849d27 float:-3.2064068E26 double:NaN;
            if (r1 == r2) goto L_0x0023;
        L_0x0013:
            r2 = 1625920338; // 0x60e99352 float:1.3464709E20 double:8.03311382E-315;
            if (r1 == r2) goto L_0x0019;
        L_0x0018:
            goto L_0x0037;
        L_0x0019:
            r1 = "android.net.wifi.CONFIGURED_NETWORKS_CHANGE";
            r0 = r0.equals(r1);
            if (r0 == 0) goto L_0x0037;
        L_0x0021:
            r0 = r3;
            goto L_0x0038;
        L_0x0023:
            r1 = "android.net.wifi.STATE_CHANGE";
            r0 = r0.equals(r1);
            if (r0 == 0) goto L_0x0037;
        L_0x002b:
            r0 = 1;
            goto L_0x0038;
        L_0x002d:
            r1 = "android.net.wifi.RSSI_CHANGED";
            r0 = r0.equals(r1);
            if (r0 == 0) goto L_0x0037;
        L_0x0035:
            r0 = 2;
            goto L_0x0038;
        L_0x0037:
            r0 = -1;
        L_0x0038:
            switch(r0) {
                case 0: goto L_0x003c;
                case 1: goto L_0x005d;
                case 2: goto L_0x005d;
                default: goto L_0x003b;
            };
        L_0x003b:
            goto L_0x0062;
        L_0x003c:
            r0 = "multipleChanges";
            r0 = r6.getBooleanExtra(r0, r3);
            if (r0 != 0) goto L_0x005d;
        L_0x0044:
            r0 = "wifiConfiguration";
            r0 = r6.getParcelableExtra(r0);
            r0 = (android.net.wifi.WifiConfiguration) r0;
            r1 = com.android.settings.wifi.details.WifiDetailPreferenceController.this;
            r1 = r1.mAccessPoint;
            r1 = r1.matches(r0);
            if (r1 == 0) goto L_0x005d;
        L_0x0058:
            r1 = com.android.settings.wifi.details.WifiDetailPreferenceController.this;
            r1.mWifiConfig = r0;
        L_0x005d:
            r0 = com.android.settings.wifi.details.WifiDetailPreferenceController.this;
            r0.updateInfo();
        L_0x0062:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.wifi.details.WifiDetailPreferenceController$AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
        }
    };
    private int mRssiSignalLevel = -1;
    private WifiDetailPreference mSecurityPref;
    private String[] mSignalStr;
    private WifiDetailPreference mSignalStrengthPref;
    private WifiDetailPreference mSubnetPref;
    private WifiConfiguration mWifiConfig;
    private WifiInfo mWifiInfo;
    private final WifiManager mWifiManager;

    @VisibleForTesting
    static class IconInjector {
        private final Context mContext;

        public IconInjector(Context context) {
            this.mContext = context;
        }

        public Drawable getIcon(int level) {
            return this.mContext.getDrawable(Utils.getWifiIconResource(level)).mutate();
        }
    }

    public static WifiDetailPreferenceController newInstance(AccessPoint accessPoint, ConnectivityManager connectivityManager, Context context, Fragment fragment, Handler handler, Lifecycle lifecycle, WifiManager wifiManager, MetricsFeatureProvider metricsFeatureProvider) {
        Context context2 = context;
        return new WifiDetailPreferenceController(accessPoint, connectivityManager, context2, fragment, handler, lifecycle, wifiManager, metricsFeatureProvider, new IconInjector(context2));
    }

    @VisibleForTesting
    WifiDetailPreferenceController(AccessPoint accessPoint, ConnectivityManager connectivityManager, Context context, Fragment fragment, Handler handler, Lifecycle lifecycle, WifiManager wifiManager, MetricsFeatureProvider metricsFeatureProvider, IconInjector injector) {
        super(context);
        this.mAccessPoint = accessPoint;
        this.mConnectivityManager = connectivityManager;
        this.mFragment = fragment;
        this.mHandler = handler;
        this.mSignalStr = context.getResources().getStringArray(R.array.wifi_signal);
        this.mWifiConfig = accessPoint.getConfig();
        this.mWifiManager = wifiManager;
        this.mMetricsFeatureProvider = metricsFeatureProvider;
        this.mIconInjector = injector;
        this.mFilter = new IntentFilter();
        this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        lifecycle.addObserver(this);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return null;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        setupEntityHeader(screen);
        this.mButtonsPref = ((ActionButtonPreference) screen.findPreference(KEY_BUTTONS_PREF)).setButton1Text(R.string.forget).setButton1Positive(false).setButton1OnClickListener(new -$$Lambda$WifiDetailPreferenceController$HDOTYXVF80U7sCZa22KqorlzriY(this)).setButton2Text(R.string.wifi_sign_in_button_text).setButton2Positive(true).setButton2OnClickListener(new -$$Lambda$WifiDetailPreferenceController$PxMNywf_HXiVAESmLubuiIo869s(this));
        this.mSignalStrengthPref = (WifiDetailPreference) screen.findPreference(KEY_SIGNAL_STRENGTH_PREF);
        this.mLinkSpeedPref = (WifiDetailPreference) screen.findPreference(KEY_LINK_SPEED);
        this.mFrequencyPref = (WifiDetailPreference) screen.findPreference(KEY_FREQUENCY_PREF);
        this.mSecurityPref = (WifiDetailPreference) screen.findPreference(KEY_SECURITY_PREF);
        this.mMacAddressPref = (WifiDetailPreference) screen.findPreference(KEY_MAC_ADDRESS_PREF);
        this.mIpAddressPref = (WifiDetailPreference) screen.findPreference(KEY_IP_ADDRESS_PREF);
        this.mGatewayPref = (WifiDetailPreference) screen.findPreference(KEY_GATEWAY_PREF);
        this.mSubnetPref = (WifiDetailPreference) screen.findPreference(KEY_SUBNET_MASK_PREF);
        this.mDnsPref = (WifiDetailPreference) screen.findPreference(KEY_DNS_PREF);
        this.mIpv6Category = (PreferenceCategory) screen.findPreference(KEY_IPV6_CATEGORY);
        this.mIpv6AddressPref = screen.findPreference(KEY_IPV6_ADDRESSES_PREF);
        this.mSecurityPref.setDetailText(this.mAccessPoint.getSecurityString(false));
    }

    private void setupEntityHeader(PreferenceScreen screen) {
        LayoutPreference headerPref = (LayoutPreference) screen.findPreference(KEY_HEADER);
        this.mEntityHeaderController = EntityHeaderController.newInstance(this.mFragment.getActivity(), this.mFragment, headerPref.findViewById(R.id.entity_header));
        ImageView iconView = (ImageView) headerPref.findViewById(R.id.entity_header_icon);
        iconView.setBackground(this.mContext.getDrawable(R.drawable.ic_settings_widget_background));
        iconView.setScaleType(ScaleType.CENTER_INSIDE);
        this.mEntityHeaderController.setLabel(this.mAccessPoint.getSsidStr());
    }

    public void onResume() {
        this.mNetwork = this.mWifiManager.getCurrentNetwork();
        this.mLinkProperties = this.mConnectivityManager.getLinkProperties(this.mNetwork);
        this.mNetworkCapabilities = this.mConnectivityManager.getNetworkCapabilities(this.mNetwork);
        updateInfo();
        this.mContext.registerReceiver(this.mReceiver, this.mFilter);
        this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback, this.mHandler);
    }

    public void onPause() {
        this.mNetwork = null;
        this.mLinkProperties = null;
        this.mNetworkCapabilities = null;
        this.mNetworkInfo = null;
        this.mWifiInfo = null;
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
    }

    private void updateInfo() {
        this.mNetworkInfo = this.mConnectivityManager.getNetworkInfo(this.mNetwork);
        this.mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (this.mNetwork == null || this.mNetworkInfo == null || this.mWifiInfo == null) {
            exitActivity();
            return;
        }
        this.mButtonsPref.setButton1Visible(canForgetNetwork());
        refreshNetworkState();
        refreshRssiViews();
        this.mMacAddressPref.setDetailText(this.mWifiInfo.getMacAddress());
        this.mLinkSpeedPref.setVisible(this.mWifiInfo.getLinkSpeed() >= 0);
        this.mLinkSpeedPref.setDetailText(this.mContext.getString(R.string.link_speed, new Object[]{Integer.valueOf(this.mWifiInfo.getLinkSpeed())}));
        int frequency = this.mWifiInfo.getFrequency();
        String band = null;
        if (frequency >= AccessPoint.LOWER_FREQ_24GHZ && frequency < AccessPoint.HIGHER_FREQ_24GHZ) {
            band = this.mContext.getResources().getString(R.string.wifi_band_24ghz);
        } else if (frequency < AccessPoint.LOWER_FREQ_5GHZ || frequency >= AccessPoint.HIGHER_FREQ_5GHZ) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Unexpected frequency ");
            stringBuilder.append(frequency);
            Log.e(str, stringBuilder.toString());
        } else {
            band = this.mContext.getResources().getString(R.string.wifi_band_5ghz);
        }
        this.mFrequencyPref.setDetailText(band);
        updateIpLayerInfo();
    }

    private void exitActivity() {
        if (DEBUG) {
            Log.d(TAG, "Exiting the WifiNetworkDetailsPage");
        }
        this.mFragment.getActivity().finish();
    }

    private void refreshNetworkState() {
        this.mAccessPoint.update(this.mWifiConfig, this.mWifiInfo, this.mNetworkInfo);
        this.mEntityHeaderController.setSummary(this.mAccessPoint.getSettingsSummary()).done(this.mFragment.getActivity(), true);
    }

    private void refreshRssiViews() {
        int signalLevel = this.mAccessPoint.getLevel();
        if (this.mRssiSignalLevel != signalLevel) {
            this.mRssiSignalLevel = signalLevel;
            Drawable wifiIcon = this.mIconInjector.getIcon(this.mRssiSignalLevel);
            wifiIcon.setTint(Utils.getColorAccent(this.mContext));
            this.mEntityHeaderController.setIcon(wifiIcon).done(this.mFragment.getActivity(), true);
            Drawable wifiIconDark = wifiIcon.getConstantState().newDrawable().mutate();
            wifiIconDark.setTint(this.mContext.getResources().getColor(R.color.oneplus_contorl_icon_color_active_default));
            this.mSignalStrengthPref.setIcon(wifiIconDark);
            this.mSignalStrengthPref.setDetailText(this.mSignalStr[this.mRssiSignalLevel]);
        }
    }

    private void updatePreference(WifiDetailPreference pref, String detailText) {
        if (TextUtils.isEmpty(detailText)) {
            pref.setVisible(false);
            return;
        }
        pref.setDetailText(detailText);
        pref.setVisible(true);
    }

    private void updateIpLayerInfo() {
        this.mButtonsPref.setButton2Visible(canSignIntoNetwork());
        ActionButtonPreference actionButtonPreference = this.mButtonsPref;
        boolean z = canSignIntoNetwork() || canForgetNetwork();
        actionButtonPreference.setVisible(z);
        if (this.mNetwork == null || this.mLinkProperties == null) {
            this.mIpAddressPref.setVisible(false);
            this.mSubnetPref.setVisible(false);
            this.mGatewayPref.setVisible(false);
            this.mDnsPref.setVisible(false);
            this.mIpv6Category.setVisible(false);
            return;
        }
        String ipv4Address = null;
        String subnet = null;
        StringJoiner ipv6Addresses = new StringJoiner("\n");
        for (LinkAddress addr : this.mLinkProperties.getLinkAddresses()) {
            if (addr.getAddress() instanceof Inet4Address) {
                ipv4Address = addr.getAddress().getHostAddress();
                subnet = ipv4PrefixLengthToSubnetMask(addr.getPrefixLength());
            } else if (addr.getAddress() instanceof Inet6Address) {
                ipv6Addresses.add(addr.getAddress().getHostAddress());
            }
        }
        String gateway = null;
        for (RouteInfo routeInfo : this.mLinkProperties.getRoutes()) {
            if (routeInfo.isIPv4Default() && routeInfo.hasGateway()) {
                gateway = routeInfo.getGateway().getHostAddress();
                break;
            }
        }
        String dnsServers = (String) this.mLinkProperties.getDnsServers().stream().map(-$$Lambda$WifiDetailPreferenceController$XZAGhHrbkIDyusER4MAM6luKcT0.INSTANCE).collect(Collectors.joining("\n"));
        updatePreference(this.mIpAddressPref, ipv4Address);
        updatePreference(this.mSubnetPref, subnet);
        updatePreference(this.mGatewayPref, gateway);
        updatePreference(this.mDnsPref, dnsServers);
        if (ipv6Addresses.length() > 0) {
            this.mIpv6AddressPref.setSummary(BidiFormatter.getInstance().unicodeWrap(ipv6Addresses.toString()));
            this.mIpv6Category.setVisible(true);
        } else {
            this.mIpv6Category.setVisible(false);
        }
    }

    /* JADX WARNING: Incorrect type for fill-array insn 0x0003, element type: byte, insn element type: null */
    private static java.lang.String ipv4PrefixLengthToSubnetMask(int r2) {
        /*
        r0 = 4;
        r0 = new byte[r0];	 Catch:{ UnknownHostException -> 0x0013 }
        r0 = {-1, -1, -1, -1};	 Catch:{ UnknownHostException -> 0x0013 }
        r0 = java.net.InetAddress.getByAddress(r0);	 Catch:{ UnknownHostException -> 0x0013 }
        r1 = android.net.NetworkUtils.getNetworkPart(r0, r2);	 Catch:{ UnknownHostException -> 0x0013 }
        r1 = r1.getHostAddress();	 Catch:{ UnknownHostException -> 0x0013 }
        return r1;
    L_0x0013:
        r0 = move-exception;
        r1 = 0;
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.wifi.details.WifiDetailPreferenceController.ipv4PrefixLengthToSubnetMask(int):java.lang.String");
    }

    private boolean canForgetNetwork() {
        return (this.mWifiInfo != null && this.mWifiInfo.isEphemeral()) || canModifyNetwork();
    }

    public boolean canModifyNetwork() {
        return (this.mWifiConfig == null || WifiUtils.isNetworkLockedDown(this.mContext, this.mWifiConfig)) ? false : true;
    }

    private boolean canSignIntoNetwork() {
        return WifiUtils.canSignIntoNetwork(this.mNetworkCapabilities);
    }

    private void forgetNetwork() {
        String str;
        StringBuilder stringBuilder;
        if (this.mWifiInfo != null && this.mWifiInfo.isEphemeral()) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("forgetNetwork mWifiInfo.isEphemeral() = ");
            stringBuilder.append(this.mWifiInfo.isEphemeral());
            Log.w(str, stringBuilder.toString());
            this.mWifiManager.disableEphemeralNetwork(this.mWifiInfo.getSSID());
        } else if (this.mWifiConfig != null) {
            if (this.mWifiConfig.isPasspoint()) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("forgetNetwork mWifiConfig.isPasspoint() = ");
                stringBuilder.append(this.mWifiConfig.isPasspoint());
                Log.w(str, stringBuilder.toString());
                this.mWifiManager.removePasspointConfiguration(this.mWifiConfig.FQDN);
            } else {
                Log.w(TAG, "forgetNetwork  null");
                this.mWifiManager.forget(this.mWifiConfig.networkId, null);
            }
        }
        this.mMetricsFeatureProvider.action(this.mFragment.getActivity(), (int) Const.CODE_C1_DSW, new Pair[0]);
        this.mFragment.getActivity().finish();
    }

    private void signIntoNetwork() {
        this.mMetricsFeatureProvider.action(this.mFragment.getActivity(), (int) PointerIconCompat.TYPE_TEXT, new Pair[0]);
        this.mConnectivityManager.startCaptivePortalApp(this.mNetwork);
    }

    public void onForget(WifiDialog dialog) {
    }

    public void onSubmit(WifiDialog dialog) {
        if (dialog.getController() != null) {
            this.mWifiManager.save(dialog.getController().getConfig(), new ActionListener() {
                public void onSuccess() {
                }

                public void onFailure(int reason) {
                    Activity activity = WifiDetailPreferenceController.this.mFragment.getActivity();
                    if (activity != null) {
                        Toast.makeText(activity, R.string.wifi_failed_save_message, 0).show();
                    }
                }
            });
        }
    }
}
