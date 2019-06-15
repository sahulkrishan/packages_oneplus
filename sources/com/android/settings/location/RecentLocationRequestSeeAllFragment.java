package com.android.settings.location;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecentLocationRequestSeeAllFragment extends DashboardFragment {
    public static final String PATH = "com.android.settings.location.RecentLocationRequestSeeAllFragment";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.location_recent_requests_see_all;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> getPreferenceControllers(Context context) {
            return RecentLocationRequestSeeAllFragment.buildPreferenceControllers(context, null, null);
        }
    };
    private static final String TAG = "RecentLocationReqAll";

    public int getMetricsCategory() {
        return 1325;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.location_recent_requests_see_all;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle, RecentLocationRequestSeeAllFragment fragment) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new RecentLocationRequestSeeAllPreferenceController(context, lifecycle, fragment));
        return controllers;
    }
}
