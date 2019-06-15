package com.android.settings.notification;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

public class ZenModeBlockedEffectsSettings extends ZenModeSettingsBase implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.zen_mode_block_settings;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            return super.getNonIndexableKeys(context);
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ZenModeBlockedEffectsSettings.buildPreferenceControllers(context, null);
        }
    };

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.zen_mode_blocked_effects_footer);
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new ZenModeVisEffectPreferenceController(context, lifecycle, "zen_effect_intent", 4, 1332, null));
        Context context2 = context;
        Lifecycle lifecycle2 = lifecycle;
        controllers.add(new ZenModeVisEffectPreferenceController(context2, lifecycle2, "zen_effect_light", 8, 1333, null));
        Lifecycle lifecycle3 = lifecycle;
        controllers.add(new ZenModeVisEffectPreferenceController(context, lifecycle3, "zen_effect_peek", 16, 1334, null));
        controllers.add(new ZenModeVisEffectPreferenceController(context2, lifecycle2, "zen_effect_status", 32, 1335, new int[]{256}));
        Context context3 = context;
        controllers.add(new ZenModeVisEffectPreferenceController(context3, lifecycle3, "zen_effect_badge", 64, 1336, null));
        controllers.add(new ZenModeVisEffectPreferenceController(context2, lifecycle2, "zen_effect_ambient", 128, 1337, null));
        controllers.add(new ZenModeVisEffectPreferenceController(context3, lifecycle3, "zen_effect_list", 256, 1338, null));
        return controllers;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.zen_mode_block_settings;
    }

    public int getMetricsCategory() {
        return 1339;
    }
}
