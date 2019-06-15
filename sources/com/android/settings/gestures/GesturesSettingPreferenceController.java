package com.android.settings.gestures;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GesturesSettingPreferenceController extends BasePreferenceController {
    private static final String FAKE_PREF_KEY = "fake_key_only_for_get_available";
    private static final String KEY_GESTURES_SETTINGS = "gesture_settings";
    private final AssistGestureFeatureProvider mFeatureProvider;
    private List<AbstractPreferenceController> mGestureControllers;

    public GesturesSettingPreferenceController(Context context) {
        super(context, KEY_GESTURES_SETTINGS);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getAssistGestureFeatureProvider();
    }

    public int getAvailabilityStatus() {
        if (this.mGestureControllers == null) {
            this.mGestureControllers = buildAllPreferenceControllers(this.mContext);
        }
        boolean isAvailable = false;
        Iterator it = this.mGestureControllers.iterator();
        while (true) {
            boolean z = false;
            if (!it.hasNext()) {
                break;
            }
            AbstractPreferenceController controller = (AbstractPreferenceController) it.next();
            if (isAvailable || controller.isAvailable()) {
                z = true;
            }
            isAvailable = z;
        }
        if (isAvailable) {
            return 0;
        }
        return 2;
    }

    private static List<AbstractPreferenceController> buildAllPreferenceControllers(@NonNull Context context) {
        AmbientDisplayConfiguration ambientDisplayConfiguration = new AmbientDisplayConfiguration(context);
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new AssistGestureSettingsPreferenceController(context, FAKE_PREF_KEY).setAssistOnly(false));
        controllers.add(new SwipeToNotificationPreferenceController(context, FAKE_PREF_KEY));
        controllers.add(new DoubleTwistPreferenceController(context, FAKE_PREF_KEY));
        controllers.add(new DoubleTapPowerPreferenceController(context, FAKE_PREF_KEY));
        controllers.add(new PickupGesturePreferenceController(context, FAKE_PREF_KEY).setConfig(ambientDisplayConfiguration));
        controllers.add(new DoubleTapScreenPreferenceController(context, FAKE_PREF_KEY).setConfig(ambientDisplayConfiguration));
        controllers.add(new PreventRingingPreferenceController(context, FAKE_PREF_KEY));
        return controllers;
    }

    public CharSequence getSummary() {
        if (!this.mFeatureProvider.isSensorAvailable(this.mContext)) {
            return "";
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        boolean assistGestureSilenceEnabled = true;
        boolean assistGestureEnabled = Secure.getInt(contentResolver, "assist_gesture_enabled", 1) != 0;
        if (Secure.getInt(contentResolver, "assist_gesture_silence_alerts_enabled", 1) == 0) {
            assistGestureSilenceEnabled = false;
        }
        if (this.mFeatureProvider.isSupported(this.mContext) && assistGestureEnabled) {
            return this.mContext.getText(R.string.language_input_gesture_summary_on_with_assist);
        }
        if (assistGestureSilenceEnabled) {
            return this.mContext.getText(R.string.language_input_gesture_summary_on_non_assist);
        }
        return this.mContext.getText(R.string.language_input_gesture_summary_off);
    }
}
