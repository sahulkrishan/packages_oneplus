package com.android.settings.datetime.timezone;

import android.content.Intent;
import android.icu.text.Collator;
import android.icu.text.LocaleDisplayNames;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.datetime.timezone.TimeZoneInfo.Formatter;
import com.android.settings.datetime.timezone.model.FilteredCountryTimeZones;
import com.android.settings.datetime.timezone.model.TimeZoneData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

public class RegionZonePicker extends BaseTimeZoneInfoPicker {
    public static final String EXTRA_REGION_ID = "com.android.settings.datetime.timezone.region_id";
    @Nullable
    private String mRegionName;

    @VisibleForTesting
    static class TimeZoneInfoComparator implements Comparator<TimeZoneInfo> {
        private Collator mCollator;
        private final Date mNow;

        @VisibleForTesting
        TimeZoneInfoComparator(Collator collator, Date now) {
            this.mCollator = collator;
            this.mNow = now;
        }

        public int compare(TimeZoneInfo tzi1, TimeZoneInfo tzi2) {
            int result = Integer.compare(tzi1.getTimeZone().getOffset(this.mNow.getTime()), tzi2.getTimeZone().getOffset(this.mNow.getTime()));
            if (result == 0) {
                result = Integer.compare(tzi1.getTimeZone().getRawOffset(), tzi2.getTimeZone().getRawOffset());
            }
            if (result == 0) {
                result = this.mCollator.compare(tzi1.getExemplarLocation(), tzi2.getExemplarLocation());
            }
            if (result != 0 || tzi1.getGenericName() == null || tzi2.getGenericName() == null) {
                return result;
            }
            return this.mCollator.compare(tzi1.getGenericName(), tzi2.getGenericName());
        }
    }

    public RegionZonePicker() {
        super(R.string.date_time_set_timezone_title, R.string.search_settings, true, false);
    }

    public int getMetricsCategory() {
        return 1356;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleDisplayNames localeDisplayNames = LocaleDisplayNames.getInstance(getLocale());
        String str = null;
        String regionId = getArguments() == null ? null : getArguments().getString(EXTRA_REGION_ID);
        if (regionId != null) {
            str = localeDisplayNames.regionDisplayName(regionId);
        }
        this.mRegionName = str;
    }

    /* Access modifiers changed, original: protected */
    @Nullable
    public CharSequence getHeaderText() {
        return this.mRegionName;
    }

    /* Access modifiers changed, original: protected */
    public Intent prepareResultData(TimeZoneInfo selectedTimeZoneInfo) {
        Intent intent = super.prepareResultData(selectedTimeZoneInfo);
        intent.putExtra(BaseTimeZonePicker.EXTRA_RESULT_REGION_ID, getArguments().getString(EXTRA_REGION_ID));
        return intent;
    }

    public List<TimeZoneInfo> getAllTimeZoneInfos(TimeZoneData timeZoneData) {
        if (getArguments() == null) {
            Log.e("RegionZoneSearchPicker", "getArguments() == null");
            getActivity().finish();
            return Collections.emptyList();
        }
        String regionId = getArguments().getString(EXTRA_REGION_ID);
        FilteredCountryTimeZones filteredCountryTimeZones = timeZoneData.lookupCountryTimeZones(regionId);
        if (filteredCountryTimeZones != null) {
            return getRegionTimeZoneInfo(filteredCountryTimeZones.getTimeZoneIds());
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("region id is not valid: ");
        stringBuilder.append(regionId);
        Log.e("RegionZoneSearchPicker", stringBuilder.toString());
        getActivity().finish();
        return Collections.emptyList();
    }

    public List<TimeZoneInfo> getRegionTimeZoneInfo(Collection<String> timeZoneIds) {
        Formatter formatter = new Formatter(getLocale(), new Date());
        TreeSet<TimeZoneInfo> timeZoneInfos = new TreeSet(new TimeZoneInfoComparator(Collator.getInstance(getLocale()), new Date()));
        for (String timeZoneId : timeZoneIds) {
            TimeZone timeZone = TimeZone.getFrozenTimeZone(timeZoneId);
            if (!timeZone.getID().equals("Etc/Unknown")) {
                timeZoneInfos.add(formatter.format(timeZone));
            }
        }
        return Collections.unmodifiableList(new ArrayList(timeZoneInfos));
    }
}
