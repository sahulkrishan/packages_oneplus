package com.android.settings.connecteddevice;

import android.app.Fragment;
import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultPaymentSettingsPreferenceController;
import com.android.settings.bluetooth.BluetoothFilesPreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.nfc.AndroidBeamPreferenceController;
import com.android.settings.print.PrintSettingPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancedConnectedDeviceDashboardFragment extends DashboardFragment {
    static final String KEY_BLUETOOTH = "bluetooth_settings";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.connected_devices_advanced;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            if (!context.getPackageManager().hasSystemFeature("android.hardware.nfc")) {
                keys.add(AndroidBeamPreferenceController.KEY_ANDROID_BEAM_SETTINGS);
            }
            keys.add(AdvancedConnectedDeviceDashboardFragment.KEY_BLUETOOTH);
            return keys;
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return AdvancedConnectedDeviceDashboardFragment.buildControllers(context, null, null);
        }
    };
    private static final String TAG = "AdvancedConnectedDeviceFrag";

    public int getMetricsCategory() {
        return 1264;
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
        return R.xml.connected_devices_advanced;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildControllers(context, getLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildControllers(Context context, Lifecycle lifecycle, Fragment fragment) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new BluetoothFilesPreferenceController(context));
        controllers.add(new BluetoothOnWhileDrivingPreferenceController(context));
        DefaultPaymentSettingsPreferenceController pspc = new DefaultPaymentSettingsPreferenceController(context);
        if (fragment != null) {
            pspc.setFragment(fragment);
        }
        controllers.add(pspc);
        PrintSettingPreferenceController printerController = new PrintSettingPreferenceController(context);
        if (lifecycle != null) {
            lifecycle.addObserver(printerController);
        }
        controllers.add(printerController);
        return controllers;
    }
}
