package com.android.settings.network;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.network.MobilePlanPreferenceController.MobilePlanPreferenceHost;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.wifi.WifiMasterSwitchPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.controllers.OPRoamingControlPreferenceController;
import com.oneplus.settings.controllers.OPWiFiCallingPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkDashboardFragment extends DashboardFragment implements MobilePlanPreferenceHost {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.network_and_internet;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return NetworkDashboardFragment.buildPreferenceControllers(context, null, null, null, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(WifiMasterSwitchPreferenceController.KEY_TOGGLE_WIFI);
            return keys;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static final String TAG = "NetworkDashboardFrag";

    @VisibleForTesting
    static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;
        private final TetherPreferenceController mTetherPreferenceController;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this(context, summaryLoader, new TetherPreferenceController(context, null));
        }

        @VisibleForTesting(otherwise = 5)
        SummaryProvider(Context context, SummaryLoader summaryLoader, TetherPreferenceController tetherPreferenceController) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
            this.mTetherPreferenceController = tetherPreferenceController;
        }

        public void setListening(boolean listening) {
            if (listening) {
                String summary = this.mContext.getString(R.string.oneplus_simcard_title);
                String mobileNetworkSummary = this.mContext.getString(R.string.network_settings_title).toLowerCase();
                summary = this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, mobileNetworkSummary});
                String dataUsageSettingSummary = this.mContext.getString(R.string.network_dashboard_summary_data_usage);
                summary = this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, dataUsageSettingSummary});
                if (this.mTetherPreferenceController.isAvailable()) {
                    String hotspotSettingSummary = this.mContext.getString(R.string.network_dashboard_summary_hotspot);
                    summary = this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, hotspotSettingSummary});
                }
                this.mSummaryLoader.setSummary(this, summary);
            }
        }
    }

    public int getMetricsCategory() {
        return 746;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.network_and_internet;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((AirplaneModePreferenceController) use(AirplaneModePreferenceController.class)).setFragment(this);
    }

    public int getHelpResource() {
        return R.string.help_url_network_dashboard;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle(), this.mMetricsFeatureProvider, this, this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle, MetricsFeatureProvider metricsFeatureProvider, Fragment fragment, MobilePlanPreferenceHost mobilePlanHost) {
        MobilePlanPreferenceController mobilePlanPreferenceController = new MobilePlanPreferenceController(context, mobilePlanHost);
        WifiMasterSwitchPreferenceController wifiPreferenceController = new WifiMasterSwitchPreferenceController(context, metricsFeatureProvider);
        VpnPreferenceController vpnPreferenceController = new VpnPreferenceController(context);
        PrivateDnsPreferenceController privateDnsPreferenceController = new PrivateDnsPreferenceController(context);
        OPWiFiCallingPreferenceController mOPWiFiCallingPreferenceController = new OPWiFiCallingPreferenceController(context);
        OPRoamingControlPreferenceController mOPRoamingControlPreferenceController = new OPRoamingControlPreferenceController(context);
        if (lifecycle != null) {
            lifecycle.addObserver(mobilePlanPreferenceController);
            lifecycle.addObserver(wifiPreferenceController);
            lifecycle.addObserver(vpnPreferenceController);
            lifecycle.addObserver(privateDnsPreferenceController);
            lifecycle.addObserver(mOPWiFiCallingPreferenceController);
            lifecycle.addObserver(mOPRoamingControlPreferenceController);
        }
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new TetherPreferenceController(context, lifecycle));
        controllers.add(vpnPreferenceController);
        controllers.add(mOPWiFiCallingPreferenceController);
        controllers.add(mOPRoamingControlPreferenceController);
        controllers.add(new ProxyPreferenceController(context));
        controllers.add(mobilePlanPreferenceController);
        controllers.add(wifiPreferenceController);
        controllers.add(privateDnsPreferenceController);
        return controllers;
    }

    public void showMobilePlanMessageDialog() {
        showDialog(1);
    }

    public Dialog onCreateDialog(int dialogId) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onCreateDialog: dialogId=");
        stringBuilder.append(dialogId);
        Log.d(str, stringBuilder.toString());
        if (dialogId != 1) {
            return super.onCreateDialog(dialogId);
        }
        MobilePlanPreferenceController controller = (MobilePlanPreferenceController) use(MobilePlanPreferenceController.class);
        return new Builder(getActivity()).setMessage(controller.getMobilePlanDialogMessage()).setCancelable(false).setPositiveButton(17039370, new -$$Lambda$NetworkDashboardFragment$ezC2Ol_SOf4CDiS8HjkkdWzGu_s(controller)).create();
    }

    public int getDialogMetricsCategory(int dialogId) {
        if (1 == dialogId) {
            return 609;
        }
        return 0;
    }
}
