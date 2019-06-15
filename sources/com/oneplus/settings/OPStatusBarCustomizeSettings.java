package com.oneplus.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPStatusBarCustomizeSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable, OnPreferenceClickListener {
    private static final int BATTERY_BAR_STYLE = 0;
    private static final int BATTERY_CIRCLE_STYLE = 1;
    private static final int BATTERY_HIDDEN_STYLE = 2;
    private static final String KEY_BATTERY_PERCENT = "enable_show_statusbar";
    private static final String KEY_BATTERY_STYLE = "battery_style";
    private static final String KEY_CLOCK = "clock";
    private static final String KEY_STATUSBAR_ICON_MANGER = "status_bar_icon_manager";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new StatusBarCustomizeIndexProvider();
    private static final String SHOW_POWER_PERCENT_IN_STATUSBAR_TITLE = "show_power_percent_in_statusbar_title";
    private static final String TAG = "OPStatusBarCustomizeSettings";
    private ListPreference mBatteryStylePreference;
    private ListPreference mClockPreference;
    private Context mContext;
    private SwitchPreference mShowBatteryPercentPreference;
    private Preference mStatusBarIconMangerPreference;

    private static class StatusBarCustomizeIndexProvider extends BaseSearchIndexProvider {
        boolean mIsPrimary;

        public StatusBarCustomizeIndexProvider() {
            this.mIsPrimary = UserHandle.myUserId() == 0;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            if (!this.mIsPrimary) {
                return result;
            }
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.op_statusbar_customize_settings;
            result.add(sir);
            return result;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_statusbar_customize_settings);
        this.mContext = getActivity();
        this.mBatteryStylePreference = (ListPreference) findPreference(KEY_BATTERY_STYLE);
        boolean z = false;
        int selectedStyle = System.getInt(this.mContext.getContentResolver(), "status_bar_battery_style", 0);
        this.mBatteryStylePreference.setValue(String.valueOf(selectedStyle));
        updateBatteryStylePreferenceDescription(selectedStyle);
        this.mBatteryStylePreference.setOnPreferenceChangeListener(this);
        this.mShowBatteryPercentPreference = (SwitchPreference) findPreference(KEY_BATTERY_PERCENT);
        this.mClockPreference = (ListPreference) findPreference(KEY_CLOCK);
        this.mStatusBarIconMangerPreference = findPreference(KEY_STATUSBAR_ICON_MANGER);
        this.mStatusBarIconMangerPreference.setOnPreferenceClickListener(this);
        int showPercetn = System.getInt(this.mContext.getContentResolver(), "status_bar_show_battery_percent", 0);
        SwitchPreference switchPreference = this.mShowBatteryPercentPreference;
        if (showPercetn == 1) {
            z = true;
        }
        switchPreference.setChecked(z);
        this.mShowBatteryPercentPreference.setOnPreferenceChangeListener(this);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(SHOW_POWER_PERCENT_IN_STATUSBAR_TITLE) && !intent.getBooleanExtra(SHOW_POWER_PERCENT_IN_STATUSBAR_TITLE, true)) {
            getPreferenceScreen().removePreference(this.mShowBatteryPercentPreference);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        if (KEY_BATTERY_STYLE.equals(key)) {
            int batteryStyle = Integer.parseInt((String) objValue);
            System.putInt(this.mContext.getContentResolver(), "status_bar_battery_style", batteryStyle);
            updateBatteryStylePreferenceDescription(batteryStyle);
            return true;
        } else if (!KEY_BATTERY_PERCENT.equals(key)) {
            return true;
        } else {
            System.putInt(this.mContext.getContentResolver(), "status_bar_show_battery_percent", ((Boolean) objValue).booleanValue());
            return true;
        }
    }

    private void updateBatteryStylePreferenceDescription(int batteryStyle) {
        if (this.mBatteryStylePreference != null) {
            if (batteryStyle >= this.mBatteryStylePreference.getEntries().length) {
                batteryStyle = this.mBatteryStylePreference.getEntries().length - 1;
            }
            this.mBatteryStylePreference.setSummary(this.mBatteryStylePreference.getEntries()[batteryStyle]);
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!KEY_STATUSBAR_ICON_MANGER.equals(preference.getKey())) {
            return false;
        }
        Intent intent = new Intent(this.mContext, OPStatusBarCustomizeIconSettings.class);
        startFragment(this, OPStatusBarCustomizeIconSettings.class.getName(), 0, 0, null);
        return true;
    }
}
