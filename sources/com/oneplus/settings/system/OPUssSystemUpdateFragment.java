package com.oneplus.settings.system;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;

public class OPUssSystemUpdateFragment extends DashboardFragment {
    private static final String TAG = "OPUssSystemUpdateFragment";

    public int getMetricsCategory() {
        return 87;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.op_carrier_system_update1_fragment;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getHelpResource() {
        return R.string.help_url_system_dashboard;
    }
}
