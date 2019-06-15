package com.android.settings.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppData;
import android.net.wifi.WifiManager;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.wifi.p2p.WifiP2pPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigureWifiSettings extends DashboardFragment {
    public static final String KEY_IP_ADDRESS = "current_ip_address";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.wifi_configure_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (info == null || info.getType() == 1) {
                keys.add(ConfigureWifiSettings.KEY_IP_ADDRESS);
            }
            if (!checkForFeatureSupportedScorers(context)) {
                keys.add(UseOpenWifiPreferenceController.KEY_USE_OPEN_WIFI_AUTOMATICALLY);
            }
            return keys;
        }

        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return context.getResources().getBoolean(R.bool.config_show_wifi_settings);
        }

        private boolean checkForFeatureSupportedScorers(Context context) {
            NetworkScoreManager mNetworkScoreManager = (NetworkScoreManager) context.getSystemService("network_score");
            NetworkScorerAppData appData = mNetworkScoreManager.getActiveScorer();
            if ((appData == null ? null : appData.getEnableUseOpenWifiActivity()) != null) {
                return true;
            }
            for (NetworkScorerAppData scorer : mNetworkScoreManager.getAllValidScorers()) {
                if (scorer.getEnableUseOpenWifiActivity() != null) {
                    return true;
                }
            }
            return false;
        }
    };
    private static final String TAG = "ConfigureWifiSettings";
    public static final int WIFI_WAKEUP_REQUEST_CODE = 600;
    private UseOpenWifiPreferenceController mUseOpenWifiPreferenceController;
    private WifiWakeupPreferenceController mWifiWakeupPreferenceController;

    public int getMetricsCategory() {
        return 338;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getInitialExpandedChildCount() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.wifi_configure_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        this.mWifiWakeupPreferenceController = new WifiWakeupPreferenceController(context, this, getLifecycle());
        this.mUseOpenWifiPreferenceController = new UseOpenWifiPreferenceController(context, this, getLifecycle());
        WifiManager wifiManager = (WifiManager) getSystemService("wifi");
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(this.mWifiWakeupPreferenceController);
        controllers.add(new OPNetworkAutoChangePreferenceController(context, getLifecycle()));
        controllers.add(new OPWifiScanAlwaysAvailablePreferenceController(context, getLifecycle()));
        controllers.add(new NotifyOpenNetworksPreferenceController(context, getLifecycle()));
        controllers.add(this.mUseOpenWifiPreferenceController);
        controllers.add(new OPWapiCertManagePreferenceController(context));
        controllers.add(new WifiInfoPreferenceController(context, getLifecycle(), wifiManager));
        controllers.add(new CellularFallbackPreferenceController(context));
        controllers.add(new WifiP2pPreferenceController(context, getLifecycle(), wifiManager));
        return controllers;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WIFI_WAKEUP_REQUEST_CODE && this.mWifiWakeupPreferenceController != null) {
            this.mWifiWakeupPreferenceController.onActivityResult(requestCode, resultCode);
        } else if (requestCode != 400 || this.mUseOpenWifiPreferenceController == null) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            this.mUseOpenWifiPreferenceController.onActivityResult(requestCode, resultCode);
        }
    }
}
