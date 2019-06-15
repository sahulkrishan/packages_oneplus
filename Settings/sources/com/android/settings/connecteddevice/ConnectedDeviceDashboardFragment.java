package com.android.settings.connecteddevice;

import android.app.Activity;
import android.content.Context;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.bluetooth.OPBluetoothDiscoverablePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.utils.OPPreferenceDividerLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnectedDeviceDashboardFragment extends DashboardFragment {
    @VisibleForTesting
    static final String KEY_AVAILABLE_DEVICES = "available_device_list";
    @VisibleForTesting
    static final String KEY_CONNECTED_DEVICES = "connected_device_list";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.connected_devices;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ConnectedDeviceDashboardFragment.buildPreferenceControllers(context, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(ConnectedDeviceDashboardFragment.KEY_AVAILABLE_DEVICES);
            keys.add(ConnectedDeviceDashboardFragment.KEY_CONNECTED_DEVICES);
            return keys;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static final String TAG = "ConnectedDeviceFrag";

    @VisibleForTesting
    static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mSummaryLoader.setSummary(this, AdvancedConnectedDeviceController.getDeviceConnetionSummaryString(this.mContext));
            }
        }
    }

    public int getMetricsCategory() {
        return 747;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getHelpResource() {
        return R.string.help_url_connected_devices;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.connected_devices;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        DiscoverableFooterPreferenceController discoverableFooterPreferenceController = new DiscoverableFooterPreferenceController(context);
        controllers.add(discoverableFooterPreferenceController);
        controllers.add(new OPBluetoothDiscoverablePreferenceController(context, lifecycle));
        OPPreferenceDividerLine mOPPreferenceDividerLine = new OPPreferenceDividerLine(context);
        controllers.add(mOPPreferenceDividerLine);
        if (lifecycle != null) {
            lifecycle.addObserver(discoverableFooterPreferenceController);
            lifecycle.addObserver(mOPPreferenceDividerLine);
        }
        return controllers;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((AvailableMediaDeviceGroupController) use(AvailableMediaDeviceGroupController.class)).init(this);
        ((ConnectedDeviceGroupController) use(ConnectedDeviceGroupController.class)).init(this);
        ((PreviouslyConnectedDevicePreferenceController) use(PreviouslyConnectedDevicePreferenceController.class)).init(this);
        ((DiscoverableFooterPreferenceController) use(DiscoverableFooterPreferenceController.class)).init(this);
    }
}
