package com.oneplus.settings.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.settings.utils.OPUtils;
import java.util.Calendar;

public class AccessibilityTimerShutdownSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String ACC_TIMERDOWN_TIMESETTINGS_KEY = "accessibility_timer_startup_device_settings";
    private static final String ACC_TIMERUP_TIMESETTINGS_KEY = "accessibility_timer_startup_device_settings";
    private static final String ACC_TIMER_SHUTDOWN_KEY = "accessibility_timer_shutdown_device";
    private static final String ACC_TIMER_STARTUP_KEY = "accessibility_timer_startup_device";
    private AlarmManager am;
    private Calendar c;
    private Intent intent;
    private SwitchPreference mShutdownPreference;
    private SwitchPreference mStartupPreference;
    private Preference mTimeDownSettingsPreference;
    private Preference mTimeUpSettingsPreference;
    private PendingIntent pIntent;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_accessibility_timer_shutdown_settings);
    }

    public void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        this.mStartupPreference = (SwitchPreference) findPreference(ACC_TIMER_STARTUP_KEY);
        this.mStartupPreference.setOnPreferenceClickListener(this);
        boolean z = false;
        this.mStartupPreference.setChecked(System.getInt(getActivity().getContentResolver(), "oem_startup_timer", 1) != 0);
        this.mShutdownPreference = (SwitchPreference) findPreference(ACC_TIMER_SHUTDOWN_KEY);
        this.mShutdownPreference.setOnPreferenceClickListener(this);
        SwitchPreference switchPreference = this.mShutdownPreference;
        if (System.getInt(getActivity().getContentResolver(), "oem_shutdown_timer", 1) != 0) {
            z = true;
        }
        switchPreference.setChecked(z);
        this.mTimeUpSettingsPreference = findPreference("accessibility_timer_startup_device_settings");
        this.mTimeUpSettingsPreference.setOnPreferenceClickListener(this);
        this.mTimeDownSettingsPreference = findPreference("accessibility_timer_startup_device_settings");
        this.mTimeDownSettingsPreference.setOnPreferenceClickListener(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(ACC_TIMER_SHUTDOWN_KEY)) {
            System.getInt(getActivity().getContentResolver(), "oem_shutdown_timer", this.mStartupPreference.isChecked());
        } else if (preference.getKey().equals(ACC_TIMER_STARTUP_KEY)) {
            System.getInt(getActivity().getContentResolver(), "oem_startup_timer", this.mStartupPreference.isChecked());
        } else if (preference.getKey().equals("accessibility_timer_startup_device_settings")) {
            this.c.setTimeInMillis(System.currentTimeMillis());
            new TimePickerDialog(getActivity(), new OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    AccessibilityTimerShutdownSettings.this.c.setTimeInMillis(System.currentTimeMillis());
                    AccessibilityTimerShutdownSettings.this.c.set(11, hourOfDay);
                    AccessibilityTimerShutdownSettings.this.c.set(12, minute);
                    AccessibilityTimerShutdownSettings.this.c.set(13, 0);
                    AccessibilityTimerShutdownSettings.this.c.set(14, 0);
                    AccessibilityTimerShutdownSettings.this.intent = new Intent("com.android.settings.action.REQUEST_POWER_ON");
                    AccessibilityTimerShutdownSettings.this.pIntent = PendingIntent.getBroadcast(AccessibilityTimerShutdownSettings.this.getActivity(), 0, AccessibilityTimerShutdownSettings.this.intent, 0);
                    AccessibilityTimerShutdownSettings.this.am = (AlarmManager) AccessibilityTimerShutdownSettings.this.getSystemService(NotificationCompat.CATEGORY_ALARM);
                    AccessibilityTimerShutdownSettings.this.am.set(0, AccessibilityTimerShutdownSettings.this.c.getTimeInMillis(), AccessibilityTimerShutdownSettings.this.pIntent);
                    AccessibilityTimerShutdownSettings.this.am.setRepeating(0, AccessibilityTimerShutdownSettings.this.c.getTimeInMillis(), 10000, AccessibilityTimerShutdownSettings.this.pIntent);
                    Preference access$500 = AccessibilityTimerShutdownSettings.this.mTimeUpSettingsPreference;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("设置的闹钟时间为:");
                    stringBuilder.append(hourOfDay);
                    stringBuilder.append(":");
                    stringBuilder.append(minute);
                    access$500.setSummary(stringBuilder.toString());
                }
            }, this.c.get(11), this.c.get(12), true).show();
            return true;
        } else if (preference.getKey().equals("accessibility_timer_startup_device_settings")) {
            this.c.setTimeInMillis(System.currentTimeMillis());
            new TimePickerDialog(getActivity(), new OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    AccessibilityTimerShutdownSettings.this.c.setTimeInMillis(System.currentTimeMillis());
                    AccessibilityTimerShutdownSettings.this.c.set(11, hourOfDay);
                    AccessibilityTimerShutdownSettings.this.c.set(12, minute);
                    AccessibilityTimerShutdownSettings.this.c.set(13, 0);
                    AccessibilityTimerShutdownSettings.this.c.set(14, 0);
                    AccessibilityTimerShutdownSettings.this.intent = new Intent("com.android.settings.action.REQUEST_POWER_OFF");
                    AccessibilityTimerShutdownSettings.this.intent.addFlags(285212672);
                    AccessibilityTimerShutdownSettings.this.pIntent = PendingIntent.getBroadcast(AccessibilityTimerShutdownSettings.this.getActivity(), 0, AccessibilityTimerShutdownSettings.this.intent, 0);
                    AccessibilityTimerShutdownSettings.this.am = (AlarmManager) AccessibilityTimerShutdownSettings.this.getSystemService(NotificationCompat.CATEGORY_ALARM);
                    AccessibilityTimerShutdownSettings.this.am.set(0, AccessibilityTimerShutdownSettings.this.c.getTimeInMillis(), AccessibilityTimerShutdownSettings.this.pIntent);
                    AccessibilityTimerShutdownSettings.this.am.setRepeating(0, AccessibilityTimerShutdownSettings.this.c.getTimeInMillis(), 10000, AccessibilityTimerShutdownSettings.this.pIntent);
                    Preference access$500 = AccessibilityTimerShutdownSettings.this.mTimeUpSettingsPreference;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("设置的闹钟时间为:");
                    stringBuilder.append(hourOfDay);
                    stringBuilder.append(":");
                    stringBuilder.append(minute);
                    access$500.setSummary(stringBuilder.toString());
                }
            }, this.c.get(11), this.c.get(12), true).show();
            return true;
        }
        return false;
    }

    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        return false;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
