package com.oneplus.settings.better;

import android.app.Dialog;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.widget.SeekBar;
import android.widget.Toast;
import com.android.internal.app.ColorDisplayController;
import com.android.internal.app.ColorDisplayController.Callback;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.fuelgauge.WallOfTextPreference;
import com.oneplus.lib.app.TimePickerDialog;
import com.oneplus.lib.app.TimePickerDialog.OnTimeSetListener;
import com.oneplus.lib.widget.TimePicker;
import com.oneplus.settings.OneplusColorManager;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.ui.OPNightModeLevelPreferenceCategory;
import com.oneplus.settings.ui.OPNightModeLevelPreferenceCategory.OPNightModeLevelPreferenceChangeListener;
import com.oneplus.settings.utils.OPUtils;
import java.text.DateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

public class OPNightMode extends SettingsPreferenceFragment implements Callback, OnPreferenceChangeListener, OPNightModeLevelPreferenceChangeListener {
    private static final int AUTO_ACTIVATE_CUSTOMIZED_VALUE = 2;
    private static final int DIALOG_TURN_OFF_TIME = 1;
    private static final int DIALOG_TURN_ON_TIME = 0;
    private static final String KEY_AUTO_ACTIVATE = "auto_activate";
    private static final String KEY_NIGHT_MODE_ENABLED_OP = "night_mode_enabled";
    private static final String KEY_NIGHT_MODE_LEVEL_OP = "night_mode_level_op";
    private static final String KEY_NIGHT_MODE_SUMMARY = "night_mode_summary";
    private static final String KEY_SET_TIME = "set_time";
    private static final String KEY_TURN_OFF_TIME = "turn_off_time";
    private static final String KEY_TURN_ON_TIME = "turn_on_time";
    private static final int NEVER_AUTO_VALUE = 0;
    private static final String NIGHT_MODE_ENABLED = "night_mode_enabled";
    private static final int SUNRISE_SUNSET_VALUE = 1;
    private static final String TAG = "OPNightMode";
    private boolean isSupportReadingMode;
    private ListPreference mAutoActivatePreference;
    private OneplusColorManager mCM;
    private ColorDisplayController mController;
    private WallOfTextPreference mNightModSummary;
    private SwitchPreference mNightModeEnabledPreference;
    private OPNightModeLevelPreferenceCategory mNightModeLevelPreferenceCategory;
    private ContentObserver mNightModeSeekBarContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            int progress = System.getIntForUser(OPNightMode.this.getContentResolver(), "oem_nightmode_progress_status", 103, -2);
            OPUtils.sendAppTrackerForEffectStrength();
            if (OPNightMode.this.mNightModeLevelPreferenceCategory != null) {
                OPNightMode.this.mNightModeLevelPreferenceCategory.setSeekBarProgress(progress);
            }
            boolean z = true;
            boolean readingmodeEnbale = System.getIntForUser(OPNightMode.this.getContentResolver(), OPReadingMode.READING_MODE_STATUS, 0, -2) != 1;
            if (OPNightMode.this.mNightModeLevelPreferenceCategory != null) {
                OPNightMode.this.mNightModeLevelPreferenceCategory.setEnabled(readingmodeEnbale);
            }
            if (Secure.getIntForUser(OPNightMode.this.getContentResolver(), "night_display_activated", 0, -2) != 1) {
                z = false;
            }
            boolean activated = z;
            if (OPNightMode.this.mNightModeLevelPreferenceCategory != null) {
                OPNightMode.this.mNightModeLevelPreferenceCategory.setEnabled(activated);
            }
        }
    };
    private PreferenceCategory mSetTimePreferenceCategory;
    private DateFormat mTimeFormatter;
    private Preference mTurnOffTimePreference;
    private Preference mTurnOnTimePreference;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_night_mode);
        Context context = getContext();
        this.isSupportReadingMode = context.getPackageManager().hasSystemFeature("oem.read_mode.support");
        this.mController = new ColorDisplayController(context);
        this.mNightModeEnabledPreference = (SwitchPreference) findPreference("night_mode_enabled");
        this.mAutoActivatePreference = (ListPreference) findPreference(KEY_AUTO_ACTIVATE);
        this.mSetTimePreferenceCategory = (PreferenceCategory) findPreference(KEY_SET_TIME);
        this.mTurnOnTimePreference = findPreference(KEY_TURN_ON_TIME);
        this.mTurnOffTimePreference = findPreference(KEY_TURN_OFF_TIME);
        this.mNightModSummary = (WallOfTextPreference) findPreference(KEY_NIGHT_MODE_SUMMARY);
        this.mNightModeLevelPreferenceCategory = (OPNightModeLevelPreferenceCategory) findPreference(KEY_NIGHT_MODE_LEVEL_OP);
        if (this.mNightModeEnabledPreference != null) {
            this.mNightModeEnabledPreference.setOnPreferenceChangeListener(this);
        }
        if (this.mNightModeLevelPreferenceCategory != null) {
            this.mNightModeLevelPreferenceCategory.setOPNightModeLevelSeekBarChangeListener(this);
        }
        this.mAutoActivatePreference.setValue(String.valueOf(this.mController.getAutoMode()));
        this.mAutoActivatePreference.setOnPreferenceChangeListener(this);
        this.mTimeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        this.mTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        updateAutoActivateModePreferenceDescription(convertAutoMode(this.mController.getAutoMode()));
        this.mCM = new OneplusColorManager(SettingsBaseApplication.mApplication);
        boolean z = true;
        if (System.getIntForUser(getContentResolver(), OPReadingMode.READING_MODE_STATUS, 0, -2) == 1) {
            z = false;
        }
        this.mNightModeLevelPreferenceCategory.setEnabled(z);
        if (this.isSupportReadingMode) {
            this.mNightModSummary.setSummary(SettingsBaseApplication.mApplication.getText(R.string.oneplus_night_mode_sensor_summary));
        }
    }

    private int convertAutoMode(int value) {
        if (value == 0) {
            return 0;
        }
        if (value == 1) {
            return 2;
        }
        return 1;
    }

    public void onStart() {
        super.onStart();
        this.mController.setListener(this);
        onActivated(this.mController.isActivated());
        onAutoModeChanged(this.mController.getAutoMode());
        onCustomStartTimeChanged(this.mController.getCustomStartTime());
        onCustomEndTimeChanged(this.mController.getCustomEndTime());
        getContentResolver().registerContentObserver(System.getUriFor("oem_nightmode_progress_status"), true, this.mNightModeSeekBarContentObserver, -2);
        getContentResolver().registerContentObserver(System.getUriFor(OPReadingMode.READING_MODE_STATUS), true, this.mNightModeSeekBarContentObserver, -2);
        getContentResolver().registerContentObserver(Secure.getUriFor("night_display_activated"), true, this.mNightModeSeekBarContentObserver, -2);
    }

    public void onStop() {
        super.onStop();
        this.mController.setListener(null);
        getContentResolver().unregisterContentObserver(this.mNightModeSeekBarContentObserver);
    }

    public void onActivated(boolean activated) {
        this.mNightModeEnabledPreference.setChecked(activated);
        this.mNightModeLevelPreferenceCategory.setEnabled(activated);
    }

    public void onAutoModeChanged(int autoMode) {
        this.mAutoActivatePreference.setValue(String.valueOf(autoMode));
        boolean showCustomSchedule = true;
        if (autoMode != 1) {
            showCustomSchedule = false;
        }
        this.mTurnOnTimePreference.setVisible(showCustomSchedule);
        this.mTurnOffTimePreference.setVisible(showCustomSchedule);
    }

    private void updateAutoActivateModePreferenceDescription(int value) {
        if (this.mAutoActivatePreference != null) {
            this.mAutoActivatePreference.setSummary(this.mAutoActivatePreference.getEntries()[value]);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        if ("night_mode_enabled".equals(key)) {
            this.mController.setActivated(((Boolean) objValue).booleanValue());
            OPUtils.sendAppTrackerForNightMode();
        } else if (KEY_AUTO_ACTIVATE.equals(key)) {
            this.mController.setAutoMode(Integer.parseInt((String) objValue));
            updateAutoActivateModePreferenceDescription(convertAutoMode(this.mController.getAutoMode()));
            OPUtils.sendAppTrackerForAutoNightMode();
        }
        return true;
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (KEY_TURN_ON_TIME.equals(key)) {
            showDialog(0);
            return true;
        } else if (!KEY_TURN_OFF_TIME.equals(key)) {
            return super.onPreferenceTreeClick(preference);
        } else {
            showDialog(1);
            return true;
        }
    }

    public Dialog onCreateDialog(final int dialogId) {
        if (dialogId != 0 && dialogId != 1) {
            return super.onCreateDialog(dialogId);
        }
        LocalTime initialTime;
        if (dialogId == 0) {
            initialTime = this.mController.getCustomStartTime();
        } else {
            initialTime = this.mController.getCustomEndTime();
        }
        Context context = getContext();
        return new TimePickerDialog(context, new OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                LocalTime time = LocalTime.of(hourOfDay, minute);
                if (dialogId == 0) {
                    if (String.valueOf(OPNightMode.this.mController.getCustomEndTime()).equals(String.valueOf(time))) {
                        Toast.makeText(OPNightMode.this.getPrefContext(), R.string.timepower_time_duplicate, 1).show();
                    } else {
                        OPNightMode.this.mController.setCustomStartTime(time);
                    }
                } else if (String.valueOf(OPNightMode.this.mController.getCustomStartTime()).equals(String.valueOf(time))) {
                    Toast.makeText(OPNightMode.this.getPrefContext(), R.string.timepower_time_duplicate, 1).show();
                } else {
                    OPNightMode.this.mController.setCustomEndTime(time);
                }
            }
        }, initialTime.getHour(), initialTime.getMinute(), android.text.format.DateFormat.is24HourFormat(context));
    }

    public int getDialogMetricsCategory(int dialogId) {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    private String getFormattedTimeString(LocalTime localTime) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(this.mTimeFormatter.getTimeZone());
        c.set(11, localTime.getHour());
        c.set(12, localTime.getMinute());
        c.set(13, 0);
        c.set(14, 0);
        return this.mTimeFormatter.format(c.getTime());
    }

    public void onCustomStartTimeChanged(LocalTime startTime) {
        this.mTurnOnTimePreference.setSummary(getFormattedTimeString(startTime));
    }

    public void onCustomEndTimeChanged(LocalTime endTime) {
        this.mTurnOffTimePreference.setSummary(getFormattedTimeString(endTime));
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (this.isSupportReadingMode) {
                this.mCM.setColorBalance((132 - progress) - 90);
            } else {
                this.mCM.setColorBalance((132 - progress) - 56);
            }
            System.putIntForUser(getContentResolver(), "oem_nightmode_progress_status", progress, -2);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        if (!this.mController.isActivated()) {
            boolean z = this.isSupportReadingMode;
        }
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        System.putIntForUser(getContentResolver(), "oem_nightmode_progress_status", seekBar.getProgress(), -2);
        this.mCM.setColorBalance(-512);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
