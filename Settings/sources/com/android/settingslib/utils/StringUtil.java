package com.android.settingslib.utils;

import android.content.Context;
import android.icu.text.DisplayContext;
import android.icu.text.MeasureFormat;
import android.icu.text.MeasureFormat.FormatWidth;
import android.icu.text.RelativeDateTimeFormatter;
import android.icu.text.RelativeDateTimeFormatter.Direction;
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit;
import android.icu.text.RelativeDateTimeFormatter.Style;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.icu.util.ULocale;
import android.text.SpannableStringBuilder;
import android.text.style.TtsSpan.MeasureBuilder;
import com.android.settingslib.R;
import java.util.ArrayList;

public class StringUtil {
    public static final int SECONDS_PER_DAY = 86400;
    public static final int SECONDS_PER_HOUR = 3600;
    public static final int SECONDS_PER_MINUTE = 60;

    public static CharSequence formatElapsedTime(Context context, double millis, boolean withSeconds) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        int seconds = (int) Math.floor(millis / 1000.0d);
        if (!withSeconds) {
            seconds += 30;
        }
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (seconds >= SECONDS_PER_DAY) {
            days = seconds / SECONDS_PER_DAY;
            seconds -= SECONDS_PER_DAY * days;
        }
        if (seconds >= SECONDS_PER_HOUR) {
            hours = seconds / SECONDS_PER_HOUR;
            seconds -= hours * SECONDS_PER_HOUR;
        }
        if (seconds >= 60) {
            minutes = seconds / 60;
            seconds -= minutes * 60;
        }
        ArrayList<Measure> measureList = new ArrayList(4);
        if (days > 0) {
            measureList.add(new Measure(Integer.valueOf(days), MeasureUnit.DAY));
        }
        if (hours > 0) {
            measureList.add(new Measure(Integer.valueOf(hours), MeasureUnit.HOUR));
        }
        if (minutes > 0) {
            measureList.add(new Measure(Integer.valueOf(minutes), MeasureUnit.MINUTE));
        }
        if (withSeconds && seconds > 0) {
            measureList.add(new Measure(Integer.valueOf(seconds), MeasureUnit.SECOND));
        }
        if (measureList.size() == 0) {
            measureList.add(new Measure(Integer.valueOf(0), withSeconds ? MeasureUnit.SECOND : MeasureUnit.MINUTE));
        }
        Measure[] measureArray = (Measure[]) measureList.toArray(new Measure[measureList.size()]);
        sb.append(MeasureFormat.getInstance(context.getResources().getConfiguration().locale, FormatWidth.SHORT).formatMeasures(measureArray));
        if (measureArray.length == 1 && MeasureUnit.MINUTE.equals(measureArray[0].getUnit())) {
            sb.setSpan(new MeasureBuilder().setNumber((long) minutes).setUnit("minute").build(), 0, sb.length(), 33);
        }
        return sb;
    }

    public static CharSequence formatRelativeTime(Context context, double millis, boolean withSeconds) {
        int seconds = (int) Math.floor(millis / 1000.0d);
        if (withSeconds && seconds < 120) {
            return context.getResources().getString(R.string.time_unit_just_now);
        }
        RelativeUnit unit;
        int value;
        if (seconds < 7200) {
            unit = RelativeUnit.MINUTES;
            value = (seconds + 30) / 60;
        } else if (seconds < 172800) {
            unit = RelativeUnit.HOURS;
            value = (seconds + 1800) / SECONDS_PER_HOUR;
        } else {
            unit = RelativeUnit.DAYS;
            value = (43200 + seconds) / SECONDS_PER_DAY;
        }
        return RelativeDateTimeFormatter.getInstance(ULocale.forLocale(context.getResources().getConfiguration().locale), null, Style.LONG, DisplayContext.CAPITALIZATION_FOR_MIDDLE_OF_SENTENCE).format((double) value, Direction.LAST, unit);
    }
}
