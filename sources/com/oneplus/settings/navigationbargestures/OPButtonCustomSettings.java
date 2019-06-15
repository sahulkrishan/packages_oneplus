package com.oneplus.settings.navigationbargestures;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.quickpay.QuickPaySettings;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import com.oneplus.settings.utils.XmlParseUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPButtonCustomSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    public static final String GSM_PACKAGE = "com.google.android.googlequicksearchbox";
    private static final String KEY_BACK_DOUBLE_TAP = "hardware_keys_back_double_tap";
    private static final String KEY_BACK_LONG_PRESS = "hardware_keys_back_long_press";
    private static final String KEY_BUTTONS_SWAP_NAVKEYS = "buttons_swap_navkeys";
    private static final String KEY_HIDE_NAVKEYS = "hide_navkeys";
    private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String KEY_MENU_DOUBLE_TAP = "hardware_keys_menu_double_tap";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_OP_NAVIGATION_BAR_TYPE = "op_navigation_bar_type";
    public static final String QUICKPAY_VALUE = "11";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_button_custom_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = new ArrayList();
            if (!OPNavigationBarGesturesSettings.isCustomSettingsEnable(context) || OPButtonCustomSettings.isSupportHardwareKeys()) {
                result.addAll(XmlParseUtils.parsePreferenceKeyFromResource(R.xml.op_button_custom_settings, context));
            }
            return result;
        }
    };
    private static final int TYPE_BACK_HOME = 2;
    private ListPreference mBackDoubleTapAction;
    private ListPreference mBackLongPressAction;
    private Context mContext;
    private SwitchPreference mHideNavkeys;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mHomeLongPressAction;
    private ListPreference mMenuDoubleTapAction;
    private ListPreference mMenuLongPressAction;
    private SwitchPreference mSwapNavkeys;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_button_custom_settings);
        this.mContext = SettingsBaseApplication.mApplication;
        this.mSwapNavkeys = (SwitchPreference) findPreference(KEY_BUTTONS_SWAP_NAVKEYS);
        this.mSwapNavkeys.setOnPreferenceChangeListener(this);
        this.mHideNavkeys = (SwitchPreference) findPreference(KEY_HIDE_NAVKEYS);
        this.mHideNavkeys.setOnPreferenceChangeListener(this);
    }

    private boolean getBackHomeEnabled() {
        return Secure.getInt(this.mContext.getContentResolver(), "swipe_up_to_switch_apps_enabled", this.mContext.getResources().getBoolean(17957058)) != 0;
    }

    public void onResume() {
        super.onResume();
        initPrefs();
        boolean z = true;
        this.mSwapNavkeys.setChecked(System.getInt(getContentResolver(), "oem_acc_key_define", 0) != 0);
        int value = System.getInt(this.mContext.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 1);
        if (getBackHomeEnabled()) {
            this.mHideNavkeys.setEnabled(false);
            this.mMenuLongPressAction.setEnabled(false);
            this.mMenuDoubleTapAction.setEnabled(false);
        } else {
            this.mHideNavkeys.setEnabled(true);
            this.mMenuLongPressAction.setEnabled(true);
            this.mMenuDoubleTapAction.setEnabled(true);
        }
        value = System.getInt(this.mContext.getContentResolver(), OPConstants.OP_GESTURE_BUTTON_ENABLED, 0);
        if (this.mHideNavkeys != null) {
            SwitchPreference switchPreference = this.mHideNavkeys;
            if (value == 0) {
                z = false;
            }
            switchPreference.setChecked(z);
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

    private static boolean isSupportHardwareKeys() {
        return SettingsBaseApplication.mApplication.getResources().getBoolean(17957029) ^ 1;
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

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean state = false;
        int i = 0;
        if (newValue instanceof Boolean) {
            state = ((Boolean) newValue).booleanValue();
        } else if (newValue instanceof String) {
            state = Integer.valueOf((String) newValue).intValue() != 0;
        }
        if (preference == this.mSwapNavkeys) {
            handleChange(this.mSwapNavkeys, newValue, "oem_acc_key_define");
            String str = OPConstants.SWAP_BUTTON;
            if (state) {
                i = 1;
            }
            OPUtils.sendAppTracker(str, i);
            return true;
        } else if (preference == this.mHideNavkeys) {
            handleChange(this.mHideNavkeys, newValue, OPConstants.OP_GESTURE_BUTTON_ENABLED);
            if (state && System.getInt(getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 0) == 1) {
                OPUtils.sendAppTracker(OPConstants.NAV_GESTURES_SETTINGS, 4);
            }
            return true;
        } else {
            if (newValue instanceof Boolean) {
                state = ((Boolean) newValue).booleanValue();
            } else if (newValue instanceof String) {
                state = Integer.valueOf((String) newValue).intValue() != 0;
            }
            if (preference == this.mHomeLongPressAction) {
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
            } else if (preference != this.mBackDoubleTapAction) {
                return false;
            } else {
                handleChange(this.mBackDoubleTapAction, newValue, OPConstants.KEY_BACK_DOUBLE_TAP_ACTION);
                return true;
            }
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
