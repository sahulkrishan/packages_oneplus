package com.android.settings.dashboard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.settings.suggestions.Suggestion;
import android.support.annotation.VisibleForTesting;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardData.Builder;
import com.android.settings.dashboard.DashboardData.ConditionHeaderData;
import com.android.settings.dashboard.DashboardData.ItemsDataDiffCallback;
import com.android.settings.dashboard.SummaryLoader.SummaryConsumer;
import com.android.settings.dashboard.conditional.Condition;
import com.android.settings.dashboard.conditional.ConditionAdapter;
import com.android.settings.dashboard.suggestions.SuggestionAdapter;
import com.android.settings.dashboard.suggestions.SuggestionAdapter.Callback;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;
import com.android.settingslib.drawer.DashboardCategory;
import com.android.settingslib.drawer.Tile;
import com.android.settingslib.suggestions.SuggestionControllerMixin;
import com.android.settingslib.utils.IconCache;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public class DashboardAdapter extends Adapter<DashboardItemHolder> implements SummaryConsumer, Callback, LifecycleObserver, OnSaveInstanceState {
    private static final String STATE_CATEGORY_LIST = "category_list";
    @VisibleForTesting
    static final String STATE_CONDITION_EXPANDED = "condition_expanded";
    public static final String TAG = "DashboardAdapter";
    private final IconCache mCache;
    private ConditionAdapter mConditionAdapter;
    private final Context mContext;
    @VisibleForTesting
    DashboardData mDashboardData;
    private final DashboardFeatureProvider mDashboardFeatureProvider;
    private boolean mFirstFrameDrawn;
    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private RecyclerView mRecyclerView;
    private boolean mShowSystemUpgrade = false;
    private SuggestionAdapter mSuggestionAdapter;
    private OnClickListener mTileClickListener = new OnClickListener() {
        public void onClick(View v) {
            DashboardAdapter.this.mDashboardFeatureProvider.openTileIntent((Activity) DashboardAdapter.this.mContext, (Tile) v.getTag());
        }
    };

    public static class DashboardItemHolder extends ViewHolder {
        public final ImageView icon;
        public ImageView newverison;
        public final TextView summary;
        public final TextView title;

        public DashboardItemHolder(View itemView) {
            super(itemView);
            this.icon = (ImageView) itemView.findViewById(16908294);
            this.title = (TextView) itemView.findViewById(16908310);
            this.summary = (TextView) itemView.findViewById(16908304);
            this.newverison = (ImageView) itemView.findViewById(R.id.op_right_icon);
        }
    }

    public static class ConditionContainerHolder extends DashboardItemHolder {
        public final RecyclerView data;

        public ConditionContainerHolder(View itemView) {
            super(itemView);
            this.data = (RecyclerView) itemView.findViewById(R.id.data);
        }
    }

    public static class ConditionHeaderHolder extends DashboardItemHolder {
        public final ImageView expandIndicator;
        public final LinearLayout icons;

        public ConditionHeaderHolder(View itemView) {
            super(itemView);
            this.icons = (LinearLayout) itemView.findViewById(R.id.additional_icons);
            this.expandIndicator = (ImageView) itemView.findViewById(R.id.expand_indicator);
        }

        public void setAllowToExpand(boolean allow) {
            if (!allow && this.expandIndicator != null) {
                this.expandIndicator.setVisibility(!allow ? 8 : 0);
            }
        }
    }

    public static class SearchLayoutHolder extends DashboardItemHolder {
        public SearchLayoutHolder(View itemView) {
            super(itemView);
        }
    }

    public static class SuggestionContainerHolder extends DashboardItemHolder {
        public final RecyclerView data;

        public SuggestionContainerHolder(View itemView) {
            super(itemView);
            this.data = (RecyclerView) itemView.findViewById(R.id.suggestion_list);
        }
    }

    public void setShowSystemUpgradeIcon(boolean value) {
        this.mShowSystemUpgrade = value;
        notifyDataSetChanged();
    }

    public DashboardAdapter(Context context, Bundle savedInstanceState, List<Condition> conditions, SuggestionControllerMixin suggestionControllerMixin, Lifecycle lifecycle) {
        DashboardCategory category = null;
        boolean conditionExpanded = false;
        this.mContext = context;
        FeatureFactory factory = FeatureFactory.getFactory(context);
        this.mMetricsFeatureProvider = factory.getMetricsFeatureProvider();
        this.mDashboardFeatureProvider = factory.getDashboardFeatureProvider(context);
        this.mCache = new IconCache(context);
        this.mSuggestionAdapter = new SuggestionAdapter(this.mContext, suggestionControllerMixin, savedInstanceState, this, lifecycle);
        setHasStableIds(true);
        if (savedInstanceState != null) {
            category = (DashboardCategory) savedInstanceState.getParcelable(STATE_CATEGORY_LIST);
            conditionExpanded = savedInstanceState.getBoolean(STATE_CONDITION_EXPANDED, false);
        }
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        this.mDashboardData = new Builder().setConditions(conditions).setSuggestions(this.mSuggestionAdapter.getSuggestions()).setCategory(category).setConditionExpanded(conditionExpanded).build();
    }

    public void setSuggestions(List<Suggestion> data) {
        DashboardData prevData = this.mDashboardData;
        this.mDashboardData = new Builder(prevData).setSuggestions(data).build();
        notifyDashboardDataChanged(prevData);
    }

    public void setCategory(DashboardCategory category) {
        DashboardData prevData = this.mDashboardData;
        Log.d(TAG, "adapter setCategory called");
        this.mDashboardData = new Builder(prevData).setCategory(category).build();
        notifyDashboardDataChanged(prevData);
    }

    public void setConditions(List<Condition> conditions) {
        DashboardData prevData = this.mDashboardData;
        Log.d(TAG, "adapter setConditions called");
        this.mDashboardData = new Builder(prevData).setConditions(conditions).build();
        if (this.mConditionAdapter != null) {
            this.mConditionAdapter.setConditions(this.mDashboardData.getConditionsToShow());
        }
        notifyDashboardDataChanged(prevData);
    }

    public void onSuggestionClosed(Suggestion suggestion) {
        List<Suggestion> list = this.mDashboardData.getSuggestions();
        if (list != null && list.size() != 0) {
            if (list.size() == 1) {
                setSuggestions(null);
            } else {
                list.remove(suggestion);
                setSuggestions(list);
            }
        }
    }

    public void notifySummaryChanged(Tile tile) {
        int position = this.mDashboardData.getPositionByTile(tile);
        if (position != -1) {
            notifyItemChanged(position, Integer.valueOf(this.mDashboardData.getItemTypeByPosition(position)));
        }
    }

    public DashboardItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        if (viewType == R.layout.op_search_layout) {
            return new SearchLayoutHolder(view);
        }
        if (viewType == R.layout.condition_header) {
            return new ConditionHeaderHolder(view);
        }
        if (viewType == R.layout.condition_container) {
            return new ConditionContainerHolder(view);
        }
        if (viewType == R.layout.suggestion_container) {
            return new SuggestionContainerHolder(view);
        }
        return new DashboardItemHolder(view);
    }

    public void onBindViewHolder(DashboardItemHolder holder, int position) {
        int type = this.mDashboardData.getItemTypeByPosition(position);
        if (type == R.layout.dashboard_tile) {
            Tile tile = (Tile) this.mDashboardData.getItemEntityByPosition(position);
            onBindTile(holder, tile);
            holder.itemView.setTag(tile);
            holder.itemView.setOnClickListener(this.mTileClickListener);
        } else if (type == R.layout.op_search_layout) {
            holder.itemView.setTag(Integer.valueOf(R.layout.op_search_layout));
            FeatureFactory.getFactory(this.mContext).getSearchFeatureProvider().initSearchLayout((Activity) this.mContext, holder.itemView);
        } else if (type != R.layout.suggestion_container) {
            switch (type) {
                case R.layout.condition_container /*2131558482*/:
                    onBindCondition((ConditionContainerHolder) holder, position);
                    return;
                case R.layout.condition_footer /*2131558483*/:
                    holder.itemView.setOnClickListener(new -$$Lambda$DashboardAdapter$EPKC8_9XatJQPDMMI1s2dy8ZY8c(this));
                    return;
                case R.layout.condition_header /*2131558484*/:
                    onBindConditionHeader((ConditionHeaderHolder) holder, (ConditionHeaderData) this.mDashboardData.getItemEntityByPosition(position));
                    return;
                default:
                    return;
            }
        } else {
            onBindSuggestion((SuggestionContainerHolder) holder, position);
        }
    }

    public static /* synthetic */ void lambda$onBindViewHolder$0(DashboardAdapter dashboardAdapter, View v) {
        dashboardAdapter.mMetricsFeatureProvider.action(dashboardAdapter.mContext, 373, false);
        DashboardData prevData = dashboardAdapter.mDashboardData;
        dashboardAdapter.mDashboardData = new Builder(prevData).setConditionExpanded(false).build();
        dashboardAdapter.notifyDashboardDataChanged(prevData);
        dashboardAdapter.scrollToTopOfConditions();
    }

    public long getItemId(int position) {
        return (long) this.mDashboardData.getItemIdByPosition(position);
    }

    public int getItemViewType(int position) {
        return this.mDashboardData.getItemTypeByPosition(position);
    }

    public int getItemCount() {
        return this.mDashboardData.size();
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    public Object getItem(long itemId) {
        return this.mDashboardData.getItemEntityById(itemId);
    }

    public Suggestion getSuggestion(int position) {
        return this.mSuggestionAdapter.getSuggestion(position);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void notifyDashboardDataChanged(DashboardData prevData) {
        if (!this.mFirstFrameDrawn || prevData == null) {
            this.mFirstFrameDrawn = true;
            notifyDataSetChanged();
            return;
        }
        DiffUtil.calculateDiff(new ItemsDataDiffCallback(prevData.getItemList(), this.mDashboardData.getItemList())).dispatchUpdatesTo((Adapter) this);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void onBindConditionHeader(ConditionHeaderHolder holder, ConditionHeaderData data) {
        holder.icon.setImageDrawable((Drawable) data.conditionIcons.get(0));
        if (data.conditionCount == 1) {
            holder.title.setText(data.title);
            holder.summary.setText(null);
            holder.icons.setVisibility(4);
        } else {
            holder.title.setText(null);
            holder.summary.setText(this.mContext.getString(R.string.condition_summary, new Object[]{Integer.valueOf(data.conditionCount)}));
            updateConditionIcons(data.conditionIcons, holder.icons);
            holder.icons.setVisibility(0);
        }
        holder.itemView.setOnClickListener(new -$$Lambda$DashboardAdapter$rjSGP6Cnddhw6FvR740SjWmziQ4(this));
    }

    public static /* synthetic */ void lambda$onBindConditionHeader$1(DashboardAdapter dashboardAdapter, View v) {
        dashboardAdapter.mMetricsFeatureProvider.action(dashboardAdapter.mContext, 373, true);
        DashboardData prevData = dashboardAdapter.mDashboardData;
        dashboardAdapter.mDashboardData = new Builder(prevData).setConditionExpanded(true).build();
        dashboardAdapter.notifyDashboardDataChanged(prevData);
        dashboardAdapter.scrollToTopOfConditions();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void onBindCondition(ConditionContainerHolder holder, int position) {
        if (this.mConditionAdapter == null) {
            this.mConditionAdapter = new ConditionAdapter(this.mContext, (List) this.mDashboardData.getItemEntityByPosition(position), this.mDashboardData.isConditionExpanded());
        }
        this.mConditionAdapter.addDismissHandling(holder.data);
        holder.data.setAdapter(this.mConditionAdapter);
        holder.data.setLayoutManager(new LinearLayoutManager(this.mContext));
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void onBindSuggestion(SuggestionContainerHolder holder, int position) {
        List<Suggestion> suggestions = (List) this.mDashboardData.getItemEntityByPosition(position);
        if (suggestions != null && suggestions.size() > 0) {
            this.mSuggestionAdapter.setSuggestions(suggestions);
            holder.data.setAdapter(this.mSuggestionAdapter);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.mContext);
        layoutManager.setOrientation(0);
        holder.data.setLayoutManager(layoutManager);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void onBindTile(DashboardItemHolder holder, Tile tile) {
        Drawable icon = this.mCache.getIcon(tile.icon);
        if (tile != null) {
            Intent tileIntent = tile.intent;
            if (tileIntent != null) {
                ComponentName cName = tileIntent.getComponent();
                if (cName != null && OPConstants.PACKAGENAME_GMS.equals(cName.getPackageName())) {
                    icon = this.mContext.getDrawable(R.drawable.op_ic_homepage_google);
                    this.mCache.updateIcon(tile.icon, icon);
                }
            }
        }
        if (!OPUtils.isAndroidModeOn(this.mContext.getContentResolver())) {
            icon.setTint(this.mContext.getColor(R.color.oneplus_accent_color));
            this.mCache.updateIcon(tile.icon, icon);
        }
        holder.icon.setImageDrawable(icon);
        holder.title.setText(tile.title);
        if (TextUtils.isEmpty(tile.summary)) {
            holder.summary.setVisibility(8);
        } else {
            holder.summary.setText(tile.summary);
            holder.summary.setVisibility(0);
        }
        if (holder.newverison == null) {
            return;
        }
        if ((holder.title.getText().equals(this.mContext.getString(R.string.header_category_system)) || holder.title.getText().equals(this.mContext.getString(R.string.system_update_settings_list_item_title))) && this.mShowSystemUpgrade) {
            holder.newverison.setVisibility(0);
        } else {
            holder.newverison.setVisibility(8);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        DashboardCategory category = this.mDashboardData.getCategory();
        if (category != null) {
            outState.putParcelable(STATE_CATEGORY_LIST, category);
        }
        outState.putBoolean(STATE_CONDITION_EXPANDED, this.mDashboardData.isConditionExpanded());
    }

    private void updateConditionIcons(List<Drawable> icons, ViewGroup parent) {
        if (icons == null || icons.size() < 2) {
            parent.setVisibility(4);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        parent.removeAllViews();
        int size = icons.size();
        for (int i = 1; i < size; i++) {
            ImageView icon = (ImageView) inflater.inflate(R.layout.condition_header_icon, parent, false);
            icon.setImageDrawable((Drawable) icons.get(i));
            parent.addView(icon);
        }
        parent.setVisibility(0);
    }

    private void scrollToTopOfConditions() {
        this.mRecyclerView.scrollToPosition(this.mDashboardData.hasSuggestion());
    }
}
