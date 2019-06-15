package com.android.settings.applications;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.notification.EmergencyBroadcastPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPPreferenceDividerLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppAndNotificationDashboardFragment extends DashboardFragment {
    private static final String KEY_MANAGE_PERMS = "manage_perms";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.app_and_notification;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return AppAndNotificationDashboardFragment.buildPreferenceControllers(context, null, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(new SpecialAppAccessPreferenceController(context).getPreferenceKey());
            return keys;
        }
    };
    private static final String TAG = "AppAndNotifDashboard";

    public int getMetricsCategory() {
        return 748;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getHelpResource() {
        return R.string.help_url_apps_and_notifications;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.app_and_notification;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Preference managePermsPref = findPreference(KEY_MANAGE_PERMS);
        if (managePermsPref != null && VERSION.IS_CTA_BUILD) {
            managePermsPref.setIntent(new Intent("com.oneplus.security.action.OPPERMISSION"));
        }
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        Application app;
        Activity activity = getActivity();
        if (activity != null) {
            app = activity.getApplication();
        } else {
            app = null;
        }
        return buildPreferenceControllers(context, app, this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Application app, Fragment host) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new EmergencyBroadcastPreferenceController(context, "app_and_notif_cell_broadcast_settings"));
        controllers.add(new SpecialAppAccessPreferenceController(context));
        controllers.add(new OPDataUsageControlPreferenceController(context));
        controllers.add(new OPPreferenceDividerLine(context));
        controllers.add(new RecentAppsPreferenceController(context, app, host));
        return controllers;
    }
}
