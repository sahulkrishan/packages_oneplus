package com.android.settings.gestures;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.Arrays;
import java.util.List;

public class PreventRingingGestureSettings extends DashboardFragment {
    private static final String KEY_PREVENT_RINGING = "gesture_prevent_ringing";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.prevent_ringing_gesture_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private static final String TAG = "RingingGestureSettings";

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public int getMetricsCategory() {
        return 1360;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.prevent_ringing_gesture_settings;
    }

    public int getHelpResource() {
        return 0;
    }
}
