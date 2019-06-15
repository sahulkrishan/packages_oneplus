package com.android.settings.applications;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.annotation.NonNull;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class SpecialAccessSettings extends DashboardFragment {
    private static final String[] DISABLED_FEATURES_LOW_RAM = new String[]{"notification_access", "zen_access", "enabled_vr_listeners", "picture_in_picture"};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.special_access;
            result.add(sir);
            return result;
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return SpecialAccessSettings.buildPreferenceControllers(context);
        }
    };
    private static final String TAG = "SpecialAccessSettings";

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.special_access;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (ActivityManager.isLowRamDeviceStatic()) {
            for (String disabledFeature : DISABLED_FEATURES_LOW_RAM) {
                if (findPreference(disabledFeature) != null) {
                    removePreference(disabledFeature);
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(@NonNull Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new HighPowerAppsController(context));
        controllers.add(new DeviceAdministratorsController(context));
        controllers.add(new PremiumSmsController(context));
        controllers.add(new DataSaverController(context));
        controllers.add(new EnabledVrListenersController(context));
        return controllers;
    }

    public int getMetricsCategory() {
        return 351;
    }
}
