package com.android.settings.datetime.timezone;

import android.content.Context;
import android.icu.impl.OlsonTimeZone;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.TimeZoneTransition;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import java.util.Date;

public class TimeZoneInfoPreferenceController extends BaseTimeZonePreferenceController {
    private static final String PREFERENCE_KEY = "footer_preference";
    @VisibleForTesting
    Date mDate;
    private final DateFormat mDateFormat = DateFormat.getDateInstance(1);
    private TimeZoneInfo mTimeZoneInfo;

    public TimeZoneInfoPreferenceController(Context context) {
        super(context, "footer_preference");
        this.mDateFormat.setContext(DisplayContext.CAPITALIZATION_NONE);
        this.mDate = new Date();
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void updateState(Preference preference) {
        preference.setTitle(this.mTimeZoneInfo == null ? "" : formatInfo(this.mTimeZoneInfo));
        preference.setVisible(this.mTimeZoneInfo != null);
    }

    public void setTimeZoneInfo(TimeZoneInfo timeZoneInfo) {
        this.mTimeZoneInfo = timeZoneInfo;
    }

    public TimeZoneInfo getTimeZoneInfo() {
        return this.mTimeZoneInfo;
    }

    private CharSequence formatOffsetAndName(TimeZoneInfo item) {
        String name = item.getGenericName();
        if (name == null) {
            if (item.getTimeZone().inDaylightTime(this.mDate)) {
                name = item.getDaylightName();
            } else {
                name = item.getStandardName();
            }
        }
        if (name == null) {
            return item.getGmtOffset().toString();
        }
        return SpannableUtil.getResourcesText(this.mContext.getResources(), R.string.zone_info_offset_and_name, item.getGmtOffset(), name);
    }

    private CharSequence formatInfo(TimeZoneInfo item) {
        CharSequence offsetAndName = formatOffsetAndName(item);
        TimeZone timeZone = item.getTimeZone();
        if (timeZone.observesDaylightTime()) {
            TimeZoneTransition nextDstTransition = findNextDstTransition(timeZone);
            if (nextDstTransition == null) {
                return null;
            }
            boolean toDst = nextDstTransition.getTo().getDSTSavings() != 0;
            String timeType = toDst ? item.getDaylightName() : item.getStandardName();
            if (timeType == null) {
                String string;
                if (toDst) {
                    string = this.mContext.getString(R.string.zone_time_type_dst);
                } else {
                    string = this.mContext.getString(R.string.zone_time_type_standard);
                }
                timeType = string;
            }
            Calendar transitionTime = Calendar.getInstance(timeZone);
            transitionTime.setTimeInMillis(nextDstTransition.getTime());
            String date = this.mDateFormat.format(transitionTime);
            return SpannableUtil.getResourcesText(this.mContext.getResources(), R.string.zone_info_footer, offsetAndName, timeType, date);
        }
        return this.mContext.getString(R.string.zone_info_footer_no_dst, new Object[]{offsetAndName});
    }

    private TimeZoneTransition findNextDstTransition(TimeZone timeZone) {
        if (!(timeZone instanceof OlsonTimeZone)) {
            return null;
        }
        OlsonTimeZone olsonTimeZone = (OlsonTimeZone) timeZone;
        TimeZoneTransition transition = olsonTimeZone.getNextTransition(this.mDate.getTime(), false);
        while (transition.getTo().getDSTSavings() == transition.getFrom().getDSTSavings()) {
            transition = olsonTimeZone.getNextTransition(transition.getTime(), false);
            if (transition == null) {
                break;
            }
        }
        return transition;
    }
}
