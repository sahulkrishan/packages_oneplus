package com.android.settings.datetime.timezone.model;

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArraySet;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import libcore.util.CountryTimeZones;
import libcore.util.CountryZonesFinder;
import libcore.util.TimeZoneFinder;

public class TimeZoneData {
    private static WeakReference<TimeZoneData> sCache = null;
    private final CountryZonesFinder mCountryZonesFinder;
    private final Set<String> mRegionIds = getNormalizedRegionIds(this.mCountryZonesFinder.lookupAllCountryIsoCodes());

    public static synchronized TimeZoneData getInstance() {
        synchronized (TimeZoneData.class) {
            TimeZoneData data = sCache == null ? null : (TimeZoneData) sCache.get();
            if (data != null) {
                return data;
            }
            data = new TimeZoneData(TimeZoneFinder.getInstance().getCountryZonesFinder());
            sCache = new WeakReference(data);
            return data;
        }
    }

    @VisibleForTesting
    public TimeZoneData(CountryZonesFinder countryZonesFinder) {
        this.mCountryZonesFinder = countryZonesFinder;
    }

    public Set<String> getRegionIds() {
        return this.mRegionIds;
    }

    public Set<String> lookupCountryCodesForZoneId(String tzId) {
        if (tzId == null) {
            return Collections.emptySet();
        }
        List<CountryTimeZones> countryTimeZones = this.mCountryZonesFinder.lookupCountryTimeZonesForZoneId(tzId);
        Set<String> regionIds = new ArraySet();
        for (CountryTimeZones countryTimeZone : countryTimeZones) {
            FilteredCountryTimeZones filteredZones = new FilteredCountryTimeZones(countryTimeZone);
            if (filteredZones.getTimeZoneIds().contains(tzId)) {
                regionIds.add(filteredZones.getRegionId());
            }
        }
        return regionIds;
    }

    public FilteredCountryTimeZones lookupCountryTimeZones(String regionId) {
        CountryTimeZones finder = regionId == null ? null : this.mCountryZonesFinder.lookupCountryTimeZones(regionId);
        if (finder == null) {
            return null;
        }
        return new FilteredCountryTimeZones(finder);
    }

    private static Set<String> getNormalizedRegionIds(List<String> regionIds) {
        Set<String> result = new HashSet(regionIds.size());
        for (String regionId : regionIds) {
            result.add(normalizeRegionId(regionId));
        }
        return Collections.unmodifiableSet(result);
    }

    public static String normalizeRegionId(String regionId) {
        return regionId == null ? null : regionId.toUpperCase(Locale.US);
    }
}
