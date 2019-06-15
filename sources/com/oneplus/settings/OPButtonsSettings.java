package com.oneplus.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.view.Window;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.Indexable;
import com.oneplus.settings.quickpay.QuickPaySettings;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPButtonsSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    private static final String BACKLIGHT_PREF = "pre_navbar_button_backlight";
    public static final String GSM_PACKAGE = "com.google.android.googlequicksearchbox";
    private static final String KEY_BACK_DOUBLE_TAP = "hardware_keys_back_double_tap";
    private static final String KEY_BACK_LONG_PRESS = "hardware_keys_back_long_press";
    private static final String KEY_BUTTONS_BRIGHTNESS = "buttons_brightness";
    private static final String KEY_BUTTONS_ENABLE_ON_SCREEN_NAVKEYS = "buttons_enable_on_screen_navkeys";
    private static final String KEY_BUTTONS_FORCE_HOME = "buttons_force_home";
    private static final String KEY_BUTTONS_SWAP_NAVKEYS = "buttons_swap_navkeys";
    private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE = "camera_double_tap_power_gesture";
    private static final String KEY_CATEGORY_BACK = "back_key";
    private static final String KEY_CATEGORY_HOME = "home_key";
    private static final String KEY_CATEGORY_MENU = "menu_key";
    private static final String KEY_HIDE_NAVKEYS = "hide_navkeys";
    private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final int KEY_LOCK_MODE_FOOT = 4;
    private static final int KEY_LOCK_MODE_HOME = 3;
    private static final int KEY_LOCK_MODE_POWER_HOME = 2;
    private static final String KEY_MENU_DOUBLE_TAP = "hardware_keys_menu_double_tap";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_NAVIGATION_BAR_TYPE = "key_navigation_bar_type";
    public static final String QUICKPAY_VALUE = "11";
    private static final int REFRESH_PREPERENCE = 1;
    private static final int REQUEST_CODE_FOR_GESTURE_GUIDE = 100;
    private static final String TAG = "SystemSettings";
    private ListPreference mBackDoubleTapAction;
    private ListPreference mBackLongPressAction;
    private SwitchPreference mCameraDoubleTapPowerGesturePreference;
    private SwitchPreference mDisableNavKeysBrightness;
    private SwitchPreference mEnableOnScreenNavkeys;
    private SwitchPreference mForceHomeButtonEnabled;
    private Handler mHandler;
    private SwitchPreference mHideNavkeys;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mHomeLongPressAction;
    private ListPreference mMenuDoubleTapAction;
    private ListPreference mMenuLongPressAction;
    private final SettingsObserver mSettingsObserver = new SettingsObserver(this.mHandler);
    private SwitchPreference mSwapNavkeys;
    private Window mWindow;

    public static class Helper {
        public static void updateSettings(Context context, boolean onScreenNavKeysEnabled) {
            updateSettings(context, onScreenNavKeysEnabled, true, false, false);
        }

        public static void updateSettings(Context context, boolean onScreenNavKeysEnabled, boolean handleLights, boolean skipOnScreenNavKeys, boolean lightsOnly) {
            if (!skipOnScreenNavKeys) {
                System.putInt(context.getContentResolver(), OPConstants.BUTTONS_SHOW_ON_SCREEN_NAVKEYS, onScreenNavKeysEnabled);
            }
            if (handleLights) {
                if (!lightsOnly) {
                    setHWKeysState(context, onScreenNavKeysEnabled);
                }
                setHWButtonsLightsState(context, onScreenNavKeysEnabled ^ 1, true);
            }
        }

        public static void setHWButtonsLightsState(Context context, boolean enabled, boolean store) {
            int defaultBrightness = context.getResources().getInteger(R.integer.config_buttonBrightnessSettingDefault);
            int i = 0;
            if (store) {
                SharedPreferences prefs = context.getSharedPreferences(OPButtonsSettings.BACKLIGHT_PREF, 0);
                Editor editor = prefs.edit();
                if (enabled) {
                    i = prefs.getInt(OPButtonsSettings.BACKLIGHT_PREF, -1);
                    if (i != -1) {
                        System.putInt(context.getContentResolver(), "buttons_brightness", i);
                        editor.remove(OPButtonsSettings.BACKLIGHT_PREF);
                    }
                } else {
                    int currentBrightness = System.getInt(context.getContentResolver(), "buttons_brightness", defaultBrightness);
                    if (!prefs.contains(OPButtonsSettings.BACKLIGHT_PREF)) {
                        editor.putInt(OPButtonsSettings.BACKLIGHT_PREF, currentBrightness);
                    }
                    System.putInt(context.getContentResolver(), "buttons_brightness", 0);
                }
                editor.commit();
                return;
            }
            ContentResolver contentResolver = context.getContentResolver();
            String str = "buttons_brightness";
            if (enabled) {
                i = defaultBrightness;
            }
            System.putInt(contentResolver, str, i);
        }

        public static void setSWKeysState(Context context) {
            boolean z = false;
            if (System.getInt(context.getContentResolver(), OPConstants.BUTTONS_SHOW_ON_SCREEN_NAVKEYS, 0) != 0) {
                z = true;
            }
            updateSettings(context, z);
        }

        private static void setHWKeysState(Context context, boolean disabled) {
            setHWKeysState(context, disabled, false);
        }

        private static void setHWKeysState(Context context, boolean disabled, boolean homeOff) {
            ContentResolver contentResolver = context.getContentResolver();
            String str = "oem_acc_key_lock_mode";
            int i = disabled ? homeOff ? 4 : 5 : 0;
            System.putInt(contentResolver, str, i);
        }
    }

    public enum KeyLockMode {
        NORMAL,
        POWER,
        POWER_HOME,
        HOME,
        FOOT,
        BACK_SWITCH,
        BASE
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri OEM_EYECARE_ENABLE_URI = System.getUriFor("oem_acc_key_lock_mode");

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.OEM_EYECARE_ENABLE_URI.equals(uri)) {
                OPButtonsSettings.this.mHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }

        public void setListening(boolean listening) {
            ContentResolver cr = OPButtonsSettings.this.getContentResolver();
            if (listening) {
                cr.registerContentObserver(this.OEM_EYECARE_ENABLE_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mWindow = getActivity().getWindow();
        addPreferencesFromResource(R.xml.op_buttons_settings);
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    OPButtonsSettings.this.loadPreferenceScreen();
                }
            }
        };
        this.mEnableOnScreenNavkeys = (SwitchPreference) findPreference(KEY_BUTTONS_ENABLE_ON_SCREEN_NAVKEYS);
        this.mEnableOnScreenNavkeys.setOnPreferenceChangeListener(this);
        this.mSwapNavkeys = (SwitchPreference) findPreference(KEY_BUTTONS_SWAP_NAVKEYS);
        this.mSwapNavkeys.setOnPreferenceChangeListener(this);
        this.mDisableNavKeysBrightness = (SwitchPreference) findPreference("buttons_brightness");
        this.mDisableNavKeysBrightness.setOnPreferenceChangeListener(this);
        this.mForceHomeButtonEnabled = (SwitchPreference) findPreference(KEY_BUTTONS_FORCE_HOME);
        this.mForceHomeButtonEnabled.setOnPreferenceChangeListener(this);
        removePreference(KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);
        this.mHideNavkeys = (SwitchPreference) findPreference(KEY_HIDE_NAVKEYS);
        this.mHideNavkeys.setOnPreferenceChangeListener(this);
    }

    private void initPrefs() {
        initListViewPrefs();
        if (!checkGMS(getContext())) {
            initListViewPrefsnogms();
        } else if (QuickPaySettings.canShowQuickPay(getContext())) {
            if (isSupportHardwareKeys()) {
                this.mHomeLongPressAction.setEntries((int) R.array.hardware_keys_action_entries_quickpay);
                this.mHomeLongPressAction.setEntryValues((int) R.array.hardware_keys_action_values_quickpay);
                return;
            }
            this.mHomeLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries_quickpay);
            this.mHomeLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_quickpay);
        } else if (isSupportHardwareKeys()) {
            this.mHomeLongPressAction.setEntries((int) R.array.hardware_keys_action_entries);
            this.mHomeLongPressAction.setEntryValues((int) R.array.hardware_keys_action_values);
        } else {
            this.mHomeLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries);
            this.mHomeLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values);
        }
    }

    public static boolean checkGMS(Context ctx) {
        try {
            ApplicationInfo info = ctx.getPackageManager().getApplicationInfo("com.google.android.googlequicksearchbox", 8192);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(17956909);
    }

    public void onResume() {
        super.onResume();
        initPrefs();
        this.mSettingsObserver.setListening(true);
        loadPreferenceScreen();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && System.getInt(getContentResolver(), OPConstants.OP_GESTURE_GUIDE_COMPLETED, 0) == 0) {
            System.putInt(getContentResolver(), OPConstants.OP_GESTURE_BUTTON_ENABLED, 0);
            OPUtils.sendAppTracker("op_fullscreen_gesture_enabled", false);
            delayEnableHideNavkey();
        }
    }

    public void onPause() {
        super.onPause();
        this.mSettingsObserver.setListening(false);
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleChange(Object pref, Object newValue, String setting) {
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            String value = (String) newValue;
            listPref.setSummary(listPref.getEntries()[listPref.findIndexOfValue(value)]);
            System.putInt(getContentResolver(), setting, Integer.valueOf(value).intValue());
        } else if (pref instanceof SwitchPreference) {
            boolean state = false;
            int i = 0;
            if (newValue instanceof Boolean) {
                state = ((Boolean) newValue).booleanValue();
            } else if (newValue instanceof String) {
                state = Integer.valueOf((String) newValue).intValue() != 0;
            }
            ContentResolver contentResolver = getContentResolver();
            if (state) {
                i = 1;
            }
            System.putInt(contentResolver, setting, i);
        }
    }

    private boolean isSupportHardwareKeys() {
        return SettingsBaseApplication.mApplication.getResources().getBoolean(17957029) ^ 1;
    }

    private void initListViewPrefsnogms() {
        if (QuickPaySettings.canShowQuickPay(getContext())) {
            if (isSupportHardwareKeys()) {
                this.mHomeLongPressAction.setEntries((int) R.array.hardware_keys_action_entries_nogms_quickpay);
                this.mHomeLongPressAction.setEntryValues((int) R.array.hardware_keys_action_values_nogms_quickpay);
            } else {
                this.mHomeLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries_nogms_quickpay);
                this.mHomeLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_nogms_quickpay);
            }
        } else if (isSupportHardwareKeys()) {
            this.mHomeLongPressAction.setEntries((int) R.array.hardware_keys_action_entries_nogms);
            this.mHomeLongPressAction.setEntryValues((int) R.array.hardware_keys_action_values_nogms);
        } else {
            this.mHomeLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries_nogms);
            this.mHomeLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_nogms);
        }
        if (isSupportHardwareKeys()) {
            this.mHomeDoubleTapAction.setEntries((int) R.array.hardware_keys_action_entries_nogms);
            this.mHomeDoubleTapAction.setEntryValues((int) R.array.hardware_keys_action_values_nogms);
            this.mMenuLongPressAction.setEntries((int) R.array.hardware_keys_action_entries_nogms);
            this.mMenuLongPressAction.setEntryValues((int) R.array.hardware_keys_action_values_nogms);
            this.mMenuDoubleTapAction.setEntries((int) R.array.hardware_keys_action_entries_nogms);
            this.mMenuDoubleTapAction.setEntryValues((int) R.array.hardware_keys_action_values_nogms);
            this.mBackLongPressAction.setEntries((int) R.array.hardware_keys_action_entries_nogms);
            this.mBackLongPressAction.setEntryValues((int) R.array.hardware_keys_action_values_nogms);
            this.mBackDoubleTapAction.setEntries((int) R.array.hardware_keys_action_entries_nogms);
            this.mBackDoubleTapAction.setEntryValues((int) R.array.hardware_keys_action_values_nogms);
            return;
        }
        this.mHomeDoubleTapAction.setEntries((int) R.array.navigation_bar_keys_action_entries_nogms);
        this.mHomeDoubleTapAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_nogms);
        this.mMenuLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries_nogms);
        this.mMenuLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_nogms);
        this.mMenuDoubleTapAction.setEntries((int) R.array.navigation_bar_keys_action_entries_nogms);
        this.mMenuDoubleTapAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_nogms);
        this.mBackLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries_nogms);
        this.mBackLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_nogms);
        this.mBackDoubleTapAction.setEntries((int) R.array.navigation_bar_keys_action_entries_nogms);
        this.mBackDoubleTapAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_nogms);
        this.mBackLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries_nogms);
        this.mBackLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values_nogms);
    }

    private void initListViewPrefs() {
        ContentResolver resolver = SettingsBaseApplication.mApplication.getContentResolver();
        this.mHomeLongPressAction = initActionList(KEY_HOME_LONG_PRESS, System.getInt(resolver, OPConstants.KEY_HOME_LONG_PRESS_ACTION, getActivity().getResources().getInteger(17694802)));
        this.mHomeDoubleTapAction = initActionList(KEY_HOME_DOUBLE_TAP, System.getInt(resolver, OPConstants.KEY_HOME_DOUBLE_TAP_ACTION, getActivity().getResources().getInteger(17694780)));
        this.mMenuLongPressAction = initActionList(KEY_MENU_LONG_PRESS, System.getInt(resolver, OPConstants.KEY_APP_SWITCH_LONG_PRESS_ACTION, getActivity().getResources().getInteger(84410375)));
        this.mMenuDoubleTapAction = initActionList(KEY_MENU_DOUBLE_TAP, System.getInt(resolver, OPConstants.KEY_APP_SWITCH_DOUBLE_TAP_ACTION, getActivity().getResources().getInteger(84410371)));
        this.mBackLongPressAction = initActionList(KEY_BACK_LONG_PRESS, System.getInt(resolver, OPConstants.KEY_BACK_LONG_PRESS_ACTION, getActivity().getResources().getInteger(84410376)));
        this.mBackDoubleTapAction = initActionList(KEY_BACK_DOUBLE_TAP, System.getInt(resolver, OPConstants.KEY_BACK_DOUBLE_TAP_ACTION, getActivity().getResources().getInteger(84410372)));
        if (!isSupportHardwareKeys()) {
            if (this.mHomeLongPressAction != null) {
                this.mHomeLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries);
                this.mHomeLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values);
            }
            if (this.mHomeDoubleTapAction != null) {
                this.mHomeDoubleTapAction.setEntries((int) R.array.navigation_bar_keys_action_entries);
                this.mHomeDoubleTapAction.setEntryValues((int) R.array.navigation_bar_keys_action_values);
            }
            if (this.mMenuLongPressAction != null) {
                this.mMenuLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries);
                this.mMenuLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values);
            }
            if (this.mMenuDoubleTapAction != null) {
                this.mMenuDoubleTapAction.setEntries((int) R.array.navigation_bar_keys_action_entries);
                this.mMenuDoubleTapAction.setEntryValues((int) R.array.navigation_bar_keys_action_values);
            }
            if (this.mBackLongPressAction != null) {
                this.mBackLongPressAction.setEntries((int) R.array.navigation_bar_keys_action_entries);
                this.mBackLongPressAction.setEntryValues((int) R.array.navigation_bar_keys_action_values);
            }
            if (this.mBackDoubleTapAction != null) {
                this.mBackDoubleTapAction.setEntries((int) R.array.navigation_bar_keys_action_entries);
                this.mBackDoubleTapAction.setEntryValues((int) R.array.navigation_bar_keys_action_values);
            }
        }
    }

    private void loadPreferenceScreen() {
        if (SettingsBaseApplication.mApplication != null) {
            ContentResolver resolver = SettingsBaseApplication.mApplication.getContentResolver();
            boolean z = true;
            boolean z2 = false;
            boolean buttonsBrightnessEnabled = System.getInt(resolver, "buttons_brightness", SettingsBaseApplication.mApplication.getResources().getInteger(R.integer.config_buttonBrightnessSettingDefault)) != 0;
            boolean onScreenNavKeysEnabled = System.getInt(resolver, OPConstants.BUTTONS_SHOW_ON_SCREEN_NAVKEYS, 0) != 0;
            boolean forceHomeEnabled = System.getInt(resolver, OPConstants.BUTTONS_FORCE_HOME_ENABLED, 0) != 0;
            this.mSwapNavkeys.setChecked(System.getInt(getContentResolver(), "oem_acc_key_define", 0) != 0);
            this.mDisableNavKeysBrightness.setChecked(buttonsBrightnessEnabled);
            this.mEnableOnScreenNavkeys.setChecked(onScreenNavKeysEnabled);
            this.mEnableOnScreenNavkeys.setEnabled(System.getInt(resolver, "oem_acc_key_lock_mode", KeyLockMode.NORMAL.ordinal()) != KeyLockMode.FOOT.ordinal());
            this.mForceHomeButtonEnabled.setChecked(forceHomeEnabled);
            PreferenceScreen prefScreen = getPreferenceScreen();
            PreferenceCategory homeCategory = (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_HOME);
            PreferenceCategory menuCategory = (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_MENU);
            PreferenceCategory backCategory = (PreferenceCategory) prefScreen.findPreference(KEY_CATEGORY_BACK);
            if (this.mDisableNavKeysBrightness != null) {
                this.mDisableNavKeysBrightness.setEnabled(!onScreenNavKeysEnabled);
            }
            if (isSupportHardwareKeys()) {
                if (this.mForceHomeButtonEnabled != null) {
                    this.mForceHomeButtonEnabled.setEnabled(onScreenNavKeysEnabled);
                }
                if (homeCategory == null || !forceHomeEnabled) {
                }
                removePreference(KEY_HIDE_NAVKEYS);
            } else {
                removePreference("buttons_brightness");
                removePreference(KEY_BUTTONS_ENABLE_ON_SCREEN_NAVKEYS);
                removePreference(KEY_BUTTONS_FORCE_HOME);
                int value = System.getInt(getContentResolver(), "op_navigation_bar_type", 1);
                SwitchPreference switchPreference = this.mHideNavkeys;
                if (value == 0) {
                    z = false;
                }
                switchPreference.setChecked(z);
                z = value == 3;
                if (this.mSwapNavkeys != null) {
                    switchPreference = this.mSwapNavkeys;
                    if (!z) {
                        z2 = true;
                    }
                    switchPreference.setEnabled(z2);
                }
                if (homeCategory != null) {
                    homeCategory.setEnabled(!z);
                }
                if (menuCategory != null) {
                    menuCategory.setEnabled(!z);
                }
                if (backCategory != null) {
                    backCategory.setEnabled(!z);
                }
            }
            if (OPUtils.isSurportNavigationBarOnly(SettingsBaseApplication.mApplication)) {
                removePreference(KEY_BUTTONS_ENABLE_ON_SCREEN_NAVKEYS);
                removePreference(KEY_HIDE_NAVKEYS);
            } else {
                removePreference(KEY_NAVIGATION_BAR_TYPE);
            }
            if (this.mCameraDoubleTapPowerGesturePreference != null) {
                boolean z3 = false;
                int value2 = Secure.getInt(getContentResolver(), "camera_double_tap_power_gesture_disabled", 0);
                SwitchPreference switchPreference2 = this.mCameraDoubleTapPowerGesturePreference;
                if (value2 == 0) {
                    z3 = true;
                }
                switchPreference2.setChecked(z3);
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean state = false;
        if (newValue instanceof Boolean) {
            state = ((Boolean) newValue).booleanValue();
        } else if (newValue instanceof String) {
            state = Integer.valueOf((String) newValue).intValue() != 0;
        }
        if (preference == this.mSwapNavkeys) {
            handleChange(this.mSwapNavkeys, newValue, "oem_acc_key_define");
            return true;
        } else if (preference == this.mHomeLongPressAction) {
            handleChange(this.mHomeLongPressAction, newValue, OPConstants.KEY_HOME_LONG_PRESS_ACTION);
            if ("11".equals((String) newValue)) {
                QuickPaySettings.gotoQuickPaySettingsPage(getActivity());
            }
            return true;
        } else if (preference == this.mHomeDoubleTapAction) {
            handleChange(this.mHomeDoubleTapAction, newValue, OPConstants.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == this.mMenuLongPressAction) {
            handleChange(this.mMenuLongPressAction, newValue, OPConstants.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference == this.mMenuDoubleTapAction) {
            handleChange(this.mMenuDoubleTapAction, newValue, OPConstants.KEY_APP_SWITCH_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == this.mBackLongPressAction) {
            handleChange(this.mBackLongPressAction, newValue, OPConstants.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == this.mBackDoubleTapAction) {
            handleChange(this.mBackDoubleTapAction, newValue, OPConstants.KEY_BACK_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == this.mDisableNavKeysBrightness) {
            Helper.setHWButtonsLightsState(getActivity(), state, false);
            loadPreferenceScreen();
            return true;
        } else if (preference == this.mEnableOnScreenNavkeys) {
            Helper.updateSettings(getActivity(), state);
            OPUtils.sendAppTracker(KEY_BUTTONS_ENABLE_ON_SCREEN_NAVKEYS, state);
            return true;
        } else if (preference == this.mForceHomeButtonEnabled) {
            handleChange(this.mForceHomeButtonEnabled, newValue, OPConstants.BUTTONS_FORCE_HOME_ENABLED);
            loadPreferenceScreen();
            return true;
        } else if (preference == this.mCameraDoubleTapPowerGesturePreference) {
            Secure.putInt(getContentResolver(), "camera_double_tap_power_gesture_disabled", ((Boolean) newValue).booleanValue() ^ 1);
            return true;
        } else if (preference != this.mHideNavkeys) {
            return false;
        } else {
            if (System.getInt(getContentResolver(), OPConstants.OP_GESTURE_GUIDE_COMPLETED, 0) == 0) {
                startActivityForResult(new Intent("oneplus.intent.action.ONEPLUS_FULLSCREEN_GESTURE_GUIDE"), 100);
            } else {
                boolean value = ((Boolean) newValue).booleanValue();
                System.putInt(getContentResolver(), OPConstants.OP_GESTURE_BUTTON_ENABLED, value);
                OPUtils.sendAppTracker("op_fullscreen_gesture_enabled", value);
                delayEnableHideNavkey();
            }
            return true;
        }
    }

    private void delayEnableHideNavkey() {
        this.mHideNavkeys.setEnabled(false);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                OPButtonsSettings.this.mHideNavkeys.setEnabled(true);
                OPButtonsSettings.this.loadPreferenceScreen();
            }
        }, 1000);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mEnableOnScreenNavkeys) {
            OPUtils.setLightNavigationBar(this.mWindow, OPUtils.getThemeMode(getContentResolver()));
            this.mEnableOnScreenNavkeys.setEnabled(false);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    OPButtonsSettings.this.mEnableOnScreenNavkeys.setEnabled(true);
                }
            }, 1000);
        }
        return super.onPreferenceTreeClick(preference);
    }

    private static List<String> getNonVisibleKeys() {
        List<String> result = new ArrayList();
        result.add(KEY_BUTTONS_ENABLE_ON_SCREEN_NAVKEYS);
        result.add(KEY_BUTTONS_SWAP_NAVKEYS);
        result.add("buttons_brightness");
        result.add(KEY_BUTTONS_FORCE_HOME);
        result.add(KEY_HOME_LONG_PRESS);
        result.add(KEY_HOME_DOUBLE_TAP);
        result.add(KEY_MENU_LONG_PRESS);
        result.add(KEY_MENU_DOUBLE_TAP);
        result.add(KEY_BACK_LONG_PRESS);
        result.add(KEY_BACK_DOUBLE_TAP);
        result.add(KEY_CATEGORY_HOME);
        result.add(KEY_CATEGORY_MENU);
        result.add(KEY_CATEGORY_BACK);
        result.add(BACKLIGHT_PREF);
        result.add(KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);
        return result;
    }
}
