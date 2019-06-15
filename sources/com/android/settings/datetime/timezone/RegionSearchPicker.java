package com.android.settings.datetime.timezone;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.Collator;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.datetime.timezone.BaseTimeZoneAdapter.AdapterItem;
import com.android.settings.datetime.timezone.model.FilteredCountryTimeZones;
import com.android.settings.datetime.timezone.model.TimeZoneData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class RegionSearchPicker extends BaseTimeZonePicker {
    private static final int REQUEST_CODE_ZONE_PICKER = 1;
    private static final String TAG = "RegionSearchPicker";
    private BaseTimeZoneAdapter<RegionItem> mAdapter;
    private TimeZoneData mTimeZoneData;

    private static class RegionInfoComparator implements Comparator<RegionItem> {
        private final Collator mCollator;

        RegionInfoComparator(Collator collator) {
            this.mCollator = collator;
        }

        public int compare(RegionItem r1, RegionItem r2) {
            return this.mCollator.compare(r1.getTitle(), r2.getTitle());
        }
    }

    @VisibleForTesting
    static class RegionItem implements AdapterItem {
        private final String mId;
        private final long mItemId;
        private final String mName;
        private final String[] mSearchKeys = new String[]{this.mId, this.mName};

        RegionItem(long itemId, String id, String name) {
            this.mId = id;
            this.mName = name;
            this.mItemId = itemId;
        }

        public String getId() {
            return this.mId;
        }

        public CharSequence getTitle() {
            return this.mName;
        }

        public CharSequence getSummary() {
            return null;
        }

        public String getIconText() {
            return null;
        }

        public String getCurrentTime() {
            return null;
        }

        public long getItemId() {
            return this.mItemId;
        }

        public String[] getSearchKeys() {
            return this.mSearchKeys;
        }
    }

    public RegionSearchPicker() {
        super(R.string.date_time_select_region, R.string.date_time_search_region, true, true);
    }

    public int getMetricsCategory() {
        return 1355;
    }

    /* Access modifiers changed, original: protected */
    public BaseTimeZoneAdapter createAdapter(TimeZoneData timeZoneData) {
        this.mTimeZoneData = timeZoneData;
        this.mAdapter = new BaseTimeZoneAdapter(createAdapterItem(timeZoneData.getRegionIds()), new -$$Lambda$RegionSearchPicker$DOJaHroZb7JziN-vdZ6PwdoM4gg(this), getLocale(), false, null);
        return this.mAdapter;
    }

    private void onListItemClick(RegionItem item) {
        String regionId = item.getId();
        FilteredCountryTimeZones countryTimeZones = this.mTimeZoneData.lookupCountryTimeZones(regionId);
        Activity activity = getActivity();
        if (countryTimeZones == null || countryTimeZones.getTimeZoneIds().isEmpty()) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Region has no time zones: ");
            stringBuilder.append(regionId);
            Log.e(str, stringBuilder.toString());
            activity.setResult(0);
            activity.finish();
            return;
        }
        List<String> timeZoneIds = countryTimeZones.getTimeZoneIds();
        if (timeZoneIds.size() == 1) {
            getActivity().setResult(-1, new Intent().putExtra(BaseTimeZonePicker.EXTRA_RESULT_REGION_ID, regionId).putExtra(BaseTimeZonePicker.EXTRA_RESULT_TIME_ZONE_ID, (String) timeZoneIds.get(0)));
            getActivity().finish();
        } else {
            Bundle args = new Bundle();
            args.putString(RegionZonePicker.EXTRA_REGION_ID, regionId);
            new SubSettingLauncher(getContext()).setDestination(RegionZonePicker.class.getCanonicalName()).setArguments(args).setSourceMetricsCategory(getMetricsCategory()).setResultListener(this, 1).launch();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                getActivity().setResult(-1, data);
            }
            getActivity().finish();
        }
    }

    private List<RegionItem> createAdapterItem(Set<String> regionIds) {
        TreeSet<RegionItem> items = new TreeSet(new RegionInfoComparator(Collator.getInstance(getLocale())));
        LocaleDisplayNames localeDisplayNames = LocaleDisplayNames.getInstance(getLocale());
        long i = 0;
        for (String regionId : regionIds) {
            long i2 = 1 + i;
            items.add(new RegionItem(i, regionId, localeDisplayNames.regionDisplayName(regionId)));
            i = i2;
        }
        return new ArrayList(items);
    }
}
