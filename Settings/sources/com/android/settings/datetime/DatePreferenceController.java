package com.android.settings.datetime;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.lib.app.DatePickerDialog;
import com.oneplus.lib.app.DatePickerDialog.OnDateSetListener;
import com.oneplus.lib.widget.DatePicker;
import java.util.Calendar;

public class DatePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnDateSetListener {
    public static final int DIALOG_DATEPICKER = 0;
    private static final String KEY_DATE = "date";
    private final AutoTimePreferenceController mAutoTimePreferenceController;
    private final DatePreferenceHost mHost;

    public interface DatePreferenceHost extends UpdateTimeAndDateCallback {
        void showDatePicker();
    }

    public DatePreferenceController(Context context, DatePreferenceHost host, AutoTimePreferenceController autoTimePreferenceController) {
        super(context);
        this.mHost = host;
        this.mAutoTimePreferenceController = autoTimePreferenceController;
    }

    public boolean isAvailable() {
        return true;
    }

    public void updateState(Preference preference) {
        if (preference instanceof RestrictedPreference) {
            preference.setSummary(DateFormat.getLongDateFormat(this.mContext).format(Calendar.getInstance().getTime()));
            if (!((RestrictedPreference) preference).isDisabledByAdmin()) {
                preference.setEnabled(this.mAutoTimePreferenceController.isEnabled() ^ 1);
            }
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_DATE)) {
            return false;
        }
        this.mHost.showDatePicker();
        return true;
    }

    public String getPreferenceKey() {
        return KEY_DATE;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        setDate(year, month, day);
        this.mHost.updateTimeAndDateDisplay(this.mContext);
    }

    public DatePickerDialog buildDatePicker(Activity activity) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog d = new DatePickerDialog(activity, R.style.f280OnePlus.Theme.Dialog.Picker, this, calendar.get(1), calendar.get(2), calendar.get(5));
        calendar.clear();
        calendar.set(2007, 0, 1);
        d.getDatePicker().setMinDate(calendar.getTimeInMillis());
        calendar.clear();
        calendar.set(2037, 11, 31);
        d.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        return d;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(1, year);
        c.set(2, month);
        c.set(5, day);
        long when = Math.max(c.getTimeInMillis(), UpdateTimeAndDateCallback.MIN_DATE);
        if (when / 1000 < 2147483647L) {
            ((AlarmManager) this.mContext.getSystemService(NotificationCompat.CATEGORY_ALARM)).setTime(when);
        }
    }
}
