package com.android.settings.location;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScanningSettings extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.location_scanning;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ScanningSettings.buildPreferenceControllers(context);
        }
    };
    private static final String TAG = "ScanningSettings";

    public int getMetricsCategory() {
        return Const.CODE_C1_CW3;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.location_scanning;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new WifiScanningPreferenceController(context));
        controllers.add(new BluetoothScanningPreferenceController(context));
        return controllers;
    }
}
