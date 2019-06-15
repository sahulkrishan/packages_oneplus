package com.android.settings.gestures;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwipeToNotificationSettings extends DashboardFragment {
    public static final String PREF_KEY_SUGGESTION_COMPLETE = "pref_swipe_to_notification_suggestion_complete";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.swipe_to_notification_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = new ArrayList();
            result.add("gesture_swipe_down_fingerprint_screen");
            result.add("gesture_swipe_down_fingerprint");
            return result;
        }
    };
    private static final String TAG = "SwipeToNotifSettings";

    public void onAttach(Context context) {
        super.onAttach(context);
        FeatureFactory.getFactory(context).getSuggestionFeatureProvider(context).getSharedPrefs(context).edit().putBoolean(PREF_KEY_SUGGESTION_COMPLETE, true).apply();
    }

    public int getMetricsCategory() {
        return 751;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.swipe_to_notification_settings;
    }
}
