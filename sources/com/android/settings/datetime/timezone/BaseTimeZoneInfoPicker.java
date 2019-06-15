package com.android.settings.datetime.timezone;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.annotation.Nullable;
import com.android.settings.R;
import com.android.settings.datetime.timezone.BaseTimeZoneAdapter.AdapterItem;
import com.android.settings.datetime.timezone.BaseTimeZonePicker.OnListItemClickListener;
import com.android.settings.datetime.timezone.model.TimeZoneData;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class BaseTimeZoneInfoPicker extends BaseTimeZonePicker {
    protected static final String TAG = "RegionZoneSearchPicker";
    protected ZoneAdapter mAdapter;

    private static class TimeZoneInfoItem implements AdapterItem {
        private final long mItemId;
        private final Resources mResources;
        private final String[] mSearchKeys;
        private final DateFormat mTimeFormat;
        private final TimeZoneInfo mTimeZoneInfo;
        private final String mTitle;

        private TimeZoneInfoItem(long itemId, TimeZoneInfo timeZoneInfo, Resources resources, DateFormat timeFormat) {
            this.mItemId = itemId;
            this.mTimeZoneInfo = timeZoneInfo;
            this.mResources = resources;
            this.mTimeFormat = timeFormat;
            this.mTitle = createTitle(timeZoneInfo);
            this.mSearchKeys = new String[]{this.mTitle};
        }

        private static String createTitle(TimeZoneInfo timeZoneInfo) {
            String name = timeZoneInfo.getExemplarLocation();
            if (name == null) {
                name = timeZoneInfo.getGenericName();
            }
            if (name == null && timeZoneInfo.getTimeZone().inDaylightTime(new Date())) {
                name = timeZoneInfo.getDaylightName();
            }
            if (name == null) {
                name = timeZoneInfo.getStandardName();
            }
            if (name == null) {
                return String.valueOf(timeZoneInfo.getGmtOffset());
            }
            return name;
        }

        public CharSequence getTitle() {
            return this.mTitle;
        }

        public CharSequence getSummary() {
            String name = this.mTimeZoneInfo.getGenericName();
            if (name == null) {
                if (this.mTimeZoneInfo.getTimeZone().inDaylightTime(new Date())) {
                    name = this.mTimeZoneInfo.getDaylightName();
                } else {
                    name = this.mTimeZoneInfo.getStandardName();
                }
            }
            if (name == null || name.equals(this.mTitle)) {
                CharSequence gmtOffset = this.mTimeZoneInfo.getGmtOffset();
                CharSequence charSequence = (gmtOffset == null || gmtOffset.toString().equals(this.mTitle)) ? "" : gmtOffset;
                return charSequence;
            }
            return SpannableUtil.getResourcesText(this.mResources, R.string.zone_info_offset_and_name, this.mTimeZoneInfo.getGmtOffset(), name);
        }

        public String getIconText() {
            return null;
        }

        public String getCurrentTime() {
            return this.mTimeFormat.format(Calendar.getInstance(this.mTimeZoneInfo.getTimeZone()));
        }

        public long getItemId() {
            return this.mItemId;
        }

        public String[] getSearchKeys() {
            return this.mSearchKeys;
        }
    }

    protected static class ZoneAdapter extends BaseTimeZoneAdapter<TimeZoneInfoItem> {
        public ZoneAdapter(Context context, List<TimeZoneInfo> timeZones, OnListItemClickListener<TimeZoneInfoItem> onListItemClickListener, Locale locale, CharSequence headerText) {
            super(createTimeZoneInfoItems(context, timeZones, locale), onListItemClickListener, locale, true, headerText);
        }

        private static List<TimeZoneInfoItem> createTimeZoneInfoItems(Context context, List<TimeZoneInfo> timeZones, Locale locale) {
            DateFormat currentTimeFormat = new SimpleDateFormat(android.text.format.DateFormat.getTimeFormatString(context), locale);
            ArrayList<TimeZoneInfoItem> results = new ArrayList(timeZones.size());
            Resources resources = context.getResources();
            long i = 0;
            for (TimeZoneInfo timeZone : timeZones) {
                long i2 = i + 1;
                results.add(new TimeZoneInfoItem(i, timeZone, resources, currentTimeFormat));
                i = i2;
            }
            return results;
        }
    }

    public abstract List<TimeZoneInfo> getAllTimeZoneInfos(TimeZoneData timeZoneData);

    protected BaseTimeZoneInfoPicker(int titleResId, int searchHintResId, boolean searchEnabled, boolean defaultExpandSearch) {
        super(titleResId, searchHintResId, searchEnabled, defaultExpandSearch);
    }

    /* Access modifiers changed, original: protected */
    public BaseTimeZoneAdapter createAdapter(TimeZoneData timeZoneData) {
        this.mAdapter = new ZoneAdapter(getContext(), getAllTimeZoneInfos(timeZoneData), new -$$Lambda$BaseTimeZoneInfoPicker$rmIiAzryW5v4Oz5tFaKKhXINMbA(this), getLocale(), getHeaderText());
        return this.mAdapter;
    }

    /* Access modifiers changed, original: protected */
    @Nullable
    public CharSequence getHeaderText() {
        return null;
    }

    private void onListItemClick(TimeZoneInfoItem item) {
        getActivity().setResult(-1, prepareResultData(item.mTimeZoneInfo));
        getActivity().finish();
    }

    /* Access modifiers changed, original: protected */
    public Intent prepareResultData(TimeZoneInfo selectedTimeZoneInfo) {
        return new Intent().putExtra(BaseTimeZonePicker.EXTRA_RESULT_TIME_ZONE_ID, selectedTimeZoneInfo.getId());
    }
}
