package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.provider.SearchIndexableResource;
import android.support.annotation.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.deviceinfo.firmwareversion.FirmwareVersionPreferenceController;
import com.android.settings.deviceinfo.imei.ImeiInfoPreferenceController;
import com.android.settings.deviceinfo.simstatus.SimStatusPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceInfoSettings extends DashboardFragment implements Indexable {
    private static final String KEY_LEGAL_CONTAINER = "legal_container";
    private static final String LOG_TAG = "DeviceInfoSettings";
    @VisibleForTesting
    static final int NON_SIM_PREFERENCES_COUNT = 2;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.device_info_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return DeviceInfoSettings.buildPreferenceControllers(context, null, null, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(DeviceInfoSettings.KEY_LEGAL_CONTAINER);
            return keys;
        }
    };
    @VisibleForTesting
    static final int SIM_PREFERENCES_COUNT = 3;
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(summaryLoader);
        }
    };

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(SummaryLoader summaryLoader) {
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mSummaryLoader.setSummary(this, DeviceModelPreferenceController.getDeviceModel());
            }
        }
    }

    public int getMetricsCategory() {
        return 40;
    }

    public int getHelpResource() {
        return R.string.help_uri_about;
    }

    public int getInitialExpandedChildCount() {
        return 0;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!((BuildNumberPreferenceController) use(BuildNumberPreferenceController.class)).onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return LOG_TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.device_info_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getActivity(), this, getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Activity activity, Fragment fragment, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new PhoneNumberPreferenceController(context));
        controllers.add(new SimStatusPreferenceController(context, fragment));
        controllers.add(new DeviceModelPreferenceController(context, fragment));
        controllers.add(new ImeiInfoPreferenceController(context, fragment));
        controllers.add(new FirmwareVersionPreferenceController(context, fragment));
        controllers.add(new IpAddressPreferenceController(context, lifecycle));
        controllers.add(new WifiMacAddressPreferenceController(context, lifecycle));
        controllers.add(new BluetoothAddressPreferenceController(context, lifecycle));
        controllers.add(new RegulatoryInfoPreferenceController(context));
        controllers.add(new SafetyInfoPreferenceController(context));
        controllers.add(new ManualPreferenceController(context));
        controllers.add(new FeedbackPreferenceController(fragment, context));
        controllers.add(new FccEquipmentIdPreferenceController(context));
        controllers.add(new BuildNumberPreferenceController(context, activity, fragment, lifecycle));
        return controllers;
    }
}
