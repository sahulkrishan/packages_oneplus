package com.android.settings.datetime.timezone.model;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import libcore.util.CountryTimeZones;
import libcore.util.CountryTimeZones.TimeZoneMapping;

public class FilteredCountryTimeZones {
    private static final long MIN_USE_DATE_OF_TIMEZONE = 1514764800000L;
    private final CountryTimeZones mCountryTimeZones;
    private final List<String> mTimeZoneIds;

    public FilteredCountryTimeZones(CountryTimeZones countryTimeZones) {
        this.mCountryTimeZones = countryTimeZones;
        this.mTimeZoneIds = Collections.unmodifiableList((List) countryTimeZones.getTimeZoneMappings().stream().filter(-$$Lambda$FilteredCountryTimeZones$4MxYnMuZMfSQu2iAD-J0AM_CAoE.INSTANCE).map(-$$Lambda$FilteredCountryTimeZones$ISUVeCzEqV6U2C82Sgby5UdDf3Y.INSTANCE).collect(Collectors.toList()));
    }

    static /* synthetic */ boolean lambda$new$0(TimeZoneMapping timeZoneMapping) {
        return timeZoneMapping.showInPicker && (timeZoneMapping.notUsedAfter == null || timeZoneMapping.notUsedAfter.longValue() >= MIN_USE_DATE_OF_TIMEZONE);
    }

    public List<String> getTimeZoneIds() {
        return this.mTimeZoneIds;
    }

    public CountryTimeZones getCountryTimeZones() {
        return this.mCountryTimeZones;
    }

    public String getRegionId() {
        return TimeZoneData.normalizeRegionId(this.mCountryTimeZones.getCountryIso());
    }
}
