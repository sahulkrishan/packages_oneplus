package com.android.settings.notification;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

public class ZenModeCallsSettings extends ZenModeSettingsBase implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.zen_mode_calls_settings;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            return super.getNonIndexableKeys(context);
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ZenModeCallsSettings.buildPreferenceControllers(context, null);
        }
    };

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new ZenModeCallsPreferenceController(context, lifecycle));
        controllers.add(new ZenModeStarredContactsPreferenceController(context, lifecycle, 8));
        controllers.add(new ZenModeRepeatCallersPreferenceController(context, lifecycle, context.getResources().getInteger(17694948)));
        controllers.add(new ZenModeBehaviorFooterPreferenceController(context, lifecycle, R.string.zen_mode_calls_footer));
        return controllers;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.zen_mode_calls_settings;
    }

    public int getMetricsCategory() {
        return Const.CODE_C1_DLY;
    }
}
