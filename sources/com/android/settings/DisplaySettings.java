package com.android.settings;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.IPowerManager.Stub;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.service.vr.IVrManager;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.android.settings.Settings.DisplaySizeAdaptionAppListActivity;
import com.android.settings.accessibility.ToggleFontSizePreferenceFragment;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.dream.DreamSettings;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.display.BrightnessUtils;
import com.oneplus.custom.utils.OpCustomizeSettings;
import com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.better.OPReadingMode;
import com.oneplus.settings.ui.ColorPickerPreference;
import com.oneplus.settings.ui.ColorPickerPreference.CustomColorClickListener;
import com.oneplus.settings.ui.OPBrightnessSeekbarPreferenceCategory;
import com.oneplus.settings.ui.OPBrightnessSeekbarPreferenceCategory.OPCallbackBrightness;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DisplaySettings extends SettingsPreferenceFragment implements CustomColorClickListener, OnPreferenceChangeListener, OnPreferenceClickListener, OPCallbackBrightness, Indexable {
    private static final int ACTIVITY_REQUEST_CODE_FOR_CUSTOM_COLOR = 100;
    private static final boolean BRIGHTNESS_TRANSLATION = false;
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    public static final String FILE_FONT_WARING = "font_waring";
    private static final String KEY_AUTO_BRIGHTNESS = "auto_brightness";
    private static final String KEY_AUTO_ROTATE = "auto_rotate";
    private static final String KEY_BACK_TOP_THEME = "back_topic_theme";
    private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE = "camera_double_tap_power_gesture";
    private static final String KEY_CAMERA_GESTURE = "camera_gesture";
    private static final String KEY_CATEGOREY_CUSTOM = "header_category_custom";
    private static final String KEY_CUSTOM_ACCENT_COLOR = "persist.sys.theme.accentcolor";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_DARK_MODE_ACTION = "oem_black_mode";
    public static final String KEY_DISPLAY_SIZE = "screen_zoom";
    private static final String KEY_DISPLAY_SIZE_ADAPTION = "display_size_adaption";
    private static final String KEY_DISPLAY_SYSTEM = "display_system";
    private static final String KEY_DOZE = "doze";
    private static final String KEY_DOZE_801 = "doze_801";
    private static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_IS_CHECKED = "is_checked";
    private static final String KEY_LAST_COLOR = "last_color";
    private static final String KEY_LED_SETTINGS = "led_settings";
    private static final String KEY_LOCKGUARD_WALLPAPER = "lockguard_wallpaper_settings";
    private static final String KEY_MANUAL_BRIGHT = "manual_brightness_displays";
    private static final String KEY_NETWORK_NAME_DISPLAYED = "network_operator_display";
    private static final String KEY_NIGHT_MODE = "oneplus_night_mode";
    private static final String KEY_NOTCH_MODE = "oneplus_notch_display_guide";
    private static final String KEY_ONEPLUS_NOTCH_AREA = "oneplus_notch_area";
    private static final String KEY_ONEPLUS_NOTCH_FULLSCREEN_APP = "oneplus_notch_fullscreen_app";
    private static final String KEY_ONEPLUS_SCREEN_REFRESH_RATE = "oneplus_screen_refresh_rate";
    private static final String KEY_ONEPLUS_SCREEN_RESOLUTION_ADJUST = "oneplus_screen_resolution_adjust";
    private static final String KEY_READING_MODE = "oneplus_reading_mode";
    private static final String KEY_SCREEN_BRIGHTNESS = "screen_brightness";
    private static final String KEY_SCREEN_COLOR_MODE = "screen_color_mode";
    private static final String KEY_SCREEN_SAVER = "screensaver";
    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_STATUS_BAR_CUSTOM = "status_bar_custom";
    private static final String KEY_TAP_TO_WAKE = "tap_to_wake";
    private static final String KEY_THEME_ACCENT_COLOR = "theme_accent_color";
    private static final String KEY_THEME_MODE = "op_theme_mode";
    private static final String KEY_VIDEO_ENHANCER = "video_enhancer";
    private static final String KEY_VR_DISPLAY_PREF = "vr_display_pref";
    private static final String KEY_WALLPAPER = "wallpaper";
    private static final int MAX_COLOR_COUNT = 7;
    public static final int MGS_THEME_STAR_WAR_MODE_CHANGE = 101;
    public static final int MSG_THEME_MODE_CHANGE = 100;
    public static final String NIGHT_MODE_ENABLED = "night_mode_enabled";
    private static final String NOTIFY_LIGHT_ENABLE_KEY = "notify_light_enable";
    private static final String OEM_BLACK_MODE_ACCENT_COLOR = "oem_black_mode_accent_color";
    private static final String OEM_BLACK_MODE_ACCENT_COLOR_INDEX = "oem_black_mode_accent_color_index";
    private static final String OEM_WHITE_MODE_ACCENT_COLOR = "oem_white_mode_accent_color";
    private static final String OEM_WHITE_MODE_ACCENT_COLOR_INDEX = "oem_white_mode_accent_color_index";
    private static final String ONEPLUS_NOTCH_MODE = "op_camera_notch_ignore";
    private static final int OP_AUTO_MODE_VALUE = 2;
    private static final String OP_SYS_SRGB_PROPERTY = "sys.srgb";
    private static final String OP_THEME_PACKAGE = "com.oneplus.skin";
    private static final String OXYGEN_THEME_INTENT = "com.oneplus.oxygen.changetheme";
    private static final String OXYGEN_THEME_INTENT_EXTRA = "oxygen_theme_status";
    public static final String SCREEN_AUTO_BRIGHTNESS_ADJ = "screen_auto_brightness_adj";
    private static final int SCREEN_COLOR_MODE_ADAPTIVE_MODEL_SETTINGS_VALUE = 5;
    private static final int SCREEN_COLOR_MODE_AUTO_SETTINGS_VALUE = 10;
    private static final int SCREEN_COLOR_MODE_BASIC_SETTINGS_VALUE = 2;
    private static final int SCREEN_COLOR_MODE_DCI_P3_SETTINGS_VALUE = 4;
    private static final int SCREEN_COLOR_MODE_DEFAULT_SETTINGS_VALUE = 1;
    private static final int SCREEN_COLOR_MODE_DEFINED_SETTINGS_VALUE = 3;
    private static final int SCREEN_COLOR_MODE_SOFT_SETTINGS_VALUE = 6;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.display_settings;
            result.add(sir);
            return result;
        }

        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            if (OPUtils.isSupportScreenCutting() && OPUtils.isSupportScreenDisplayAdaption()) {
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(R.string.oneplus_app_display_fullscreen_title);
                data.screenTitle = res.getString(R.string.display_settings);
                result.add(data);
            }
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            if (!context.getResources().getBoolean(17956946)) {
                result.add(DisplaySettings.KEY_SCREEN_SAVER);
            }
            if (!DisplaySettings.isAutomaticBrightnessAvailable(context.getResources())) {
                result.add(DisplaySettings.KEY_AUTO_BRIGHTNESS);
            }
            if (!DisplaySettings.isDozeAvailable(context)) {
                result.add(DisplaySettings.KEY_DOZE);
                result.add(DisplaySettings.KEY_DOZE_801);
            }
            if (OPUtils.isSupportCustomFingerprint()) {
                result.add(DisplaySettings.KEY_DOZE);
            } else {
                result.add(DisplaySettings.KEY_DOZE_801);
            }
            if (!RotationPolicy.isRotationLockToggleVisible(context)) {
                result.add(DisplaySettings.KEY_AUTO_ROTATE);
            }
            if (!DisplaySettings.isTapToWakeAvailable(context.getResources())) {
                result.add(DisplaySettings.KEY_TAP_TO_WAKE);
            }
            if (!DisplaySettings.isCameraGestureAvailable(context.getResources())) {
                result.add(DisplaySettings.KEY_CAMERA_GESTURE);
            }
            if (!DisplaySettings.isCameraDoubleTapPowerGestureAvailable(context.getResources())) {
                result.add(DisplaySettings.KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);
            }
            if (!DisplaySettings.isVrDisplayModeAvailable(context)) {
                result.add(DisplaySettings.KEY_VR_DISPLAY_PREF);
            }
            result.add(DisplaySettings.KEY_BACK_TOP_THEME);
            if (!DisplaySettings.isOnePlusLaunchrSupportSetWallpaper()) {
                result.add(DisplaySettings.KEY_LOCKGUARD_WALLPAPER);
            }
            if (OPUtils.isGuestMode()) {
                result.add(DisplaySettings.KEY_SCREEN_COLOR_MODE);
            }
            if (!DisplaySettings.isSupportReadingMode) {
                result.add(DisplaySettings.KEY_READING_MODE);
            }
            if (!OPUtils.isSupportVideoEnhancer()) {
                result.add(DisplaySettings.KEY_VIDEO_ENHANCER);
            }
            if (!OPUtils.isSupportScreenDisplayAdaption() || OPUtils.isSupportScreenCutting() || OPUtils.isSupportAppsDisplayInFullscreen()) {
                result.add(DisplaySettings.KEY_DISPLAY_SIZE_ADAPTION);
            }
            if (!OPUtils.isSupportScreenCutting()) {
                result.add(DisplaySettings.KEY_ONEPLUS_NOTCH_AREA);
                result.add(DisplaySettings.KEY_NOTCH_MODE);
                result.add(DisplaySettings.KEY_ONEPLUS_NOTCH_FULLSCREEN_APP);
            }
            if (OPUtils.isGuestMode()) {
                result.add(DisplaySettings.KEY_NOTCH_MODE);
            }
            if (OPUtils.isGuestMode() || !OPUtils.isSupportCustomBlinkLight()) {
                result.add(DisplaySettings.KEY_LED_SETTINGS);
                result.add(DisplaySettings.NOTIFY_LIGHT_ENABLE_KEY);
            }
            if (!OPUtils.isSupportScreenRefreshRate()) {
                result.add(DisplaySettings.KEY_ONEPLUS_SCREEN_REFRESH_RATE);
            }
            if (!OPUtils.isSupportScreenRefreshRate() || OPUtils.isGuestMode()) {
                result.add("oneplus_screen_resolution_adjust");
            }
            return result;
        }
    };
    private static final String SHARED_PREFERENCES_NAME = "customization_settings";
    private static final String SHOW_NETWORK_NAME_MODE = "show_network_name_mode";
    private static final int SHOW_NETWORK_NAME_OFF = 0;
    private static final int SHOW_NETWORK_NAME_ON = 1;
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader, null);
        }
    };
    private static final String TAG = "DisplaySettings";
    private static final int THEME_ANDROID_MODE = 2;
    private static final int THEME_DARK_MODE = 1;
    private static final int THEME_LIGHT_MODE = 0;
    private static final String THEME_MODE_ACTION = "android.settings.OEM_THEME_MODE";
    private static final int THEME_MODE_STAR_WAR_VALUE = 1;
    private static final String TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE = "toggle_lock_screen_rotation_preference";
    private static boolean ValueAnimatorFlag = true;
    private static boolean isSupportReadingMode = false;
    private static final String sDCI_P3Path = "/sys/devices/virtual/graphics/fb0/DCI_P3";
    private static final String sOPEN_VALUE = "mode = 1";
    private static final String sRGBPath = "/sys/devices/virtual/graphics/fb0/SRGB";
    private boolean isAutoSwitchClickedDrivenBrightnessChange;
    private ColorPickerPreference mAccentColorPreference;
    private ContentObserver mAccessibilityDisplayDaltonizerAndInversionContentObserver = new ContentObserver(new Handler()) {
        private final Uri ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED_URI = Secure.getUriFor("accessibility_display_daltonizer_enabled");
        private final Uri ACCESSIBILITY_DISPLAY_GRAYSCALE_ENABLED_URI = System.getUriFor("accessibility_display_grayscale_enabled");
        private final Uri ACCESSIBILITY_DISPLAY_INVERSION_ENABLED_URI = Secure.getUriFor("accessibility_display_inversion_enabled");

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED_URI.equals(uri) || this.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED_URI.equals(uri) || this.ACCESSIBILITY_DISPLAY_GRAYSCALE_ENABLED_URI.equals(uri)) {
                Preference access$100;
                boolean z;
                boolean z2 = false;
                boolean isDisplayDaltonizeEnabled = Secure.getInt(DisplaySettings.this.getContentResolver(), "accessibility_display_daltonizer_enabled", 12) == 1;
                boolean isDisplayInversionEnabled = Secure.getInt(DisplaySettings.this.getContentResolver(), "accessibility_display_inversion_enabled", 0) == 1;
                boolean isWellbeingGrayscaleEnabled = System.getInt(DisplaySettings.this.getContentResolver(), "accessibility_display_grayscale_enabled", 1) == 0;
                boolean OPNightModeState = Secure.getIntForUser(DisplaySettings.this.getContentResolver(), "night_display_activated", 0, -2) == 1;
                if (DisplaySettings.this.mNightModePreference != null) {
                    access$100 = DisplaySettings.this.mNightModePreference;
                    z = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled || isWellbeingGrayscaleEnabled) ? false : true;
                    access$100.setEnabled(z);
                }
                if (DisplaySettings.this.mScreenColorModePreference != null) {
                    access$100 = DisplaySettings.this.mScreenColorModePreference;
                    z = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled || OPNightModeState || isWellbeingGrayscaleEnabled) ? false : true;
                    access$100.setEnabled(z);
                }
                if (DisplaySettings.this.mReadingModePreference != null) {
                    access$100 = DisplaySettings.this.mReadingModePreference;
                    if (!(isDisplayDaltonizeEnabled || isDisplayInversionEnabled || isWellbeingGrayscaleEnabled)) {
                        z2 = true;
                    }
                    access$100.setEnabled(z2);
                }
            }
        }
    };
    private SwitchPreference mAutoBrightnessPreference;
    private boolean mAutomatic;
    private boolean mAutomaticAvailable;
    private SwitchPreference mBacktopThemePreference;
    private int[] mBlackColorStringIds;
    private String[] mBlackColors;
    private OPBrightnessSeekbarPreferenceCategory mBrightPreference;
    private BrightnessObserver mBrightnessObserver;
    private SwitchPreference mCameraDoubleTapPowerGesturePreference;
    private SwitchPreference mCameraGesturePreference;
    private String[] mColors;
    private Context mContext;
    private String mCurrentTempColor;
    private PreferenceCategory mCustomRootPreference;
    private int mDarkModeEnable;
    private SwitchPreference mDarkModePreferce;
    private DarkModeRunnable mDarkModeRunnable = new DarkModeRunnable();
    private int mDefaultBacklight;
    private int mDefaultBacklightForVr;
    private DefaultHandler mDefaultHandler;
    private int mDefaultThemeMode = 0;
    private DisplayManager mDisplayManager;
    private Preference mDisplaySizeAdaptionPreference;
    private Preference mDozePreference;
    private Preference mFontSizePref;
    private Handler mHandler;
    private Preference mLedSettingsPreference;
    private Preference mLockWallPaperPreference;
    private int mMaximumBacklight;
    private int mMaximumBacklightForVr;
    private int mMinimumBacklight;
    private int mMinimumBacklightForVr;
    private SwitchPreference mNetworkNameDisplayedPreference = null;
    private boolean mNewController = false;
    private int mNightModeLevel = -1;
    private ContentObserver mNightModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            boolean OPNightModeState = Secure.getIntForUser(DisplaySettings.this.getContentResolver(), "night_display_activated", 0, -2) != 0;
            boolean OPReadingModeState = System.getIntForUser(DisplaySettings.this.getContentResolver(), OPReadingMode.READING_MODE_STATUS, 0, -2) != 0;
            if (OPNightModeState) {
                DisplaySettings.this.mScreenColorModePreference.setEnabled(false);
                DisplaySettings.this.mScreenColorModePreference.setSummary(DisplaySettings.this.getResources().getString(R.string.oneplus_screen_color_mode_title_summary));
            } else if (OPReadingModeState && DisplaySettings.isSupportReadingMode) {
                DisplaySettings.this.mScreenColorModePreference.setEnabled(false);
                DisplaySettings.this.mScreenColorModePreference.setSummary(DisplaySettings.this.getResources().getString(R.string.oneplus_screen_color_mode_reading_mode_on_summary));
            } else {
                DisplaySettings.this.updateScreenColorModePreference();
                DisplaySettings.this.mScreenColorModePreference.setEnabled(true);
            }
        }
    };
    private Preference mNightModePreference;
    private Preference mNotchModeAppPreference;
    private Preference mNotchModePreference;
    private int mNotifyLightEnable;
    private SwitchPreference mNotifyLightPreference;
    private IPowerManager mPower;
    private Preference mReadingModePreference;
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        public void onChange() {
            DisplaySettings.this.updateLockScreenRotation();
        }
    };
    private PreferenceCategory mScreenBrightnessRootPreference;
    private Preference mScreenColorModePreference;
    private Preference mScreenRefreshRate;
    private Preference mScreenResolutionAdjust;
    private Preference mScreenSaverPreference;
    private TimeoutListPreference mScreenTimeoutPreference;
    private ValueAnimator mSliderAnimator;
    private PreferenceCategory mSystemRootPreference;
    private SwitchPreference mTapToWakePreference;
    private ListPreference mThemeModePreference;
    private SwitchPreference mToggleLockScreenRotationPreference;
    private Preference mVideoEnhancerPreference;
    private int[] mWhiteColorStringIds;
    private String[] mWhiteColors;

    private class BrightnessObserver extends ContentObserver {
        private final Uri BRIGHTNESS_ADJ_URI = System.getUriFor(DisplaySettings.SCREEN_AUTO_BRIGHTNESS_ADJ);
        private final Uri BRIGHTNESS_MODE_URI = System.getUriFor("screen_brightness_mode");
        private final Uri BRIGHTNESS_URI = System.getUriFor(DisplaySettings.KEY_SCREEN_BRIGHTNESS);
        private final Uri SCREEN_TIMEOUT_URI = System.getUriFor("screen_off_timeout");

        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (!selfChange) {
                if (this.BRIGHTNESS_MODE_URI.equals(uri)) {
                    DisplaySettings.this.updateMode();
                } else if (this.BRIGHTNESS_URI.equals(uri) || this.BRIGHTNESS_ADJ_URI.equals(uri)) {
                    DisplaySettings.this.updateSlider();
                } else if (this.SCREEN_TIMEOUT_URI.equals(uri)) {
                    int currentTimeout = System.getInt(DisplaySettings.this.mContext.getContentResolver(), "screen_off_timeout", 30000);
                    DisplaySettings.this.mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
                    DisplaySettings.this.updateTimeoutPreferenceDescription((long) currentTimeout);
                }
            }
        }

        public void startObserving() {
            ContentResolver cr = DisplaySettings.this.mContext.getContentResolver();
            cr.unregisterContentObserver(this);
            cr.registerContentObserver(this.BRIGHTNESS_MODE_URI, false, this, -1);
            cr.registerContentObserver(this.BRIGHTNESS_URI, false, this, -1);
            cr.registerContentObserver(this.BRIGHTNESS_ADJ_URI, false, this, -1);
            cr.registerContentObserver(this.SCREEN_TIMEOUT_URI, false, this, -1);
        }

        public void stopObserving() {
            DisplaySettings.this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    class DarkModeRunnable implements Runnable {
        boolean dValue = false;

        public DarkModeRunnable(boolean value) {
            this.dValue = value;
        }

        public void setValue(boolean value) {
            this.dValue = value;
        }

        public void run() {
            DisplaySettings.this.setAccentColor();
            System.putInt(DisplaySettings.this.mContext.getContentResolver(), DisplaySettings.KEY_DARK_MODE_ACTION, this.dValue);
            Intent intent = new Intent(DisplaySettings.THEME_MODE_ACTION);
            intent.setPackage(DisplaySettings.OP_THEME_PACKAGE);
            intent.addFlags(268435456);
            intent.putExtra(DisplaySettings.KEY_DARK_MODE_ACTION, this.dValue);
            DisplaySettings.this.mContext.sendBroadcast(intent);
            OPUtils.sendAppTrackerForThemes();
        }
    }

    class DefaultHandler extends Handler {
        private final WeakReference<Context> mReference;

        public DefaultHandler(Context settings) {
            this.mReference = new WeakReference(settings);
        }

        public void handleMessage(Message msg) {
            Context service = (Context) this.mReference.get();
            if (service != null) {
                Intent intent;
                switch (msg.what) {
                    case 100:
                        intent = new Intent(DisplaySettings.THEME_MODE_ACTION);
                        intent.setPackage(DisplaySettings.OP_THEME_PACKAGE);
                        intent.addFlags(268435456);
                        intent.putExtra("oem_theme_mode", msg.arg1);
                        intent.putExtra("special_theme", false);
                        DisplaySettings.this.setAccentColor();
                        service.sendBroadcast(intent);
                        break;
                    case 101:
                        intent = new Intent(DisplaySettings.THEME_MODE_ACTION);
                        intent.setPackage(DisplaySettings.OP_THEME_PACKAGE);
                        intent.addFlags(268435456);
                        intent.putExtra("oem_theme_mode", msg.arg1);
                        intent.putExtra("special_theme", true);
                        if (CUSTOM_TYPE.SW.equals(OpCustomizeSettings.getCustomType())) {
                            System.putString(DisplaySettings.this.getContentResolver(), OPConstants.ONEPLUS_ACCENT_COLOR, OPConstants.ONEPLUS_ACCENT_SW_COLOR);
                            SystemProperties.set(DisplaySettings.KEY_CUSTOM_ACCENT_COLOR, OPConstants.ONEPLUS_ACCENT_SW_COLOR.replace("#", ""));
                        } else if (CUSTOM_TYPE.AVG.equals(OpCustomizeSettings.getCustomType())) {
                            System.putString(DisplaySettings.this.getContentResolver(), OPConstants.ONEPLUS_ACCENT_COLOR, OPConstants.ONEPLUS_ACCENT_AVG_COLOR);
                            SystemProperties.set(DisplaySettings.KEY_CUSTOM_ACCENT_COLOR, OPConstants.ONEPLUS_ACCENT_AVG_COLOR.replace("#", ""));
                        } else if (CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType())) {
                            System.putString(DisplaySettings.this.getContentResolver(), OPConstants.ONEPLUS_ACCENT_COLOR, OPConstants.ONEPLUS_ACCENT_MCL_COLOR);
                            SystemProperties.set(DisplaySettings.KEY_CUSTOM_ACCENT_COLOR, OPConstants.ONEPLUS_ACCENT_MCL_COLOR.replace("#", ""));
                        } else {
                            System.putString(DisplaySettings.this.getContentResolver(), OPConstants.ONEPLUS_ACCENT_COLOR, OPConstants.ONEPLUS_ACCENT_DEFAULT_COLOR);
                            SystemProperties.set(DisplaySettings.KEY_CUSTOM_ACCENT_COLOR, OPConstants.ONEPLUS_ACCENT_DEFAULT_COLOR.replace("#", ""));
                        }
                        service.sendBroadcast(intent);
                        break;
                }
            }
        }
    }

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
            if (System.getInt(this.mContext.getContentResolver(), "screen_brightness_mode", 1) == 1) {
                int i = 1;
            } else {
                boolean auto = false;
            }
            if (OPUtils.isGuestMode()) {
                String summary = this.mContext.getString(R.string.status_bar_title);
                String dozeSummary = this.mContext.getString(R.string.oneplus_doze_title_801).toLowerCase();
                this.mLoader.setSummary(this, this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary, dozeSummary}));
                return;
            }
            this.mLoader.setSummary(this, this.mContext.getString(R.string.oneplus_display_dashboard_summary));
            if (OPUtils.isSupportCustomFingerprint()) {
                this.mLoader.setSummary(this, this.mContext.getString(R.string.oneplus_display_dashboard_summary_801));
            }
        }
    }

    public int getMetricsCategory() {
        return 46;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context activity = getActivity();
        ContentResolver resolver = activity.getContentResolver();
        this.mContext = getActivity();
        this.mDefaultHandler = new DefaultHandler(activity.getApplication());
        addPreferencesFromResource(R.xml.display_settings);
        Resources res = this.mContext.getResources();
        initAccentColors(res);
        this.mScreenBrightnessRootPreference = (PreferenceCategory) findPreference(KEY_SCREEN_BRIGHTNESS);
        this.mSystemRootPreference = (PreferenceCategory) findPreference(KEY_DISPLAY_SYSTEM);
        this.mCustomRootPreference = (PreferenceCategory) findPreference(KEY_CATEGOREY_CUSTOM);
        this.mScreenSaverPreference = findPreference(KEY_SCREEN_SAVER);
        if (!(this.mScreenSaverPreference == null || getResources().getBoolean(17956946))) {
            getPreferenceScreen().removePreference(this.mScreenSaverPreference);
        }
        this.mScreenTimeoutPreference = (TimeoutListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        this.mFontSizePref = findPreference(KEY_FONT_SIZE);
        if (isAutomaticBrightnessAvailable(getResources())) {
            this.mAutoBrightnessPreference = (SwitchPreference) findPreference(KEY_AUTO_BRIGHTNESS);
            this.mAutoBrightnessPreference.setOnPreferenceChangeListener(this);
        } else {
            removePreference(KEY_AUTO_BRIGHTNESS);
        }
        if (isDozeAvailable(activity)) {
            if (OPUtils.isSupportCustomFingerprint()) {
                this.mDozePreference = findPreference(KEY_DOZE_801);
                this.mScreenBrightnessRootPreference.removePreference(findPreference(KEY_DOZE));
            } else {
                this.mDozePreference = findPreference(KEY_DOZE);
                this.mScreenBrightnessRootPreference.removePreference(findPreference(KEY_DOZE_801));
                if (OPUtils.isSupportAlwaysOnDisplay()) {
                    this.mDozePreference.setSummary((int) R.string.oneplus_hand_up_proximity_title);
                }
            }
            this.mDozePreference.setOnPreferenceClickListener(this);
        } else {
            removePreference(KEY_DOZE);
            removePreference(KEY_DOZE_801);
        }
        boolean z = true;
        if (isVrDisplayModeAvailable(activity)) {
            DropDownPreference vrDisplayPref = (DropDownPreference) findPreference(KEY_VR_DISPLAY_PREF);
            vrDisplayPref.setEntries(new CharSequence[]{activity.getString(R.string.display_vr_pref_low_persistence), activity.getString(R.string.display_vr_pref_off)});
            vrDisplayPref.setEntryValues(new CharSequence[]{"0", "1"});
            final Context c = activity;
            vrDisplayPref.setValueIndex(Secure.getIntForUser(c.getContentResolver(), "vr_display_mode", 0, ActivityManager.getCurrentUser()));
            vrDisplayPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (!Secure.putIntForUser(c.getContentResolver(), "vr_display_mode", Integer.parseInt((String) newValue), ActivityManager.getCurrentUser())) {
                        Log.e(DisplaySettings.TAG, "Could not change setting for vr_display_mode");
                    }
                    return true;
                }
            });
        } else {
            this.mSystemRootPreference.removePreference((DropDownPreference) findPreference(KEY_VR_DISPLAY_PREF));
        }
        this.mHandler = new Handler();
        this.mDarkModePreferce = (SwitchPreference) findPreference(KEY_DARK_MODE);
        this.mDarkModePreferce.setOnPreferenceChangeListener(this);
        this.mDarkModeEnable = System.getInt(getActivity().getContentResolver(), KEY_DARK_MODE_ACTION, 0);
        this.mDarkModePreferce.setChecked(this.mDarkModeEnable != 0);
        this.mCustomRootPreference.removePreference(this.mDarkModePreferce);
        this.mThemeModePreference = (ListPreference) findPreference(KEY_THEME_MODE);
        CharSequence[] entries;
        CharSequence[] entriesvalue;
        if (CUSTOM_TYPE.SW.equals(OpCustomizeSettings.getCustomType())) {
            entries = getResources().getStringArray(R.array.oneplus_theme_mode_starwar_entries);
            entriesvalue = getResources().getStringArray(R.array.oneplus_theme_mode_starwar_values);
            this.mThemeModePreference.setEntries(entries);
            this.mThemeModePreference.setEntryValues(entriesvalue);
        } else if (CUSTOM_TYPE.AVG.equals(OpCustomizeSettings.getCustomType())) {
            entries = getResources().getStringArray(R.array.oneplus_theme_2_entries);
            entriesvalue = getResources().getStringArray(R.array.oneplus_theme_2__values);
            this.mThemeModePreference.setEntries(entries);
            this.mThemeModePreference.setEntryValues(entriesvalue);
        } else if (CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType())) {
            entries = getResources().getStringArray(R.array.oneplus_theme_3_entries);
            entriesvalue = getResources().getStringArray(R.array.oneplus_theme_3_values);
            this.mThemeModePreference.setEntries(entries);
            this.mThemeModePreference.setEntryValues(entriesvalue);
        }
        int themeMode = System.getInt(getActivity().getContentResolver(), KEY_DARK_MODE_ACTION, this.mDefaultThemeMode);
        int specialthemeMode = System.getInt(getActivity().getContentResolver(), "oem_special_theme", 0);
        if (themeMode == 1 && specialthemeMode == 1) {
            this.mThemeModePreference.setValue(String.valueOf(3));
        } else {
            this.mThemeModePreference.setValue(String.valueOf(themeMode));
        }
        this.mThemeModePreference.setOnPreferenceChangeListener(this);
        updateThemeModePreferenceDescription(getThemeModeValue(themeMode));
        if (!getActivity().getPackageManager().hasSystemFeature("oem.op_dark_mode.support") || OPUtils.isGuestMode()) {
            this.mCustomRootPreference.removePreference(this.mThemeModePreference);
        }
        this.mToggleLockScreenRotationPreference = (SwitchPreference) findPreference(TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE);
        if (!RotationPolicy.isRotationSupported(getActivity())) {
            removePreference(TOGGLE_LOCK_SCREEN_ROTATION_PREFERENCE);
        }
        this.mBacktopThemePreference = (SwitchPreference) findPreference(KEY_BACK_TOP_THEME);
        this.mBacktopThemePreference.setOnPreferenceClickListener(this);
        if (this.mBacktopThemePreference != null) {
            this.mCustomRootPreference.removePreference(this.mBacktopThemePreference);
        }
        this.mNotifyLightEnable = System.getInt(getActivity().getContentResolver(), "oem_acc_breath_light", 0);
        this.mNotifyLightPreference = (SwitchPreference) findPreference(NOTIFY_LIGHT_ENABLE_KEY);
        this.mNotifyLightPreference.setOnPreferenceChangeListener(this);
        SwitchPreference switchPreference = this.mNotifyLightPreference;
        if (this.mNotifyLightEnable == 0) {
            z = false;
        }
        switchPreference.setChecked(z);
        this.mLockWallPaperPreference = findPreference(KEY_LOCKGUARD_WALLPAPER);
        this.mLockWallPaperPreference.setOnPreferenceClickListener(this);
        if (!isOnePlusLaunchrSupportSetWallpaper()) {
            this.mCustomRootPreference.removePreference(this.mLockWallPaperPreference);
        }
        PowerManager pm = (PowerManager) getActivity().getSystemService("power");
        this.mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
        this.mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();
        this.mDefaultBacklight = pm.getDefaultScreenBrightnessSetting();
        this.mMinimumBacklightForVr = pm.getMinimumScreenBrightnessForVrSetting();
        this.mMaximumBacklightForVr = pm.getMaximumScreenBrightnessForVrSetting();
        this.mDefaultBacklightForVr = pm.getDefaultScreenBrightnessForVrSetting();
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mAutomaticAvailable = getActivity().getResources().getBoolean(17956895);
        this.mPower = Stub.asInterface(ServiceManager.getService("power"));
        this.mNewController = this.mContext.getPackageManager().hasSystemFeature("oem.autobrightctl.animation.support");
        this.mBrightPreference = (OPBrightnessSeekbarPreferenceCategory) findPreference(KEY_MANUAL_BRIGHT);
        this.mBrightPreference.setCallback(this);
        this.mBrightnessObserver = new BrightnessObserver(this.mHandler);
        this.mBrightnessObserver.startObserving();
        this.mAccentColorPreference = (ColorPickerPreference) findPreference(KEY_THEME_ACCENT_COLOR);
        this.mAccentColorPreference.setCustomColorClickListener(this);
        setCustomAccentColor();
        this.mLedSettingsPreference = findPreference(KEY_LED_SETTINGS);
        if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver())) {
            this.mAccentColorPreference.setColorPalette(this.mWhiteColors, this.mWhiteColorStringIds);
            this.mAccentColorPreference.setDefaultColor(res.getString(R.color.op_primary_default_light));
        } else if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            this.mAccentColorPreference.setColorPalette(this.mBlackColors, this.mBlackColorStringIds);
            this.mAccentColorPreference.setDefaultColor(res.getString(R.color.op_primary_default_dark));
        } else {
            this.mAccentColorPreference.setDefaultColor(res.getString(R.color.op_primary_default_light));
        }
        this.mAccentColorPreference.setMessageText((int) R.string.color_picker_accent_color_message);
        updateState();
        if (OPUtils.isGuestMode() || !OPUtils.isSupportCustomBlinkLight()) {
            this.mCustomRootPreference.removePreference(this.mNotifyLightPreference);
            this.mCustomRootPreference.removePreference(this.mLedSettingsPreference);
        }
        this.mScreenColorModePreference = findPreference(KEY_SCREEN_COLOR_MODE);
        isSupportReadingMode = this.mContext.getPackageManager().hasSystemFeature("oem.read_mode.support");
        this.mNightModePreference = findPreference(KEY_NIGHT_MODE);
        this.mReadingModePreference = findPreference(KEY_READING_MODE);
        if (!(this.mReadingModePreference == null || isSupportReadingMode)) {
            this.mScreenBrightnessRootPreference.removePreference(this.mReadingModePreference);
        }
        if (!(!OPUtils.isGuestMode() || this.mScreenBrightnessRootPreference == null || this.mScreenColorModePreference == null)) {
            this.mScreenBrightnessRootPreference.removePreference(this.mScreenColorModePreference);
        }
        this.mVideoEnhancerPreference = findPreference(KEY_VIDEO_ENHANCER);
        if (!(OPUtils.isSupportVideoEnhancer() || this.mVideoEnhancerPreference == null)) {
            this.mScreenBrightnessRootPreference.removePreference(this.mVideoEnhancerPreference);
        }
        this.mDisplaySizeAdaptionPreference = findPreference(KEY_DISPLAY_SIZE_ADAPTION);
        this.mDisplaySizeAdaptionPreference.setOnPreferenceClickListener(this);
        this.mNotchModePreference = findPreference(KEY_NOTCH_MODE);
        this.mNotchModeAppPreference = findPreference(KEY_ONEPLUS_NOTCH_FULLSCREEN_APP);
        this.mNotchModeAppPreference.setOnPreferenceClickListener(this);
        if (OPUtils.isSupportScreenCutting()) {
            this.mScreenBrightnessRootPreference.removePreference(this.mDisplaySizeAdaptionPreference);
        } else {
            this.mScreenBrightnessRootPreference.removePreference(this.mNotchModePreference);
            this.mScreenBrightnessRootPreference.removePreference(this.mNotchModeAppPreference);
            if (!(OPUtils.isSupportScreenDisplayAdaption() && OPUtils.isSupportAppsDisplayInFullscreen())) {
                this.mScreenBrightnessRootPreference.removePreference(this.mDisplaySizeAdaptionPreference);
            }
        }
        if (OPUtils.isGuestMode() && this.mNotchModePreference != null) {
            this.mNotchModePreference.setVisible(false);
        }
        if (!OPUtils.isSupportFontStyleSetting() || OPUtils.isGuestMode()) {
            this.mCustomRootPreference.removePreference(findPreference("font_style_settings"));
        }
        if (OPUtils.isGuestMode()) {
            removePreference(KEY_CATEGOREY_CUSTOM);
        }
        if (this.mSliderAnimator != null) {
            this.mSliderAnimator.cancel();
        }
        if (UserHandle.myUserId() != 0) {
            removePreference(KEY_STATUS_BAR_CUSTOM);
        }
        this.mScreenRefreshRate = findPreference(KEY_ONEPLUS_SCREEN_REFRESH_RATE);
        if (!OPUtils.isSupportScreenRefreshRate()) {
            this.mScreenRefreshRate.setVisible(false);
        }
        this.mScreenResolutionAdjust = findPreference("oneplus_screen_resolution_adjust");
        if (!OPUtils.isSupportScreenRefreshRate() || OPUtils.isGuestMode()) {
            this.mScreenResolutionAdjust.setVisible(false);
        }
    }

    private static boolean allowAllRotations(Context context) {
        return Resources.getSystem().getBoolean(17956870);
    }

    private static boolean isDozeAvailable(Context context) {
        String name = Build.IS_DEBUGGABLE ? SystemProperties.get("debug.doze.component") : null;
        if (TextUtils.isEmpty(name)) {
            name = context.getResources().getString(17039690);
        }
        return TextUtils.isEmpty(name) ^ 1;
    }

    private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(17957046);
    }

    private static boolean isAutomaticBrightnessAvailable(Resources res) {
        return true;
    }

    private static boolean isCameraGestureAvailable(Resources res) {
        if (!(res.getInteger(17694753) != -1) || SystemProperties.getBoolean("gesture.disable_camera_launch", false)) {
            return false;
        }
        return true;
    }

    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(17956909);
    }

    private static boolean isVrDisplayModeAvailable(Context context) {
        PackageManager pm = context.getPackageManager();
        return false;
    }

    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        if (isAdded()) {
            String summary;
            TimeoutListPreference preference = this.mScreenTimeoutPreference;
            if (preference.isDisabledByAdmin()) {
                summary = this.mContext.getResources().getString(R.string.disabled_by_policy_title);
            } else if (currentTimeout < 0) {
                summary = "";
            } else {
                CharSequence[] entries = preference.getEntries();
                CharSequence[] values = preference.getEntryValues();
                if (entries == null || entries.length == 0) {
                    summary = "";
                } else {
                    int best = 0;
                    for (int i = 0; i < values.length; i++) {
                        if (currentTimeout >= Long.parseLong(values[i].toString())) {
                            best = i;
                        }
                    }
                    summary = this.mContext.getResources().getString(R.string.screen_timeout_summary, new Object[]{entries[best]});
                }
            }
            preference.setSummary(summary);
        }
    }

    public void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(Secure.getUriFor("night_display_activated"), true, this.mNightModeObserver, -1);
        getContentResolver().registerContentObserver(System.getUriFor(OPReadingMode.READING_MODE_STATUS), true, this.mNightModeObserver, -1);
        getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_daltonizer_enabled"), true, this.mAccessibilityDisplayDaltonizerAndInversionContentObserver, -1);
        getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_display_inversion_enabled"), true, this.mAccessibilityDisplayDaltonizerAndInversionContentObserver, -1);
        getContentResolver().registerContentObserver(System.getUriFor("accessibility_display_grayscale_enabled"), true, this.mAccessibilityDisplayDaltonizerAndInversionContentObserver, -1);
    }

    public void onResume() {
        EnforcedAdmin admin;
        super.onResume();
        updateState();
        long currentTimeout = System.getLong(getActivity().getContentResolver(), "screen_off_timeout", 30000);
        this.mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        this.mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        DevicePolicyManager dpm = (DevicePolicyManager) getActivity().getSystemService("device_policy");
        if (dpm != null) {
            admin = RestrictedLockUtils.checkIfMaximumTimeToLockIsSet(getActivity());
            this.mScreenTimeoutPreference.removeUnusableTimeouts(dpm.getMaximumTimeToLock(0, UserHandle.myUserId()), admin);
        }
        updateTimeoutPreferenceDescription(currentTimeout);
        admin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_config_screen_timeout", UserHandle.myUserId());
        if (admin != null) {
            this.mScreenTimeoutPreference.removeUnusableTimeouts(0, admin);
        }
        disablePreferenceIfManaged("wallpaper", "no_set_wallpaper");
        updateLockScreenRotation();
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.registerRotationPolicyListener(getActivity(), this.mRotationPolicyListener);
        }
        SwitchPreference switchPreference = this.mBacktopThemePreference;
        boolean z = false;
        if (System.getInt(getActivity().getContentResolver(), "oem_acc_backgap_theme", 0) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
        disableEntryForAccessibilityDisplayDaltonizerAndInversion();
    }

    private void disableEntryForAccessibilityDisplayDaltonizerAndInversion() {
        boolean z = false;
        boolean isDisplayDaltonizeEnabled = Secure.getInt(getContentResolver(), "accessibility_display_daltonizer_enabled", 12) == 1;
        boolean isDisplayInversionEnabled = Secure.getInt(getContentResolver(), "accessibility_display_inversion_enabled", 0) == 1;
        boolean isWellbeingGrayscaleEnabled = System.getInt(getContentResolver(), "accessibility_display_grayscale_enabled", 1) == 0;
        if (isDisplayDaltonizeEnabled || isDisplayInversionEnabled || isWellbeingGrayscaleEnabled) {
            Preference preference;
            boolean z2;
            if (this.mNightModePreference != null) {
                preference = this.mNightModePreference;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled || isWellbeingGrayscaleEnabled) ? false : true;
                preference.setEnabled(z2);
            }
            if (this.mScreenColorModePreference != null) {
                preference = this.mScreenColorModePreference;
                z2 = (isDisplayDaltonizeEnabled || isDisplayInversionEnabled || isWellbeingGrayscaleEnabled) ? false : true;
                preference.setEnabled(z2);
            }
            if (this.mReadingModePreference != null) {
                preference = this.mReadingModePreference;
                if (!(isDisplayDaltonizeEnabled || isDisplayInversionEnabled || isWellbeingGrayscaleEnabled)) {
                    z = true;
                }
                preference.setEnabled(z);
            }
        }
    }

    public void onPause() {
        super.onPause();
        this.mAccentColorPreference.onDismiss(null);
        if (RotationPolicy.isRotationSupported(getActivity())) {
            RotationPolicy.unregisterRotationPolicyListener(getActivity(), this.mRotationPolicyListener);
        }
    }

    public void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(this.mNightModeObserver);
        getContentResolver().unregisterContentObserver(this.mAccessibilityDisplayDaltonizerAndInversionContentObserver);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mBrightnessObserver != null) {
            this.mBrightnessObserver.stopObserving();
        }
    }

    private void updateState() {
        int notchMode;
        updateFontSizeSummary();
        updateScreenSaverSummary();
        boolean z = false;
        this.mNotifyLightEnable = System.getInt(getContentResolver(), "oem_acc_breath_light", 0);
        if (this.mLedSettingsPreference != null) {
            this.mLedSettingsPreference.setEnabled(this.mNotifyLightEnable == 1);
        }
        if (this.mAutoBrightnessPreference != null) {
            this.mAutoBrightnessPreference.setChecked(System.getInt(getContentResolver(), "screen_brightness_mode", 0) != 0);
        }
        if (this.mNetworkNameDisplayedPreference != null) {
            this.mNetworkNameDisplayedPreference.setChecked(System.getInt(getContentResolver(), SHOW_NETWORK_NAME_MODE, 1) != 0);
        }
        if (this.mTapToWakePreference != null) {
            this.mTapToWakePreference.setChecked(Secure.getInt(getContentResolver(), "double_tap_to_wake", 0) != 0);
        }
        if (this.mCameraGesturePreference != null) {
            this.mCameraGesturePreference.setChecked(Secure.getInt(getContentResolver(), "camera_gesture_disabled", 0) == 0);
        }
        if (this.mCameraDoubleTapPowerGesturePreference != null) {
            this.mCameraDoubleTapPowerGesturePreference.setChecked(Secure.getInt(getContentResolver(), "camera_double_tap_power_gesture_disabled", 0) == 0);
        }
        if (this.mAccentColorPreference != null) {
            SharedPreferences prefs = getPrefs();
            this.mAccentColorPreference.setOnPreferenceChangeListener(null);
            if (!isAccentColorPreferenceEnabled() || this.mColors == null) {
                this.mAccentColorPreference.setColor(this.mContext.getResources().getString(R.color.op_primary_default_light));
            } else {
                this.mAccentColorPreference.setColor(getAccentColor());
            }
            this.mAccentColorPreference.setEnabled(isAccentColorPreferenceEnabled());
            this.mAccentColorPreference.setOnPreferenceChangeListener(this);
            if (!getActivity().getPackageManager().hasSystemFeature("oem.op_dark_mode.support") || OPUtils.isGuestMode()) {
                this.mCustomRootPreference.removePreference(this.mAccentColorPreference);
            }
        }
        if (this.mScreenColorModePreference != null) {
            boolean OPNightModeState = Secure.getIntForUser(getContentResolver(), "night_display_activated", 0, -2) != 0;
            boolean OPReadingModeState = System.getIntForUser(getContentResolver(), OPReadingMode.READING_MODE_STATUS, 0, -2) != 0;
            if (System.getInt(getContentResolver(), "accessibility_display_grayscale_enabled", 1) == 0) {
                this.mScreenColorModePreference.setEnabled(false);
                updateScreenColorModePreference();
            } else if (OPNightModeState) {
                this.mScreenColorModePreference.setEnabled(false);
                this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_screen_color_mode_title_summary));
            } else if (OPReadingModeState && isSupportReadingMode) {
                this.mScreenColorModePreference.setEnabled(false);
                this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_screen_color_mode_reading_mode_on_summary));
            } else {
                updateScreenColorModePreference();
                this.mScreenColorModePreference.setEnabled(true);
            }
        }
        if (this.mVideoEnhancerPreference != null) {
            this.mVideoEnhancerPreference.setSummary(SystemProperties.getBoolean("persist.sys.oem.vendor.media.vpp.enable", false) ? R.string.switch_on_text : R.string.switch_off_text);
        }
        if (OPUtils.isSupportScreenCutting() && this.mNotchModeAppPreference != null) {
            notchMode = System.getInt(getContentResolver(), ONEPLUS_NOTCH_MODE, 0);
            Preference preference = this.mNotchModeAppPreference;
            if (notchMode == 0) {
                z = true;
            }
            preference.setEnabled(z);
        }
        if (this.mScreenResolutionAdjust != null) {
            notchMode = Global.getInt(this.mContext.getContentResolver(), "oneplus_screen_resolution_adjust", 2);
            if (notchMode == 1) {
                this.mScreenResolutionAdjust.setSummary((int) R.string.oneplus_screen_resolution_adjust_1080p);
            } else if (notchMode == 0) {
                this.mScreenResolutionAdjust.setSummary((int) R.string.oneplus_screen_resolution_adjust_other);
            }
            if (notchMode == 2) {
                this.mScreenResolutionAdjust.setSummary((int) R.string.oneplus_screen_resolution_adjust_intelligent_switch);
            }
        }
        if (this.mScreenRefreshRate != null) {
            this.mScreenRefreshRate.setSummary(Global.getInt(this.mContext.getContentResolver(), KEY_ONEPLUS_SCREEN_REFRESH_RATE, 2) == 2 ? R.string.oneplus_screen_refresh_rate_auto : R.string.oneplus_screen_refresh_rate_60hz);
        }
    }

    private void updateScreenColorModePreference() {
        if (isAdded()) {
            int value = System.getIntForUser(this.mContext.getContentResolver(), "screen_color_mode_settings_value", 1, -2);
            if ("1".equals(SystemProperties.get("ro.sensor.not_support_rbg", "0"))) {
                if (1 == value) {
                    this.mScreenColorModePreference.setSummary(getResources().getString(R.string.screen_color_mode_vivid));
                } else if (3 == value) {
                    this.mScreenColorModePreference.setSummary(getResources().getString(R.string.screen_color_mode_advanced));
                } else if (10 == value) {
                    this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_screen_better_nature));
                }
            } else if (1 == value) {
                this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_screen_color_mode_default));
            } else if (2 == value) {
                this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_screen_color_mode_basic));
            } else if (3 == value) {
                this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_screen_color_mode_defined));
            } else if (4 == value) {
                this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_screen_color_mode_dci_p3));
            } else if (5 == value) {
                this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_adaptive_model));
            } else if (6 == value) {
                this.mScreenColorModePreference.setSummary(getResources().getString(R.string.oneplus_screen_color_mode_soft));
            }
        }
    }

    private void updateScreenSaverSummary() {
        if (this.mScreenSaverPreference != null) {
            this.mScreenSaverPreference.setSummary(DreamSettings.getSummaryTextWithDreamName(getActivity()));
        }
    }

    private void updateFontSizeSummary() {
        Context context = this.mFontSizePref.getContext();
        float currentScale = System.getFloat(context.getContentResolver(), "font_scale", 1.0f);
        Resources res = context.getResources();
        this.mFontSizePref.setSummary(res.getStringArray(2130903123)[ToggleFontSizePreferenceFragment.fontSizeValueToIndex(currentScale, res.getStringArray(2130903124))]);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        int value;
        String key = preference.getKey();
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            try {
                value = Integer.parseInt((String) objValue);
                System.putInt(getContentResolver(), "screen_off_timeout", value);
                updateTimeoutPreferenceDescription((long) value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }
        if (preference == this.mAutoBrightnessPreference) {
            System.putInt(getContentResolver(), "screen_brightness_mode", ((Boolean) objValue).booleanValue());
            OPUtils.sendAppTrackerForAutoBrightness();
        }
        if (preference == this.mNetworkNameDisplayedPreference) {
            System.putInt(getContentResolver(), SHOW_NETWORK_NAME_MODE, ((Boolean) objValue).booleanValue());
        }
        if (preference == this.mTapToWakePreference) {
            Secure.putInt(getContentResolver(), "double_tap_to_wake", ((Boolean) objValue).booleanValue());
        }
        if (preference == this.mCameraGesturePreference) {
            Secure.putInt(getContentResolver(), "camera_gesture_disabled", ((Boolean) objValue).booleanValue() ^ 1);
        }
        if (preference == this.mCameraDoubleTapPowerGesturePreference) {
            Secure.putInt(getContentResolver(), "camera_double_tap_power_gesture_disabled", ((Boolean) objValue).booleanValue() ^ 1);
        }
        boolean value2;
        if (preference == this.mDarkModePreferce) {
            value2 = ((Boolean) objValue).booleanValue();
            if (this.mDarkModeRunnable == null) {
                this.mDarkModeRunnable = new DarkModeRunnable();
            }
            this.mDarkModeRunnable.setValue(value2);
            this.mHandler.removeCallbacks(this.mDarkModeRunnable);
            this.mHandler.postDelayed(this.mDarkModeRunnable, 300);
            return true;
        } else if (preference == this.mNotifyLightPreference) {
            value2 = ((Boolean) objValue).booleanValue();
            updateNotifyLightStatus(value2);
            if (this.mLedSettingsPreference != null) {
                this.mLedSettingsPreference.setEnabled(value2);
            }
            return true;
        } else {
            if (KEY_THEME_ACCENT_COLOR.equals(key)) {
                String theme = (String) objValue;
                int i;
                if (TextUtils.isEmpty(theme)) {
                    sendTheme(0, false);
                } else if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
                    for (i = 0; i < this.mColors.length; i++) {
                        if (i == 11) {
                            sendTheme(i, false);
                        } else if (theme.equals(this.mColors[i])) {
                            sendTheme(i, false);
                            break;
                        }
                    }
                } else if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver())) {
                    for (i = 0; i < this.mColors.length; i++) {
                        if (i == 11) {
                            sendTheme(i, false);
                        } else if (theme.equals(this.mColors[i])) {
                            sendTheme(i, false);
                            break;
                        }
                    }
                }
            }
            if (KEY_THEME_MODE.equals(key)) {
                try {
                    value = Integer.parseInt((String) objValue);
                    Message msg;
                    if (value != 3) {
                        System.putInt(getContentResolver(), KEY_DARK_MODE_ACTION, value);
                        Global.putInt(getContentResolver(), KEY_DARK_MODE_ACTION, value);
                        System.putInt(getContentResolver(), "oem_special_theme", 0);
                        OPUtils.sendAppTracker(KEY_THEME_MODE, value);
                        msg = this.mDefaultHandler.obtainMessage(100);
                        msg.arg1 = value;
                        this.mDefaultHandler.sendMessageDelayed(msg, 100);
                        updateThemeModePreferenceDescription(getThemeModeValue(value));
                    } else {
                        System.putInt(getContentResolver(), KEY_DARK_MODE_ACTION, 1);
                        Global.putInt(getContentResolver(), KEY_DARK_MODE_ACTION, 1);
                        System.putInt(getContentResolver(), "oem_special_theme", 1);
                        OPUtils.sendAppTracker(KEY_THEME_MODE, 3);
                        msg = this.mDefaultHandler.obtainMessage(101);
                        msg.arg1 = 1;
                        this.mDefaultHandler.sendMessageDelayed(msg, 100);
                        updateThemeModePreferenceDescription(getThemeModeValue(value));
                    }
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "could not persist screen timeout setting", e2);
                }
            }
            if (KEY_VIDEO_ENHANCER.equals(key)) {
                value2 = ((Boolean) objValue).booleanValue();
                SystemProperties.set("persist.sys.oem.vendor.media.vpp.enable", value2 ? "true" : "false");
                OPUtils.sendAppTracker(KEY_VIDEO_ENHANCER, value2 ? "1" : "0");
            }
            return true;
        }
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (this.mToggleLockScreenRotationPreference == preference) {
            handleLockScreenRotationPreferenceClick();
        }
        return super.onPreferenceTreeClick(preference);
    }

    private int getThemeModeValue(int value) {
        if (value == 2) {
            return 0;
        }
        if (value == 0) {
            return 1;
        }
        if (value == 1) {
            return 2;
        }
        return value;
    }

    public boolean onPreferenceClick(Preference preference) {
        Intent intent;
        if (preference.getKey().equals(KEY_DOZE) || preference.getKey().equals(KEY_DOZE_801)) {
            try {
                intent = new Intent();
                intent.setClassName("com.oneplus.aod", "com.oneplus.settings.SettingsActivity");
                this.mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (preference.getKey().equals(KEY_DISPLAY_SIZE_ADAPTION) || preference.getKey().equals(KEY_ONEPLUS_NOTCH_FULLSCREEN_APP)) {
            intent = null;
            try {
                intent = new Intent("com.android.settings.action.DISPLAYSIZEADAPTION");
                intent.putExtra(ManageApplications.EXTRA_CLASSNAME, DisplaySizeAdaptionAppListActivity.class.getName());
                startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("No activity found for ");
                stringBuilder.append(intent);
                Log.d(str, stringBuilder.toString());
            }
            return true;
        } else {
            if (preference.getKey().equals(KEY_LOCKGUARD_WALLPAPER)) {
                intent = new Intent("android.intent.action.SET_WALLPAPER");
                intent.setPackage("net.oneplus.launcher");
                if (OPUtils.isActionExist(this.mContext, intent, "android.intent.action.SET_WALLPAPER")) {
                    try {
                        startActivity(intent);
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                }
            }
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isInVrMode() {
        try {
            return IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager")).getVrModeState();
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to check vr mode!", e);
            return false;
        }
    }

    private void setBrightness(int brightness) {
        this.mDisplayManager.setTemporaryBrightness(brightness);
    }

    public void onOPBrightValueStartTrackingTouch(int value) {
    }

    public void onOPBrightValueChanged(int bright, int value) {
        int min;
        int max;
        if (this.mSliderAnimator != null) {
            this.mSliderAnimator.cancel();
        }
        String setting;
        if (isInVrMode()) {
            min = this.mMinimumBacklightForVr;
            max = this.mMaximumBacklightForVr;
            setting = "screen_brightness_for_vr";
        } else {
            min = this.mMinimumBacklight;
            max = this.mMaximumBacklight;
            setting = KEY_SCREEN_BRIGHTNESS;
        }
        setBrightness(BrightnessUtils.convertGammaToLinear(value, min, max));
    }

    public void saveBrightnessDataBase(int brightness) {
        int min;
        int max;
        String setting;
        if (this.mSliderAnimator != null) {
            this.mSliderAnimator.cancel();
        }
        if (isInVrMode()) {
            min = this.mMinimumBacklightForVr;
            max = this.mMaximumBacklightForVr;
            setting = "screen_brightness_for_vr";
        } else {
            min = this.mMinimumBacklight;
            max = this.mMaximumBacklight;
            setting = KEY_SCREEN_BRIGHTNESS;
        }
        final int val = BrightnessUtils.convertGammaToLinear(brightness, min, max);
        AsyncTask.execute(new Runnable() {
            public void run() {
                System.putIntForUser(DisplaySettings.this.mContext.getContentResolver(), setting, val, -2);
                if (System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "screen_brightness_mode", 0) == 0) {
                    OPUtils.sendAppTrackerForBrightness();
                }
            }
        });
    }

    private void updateThemeModePreferenceDescription(int themeMode) {
        int specialthemeMode = System.getInt(getActivity().getContentResolver(), "oem_special_theme", 0);
        CharSequence[] entries = this.mThemeModePreference.getEntries();
        if (specialthemeMode == 1) {
            if (entries.length > 3) {
                this.mThemeModePreference.setSummary(entries[3]);
            }
        } else if (this.mThemeModePreference != null && themeMode < 3) {
            this.mThemeModePreference.setSummary(entries[themeMode]);
        }
    }

    private String getAccentColor() {
        String accentColor = System.getString(getActivity().getContentResolver(), OPConstants.ONEPLUS_ACCENT_COLOR);
        if (!TextUtils.isEmpty(this.mCurrentTempColor)) {
            accentColor = this.mCurrentTempColor;
        }
        if (TextUtils.isEmpty(accentColor)) {
            accentColor = this.mColors[getColorIndex()];
        }
        return accentColor.toLowerCase();
    }

    private int getColorIndex() {
        if (!OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            return System.getInt(getActivity().getContentResolver(), OEM_WHITE_MODE_ACCENT_COLOR_INDEX, 0);
        }
        int index = System.getInt(getActivity().getContentResolver(), OEM_BLACK_MODE_ACCENT_COLOR_INDEX, 0);
        if (index <= this.mColors.length - 1) {
            return index;
        }
        index = index > 7 ? index - 7 : index;
        System.putInt(getContentResolver(), OEM_BLACK_MODE_ACCENT_COLOR_INDEX, index);
        return index;
    }

    private void saveColorInfo(int index) {
        String accentColor;
        if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            if (index == 11) {
                accentColor = this.mCurrentTempColor;
                System.putString(getActivity().getContentResolver(), OPConstants.ONEPLUS_BLACK_CUSTOM_ACCENT_COLOR, accentColor);
            } else {
                accentColor = this.mColors[index];
            }
            System.putString(getActivity().getContentResolver(), OEM_BLACK_MODE_ACCENT_COLOR, accentColor);
            System.putInt(getActivity().getContentResolver(), OEM_BLACK_MODE_ACCENT_COLOR_INDEX, index);
        } else {
            if (index == 11) {
                accentColor = this.mCurrentTempColor;
                System.putString(getActivity().getContentResolver(), OPConstants.ONEPLUS_WHITE_CUSTOM_ACCENT_COLOR, accentColor);
            } else {
                accentColor = this.mColors[index];
            }
            System.putString(getActivity().getContentResolver(), OEM_WHITE_MODE_ACCENT_COLOR, accentColor);
            System.putInt(getActivity().getContentResolver(), OEM_WHITE_MODE_ACCENT_COLOR_INDEX, index);
        }
        OPUtils.sendAppTrackerForAccentColor();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (100 == requestCode && resultCode == -1 && data != null) {
            this.mCurrentTempColor = data.getStringExtra("current_temp_color");
            this.mAccentColorPreference.setCustomBgColor(this.mCurrentTempColor);
            if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver())) {
                this.mAccentColorPreference.setColorPalette(this.mWhiteColors, this.mWhiteColorStringIds);
                this.mWhiteColors[11] = this.mCurrentTempColor;
            } else if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
                this.mAccentColorPreference.setColorPalette(this.mBlackColors, this.mBlackColorStringIds);
                this.mBlackColors[11] = this.mCurrentTempColor;
            }
        }
    }

    public void onCustomColorClick() {
        Intent customColor = new Intent("oneplus.intent.action.ONEPLUS_COLOR_PICKER");
        if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver())) {
            this.mCurrentTempColor = System.getString(getActivity().getContentResolver(), OPConstants.ONEPLUS_WHITE_CUSTOM_ACCENT_COLOR);
        } else if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            this.mCurrentTempColor = System.getString(getActivity().getContentResolver(), OPConstants.ONEPLUS_BLACK_CUSTOM_ACCENT_COLOR);
        }
        customColor.putExtra("current_color", this.mCurrentTempColor);
        startActivityForResult(customColor, 100);
    }

    private void sendTheme(int index, boolean fromThemeSwitch) {
        SharedPreferences prefs = getPrefs();
        saveColorInfo(index);
        String accentColor = this.mColors[index];
        if (index == 11) {
            accentColor = this.mCurrentTempColor;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("set accentColor ");
        stringBuilder.append(accentColor);
        stringBuilder.append(" index:");
        stringBuilder.append(index);
        Log.d(str, stringBuilder.toString());
        System.putString(getContentResolver(), OPConstants.ONEPLUS_ACCENT_COLOR, accentColor);
        if (index == 11) {
            if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver())) {
                System.putString(getContentResolver(), OPConstants.ONEPLUS_WHITE_CUSTOM_ACCENT_COLOR, accentColor);
            } else if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
                System.putString(getContentResolver(), OPConstants.ONEPLUS_BLACK_CUSTOM_ACCENT_COLOR, accentColor);
            }
        }
        if (!TextUtils.isEmpty(accentColor)) {
            accentColor = accentColor.replace("#", "");
        }
        setCustomAccentColor();
        SystemProperties.set(KEY_CUSTOM_ACCENT_COLOR, accentColor);
        Intent intent = new Intent("android.settings.OEM_COLOR_MODE");
        intent.setPackage(OP_THEME_PACKAGE);
        intent.addFlags(268435456);
        getContext().sendBroadcast(intent);
    }

    private SharedPreferences getPrefs() {
        return this.mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
    }

    private boolean isAccentColorPreferenceEnabled() {
        int specialthemeMode = System.getInt(getActivity().getContentResolver(), "oem_special_theme", 0);
        if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver()) || (OPUtils.isBlackModeOn(this.mContext.getContentResolver()) && specialthemeMode != 1)) {
            return true;
        }
        return false;
    }

    private void initAccentColors(Resources res) {
        Resources resources = res;
        this.mWhiteColors = new String[]{resources.getString(R.color.op_primary_default_light), resources.getString(R.color.op_primary_golden_light), resources.getString(R.color.op_primary_lemon_yellow_light), resources.getString(R.color.op_primary_grass_green_light), resources.getString(R.color.op_primary_charm_purple_light), resources.getString(R.color.op_primary_sky_blue_light), resources.getString(R.color.op_primary_vigour_red_light), resources.getString(R.color.op_primary_fashion_pink_light), resources.getString(R.color.op_primary_red_light), resources.getString(R.color.op_primary_blue_light), resources.getString(R.color.op_primary_green_light), resources.getString(R.color.op_primary_green_custom)};
        this.mBlackColors = new String[]{resources.getString(R.color.op_primary_default_dark), resources.getString(R.color.op_primary_golden_dark), resources.getString(R.color.op_primary_lemon_yellow_dark), resources.getString(R.color.op_primary_grass_green_dark), resources.getString(R.color.op_primary_charm_purple_dark), resources.getString(R.color.op_primary_sky_blue_dark), resources.getString(R.color.op_primary_vigour_red_dark), resources.getString(R.color.op_primary_fashion_pink_dark), resources.getString(R.color.op_primary_red_dark), resources.getString(R.color.op_primary_blue_dark), resources.getString(R.color.op_primary_green_dark), resources.getString(R.color.op_primary_green_custom)};
        if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver())) {
            this.mColors = this.mWhiteColors;
        } else if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            this.mColors = this.mBlackColors;
        }
        this.mWhiteColorStringIds = new int[]{R.string.op_primary_default_light_label, R.string.op_primary_golden_light_label, R.string.op_primary_lemon_yellow_light_label, R.string.op_primary_grass_green_light_label, R.string.op_primary_charm_purple_light_label, R.string.op_primary_sky_blue_light_label, R.string.op_primary_vigour_red_light_label, R.string.op_primary_fashion_pink_light_label, R.string.op_primary_red_label, R.string.op_primary_royal_blue_label, R.string.op_primary_dark_green_label, R.string.customization_settings_title};
        this.mBlackColorStringIds = new int[]{R.string.op_primary_default_dark_label, R.string.op_primary_golden_dark_label, R.string.op_primary_lemon_yellow_dark_label, R.string.op_primary_grass_green_dark_label, R.string.op_primary_charm_purple_dark_label, R.string.op_primary_sky_blue_dark_label, R.string.op_primary_vigour_red_dark_label, R.string.op_primary_fashion_pink_dark_label, R.string.op_primary_red_label, R.string.op_primary_royal_blue_label, R.string.op_primary_dark_green_label, R.string.customization_settings_title};
    }

    private void setCustomAccentColor() {
        String whiteCustomColor = System.getString(getActivity().getContentResolver(), OPConstants.ONEPLUS_WHITE_CUSTOM_ACCENT_COLOR);
        if (TextUtils.isEmpty(whiteCustomColor)) {
            whiteCustomColor = OPConstants.ONEPLUS_ACCENT_RED_COLOR;
        }
        this.mWhiteColors[11] = whiteCustomColor;
        String blackCustomColor = System.getString(getActivity().getContentResolver(), OPConstants.ONEPLUS_BLACK_CUSTOM_ACCENT_COLOR);
        if (TextUtils.isEmpty(blackCustomColor)) {
            blackCustomColor = OPConstants.ONEPLUS_ACCENT_RED_COLOR;
        }
        this.mBlackColors[11] = blackCustomColor;
        if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver())) {
            this.mAccentColorPreference.setColorPalette(this.mWhiteColors, this.mWhiteColorStringIds);
            this.mAccentColorPreference.setDefaultColor(this.mContext.getResources().getString(R.color.op_primary_default_light));
        } else if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            this.mAccentColorPreference.setColorPalette(this.mBlackColors, this.mBlackColorStringIds);
            this.mAccentColorPreference.setDefaultColor(this.mContext.getResources().getString(R.color.op_primary_default_dark));
        } else {
            this.mAccentColorPreference.setDefaultColor(this.mContext.getResources().getString(R.color.op_primary_default_light));
        }
    }

    private void setAccentColor() {
        String accentColor;
        if (OPUtils.isWhiteModeOn(this.mContext.getContentResolver())) {
            accentColor = System.getString(getActivity().getContentResolver(), OEM_WHITE_MODE_ACCENT_COLOR);
        } else if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            accentColor = System.getString(getActivity().getContentResolver(), OEM_BLACK_MODE_ACCENT_COLOR);
        } else {
            accentColor = this.mContext.getResources().getString(R.color.op_primary_default_light);
        }
        System.putString(getContentResolver(), OPConstants.ONEPLUS_ACCENT_COLOR, accentColor);
        if (!TextUtils.isEmpty(accentColor)) {
            accentColor = accentColor.replace("#", "");
        }
        setCustomAccentColor();
        SystemProperties.set(KEY_CUSTOM_ACCENT_COLOR, accentColor);
    }

    private void killSelf() {
        Intent home = new Intent("android.intent.action.MAIN");
        home.addCategory("android.intent.category.HOME");
        startActivity(home);
        finish();
    }

    private void updateLockScreenRotation() {
        Context context = SettingsBaseApplication.mApplication;
        if (context != null) {
            this.mToggleLockScreenRotationPreference.setChecked(RotationPolicy.isRotationLocked(context) ^ 1);
        }
    }

    private void handleLockScreenRotationPreferenceClick() {
        RotationPolicy.setRotationLockForAccessibility(SettingsBaseApplication.mApplication, this.mToggleLockScreenRotationPreference.isChecked() ^ 1);
    }

    private void updateNotifyLightStatus(int value) {
        System.putInt(getActivity().getContentResolver(), "oem_acc_breath_light", value);
        System.putInt(getActivity().getContentResolver(), "notification_light_pulse", value);
        System.putInt(getActivity().getContentResolver(), OPConstants.BATTERY_LED_LOW_POWER, value);
        System.putInt(getActivity().getContentResolver(), OPConstants.BATTERY_LED_CHARGING, value);
    }

    private void updateSlider() {
        int value;
        if (this.mSliderAnimator != null) {
            this.mSliderAnimator.cancel();
        }
        if (isInVrMode()) {
            value = BrightnessUtils.convertLinearToGamma(System.getInt(this.mContext.getContentResolver(), "screen_brightness_for_vr", this.mMaximumBacklightForVr), this.mMinimumBacklightForVr, this.mMaximumBacklightForVr);
        } else {
            value = BrightnessUtils.convertLinearToGamma(System.getInt(this.mContext.getContentResolver(), KEY_SCREEN_BRIGHTNESS, this.mMinimumBacklight), this.mMinimumBacklight, this.mMaximumBacklight);
        }
        if (System.getInt(getContentResolver(), "screen_brightness_mode", 0) != 0) {
            this.mSliderAnimator = new ValueAnimator();
            this.mSliderAnimator.setIntValues(new int[]{this.mBrightPreference.getBrightness(), value});
            this.mSliderAnimator.addUpdateListener(new -$$Lambda$DisplaySettings$qOh46548JQf3cUmLta2I9UEyRo4(this));
            this.mSliderAnimator.setDuration(3000);
            this.mSliderAnimator.start();
            return;
        }
        this.mBrightPreference.setBrightness(value);
    }

    private void updateMode() {
        if (this.mAutomaticAvailable) {
            boolean z = false;
            if (System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2) != 0) {
                z = true;
            }
            this.mAutomatic = z;
            this.mAutoBrightnessPreference.setChecked(this.mAutomatic);
        }
    }

    public int getHelpResource() {
        return R.string.help_uri_display;
    }

    private void disablePreferenceIfManaged(String key, String restriction) {
        RestrictedPreference pref = (RestrictedPreference) findPreference(key);
        if (pref != null) {
            pref.setDisabledByAdmin(null);
            if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), restriction, UserHandle.myUserId())) {
                pref.setEnabled(false);
            } else {
                pref.checkRestrictionAndSetDisabled(restriction);
            }
        }
    }

    private static boolean isOnePlusLaunchrSupportSetWallpaper() {
        Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
        intent.setPackage("net.oneplus.launcher");
        if (OPUtils.isActionExist(SettingsBaseApplication.mApplication, intent, "android.intent.action.SET_WALLPAPER")) {
            return true;
        }
        return false;
    }
}
