package com.android.settingslib.utils;

import android.content.Context;
import android.icu.text.DateFormat;
import android.icu.text.MeasureFormat;
import android.icu.text.MeasureFormat.FormatWidth;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.android.settingslib.R;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class PowerUtil {
    private static final long FIFTEEN_MINUTES_MILLIS = TimeUnit.MINUTES.toMillis(15);
    private static final long ONE_DAY_MILLIS = TimeUnit.DAYS.toMillis(1);
    private static final long ONE_HOUR_MILLIS = TimeUnit.HOURS.toMillis(1);
    private static final long SEVEN_MINUTES_MILLIS = TimeUnit.MINUTES.toMillis(7);
    private static final long TWO_DAYS_MILLIS = TimeUnit.DAYS.toMillis(2);

    public static String getBatteryRemainingStringFormatted(Context context, long drainTimeMs, @Nullable String percentageString, boolean basedOnUsage) {
        if (drainTimeMs <= 0) {
            return null;
        }
        if (drainTimeMs <= SEVEN_MINUTES_MILLIS) {
            return getShutdownImminentString(context, percentageString);
        }
        if (drainTimeMs <= FIFTEEN_MINUTES_MILLIS) {
            return getUnderFifteenString(context, StringUtil.formatElapsedTime(context, (double) FIFTEEN_MINUTES_MILLIS, false), percentageString);
        }
        if (drainTimeMs >= TWO_DAYS_MILLIS) {
            return getMoreThanTwoDaysString(context, percentageString);
        }
        if (drainTimeMs >= ONE_DAY_MILLIS) {
            return getMoreThanOneDayString(context, drainTimeMs, percentageString, basedOnUsage);
        }
        return getRegularTimeRemainingString(context, drainTimeMs, percentageString, basedOnUsage);
    }

    private static String getShutdownImminentString(Context context, String percentageString) {
        if (TextUtils.isEmpty(percentageString)) {
            return context.getString(R.string.power_remaining_duration_only_shutdown_imminent);
        }
        return context.getString(R.string.power_remaining_duration_shutdown_imminent, new Object[]{percentageString});
    }

    private static String getUnderFifteenString(Context context, CharSequence timeString, String percentageString) {
        if (TextUtils.isEmpty(percentageString)) {
            return context.getString(R.string.power_remaining_less_than_duration_only, new Object[]{timeString});
        }
        return context.getString(R.string.power_remaining_less_than_duration, new Object[]{timeString, percentageString});
    }

    private static String getMoreThanOneDayString(Context context, long drainTimeMs, String percentageString, boolean basedOnUsage) {
        CharSequence timeString = StringUtil.formatElapsedTime(context, (double) roundTimeToNearestThreshold(drainTimeMs, ONE_HOUR_MILLIS), false);
        int id;
        if (TextUtils.isEmpty(percentageString)) {
            if (basedOnUsage) {
                id = R.string.power_remaining_duration_only_enhanced;
            } else {
                id = R.string.power_remaining_duration_only;
            }
            return context.getString(id, new Object[]{timeString});
        }
        if (basedOnUsage) {
            id = R.string.power_discharging_duration_enhanced;
        } else {
            id = R.string.power_discharging_duration;
        }
        return context.getString(id, new Object[]{timeString, percentageString});
    }

    private static String getMoreThanTwoDaysString(Context context, String percentageString) {
        MeasureFormat frmt = MeasureFormat.getInstance(context.getResources().getConfiguration().getLocales().get(0), FormatWidth.SHORT);
        Measure daysMeasure = new Measure(Integer.valueOf(2), MeasureUnit.DAY);
        if (TextUtils.isEmpty(percentageString)) {
            int i = R.string.power_remaining_only_more_than_subtext;
            Object[] objArr = new Object[1];
            objArr[0] = frmt.formatMeasures(new Measure[]{daysMeasure});
            return context.getString(i, objArr);
        }
        int i2 = R.string.power_remaining_more_than_subtext;
        r4 = new Object[2];
        r4[0] = frmt.formatMeasures(new Measure[]{daysMeasure});
        r4[1] = percentageString;
        return context.getString(i2, r4);
    }

    private static String getRegularTimeRemainingString(Context context, long drainTimeMs, String percentageString, boolean basedOnUsage) {
        CharSequence timeString = DateFormat.getInstanceForSkeleton(android.text.format.DateFormat.getTimeFormatString(context)).format(Date.from(Instant.ofEpochMilli(roundTimeToNearestThreshold(System.currentTimeMillis() + drainTimeMs, FIFTEEN_MINUTES_MILLIS))));
        int id;
        if (TextUtils.isEmpty(percentageString)) {
            if (basedOnUsage) {
                id = R.string.power_discharge_by_only_enhanced;
            } else {
                id = R.string.power_discharge_by_only;
            }
            return context.getString(id, new Object[]{timeString});
        }
        if (basedOnUsage) {
            id = R.string.power_discharge_by_enhanced;
        } else {
            id = R.string.power_discharge_by;
        }
        return context.getString(id, new Object[]{timeString, percentageString});
    }

    public static long convertUsToMs(long timeUs) {
        return timeUs / 1000;
    }

    public static long convertMsToUs(long timeMs) {
        return 1000 * timeMs;
    }

    public static long roundTimeToNearestThreshold(long drainTime, long threshold) {
        long time = Math.abs(drainTime);
        long multiple = Math.abs(threshold);
        long remainder = time % multiple;
        if (remainder < multiple / 2) {
            return time - remainder;
        }
        return (time - remainder) + multiple;
    }
}
