package com.android.settings.gestures;

import android.content.Context;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;

public class GestureSettings extends DashboardFragment {
    private static final String KEY_ASSIST = "gesture_assist_input_summary";
    private static final String KEY_DOUBLE_TAP_POWER = "gesture_double_tap_power_input_summary";
    private static final String KEY_DOUBLE_TAP_SCREEN = "gesture_double_tap_screen_input_summary";
    private static final String KEY_DOUBLE_TWIST = "gesture_double_twist_input_summary";
    private static final String KEY_PICK_UP = "gesture_pick_up_input_summary";
    private static final String KEY_PREVENT_RINGING = "gesture_prevent_ringing_summary";
    private static final String KEY_SWIPE_DOWN = "gesture_swipe_down_fingerprint_input_summary";
    private static final String KEY_SWIPE_UP = "gesture_swipe_up_input_summary";
    private static final String TAG = "GestureSettings";
    private AmbientDisplayConfiguration mAmbientDisplayConfig;

    public int getMetricsCategory() {
        return 459;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.gestures;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((AssistGestureSettingsPreferenceController) use(AssistGestureSettingsPreferenceController.class)).setAssistOnly(false);
        ((PickupGesturePreferenceController) use(PickupGesturePreferenceController.class)).setConfig(getConfig(context));
        ((DoubleTapScreenPreferenceController) use(DoubleTapScreenPreferenceController.class)).setConfig(getConfig(context));
    }

    private AmbientDisplayConfiguration getConfig(Context context) {
        if (this.mAmbientDisplayConfig == null) {
            this.mAmbientDisplayConfig = new AmbientDisplayConfiguration(context);
        }
        return this.mAmbientDisplayConfig;
    }
}
