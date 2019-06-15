package com.oneplus.settings.utils;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.UserInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.LocaleList;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.annotation.ColorInt;
import android.support.annotation.StyleRes;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.OpFeatures;
import android.util.TypedValue;
import android.view.Window;
import android.widget.ListView;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.ColorDisplayController;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.utils.FileSizeFormatter;
import com.android.settings.wifi.OPAutoSwitchMobileDataPreferenceController;
import com.android.settings.wifi.OPIntelligentlySelectBestWifiPreferenceController;
import com.android.settingslib.deviceinfo.PrivateStorageInfo;
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.common.base.Ascii;
import com.oneplus.lib.util.ReflectUtil;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.backgroundoptimize.BgOActivityManager;
import com.oneplus.settings.better.OPAppModel;
import com.oneplus.settings.better.OPGamingMode;
import com.oneplus.settings.better.OPHapticFeedback;
import com.oneplus.settings.better.OPReadingMode;
import com.oneplus.settings.faceunlock.OPFaceUnlockModeSettings;
import com.oneplus.settings.faceunlock.OPFaceUnlockSettings;
import com.oneplus.settings.gestures.OPGestureUtils;
import com.oneplus.settings.highpowerapp.PackageUtils;
import com.oneplus.settings.im.OPQuickReplySettings;
import com.oneplus.settings.system.OPRamBoostSettings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.oneplus.odm.insight.tracker.AppTracker;

public class OPUtils {
    public static final int ANDROID_SYSTEM_UID = 1000;
    public static final String COMPANY = "oneplus";
    public static final boolean DEBUG_FOR_FINGERPRINT = false;
    public static final String OEM_REPEATE_INCALL_LIMITE = "oem_repeate_incall_unlimite";
    public static final String OEM_TREE_KEY_DEFINE = "oem_three_key_define";
    public static final String ONEPLUS_15801 = "15801";
    public static final String ONEPLUS_15811 = "15811";
    public static final String ONEPLUS_CLOUD_PACKAGE = "com.oneplus.cloud";
    public static final int ONEPLUS_METRICSLOGGER = 9999;
    public static final String ONEPLUS_VOICE_ASSISTANT_PACKAGE = "com.oneplus.speechassist";
    public static final String TAG = "OPUtils";
    public static final String TRACKER_CATEGORY = "OPSettings";
    public static final String[] UNIT_OF_STORAGE = new String[]{"(?<![吉千兆太])比特", "(?<![吉千兆太])字节", "吉比特", "吉字节", "千比特", "千字节", "兆比特", "兆字节", "太比特", "太字节"};
    public static final String[] UNIT_OF_STORAGE_REPLACE = new String[]{"b", "B", "Gb", "GB", "Kb", "KB", "Mb", "MB", "Tb", "TB"};
    public static final String ZH_CN_HANS_ID = "zh-Hans-CN";
    public static final String ZH_CN_ID = "zh-CN";
    public static final String ZH_CN_LABEL = "zh_CN";
    public static final String ZH_EN_ID = "en-US";
    public static final String ZH_TW_HANT_ID = "zh-Hant-TW";
    public static final String ZH_TW_ID = "zh-TW";
    public static final List<String> bgServiceApplist = Arrays.asList(bgServicePackages);
    public static final String[] bgServicePackages = new String[]{"com.oneplus.card", ONEPLUS_CLOUD_PACKAGE, "com.oneplus.appupgrader", "com.oneplus.dirac.simplemanager", "com.oneplus.soundrecorder", "com.oneplus.sound.tuner"};
    public static Boolean isExist_Cloud_Package = null;
    public static Boolean isUstModeEnabled = null;
    private static AppTracker mAppTracker;
    public static boolean mAppUpdated = false;
    private static final String[] productNotNeedTcpTimestampsControl = new String[]{"OnePlus3", "OnePlus3T", "OnePlus5", "OnePlus5T", "OnePlus6", "P7819", "EC101", "P8801", "P8811", "OnePlus6T", "OnePlus6TSingle"};

    public static boolean isSupportUstMode() {
        if (isUstModeEnabled != null) {
            return isUstModeEnabled.booleanValue();
        }
        if (ReflectUtil.isFeatureSupported("OP_FEATURE_UST_MODE")) {
            isUstModeEnabled = Boolean.valueOf(true);
        } else {
            isUstModeEnabled = Boolean.valueOf(false);
        }
        return isUstModeEnabled.booleanValue();
    }

    public static String replaceFileSize(String str) {
        String sizeString = str;
        for (int i = 0; i < UNIT_OF_STORAGE.length; i++) {
            sizeString = sizeString.replaceAll(UNIT_OF_STORAGE[i], UNIT_OF_STORAGE_REPLACE[i]);
        }
        return sizeString;
    }

    public static String formatFileSize(Context ctx, long size) {
        String sizeString = Formatter.formatFileSize(ctx, size);
        if (VERSION.SDK_INT > 26) {
            Locale defaultLocale = Locale.getDefault();
            String language = defaultLocale.getLanguage();
            String country = defaultLocale.getCountry();
            if (language.equalsIgnoreCase("zh") && country.equalsIgnoreCase("CN")) {
                for (int i = 0; i < UNIT_OF_STORAGE.length; i++) {
                    sizeString = sizeString.replaceAll(UNIT_OF_STORAGE[i], UNIT_OF_STORAGE_REPLACE[i]);
                }
            }
        }
        return sizeString;
    }

    public static String formatShortFileSize(Context ctx, long size) {
        String sizeString = Formatter.formatShortFileSize(ctx, size);
        if (VERSION.SDK_INT > 26) {
            Locale defaultLocale = Locale.getDefault();
            String language = defaultLocale.getLanguage();
            String country = defaultLocale.getCountry();
            if (language.equalsIgnoreCase("zh") && country.equalsIgnoreCase("CN")) {
                for (int i = 0; i < UNIT_OF_STORAGE.length; i++) {
                    sizeString = sizeString.replaceAll(UNIT_OF_STORAGE[i], UNIT_OF_STORAGE_REPLACE[i]);
                }
            }
        }
        return sizeString;
    }

    public static void setAppUpdated(boolean isUpdated) {
        mAppUpdated = isUpdated;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setAppUpdated:");
        stringBuilder.append(mAppUpdated);
        Log.i(str, stringBuilder.toString());
    }

    public static boolean isAppExist(Context context, String packageName) {
        boolean isExist = false;
        if (!ONEPLUS_CLOUD_PACKAGE.equals(packageName)) {
            if (getApplicationInfo(context, packageName) != null) {
                isExist = true;
            }
            return isExist;
        } else if (isExist_Cloud_Package != null) {
            return isExist_Cloud_Package.booleanValue();
        } else {
            if (getApplicationInfo(context, packageName) != null) {
                isExist_Cloud_Package = Boolean.valueOf(true);
            } else {
                isExist_Cloud_Package = Boolean.valueOf(false);
            }
            return isExist_Cloud_Package.booleanValue();
        }
    }

    public static ResolveInfo getResolveInfoByPackageName(Context context, String pkg) {
        PackageManager packageManager = context.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        mainIntent.setPackage(pkg);
        List<ResolveInfo> mAllApps = packageManager.queryIntentActivities(mainIntent, 0);
        if (mAllApps == null || mAllApps.size() <= 0) {
            return null;
        }
        return (ResolveInfo) mAllApps.get(0);
    }

