package com.android.settings.datetime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.core.instrumentation.VisibilityLoggerMixin;
import com.android.settingslib.datetime.ZoneGetter;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class ZonePicker extends ListFragment implements Instrumentable {
    private static final int MENU_ALPHABETICAL = 1;
    private static final int MENU_TIMEZONE = 2;
    private SimpleAdapter mAlphabeticalAdapter;
    private boolean mSortedByTimezone;
    private SimpleAdapter mTimezoneSortedAdapter;
    private VisibilityLoggerMixin mVisibilityLoggerMixin;

    @VisibleForTesting
    static class MyComparator implements Comparator<Map<?, ?>> {
        private final Collator mCollator = Collator.getInstance();
        private boolean mSortedByName;
        private String mSortingKey;

        public MyComparator(String sortingKey) {
            this.mSortingKey = sortingKey;
            this.mSortedByName = ZoneGetter.KEY_DISPLAY_LABEL.equals(sortingKey);
        }

        public void setSortingKey(String sortingKey) {
            this.mSortingKey = sortingKey;
            this.mSortedByName = ZoneGetter.KEY_DISPLAY_LABEL.equals(sortingKey);
        }

        public int compare(Map<?, ?> map1, Map<?, ?> map2) {
            Object value1 = map1.get(this.mSortingKey);
            Object value2 = map2.get(this.mSortingKey);
            if (!isComparable(value1)) {
                return isComparable(value2);
            }
            if (!isComparable(value2)) {
                return -1;
            }
            if (this.mSortedByName) {
                return this.mCollator.compare(value1, value2);
            }
            return ((Comparable) value1).compareTo(value2);
        }

        private boolean isComparable(Object value) {
            return value != null && (value instanceof Comparable);
        }
    }

    private static class TimeZoneViewBinder implements ViewBinder {
        private TimeZoneViewBinder() {
        }

        public boolean setViewValue(View view, Object data, String textRepresentation) {
            ((TextView) view).setText((CharSequence) data);
            return true;
        }
    }

    public static SimpleAdapter constructTimezoneAdapter(Context context, boolean sortedByName) {
        return constructTimezoneAdapter(context, sortedByName, R.layout.date_time_custom_list_item_2);
    }

    public static SimpleAdapter constructTimezoneAdapter(Context context, boolean sortedByName, int layoutId) {
        String sortKey;
        String[] from = new String[]{ZoneGetter.KEY_DISPLAY_LABEL, ZoneGetter.KEY_OFFSET_LABEL};
        int[] to = new int[]{16908308, 16908309};
        if (sortedByName) {
            sortKey = ZoneGetter.KEY_DISPLAY_LABEL;
        } else {
            sortKey = ZoneGetter.KEY_OFFSET;
        }
        MyComparator comparator = new MyComparator(sortKey);
        List<Map<String, Object>> sortedList = ZoneGetter.getZonesList(context);
        Collections.sort(sortedList, comparator);
        SimpleAdapter adapter = new SimpleAdapter(context, sortedList, layoutId, from, to);
        adapter.setViewBinder(new TimeZoneViewBinder());
        return adapter;
    }

    public static int getTimeZoneIndex(SimpleAdapter adapter, TimeZone tz) {
        String defaultId = tz.getID();
        int listSize = adapter.getCount();
        for (int i = 0; i < listSize; i++) {
            if (defaultId.equals((String) ((HashMap) adapter.getItem(i)).get(ZoneGetter.KEY_ID))) {
                return i;
            }
        }
        return -1;
    }

    public int getMetricsCategory() {
        return 515;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        this.mTimezoneSortedAdapter = constructTimezoneAdapter(activity, false);
        this.mAlphabeticalAdapter = constructTimezoneAdapter(activity, true);
        setSorting(true);
        setHasOptionsMenu(true);
        activity.setTitle(R.string.date_time_set_timezone);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mVisibilityLoggerMixin = new VisibilityLoggerMixin(getMetricsCategory(), FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        prepareCustomPreferencesList((ListView) view.findViewById(16908298));
        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, R.string.zone_list_menu_sort_alphabetically).setIcon(17301660);
        menu.add(0, 2, 0, R.string.zone_list_menu_sort_by_timezone).setIcon(R.drawable.ic_menu_3d_globe);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (this.mSortedByTimezone) {
            menu.findItem(2).setVisible(false);
            menu.findItem(1).setVisible(true);
            return;
        }
        menu.findItem(2).setVisible(true);
        menu.findItem(1).setVisible(false);
    }

    public void onResume() {
        super.onResume();
        this.mVisibilityLoggerMixin.onResume();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                setSorting(false);
                return true;
            case 2:
                setSorting(true);
                return true;
            default:
                return false;
        }
    }

    static void prepareCustomPreferencesList(ListView list) {
        list.setScrollBarStyle(33554432);
        list.setClipToPadding(false);
        list.setDivider(null);
    }

    private void setSorting(boolean sortByTimezone) {
        SimpleAdapter adapter = sortByTimezone ? this.mTimezoneSortedAdapter : this.mAlphabeticalAdapter;
        setListAdapter(adapter);
        this.mSortedByTimezone = sortByTimezone;
        int defaultIndex = getTimeZoneIndex(adapter, TimeZone.getDefault());
        if (defaultIndex >= 0) {
            setSelection(defaultIndex);
        }
    }

    public void onListItemClick(ListView listView, View v, int position, long id) {
        if (isResumed()) {
            ((AlarmManager) getActivity().getSystemService(NotificationCompat.CATEGORY_ALARM)).setTimeZone((String) ((Map) listView.getItemAtPosition(position)).get(ZoneGetter.KEY_ID));
            getActivity().onBackPressed();
        }
    }

    public void onPause() {
        super.onPause();
        this.mVisibilityLoggerMixin.onPause();
    }
}
