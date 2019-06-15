package com.oneplus.settings.better;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPHapticFeedback extends SettingsPreferenceFragment {
    private static final String APPTRACKER_EVENT = "game_mode_haptic";
    public static final String KEY_SETTINGS_PROVIDER = "op_game_mode_vibrate_feedback";
    private final String KEY_HAPTIC_FEEDBACK_NO_APP = "op_haptic_feedback_no_app";
    private final String KEY_HAPTIC_FEEDBACK_SUPPORT_CATEGORY = "op_haptic_feedback_support_category";
    private final String KEY_HAPTIC_FEEDBACK_SWITCH = "op_haptic_feedback_switch";
    private Context mContext;
    private List<String> mHapticFeedbackAppList = new ArrayList();
    public List<PackageInfo> mHapticFeedbackInstalledApps = new ArrayList();
    private PreferenceCategory mHapticFeedbackSupportCategory;
    private PackageManager mPackageManager;
    private SwitchPreference mSwitchPreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContext = getActivity();
        loadConfig();
        this.mPackageManager = this.mContext.getPackageManager();
        for (PackageInfo p : this.mPackageManager.getInstalledPackages(0)) {
            if (this.mHapticFeedbackAppList.contains(p.packageName)) {
                this.mHapticFeedbackInstalledApps.add(p);
            }
        }
    }

    private void loadConfig() {
        this.mHapticFeedbackAppList.clear();
        String[] configArray = this.mContext.getResources().getStringArray(84017166);
        if (configArray != null && configArray.length > 0) {
            int i = 0;
            while (i < configArray.length) {
                try {
                    String[] values = configArray[i].split(";");
                    if (values != null && values.length == 3) {
                        this.mHapticFeedbackAppList.add(values[0]);
                    }
                    i++;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        addPreferencesFromResource(R.xml.op_haptic_feedback);
        PreferenceScreen root = getPreferenceScreen();
        this.mHapticFeedbackSupportCategory = (PreferenceCategory) root.findPreference("op_haptic_feedback_support_category");
        this.mSwitchPreference = (SwitchPreference) root.findPreference("op_haptic_feedback_switch");
        this.mSwitchPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object objValue) {
                OPHapticFeedback.setHapticFeedbackState(OPHapticFeedback.this.mContext, ((Boolean) objValue).booleanValue());
                return true;
            }
        });
        refreshUI();
        super.onViewCreated(view, savedInstanceState);
    }

    private void refreshUI() {
        this.mSwitchPreference.setChecked(getHapticFeedbackState(this.mContext));
        Preference noAppPreference = this.mHapticFeedbackSupportCategory.findPreference("op_haptic_feedback_no_app");
        if (!this.mHapticFeedbackInstalledApps.isEmpty()) {
            if (noAppPreference != null) {
                noAppPreference.setVisible(false);
            }
            for (PackageInfo info : this.mHapticFeedbackInstalledApps) {
                Preference preference = new Preference(this.mContext);
                preference.setLayoutResource(R.layout.op_preference_material);
                preference.setIconSpaceReserved(true);
                preference.setSelectable(false);
                preference.setKey(info.packageName);
                preference.setIcon(info.applicationInfo.loadIcon(this.mPackageManager));
                preference.setTitle(info.applicationInfo.loadLabel(this.mPackageManager).toString());
                this.mHapticFeedbackSupportCategory.addPreference(preference);
            }
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public static boolean getHapticFeedbackState(Context ctx) {
        return System.getIntForUser(ctx.getContentResolver(), KEY_SETTINGS_PROVIDER, 0, -2) == 1;
    }

    public static void setHapticFeedbackState(Context ctx, boolean value) {
        System.putIntForUser(ctx.getContentResolver(), KEY_SETTINGS_PROVIDER, value, -2);
        OPUtils.sendAppTracker(APPTRACKER_EVENT, (int) value);
    }

    public static void sendDefaultAppTracker() {
        OPUtils.sendAppTracker(APPTRACKER_EVENT, getHapticFeedbackState(SettingsBaseApplication.mApplication));
    }
}
