package com.android.settings.support;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.overlay.SupportFeatureProvider;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.List;

public class SupportDashboardActivity extends Activity implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        private static final String SUPPORT_SEARCH_INDEX_KEY = "support_dashboard_activity";

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.page_tab_title_support);
            data.screenTitle = context.getString(R.string.settings_label);
            data.summaryOn = context.getString(R.string.support_summary);
            data.iconResId = R.drawable.ic_homepage_support;
            data.intentTargetPackage = context.getPackageName();
            data.intentTargetClass = SupportDashboardActivity.class.getName();
            data.intentAction = "android.intent.action.MAIN";
            data.key = SUPPORT_SEARCH_INDEX_KEY;
            result.add(data);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            if (!context.getResources().getBoolean(R.bool.config_support_enabled)) {
                keys.add(SUPPORT_SEARCH_INDEX_KEY);
            }
            return keys;
        }
    };

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SupportFeatureProvider supportFeatureProvider = FeatureFactory.getFactory(this).getSupportFeatureProvider(this);
        if (supportFeatureProvider != null) {
            supportFeatureProvider.startSupportV2(this);
            finish();
        }
    }
}
