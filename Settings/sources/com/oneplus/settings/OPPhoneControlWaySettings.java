package com.oneplus.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.Window;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.Indexable;
import com.android.settings.ui.RadioButtonPreference;
import com.android.settings.ui.RadioButtonPreference.OnClickListener;
import com.oneplus.settings.ui.OPPhoneControlWayCategory;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPPhoneControlWaySettings extends SettingsPreferenceFragment implements OnClickListener, OnPreferenceChangeListener, Indexable {
    private static final String KEY_ALWAYS_SHOW_NAVIGATION_BAR = "always_show_navigation_bar";
    private static final String KEY_BUTTONS_ENABLE_ON_SCREEN_NAVKEYS = "buttons_enable_on_screen_navkeys";
    private static final String KEY_BUTTONS_SWAP_NAVKEYS = "buttons_swap_navkeys";
    private static final String KEY_GESTURE_NAVIGATION_BAR = "gesture_navigation_bar";
    private static final String KEY_HIDE_NAVIGATION_BAR = "hide_navigation_bar";
    private static final String KEY_OP_NAVIGATION_BAR_TYPE = "op_navigation_bar_type";
    private static final String KEY_PHONE_CONTROL_WAY = "phone_control_way";
    private static final int REQUEST_CODE_FOR_GESTURE_GUIDE = 100;
    private static final String TAG = "OPPhoneControlWaySettings";
    private static final int TYPE_ALWAYS_SHOW_NAVIGATION_BAR = 1;
    private static final int TYPE_GESTURE_NAVIGATION_BAR = 3;
    private static final int TYPE_HIDE_NAVIGATION_BAR = 2;
    private RadioButtonPreference mAlwaysShowNavigationBar;
    private Context mContext;
    private RadioButtonPreference mGestureNavigationBar;
    private Handler mHandler = new Handler();
    private RadioButtonPreference mLeftButtonNavigationBar;
    private OPPhoneControlWayCategory mOPPhoneControlWayCategory;
    private Window mWindow;

    public enum KeyLockMode {
        NORMAL,
        POWER,
        POWER_HOME,
        HOME,
        FOOT,
        BACK_SWITCH,
        BASE
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_phone_control_way_settings);
        this.mContext = SettingsBaseApplication.mApplication;
        this.mWindow = getActivity().getWindow();
        initPref();
    }

    private void initPref() {
        this.mAlwaysShowNavigationBar = (RadioButtonPreference) findPreference(KEY_ALWAYS_SHOW_NAVIGATION_BAR);
        this.mLeftButtonNavigationBar = (RadioButtonPreference) findPreference(KEY_HIDE_NAVIGATION_BAR);
        this.mGestureNavigationBar = (RadioButtonPreference) findPreference(KEY_GESTURE_NAVIGATION_BAR);
        this.mAlwaysShowNavigationBar.setOnClickListener(this);
        this.mLeftButtonNavigationBar.setOnClickListener(this);
        this.mGestureNavigationBar.setOnClickListener(this);
        this.mOPPhoneControlWayCategory = (OPPhoneControlWayCategory) findPreference(KEY_PHONE_CONTROL_WAY);
    }

    public void onResume() {
        updateUI();
        super.onResume();
        if (this.mOPPhoneControlWayCategory != null) {
            this.mOPPhoneControlWayCategory.startAnim();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mOPPhoneControlWayCategory != null) {
            this.mOPPhoneControlWayCategory.stopAnim();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mOPPhoneControlWayCategory != null) {
            this.mOPPhoneControlWayCategory.releaseAnim();
        }
    }

    private void updateUI() {
        int value = System.getInt(this.mContext.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 1);
        boolean z = false;
        this.mAlwaysShowNavigationBar.setChecked(value == 1);
        this.mLeftButtonNavigationBar.setChecked(value == 2);
        RadioButtonPreference radioButtonPreference = this.mGestureNavigationBar;
        if (value == 3) {
            z = true;
        }
        radioButtonPreference.setChecked(z);
        this.mAlwaysShowNavigationBar.setEnabled(true);
        this.mLeftButtonNavigationBar.setEnabled(true);
        this.mGestureNavigationBar.setEnabled(true);
    }

    public void onRadioButtonClicked(RadioButtonPreference pref) {
        if (pref == this.mAlwaysShowNavigationBar) {
            this.mAlwaysShowNavigationBar.setChecked(true);
            this.mLeftButtonNavigationBar.setChecked(false);
            this.mGestureNavigationBar.setChecked(false);
            showNavbar();
            delayHideNavkey();
            setNavigationType(1);
        } else if (pref == this.mLeftButtonNavigationBar) {
            this.mAlwaysShowNavigationBar.setChecked(false);
            this.mLeftButtonNavigationBar.setChecked(true);
            this.mGestureNavigationBar.setChecked(false);
            showNavbar();
            delayHideNavkey();
            setNavigationType(2);
        } else if (pref == this.mGestureNavigationBar) {
            this.mAlwaysShowNavigationBar.setChecked(false);
            this.mLeftButtonNavigationBar.setChecked(false);
            this.mGestureNavigationBar.setChecked(true);
            System.putInt(this.mContext.getContentResolver(), "lock_to_app_enabled", 0);
            delayHideNavkey();
            setNavigationType(3);
        }
    }

    private void showNavbar() {
    }

    private void hideNavbar() {
    }

    private void delayHideNavkey() {
        this.mAlwaysShowNavigationBar.setEnabled(false);
        this.mLeftButtonNavigationBar.setEnabled(false);
        this.mGestureNavigationBar.setEnabled(false);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                OPPhoneControlWaySettings.this.mAlwaysShowNavigationBar.setEnabled(true);
                OPPhoneControlWaySettings.this.mLeftButtonNavigationBar.setEnabled(true);
                OPPhoneControlWaySettings.this.mGestureNavigationBar.setEnabled(true);
                OPPhoneControlWaySettings.this.updateUI();
            }
        }, 1000);
    }

    private void setNavigationType(int type) {
        System.putInt(this.mContext.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, type);
        this.mOPPhoneControlWayCategory.setViewType(type);
        OPUtils.sendAppTracker("op_fullscreen_gesture_enabled", type);
    }

    private int getNavigationType() {
        return System.getInt(this.mContext.getContentResolver(), KEY_OP_NAVIGATION_BAR_TYPE, 1);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    private static List<String> getNonVisibleKeys() {
        List<String> result = new ArrayList();
        result.add(KEY_BUTTONS_ENABLE_ON_SCREEN_NAVKEYS);
        result.add(KEY_BUTTONS_SWAP_NAVKEYS);
        return result;
    }
}
