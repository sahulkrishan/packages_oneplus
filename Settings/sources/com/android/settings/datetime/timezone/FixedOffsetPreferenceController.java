package com.android.settings.datetime.timezone;

import android.content.Context;
import com.android.settings.R;

public class FixedOffsetPreferenceController extends BaseTimeZonePreferenceController {
    private static final String PREFERENCE_KEY = "fixed_offset";
    private TimeZoneInfo mTimeZoneInfo;

    public FixedOffsetPreferenceController(Context context) {
        super(context, PREFERENCE_KEY);
    }

    public CharSequence getSummary() {
        if (this.mTimeZoneInfo == null) {
            return "";
        }
        if (this.mTimeZoneInfo.getStandardName() == null) {
            return this.mTimeZoneInfo.getGmtOffset();
        }
        return SpannableUtil.getResourcesText(this.mContext.getResources(), R.string.zone_info_offset_and_name, this.mTimeZoneInfo.getGmtOffset(), standardName);
    }

    public void setTimeZoneInfo(TimeZoneInfo timeZoneInfo) {
        this.mTimeZoneInfo = timeZoneInfo;
    }

    public TimeZoneInfo getTimeZoneInfo() {
        return this.mTimeZoneInfo;
    }
}
