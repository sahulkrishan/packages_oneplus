package com.android.settings.development.qstile;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class DevelopmentTileConfigFragment extends DashboardFragment {
    private static final String TAG = "DevelopmentTileConfig";

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.development_tile_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new DevelopmentTilePreferenceController(context));
        return controllers;
    }

    public int getMetricsCategory() {
        return 1224;
    }
}
