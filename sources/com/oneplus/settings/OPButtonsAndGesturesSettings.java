package com.oneplus.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.gestures.OPAssistantAPPSwitchPreferenceController;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPButtonsAndGesturesSettings extends DashboardFragment implements OnPreferenceChangeListener, Indexable {
    private static final String FINGERPRINT_GESTURE_SWIPE_DOWN_UP_KEY = "op_fingerprint_gesture_swipe_down_up";
    private static final String FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY = "op_fingerprint_long_press_camera_shot";
    private static final String KEY_ALERTSLIDER_SETTINGS_SOC_TRI_STATE = "op_alertslider_settings_soc_tri_state";
    private static final String KEY_BUTTONS_AND_FULLSCREEN_GESTURES = "op_buttons_and_fullscreen_gestures";
    private static final String KEY_BUTTONS_SETTINGS = "buttons_settings";
    private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE = "camera_double_tap_power_gesture";
    private static final int ONEPLUS_EMERGENCY_TAP_POWER_GESTURE_FIVE_TIMES = 5;
    private static final int ONEPLUS_EMERGENCY_TAP_POWER_GESTURE_NO_TIMES = -1;
    private static final int ONEPLUS_EMERGENCY_TAP_POWER_GESTURE_THREE_TIMES = 3;
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_buttons_and_gesture_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            super.getNonIndexableKeys(context);
            List<String> result = new ArrayList();
            if (OPUtils.isSupportCustomFingerprint()) {
                result.add(OPButtonsAndGesturesSettings.FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY);
            }
            if (OPUtils.isSurportBackFingerprint(context) && !OPUtils.isSupportGesturePullNotificationBar()) {
                result.add(OPButtonsAndGesturesSettings.FINGERPRINT_GESTURE_SWIPE_DOWN_UP_KEY);
            }
            if (OPButtonsAndGesturesSettings.isSupportHardwareKeys()) {
                result.add(OPButtonsAndGesturesSettings.KEY_BUTTONS_AND_FULLSCREEN_GESTURES);
            } else {
                result.add(OPButtonsAndGesturesSettings.KEY_BUTTONS_SETTINGS);
            }
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader, null);
        }
    };
    private static final String TAG = "OPOthersSettings";
    private Preference mAlertsliderSettingsPreference;
    private Preference mButtonsAndFullscreenGesturesPreference;
    private Preference mButtonsSettingsPreference;
    private SwitchPreference mCameraDoubleTapPowerGesturePreference;
    private Context mContext;
    private SwitchPreference mFingerprintGestureLongpressCamera;
    private SwitchPreference mFingerprintGestureSwipeDownUp;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;

        /* synthetic */ SummaryProvider(Context x0, SummaryLoader x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                updateSummary();
            }
        }

        private void updateSummary() {
            if (OPUtils.isSupportSocTriState()) {
                this.mLoader.setSummary(this, this.mContext.getString(R.string.oneplus_buttons_dashboard_summary));
                return;
            }
            String summary = this.mContext.getString(R.string.alertslider_settings);
            String navkeysSummary = this.mContext.getString(R.string.buttons_enable_on_screen_navkeys_title).toLowerCase();
            summary = this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, navkeysSummary});
            String quickgestureSummary = this.mContext.getString(R.string.oneplus_quick_gestures).toLowerCase();
            this.mLoader.setSummary(this, this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, quickgestureSummary}));
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = SettingsBaseApplication.mApplication;
        if (isCameraDoubleTapPowerGestureAvailable(getResources())) {
            this.mCameraDoubleTapPowerGesturePreference = (SwitchPreference) findPreference(KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);
            this.mCameraDoubleTapPowerGesturePreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);
        }
        this.mFingerprintGestureLongpressCamera = (SwitchPreference) findPreference(FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY);
        this.mFingerprintGestureLongpressCamera.setOnPreferenceChangeListener(this);
        this.mFingerprintGestureSwipeDownUp = (SwitchPreference) findPreference(FINGERPRINT_GESTURE_SWIPE_DOWN_UP_KEY);
        this.mFingerprintGestureSwipeDownUp.setOnPreferenceChangeListener(this);
        if (!OPUtils.isSurportBackFingerprint(this.mContext) || OPUtils.isSupportCustomFingerprint()) {
            this.mFingerprintGestureLongpressCamera.setVisible(false);
            if (!OPUtils.isSupportGesturePullNotificationBar()) {
                this.mFingerprintGestureSwipeDownUp.setVisible(false);
            }
        } else if (!OPUtils.isSupportGesturePullNotificationBar()) {
            this.mFingerprintGestureSwipeDownUp.setVisible(false);
        }
        this.mAlertsliderSettingsPreference = findPreference(KEY_ALERTSLIDER_SETTINGS_SOC_TRI_STATE);
        if (!(OPUtils.isSupportSocTriState() || this.mAlertsliderSettingsPreference == null)) {
            this.mAlertsliderSettingsPreference.setTitle((int) R.string.alertslider_settings);
        }
        this.mButtonsAndFullscreenGesturesPreference = findPreference(KEY_BUTTONS_AND_FULLSCREEN_GESTURES);
        this.mButtonsSettingsPreference = findPreference(KEY_BUTTONS_SETTINGS);
        if (isSupportHardwareKeys()) {
            this.mButtonsAndFullscreenGesturesPreference.setVisible(false);
        } else {
            this.mButtonsSettingsPreference.setVisible(false);
        }
    }

    private static boolean isSupportHardwareKeys() {
        return SettingsBaseApplication.mApplication.getResources().getBoolean(17957029) ^ 1;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.op_buttons_and_gesture_settings;
    }

    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(17956909);
    }

    private void loadPreferenceScreen() {
        boolean z = true;
        boolean inEmergencyCall = Global.getInt(getContentResolver(), "emergency_affordance_needed", 0) != 0;
        int times = Global.getInt(getContentResolver(), "oneplus_emergency_tap_power_gesture_times", -1);
        if (times == -1) {
            if (inEmergencyCall) {
                times = 3;
            } else {
                times = 5;
            }
        }
        if (times == 3) {
            this.mCameraDoubleTapPowerGesturePreference.setEnabled(false);
            this.mCameraDoubleTapPowerGesturePreference.setSummary((int) R.string.oneplus_emergency_tap_power_gesture_tips);
        } else {
            this.mCameraDoubleTapPowerGesturePreference.setEnabled(true);
            this.mCameraDoubleTapPowerGesturePreference.setSummary((int) R.string.camera_double_tap_power_gesture_title);
        }
        if (this.mCameraDoubleTapPowerGesturePreference != null) {
            int value = Secure.getInt(getContentResolver(), "camera_double_tap_power_gesture_disabled", 0);
            SwitchPreference switchPreference = this.mCameraDoubleTapPowerGesturePreference;
            if (value != 0) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
        if (this.mFingerprintGestureLongpressCamera != null) {
            this.mFingerprintGestureLongpressCamera.setChecked(isFingerprintLongpressCameraShotEnabled(this.mContext));
        }
        if (this.mFingerprintGestureSwipeDownUp != null) {
            this.mFingerprintGestureSwipeDownUp.setChecked(isSystemUINavigationEnabled(this.mContext));
        }
    }

    private static boolean isSystemUINavigationEnabled(Context context) {
        return Secure.getInt(context.getContentResolver(), "system_navigation_keys_enabled", 0) == 1;
    }

    private static boolean isFingerprintLongpressCameraShotEnabled(Context context) {
        return System.getInt(context.getContentResolver(), FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY, 0) == 1;
    }

    public void onResume() {
        super.onResume();
        loadPreferenceScreen();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 0;
        boolean state;
        if (newValue instanceof Boolean) {
            state = ((Boolean) newValue).booleanValue();
        } else if (newValue instanceof String) {
            state = Integer.valueOf((String) newValue).intValue() != 0;
        }
        if (preference == this.mCameraDoubleTapPowerGesturePreference) {
            boolean value = ((Boolean) newValue).booleanValue();
            ContentResolver contentResolver = getContentResolver();
            String str = "camera_double_tap_power_gesture_disabled";
            if (!value) {
                i = 1;
            }
            Secure.putInt(contentResolver, str, i);
            return true;
        } else if (preference == this.mFingerprintGestureLongpressCamera) {
            System.putInt(getContentResolver(), FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY, ((Boolean) newValue).booleanValue());
            return true;
        } else if (preference != this.mFingerprintGestureSwipeDownUp) {
            return false;
        } else {
            Secure.putInt(getContentResolver(), "system_navigation_keys_enabled", ((Boolean) newValue).booleanValue());
            return true;
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        OPAssistantAPPSwitchPreferenceController mOPVoiceAssistantSwitchPreferenceController = new OPAssistantAPPSwitchPreferenceController(context);
        getLifecycle().addObserver(mOPVoiceAssistantSwitchPreferenceController);
        controllers.add(mOPVoiceAssistantSwitchPreferenceController);
        return controllers;
    }
}