    private static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ApplicationInfo getApplicationInfoByUserId(Context context, String packageName, int uid) {
        try {
            return context.getPackageManager().getApplicationInfoByUserId(packageName, 0, UserHandle.getUserId(uid));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isNumeric(String str) {
        if (Pattern.compile("[0-9]*").matcher(str).matches()) {
            return true;
        }
        return false;
    }

    public static boolean isAppPakExist(Context context, String packageName) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (applicationInfo != null) {
            return true;
        }
        return false;
    }

    public static boolean isActionExist(Context context, Intent oriIntent, String intent_action_scan) {
        Intent intent;
        PackageManager packageManager = context.getPackageManager();
        if (oriIntent == null) {
            intent = new Intent();
        } else {
            intent = (Intent) oriIntent.clone();
        }
        intent.setAction(intent_action_scan);
        return packageManager.queryIntentActivities(intent, 65536).size() > 0;
    }

    public static boolean isGuestMode() {
        return UserHandle.myUserId() != 0;
    }

    public static void sendAppTrackerForQuickReplyIMStatus() {
        String[] surportApps = new String[]{OPConstants.PACKAGE_WECHAT, OPConstants.PACKAGE_WHATSAPP, OPConstants.PACKAGE_INSTAGRAM, OPConstants.PACKAGE_MOBILEQQ};
        StringBuilder quickReplyAppsStates = new StringBuilder();
        for (String pkg : surportApps) {
            if (isAppExist(SettingsBaseApplication.mApplication, pkg)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(pkg);
                stringBuilder.append(":");
                stringBuilder.append(isQuickReplyAppSelected(pkg));
                stringBuilder.append(",");
                quickReplyAppsStates.append(stringBuilder.toString());
            }
        }
        if (!TextUtils.isEmpty(quickReplyAppsStates)) {
            sendAppTracker("lqr_im_states", quickReplyAppsStates.toString());
        }
    }

    public static void sendAppTrackerForQuickReplyKeyboardStatus() {
        sendAppTracker("lqr_fk_switch", System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), OPQuickReplySettings.OP_QUICKREPLY_IME_ADJUST, 0));
    }

    public static void sendAppTrackerForFodAnimStyle() {
        sendAppTracker(OPConstants.FOD_ANIMAITON_STYLE, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, 0, -2));
    }

    public static void sendAppTrackerForGestureAndButton() {
        if (isAllowSendAppTracker(SettingsBaseApplication.mApplication.getApplicationContext())) {
            int gestureType = System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "op_navigation_bar_type", 1);
            if (gestureType == 3) {
                sendAppTracker(OPConstants.SWAP_BUTTON, 0);
            } else {
                sendAppTracker(OPConstants.SWAP_BUTTON, System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "oem_acc_key_define", 0));
            }
            if (gestureType == 1 && System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), OPConstants.OP_GESTURE_BUTTON_ENABLED, 0) == 1) {
                gestureType = 4;
            }
            sendAppTracker(OPConstants.NAV_GESTURES_SETTINGS, gestureType);
        }
    }

    public static void sendAppTrackerForQuickLaunch() {
        sendAppTracker(OPConstants.QUICK_LAUNCH_SHORTCUTS, getAllQuickLaunchStrings(SettingsBaseApplication.mApplication.getApplicationContext()));
    }

    public static void sendAppTrackerForAssistantAPP() {
        boolean z = true;
        if (System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "quick_turn_on_voice_assistant", 0) != 1) {
            z = false;
        }
        sendAppTracker("quick_turn_on_voice_assistant", z ? "on" : "off");
    }

    public static void sendAppTrackerForAutoBrightness() {
        sendAppTracker(OPConstants.ADAPTIVE_BRIGHTNESS_CLICK_AUTO_OPEN, System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "screen_brightness_mode", 0));
    }

    public static void sendAppTrackerForBrightness() {
        int brightneddLevel;
        int brightnessValue = System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "screen_brightness", 0, -2);
        if (brightnessValue < 40) {
            brightneddLevel = 1;
        } else if (brightnessValue < Const.CODE_C1_DLW) {
            brightneddLevel = 2;
        } else if (brightnessValue < 220) {
            brightneddLevel = 3;
        } else if (brightnessValue < 420) {
            brightneddLevel = 4;
        } else {
            brightneddLevel = 5;
        }
        sendAppTracker(OPConstants.ADAPTIVE_BRIGHTNESS_MANUAL_SLIDER, brightneddLevel);
    }

    public static void sendAppTrackerForAutoNightMode() {
        sendAppTracker(OPConstants.NIGHT_MODE_AUTO_OPEN, new ColorDisplayController(SettingsBaseApplication.mApplication).getAutoMode());
    }

    public static void sendAppTrackerForNightMode() {
        sendAppTracker(OPConstants.NIGHT_MODE_MANUAL_OPEN, new ColorDisplayController(SettingsBaseApplication.mApplication).isActivated());
    }

    public static void sendAppTrackerForEffectStrength() {
        int strengthLevel;
        int progress = System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "oem_nightmode_progress_status", 103, -2);
        if (progress < 44) {
            strengthLevel = 1;
        } else if (progress < 88) {
            strengthLevel = 2;
        } else {
            strengthLevel = 3;
        }
        sendAppTracker(OPConstants.NIGHT_MODE_EFFECT_STRENGTH, strengthLevel);
    }

    public static void sendAppTrackerForReadingModeApps(String pkgNames) {
        sendAppTracker(OPConstants.READ_MODE_APPS, pkgNames);
    }

    public static void sendAppTrackerForReadingModeNotification() {
        sendAppTracker(OPConstants.READ_MODE_BLOCK_PEEK_NOTI, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "reading_mode_block_notification", 0, -2));
    }

    public static void sendAppTrackerForReadingMode() {
        String readingMode = System.getStringForUser(SettingsBaseApplication.mApplication.getContentResolver(), OPReadingMode.READING_MODE_STATUS_MANUAL, -2);
        int mode = 0;
        if ("force-on".equals(readingMode)) {
            mode = 1;
        } else if ("force-off".equals(readingMode)) {
            mode = 0;
        }
        sendAppTracker(OPConstants.READ_MODE_MANUAL_OPEN, mode);
    }

    public static void sendAppTrackerForScreenColorMode() {
        sendAppTracker(OPConstants.SCREEN_CALIBRATION_SCREEN_CALIBRATION, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "screen_color_mode_settings_value", 1, -2));
    }

    public static void sendAppTrackerForScreenCustomColorMode() {
        int modeLevel;
        int colorModeValue = System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "oem_screen_better_value", 0);
        if (colorModeValue < 33) {
            modeLevel = 1;
        } else if (colorModeValue < 66) {
            modeLevel = 2;
        } else {
            modeLevel = 3;
        }
        sendAppTracker(OPConstants.SCREEN_CALIBRATION_CUSTOM_COLOR, modeLevel);
    }

    public static void sendAppTrackerForThemes() {
        sendAppTracker(OPConstants.ONEPLUS_THEME, System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "oem_black_mode", 0));
    }

    public static void sendAppTrackerForAccentColor() {
        sendAppTracker(OPConstants.ONEPLUS_THEME_ACCENT_COLOR_WHITE, System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "oem_white_mode_accent_color_index", 0));
        sendAppTracker(OPConstants.ONEPLUS_THEME_ACCENT_COLOR_BLACK, System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "oem_black_mode_accent_color_index", 0));
    }

    public static void sendAppTrackerForDefaultHomeAppByComponentName(String cpName) {
        sendAppTracker(OPConstants.DEFAULT_APP_HOME_APP, cpName);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sendAppTrackerForDefaultHomeAppByPackageName componentName is:");
        stringBuilder.append(cpName);
        Log.d(str, stringBuilder.toString());
    }

    public static void sendAppTrackerForAssistAppByComponentName(String cpName) {
        sendAppTracker(OPConstants.DEFAULT_APP_ASSIST_VOICE_INPUT, cpName);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sendAppTrackerForAssistAppByComponentName componentName is:");
        stringBuilder.append(cpName);
        Log.d(str, stringBuilder.toString());
    }

    public static void sendAppTrackerForAssistApp() {
        try {
            ComponentName cn = new AssistUtils(SettingsBaseApplication.mApplication).getAssistComponentForUser(UserHandle.myUserId());
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sendAppTrackerForAssistApp componentNamePkg is:");
            stringBuilder.append(cn);
            Log.d(str, stringBuilder.toString());
            if (cn != null) {
                sendAppTracker(OPConstants.DEFAULT_APP_ASSIST_VOICE_INPUT, cn.toString());
            }
        } catch (Exception e) {
            Log.d(TAG, "sendAppTrackerForAssistApp componentNamePkg is not exist");
        }
    }

    public static void sendAppTrackerForDefaultHomeApp() {
        try {
            ComponentName currentDefaultHome = SettingsBaseApplication.mApplication.getPackageManager().getHomeActivities(new ArrayList());
            if (currentDefaultHome != null) {
                sendAppTracker(OPConstants.DEFAULT_APP_HOME_APP, currentDefaultHome.toString());
            }
        } catch (Exception e) {
            Log.d(TAG, "sendAppTrackerForDefaultJHomeApp componentNamePkg is not exist");
        }
    }

    public static void sendAppTrackerForGameModeSpeakerAnswer() {
        sendAppTracker(OPConstants.GAME_MODE_SPEAKER_ANSWER, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), OPGamingMode.GAME_MODE_ANSWER_NO_INCALLUI, 0, -2));
    }

    public static void sendAppTrackerForGameModeNotificationShow() {
        sendAppTracker(OPConstants.GAME_MODE_NOTIFICATION_SHOW, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), OPGamingMode.GAME_MODE_BLOCK_NOTIFICATION, 0, -2));
    }

    public static void sendAppTrackerForGameMode3drPartyCalls() {
        sendAppTracker(OPConstants.GAME_MODE_3RD_PARTY_CALLS, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "game_mode_notifications_3rd_calls", 0, -2));
    }

    public static void sendAppTrackerForGameModeAdEnable() {
        sendAppTracker(OPConstants.GAME_MODE_GAME_MODE_AD_ENABLE, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "op_game_mode_ad_enable", 0, -2));
    }

    public static void sendAppTrackerForGameModeBrightness() {
        sendAppTracker(OPConstants.GAME_MODE_DISABLE_AUTO_BRIGHTNESS, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), OPGamingMode.GAME_MODE_CLOSE_AUTOMATIC_BRIGHTNESS, 0, -2));
    }

    public static void sendAppTrackerForGameModeNetWorkBoost() {
        sendAppTracker(OPConstants.GAME_MODE_NETWORK_BOOST, System.getIntForUser(SettingsBaseApplication.mApplication.getContentResolver(), "game_mode_network_acceleration", 0, -2));
    }

    public static void sendAppTrackerForGameModeApps(String pkgNames) {
        sendAppTracker(OPConstants.GAME_MODE_APPS, pkgNames);
    }

    public static void sendAppTrackerForSmartWifiSwitch() {
        sendAppTracker(OPConstants.WIFI_SMART_CHOICE, System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), OPIntelligentlySelectBestWifiPreferenceController.WIFI_SHOULD_SWITCH_NETWORK, 0));
    }

    public static void sendAppTrackerForDataAutoSwitch() {
        sendAppTracker(OPConstants.DATA_AUTO_SWITCH, System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), OPAutoSwitchMobileDataPreferenceController.WIFI_AUTO_CHANGE_TO_MOBILE_DATA, 0));
    }

    public static void sendAppTrackerForAllSettings() {
        new Thread(new Runnable() {
            public void run() {
                OPUtils.sendAppTracker("auto_face_unlock", System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), OPFaceUnlockSettings.ONEPLUS_AUTO_FACE_UNLOCK_ENABLE, 0));
                OPUtils.sendAppTracker("op_three_key_screenshots_enabled", System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "oem_acc_sensor_three_finger", 0));
                OPUtils.sendAppTrackerForGestureAndButton();
                OPUtils.sendAppTrackerForAssistantAPP();
                OPUtils.sendAppTracker("notch_display", System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "op_camera_notch_ignore", 0));
                OPUtils.sendAppTrackerForQuickLaunch();
                OPUtils.sendAppTrackerForFodAnimStyle();
                OPUtils.sendAppTrackerForAutoBrightness();
                OPUtils.sendAppTrackerForBrightness();
                OPUtils.sendAppTrackerForAutoNightMode();
                OPUtils.sendAppTrackerForNightMode();
                OPUtils.sendAppTrackerForEffectStrength();
                OPUtils.sendAppTrackerForReadingModeApps(System.getString(SettingsBaseApplication.mApplication.getContentResolver(), OPConstants.READ_MODE_APPS));
                OPUtils.sendAppTrackerForReadingModeNotification();
                OPUtils.sendAppTrackerForReadingMode();
                OPUtils.sendAppTrackerForScreenColorMode();
                OPUtils.sendAppTrackerForScreenCustomColorMode();
                OPUtils.sendAppTrackerForThemes();
                OPUtils.sendAppTrackerForAccentColor();
                OPUtils.sendAppTrackerForAssistApp();
                OPUtils.sendAppTrackerForDefaultHomeApp();
                OPUtils.sendAppTrackerForGameModeSpeakerAnswer();
                OPUtils.sendAppTrackerForGameModeNotificationShow();
                OPUtils.sendAppTrackerForGameMode3drPartyCalls();
                OPUtils.sendAppTrackerForGameModeAdEnable();
                OPUtils.sendAppTrackerForGameModeBrightness();
                OPUtils.sendAppTrackerForGameModeNetWorkBoost();
                OPUtils.sendAppTrackerForGameModeApps(System.getString(SettingsBaseApplication.mApplication.getContentResolver(), OPConstants.GAME_MODE_APPS));
                OPUtils.sendAppTrackerForGameModeRemovedApps();
                OPUtils.sendAppTrackerForSmartWifiSwitch();
                OPUtils.sendAppTrackerForDataAutoSwitch();
                OPUtils.sendAppTrackerForQuickReply();
                OPUtils.sendAppTrackerForQuickReplyIMStatus();
                OPUtils.sendAppTrackerForQuickReplyKeyboardStatus();
                OPHapticFeedback.sendDefaultAppTracker();
                OPRamBoostSettings.sendDefaultAppTracker();
                OPUtils.sendAppTracker("pop_up_face_unlock", System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), OPFaceUnlockModeSettings.ONEPLUS_FACE_UNLOCK_POWERKEY_RECOGNIZE_ENABLE, 0));
            }
        }).start();
    }

    public static void sendAppTracker(String key, boolean value) {
        if (isAllowSendAppTracker(SettingsBaseApplication.mApplication.getApplicationContext())) {
            mAppTracker = new AppTracker(SettingsBaseApplication.mApplication);
            Map<String, String> mdmData = new HashMap();
            mdmData.put(key, Boolean.toString(value));
            if (mdmData.size() > 0) {
                mAppTracker.onEvent(key, mdmData);
            }
            sendGoogleTracker(TRACKER_CATEGORY, key, Boolean.toString(value));
        }
    }

    public static void sendAppTracker(String key, String value) {
        if (isAllowSendAppTracker(SettingsBaseApplication.mApplication.getApplicationContext())) {
            mAppTracker = new AppTracker(SettingsBaseApplication.mApplication);
            Map<String, String> mdmData = new HashMap();
            mdmData.put(key, value);
            if (mdmData.size() > 0) {
                mAppTracker.onEvent(key, mdmData);
            }
            sendGoogleTracker(TRACKER_CATEGORY, key, value);
        }
    }

    public static void sendAppTracker(String key, int value) {
        if (isAllowSendAppTracker(SettingsBaseApplication.mApplication.getApplicationContext())) {
            mAppTracker = new AppTracker(SettingsBaseApplication.mApplication);
            Map<String, String> mdmData = new HashMap();
            mdmData.put(key, Integer.toString(value));
            if (mdmData.size() > 0) {
                mAppTracker.onEvent(key, mdmData);
            }
            sendGoogleTracker(TRACKER_CATEGORY, key, Integer.toString(value));
        }
    }

    public static void sendAppTracker(String key, Long value) {
        if (isAllowSendAppTracker(SettingsBaseApplication.mApplication.getApplicationContext())) {
            mAppTracker = new AppTracker(SettingsBaseApplication.mApplication);
            Map<String, String> mdmData = new HashMap();
            mdmData.put(key, Long.toString(value.longValue()));
            if (mdmData.size() > 0) {
                mAppTracker.onEvent(key, mdmData);
            }
            sendGoogleTracker(TRACKER_CATEGORY, key, Long.toString(value.longValue()));
        }
    }

    private static int compositeColorComponent(int c1, int a1, int c2, int a2, int a) {
        if (a == 0) {
            return 0;
        }
        return ((((255 * c2) * a2) + ((c1 * a1) * (255 - a2))) / a) / 255;
    }

    public static int compositeColor(int argb1, int argb2) {
        int a1 = Color.alpha(argb1);
        int a2 = Color.alpha(argb2);
        int a = 255 - (((255 - a2) * (255 - a1)) / 255);
        return Color.argb(a, compositeColorComponent(Color.red(argb1), a1, Color.red(argb2), a2, a), compositeColorComponent(Color.green(argb1), a1, Color.green(argb2), a2, a), compositeColorComponent(Color.blue(argb1), a1, Color.blue(argb2), a2, a));
    }

    public static boolean isO2() {
        return OpFeatures.isSupport(new int[]{1});
    }

    public static boolean isOP6ModeBefore() {
        return Build.MODEL.contains("A50") || Build.MODEL.contains("A30") || Build.MODEL.contains("A20") || Build.MODEL.contains("A10") || Build.MODEL.contains("A00") || Build.MODEL.contains("E10");
    }

    public static boolean isOP3() {
        return ONEPLUS_15801.equals(SystemProperties.get("ro.boot.project_name"));
    }

    public static boolean isOP3T() {
        return ONEPLUS_15811.equals(SystemProperties.get("ro.boot.project_name"));
    }

    public static boolean isNeedTcpTimestampsControl() {
        String productName = SystemProperties.get("ro.boot.project_name");
        for (String equalsIgnoreCase : productNotNeedTcpTimestampsControl) {
            if (productName.equalsIgnoreCase(equalsIgnoreCase)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSurportGesture20(Context context) {
        return context.getPackageManager().hasSystemFeature(OPConstants.OEM_BLACKSCREENGESTURE_2_SUPPORT);
    }

    public static boolean isSurportNoNeedPowerOnPassword(Context context) {
        return context.getPackageManager().hasSystemFeature(OPConstants.NO_NEED_POWER_ON_PASSWORD);
    }

    public static boolean isSurportProductInfo16859(Context context) {
        return context.getPackageManager().hasSystemFeature(OPConstants.OEM_PRODUCT_INFO_16859_SUPPORT);
    }

    public static boolean isSurportProductInfo17801(Context context) {
        return context.getPackageManager().hasSystemFeature(OPConstants.OEM_PRODUCT_INFO_17801_SUPPORT);
    }

    public static boolean isSurportProductInfo(Context context) {
        return isSurportProductInfo16859(context) || isSurportProductInfo17801(context);
    }

    public static boolean isSurportSimNfc(Context context) {
        return context.getPackageManager().hasSystemFeature(OPConstants.OEM_SIM_NFC_SUPPORT);
    }

    public static boolean isSurportFaceUnlock(Context context) {
        return OpFeatures.isSupport(new int[]{38});
    }

    public static boolean isSurportBackFingerprint(Context context) {
        return context.getResources().getBoolean(17957029);
    }

    public static boolean isSurportNavigationBarOnly(Context context) {
        return context.getResources().getBoolean(17957029);
    }

    public static int getFingerprintScaleAnimStep(Context context) {
        if (!isSurportBackFingerprint(context) || isFingerprintNeedEnrollTime20(context)) {
            return 10;
        }
        return 8;
    }

    public static void disableAospFaceUnlock(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.setApplicationEnabledSetting(OPConstants.FACELOCK_PACKAGE_NAME, 2, 1);
            pm.setApplicationHiddenSettingAsUser(OPConstants.FACELOCK_PACKAGE_NAME, true, UserHandle.OWNER);
        } catch (Exception e) {
        }
    }

    public static void restoreBackupEntranceInLauncher(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                ContentResolver resolver = context.getContentResolver();
                String pkgName = "com.oneplus.backuprestore";
                String mainActivityName = "com.oneplus.backuprestore.activity.BootActivity";
                if (System.getInt(resolver, "oneplus_backuprestore_disabled", 0) == 1) {
                    try {
                        Log.d(OPUtils.TAG, "restore entry");
                        context.getPackageManager().setComponentEnabledSetting(new ComponentName(pkgName, mainActivityName), 1, 1);
                        System.putInt(resolver, "oneplus_backuprestore_disabled", 0);
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    public static void enableAppBgService(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                BatteryUtils batteryUtils = BatteryUtils.getInstance(context);
                BgOActivityManager bgOActivityManager = BgOActivityManager.getInstance(context);
                for (String pkg : OPUtils.bgServicePackages) {
                    if (OPUtils.isAppExist(context, pkg) && BgOActivityManager.getInstance(context).getAppControlMode(pkg, 0) == 0) {
                        batteryUtils.setForceAppStandby(batteryUtils.getPackageUid(pkg), pkg, 0);
                        bgOActivityManager.setAppControlMode(pkg, 0, 1);
                        String str = OPUtils.TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("enableAppBgService pkg:");
                        stringBuilder.append(pkg);
                        Log.d(str, stringBuilder.toString());
                    }
                }
            }
        }).start();
    }

    public static void disableWirelessAdbDebuging() {
        SystemProperties.set("service.adb.tcp.port", "-1");
    }

    public static boolean isApplicationEnabled(Context context, String pkg) {
        try {
            if (context.getPackageManager().getApplicationEnabledSetting(pkg) == 2) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public static void disableCardPackageEntranceInLauncher(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                ContentResolver resolver = context.getContentResolver();
                String pkgName = "com.oneplus.card";
                String mainActivityName = "com.oneplus.card.entity.activity.CardlistActivity";
                if (OPUtils.isO2() && PackageUtils.isSystemApplication(context, pkgName) && OPUtils.isCardPackageListActivityEnable(context)) {
                    try {
                        Log.d(OPUtils.TAG, "disableCardPackageEntranceInLauncher");
                        context.getPackageManager().setComponentEnabledSetting(new ComponentName(pkgName, mainActivityName), 2, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static boolean isCardPackageListActivityEnable(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.oneplus.card", "com.oneplus.card.entity.activity.CardlistActivity");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setAction("android.intent.action.MAIN");
        boolean z = false;
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        if (list == null) {
            return false;
        }
        if (list.size() > 0) {
            z = true;
        }
        return z;
    }

    public static void enablePackageInstaller(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(UserHandle.myUserId());
                if (userInfo != null && userInfo.id == 999) {
                    try {
                        PackageManager pm = context.getPackageManager();
                        ComponentName o2ComponentName;
                        if (OPUtils.isO2()) {
                            o2ComponentName = new ComponentName("com.google.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity");
                            if (pm.getComponentEnabledSetting(o2ComponentName) != 1) {
                                pm.setComponentEnabledSetting(o2ComponentName, 1, 1);
                            }
                            return;
                        }
                        o2ComponentName = new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity");
                        if (pm.getComponentEnabledSetting(o2ComponentName) != 1) {
                            pm.setComponentEnabledSetting(o2ComponentName, 1, 1);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
    }

    public static boolean isFaceUnlockEnabled(Context context) {
        boolean z = false;
        boolean isEnabled = false;
        try {
            int state = context.getPackageManager().getApplicationEnabledSetting(OPConstants.FACELOCK_PACKAGE_NAME);
            if (state == 1 || state == 0) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            return isEnabled;
        }
    }

    public static int getAccentColor(Context context) {
        TypedValue tintColor = new TypedValue();
        context.getTheme().resolveAttribute(16843829, tintColor, true);
        return context.getColor(tintColor.resourceId);
    }

    public static int getCustomAccentColor() {
        String accentColor = SystemProperties.get("persist.sys.theme.accentcolor");
        if (TextUtils.isEmpty(accentColor)) {
            return Color.parseColor(OPConstants.ONEPLUS_ACCENT_DEFAULT_COLOR);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("#");
        stringBuilder.append(accentColor);
        return Color.parseColor(stringBuilder.toString());
    }

    public static String showROMStorage(Context mContext) {
        return Formatter.formatFileSize(mContext, PrivateStorageInfo.getPrivateStorageInfo(new StorageManagerVolumeProvider((StorageManager) mContext.getSystemService(StorageManager.class))).totalBytes).replace(" ", "");
    }

    private static String formatMemoryDisplay(long size) {
        long mega = (PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID * size) / FileSizeFormatter.MEGABYTE_IN_BYTES;
        int mul = (int) (mega / 512);
        int modulus = (int) (mega % 512);
        StringBuilder stringBuilder;
        if (mul == 0) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(mega);
            stringBuilder.append("MB");
            return stringBuilder.toString();
        } else if (modulus > 256) {
            mul++;
            if (mul % 2 == 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append((int) (0.5f * ((float) mul)));
                stringBuilder.append("GB");
                return stringBuilder.toString();
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append(0.5f * ((float) mul));
            stringBuilder.append("GB");
            return stringBuilder.toString();
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append((0.5f * ((float) mul)) + 0.25f);
            stringBuilder.append("GB");
            return stringBuilder.toString();
        }
    }

    public static String getTotalMemory() {
        IOException e;
        String str2 = "";
        FileReader fr = null;
        BufferedReader localBufferedReader = null;
        try {
            fr = new FileReader("/proc/meminfo");
            localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            if (str2 == null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                try {
                    fr.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
                return null;
            }
            str2 = str2.substring(10).trim();
            str2 = str2.substring(0, str2.length() - 2);
            str2 = str2.trim();
            try {
                localBufferedReader.close();
            } catch (IOException e3) {
                e3.printStackTrace();
            }
            try {
                fr.close();
            } catch (IOException e4) {
                e3 = e4;
            }
            return formatMemoryDisplay(Long.parseLong(str2));
            e3.printStackTrace();
            return formatMemoryDisplay(Long.parseLong(str2));
        } catch (IOException e5) {
            if (localBufferedReader != null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e32) {
                    e32.printStackTrace();
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e6) {
                    e32 = e6;
                }
            }
        } catch (Throwable th) {
            if (localBufferedReader != null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e2222) {
                    e2222.printStackTrace();
                }
            }
        }
    }

    public static boolean isDeviceSurportFaceUnlock() {
        return OPConstants.ONEPLUS_A5000.equalsIgnoreCase(Build.MODEL) || OPConstants.ONEPLUS_A5010.equalsIgnoreCase(Build.MODEL);
    }

    public static boolean isFingerprintNeedEnrollTime20(Context context) {
        String fpVersion = SystemProperties.get("persist.vendor.oem.fp.version", "5");
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fpVersion = ");
        stringBuilder.append(fpVersion);
        Log.d(str, stringBuilder.toString());
        if ("4".equals(fpVersion)) {
            return true;
        }
        return false;
    }

    public static boolean isFingerprintNeedEnrollTime16(Context context) {
        return (!isSurportBackFingerprint(context) || OPConstants.ONEPLUS_A5000.equalsIgnoreCase(Build.MODEL) || OPConstants.ONEPLUS_A5010.equalsIgnoreCase(Build.MODEL)) ? false : true;
    }

    public static boolean isZhCn(Context context) {
        return context.getResources().getConfiguration().locale.getCountry().equals("CN");
    }

    public static boolean isZh(Context context) {
        if (context.getResources().getConfiguration().locale.getLanguage().endsWith("zh")) {
            return true;
        }
        return false;
    }

    public static boolean isEn(Context context) {
        return "en".equals(context.getResources().getConfiguration().locale.getLanguage());
    }

    public static int getThemeMode(ContentResolver contentResolver) {
        return System.getIntForUser(contentResolver, "oem_black_mode", 0, 0);
    }

    public static boolean isWhiteModeOn(ContentResolver contentResolver) {
        return System.getIntForUser(contentResolver, "oem_black_mode", 0, 0) == 0;
    }

    public static boolean isBlackModeOn(ContentResolver contentResolver) {
        return System.getIntForUser(contentResolver, "oem_black_mode", 0, 0) == 1;
    }

    public static boolean isAndroidModeOn(ContentResolver contentResolver) {
        return System.getIntForUser(contentResolver, "oem_black_mode", 0, 0) == 2;
    }

    public static boolean isStarWarModeOn(ContentResolver contentResolver) {
        return System.getIntForUser(contentResolver, "oem_special_theme", 0, 0) == 1;
    }

    public static boolean isThemeOn(ContentResolver contentResolver) {
        return System.getIntForUser(contentResolver, "oem_special_theme", 0, 0) == 1;
    }

    public static int getRightTheme(Context context, @StyleRes int themeLight, @StyleRes int themeDark) {
        return getRightTheme(context.getContentResolver(), themeLight, themeDark);
    }

    public static int getRightTheme(ContentResolver contentResolver, @StyleRes int themeLight, @StyleRes int themeDark) {
        return isBlackModeOn(contentResolver) ? themeDark : themeLight;
    }

    public static int getOnePlusPrimaryColor(Context context) {
        return context.getColor(R.color.settings_accent_color);
    }

    public static ColorStateList createColorStateList(int color_state_pressed, int color_state_selected, int color_state_enabled, int color_state_default) {
        int[][] iArr = new int[4][];
        iArr[0] = new int[]{16842919};
        iArr[1] = new int[]{16842913};
        iArr[2] = new int[]{16842910};
        iArr[3] = new int[0];
        return new ColorStateList(iArr, new int[]{color_state_pressed, color_state_selected, color_state_enabled, color_state_default});
    }

    public static ColorStateList creatOneplusPrimaryColorStateList(Context context) {
        int onePlusPrimaryColor = getAccentColor(context);
        int color_state_disable = context.getResources().getColor(R.color.oneplus_font_list_setting_title);
        int color_state_pressed = context.getResources().getColor(R.color.oneplus_font_list_subtitle);
        return createColorStateList(color_state_pressed, color_state_pressed, onePlusPrimaryColor, color_state_disable);
    }

    public static int getColor(Theme theme, int attrId) {
        TypedValue outValue = new TypedValue();
        theme.resolveAttribute(attrId, outValue, true);
        return outValue.data;
    }

    public static Bitmap getTintSvgBitmap(Context context, int vectorDrawableId, @ColorInt int color) throws NotFoundException {
        Bitmap bitmap = getBitmap(context, vectorDrawableId);
        Bitmap tintBitmap = tintBitmap(bitmap, color);
        bitmap.recycle();
        return tintBitmap;
    }

    public static Bitmap getBitmap(Context context, int vectorDrawableId) throws NotFoundException {
        if (VERSION.SDK_INT <= 21) {
            return BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap tintBitmap(Bitmap bitmap, @ColorInt int color) {
        if (bitmap == null) {
            return bitmap;
        }
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        new Canvas(bitmapResult).drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmapResult;
    }

    public static Bitmap getActivityIcon(Bitmap bitmap, @ColorInt int color) {
        if (bitmap == null) {
            return bitmap;
        }
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, Mode.SRC_IN));
        Bitmap bitmapResult = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        new Canvas(bitmapResult).drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmapResult;
    }

    public static int dp2Px(DisplayMetrics displayMetrics, float dp) {
        return (int) TypedValue.applyDimension(1, dp, displayMetrics);
    }

    public static int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        return (int) ((pxValue / context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static boolean isLTRLayout(Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == 0;
    }

    public static void setPreferenceDivider(Context context, SettingsPreferenceFragment pf, int dividerDrawableStart, int dividerDrawableEnd, int dividerHight) {
        Configuration config = context.getResources().getConfiguration();
        Resources res = context.getResources();
        if (isLTRLayout(context)) {
            pf.setDivider(res.getDrawable(dividerDrawableStart));
        } else {
            pf.setDivider(res.getDrawable(dividerDrawableEnd));
        }
        float hight = (float) res.getDimensionPixelSize(dividerHight);
        pf.setDividerHeight(dip2px(context, 1.0f));
    }

    public static boolean isInQuickLaunchList(Context context, OPAppModel model) {
        if (model == null) {
            return false;
        }
        boolean isInQuickLaunchList;
        String allQuickLaunchApp = getAllQuickLaunchStrings(context);
        if (model.getType() == 0 && allQuickLaunchApp.contains(getQuickLaunchAppString(model))) {
            isInQuickLaunchList = true;
        } else if (model.getType() == 1 && allQuickLaunchApp.contains(getQuickLaunchShortcutsString(model))) {
            isInQuickLaunchList = true;
        } else if (model.getType() == 2 && allQuickLaunchApp.contains(getQuickPayAppString(model))) {
            isInQuickLaunchList = true;
        } else {
            isInQuickLaunchList = false;
        }
        return isInQuickLaunchList;
    }

    public static boolean isQuickReplyAppSelected(OPAppModel model) {
        String allQuickReplyAppList = getQuickReplyAppListString(SettingsBaseApplication.mApplication);
        if (TextUtils.isEmpty(allQuickReplyAppList) || !allQuickReplyAppList.contains(model.getPkgName())) {
            return false;
        }
        return true;
    }

    public static boolean isQuickReplyAppSelected(String pkg) {
        String allQuickReplyAppList = getQuickReplyAppListString(SettingsBaseApplication.mApplication);
        if (TextUtils.isEmpty(allQuickReplyAppList) || !allQuickReplyAppList.contains(pkg)) {
            return false;
        }
        return true;
    }

    public static void deleteQuickReply(OPAppModel model) {
        StringBuilder quickReplyAppList = new StringBuilder(getQuickReplyAppListString(SettingsBaseApplication.mApplication));
        String quickReplyPkg = getQuickReplyAppString(model);
        int index = quickReplyAppList.indexOf(quickReplyPkg);
        quickReplyAppList.delete(index, quickReplyPkg.length() + index);
        saveQuickReplyAppLisStrings(SettingsBaseApplication.mApplication, quickReplyAppList.toString());
    }

    public static String getQuickReplyAppString(OPAppModel model) {
        if (model == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(model.getPkgName());
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    public static String getQuickReplyAppListString(Context context) {
        String quickReplyAppList = System.getString(context.getContentResolver(), "op_quickreply_im_list");
        if (TextUtils.isEmpty(quickReplyAppList)) {
            return "";
        }
        return quickReplyAppList;
    }

    public static void saveQuickReplyAppLisStrings(Context context, String quickReplyAppList) {
        System.putString(context.getContentResolver(), "op_quickreply_im_list", removeRepeatedStrings(quickReplyAppList));
        sendAppTrackerForQuickReply();
    }

    public static String removeRepeatedStrings(String sourceSting) {
        if (TextUtils.isEmpty(sourceSting)) {
            return "";
        }
        Set<String> staffsSet = new HashSet(Arrays.asList(sourceSting.split(";")));
        StringBuilder strappend = new StringBuilder();
        for (String str : staffsSet) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append(";");
            strappend.append(stringBuilder.toString());
        }
        if (TextUtils.isEmpty(strappend)) {
            return "";
        }
        return strappend.toString();
    }

    public static void sendAppTrackerForQuickReply() {
        sendAppTracker(OPConstants.OP_IM_QUICK_REPLY, getQuickReplyAppListString(SettingsBaseApplication.mApplication.getApplicationContext()));
    }

    public static void deleteGameModeAppString(OPAppModel model) {
        StringBuilder gameModeAppList = new StringBuilder(getGameModeAppListString(SettingsBaseApplication.mApplication));
        String gameModePkg = getGameModeAppString(model);
        int index = gameModeAppList.indexOf(gameModePkg);
        gameModeAppList.delete(index, gameModePkg.length() + index);
        saveGameModeRemovedAppLisStrings(SettingsBaseApplication.mApplication, gameModeAppList.toString());
    }

    public static String getGameModeAppString(OPAppModel model) {
        if (model == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(model.getPkgName());
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    public static String getGameModeAppListString(Context context) {
        String gameModeAppList = Global.getString(context.getContentResolver(), OPConstants.OP_GAMEMODE_REMOVED_PACKAGES_BY_USER_KEY);
        if (TextUtils.isEmpty(gameModeAppList)) {
            return "";
        }
        return gameModeAppList;
    }

    public static boolean isInRemovedGameAppListString(Context context, OPAppModel model) {
        String removedAppListString = Global.getString(context.getContentResolver(), OPConstants.OP_GAMEMODE_REMOVED_PACKAGES_BY_USER_KEY);
        if (TextUtils.isEmpty(removedAppListString) || !removedAppListString.contains(model.getPkgName())) {
            return false;
        }
        return true;
    }

    public static void saveGameModeRemovedAppLisStrings(Context context, String gameModeAppList) {
        Global.putString(context.getContentResolver(), OPConstants.OP_GAMEMODE_REMOVED_PACKAGES_BY_USER_KEY, gameModeAppList);
    }

    public static void sendAppTrackerForGameModeRemovedApps() {
        sendAppTracker(OPConstants.OP_GAMEMODE_REMOVED_PACKAGES_BY_USER_KEY, Global.getString(SettingsBaseApplication.mApplication.getContentResolver(), OPConstants.OP_GAMEMODE_REMOVED_PACKAGES_BY_USER_KEY));
    }

    public static boolean isQuickPayModel(OPAppModel model) {
        if (model == null) {
            return false;
        }
        if ("0".equals(model.getShortCutId()) || "1".equals(model.getShortCutId()) || "2".equals(model.getShortCutId()) || "3".equals(model.getShortCutId()) || "4".equals(model.getShortCutId())) {
            return true;
        }
        return false;
    }

    public static String getQuickPayAppString(OPAppModel model) {
        if (model == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OPConstants.OPEN_QUICK_PAY);
        stringBuilder.append(model.getPkgName());
        stringBuilder.append(";");
        stringBuilder.append(model.getShortCutId());
        stringBuilder.append(",");
        return stringBuilder.toString();
    }

    public static String getQuickLaunchAppString(OPAppModel model) {
        if (model == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OPConstants.OPEN_APP);
        stringBuilder.append(model.getPkgName());
        stringBuilder.append(";");
        stringBuilder.append(model.getUid());
        stringBuilder.append(",");
        return stringBuilder.toString();
    }

    public static String getQuickLaunchShortcutsString(OPAppModel model) {
        if (model == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OPConstants.OPEN_SHORTCUT);
        stringBuilder.append(model.getPkgName());
        stringBuilder.append(";");
        stringBuilder.append(model.getShortCutId());
        stringBuilder.append(";");
        stringBuilder.append(model.getUid());
        stringBuilder.append(",");
        return stringBuilder.toString();
    }

    public static int getQuickLaunchShortcutsAccount(Context context) {
        String shortcut = Secure.getString(context.getContentResolver(), OPConstants.QUICK_LAUNCH_APPS_KEY);
        if (TextUtils.isEmpty(shortcut)) {
            return 0;
        }
        return shortcut.split(",").length;
    }

    public static String getAllQuickLaunchStrings(Context context) {
        String allQuickLaunch = Secure.getString(context.getContentResolver(), OPConstants.QUICK_LAUNCH_APPS_KEY);
        if (TextUtils.isEmpty(allQuickLaunch)) {
            return "";
        }
        return allQuickLaunch;
    }

    public static Drawable getQuickPayIconByType(Context context, int type) {
        int iconId = R.drawable.op_wechat_qrcode;
        switch (type) {
            case 0:
                iconId = R.drawable.op_wechat_qrcode;
                break;
            case 1:
                iconId = R.drawable.op_wechat_scanning;
                break;
            case 2:
                iconId = R.drawable.op_alipay_qrcode;
                break;
            case 3:
                iconId = R.drawable.op_alipay_scanning;
                break;
        }
        return context.getDrawable(iconId);
    }

    public static List<OPAppModel> loadShortcutByPackageName(Context context, String pkg, int uid) {
        Context context2 = context;
        ArrayList mAllQuickLaunchShortcuts = new ArrayList();
        List<ShortcutInfo> shortcutInfo = OPGestureUtils.loadShortCuts(context, pkg);
        if (shortcutInfo == null) {
            return mAllQuickLaunchShortcuts;
        }
        int size = shortcutInfo.size();
        LauncherApps mLauncherApps = (LauncherApps) context2.getSystemService("launcherapps");
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= size) {
                return mAllQuickLaunchShortcuts;
            }
            ShortcutInfo s = (ShortcutInfo) shortcutInfo.get(i2);
            CharSequence label = s.getLongLabel();
            if (TextUtils.isEmpty(label)) {
                label = s.getShortLabel();
            }
            if (TextUtils.isEmpty(label)) {
                label = s.getId();
            }
            OPAppModel model = new OPAppModel(s.getPackage(), label.toString(), s.getId(), uid, false);
            model.setAppLabel(getAppLabel(context2, s.getPackage()));
            model.setType(1);
            model.setSelected(isInQuickLaunchList(context2, model));
            model.setAppIcon(getAppIcon(context, pkg));
            try {
                model.setShortCutIcon(mLauncherApps.getShortcutIconDrawable(s, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mAllQuickLaunchShortcuts.add(model);
            i = i2 + 1;
        }
    }

    public static OPAppModel loadShortcutByPackageNameAndShortcutId(Context context, String pkg, String shortCutId, int uid) {
        Context context2 = context;
        List<ShortcutInfo> shortcutInfo = OPGestureUtils.loadShortCuts(context, pkg);
        if (shortcutInfo == null) {
            return null;
        }
        int size = shortcutInfo.size();
        LauncherApps mLauncherApps = (LauncherApps) context2.getSystemService("launcherapps");
        for (int i = 0; i < size; i++) {
            ShortcutInfo s = (ShortcutInfo) shortcutInfo.get(i);
            if (shortCutId.equals(s.getId())) {
                CharSequence label = s.getLongLabel();
                if (TextUtils.isEmpty(label)) {
                    label = s.getShortLabel();
                }
                if (TextUtils.isEmpty(label)) {
                    label = s.getId();
                }
                OPAppModel model = new OPAppModel(s.getPackage(), label.toString(), s.getId(), uid, false);
                model.setAppLabel(getAppLabel(context2, s.getPackage()));
                model.setType(1);
                model.setSelected(isInQuickLaunchList(context2, model));
                model.setAppIcon(getAppIcon(context, pkg));
                try {
                    model.setShortCutIcon(mLauncherApps.getShortcutIconDrawable(s, 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return model;
            }
        }
        String str = shortCutId;
        return null;
    }

    public static Drawable getBadgedIcon(PackageManager pm, ApplicationInfo info) {
        return pm.getUserBadgedIcon(pm.loadUnbadgedItemIcon(info, info), new UserHandle(UserHandle.getUserId(info.uid)));
    }

    public static int getQuickLaunchAppCount(Context context) {
        List<OPAppModel> appList = parseAllQuickLaunchStrings(context);
        if (appList == null) {
            return 0;
        }
        return appList.size();
    }

    public static List<OPAppModel> parseAllQuickLaunchStrings(Context context) {
        Context context2 = context;
        PackageManager pm = context.getPackageManager();
        List<OPAppModel> appList = new ArrayList();
        String allQuickLaunch = Secure.getString(context.getContentResolver(), OPConstants.QUICK_LAUNCH_APPS_KEY);
        StringBuilder allQuickLaunchTemp = new StringBuilder(getAllQuickLaunchStrings(context));
        if (!TextUtils.isEmpty(allQuickLaunch)) {
            String[] allQuickLaunchList = allQuickLaunch.split(",");
            int i = 0;
            int i2 = 0;
            while (i2 < allQuickLaunchList.length) {
                String[] app;
                String pkgName;
                String pkgUid;
                String pkgName2;
                OPAppModel shortcutModel;
                if (allQuickLaunchList[i2].startsWith(OPConstants.OPEN_APP)) {
                    app = allQuickLaunchList[i2].split(";");
                    pkgName = app[i].substring(app[i].indexOf(":") + 1);
                    if (!isAppExist(context2, pkgName) || getResolveInfoByPackageName(context2, pkgName) == null) {
                        int index = allQuickLaunchTemp.indexOf(allQuickLaunchList[i2]);
                        allQuickLaunchTemp.delete(index, (allQuickLaunchList[i2].length() + index) + 1);
                    } else {
                        pkgUid = app[1];
                        OPAppModel oPAppModel = new OPAppModel(pkgName, getAppLabel(context2, pkgName), "", Integer.valueOf(pkgUid).intValue(), false);
                        oPAppModel.setAppIcon(getBadgedIcon(pm, getApplicationInfoByUserId(context2, pkgName, Integer.valueOf(pkgUid).intValue())));
                        oPAppModel.setType(i);
                        appList.add(oPAppModel);
                    }
                } else if (allQuickLaunchList[i2].startsWith(OPConstants.OPEN_SHORTCUT)) {
                    app = allQuickLaunchList[i2].split(";");
                    pkgName2 = app[i].substring(app[i].indexOf(":") + 1);
                    if (!isAppExist(context2, pkgName2) || getResolveInfoByPackageName(context2, pkgName2) == null) {
                        int index2 = allQuickLaunchTemp.indexOf(allQuickLaunchList[i2]);
                        allQuickLaunchTemp.delete(index2, (allQuickLaunchList[i2].length() + index2) + 1);
                    } else {
                        String shortcutId = app[1];
                        pkgName = app[2];
                        OPAppModel shortcutModel2 = loadShortcutInfoByPackageName(context2, pkgName2, shortcutId, Integer.valueOf(pkgName).intValue());
                        if (shortcutModel2 == null) {
                            shortcutModel2 = new OPAppModel(pkgName2, getAppLabel(context2, pkgName2), shortcutId, Integer.valueOf(pkgName).intValue(), false);
                        }
                        shortcutModel = shortcutModel2;
                        shortcutModel.setType(1);
                        appList.add(shortcutModel);
                    }
                } else if (allQuickLaunchList[i2].startsWith(OPConstants.OPEN_QUICK_PAY)) {
                    app = allQuickLaunchList[i2].split(";");
                    pkgName2 = app[i].substring(app[i].indexOf(":") + 1);
                    if (!isAppExist(context2, pkgName2) || getResolveInfoByPackageName(context2, pkgName2) == null) {
                        i = allQuickLaunchTemp.indexOf(allQuickLaunchList[i2]);
                        allQuickLaunchTemp.delete(i, (allQuickLaunchList[i2].length() + i) + 1);
                    } else {
                        pkgUid = app[1];
                        int payTypeValue = Integer.valueOf(pkgUid).intValue();
                        i = 4;
                        shortcutModel = new OPAppModel(pkgName2, context.getResources().getStringArray(R.array.oneplus_quickpay_ways_name)[payTypeValue > 4 ? 4 : payTypeValue], pkgUid, 0, false);
                        if (payTypeValue == i) {
                            shortcutModel.setAppIcon(getAppIcon(context2, pkgName2));
                        } else {
                            shortcutModel.setAppIcon(getQuickPayIconByType(context2, payTypeValue));
                        }
                        shortcutModel.setType(2);
                        appList.add(shortcutModel);
                    }
                }
                i2++;
                i = 0;
            }
        }
        saveQuickLaunchStrings(context2, allQuickLaunchTemp.toString());
        return appList;
    }

    public static OPAppModel loadShortcutInfoByPackageName(Context context, String pkg, String shortcutId, int uid) {
        Context context2 = context;
        List<ShortcutInfo> shortcutInfo = OPGestureUtils.loadShortCuts(context, pkg);
        if (shortcutInfo == null) {
            return null;
        }
        int size = shortcutInfo.size();
        LauncherApps mLauncherApps = (LauncherApps) context2.getSystemService("launcherapps");
        for (int i = 0; i < size; i++) {
            ShortcutInfo s = (ShortcutInfo) shortcutInfo.get(i);
            CharSequence label = s.getLongLabel();
            if (TextUtils.isEmpty(label)) {
                label = s.getShortLabel();
            }
            if (TextUtils.isEmpty(label)) {
                label = s.getId();
            }
            if (shortcutId.equals(s.getId())) {
                OPAppModel model = new OPAppModel(s.getPackage(), label.toString(), s.getId(), uid, false);
                model.setType(1);
                model.setSelected(isInQuickLaunchList(context2, model));
                model.setAppIcon(getAppIcon(context, pkg));
                try {
                    model.setShortCutIcon(mLauncherApps.getShortcutIconDrawable(s, 0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return model;
            }
        }
        String str = shortcutId;
        return null;
    }

    public static String getAppLabel(Context context, String pakgename) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(pakgename, 128)).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Drawable getAppIcon(Context context, String pakgename) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationIcon(pm.getApplicationInfo(pakgename, 128));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveQuickLaunchStrings(Context context, String quickLaunch) {
        Secure.putString(context.getContentResolver(), OPConstants.QUICK_LAUNCH_APPS_KEY, quickLaunch);
    }

    public static void setListDivider(Context context, ListView listview, int dividerDrawableStart, int dividerDrawableEnd, int dividerHight) {
        Configuration config = context.getResources().getConfiguration();
        Resources res = context.getResources();
        if (isLTRLayout(context)) {
            listview.setDivider(res.getDrawable(dividerDrawableStart));
        } else {
            listview.setDivider(res.getDrawable(dividerDrawableEnd));
        }
        listview.setDividerHeight(res.getDimensionPixelSize(dividerHight));
    }

    public static void replaceZhCnToZhCnHANS(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                List<LocaleInfo> localeList = OPUtils.getUserLocaleList(context);
                int count = localeList.size();
                Locale[] newList = new Locale[count];
                for (int i = 0; i < count; i++) {
                    newList[i] = ((LocaleInfo) localeList.get(i)).getLocale();
                }
                LocaleList ll = new LocaleList(newList);
                LocaleList.setDefault(ll);
                LocalePicker.updateLocales(ll);
            }
        }).start();
    }

    private static List<LocaleInfo> getUserLocaleList(Context context) {
        List<LocaleInfo> result = new ArrayList();
        LocaleList localeList = LocalePicker.getLocales();
        for (int i = 0; i < localeList.size(); i++) {
            Locale locale = localeList.get(i);
            if (ZH_CN_ID.equals(LocaleStore.getLocaleInfo(locale).getId())) {
                locale = Locale.forLanguageTag(ZH_CN_HANS_ID);
            }
            result.add(LocaleStore.getLocaleInfo(locale));
        }
        return result;
    }

    public static boolean isFeatureSupport(Context context, String feature) {
        return context.getPackageManager().hasSystemFeature(feature);
    }

    public static void sendGoogleTracker(String category, String key, String value) {
        SettingsBaseApplication app = SettingsBaseApplication.mApplication;
        if (!(TextUtils.isEmpty(value) || app == null)) {
            try {
                if (app.isBetaRom()) {
                    String label = null;
                    long v = 0;
                    if (isNumeric(value)) {
                        v = (long) Integer.valueOf(value).intValue();
                    } else {
                        label = value;
                    }
                    app.getDefaultTracker().send(MapBuilder.createEvent(category, key, label, Long.valueOf(v)).build());
                }
            } catch (NumberFormatException e) {
            }
        }
    }

    public static boolean isBetaRom() {
        String SYSTEM_PROPERTY_KEY_IS_BETA_ROM = "ro.build.beta";
        String SYSTEM_PROPERTY_KEY_ENABLE_GA = "persist.op.ga";
        String isBeta = SystemProperties.get("ro.build.beta");
        String isEanbleGA = SystemProperties.get("persist.op.ga");
        if ("1".equals(isBeta) || "1".equals(isEanbleGA)) {
            return true;
        }
        return false;
    }

    public static boolean isSupportFontStyleSetting() {
        String country = Locale.getDefault().getCountry();
        String language = Locale.getDefault().getLanguage();
        String[] supportlanguage = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_font_style_support_language);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("language = ");
        stringBuilder.append(language);
        stringBuilder.append(" country = ");
        stringBuilder.append(country);
        Log.d("FontStyleSetting", stringBuilder.toString());
        for (String equalsIgnoreCase : supportlanguage) {
            if (equalsIgnoreCase.equalsIgnoreCase(language)) {
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("support language = ");
                stringBuilder2.append(language);
                Log.d("FontStyleSetting", stringBuilder2.toString());
                return true;
            }
        }
        return false;
    }

    public static String getImei(Context context) {
        String imei = ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
        if (imei == null) {
            Log.i(TAG, "IMEI is null");
            return "";
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IMEI = ");
        stringBuilder.append(imei);
        Log.i(str, stringBuilder.toString());
        return imei;
    }

    public static String getDeviceModel() {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DeviceModel = ");
        stringBuilder.append(Build.MODEL);
        Log.i(str, stringBuilder.toString());
        return Build.MODEL;
    }

    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService("connectivity");
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected() && info.getState() == State.CONNECTED) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkNetworkAviliable(Context context) {
        if (isConnected(context)) {
            return true;
        }
        return false;
    }

    public static boolean hasMultiAppProfiles(UserManager userManager) {
        for (UserInfo user : userManager.getProfiles(UserHandle.myUserId())) {
            if (user.id == 999) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMultiAppProfiles(UserHandle userHandle) {
        return 999 == userHandle.getIdentifier();
    }

    public static boolean hasMultiApp(Context context, String pkgName) {
        boolean hasMultiApp = false;
        for (ApplicationInfo info : context.getPackageManager().getInstalledApplicationsAsUser(null, 999)) {
            if (pkgName.equals(info.packageName)) {
                hasMultiApp = true;
                break;
            }
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("hasMultiApp ,");
        stringBuilder.append(pkgName);
        stringBuilder.append(" hasMultiApp:");
        stringBuilder.append(hasMultiApp);
        Log.d(str, stringBuilder.toString());
        return hasMultiApp;
    }

    public static void removeMultiApp(Context context, String pkgName) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("removeMultiApp ,");
        stringBuilder.append(pkgName);
        Log.e(str, stringBuilder.toString());
        PackageManager mPackageManager = context.getPackageManager();
        try {
            UserInfo ui = getCorpUserInfo(context);
            if (ui != null) {
                String str2 = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("removeMultiApp-uid:");
                stringBuilder2.append(ui.id);
                Log.d(str2, stringBuilder2.toString());
                if (ui.id == 999) {
                    mPackageManager.deletePackageAsUser(pkgName, null, 0, ui.id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeMultiApp(Context context, String pkgName, int userId) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("removeMultiApp ,");
        stringBuilder.append(pkgName);
        Log.e(str, stringBuilder.toString());
        try {
            context.getPackageManager().deletePackageAsUser(pkgName, null, 0, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void installMultiApp(Context context, String packageName, int userId) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("installMultiApp");
        stringBuilder.append(packageName);
        Log.e(str, stringBuilder.toString());
        try {
            int status = context.getPackageManager().installExistingPackageAsUser(packageName, userId);
            String str2;
            StringBuilder stringBuilder2;
            if (status == -111) {
                Log.e(TAG, "Could not install mobile device management app on managed profile because the user is restricted");
            } else if (status == -3) {
                Log.e(TAG, "Could not install mobile device management app on managed profile because the package could not be found");
            } else if (status != 1) {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Could not install mobile device management app on managed profile. Unknown status: ");
                stringBuilder2.append(status);
                Log.e(str2, stringBuilder2.toString());
            } else {
                str2 = TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("installMultiApp");
                stringBuilder2.append(packageName);
                stringBuilder2.append("success");
                Log.e(str2, stringBuilder2.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "This should not happen.", e);
        }
    }

    public static UserInfo getCorpUserInfo(Context context) {
        UserManager mUserManager = (UserManager) context.getSystemService("user");
        int myUser = mUserManager.getUserHandle();
        for (UserInfo ui : mUserManager.getUsers()) {
            if (ui.isManagedProfile()) {
                UserInfo parent = mUserManager.getProfileParent(ui.id);
                if (parent != null) {
                    if (parent.id == myUser) {
                        return ui;
                    }
                }
            }
        }
        return null;
    }

    public static void stopTethering(Context context) {
        ((ConnectivityManager) context.getSystemService("connectivity")).stopTethering(0);
    }

    public static String resetDeviceNameIfInvalid(Context context) {
        String defaultName = SystemProperties.get("ro.display.series");
        String modified = System.getString(context.getContentResolver(), "oem_oneplus_modified_devicename");
        String opDeviceName = System.getString(context.getContentResolver(), "oem_oneplus_devicename");
        if (modified == null || !TextUtils.isEmpty(opDeviceName)) {
            return opDeviceName;
        }
        System.putString(context.getContentResolver(), "oem_oneplus_devicename", defaultName);
        return defaultName;
    }

    public static String getFileNameNoEx(String filename) {
        if (filename != null && filename.length() > 0) {
            int dot = filename.lastIndexOf(46);
            if (dot > -1 && dot < filename.length()) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String readFile(String path) {
        String value = "0";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            value = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        }
        return value;
    }

    public static boolean isFileExists(String strFile) {
        try {
            if (new File(strFile).exists()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a5  */
    public static boolean isLaboratoryFeatureExist() {
        /*
        r0 = 0;
        r1 = r0;
        r2 = com.oneplus.settings.SettingsBaseApplication.mApplication;	 Catch:{ Exception -> 0x0093 }
        r2 = r2.getPackageManager();	 Catch:{ Exception -> 0x0093 }
        r3 = 128; // 0x80 float:1.794E-43 double:6.32E-322;
        r2 = r2.getInstalledPackages(r3);	 Catch:{ Exception -> 0x0093 }
        r3 = r2.isEmpty();	 Catch:{ Exception -> 0x0093 }
        if (r3 == 0) goto L_0x0015;
    L_0x0014:
        return r0;
    L_0x0015:
        r3 = r2.iterator();	 Catch:{ Exception -> 0x0093 }
    L_0x0019:
        r4 = r3.hasNext();	 Catch:{ Exception -> 0x0093 }
        if (r4 == 0) goto L_0x0092;
    L_0x001f:
        r4 = r3.next();	 Catch:{ Exception -> 0x0093 }
        r4 = (android.content.pm.PackageInfo) r4;	 Catch:{ Exception -> 0x0093 }
        r5 = r4.applicationInfo;	 Catch:{ Exception -> 0x0093 }
        r5 = r5.metaData;	 Catch:{ Exception -> 0x0093 }
        if (r5 == 0) goto L_0x0091;
    L_0x002b:
        r6 = "oneplus_lab_feature";
        r6 = r5.containsKey(r6);	 Catch:{ Exception -> 0x0093 }
        if (r6 == 0) goto L_0x0091;
    L_0x0033:
        r6 = com.oneplus.settings.SettingsBaseApplication.mApplication;	 Catch:{ Exception -> 0x0093 }
        r7 = r4.packageName;	 Catch:{ Exception -> 0x0093 }
        r6 = r6.createPackageContext(r7, r0);	 Catch:{ Exception -> 0x0093 }
        r7 = "oneplus_lab_feature";
        r7 = r5.getString(r7);	 Catch:{ Exception -> 0x0093 }
        r8 = ";";
        r8 = r7.split(r8);	 Catch:{ Exception -> 0x0093 }
        r9 = r1;
        r1 = r0;
    L_0x0049:
        r10 = r8.length;	 Catch:{ Exception -> 0x008e }
        if (r1 >= r10) goto L_0x008c;
    L_0x004c:
        r10 = r8[r1];	 Catch:{ Exception -> 0x008e }
        r11 = ",";
        r10 = r10.split(r11);	 Catch:{ Exception -> 0x008e }
        r11 = r6.getResources();	 Catch:{ Exception -> 0x008e }
        r12 = 2;
        r13 = r10[r12];	 Catch:{ Exception -> 0x008e }
        r14 = "string";
        r15 = r4.packageName;	 Catch:{ Exception -> 0x008e }
        r11 = r11.getIdentifier(r13, r14, r15);	 Catch:{ Exception -> 0x008e }
        if (r11 == 0) goto L_0x006e;
    L_0x0065:
        r12 = r6.getResources();	 Catch:{ Exception -> 0x008e }
        r12 = r12.getString(r11);	 Catch:{ Exception -> 0x008e }
        goto L_0x0070;
    L_0x006e:
        r12 = r10[r12];	 Catch:{ Exception -> 0x008e }
    L_0x0070:
        r13 = android.text.TextUtils.isEmpty(r12);	 Catch:{ Exception -> 0x008e }
        if (r13 == 0) goto L_0x0077;
    L_0x0076:
        goto L_0x0089;
    L_0x0077:
        r13 = com.oneplus.settings.SettingsBaseApplication.mApplication;	 Catch:{ Exception -> 0x008e }
        r13 = isSurportSimNfc(r13);	 Catch:{ Exception -> 0x008e }
        if (r13 != 0) goto L_0x0088;
    L_0x007f:
        r13 = "oneplus_nfc_security_module_key";
        r13 = r13.equals(r12);	 Catch:{ Exception -> 0x008e }
        if (r13 == 0) goto L_0x0088;
    L_0x0087:
        goto L_0x0089;
    L_0x0088:
        r9 = 1;
    L_0x0089:
        r1 = r1 + 1;
        goto L_0x0049;
    L_0x008c:
        r1 = r9;
        goto L_0x0091;
    L_0x008e:
        r0 = move-exception;
        r1 = r9;
        goto L_0x0094;
    L_0x0091:
        goto L_0x0019;
    L_0x0092:
        goto L_0x009e;
    L_0x0093:
        r0 = move-exception;
    L_0x0094:
        r2 = "OPUtils-isLaboratoryFeatureExist";
        r3 = "some unknown error happened.";
        android.util.Log.e(r2, r3);
        r0.printStackTrace();
    L_0x009e:
        r0 = "OPUtils-isLaboratoryFeatureExist:";
        if (r1 == 0) goto L_0x00a5;
    L_0x00a2:
        r2 = "true";
        goto L_0x00a7;
    L_0x00a5:
        r2 = "false";
    L_0x00a7:
        android.util.Log.d(r0, r2);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.utils.OPUtils.isLaboratoryFeatureExist():boolean");
    }

    public static boolean isSupportSocTriState() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_ALERT_SLIDER_RVS");
    }

    public static boolean isSupportVideoEnhancer() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_VIDEO_ENHANCER");
    }

    public static boolean isAllowSendAppTracker(Context mContext) {
        return System.getIntForUser(mContext.getContentResolver(), "oem_join_user_plan_settings", 0, 0) == 1;
    }

    public static boolean isOpBluetoothHeadset() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_BLUETOOTH_HEADSET");
    }

    public static boolean isSupportHearingAid() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_HEARING_AID");
    }

    public static boolean isSupportGestureAnswerCall() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_USE_GESTURE_TO_ANSWER_CALL");
    }

    public static boolean isSupportGesturePullNotificationBar() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_SHOW_NOTIFICATION_BAR_BY_FINGERPRINT_SENSOR");
    }

    public static boolean isSupportScreenCutting() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_CAMERA_NOTCH");
    }

    public static boolean isSupportGameModeNetBoost() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_GAMEMODE_NETBOOST");
    }

    public static boolean isSupportOP2Recovey() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_OP2_RECOVERY");
    }

    public static boolean isSupportNewPlanPowerOffAlarm() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_NEW_PLAN_POWEWR_OFF_ALARM");
    }

    public static boolean isSupportAppSecureRecommd() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_INSTALL_FROM_MARKET");
    }

    public static boolean isSupportScreenDisplayAdaption() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_SCREEN_COMPAT");
    }

    public static boolean isSupportAlwaysOnDisplay() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_ALWAYS_ON_DISPLAY");
    }

    public static int parseColor(String color) {
        if (!(TextUtils.isEmpty(color) || color.contains("#"))) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("#");
            stringBuilder.append(color);
            color = stringBuilder.toString();
        }
        return Color.parseColor(color);
    }

    public static boolean isSupportSleepStandby() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_OPSM");
    }

    public static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0 || codePoint == 9 || codePoint == 10 || codePoint == 13 || ((codePoint >= ' ' && codePoint <= 55295) || ((codePoint >= 57344 && codePoint <= 65533) || (codePoint >= Ascii.MIN && codePoint <= 65535)))) ? false : true;
    }

    public static boolean isSupportCustomFingerprint() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_SUPPORT_CUSTOM_FINGERPRINT");
    }

    public static boolean isSupportGameModePowerSaver() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_GAMEMODE_POWERSAVER");
    }

    public static boolean isSupportCustomBlinkLight() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_CUSTOM_BLINK_LIGHT");
    }

    public static boolean isProductRTTSupport() {
        return OpFeatures.isSupport(new int[]{83});
    }

    public static boolean isSupportQuickLaunch() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_QUICK_LAUNCH");
    }

    public static boolean isSupportNewGesture() {
        return System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "op_gesture_button_launcher", 0) == 1;
    }

    public static boolean isSupportScreenRefreshRate() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_SCREEN_REFRESH_RATE");
    }

    public static boolean isSupportXVibrate() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_X_LINEAR_VIBRATION_MOTOR");
    }

    public static boolean isSupportQuickReply() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_QUICK_REPLY");
    }

    public static boolean isSupportGameAdMode() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_AD_MODE");
    }

    public static boolean isSupportXCamera() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_MOTOR_CONTROL");
    }

    public static boolean isSupportPocketMode() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_ENABLE_POCKETMODE_SWITCH");
    }

    public static void setLightNavigationBar(Window mWindow, int theme) {
        if (mWindow != null) {
            int vis = mWindow.getDecorView().getSystemUiVisibility();
            if (theme == 0) {
                vis = (vis | 16) | 8192;
            } else if (theme == 2) {
                vis |= 16;
            } else {
                vis &= -17;
            }
            mWindow.getDecorView().setSystemUiVisibility(vis);
        }
    }

    public static boolean isSupportEarphoneMode() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_EARPHONE_MODE");
    }

    public static boolean isSM8150Products() {
        return Build.BOARD.equals("msmnile");
    }

    public static boolean isSupportAppsDisplayInFullscreen() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_APPS_DISPLAY_IN_FULLSCREEN");
    }

    public static boolean isSupportUsePackageInstallPermissionToVerifyCta() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_CTA_USE_PACKAGEINSTALLER_PERMISSION");
    }

    public static SpannableStringBuilder parseLink(String linkFrontContent, String linkUrl, String linkTitle, String endContext) {
        String urlContent = new StringBuilder();
        urlContent.append(linkFrontContent);
        urlContent.append("<a href=\"");
        urlContent.append(linkUrl);
        urlContent.append("\">");
        urlContent.append(linkTitle);
        urlContent.append("</a>");
        urlContent.append(endContext);
        Spannable urlSpannable = (Spannable) Html.fromHtml(urlContent.toString());
        int i = 0;
        URLSpan[] urls = (URLSpan[]) urlSpannable.getSpans(0, urlSpannable.length(), URLSpan.class);
        SpannableStringBuilder style = new SpannableStringBuilder(urlSpannable);
        style.clearSpans();
        int length = urls.length;
        while (i < length) {
            URLSpan url = urls[i];
            style.setSpan(new URLSpan(url.getURL()), urlSpannable.getSpanStart(url), urlSpannable.getSpanEnd(url), 33);
            i++;
        }
        return style;
    }

    public static boolean isGuaProject() {
        String[] guacamoleproject = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_guacamole_project);
        int i = 0;
        while (i < guacamoleproject.length) {
            if (guacamoleproject[i] != null && guacamoleproject[i].equalsIgnoreCase(Build.MODEL)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static boolean is18857Project() {
        if (Build.MODEL.equalsIgnoreCase(SettingsBaseApplication.mApplication.getResources().getString(R.string.oneplus_oneplus_model_18857_for_cn)) || Build.MODEL.equalsIgnoreCase(SettingsBaseApplication.mApplication.getResources().getString(R.string.oneplus_oneplus_model_18857_for_in)) || Build.MODEL.equalsIgnoreCase(SettingsBaseApplication.mApplication.getResources().getString(R.string.oneplus_oneplus_model_18857_for_eu)) || Build.MODEL.equalsIgnoreCase(SettingsBaseApplication.mApplication.getResources().getString(R.string.oneplus_oneplus_model_18857_for_us))) {
            return true;
        }
        return false;
    }

    public static boolean isSupportZVibrationMotor() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_Z_VIBRATION_MOTOR");
    }

    public static boolean isSupportSmartBoost() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_SMART_BOOST");
    }

    public static boolean isSupportReadingModeInterpolater() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_READING_MODE_INTERPOLATER");
    }

    public static boolean isnoDisplaySarValueProject() {
        String[] nodisplaysarvalueproject = SettingsBaseApplication.mApplication.getResources().getStringArray(R.array.oneplus_no_display_sar_value_project);
        int i = 0;
        while (i < nodisplaysarvalueproject.length) {
            if (nodisplaysarvalueproject[i] != null && nodisplaysarvalueproject[i].equalsIgnoreCase(Build.MODEL)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static void sendAnalytics(String eventname, String label, String value) {
        AppTrackerHelper.getInstance().putAnalytics(eventname, label, value);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("eventname : ");
        stringBuilder.append(eventname);
        stringBuilder.append(" label : ");
        stringBuilder.append(label);
        stringBuilder.append(" value : ");
        stringBuilder.append(value);
        Log.d("AppTracker Analytics", stringBuilder.toString());
    }

    public static SpannableStringBuilder parseLinkLaunchAction(String linkFrontContent, String linkTitle, String endContext, ClickableSpan actionSpan) {
        String urlContent = new StringBuilder();
        urlContent.append(linkFrontContent);
        urlContent.append("<a href=\"\">");
        urlContent.append(linkTitle);
        urlContent.append("</a>");
        urlContent.append(endContext);
        Spannable urlSpannable = (Spannable) Html.fromHtml(urlContent.toString());
        int i = 0;
        URLSpan[] urls = (URLSpan[]) urlSpannable.getSpans(0, urlSpannable.length(), URLSpan.class);
        SpannableStringBuilder style = new SpannableStringBuilder(urlSpannable);
        style.clearSpans();
        int length = urls.length;
        while (i < length) {
            URLSpan url = urls[i];
            style.setSpan(actionSpan, urlSpannable.getSpanStart(url), urlSpannable.getSpanEnd(url), 33);
            i++;
        }
        return style;
    }

    public static boolean isEUVersion() {
        return "true".equals(SystemProperties.get("ro.build.eu", "false"));
    }

    public static boolean isSupportUss() {
        return ReflectUtil.isFeatureSupported("OP_FEATURE_USS");
    }
}
