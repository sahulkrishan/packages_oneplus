package com.android.settings.notification;

import android.app.AlertDialog.Builder;
import android.app.AutomaticZenRule;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ZenModeScheduleRuleSettings extends ZenModeRuleSettingsBase {
    public static final String ACTION = "android.settings.ZEN_MODE_SCHEDULE_RULE_SETTINGS";
    private static final String KEY_DAYS = "days";
    private static final String KEY_END_TIME = "end_time";
    private static final String KEY_EXIT_AT_ALARM = "exit_at_alarm";
    private static final String KEY_START_TIME = "start_time";
    private final SimpleDateFormat mDayFormat = new SimpleDateFormat("EEE");
    private Preference mDays;
    private TimePickerPreference mEnd;
    private SwitchPreference mExitAtAlarm;
    private ScheduleInfo mSchedule;
    private TimePickerPreference mStart;

    private static class TimePickerPreference extends Preference {
        private Callback mCallback;
        private final Context mContext;
        private int mHourOfDay;
        private int mMinute;
        private int mSummaryFormat;

        public interface Callback {
            boolean onSetTime(int i, int i2);
        }

        public static class TimePickerFragment extends InstrumentedDialogFragment implements OnTimeSetListener {
            public TimePickerPreference pref;

            public int getMetricsCategory() {
                return 556;
            }

            public Dialog onCreateDialog(Bundle savedInstanceState) {
                boolean usePref = this.pref != null && this.pref.mHourOfDay >= 0 && this.pref.mMinute >= 0;
                Calendar c = Calendar.getInstance();
                return new TimePickerDialog(getActivity(), this, usePref ? this.pref.mHourOfDay : c.get(11), usePref ? this.pref.mMinute : c.get(12), DateFormat.is24HourFormat(getActivity()));
            }

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (this.pref != null) {
                    this.pref.setTime(hourOfDay, minute);
                }
            }
        }

        public TimePickerPreference(Context context, final FragmentManager mgr) {
            super(context);
            this.mContext = context;
            setPersistent(false);
            setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    TimePickerFragment frag = new TimePickerFragment();
                    frag.pref = TimePickerPreference.this;
                    frag.show(mgr, TimePickerPreference.class.getName());
                    return true;
                }
            });
        }

        public void setCallback(Callback callback) {
            this.mCallback = callback;
        }

        public void setSummaryFormat(int resId) {
            this.mSummaryFormat = resId;
            updateSummary();
        }

        public void setTime(int hourOfDay, int minute) {
            if (this.mCallback == null || this.mCallback.onSetTime(hourOfDay, minute)) {
                this.mHourOfDay = hourOfDay;
                this.mMinute = minute;
                updateSummary();
            }
        }

        private void updateSummary() {
            Calendar c = Calendar.getInstance();
            c.set(11, this.mHourOfDay);
            c.set(12, this.mMinute);
            String time = DateFormat.getTimeFormat(this.mContext).format(c.getTime());
            if (this.mSummaryFormat != 0) {
                time = this.mContext.getResources().getString(this.mSummaryFormat, new Object[]{time});
            }
            setSummary((CharSequence) time);
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean setRule(AutomaticZenRule rule) {
        ScheduleInfo tryParseScheduleConditionId;
        if (rule != null) {
            tryParseScheduleConditionId = ZenModeConfig.tryParseScheduleConditionId(rule.getConditionId());
        } else {
            tryParseScheduleConditionId = null;
        }
        this.mSchedule = tryParseScheduleConditionId;
        return this.mSchedule != null;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.zen_mode_schedule_rule_settings;
    }

    /* Access modifiers changed, original: protected */
    public void onCreateInternal() {
        PreferenceScreen root = getPreferenceScreen();
        this.mDays = root.findPreference(KEY_DAYS);
        this.mDays.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ZenModeScheduleRuleSettings.this.showDaysDialog();
                return true;
            }
        });
        FragmentManager mgr = getFragmentManager();
        this.mStart = new TimePickerPreference(getPrefContext(), mgr);
        this.mStart.setKey(KEY_START_TIME);
        this.mStart.setTitle((int) R.string.zen_mode_start_time);
        this.mStart.setCallback(new Callback() {
            public boolean onSetTime(int hour, int minute) {
                if (ZenModeScheduleRuleSettings.this.mDisableListeners) {
                    return true;
                }
                if (!ZenModeConfig.isValidHour(hour) || !ZenModeConfig.isValidMinute(minute)) {
                    return false;
                }
                if (hour == ZenModeScheduleRuleSettings.this.mSchedule.startHour && minute == ZenModeScheduleRuleSettings.this.mSchedule.startMinute) {
                    return true;
                }
                if (ZenModeRuleSettingsBase.DEBUG) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onPrefChange start h=");
                    stringBuilder.append(hour);
                    stringBuilder.append(" m=");
                    stringBuilder.append(minute);
                    Log.d("ZenModeSettings", stringBuilder.toString());
                }
                ZenModeScheduleRuleSettings.this.mSchedule.startHour = hour;
                ZenModeScheduleRuleSettings.this.mSchedule.startMinute = minute;
                ZenModeScheduleRuleSettings.this.updateRule(ZenModeConfig.toScheduleConditionId(ZenModeScheduleRuleSettings.this.mSchedule));
                return true;
            }
        });
        root.addPreference(this.mStart);
        this.mStart.setDependency(this.mDays.getKey());
        this.mEnd = new TimePickerPreference(getPrefContext(), mgr);
        this.mEnd.setKey(KEY_END_TIME);
        this.mEnd.setTitle((int) R.string.zen_mode_end_time);
        this.mEnd.setCallback(new Callback() {
            public boolean onSetTime(int hour, int minute) {
                if (ZenModeScheduleRuleSettings.this.mDisableListeners) {
                    return true;
                }
                if (!ZenModeConfig.isValidHour(hour) || !ZenModeConfig.isValidMinute(minute)) {
                    return false;
                }
                if (hour == ZenModeScheduleRuleSettings.this.mSchedule.endHour && minute == ZenModeScheduleRuleSettings.this.mSchedule.endMinute) {
                    return true;
                }
                if (ZenModeRuleSettingsBase.DEBUG) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("onPrefChange end h=");
                    stringBuilder.append(hour);
                    stringBuilder.append(" m=");
                    stringBuilder.append(minute);
                    Log.d("ZenModeSettings", stringBuilder.toString());
                }
                ZenModeScheduleRuleSettings.this.mSchedule.endHour = hour;
                ZenModeScheduleRuleSettings.this.mSchedule.endMinute = minute;
                ZenModeScheduleRuleSettings.this.updateRule(ZenModeConfig.toScheduleConditionId(ZenModeScheduleRuleSettings.this.mSchedule));
                return true;
            }
        });
        root.addPreference(this.mEnd);
        this.mEnd.setDependency(this.mDays.getKey());
        this.mExitAtAlarm = (SwitchPreference) root.findPreference(KEY_EXIT_AT_ALARM);
        this.mExitAtAlarm.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                ZenModeScheduleRuleSettings.this.mSchedule.exitAtAlarm = ((Boolean) o).booleanValue();
                ZenModeScheduleRuleSettings.this.updateRule(ZenModeConfig.toScheduleConditionId(ZenModeScheduleRuleSettings.this.mSchedule));
                return true;
            }
        });
    }

    private void updateDays() {
        int[] days = this.mSchedule.days;
        if (days != null && days.length > 0) {
            CharSequence sb = new StringBuilder();
            Calendar c = Calendar.getInstance();
            int[] daysOfWeek = ZenModeScheduleDaysSelection.getDaysOfWeekForLocale(c);
            for (int day : daysOfWeek) {
                int j = 0;
                while (j < days.length) {
                    if (day == days[j]) {
                        c.set(7, day);
                        if (sb.length() > 0) {
                            sb.append(this.mContext.getString(R.string.summary_divider_text));
                        }
                        sb.append(this.mDayFormat.format(c.getTime()));
                    } else {
                        j++;
                    }
                }
            }
            if (sb.length() > 0) {
                this.mDays.setSummary(sb);
                this.mDays.notifyDependencyChange(false);
                return;
            }
        }
        this.mDays.setSummary((int) R.string.zen_mode_schedule_rule_days_none);
        this.mDays.notifyDependencyChange(true);
    }

    private void updateEndSummary() {
        int summaryFormat = 0;
        if ((this.mSchedule.startHour * 60) + this.mSchedule.startMinute >= (60 * this.mSchedule.endHour) + this.mSchedule.endMinute) {
            summaryFormat = R.string.zen_mode_end_time_next_day_summary_format;
        }
        this.mEnd.setSummaryFormat(summaryFormat);
    }

    /* Access modifiers changed, original: protected */
    public void updateControlsInternal() {
        updateDays();
        this.mStart.setTime(this.mSchedule.startHour, this.mSchedule.startMinute);
        this.mEnd.setTime(this.mSchedule.endHour, this.mSchedule.endMinute);
        this.mExitAtAlarm.setChecked(this.mSchedule.exitAtAlarm);
        updateEndSummary();
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        this.mHeader = new ZenAutomaticRuleHeaderPreferenceController(context, this, getLifecycle());
        this.mSwitch = new ZenAutomaticRuleSwitchPreferenceController(context, this, getLifecycle());
        controllers.add(this.mHeader);
        controllers.add(this.mSwitch);
        return controllers;
    }

    public int getMetricsCategory() {
        return Const.CODE_C1_SPA;
    }

    private void showDaysDialog() {
        new Builder(this.mContext).setTitle(R.string.zen_mode_schedule_rule_days).setView(new ZenModeScheduleDaysSelection(this.mContext, this.mSchedule.days) {
            /* Access modifiers changed, original: protected */
            public void onChanged(int[] days) {
                if (!ZenModeScheduleRuleSettings.this.mDisableListeners && !Arrays.equals(days, ZenModeScheduleRuleSettings.this.mSchedule.days)) {
                    if (ZenModeRuleSettingsBase.DEBUG) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("days.onChanged days=");
                        stringBuilder.append(Arrays.asList(new int[][]{days}));
                        Log.d("ZenModeSettings", stringBuilder.toString());
                    }
                    ZenModeScheduleRuleSettings.this.mSchedule.days = days;
                    ZenModeScheduleRuleSettings.this.updateRule(ZenModeConfig.toScheduleConditionId(ZenModeScheduleRuleSettings.this.mSchedule));
                }
            }
        }).setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                ZenModeScheduleRuleSettings.this.updateDays();
            }
        }).setPositiveButton(R.string.done_button, null).show();
    }
}
