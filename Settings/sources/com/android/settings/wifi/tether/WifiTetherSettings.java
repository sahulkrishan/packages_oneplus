package com.android.settings.wifi.tether;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.PointerIconCompat;
import android.util.Log;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBarController;
import com.android.settings.wifi.WifiUtils;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController.OnTetherConfigUpdateListener;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.settings.wifi.tether.OPWifiTetherApBandPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WifiTetherSettings extends RestrictedDashboardFragment implements OnTetherConfigUpdateListener {
    private static final String KEY_WIFI_TETHER_AUTO_OFF = "wifi_tether_auto_turn_off";
    private static final int MSG_WHAT_START_TETHER = 1;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.wifi_tether_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = super.getNonIndexableKeys(context);
            if (isConnManagerEnable(context)) {
                result.add(OPWifiTetherDeviceManagerController.PREF_KEY);
            }
            if (OPUtils.isGuestMode()) {
                result.add("wifi_tether_network_name");
                result.add("wifi_tether_security");
                result.add("wifi_tether_network_password");
                result.add("wifi_tether_network_ap_band");
                result.add("wifi_tether_security");
                result.add(WifiTetherSettings.KEY_WIFI_TETHER_AUTO_OFF);
                result.add("wifi_tether_custom_auto_turn_off");
                result.add("wifi_tether_network_ap_band_single_select");
                result.add(OPWifiTetherDeviceManagerController.PREF_KEY);
            }
            return result;
        }

        private boolean isConnManagerEnable(Context context) {
            WifiManager mWifiManager = (WifiManager) context.getSystemService("wifi");
            return OPUtils.isAppPakExist(context, "com.oneplus.wifiapsettings") && mWifiManager != null && mWifiManager.getWifiApState() == 13;
        }
    };
    private static final String TAG = "WifiTetherSettings";
    private static final IntentFilter TETHER_STATE_CHANGE_FILTER = new IntentFilter("android.net.conn.TETHER_STATE_CHANGED");
    private WifiTetherApBandPreferenceController mApBandPreferenceController;
    private OPWifiTetherDeviceManagerController mConnectedDeviceManagerController;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                WifiTetherSettings.this.startTether();
            }
        }
    };
    private OPWifiTetherApBandPreferenceController mOPApBandPreferenceController;
    private OPWifiTetherCustomAutoTurnOffPreferenceController mOPWifiTetherCustomAutoTurnOffPreferenceController;
    private WifiTetherPasswordPreferenceController mPasswordPreferenceController;
    private boolean mRestartWifiApAfterConfigChange;
    private WifiTetherSSIDPreferenceController mSSIDPreferenceController;
    private WifiTetherSecurityPreferenceController mSecurityPreferenceController;
    private WifiTetherSwitchBarController mSwitchBarController;
    @VisibleForTesting
    TetherChangeReceiver mTetherChangeReceiver;
    private WifiManager mWifiManager;

    @VisibleForTesting
    class TetherChangeReceiver extends BroadcastReceiver {
        TetherChangeReceiver() {
        }

        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            String str = WifiTetherSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updating display config due to receiving broadcast action ");
            stringBuilder.append(action);
            Log.d(str, stringBuilder.toString());
            WifiTetherSettings.this.updateDisplayWithNewConfig();
            if (action.equals("android.net.conn.TETHER_STATE_CHANGED")) {
                if (WifiTetherSettings.this.mWifiManager.getWifiApState() == 11 && WifiTetherSettings.this.mRestartWifiApAfterConfigChange) {
                    WifiTetherSettings.this.mHandler.removeMessages(1);
                    WifiTetherSettings.this.mHandler.sendEmptyMessageDelayed(1, 200);
                }
            } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                int state = intent.getIntExtra("wifi_state", 0);
                if (WifiTetherSettings.this.mConnectedDeviceManagerController != null) {
                    WifiTetherSettings.this.mConnectedDeviceManagerController.updateDisplay();
                }
                if (state == 11 && WifiTetherSettings.this.mRestartWifiApAfterConfigChange) {
                    WifiTetherSettings.this.mHandler.removeMessages(1);
                    WifiTetherSettings.this.mHandler.sendEmptyMessageDelayed(1, 200);
                }
            }
        }
    }

    static {
        TETHER_STATE_CHANGE_FILTER.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
    }

    public WifiTetherSettings() {
        super("no_config_tethering");
    }

    public int getMetricsCategory() {
        return PointerIconCompat.TYPE_HORIZONTAL_DOUBLE_ARROW;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mTetherChangeReceiver = new TetherChangeReceiver();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SettingsActivity activity = (SettingsActivity) getActivity();
        SwitchBar switchBar = activity.getSwitchBar();
        this.mSwitchBarController = new WifiTetherSwitchBarController(activity, new SwitchBarController(switchBar));
        getLifecycle().addObserver(this.mSwitchBarController);
        switchBar.show();
    }

    public void onStart() {
        super.onStart();
        Context context = getContext();
        if (context != null) {
            context.registerReceiver(this.mTetherChangeReceiver, TETHER_STATE_CHANGE_FILTER);
        }
    }

    public void onStop() {
        super.onStop();
        Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(this.mTetherChangeReceiver);
        }
        this.mHandler.removeMessages(1);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.wifi_tether_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mSSIDPreferenceController = new WifiTetherSSIDPreferenceController(context, this);
        this.mSecurityPreferenceController = new WifiTetherSecurityPreferenceController(context, this);
        this.mPasswordPreferenceController = new WifiTetherPasswordPreferenceController(context, this);
        this.mApBandPreferenceController = new WifiTetherApBandPreferenceController(context, this);
        this.mOPApBandPreferenceController = new OPWifiTetherApBandPreferenceController(context, this);
        this.mConnectedDeviceManagerController = new OPWifiTetherDeviceManagerController(context, this);
        this.mOPWifiTetherCustomAutoTurnOffPreferenceController = new OPWifiTetherCustomAutoTurnOffPreferenceController(context);
        controllers.add(this.mSSIDPreferenceController);
        controllers.add(this.mSecurityPreferenceController);
        controllers.add(this.mPasswordPreferenceController);
        controllers.add(this.mApBandPreferenceController);
        controllers.add(this.mOPApBandPreferenceController);
        controllers.add(this.mConnectedDeviceManagerController);
        controllers.add(this.mOPWifiTetherCustomAutoTurnOffPreferenceController);
        controllers.add(new WifiTetherAutoOffPreferenceController(context, KEY_WIFI_TETHER_AUTO_OFF));
        return controllers;
    }

    public void onTetherConfigUpdated() {
        final WifiConfiguration config = buildNewConfig();
        this.mPasswordPreferenceController.updateVisibility(config.getAuthType());
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (WifiTetherSettings.this.mWifiManager.getWifiApState() == 13) {
                    Log.d("TetheringSettings", "Wifi AP config changed while enabled, stop and restart");
                    WifiTetherSettings.this.mRestartWifiApAfterConfigChange = true;
                    WifiTetherSettings.this.mSwitchBarController.stopTether();
                }
                WifiTetherSettings.this.mWifiManager.setWifiApConfiguration(config);
            }
        }, 100);
    }

    private WifiConfiguration buildNewConfig() {
        WifiConfiguration config = new WifiConfiguration();
        int securityType = this.mSecurityPreferenceController.getSecurityType();
        config.SSID = this.mSSIDPreferenceController.getSSID();
        config.allowedKeyManagement.set(securityType);
        config.preSharedKey = this.mPasswordPreferenceController.getPasswordValidated(securityType);
        config.allowedAuthAlgorithms.set(0);
        if (WifiUtils.isSupportDualBand()) {
            config.apBand = this.mApBandPreferenceController.getBandIndex();
        } else {
            config.apBand = this.mOPApBandPreferenceController.getBandIndex();
        }
        if (OpFeatures.isSupport(new int[]{85})) {
            if (Secure.getIntForUser(getContext().getContentResolver(), "oneplus_is_broadcat_wifi_name", 0, -2) == 0) {
                config.hiddenSSID = true;
            } else {
                config.hiddenSSID = false;
            }
        }
        return config;
    }

    private void startTether() {
        Log.d(TAG, "startTether");
        this.mRestartWifiApAfterConfigChange = false;
        this.mSwitchBarController.startTether();
    }

    private void updateDisplayWithNewConfig() {
        ((WifiTetherSSIDPreferenceController) use(WifiTetherSSIDPreferenceController.class)).updateDisplay();
        ((WifiTetherSecurityPreferenceController) use(WifiTetherSecurityPreferenceController.class)).updateDisplay();
        ((WifiTetherPasswordPreferenceController) use(WifiTetherPasswordPreferenceController.class)).updateDisplay();
        ((WifiTetherApBandPreferenceController) use(WifiTetherApBandPreferenceController.class)).updateDisplay();
    }
}
