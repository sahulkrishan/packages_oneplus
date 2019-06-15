package com.android.settings.enterprise;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnterprisePrivacySettings extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return EnterprisePrivacySettings.isPageEnabled(context);
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.enterprise_privacy_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return EnterprisePrivacySettings.buildPreferenceControllers(context, false);
        }
    };
    static final String TAG = "EnterprisePrivacySettings";

    public int getMetricsCategory() {
        return 628;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.enterprise_privacy_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, true);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, boolean async) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new NetworkLogsPreferenceController(context));
        controllers.add(new BugReportsPreferenceController(context));
        controllers.add(new SecurityLogsPreferenceController(context));
        List<AbstractPreferenceController> exposureChangesCategoryControllers = new ArrayList();
        exposureChangesCategoryControllers.add(new EnterpriseInstalledPackagesPreferenceController(context, async));
        exposureChangesCategoryControllers.add(new AdminGrantedLocationPermissionsPreferenceController(context, async));
        exposureChangesCategoryControllers.add(new AdminGrantedMicrophonePermissionPreferenceController(context, async));
        exposureChangesCategoryControllers.add(new AdminGrantedCameraPermissionPreferenceController(context, async));
        exposureChangesCategoryControllers.add(new EnterpriseSetDefaultAppsPreferenceController(context));
        exposureChangesCategoryControllers.add(new AlwaysOnVpnCurrentUserPreferenceController(context));
        exposureChangesCategoryControllers.add(new AlwaysOnVpnManagedProfilePreferenceController(context));
        exposureChangesCategoryControllers.add(new ImePreferenceController(context));
        exposureChangesCategoryControllers.add(new GlobalHttpProxyPreferenceController(context));
        exposureChangesCategoryControllers.add(new CaCertsCurrentUserPreferenceController(context));
        exposureChangesCategoryControllers.add(new CaCertsManagedProfilePreferenceController(context));
        exposureChangesCategoryControllers.add(new BackupsEnabledPreferenceController(context));
        controllers.addAll(exposureChangesCategoryControllers);
        controllers.add(new PreferenceCategoryController(context, "exposure_changes_category").setChildren(exposureChangesCategoryControllers));
        controllers.add(new FailedPasswordWipeCurrentUserPreferenceController(context));
        controllers.add(new FailedPasswordWipeManagedProfilePreferenceController(context));
        return controllers;
    }

    public static boolean isPageEnabled(Context context) {
        return FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context).hasDeviceOwner();
    }
}
