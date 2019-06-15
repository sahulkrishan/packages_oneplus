package com.oneplus.settings.system;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;

public class OPRamBoostSettings extends SettingsPreferenceFragment {
    private static final String KEY_RAMBOOST_INSTRUCTIONS = "op_ramboost_instructions";
    public static final String KEY_SETTINGS_PROVIDER = "op_smartboost_enable";
    private final String EVENT_RAMBOOST = "RAM_Boost";
    private final String KEY_RAMBOOST_SWITCH = "op_ramboost_switch";
    private Context mContext;
    private RamBoostLottieAnimPreference mLottieAnimPreference;
    private SwitchPreference mSwitchPreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.op_ramboost_settings);
        PreferenceScreen root = getPreferenceScreen();
        this.mSwitchPreference = (SwitchPreference) root.findPreference("op_ramboost_switch");
        this.mLottieAnimPreference = (RamBoostLottieAnimPreference) root.findPreference(KEY_RAMBOOST_INSTRUCTIONS);
        String jsonFile = "op_ramboost_anim_white.json";
        if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
            jsonFile = "op_ramboost_anim_dark.json";
        }
        this.mLottieAnimPreference.setAnimFile(jsonFile);
        this.mSwitchPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object objValue) {
                Boolean value = (Boolean) objValue;
                OPRamBoostSettings.setRamBoostState(OPRamBoostSettings.this.mContext, value.booleanValue());
                OPUtils.sendAnalytics("ramboost", NotificationCompat.CATEGORY_STATUS, value.booleanValue() ? "1" : "0");
                return true;
            }
        });
        refreshUI();
        super.onViewCreated(view, savedInstanceState);
    }

    private void refreshUI() {
        this.mSwitchPreference.setChecked(getRamBoostState(this.mContext));
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public static boolean getRamBoostState(Context ctx) {
        return System.getIntForUser(ctx.getContentResolver(), KEY_SETTINGS_PROVIDER, 0, -2) == 1;
    }

    public static void setRamBoostState(Context ctx, boolean value) {
        System.putIntForUser(ctx.getContentResolver(), KEY_SETTINGS_PROVIDER, value, -2);
    }

    public static void sendDefaultAppTracker() {
        OPUtils.sendAppTracker(KEY_RAMBOOST_INSTRUCTIONS, getRamBoostState(SettingsBaseApplication.mApplication));
    }
}
