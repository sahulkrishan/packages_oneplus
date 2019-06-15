package com.android.settings.notification;

import android.app.Fragment;
import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.utils.ManagedServiceSettings.Config;
import com.android.settings.utils.ManagedServiceSettings.Config.Builder;
import com.android.settings.utils.ZenServiceListing;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

public class ZenModeAutomationSettings extends ZenModeSettingsBase {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.zen_mode_automation_settings;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add("zen_mode_add_automatic_rule");
            keys.add("zen_mode_automatic_rules");
            return keys;
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ZenModeAutomationSettings.buildPreferenceControllers(context, null, null, null);
        }
    };
    protected final Config CONFIG = getConditionProviderConfig();

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        ZenServiceListing serviceListing = new ZenServiceListing(getContext(), this.CONFIG);
        serviceListing.reloadApprovedServices();
        return buildPreferenceControllers(context, this, serviceListing, getLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Fragment parent, ZenServiceListing serviceListing, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new ZenModeAddAutomaticRulePreferenceController(context, parent, serviceListing, lifecycle));
        controllers.add(new ZenModeAutomaticRulesPreferenceController(context, parent, lifecycle));
        return controllers;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.zen_mode_automation_settings;
    }

    public int getMetricsCategory() {
        return Const.CODE_C1_DLC;
    }

    protected static Config getConditionProviderConfig() {
        return new Builder().setTag("ZenModeSettings").setIntentAction("android.service.notification.ConditionProviderService").setPermission("android.permission.BIND_CONDITION_PROVIDER_SERVICE").setNoun("condition provider").build();
    }
}
