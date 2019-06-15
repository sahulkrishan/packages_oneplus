package com.oneplus.settings.better;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import com.android.internal.app.ColorDisplayController;
import com.android.internal.app.ColorDisplayController.Callback;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
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
    public static final float DEFAULT_BRIGHTNESS_VALUE = 1.0f;
    public static final int DEFAULT_COLOR_PROGRESS = (OPUtils.isSupportReadingModeInterpolater() ? 29 : 103);
    private static final int DIALOG_TURN_OFF_TIME = 1;
    private static final int DIALOG_TURN_ON_TIME = 0;
    private static final String KEY_AUTO_ACTIVATE = "auto_activate";
    public static final String KEY_NIGHT_MODE_BRIGHTNESS = "oem_nightmode_brightness_progress";
    private static final String KEY_NIGHT_MODE_ENABLED_OP = "night_mode_enabled";
    private static final String KEY_NIGHT_MODE_LEVEL_OP = "night_mode_level_op";
    private static final String KEY_TURN_OFF_TIME = "turn_off_time";
    private static final String KEY_TURN_ON_TIME = "turn_on_time";
    public static final float MAX_BRIGHTNESS = 1.0f;
    public static final float MIN_BRIGHTNESS = 0.55f;
    private static final int NEVER_AUTO_VALUE = 0;
    private static final int SUNRISE_SUNSET_VALUE = 1;
    private static final String TAG = "OPNightMode";
    private boolean isSupportReadingMode;
    private ListPreference mAutoActivatePreference;
    private OneplusColorManager mCM;
    private ColorDisplayController mController;
    private DisplayManager mDisplayManager;
    private boolean mIsReadingModeOn;
    private SwitchPreference mNightModeEnabledPreference;
    private OPNightModeLevelPreferenceCategory mNightModeLevelPreferenceCategory;
    private ContentObserver mNightModeSeekBarContentObserver = new ContentObserver(new Handler()) {
        private final Uri ACCESSIBILITY_DISPLAY_GRAYSCALE_ENABLED_URI = System.getUriFor("accessibility_display_grayscale_enabled");

        public void onChange(boolean selfChange, Uri uri) {
            int colorProgress = System.getIntForUser(OPNightMode.this.getContentResolver(), "oem_nightmode_progress_status", OPNightMode.DEFAULT_COLOR_PROGRESS, -2);
            int brightnessProgress = OPNightMode.transferToBrightnessProgress(System.getFloatForUser(OPNightMode.this.getContentResolver(), OPNightMode.KEY_NIGHT_MODE_BRIGHTNESS, 1.0f, -2));
            if (this.ACCESSIBILITY_DISPLAY_GRAYSCALE_ENABLED_URI.equals(uri)) {
                OPNightMode.this.disableEntryForWellbeingGrayscale();
            }
            OPUtils.sendAppTrackerForEffectStrength();
        }
    };
    private ValueAnimator mOPenNightModeAnimator;
    private ValueAnimator mRestoreBrightnessAnimator;
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
        this.mTurnOnTimePreference = findPreference(KEY_TURN_ON_TIME);
        this.mTurnOffTimePreference = findPreference(KEY_TURN_OFF_TIME);
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
        this.mDisplayManager = (DisplayManager) getSystemService("display");
        boolean z = false;
        if (System.getIntForUser(getContentResolver(), OPReadingMode.READING_MODE_STATUS, 0, -2) != 0) {
            z = true;
        }
        this.mIsReadingModeOn = z;
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
        getContentResolver().registerContentObserver(System.getUriFor("accessibility_display_grayscale_enabled"), true, this.mNightModeSeekBarContentObserver, -2);
    }

    public void onStop() {
        super.onStop();
        this.mController.setListener(null);
        getContentResolver().unregisterContentObserver(this.mNightModeSeekBarContentObserver);
    }

    public void onResume() {
        super.onResume();
        onActivated(this.mController.isActivated());
        disableEntryForWellbeingGrayscale();
    }

    public void onActivated(boolean activated) {
        this.mNightModeEnabledPreference.setChecked(activated);
        disableEntryForWellbeingGrayscale();
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

    private void disableEntryForWellbeingGrayscale() {
        boolean z = false;
        boolean isWellbeingGrayscaleEnabled = System.getInt(getContentResolver(), "accessibility_display_grayscale_enabled", 1) == 0;
        boolean OPNightModeState = Secure.getIntForUser(getContentResolver(), "night_display_activated", 0, -2) == 1;
        if (this.mNightModeEnabledPreference != null) {
            this.mNightModeEnabledPreference.setEnabled(!isWellbeingGrayscaleEnabled);
        }
        if (this.mAutoActivatePreference != null) {
            this.mAutoActivatePreference.setEnabled(!isWellbeingGrayscaleEnabled);
        }
        if (this.mNightModeLevelPreferenceCategory != null) {
            OPNightModeLevelPreferenceCategory oPNightModeLevelPreferenceCategory = this.mNightModeLevelPreferenceCategory;
            if (OPNightModeState && !isWellbeingGrayscaleEnabled) {
                z = true;
            }
            oPNightModeLevelPreferenceCategory.setEnabled(z);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        String key = preference.getKey();
        if ("night_mode_enabled".equals(key)) {
            boolean enabled = ((Boolean) objValue).booleanValue();
            OPUtils.sendAppTrackerForNightMode();
            this.mNightModeLevelPreferenceCategory.setEnabled(enabled);
            int colorProgress = this.mNightModeLevelPreferenceCategory.getColorProgress();
            int brightnessProgress = this.mNightModeLevelPreferenceCategory.getBrightnessProgress();
            float brightnessValue = transferToBrightnessValue(brightnessProgress);
            if (enabled) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("onPreferenceChange colorProgress:");
                stringBuilder.append(colorProgress);
                stringBuilder.append(" brightnessProgress:");
                stringBuilder.append(brightnessProgress);
                stringBuilder.append(" brightnessValue:");
                stringBuilder.append(brightnessValue);
                Log.d(str, stringBuilder.toString());
                this.mController.setActivated(true);
                saveColorTemperatureProgress(colorProgress);
                saveBrightnessProgress(brightnessValue);
            } else {
                this.mController.setActivated(false);
            }
        } else if (KEY_AUTO_ACTIVATE.equals(key)) {
            this.mController.setAutoMode(Integer.parseInt((String) objValue));
            updateAutoActivateModePreferenceDescription(convertAutoMode(this.mController.getAutoMode()));
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

    public void onColorProgressChanged(int progress, boolean fromUser) {
        if (fromUser) {
            setColorBalance(progress);
            saveColorTemperatureProgress(progress);
        }
    }

    public void onColorStartTrackingTouch(int progress) {
        if (!this.mController.isActivated()) {
            boolean z = this.isSupportReadingMode;
        }
    }

    public void onColorStopTrackingTouch(int progress) {
        this.mCM.setColorBalance(-512);
        saveColorTemperatureProgress(progress);
    }

    public void onBrightnessProgressChanged(int progress, boolean fromUser) {
        if (fromUser) {
            saveBrightnessProgress(transferToBrightnessValue(progress));
        }
    }

    public void onBrightnessStartTrackingTouch(int progress) {
    }

    public void onBrightnessStopTrackingTouch(int progress) {
        onBrightnessProgressChanged(progress, true);
    }

    private void closeNightModeGradually(float fromValue) {
        if (this.mRestoreBrightnessAnimator != null) {
            this.mRestoreBrightnessAnimator.cancel();
        }
        this.mRestoreBrightnessAnimator = ValueAnimator.ofFloat(new float[]{fromValue, 1.0f});
        this.mRestoreBrightnessAnimator.setDuration(800);
        this.mRestoreBrightnessAnimator.setInterpolator(new LinearInterpolator());
        this.mRestoreBrightnessAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
            }
        });
        this.mRestoreBrightnessAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                OPNightMode.this.mNightModeEnabledPreference.setEnabled(false);
                OPNightMode.this.mController.setActivated(false);
            }

            public void onAnimationEnd(Animator animation) {
                OPNightMode.this.mNightModeEnabledPreference.setEnabled(true);
            }

            public void onAnimationCancel(Animator animation) {
                OPNightMode.this.mNightModeEnabledPreference.setEnabled(true);
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
        this.mRestoreBrightnessAnimator.start();
    }

    private void openNightModeGradually(int colorBalance, float brightness) {
        if (this.mOPenNightModeAnimator != null) {
            this.mOPenNightModeAnimator.cancel();
        }
        this.mOPenNightModeAnimator = ValueAnimator.ofFloat(new float[]{1.0f, brightness});
        this.mOPenNightModeAnimator.setDuration(400);
        this.mOPenNightModeAnimator.setInterpolator(new LinearInterpolator());
        this.mOPenNightModeAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
            }
        });
        this.mOPenNightModeAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                OPNightMode.this.mNightModeEnabledPreference.setEnabled(false);
                OPNightMode.this.mController.setActivated(true);
            }

            public void onAnimationEnd(Animator animation) {
                OPNightMode.this.mNightModeEnabledPreference.setEnabled(true);
            }

            public void onAnimationCancel(Animator animation) {
                OPNightMode.this.mNightModeEnabledPreference.setEnabled(true);
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
        this.mOPenNightModeAnimator.start();
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    private void setColorBalance(int colorProgress) {
        if (!this.isSupportReadingMode) {
            this.mCM.setColorBalance((132 - colorProgress) - 56);
        } else if (OPUtils.isSupportReadingModeInterpolater()) {
            this.mCM.setColorBalance(9 - colorProgress);
        } else {
            this.mCM.setColorBalance((132 - colorProgress) - 90);
        }
    }

    public static float transferToBrightnessValue(int progress) {
        return 1.0f - ((((float) progress) * 0.45f) / 100.0f);
    }

    public static int transferToBrightnessProgress(float brightnessValue) {
        return (int) (((1.0f - brightnessValue) / 0.45f) * 1120403456);
    }

    private void saveColorTemperatureProgress(int progress) {
        System.putIntForUser(getContentResolver(), "oem_nightmode_progress_status", progress, -2);
    }

    private void saveBrightnessProgress(float brightnessValue) {
        System.putFloatForUser(getContentResolver(), KEY_NIGHT_MODE_BRIGHTNESS, brightnessValue, -2);
    }

    public static boolean checkCurrentValueIsDefault(int colorProgress, float brightnessValue) {
        return colorProgress == DEFAULT_COLOR_PROGRESS && brightnessValue == 1.0f;
    }

    public void onDestroy() {
        super.onDestroy();
        OPUtils.sendAnalytics("night_mode", "auto_open", String.valueOf(convertAutoMode(this.mController.getAutoMode())));
        int colorProgress = System.getIntForUser(getContentResolver(), "oem_nightmode_progress_status", DEFAULT_COLOR_PROGRESS, -2);
        if (((double) colorProgress) <= ((double) this.mNightModeLevelPreferenceCategory.getColorProgressMax()) * 0.33d) {
            OPUtils.sendAnalytics("night_mode", "screen_color", "1");
        } else if (((double) colorProgress) <= ((double) this.mNightModeLevelPreferenceCategory.getColorProgressMax()) * 0.66d) {
            OPUtils.sendAnalytics("night_mode", "screen_color", "2");
        } else if (colorProgress <= this.mNightModeLevelPreferenceCategory.getColorProgressMax()) {
            OPUtils.sendAnalytics("night_mode", "screen_color", "3");
        }
        float brightnessValue = (float) transferToBrightnessProgress(System.getFloatForUser(getContentResolver(), KEY_NIGHT_MODE_BRIGHTNESS, 1.0f, -2));
        if (((double) brightnessValue) <= ((double) this.mNightModeLevelPreferenceCategory.getBrightnessProgressMax()) * 0.33d) {
            OPUtils.sendAnalytics("night_mode", "brightness", "1");
        } else if (((double) brightnessValue) <= ((double) this.mNightModeLevelPreferenceCategory.getBrightnessProgressMax()) * 0.66d) {
            OPUtils.sendAnalytics("night_mode", "brightness", "2");
        } else if (brightnessValue <= ((float) this.mNightModeLevelPreferenceCategory.getBrightnessProgressMax())) {
            OPUtils.sendAnalytics("night_mode", "brightness", "3");
        }
    }
}
