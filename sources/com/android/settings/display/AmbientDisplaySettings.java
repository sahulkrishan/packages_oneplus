package com.android.settings.display;

import android.content.Context;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.gestures.DoubleTapScreenPreferenceController;
import com.android.settings.gestures.PickupGesturePreferenceController;

public class AmbientDisplaySettings extends DashboardFragment {
    public static final String KEY_AMBIENT_DISPLAY_ALWAYS_ON = "ambient_display_always_on";
    private static final String TAG = "AmbientDisplaySettings";
    private AmbientDisplayConfiguration mConfig;

    public void onAttach(Context context) {
        super.onAttach(context);
        ((AmbientDisplayAlwaysOnPreferenceController) use(AmbientDisplayAlwaysOnPreferenceController.class)).setConfig(getConfig(context)).setCallback(new -$$Lambda$AmbientDisplaySettings$EZV3GOIvjt5KUMzbyw8y8_onopw(this));
        ((AmbientDisplayNotificationsPreferenceController) use(AmbientDisplayNotificationsPreferenceController.class)).setConfig(getConfig(context));
        ((DoubleTapScreenPreferenceController) use(DoubleTapScreenPreferenceController.class)).setConfig(getConfig(context));
        ((PickupGesturePreferenceController) use(PickupGesturePreferenceController.class)).setConfig(getConfig(context));
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.ambient_display_settings;
    }

    public int getMetricsCategory() {
        return 1003;
    }

    private AmbientDisplayConfiguration getConfig(Context context) {
        if (this.mConfig == null) {
            this.mConfig = new AmbientDisplayConfiguration(context);
        }
        return this.mConfig;
    }
}
