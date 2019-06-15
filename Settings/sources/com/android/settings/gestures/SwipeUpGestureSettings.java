package com.android.settings.gestures;

import android.content.Context;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;

public class SwipeUpGestureSettings extends DashboardFragment {
    public static final String PREF_KEY_SUGGESTION_COMPLETE = "pref_swipe_up_suggestion_complete";
    private static final String TAG = "SwipeUpGesture";

    public void onAttach(Context context) {
        super.onAttach(context);
        FeatureFactory.getFactory(context).getSuggestionFeatureProvider(context).getSharedPrefs(context).edit().putBoolean(PREF_KEY_SUGGESTION_COMPLETE, true).apply();
    }

    public int getMetricsCategory() {
        return 1374;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.swipe_up_gesture_settings;
    }
}
