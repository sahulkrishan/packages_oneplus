package com.oneplus.settings.timer.timepower;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.view.ContextThemeWrapper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.oneplus.lib.app.TimePickerDialog;
import com.oneplus.lib.app.TimePickerDialog.OnTimeSetListener;
import com.oneplus.lib.widget.TimePicker;
import com.oneplus.settings.utils.OPUtils;
import java.lang.reflect.Array;

public class TimepowerSettingsFragment extends SettingsPreferenceFragment implements OnTimeSetListener, OnPreferenceChangeListener {
    private static final String ACTION_CANCEL_POWEROFF_ALARM = "org.codeaurora.poweroffalarm.action.CANCEL_ALARM";
    private static final String ACTION_SET_POWEROFF_ALARM = "org.codeaurora.poweroffalarm.action.SET_ALARM";
    private static final String EXTRA_DISPLAY_TIME = "display_time";
    private static final String EXTRA_HOUR = "hour";
    private static final String EXTRA_IS_24HOUR = "24hour";
    private static final String EXTRA_MINUTE = "minute";
    private static final String EXTRA_POWER_STATE = "power_state";
    private static final String EXTRA_POWER_TYPE = "power_type";
    private static final int ITEM_COUNT = 2;
    private static final String KEY_POWER_OFF_SETTINGS = "oneplus_power_off_settings";
    private static final String KEY_POWER_ON_SETTINGS = "oneplus_power_on_settings";
    private static final String POWER_OFF_ALARM_PACKAGE = "com.qualcomm.qti.poweroffalarm";
    private static final int POWER_OFF_TYPE = 1;
    private static final int POWER_ON_TYPE = 0;
    private static final String PREFERENCE_POWER_OFF_SETTINGS = "power_off_settings";
    private static final String PREFERENCE_POWER_OFF_STATE = "power_off_switch";
    private static final String PREFERENCE_POWER_ON_SETTINGS = "power_on_settings";
    private static final String PREFERENCE_POWER_ON_STATE = "power_on_switch";
    private static final int REQUEST_CODE_POWER_OFF = 1;
    private static final int REQUEST_CODE_POWER_ON = 0;
    private static final String TAG = "TimepowerSettingsFragment";
    private static final String TIME = "time";
    private static boolean misCheckedPoweroff = false;
    private static boolean misCheckedPoweron = false;
    private int mCode;
    private boolean mDlgVisible = false;
    private TimepowerPreference mPowerOffPref;
    private Preference mPowerOffPreference;
    private SwitchPreference mPowerOffStatePref;
    private TimepowerPreference mPowerOnPref;
    private Preference mPowerOnPreference;
    private SwitchPreference mPowerOnStatePref;
    private boolean mPowerState;
    private boolean[][] mStateArray = ((boolean[][]) Array.newInstance(boolean.class, new int[]{2, 2}));
    private int[][] mTimeArray = ((int[][]) Array.newInstance(int.class, new int[]{2, 2}));
    private TimePicker mTimePicker;
    OnDismissListener onDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
            TimepowerSettingsFragment.this.mDlgVisible = false;
        }
    };

    private class TimeSetDialogListener implements OnClickListener, OnCancelListener {
        public void onClick(DialogInterface dialog, int whichButton) {
            if (whichButton == -1) {
            }
            onCancel(dialog);
        }

        public void onCancel(DialogInterface dialog) {
            Log.i(TimepowerSettingsFragment.TAG, "=========TimeSetDialogListener CANCEL=======");
            dialog.cancel();
            TimepowerSettingsFragment.this.mDlgVisible = false;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_time_power_preference);
        init();
    }

    private void init() {
        readData();
        boolean powerOnState = this.mStateArray[0][1];
        CharSequence powerOnTime = formatTime(this.mTimeArray[0][0], this.mTimeArray[0][1]);
        boolean powerOffState = this.mStateArray[1][1];
        CharSequence powerOffTime = formatTime(this.mTimeArray[1][0], this.mTimeArray[1][1]);
        this.mPowerOnStatePref = (SwitchPreference) findPreference(PREFERENCE_POWER_ON_STATE);
        this.mPowerOnStatePref.setChecked(powerOnState);
        this.mPowerOnStatePref.setOnPreferenceChangeListener(this);
        this.mPowerOffStatePref = (SwitchPreference) findPreference(PREFERENCE_POWER_OFF_STATE);
        this.mPowerOffStatePref.setChecked(powerOffState);
        this.mPowerOffStatePref.setOnPreferenceChangeListener(this);
        this.mPowerOnPref = (TimepowerPreference) findPreference(PREFERENCE_POWER_ON_SETTINGS);
        this.mPowerOnPref.setTitle(powerOnTime);
        this.mPowerOnPref.setViewClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!TimepowerSettingsFragment.this.mDlgVisible) {
                    TimepowerSettingsFragment.this.startDialogForResult(TimepowerSettingsFragment.this.getTimeSettingsIntent(0), 0);
                }
            }
        });
        this.mPowerOnPreference = findPreference(KEY_POWER_ON_SETTINGS);
        this.mPowerOnPreference.setSummary(powerOnTime);
        this.mPowerOnPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                if (TimepowerSettingsFragment.this.mDlgVisible) {
                    return true;
                }
                TimepowerSettingsFragment.this.startDialogForResult(TimepowerSettingsFragment.this.getTimeSettingsIntent(0), 0);
                return false;
            }
        });
        this.mPowerOffPref = (TimepowerPreference) findPreference(PREFERENCE_POWER_OFF_SETTINGS);
        this.mPowerOffPref.setTitle(powerOffTime);
        this.mPowerOffPref.setViewClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!TimepowerSettingsFragment.this.mDlgVisible) {
                    TimepowerSettingsFragment.this.startDialogForResult(TimepowerSettingsFragment.this.getTimeSettingsIntent(1), 1);
                }
            }
        });
        this.mPowerOffPreference = findPreference(KEY_POWER_OFF_SETTINGS);
        this.mPowerOffPreference.setSummary(powerOffTime);
        this.mPowerOffPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                if (TimepowerSettingsFragment.this.mDlgVisible) {
                    return true;
                }
                TimepowerSettingsFragment.this.startDialogForResult(TimepowerSettingsFragment.this.getTimeSettingsIntent(1), 1);
                return false;
            }
        });
        removePreference(PREFERENCE_POWER_ON_SETTINGS);
        removePreference(PREFERENCE_POWER_OFF_SETTINGS);
    }

    private void readData() {
        String config = System.getString(getContentResolver(), "def_timepower_config");
        if (config != null) {
            int i = 0;
            int j = 0;
            while (i <= 6) {
                String tmp = config.substring(i, i + 6);
                this.mTimeArray[j][0] = Integer.parseInt(tmp.substring(0, 2));
                this.mTimeArray[j][1] = Integer.parseInt(tmp.substring(2, 4));
                this.mStateArray[j][0] = intToBool(Integer.parseInt(tmp.substring(4, 5)));
                this.mStateArray[j][1] = intToBool(Integer.parseInt(tmp.substring(5, 6)));
                i += 6;
                j++;
            }
        }
    }

    private String formatTime(int hourOfDay, int minute) {
        String result;
        if (is24Hour()) {
            result = new StringBuilder();
            result.append(String.format("%1$02d", new Object[]{Integer.valueOf(hourOfDay)}));
            result.append(":");
            result.append(String.format("%1$02d", new Object[]{Integer.valueOf(minute)}));
            return result.toString();
        }
        result = getString(R.string.android_am);
        if (hourOfDay >= 12) {
            result = getString(R.string.android_pm);
            if (hourOfDay > 12) {
                hourOfDay -= 12;
            }
        } else if (hourOfDay == 0) {
            hourOfDay = 12;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(result);
        stringBuilder.append(String.format("%1$02d", new Object[]{Integer.valueOf(hourOfDay)}));
        stringBuilder.append(":");
        stringBuilder.append(String.format("%1$02d", new Object[]{Integer.valueOf(minute)}));
        return stringBuilder.toString();
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getActivity());
    }

    private Intent getTimeSettingsIntent(int type) {
        if (type != 0 && type != 1) {
            return null;
        }
        boolean powerstate = this.mStateArray[0][0];
        Log.i("TIMER", this.mPowerOnPreference.getSummary().toString());
        String currenttime = this.mPowerOnPreference.getSummary().toString();
        int hour = this.mTimeArray[0][0];
        int minute = this.mTimeArray[0][1];
        if (type == 1) {
            powerstate = this.mStateArray[1][0];
            currenttime = this.mPowerOffPref.getTitle().toString();
            Log.i("TIMER", this.mPowerOffPreference.getSummary().toString());
            currenttime = this.mPowerOffPreference.getSummary().toString();
            hour = this.mTimeArray[1][0];
            minute = this.mTimeArray[1][1];
        }
        return getEditIntent(type, hour, minute, powerstate, currenttime);
    }

    private Intent getEditIntent(int type, int hour, int minute, boolean powerstate, String currenttime) {
        if (type != 0 && type != 1) {
            return null;
        }
        Intent intent = new Intent();
        Bundle extras = new Bundle();
        extras.putBoolean(EXTRA_IS_24HOUR, is24Hour());
        extras.putBoolean(EXTRA_POWER_STATE, powerstate);
        extras.putString(EXTRA_DISPLAY_TIME, currenttime);
        extras.putInt(EXTRA_HOUR, hour);
        extras.putInt(EXTRA_MINUTE, minute);
        extras.putInt(EXTRA_POWER_TYPE, type);
        intent.putExtras(extras);
        return intent;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        boolean isChecked = false;
        if (preference instanceof SwitchPreference) {
            isChecked = ((Boolean) objValue).booleanValue();
        }
        String key = preference.getKey();
        if (PREFERENCE_POWER_ON_STATE.equals(key)) {
            updateState(0, isChecked);
            return true;
        } else if (!PREFERENCE_POWER_OFF_STATE.equals(key)) {
            return false;
        } else {
            updateState(1, isChecked);
            return true;
        }
    }

    public static boolean getPowerOnOffStatus(String OnOff) {
        if (OnOff.equals("PowerOnFlag")) {
            return misCheckedPoweron;
        }
        if (OnOff.equals("PowerOffFlag")) {
            return misCheckedPoweroff;
        }
        return false;
    }

    private void updateState(int powerType, boolean checked) {
        if (powerType == 0 || powerType == 1) {
            this.mStateArray[powerType][1] = checked;
            Intent intent;
            if (powerType == 0 && checked) {
                cancleNewPlanLastPowerOn();
                writeData();
                misCheckedPoweron = true;
                intent = new Intent(SettingsUtil.ACTION_POWER_OP_ON);
                intent.addFlags(285212672);
                getActivity().sendBroadcast(intent);
                if (OPUtils.isSupportNewPlanPowerOffAlarm()) {
                    setPowerOn();
                }
            } else if (powerType == 1 && checked) {
                writeData();
                new Bundle().putLong(SettingsUtil.TRIGGER_TIME, new long[2][1]);
                misCheckedPoweroff = true;
                intent = new Intent("com.android.settings.action.REQUEST_POWER_OFF");
                intent.addFlags(285212672);
                getActivity().sendBroadcast(new Intent(intent));
            } else if (powerType == 1 && !checked) {
                misCheckedPoweroff = false;
                writeData();
                intent = new Intent(SettingsUtil.ACTION_POWER_CANCEL_OP_OFF);
                intent.addFlags(285212672);
                getActivity().sendBroadcast(new Intent(intent));
            } else if (powerType == 0 && !checked) {
                writeData();
                misCheckedPoweron = false;
                intent = new Intent(SettingsUtil.ACTION_POWER_OP_ON);
                intent.addFlags(285212672);
                getActivity().sendBroadcast(intent);
                if (OPUtils.isSupportNewPlanPowerOffAlarm()) {
                    long[] jArr = new long[2];
                    jArr = SettingsUtil.getNearestTime(System.getString(getActivity().getContentResolver(), "def_timepower_config"));
                    Intent powerOffIntent = new Intent(ACTION_CANCEL_POWEROFF_ALARM);
                    powerOffIntent.putExtra(TIME, jArr[0]);
                    powerOffIntent.setPackage(POWER_OFF_ALARM_PACKAGE);
                    powerOffIntent.addFlags(285212672);
                    getActivity().sendBroadcast(powerOffIntent);
                }
            }
        }
    }

    private void writeData() {
        String Config = new String("");
        for (int i = 0; i < 2; i++) {
            String tmp = new StringBuilder();
            tmp.append(String.format("%1$02d", new Object[]{Integer.valueOf(this.mTimeArray[i][0])}));
            tmp.append(String.format("%1$02d", new Object[]{Integer.valueOf(this.mTimeArray[i][1])}));
            tmp.append(String.format("%1$01d", new Object[]{Integer.valueOf(boolToInt(this.mStateArray[i][0]))}));
            tmp.append(String.format("%1$01d", new Object[]{Integer.valueOf(boolToInt(this.mStateArray[i][1]))}));
            tmp = tmp.toString();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Config);
            stringBuilder.append(tmp);
            Config = stringBuilder.toString();
        }
        String str = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("writeData: ");
        stringBuilder2.append(Config);
        Log.d(str, stringBuilder2.toString());
        System.putString(getContentResolver(), "def_timepower_config", Config);
    }

    private void cancleNewPlanLastPowerOn() {
        if (OPUtils.isSupportNewPlanPowerOffAlarm()) {
            long[] nearestTime = new long[2];
            nearestTime = SettingsUtil.getNearestTime(System.getString(getActivity().getContentResolver(), "def_timepower_config"));
            Intent powerOffIntent = new Intent(ACTION_CANCEL_POWEROFF_ALARM);
            powerOffIntent.putExtra(TIME, nearestTime[0]);
            powerOffIntent.setPackage(POWER_OFF_ALARM_PACKAGE);
            powerOffIntent.addFlags(285212672);
            getActivity().sendBroadcast(powerOffIntent);
        }
    }

    private void setPowerOn() {
        long[] nearestTime = new long[2];
        nearestTime = SettingsUtil.getNearestTime(System.getString(getActivity().getContentResolver(), "def_timepower_config"));
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("setPowerOn writeData: ");
        stringBuilder.append(nearestTime[0]);
        Log.d(str, stringBuilder.toString());
        ((AlarmManager) getActivity().getSystemService(NotificationCompat.CATEGORY_ALARM)).setExact(0, nearestTime[0], PendingIntent.getBroadcast(getActivity(), 0, new Intent(SettingsUtil.ACTION_POWER_OP_ON), 134217728));
    }

    private void returnNewTimeSetResult(int requestCode, Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            int hour = extras.getInt(EXTRA_HOUR);
            int minute = extras.getInt(EXTRA_MINUTE);
            int index = 0;
            int anotherindex = 1;
            if (requestCode == 1) {
                index = 1;
                anotherindex = 0;
            }
            if (hour == this.mTimeArray[anotherindex][0] && minute == this.mTimeArray[anotherindex][1]) {
                Toast.makeText(getActivity(), getString(R.string.timepower_time_duplicate), 0).show();
                return;
            }
            this.mTimeArray[index][0] = extras.getInt(EXTRA_HOUR);
            this.mTimeArray[index][1] = extras.getInt(EXTRA_MINUTE);
            CharSequence timeDisplay = formatTime(this.mTimeArray[index][0], this.mTimeArray[index][1]);
            this.mStateArray[index][0] = extras.getBoolean(EXTRA_POWER_STATE);
            boolean sendMsgOut = this.mStateArray[index][1];
            if (requestCode == 0) {
                this.mPowerOnPref.setTitle(timeDisplay);
                this.mPowerOnPreference.setSummary(timeDisplay);
            } else if (requestCode == 1) {
                this.mPowerOffPref.setTitle(timeDisplay);
                this.mPowerOffPreference.setSummary(timeDisplay);
            }
            if (sendMsgOut) {
                Intent intent;
                if (this.mCode == 0) {
                    cancleNewPlanLastPowerOn();
                    writeData();
                    intent = new Intent(SettingsUtil.ACTION_POWER_OP_ON);
                    intent.addFlags(285212672);
                    getActivity().sendBroadcast(intent);
                    setPowerOn();
                } else if (this.mCode == 1) {
                    writeData();
                    intent = new Intent("com.android.settings.action.REQUEST_POWER_OFF");
                    intent.addFlags(285212672);
                    getActivity().sendBroadcast(intent);
                } else {
                    writeData();
                }
            } else if (requestCode == 0) {
                updateState(0, true);
                this.mPowerOnStatePref.setChecked(true);
            } else {
                updateState(1, true);
                this.mPowerOffStatePref.setChecked(true);
            }
        }
    }

    private void startDialogForResult(Intent data, int code) {
        if (data != null) {
            this.mCode = code;
            this.mTimePicker = new TimePicker(new ContextThemeWrapper(getActivity(), (int) R.style.f956Theme.Settings.TimePicker));
            int resId = R.string.timepower_edit_title;
            Bundle bundle = data.getExtras();
            this.mPowerState = bundle.getBoolean(EXTRA_POWER_STATE);
            boolean mIs24Hour = bundle.getBoolean(EXTRA_IS_24HOUR);
            int mDlgHour = bundle.getInt(EXTRA_HOUR);
            int mDlgMinute = bundle.getInt(EXTRA_MINUTE);
            int type = bundle.getInt(EXTRA_POWER_TYPE);
            if (type == 0) {
                resId = R.string.timepower_power_on_title;
            } else if (type == 1) {
                resId = R.string.timepower_power_off_title;
            }
            this.mTimePicker.setIs24HourView(Boolean.valueOf(mIs24Hour));
            this.mTimePicker.setCurrentHour(Integer.valueOf(mDlgHour));
            this.mTimePicker.setCurrentMinute(Integer.valueOf(mDlgMinute));
            TimeSetDialogListener listener = new TimeSetDialogListener();
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), R.style.f271OnePlus.Theme.Dialog.Picker, this, mDlgHour, mDlgMinute, mIs24Hour);
            timePickerDialog.setOnDismissListener(this.onDismissListener);
            timePickerDialog.show();
            this.mDlgVisible = true;
        }
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        ReturnData(hourOfDay, minute);
        this.mDlgVisible = false;
    }

    private void ReturnData(int hourOfDay, int minute) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_POWER_STATE, this.mPowerState);
        bundle.putInt(EXTRA_HOUR, hourOfDay);
        bundle.putInt(EXTRA_MINUTE, minute);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        returnNewTimeSetResult(this.mCode, intent);
    }

    private static int boolToInt(boolean b) {
        return b;
    }

    private static boolean intToBool(int i) {
        return i != 0;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
