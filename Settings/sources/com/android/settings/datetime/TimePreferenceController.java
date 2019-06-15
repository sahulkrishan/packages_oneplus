package com.android.settings.datetime;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.lib.app.TimePickerDialog;
import com.oneplus.lib.app.TimePickerDialog.OnTimeSetListener;
import com.oneplus.lib.widget.TimePicker;
import java.util.Calendar;

public class TimePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnTimeSetListener {
    public static final int DIALOG_TIMEPICKER = 1;
    private static final String KEY_TIME = "time";
    private final AutoTimePreferenceController mAutoTimePreferenceController;
    private final TimePreferenceHost mHost;

    public interface TimePreferenceHost extends UpdateTimeAndDateCallback {
        void showTimePicker();
    }

    public TimePreferenceController(Context context, TimePreferenceHost callback, AutoTimePreferenceController autoTimePreferenceController) {
        super(context);
        this.mHost = callback;
        this.mAutoTimePreferenceController = autoTimePreferenceController;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        if (preference instanceof RestrictedPreference) {
            preference.setSummary(DateFormat.getTimeFormat(this.mContext).format(Calendar.getInstance().getTime()));
            if (!((RestrictedPreference) preference).isDisabledByAdmin()) {
                preference.setEnabled(this.mAutoTimePreferenceController.isEnabled() ^ 1);
            }
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(KEY_TIME, preference.getKey())) {
            return false;
        }
        this.mHost.showTimePicker();
        return true;
    }

    public String getPreferenceKey() {
        return KEY_TIME;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (this.mContext != null) {
            setTime(hourOfDay, minute);
            this.mHost.updateTimeAndDateDisplay(this.mContext);
        }
    }

    public TimePickerDialog buildTimePicker(Activity activity) {
        Calendar calendar = Calendar.getInstance();
        return new TimePickerDialog(activity, R.style.f280OnePlus.Theme.Dialog.Picker, this, calendar.get(11), calendar.get(12), DateFormat.is24HourFormat(activity));
    }

    /* Access modifiers changed, original: 0000 */
    public void setTime(int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(11, hourOfDay);
        c.set(12, minute);
        c.set(13, 0);
        c.set(14, 0);
        long when = Math.max(c.getTimeInMillis(), UpdateTimeAndDateCallback.MIN_DATE);
        if (when / 1000 < 2147483647L) {
            ((AlarmManager) this.mContext.getSystemService(NotificationCompat.CATEGORY_ALARM)).setTime(when);
        }
    }
}
