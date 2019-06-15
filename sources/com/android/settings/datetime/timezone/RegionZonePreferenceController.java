package com.android.settings.datetime.timezone;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;

public class RegionZonePreferenceController extends BaseTimeZonePreferenceController {
    private static final String PREFERENCE_KEY = "region_zone";
    private boolean mIsClickable;
    private TimeZoneInfo mTimeZoneInfo;

    public RegionZonePreferenceController(Context context) {
        super(context, PREFERENCE_KEY);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(isClickable());
    }

    public CharSequence getSummary() {
        if (this.mTimeZoneInfo == null) {
            return "";
        }
        return SpannableUtil.getResourcesText(this.mContext.getResources(), R.string.zone_info_exemplar_location_and_offset, this.mTimeZoneInfo.getExemplarLocation(), this.mTimeZoneInfo.getGmtOffset());
    }

    public void setTimeZoneInfo(TimeZoneInfo timeZoneInfo) {
        this.mTimeZoneInfo = timeZoneInfo;
    }

    public TimeZoneInfo getTimeZoneInfo() {
        return this.mTimeZoneInfo;
    }

    public void setClickable(boolean clickable) {
        this.mIsClickable = clickable;
    }

    public boolean isClickable() {
        return this.mIsClickable;
    }
}
