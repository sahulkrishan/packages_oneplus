package com.android.settings.gestures;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.Arrays;
import java.util.List;

public class DoubleTapScreenSettings extends DashboardFragment {
    public static final String PREF_KEY_SUGGESTION_COMPLETE = "pref_double_tap_screen_suggestion_complete";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.double_tap_screen_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private static final String TAG = "DoubleTapScreen";

    public void onAttach(Context context) {
        super.onAttach(context);
        FeatureFactory.getFactory(context).getSuggestionFeatureProvider(context).getSharedPrefs(context).edit().putBoolean(PREF_KEY_SUGGESTION_COMPLETE, true).apply();
        ((DoubleTapScreenPreferenceController) use(DoubleTapScreenPreferenceController.class)).setConfig(new AmbientDisplayConfiguration(context));
    }

    public int getMetricsCategory() {
        return 754;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.double_tap_screen_settings;
    }

    public int getHelpResource() {
        return R.string.help_url_double_tap_screen;
    }
}
