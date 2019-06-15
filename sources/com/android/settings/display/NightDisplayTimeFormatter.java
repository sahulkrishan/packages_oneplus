package com.android.settings.display;

import android.content.Context;
import com.android.internal.app.ColorDisplayController;
import com.android.settings.R;
import java.text.DateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

public class NightDisplayTimeFormatter {
    private DateFormat mTimeFormatter;

    NightDisplayTimeFormatter(Context context) {
        this.mTimeFormatter = android.text.format.DateFormat.getTimeFormat(context);
        this.mTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public String getFormattedTimeString(LocalTime localTime) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(this.mTimeFormatter.getTimeZone());
        c.set(11, localTime.getHour());
        c.set(12, localTime.getMinute());
        c.set(13, 0);
        c.set(14, 0);
        return this.mTimeFormatter.format(c.getTime());
    }

    public String getAutoModeTimeSummary(Context context, ColorDisplayController controller) {
        int summaryFormatResId;
        if (controller.isActivated()) {
            summaryFormatResId = R.string.night_display_summary_on;
        } else {
            summaryFormatResId = R.string.night_display_summary_off;
        }
        return context.getString(summaryFormatResId, new Object[]{getAutoModeSummary(context, controller)});
    }

    private String getAutoModeSummary(Context context, ColorDisplayController controller) {
        boolean isActivated = controller.isActivated();
        int autoMode = controller.getAutoMode();
        int i;
        if (autoMode == 1) {
            if (isActivated) {
                return context.getString(R.string.night_display_summary_on_auto_mode_custom, new Object[]{getFormattedTimeString(controller.getCustomEndTime())});
            }
            return context.getString(R.string.night_display_summary_off_auto_mode_custom, new Object[]{getFormattedTimeString(controller.getCustomStartTime())});
        } else if (autoMode == 2) {
            if (isActivated) {
                i = R.string.night_display_summary_on_auto_mode_twilight;
            } else {
                i = R.string.night_display_summary_off_auto_mode_twilight;
            }
            return context.getString(i);
        } else {
            if (isActivated) {
                i = R.string.night_display_summary_on_auto_mode_never;
            } else {
                i = R.string.night_display_summary_off_auto_mode_never;
            }
            return context.getString(i);
        }
    }
}
