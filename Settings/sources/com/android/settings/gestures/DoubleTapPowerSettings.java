package com.android.settings.gestures;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;

public class DoubleTapPowerSettings extends DashboardFragment {
    public static final String PREF_KEY_SUGGESTION_COMPLETE = "pref_double_tap_power_suggestion_complete";
    private static final String TAG = "DoubleTapPower";

    public void onAttach(Context context) {
        super.onAttach(context);
        FeatureFactory.getFactory(context).getSuggestionFeatureProvider(context).getSharedPrefs(context).edit().putBoolean(PREF_KEY_SUGGESTION_COMPLETE, true).apply();
    }

    public int getMetricsCategory() {
        return 752;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.double_tap_power_settings;
    }
}
