package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmartBatterySettings extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.smart_battery_detail;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return SmartBatterySettings.buildPreferenceControllers(context, null, null);
        }
    };
    public static final String TAG = "SmartBatterySettings";

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.smart_battery_footer);
    }

    public int getMetricsCategory() {
        return 1281;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.smart_battery_detail;
    }

    public int getHelpResource() {
        return R.string.help_uri_smart_battery_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, (SettingsActivity) getActivity(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, SettingsActivity settingsActivity, InstrumentedPreferenceFragment fragment) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new SmartBatteryPreferenceController(context));
        if (settingsActivity == null || fragment == null) {
            controllers.add(new RestrictAppPreferenceController(context));
        } else {
            controllers.add(new RestrictAppPreferenceController(fragment));
        }
        return controllers;
    }
}
