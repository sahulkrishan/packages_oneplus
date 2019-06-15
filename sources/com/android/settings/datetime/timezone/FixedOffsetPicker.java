package com.android.settings.datetime.timezone;

import android.icu.util.TimeZone;
import com.android.settings.R;
import com.android.settings.datetime.timezone.TimeZoneInfo.Formatter;
import com.android.settings.datetime.timezone.model.TimeZoneData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FixedOffsetPicker extends BaseTimeZoneInfoPicker {
    private static final int MAX_HOURS_OFFSET = 12;
    private static final int MIN_HOURS_OFFSET = -14;

    public FixedOffsetPicker() {
        super(R.string.date_time_select_fixed_offset_time_zones, R.string.search_settings, false, false);
    }

    public int getMetricsCategory() {
        return 1357;
    }

    public List<TimeZoneInfo> getAllTimeZoneInfos(TimeZoneData timeZoneData) {
        return loadFixedOffsets();
    }

    private List<TimeZoneInfo> loadFixedOffsets() {
        Formatter formatter = new Formatter(getLocale(), new Date());
        List<TimeZoneInfo> timeZoneInfos = new ArrayList();
        timeZoneInfos.add(formatter.format(TimeZone.getFrozenTimeZone("Etc/UTC")));
        for (int hoursOffset = 12; hoursOffset >= MIN_HOURS_OFFSET; hoursOffset--) {
            if (hoursOffset != 0) {
                timeZoneInfos.add(formatter.format(TimeZone.getFrozenTimeZone(String.format(Locale.US, "Etc/GMT%+d", new Object[]{Integer.valueOf(hoursOffset)}))));
            }
        }
        return Collections.unmodifiableList(timeZoneInfos);
    }
}
