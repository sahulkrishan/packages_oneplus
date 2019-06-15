package com.android.settings.enterprise;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.applications.ApplicationFeatureProvider.ListOfAppsCallback;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.enterprise.ApplicationListPreferenceController.ApplicationListBuilder;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public abstract class ApplicationListFragment extends DashboardFragment implements ApplicationListBuilder {
    static final String TAG = "EnterprisePrivacySettings";

    private static abstract class AdminGrantedPermission extends ApplicationListFragment {
        private final String[] mPermissions;

        public AdminGrantedPermission(String[] permissions) {
            this.mPermissions = permissions;
        }

        public void buildApplicationList(Context context, ListOfAppsCallback callback) {
            FeatureFactory.getFactory(context).getApplicationFeatureProvider(context).listAppsWithAdminGrantedPermissions(this.mPermissions, callback);
        }

        public int getMetricsCategory() {
            return 939;
        }
    }

    public static class EnterpriseInstalledPackages extends ApplicationListFragment {
        public int getMetricsCategory() {
            return 938;
        }

        public void buildApplicationList(Context context, ListOfAppsCallback callback) {
            FeatureFactory.getFactory(context).getApplicationFeatureProvider(context).listPolicyInstalledApps(callback);
        }
    }

    public static class AdminGrantedPermissionCamera extends AdminGrantedPermission {
        public /* bridge */ /* synthetic */ void buildApplicationList(Context context, ListOfAppsCallback listOfAppsCallback) {
            super.buildApplicationList(context, listOfAppsCallback);
        }

        public /* bridge */ /* synthetic */ int getMetricsCategory() {
            return super.getMetricsCategory();
        }

        public AdminGrantedPermissionCamera() {
            super(new String[]{"android.permission.CAMERA"});
        }
    }

    public static class AdminGrantedPermissionLocation extends AdminGrantedPermission {
        public /* bridge */ /* synthetic */ void buildApplicationList(Context context, ListOfAppsCallback listOfAppsCallback) {
            super.buildApplicationList(context, listOfAppsCallback);
        }

        public /* bridge */ /* synthetic */ int getMetricsCategory() {
            return super.getMetricsCategory();
        }

        public AdminGrantedPermissionLocation() {
            super(new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"});
        }
    }

    public static class AdminGrantedPermissionMicrophone extends AdminGrantedPermission {
        public /* bridge */ /* synthetic */ void buildApplicationList(Context context, ListOfAppsCallback listOfAppsCallback) {
            super.buildApplicationList(context, listOfAppsCallback);
        }

        public /* bridge */ /* synthetic */ int getMetricsCategory() {
            return super.getMetricsCategory();
        }

        public AdminGrantedPermissionMicrophone() {
            super(new String[]{"android.permission.RECORD_AUDIO"});
        }
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.app_list_disclosure_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new ApplicationListPreferenceController(context, this, context.getPackageManager(), this));
        return controllers;
    }
}
