package com.android.settings.enterprise;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class EnterpriseSetDefaultAppsListFragment extends DashboardFragment {
    static final String TAG = "EnterprisePrivacySettings";

    public int getMetricsCategory() {
        return 940;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.enterprise_set_default_apps_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new EnterpriseSetDefaultAppsListPreferenceController(context, this, context.getPackageManager()));
        return controllers;
    }
}
