package com.android.settings.display;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.Arrays;
import java.util.List;

public class AutoBrightnessSettings extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.auto_brightness_detail;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private static final String TAG = "AutoBrightnessSettings";

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.auto_brightness_description);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.auto_brightness_detail;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getMetricsCategory() {
        return 1381;
    }

    public int getHelpResource() {
        return R.string.help_url_auto_brightness;
    }
}
