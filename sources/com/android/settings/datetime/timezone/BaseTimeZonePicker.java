package com.android.settings.datetime.timezone;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.datetime.timezone.BaseTimeZoneAdapter.AdapterItem;
import com.android.settings.datetime.timezone.model.TimeZoneData;
import com.android.settings.datetime.timezone.model.TimeZoneDataLoader.LoaderCreator;
import com.oneplus.lib.widget.SearchView;
import com.oneplus.lib.widget.SearchView.OnQueryTextListener;
import com.oneplus.settings.utils.OPUtils;
import java.util.Locale;

public abstract class BaseTimeZonePicker extends InstrumentedFragment implements OnQueryTextListener {
    public static final String EXTRA_RESULT_REGION_ID = "com.android.settings.datetime.timezone.result_region_id";
    public static final String EXTRA_RESULT_TIME_ZONE_ID = "com.android.settings.datetime.timezone.result_time_zone_id";
    private BaseTimeZoneAdapter mAdapter;
    private final boolean mDefaultExpandSearch;
    protected Locale mLocale;
    private RecyclerView mRecyclerView;
    private final boolean mSearchEnabled;
    private final int mSearchHintResId;
    private SearchView mSearchView;
    private TimeZoneData mTimeZoneData;
    private final int mTitleResId;

    public interface OnListItemClickListener<T extends AdapterItem> {
        void onListItemClick(T t);
    }

    public abstract BaseTimeZoneAdapter createAdapter(TimeZoneData timeZoneData);

    protected BaseTimeZonePicker(int titleResId, int searchHintResId, boolean searchEnabled, boolean defaultExpandSearch) {
        this.mTitleResId = titleResId;
        this.mSearchHintResId = searchHintResId;
        this.mSearchEnabled = searchEnabled;
        this.mDefaultExpandSearch = defaultExpandSearch;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(this.mTitleResId);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_zone_items_list, container, false);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), 1, false));
        this.mRecyclerView.setAdapter(this.mAdapter);
        getLoaderManager().initLoader(0, null, new LoaderCreator(getContext(), new -$$Lambda$MBKbnic3yruONZHLQGUj0vAB5hk(this)));
        return view;
    }

    public void onTimeZoneDataReady(TimeZoneData timeZoneData) {
        if (this.mTimeZoneData == null && timeZoneData != null) {
            this.mTimeZoneData = timeZoneData;
            this.mAdapter = createAdapter(this.mTimeZoneData);
            if (this.mRecyclerView != null) {
                this.mRecyclerView.setAdapter(this.mAdapter);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Locale getLocale() {
        return getContext().getResources().getConfiguration().getLocales().get(0);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mSearchEnabled) {
            inflater.inflate(R.menu.time_zone_base_search_menu, menu);
            MenuItem searchMenuItem = menu.findItem(R.id.time_zone_search_menu);
            this.mSearchView = (SearchView) searchMenuItem.getActionView();
            this.mSearchView.setQueryHint(getText(this.mSearchHintResId));
            this.mSearchView.setOnQueryTextListener(this);
            if (this.mDefaultExpandSearch) {
                searchMenuItem.expandActionView();
                this.mSearchView.setIconified(false);
                this.mSearchView.setActivated(true);
                this.mSearchView.setQuery("", true);
            }
            TextView textView = (TextView) this.mSearchView.findViewById(this.mSearchView.getContext().getResources().getIdentifier("com.android.settings:id/search_src_text", null, null));
            if (OPUtils.isWhiteModeOn(getActivity().getContentResolver())) {
                textView.setTextColor(getActivity().getResources().getColor(R.color.oneplus__text_color_primary_light));
                textView.setHintTextColor(getActivity().getResources().getColor(R.color.oneplus__text_color_hint_light));
                return;
            }
            textView.setTextColor(getActivity().getResources().getColor(R.color.oneplus__text_color_primary_dark));
            textView.setHintTextColor(getActivity().getResources().getColor(R.color.oneplus__text_color_hint_dark));
        }
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        if (this.mAdapter != null) {
            this.mAdapter.getFilter().filter(newText);
        }
        return false;
    }
}
