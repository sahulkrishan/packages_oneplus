package com.oneplus.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.oneplus.settings.gestures.OPGestureUtils;
import com.oneplus.settings.ui.OPGesturePreference;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPGestureSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {
    private static final String ANTI_MISOPERATION_SCREEN_TOUCH = "anti_misoperation_of_the_screen_touch_enable";
    private static String BLACK_SCREEN_GESTURES = "black_screen_gestures";
    private static final String BLACK_SCREEN_SETTINGS_KEY = "black_screen_setting_key";
    private static final String DOUBLE_CLICK_LIGHT_SCREEN_KEY = "double_click_light_screen_key";
    private static final String DRAW_O_START_CAMERA_KEY = "draw_o_start_camera_key";
    private static final String FINGERPRINT_GESTURE_CONTROL_KEY = "fingerprint_gesture_control";
    private static final String FINGERPRINT_GESTURE_SWIPE_DOWN_UP_KEY = "op_fingerprint_gesture_swipe_down_up";
    private static final String FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY = "op_fingerprint_long_press_camera_shot";
    private static final String FLASH_LIGHT_KEY = "open_light_device_key";
    private static String GESTURE_TO_ANSWER_CALL_KEY = "gesture_to_answer_call";
    private static String MOTION_SENSOR_CONTROL_KEY = "motion_sensor__control";
    private static final String MUSCI_CONTROL_KEY = "music_control_key";
    private static final String MUSIC_CONTROL_NEXT_KEY = "music_control_next_key";
    private static final String MUSIC_CONTROL_PAUSE_KEY = "music_control_pause_key";
    private static final String MUSIC_CONTROL_PREV_KEY = "music_control_prev_key";
    private static final String MUSIC_CONTROL_START_KEY = "music_control_start_key";
    private static String MUSIC_ROOT_KEY = "music_control";
    private static final String ROTATION_SILENT_KEY = "rotation_silent_enable";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new OPGestureSearchIndexProvider();
    private static String STARTUP_ROOT_KEY = "quick_startup";
    private static final String THREE_SCEENTSHOTS_KEY = "three_screenshots_enable";
    private int isDoubleClickEnable;
    private int isFlashlightEnable;
    private int isMusicControlEnable;
    private int isMusicNextEnable;
    private int isMusicPauseEnable;
    private int isMusicPlayEnable;
    private int isMusicPrevEnable;
    private int isStartUpCameraEnable;
    private boolean isSupportThreeScrrenShot = false;
    private PreferenceCategory mBlackScreenPrefererce;
    private int mBlackSettingValues;
    private SwitchPreference mCameraPerference;
    private Context mContext;
    private SwitchPreference mDoubleLightScreenPreference;
    private OPGesturePreference mDrawMStartAppPreference;
    private OPGesturePreference mDrawOStartAppPreference;
    private OPGesturePreference mDrawSStartAppPreference;
    private OPGesturePreference mDrawVStartAppPreference;
    private OPGesturePreference mDrawWStartAppPreference;
    private PreferenceCategory mFingerprintGestureCategory;
    private SwitchPreference mFingerprintGestureLongpressCamera;
    private SwitchPreference mFingerprintGestureSwipeDownUp;
    private SwitchPreference mFlashLightPreference;
    private Preference mGestureToAnswerCall;
    private PreferenceCategory mMotionSensorControl;
    private SwitchPreference mMusicControlPreference;
    private SwitchPreference mMusicNextPreference;
    private SwitchPreference mMusicPausePreference;
    private SwitchPreference mMusicPreference;
    private PreferenceCategory mMusicPrefererce;
    private SwitchPreference mMusicPrevPreference;
    private SwitchPreference mMusicStartPreference;
    private SwitchPreference mRotationSilent;
    private PreferenceCategory mStartUpPreferece;
    private SwitchPreference mThreeSwipeScreenShot;
    private UserManager mUm;
    private PreferenceScreen root;

    private static class OPGestureSearchIndexProvider extends BaseSearchIndexProvider {
        boolean mIsPrimary;

        public OPGestureSearchIndexProvider() {
            this.mIsPrimary = UserHandle.myUserId() == 0;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            if (!this.mIsPrimary) {
                return result;
            }
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.op_gesture_settings;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> results = new ArrayList();
            if (!this.mIsPrimary) {
                results = OPGestureSettings.getNonVisibleKeys();
            }
            if (!this.mIsPrimary || OPUtils.isSurportGesture20(context)) {
                results.add(OPGestureSettings.FLASH_LIGHT_KEY);
                results.add(OPGestureSettings.DRAW_O_START_CAMERA_KEY);
            }
            if (!(this.mIsPrimary && OPUtils.isSurportGesture20(context))) {
                results.add(OPConstants.KEY_DRAW_O_START_APP);
                results.add(OPConstants.KEY_DRAW_V_START_APP);
                results.add(OPConstants.KEY_DRAW_S_START_APP);
                results.add(OPConstants.KEY_DRAW_M_START_APP);
                results.add(OPConstants.KEY_DRAW_W_START_APP);
            }
            if (!OPUtils.isSupportGesturePullNotificationBar()) {
                results.add(OPGestureSettings.FINGERPRINT_GESTURE_SWIPE_DOWN_UP_KEY);
            }
            results.add(OPGestureSettings.FINGERPRINT_GESTURE_CONTROL_KEY);
            results.add(OPGestureSettings.FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY);
            return results;
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mUm = (UserManager) getSystemService("user");
        addPreferencesFromResource(R.xml.op_gesture_settings);
        this.mContext = getActivity();
        initFingerprintGesture();
        initBlackScreenView();
        initGestureViews();
        initSensorView();
    }

    private void initSensorView() {
        this.root = getPreferenceScreen();
        this.isSupportThreeScrrenShot = this.mContext.getPackageManager().hasSystemFeature("oem.threeScreenshot.support");
        this.mThreeSwipeScreenShot = (SwitchPreference) findPreference(THREE_SCEENTSHOTS_KEY);
        this.mThreeSwipeScreenShot.setOnPreferenceClickListener(this);
        this.mRotationSilent = (SwitchPreference) findPreference(ROTATION_SILENT_KEY);
        this.mRotationSilent.setOnPreferenceClickListener(this);
        boolean z = false;
        this.mThreeSwipeScreenShot.setChecked(System.getInt(getActivity().getContentResolver(), "oem_acc_sensor_three_finger", 0) != 0);
        SwitchPreference switchPreference = this.mRotationSilent;
        if (System.getInt(getActivity().getContentResolver(), "oem_acc_sensor_rotate_silent", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
        if (!this.isSupportThreeScrrenShot) {
            this.root.removePreference(this.mThreeSwipeScreenShot);
        }
    }

    private void initFingerprintGesture() {
        this.mFingerprintGestureCategory = (PreferenceCategory) findPreference(FINGERPRINT_GESTURE_CONTROL_KEY);
        getPreferenceScreen().removePreference(this.mFingerprintGestureCategory);
        getPreferenceScreen().removePreference(findPreference("preference_divider_line_1"));
    }

    private static boolean isSystemUINavigationEnabled(Context context) {
        return Secure.getInt(context.getContentResolver(), "system_navigation_keys_enabled", 0) == 1;
    }

    private static boolean isFingerprintLongpressCameraShotEnabled(Context context) {
        return System.getInt(context.getContentResolver(), FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY, 0) == 1;
    }

    private void initGestureViews() {
        this.mDrawOStartAppPreference = (OPGesturePreference) findPreference(OPConstants.KEY_DRAW_O_START_APP);
        this.mDrawVStartAppPreference = (OPGesturePreference) findPreference(OPConstants.KEY_DRAW_V_START_APP);
        this.mDrawSStartAppPreference = (OPGesturePreference) findPreference(OPConstants.KEY_DRAW_S_START_APP);
        this.mDrawMStartAppPreference = (OPGesturePreference) findPreference(OPConstants.KEY_DRAW_M_START_APP);
        this.mDrawWStartAppPreference = (OPGesturePreference) findPreference(OPConstants.KEY_DRAW_W_START_APP);
        if (!OPUtils.isSurportGesture20(this.mContext)) {
            this.mBlackScreenPrefererce.removePreference(this.mDrawOStartAppPreference);
            this.mBlackScreenPrefererce.removePreference(this.mDrawVStartAppPreference);
            this.mBlackScreenPrefererce.removePreference(this.mDrawSStartAppPreference);
            this.mBlackScreenPrefererce.removePreference(this.mDrawMStartAppPreference);
            this.mBlackScreenPrefererce.removePreference(this.mDrawWStartAppPreference);
        }
    }

    private void initGestureSummary() {
        Activity activity = getActivity();
        if (activity != null) {
            this.mDrawOStartAppPreference.setSummary((CharSequence) OPGestureUtils.getGestureSummarybyGestureKey(activity, OPConstants.KEY_DRAW_O_START_APP));
            this.mDrawVStartAppPreference.setSummary((CharSequence) OPGestureUtils.getGestureSummarybyGestureKey(activity, OPConstants.KEY_DRAW_V_START_APP));
            this.mDrawSStartAppPreference.setSummary((CharSequence) OPGestureUtils.getGestureSummarybyGestureKey(activity, OPConstants.KEY_DRAW_S_START_APP));
            this.mDrawMStartAppPreference.setSummary((CharSequence) OPGestureUtils.getGestureSummarybyGestureKey(activity, OPConstants.KEY_DRAW_M_START_APP));
            this.mDrawWStartAppPreference.setSummary((CharSequence) OPGestureUtils.getGestureSummarybyGestureKey(activity, OPConstants.KEY_DRAW_W_START_APP));
        }
    }

    private void initBlackScreenView() {
        this.mMotionSensorControl = (PreferenceCategory) findPreference(MOTION_SENSOR_CONTROL_KEY);
        this.mGestureToAnswerCall = findPreference(GESTURE_TO_ANSWER_CALL_KEY);
        this.mStartUpPreferece = (PreferenceCategory) findPreference(STARTUP_ROOT_KEY);
        this.mMusicPrefererce = (PreferenceCategory) findPreference(MUSIC_ROOT_KEY);
        this.mBlackScreenPrefererce = (PreferenceCategory) findPreference(BLACK_SCREEN_GESTURES);
        this.mCameraPerference = (SwitchPreference) findPreference(DRAW_O_START_CAMERA_KEY);
        this.mCameraPerference.setOnPreferenceClickListener(this);
        if (OPUtils.isSurportGesture20(this.mContext)) {
            this.mBlackScreenPrefererce.removePreference(this.mCameraPerference);
        }
        this.mDoubleLightScreenPreference = (SwitchPreference) findPreference(DOUBLE_CLICK_LIGHT_SCREEN_KEY);
        this.mDoubleLightScreenPreference.setOnPreferenceClickListener(this);
        this.mMusicControlPreference = (SwitchPreference) findPreference(MUSCI_CONTROL_KEY);
        this.mMusicControlPreference.setOnPreferenceClickListener(this);
        this.mFlashLightPreference = (SwitchPreference) findPreference(FLASH_LIGHT_KEY);
        this.mFlashLightPreference.setOnPreferenceClickListener(this);
        if (OPUtils.isSurportGesture20(this.mContext)) {
            this.mBlackScreenPrefererce.removePreference(this.mFlashLightPreference);
        }
        getConfig();
        boolean z = true;
        if (!OPUtils.isSurportGesture20(this.mContext)) {
            this.mCameraPerference.setChecked(this.isStartUpCameraEnable != 0);
        }
        this.mDoubleLightScreenPreference.setChecked(this.isDoubleClickEnable != 0);
        this.mMusicControlPreference.setChecked(this.isMusicControlEnable != 0);
        if (!OPUtils.isSurportGesture20(this.mContext)) {
            SwitchPreference switchPreference = this.mFlashLightPreference;
            if (this.isFlashlightEnable == 0) {
                z = false;
            }
            switchPreference.setChecked(z);
        }
    }

    public void onResume() {
        super.onResume();
        initGestureSummary();
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(DRAW_O_START_CAMERA_KEY)) {
            if (this.mCameraPerference.isChecked()) {
                OPGestureUtils.set1(this.mContext, 6);
            } else {
                OPGestureUtils.set0(this.mContext, 6);
            }
            return true;
        } else if (preference.getKey().equals(DOUBLE_CLICK_LIGHT_SCREEN_KEY)) {
            if (this.mDoubleLightScreenPreference.isChecked()) {
                if (OPUtils.isSupportCustomFingerprint() && OPGestureUtils.get(System.getInt(getContext().getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0), 11) == 1) {
                    Toast.makeText(getContext(), R.string.oneplus_security_settings_fingerprint_toggle_two_toast_1, 0).show();
                    OPGestureUtils.set0(getContext(), 11);
                    OPUtils.sendAppTracker(OPConstants.TAP_SCREEN_SHOW, 0);
                }
                OPGestureUtils.set1(this.mContext, 7);
            } else {
                OPGestureUtils.set0(this.mContext, 7);
            }
            return true;
        } else if (preference.getKey().equals(MUSCI_CONTROL_KEY)) {
            toggleMusicController(this.mMusicControlPreference.isChecked());
            return true;
        } else if (preference.getKey().equals(MUSIC_CONTROL_NEXT_KEY)) {
            if (this.mMusicNextPreference.isChecked()) {
                OPGestureUtils.set1(this.mContext, 3);
            } else {
                OPGestureUtils.set0(this.mContext, 3);
            }
            return true;
        } else if (preference.getKey().equals(MUSIC_CONTROL_PREV_KEY)) {
            if (this.mMusicPrevPreference.isChecked()) {
                OPGestureUtils.set1(this.mContext, 4);
            } else {
                OPGestureUtils.set0(this.mContext, 4);
            }
            return true;
        } else if (preference.getKey().equals(FLASH_LIGHT_KEY)) {
            if (this.mFlashLightPreference.isChecked()) {
                OPGestureUtils.set1(this.mContext, 0);
            } else {
                OPGestureUtils.set0(this.mContext, 0);
            }
            return true;
        } else if (preference.getKey().equals(MUSIC_CONTROL_START_KEY)) {
            if (this.mMusicStartPreference.isChecked()) {
                OPGestureUtils.set1(this.mContext, 1);
            } else {
                OPGestureUtils.set0(this.mContext, 1);
            }
            return true;
        } else if (preference.getKey().equals(MUSIC_CONTROL_PAUSE_KEY)) {
            if (this.mMusicPausePreference.isChecked()) {
                OPGestureUtils.set1(this.mContext, 2);
            } else {
                OPGestureUtils.set0(this.mContext, 2);
            }
            return true;
        } else if (preference.getKey().equals(THREE_SCEENTSHOTS_KEY)) {
            System.putInt(getActivity().getContentResolver(), "oem_acc_sensor_three_finger", this.mThreeSwipeScreenShot.isChecked());
            OPUtils.sendAppTracker("op_three_key_screenshots_enabled", this.mThreeSwipeScreenShot.isChecked());
            if (this.mUm != null && this.mUm.isUserRunning(999)) {
                System.putIntForUser(getActivity().getContentResolver(), "oem_acc_sensor_three_finger", this.mThreeSwipeScreenShot.isChecked(), 999);
            }
            return true;
        } else if (!preference.getKey().equals(ROTATION_SILENT_KEY)) {
            return false;
        } else {
            System.putInt(getActivity().getContentResolver(), "oem_acc_sensor_rotate_silent", this.mRotationSilent.isChecked());
            return true;
        }
    }

    private void toggleMusicController(boolean open) {
        if (open) {
            OPGestureUtils.set1(this.mContext, 1);
            OPGestureUtils.set1(this.mContext, 2);
            OPGestureUtils.set1(this.mContext, 3);
            OPGestureUtils.set1(this.mContext, 4);
            return;
        }
        OPGestureUtils.set0(this.mContext, 1);
        OPGestureUtils.set0(this.mContext, 2);
        OPGestureUtils.set0(this.mContext, 3);
        OPGestureUtils.set0(this.mContext, 4);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean enabled = ((Boolean) newValue).booleanValue();
        String key = preference.getKey();
        if (FINGERPRINT_GESTURE_SWIPE_DOWN_UP_KEY.equals(key)) {
            Secure.putInt(getContentResolver(), "system_navigation_keys_enabled", enabled);
        } else if (FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY.equals(key)) {
            System.putInt(getContentResolver(), FINGERPRINT_LONG_PRESS_CAMERA_SHOT_KEY, enabled);
        }
        return true;
    }

    private void getConfig() {
        this.mBlackSettingValues = System.getInt(getActivity().getContentResolver(), "oem_acc_blackscreen_gestrue_enable", 0);
        this.isFlashlightEnable = OPGestureUtils.get(this.mBlackSettingValues, 0);
        int i = 1;
        this.isMusicPlayEnable = OPGestureUtils.get(this.mBlackSettingValues, 1);
        this.isMusicPauseEnable = OPGestureUtils.get(this.mBlackSettingValues, 2);
        this.isMusicNextEnable = OPGestureUtils.get(this.mBlackSettingValues, 3);
        this.isMusicPrevEnable = OPGestureUtils.get(this.mBlackSettingValues, 4);
        if (this.isMusicPlayEnable != 1) {
            i = 0;
        }
        this.isMusicControlEnable = i;
        this.isStartUpCameraEnable = OPGestureUtils.get(this.mBlackSettingValues, 6);
        this.isDoubleClickEnable = OPGestureUtils.get(this.mBlackSettingValues, 7);
    }

    public boolean checkIfNeedPasswordToPowerOn() {
        return Global.getInt(getActivity().getContentResolver(), "require_password_to_decrypt", 0) == 1;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    private static List<String> getNonVisibleKeys() {
        List<String> result = new ArrayList();
        result.add(DOUBLE_CLICK_LIGHT_SCREEN_KEY);
        result.add(MUSCI_CONTROL_KEY);
        result.add(ROTATION_SILENT_KEY);
        result.add(THREE_SCEENTSHOTS_KEY);
        result.add(ANTI_MISOPERATION_SCREEN_TOUCH);
        result.add(FINGERPRINT_GESTURE_CONTROL_KEY);
        return result;
    }
}
