package com.android.settings.accessibility;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.List;

public class VibrationSettings extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> indexables = new ArrayList();
            SearchIndexableResource indexable = new SearchIndexableResource(context);
            indexable.xmlResId = R.xml.accessibility_vibration_settings;
            indexables.add(indexable);
            return indexables;
        }
    };
    private static final String TAG = "VibrationSettings";

    public int getMetricsCategory() {
        return 1292;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_vibration_settings;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }
}
