package com.android.settings.datetime.timezone;

import android.icu.text.BreakIterator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filter.FilterResults;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.datetime.timezone.BaseTimeZonePicker.OnListItemClickListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaseTimeZoneAdapter<T extends AdapterItem> extends Adapter<ViewHolder> {
    @VisibleForTesting
    static final int TYPE_HEADER = 0;
    @VisibleForTesting
    static final int TYPE_ITEM = 1;
    private ArrayFilter mFilter;
    private final CharSequence mHeaderText;
    private List<T> mItems;
    private final Locale mLocale;
    private final OnListItemClickListener<T> mOnListItemClickListener;
    private final List<T> mOriginalItems;
    private final boolean mShowHeader;
    private final boolean mShowItemSummary;

    public interface AdapterItem {
        String getCurrentTime();

        String getIconText();

        long getItemId();

        String[] getSearchKeys();

        CharSequence getSummary();

        CharSequence getTitle();
    }

    @VisibleForTesting
    public class ArrayFilter extends Filter {
        private BreakIterator mBreakIterator = BreakIterator.getWordInstance(BaseTimeZoneAdapter.this.mLocale);

        /* Access modifiers changed, original: protected */
        @WorkerThread
        public FilterResults performFiltering(CharSequence prefix) {
            List<T> newItems;
            if (TextUtils.isEmpty(prefix)) {
                newItems = BaseTimeZoneAdapter.this.mOriginalItems;
            } else {
                String prefixString = prefix.toString().toLowerCase(BaseTimeZoneAdapter.this.mLocale);
                List<T> newItems2 = new ArrayList();
                for (AdapterItem item : BaseTimeZoneAdapter.this.mOriginalItems) {
                    for (String searchKey : item.getSearchKeys()) {
                        String searchKey2 = searchKey2.toLowerCase(BaseTimeZoneAdapter.this.mLocale);
                        if (searchKey2.startsWith(prefixString)) {
                            newItems2.add(item);
                            break;
                        }
                        this.mBreakIterator.setText(searchKey2);
                        int wordStart = 0;
                        int wordLimit = this.mBreakIterator.next();
                        while (wordLimit != -1) {
                            if (this.mBreakIterator.getRuleStatus() != 0 && searchKey2.startsWith(prefixString, wordStart)) {
                                newItems2.add(item);
                                break;
                            }
                            wordStart = wordLimit;
                            wordLimit = this.mBreakIterator.next();
                        }
                    }
                }
                newItems = newItems2;
            }
            FilterResults results = new FilterResults();
            results.values = newItems;
            results.count = newItems.size();
            return results;
        }

        @VisibleForTesting
        public void publishResults(CharSequence constraint, FilterResults results) {
            BaseTimeZoneAdapter.this.mItems = (List) results.values;
            BaseTimeZoneAdapter.this.notifyDataSetChanged();
        }
    }

    private static class HeaderViewHolder extends ViewHolder {
        private final TextView mTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            this.mTextView = (TextView) itemView.findViewById(16908310);
        }

        public void setText(CharSequence text) {
            this.mTextView.setText(text);
        }
    }

    @VisibleForTesting
    public static class ItemViewHolder<T extends AdapterItem> extends ViewHolder implements OnClickListener {
        final TextView mIconTextView;
        private T mItem;
        final OnListItemClickListener<T> mOnListItemClickListener;
        final View mSummaryFrame;
        final TextView mSummaryView;
        final TextView mTimeView;
        final TextView mTitleView;

        public ItemViewHolder(View itemView, OnListItemClickListener<T> onListItemClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.mSummaryFrame = itemView.findViewById(R.id.summary_frame);
            this.mTitleView = (TextView) itemView.findViewById(16908310);
            this.mIconTextView = (TextView) itemView.findViewById(R.id.icon_text);
            this.mSummaryView = (TextView) itemView.findViewById(16908304);
            this.mTimeView = (TextView) itemView.findViewById(R.id.current_time);
            this.mOnListItemClickListener = onListItemClickListener;
        }

        public void setAdapterItem(T item) {
            this.mItem = item;
            this.mTitleView.setText(item.getTitle());
            this.mIconTextView.setText(item.getIconText());
            this.mSummaryView.setText(item.getSummary());
            this.mTimeView.setText(item.getCurrentTime());
        }

        public void onClick(View v) {
            this.mOnListItemClickListener.onListItemClick(this.mItem);
        }
    }

    public BaseTimeZoneAdapter(List<T> items, OnListItemClickListener<T> onListItemClickListener, Locale locale, boolean showItemSummary, @Nullable CharSequence headerText) {
        this.mOriginalItems = items;
        this.mItems = items;
        this.mOnListItemClickListener = onListItemClickListener;
        this.mLocale = locale;
        this.mShowItemSummary = showItemSummary;
        this.mShowHeader = headerText != null;
        this.mHeaderText = headerText;
        setHasStableIds(true);
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 0:
                return new HeaderViewHolder(inflater.inflate(R.layout.preference_category_material_settings, parent, false));
            case 1:
                return new ItemViewHolder(inflater.inflate(R.layout.time_zone_search_item, parent, false), this.mOnListItemClickListener);
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unexpected viewType: ");
                stringBuilder.append(viewType);
                throw new IllegalArgumentException(stringBuilder.toString());
        }
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).setText(this.mHeaderText);
        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder<T> itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.setAdapterItem(getDataItem(position));
            itemViewHolder.mSummaryFrame.setVisibility(this.mShowItemSummary ? 0 : 8);
        }
    }

    public long getItemId(int position) {
        return isPositionHeader(position) ? -1 : getDataItem(position).getItemId();
    }

    public int getItemCount() {
        return this.mItems.size() + getHeaderCount();
    }

    public int getItemViewType(int position) {
        return isPositionHeader(position) ^ 1;
    }

    public final void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    private int getHeaderCount() {
        return this.mShowHeader;
    }

    private boolean isPositionHeader(int position) {
        return this.mShowHeader && position == 0;
    }

    @NonNull
    public ArrayFilter getFilter() {
        if (this.mFilter == null) {
            this.mFilter = new ArrayFilter();
        }
        return this.mFilter;
    }

    @VisibleForTesting
    public T getDataItem(int position) {
        return (AdapterItem) this.mItems.get(position - getHeaderCount());
    }
}
