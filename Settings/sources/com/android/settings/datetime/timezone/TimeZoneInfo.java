package com.android.settings.datetime.timezone;

import android.icu.text.TimeZoneFormat;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.TimeZone;
import android.text.TextUtils;
import com.android.settingslib.datetime.ZoneGetter;
import java.util.Date;
import java.util.Locale;

public class TimeZoneInfo {
    private final String mDaylightName;
    private final String mExemplarLocation;
    private final String mGenericName;
    private final CharSequence mGmtOffset;
    private final String mId = this.mTimeZone.getID();
    private final String mStandardName;
    private final TimeZone mTimeZone;

    public static class Builder {
        private String mDaylightName;
        private String mExemplarLocation;
        private String mGenericName;
        private CharSequence mGmtOffset;
        private String mStandardName;
        private final TimeZone mTimeZone;

        public Builder(TimeZone timeZone) {
            if (timeZone != null) {
                this.mTimeZone = timeZone;
                return;
            }
            throw new IllegalArgumentException("TimeZone must not be null!");
        }

        public Builder setGenericName(String genericName) {
            this.mGenericName = genericName;
            return this;
        }

        public Builder setStandardName(String standardName) {
            this.mStandardName = standardName;
            return this;
        }

        public Builder setDaylightName(String daylightName) {
            this.mDaylightName = daylightName;
            return this;
        }

        public Builder setExemplarLocation(String exemplarLocation) {
            this.mExemplarLocation = exemplarLocation;
            return this;
        }

        public Builder setGmtOffset(CharSequence gmtOffset) {
            this.mGmtOffset = gmtOffset;
            return this;
        }

        public TimeZoneInfo build() {
            if (!TextUtils.isEmpty(this.mGmtOffset)) {
                return new TimeZoneInfo(this);
            }
            throw new IllegalStateException("gmtOffset must not be empty!");
        }
    }

    public static class Formatter {
        private final Locale mLocale;
        private final Date mNow;
        private final TimeZoneFormat mTimeZoneFormat;

        public Formatter(Locale locale, Date now) {
            this.mLocale = locale;
            this.mNow = now;
            this.mTimeZoneFormat = TimeZoneFormat.getInstance(locale);
        }

        public TimeZoneInfo format(String timeZoneId) {
            return format(TimeZone.getFrozenTimeZone(timeZoneId));
        }

        public TimeZoneInfo format(TimeZone timeZone) {
            String id = timeZone.getID();
            TimeZoneNames timeZoneNames = this.mTimeZoneFormat.getTimeZoneNames();
            return new Builder(timeZone).setGenericName(timeZoneNames.getDisplayName(id, NameType.LONG_GENERIC, this.mNow.getTime())).setStandardName(timeZoneNames.getDisplayName(id, NameType.LONG_STANDARD, this.mNow.getTime())).setDaylightName(timeZoneNames.getDisplayName(id, NameType.LONG_DAYLIGHT, this.mNow.getTime())).setExemplarLocation(timeZoneNames.getExemplarLocationName(id)).setGmtOffset(ZoneGetter.getGmtOffsetText(this.mTimeZoneFormat, this.mLocale, java.util.TimeZone.getTimeZone(id), this.mNow)).build();
        }
    }

    public TimeZoneInfo(Builder builder) {
        this.mTimeZone = builder.mTimeZone;
        this.mGenericName = builder.mGenericName;
        this.mStandardName = builder.mStandardName;
        this.mDaylightName = builder.mDaylightName;
        this.mExemplarLocation = builder.mExemplarLocation;
        this.mGmtOffset = builder.mGmtOffset;
    }

    public String getId() {
        return this.mId;
    }

    public TimeZone getTimeZone() {
        return this.mTimeZone;
    }

    public String getExemplarLocation() {
        return this.mExemplarLocation;
    }

    public String getGenericName() {
        return this.mGenericName;
    }

    public String getStandardName() {
        return this.mStandardName;
    }

    public String getDaylightName() {
        return this.mDaylightName;
    }

    public CharSequence getGmtOffset() {
        return this.mGmtOffset;
    }
}
