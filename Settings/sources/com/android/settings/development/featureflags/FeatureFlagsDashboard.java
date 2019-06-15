package com.android.settings.development.featureflags;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.List;

public class FeatureFlagsDashboard extends DashboardFragment {
    private static final String TAG = "FeatureFlagsDashboard";

    public int getMetricsCategory() {
        return 1217;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.feature_flags_settings;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((FeatureFlagFooterPreferenceController) use(FeatureFlagFooterPreferenceController.class)).setFooterMixin(this.mFooterPreferenceMixin);
    }

    public int getHelpResource() {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        Lifecycle lifecycle = getLifecycle();
        FeatureFlagFooterPreferenceController footerController = new FeatureFlagFooterPreferenceController(context);
        controllers.add(new FeatureFlagsPreferenceController(context, lifecycle));
        controllers.add(footerController);
        lifecycle.addObserver(footerController);
        return controllers;
    }
}
