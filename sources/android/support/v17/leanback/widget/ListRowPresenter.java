package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v17.leanback.R;
import android.support.v17.leanback.system.Settings;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.widget.BaseGridView.OnUnhandledKeyListener;
import android.support.v17.leanback.widget.ItemBridgeAdapter.Wrapper;
import android.support.v17.leanback.widget.Presenter.ViewHolderTask;
import android.support.v17.leanback.widget.ShadowOverlayHelper.Builder;
import android.support.v17.leanback.widget.ShadowOverlayHelper.Options;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import java.util.HashMap;

public class ListRowPresenter extends RowPresenter {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_RECYCLED_POOL_SIZE = 24;
    private static final String TAG = "ListRowPresenter";
    private static int sExpandedRowNoHovercardBottomPadding;
    private static int sExpandedSelectedRowTopPadding;
    private static int sSelectedRowTopPadding;
    private int mBrowseRowsFadingEdgeLength;
    private int mExpandedRowHeight;
    private int mFocusZoomFactor;
    private PresenterSelector mHoverCardPresenterSelector;
    private boolean mKeepChildForeground;
    private int mNumRows;
    private HashMap<Presenter, Integer> mRecycledPoolSize;
    private boolean mRoundedCornersEnabled;
    private int mRowHeight;
    private boolean mShadowEnabled;
    ShadowOverlayHelper mShadowOverlayHelper;
    private Wrapper mShadowOverlayWrapper;
    private boolean mUseFocusDimmer;

    public static class SelectItemViewHolderTask extends ViewHolderTask {
        private int mItemPosition;
        ViewHolderTask mItemTask;
        private boolean mSmoothScroll = true;

        public SelectItemViewHolderTask(int itemPosition) {
            setItemPosition(itemPosition);
        }

        public void setItemPosition(int itemPosition) {
            this.mItemPosition = itemPosition;
        }

        public int getItemPosition() {
            return this.mItemPosition;
        }

        public void setSmoothScroll(boolean smoothScroll) {
            this.mSmoothScroll = smoothScroll;
        }

        public boolean isSmoothScroll() {
            return this.mSmoothScroll;
        }

        public ViewHolderTask getItemTask() {
            return this.mItemTask;
        }

        public void setItemTask(ViewHolderTask itemTask) {
            this.mItemTask = itemTask;
        }

        public void run(android.support.v17.leanback.widget.Presenter.ViewHolder holder) {
            if (holder instanceof ViewHolder) {
                HorizontalGridView gridView = ((ViewHolder) holder).getGridView();
                ViewHolderTask task = null;
                if (this.mItemTask != null) {
                    task = new ViewHolderTask() {
                        final ViewHolderTask itemTask = SelectItemViewHolderTask.this.mItemTask;

                        public void run(android.support.v7.widget.RecyclerView.ViewHolder rvh) {
                            this.itemTask.run(((android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder) rvh).getViewHolder());
                        }
                    };
                }
                if (isSmoothScroll()) {
                    gridView.setSelectedPositionSmooth(this.mItemPosition, task);
                } else {
                    gridView.setSelectedPosition(this.mItemPosition, task);
                }
            }
        }
    }

    class ListRowPresenterItemBridgeAdapter extends ItemBridgeAdapter {
        ViewHolder mRowViewHolder;

        ListRowPresenterItemBridgeAdapter(ViewHolder rowViewHolder) {
            this.mRowViewHolder = rowViewHolder;
        }

        /* Access modifiers changed, original: protected */
        public void onCreate(android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder viewHolder) {
            if (viewHolder.itemView instanceof ViewGroup) {
                TransitionHelper.setTransitionGroup((ViewGroup) viewHolder.itemView, true);
            }
            if (ListRowPresenter.this.mShadowOverlayHelper != null) {
                ListRowPresenter.this.mShadowOverlayHelper.onViewCreated(viewHolder.itemView);
            }
        }

        public void onBind(final android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder viewHolder) {
            if (this.mRowViewHolder.getOnItemViewClickedListener() != null) {
                viewHolder.mHolder.view.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibh = (android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder) ListRowPresenterItemBridgeAdapter.this.mRowViewHolder.mGridView.getChildViewHolder(viewHolder.itemView);
                        if (ListRowPresenterItemBridgeAdapter.this.mRowViewHolder.getOnItemViewClickedListener() != null) {
                            ListRowPresenterItemBridgeAdapter.this.mRowViewHolder.getOnItemViewClickedListener().onItemClicked(viewHolder.mHolder, ibh.mItem, ListRowPresenterItemBridgeAdapter.this.mRowViewHolder, (ListRow) ListRowPresenterItemBridgeAdapter.this.mRowViewHolder.mRow);
                        }
                    }
                });
            }
        }

        public void onUnbind(android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder viewHolder) {
            if (this.mRowViewHolder.getOnItemViewClickedListener() != null) {
                viewHolder.mHolder.view.setOnClickListener(null);
            }
        }

        public void onAttachedToWindow(android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder viewHolder) {
            ListRowPresenter.this.applySelectLevelToChild(this.mRowViewHolder, viewHolder.itemView);
            this.mRowViewHolder.syncActivatedStatus(viewHolder.itemView);
        }

        public void onAddPresenter(Presenter presenter, int type) {
            this.mRowViewHolder.getGridView().getRecycledViewPool().setMaxRecycledViews(type, ListRowPresenter.this.getRecycledPoolSize(presenter));
        }
    }

    public static class ViewHolder extends android.support.v17.leanback.widget.RowPresenter.ViewHolder {
        final HorizontalGridView mGridView;
        final HorizontalHoverCardSwitcher mHoverCardViewSwitcher = new HorizontalHoverCardSwitcher();
        ItemBridgeAdapter mItemBridgeAdapter;
        final ListRowPresenter mListRowPresenter;
        final int mPaddingBottom;
        final int mPaddingLeft;
        final int mPaddingRight;
        final int mPaddingTop;

        public ViewHolder(View rootView, HorizontalGridView gridView, ListRowPresenter p) {
            super(rootView);
            this.mGridView = gridView;
            this.mListRowPresenter = p;
            this.mPaddingTop = this.mGridView.getPaddingTop();
            this.mPaddingBottom = this.mGridView.getPaddingBottom();
            this.mPaddingLeft = this.mGridView.getPaddingLeft();
            this.mPaddingRight = this.mGridView.getPaddingRight();
        }

        public final ListRowPresenter getListRowPresenter() {
            return this.mListRowPresenter;
        }

        public final HorizontalGridView getGridView() {
            return this.mGridView;
        }

        public final ItemBridgeAdapter getBridgeAdapter() {
            return this.mItemBridgeAdapter;
        }

        public int getSelectedPosition() {
            return this.mGridView.getSelectedPosition();
        }

        public android.support.v17.leanback.widget.Presenter.ViewHolder getItemViewHolder(int position) {
            android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibvh = (android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder) this.mGridView.findViewHolderForAdapterPosition(position);
            if (ibvh == null) {
                return null;
            }
            return ibvh.getViewHolder();
        }

        public android.support.v17.leanback.widget.Presenter.ViewHolder getSelectedItemViewHolder() {
            return getItemViewHolder(getSelectedPosition());
        }

        public Object getSelectedItem() {
            android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibvh = (android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder) this.mGridView.findViewHolderForAdapterPosition(getSelectedPosition());
            if (ibvh == null) {
                return null;
            }
            return ibvh.getItem();
        }
    }

    public ListRowPresenter() {
        this(2);
    }

    public ListRowPresenter(int focusZoomFactor) {
        this(focusZoomFactor, false);
    }

    public ListRowPresenter(int focusZoomFactor, boolean useFocusDimmer) {
        this.mNumRows = 1;
        this.mShadowEnabled = true;
        this.mBrowseRowsFadingEdgeLength = -1;
        this.mRoundedCornersEnabled = true;
        this.mKeepChildForeground = true;
        this.mRecycledPoolSize = new HashMap();
        if (FocusHighlightHelper.isValidZoomIndex(focusZoomFactor)) {
            this.mFocusZoomFactor = focusZoomFactor;
            this.mUseFocusDimmer = useFocusDimmer;
            return;
        }
        throw new IllegalArgumentException("Unhandled zoom factor");
    }

    public void setRowHeight(int rowHeight) {
        this.mRowHeight = rowHeight;
    }

    public int getRowHeight() {
        return this.mRowHeight;
    }

    public void setExpandedRowHeight(int rowHeight) {
        this.mExpandedRowHeight = rowHeight;
    }

    public int getExpandedRowHeight() {
        return this.mExpandedRowHeight != 0 ? this.mExpandedRowHeight : this.mRowHeight;
    }

    public final int getFocusZoomFactor() {
        return this.mFocusZoomFactor;
    }

    @Deprecated
    public final int getZoomFactor() {
        return this.mFocusZoomFactor;
    }

    public final boolean isFocusDimmerUsed() {
        return this.mUseFocusDimmer;
    }

    public void setNumRows(int numRows) {
        this.mNumRows = numRows;
    }

    /* Access modifiers changed, original: protected */
    public void initializeRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder) {
        super.initializeRowViewHolder(holder);
        final ViewHolder rowViewHolder = (ViewHolder) holder;
        Context context = holder.view.getContext();
        boolean z = false;
        if (this.mShadowOverlayHelper == null) {
            Builder needsShadow = new Builder().needsOverlay(needsDefaultListSelectEffect()).needsShadow(needsDefaultShadow());
            boolean z2 = isUsingOutlineClipping(context) && areChildRoundedCornersEnabled();
            this.mShadowOverlayHelper = needsShadow.needsRoundedCorner(z2).preferZOrder(isUsingZOrder(context)).keepForegroundDrawable(this.mKeepChildForeground).options(createShadowOverlayOptions()).build(context);
            if (this.mShadowOverlayHelper.needsWrapper()) {
                this.mShadowOverlayWrapper = new ItemBridgeAdapterShadowOverlayWrapper(this.mShadowOverlayHelper);
            }
        }
        rowViewHolder.mItemBridgeAdapter = new ListRowPresenterItemBridgeAdapter(rowViewHolder);
        rowViewHolder.mItemBridgeAdapter.setWrapper(this.mShadowOverlayWrapper);
        this.mShadowOverlayHelper.prepareParentForShadow(rowViewHolder.mGridView);
        FocusHighlightHelper.setupBrowseItemFocusHighlight(rowViewHolder.mItemBridgeAdapter, this.mFocusZoomFactor, this.mUseFocusDimmer);
        HorizontalGridView horizontalGridView = rowViewHolder.mGridView;
        if (this.mShadowOverlayHelper.getShadowType() != 3) {
            z = true;
        }
        horizontalGridView.setFocusDrawingOrderEnabled(z);
        rowViewHolder.mGridView.setOnChildSelectedListener(new OnChildSelectedListener() {
            public void onChildSelected(ViewGroup parent, View view, int position, long id) {
                ListRowPresenter.this.selectChildView(rowViewHolder, view, true);
            }
        });
        rowViewHolder.mGridView.setOnUnhandledKeyListener(new OnUnhandledKeyListener() {
            public boolean onUnhandledKey(KeyEvent event) {
                return rowViewHolder.getOnKeyListener() != null && rowViewHolder.getOnKeyListener().onKey(rowViewHolder.view, event.getKeyCode(), event);
            }
        });
        rowViewHolder.mGridView.setNumRows(this.mNumRows);
    }

    /* Access modifiers changed, original: final */
    public final boolean needsDefaultListSelectEffect() {
        return isUsingDefaultListSelectEffect() && getSelectEffectEnabled();
    }

    public void setRecycledPoolSize(Presenter presenter, int size) {
        this.mRecycledPoolSize.put(presenter, Integer.valueOf(size));
    }

    public int getRecycledPoolSize(Presenter presenter) {
        return this.mRecycledPoolSize.containsKey(presenter) ? ((Integer) this.mRecycledPoolSize.get(presenter)).intValue() : 24;
    }

    public final void setHoverCardPresenterSelector(PresenterSelector selector) {
        this.mHoverCardPresenterSelector = selector;
    }

    public final PresenterSelector getHoverCardPresenterSelector() {
        return this.mHoverCardPresenterSelector;
    }

    /* Access modifiers changed, original: 0000 */
    public void selectChildView(ViewHolder rowViewHolder, View view, boolean fireEvent) {
        if (view == null) {
            if (this.mHoverCardPresenterSelector != null) {
                rowViewHolder.mHoverCardViewSwitcher.unselect();
            }
            if (fireEvent && rowViewHolder.getOnItemViewSelectedListener() != null) {
                rowViewHolder.getOnItemViewSelectedListener().onItemSelected(null, null, rowViewHolder, rowViewHolder.mRow);
            }
        } else if (rowViewHolder.mSelected) {
            android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibh = (android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder) rowViewHolder.mGridView.getChildViewHolder(view);
            if (this.mHoverCardPresenterSelector != null) {
                rowViewHolder.mHoverCardViewSwitcher.select(rowViewHolder.mGridView, view, ibh.mItem);
            }
            if (fireEvent && rowViewHolder.getOnItemViewSelectedListener() != null) {
                rowViewHolder.getOnItemViewSelectedListener().onItemSelected(ibh.mHolder, ibh.mItem, rowViewHolder, rowViewHolder.mRow);
            }
        }
    }

    private static void initStatics(Context context) {
        if (sSelectedRowTopPadding == 0) {
            sSelectedRowTopPadding = context.getResources().getDimensionPixelSize(R.dimen.lb_browse_selected_row_top_padding);
            sExpandedSelectedRowTopPadding = context.getResources().getDimensionPixelSize(R.dimen.lb_browse_expanded_selected_row_top_padding);
            sExpandedRowNoHovercardBottomPadding = context.getResources().getDimensionPixelSize(R.dimen.lb_browse_expanded_row_no_hovercard_bottom_padding);
        }
    }

    private int getSpaceUnderBaseline(ViewHolder vh) {
        android.support.v17.leanback.widget.RowHeaderPresenter.ViewHolder headerViewHolder = vh.getHeaderViewHolder();
        if (headerViewHolder == null) {
            return 0;
        }
        if (getHeaderPresenter() != null) {
            return getHeaderPresenter().getSpaceUnderBaseline(headerViewHolder);
        }
        return headerViewHolder.view.getPaddingBottom();
    }

    private void setVerticalPadding(ViewHolder vh) {
        int paddingTop;
        int paddingBottom;
        if (vh.isExpanded()) {
            paddingTop = (vh.isSelected() ? sExpandedSelectedRowTopPadding : vh.mPaddingTop) - getSpaceUnderBaseline(vh);
            paddingBottom = this.mHoverCardPresenterSelector == null ? sExpandedRowNoHovercardBottomPadding : vh.mPaddingBottom;
        } else if (vh.isSelected()) {
            paddingTop = sSelectedRowTopPadding - vh.mPaddingBottom;
            paddingBottom = sSelectedRowTopPadding;
        } else {
            paddingTop = 0;
            paddingBottom = vh.mPaddingBottom;
        }
        vh.getGridView().setPadding(vh.mPaddingLeft, paddingTop, vh.mPaddingRight, paddingBottom);
    }

    /* Access modifiers changed, original: protected */
    public android.support.v17.leanback.widget.RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        initStatics(parent.getContext());
        ListRowView rowView = new ListRowView(parent.getContext());
        setupFadingEffect(rowView);
        if (this.mRowHeight != 0) {
            rowView.getGridView().setRowHeight(this.mRowHeight);
        }
        return new ViewHolder(rowView, rowView.getGridView(), this);
    }

    /* Access modifiers changed, original: protected */
    public void dispatchItemSelectedListener(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, boolean selected) {
        ViewHolder vh = (ViewHolder) holder;
        android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder itemViewHolder = (android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder) vh.mGridView.findViewHolderForPosition(vh.mGridView.getSelectedPosition());
        if (itemViewHolder == null) {
            super.dispatchItemSelectedListener(holder, selected);
            return;
        }
        if (selected && holder.getOnItemViewSelectedListener() != null) {
            holder.getOnItemViewSelectedListener().onItemSelected(itemViewHolder.getViewHolder(), itemViewHolder.mItem, vh, vh.getRow());
        }
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewSelected(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, boolean selected) {
        super.onRowViewSelected(holder, selected);
        ViewHolder vh = (ViewHolder) holder;
        setVerticalPadding(vh);
        updateFooterViewSwitcher(vh);
    }

    private void updateFooterViewSwitcher(ViewHolder vh) {
        if (vh.mExpanded && vh.mSelected) {
            if (this.mHoverCardPresenterSelector != null) {
                vh.mHoverCardViewSwitcher.init((ViewGroup) vh.view, this.mHoverCardPresenterSelector);
            }
            android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibh = (android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder) vh.mGridView.findViewHolderForPosition(vh.mGridView.getSelectedPosition());
            selectChildView(vh, ibh == null ? null : ibh.itemView, false);
        } else if (this.mHoverCardPresenterSelector != null) {
            vh.mHoverCardViewSwitcher.unselect();
        }
    }

    private void setupFadingEffect(ListRowView rowView) {
        HorizontalGridView gridView = rowView.getGridView();
        if (this.mBrowseRowsFadingEdgeLength < 0) {
            TypedArray ta = gridView.getContext().obtainStyledAttributes(R.styleable.LeanbackTheme);
            this.mBrowseRowsFadingEdgeLength = (int) ta.getDimension(R.styleable.LeanbackTheme_browseRowsFadingEdgeLength, 0.0f);
            ta.recycle();
        }
        gridView.setFadingLeftEdgeLength(this.mBrowseRowsFadingEdgeLength);
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewExpanded(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, boolean expanded) {
        super.onRowViewExpanded(holder, expanded);
        ViewHolder vh = (ViewHolder) holder;
        if (getRowHeight() != getExpandedRowHeight()) {
            vh.getGridView().setRowHeight(expanded ? getExpandedRowHeight() : getRowHeight());
        }
        setVerticalPadding(vh);
        updateFooterViewSwitcher(vh);
    }

    /* Access modifiers changed, original: protected */
    public void onBindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        ViewHolder vh = (ViewHolder) holder;
        ListRow rowItem = (ListRow) item;
        vh.mItemBridgeAdapter.setAdapter(rowItem.getAdapter());
        vh.mGridView.setAdapter(vh.mItemBridgeAdapter);
        vh.mGridView.setContentDescription(rowItem.getContentDescription());
    }

    /* Access modifiers changed, original: protected */
    public void onUnbindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder) {
        ViewHolder vh = (ViewHolder) holder;
        vh.mGridView.setAdapter(null);
        vh.mItemBridgeAdapter.clear();
        super.onUnbindRowViewHolder(holder);
    }

    public final boolean isUsingDefaultSelectEffect() {
        return false;
    }

    public boolean isUsingDefaultListSelectEffect() {
        return true;
    }

    public boolean isUsingDefaultShadow() {
        return ShadowOverlayHelper.supportsShadow();
    }

    public boolean isUsingZOrder(Context context) {
        return Settings.getInstance(context).preferStaticShadows() ^ 1;
    }

    public boolean isUsingOutlineClipping(Context context) {
        return Settings.getInstance(context).isOutlineClippingDisabled() ^ 1;
    }

    public final void setShadowEnabled(boolean enabled) {
        this.mShadowEnabled = enabled;
    }

    public final boolean getShadowEnabled() {
        return this.mShadowEnabled;
    }

    public final void enableChildRoundedCorners(boolean enable) {
        this.mRoundedCornersEnabled = enable;
    }

    public final boolean areChildRoundedCornersEnabled() {
        return this.mRoundedCornersEnabled;
    }

    /* Access modifiers changed, original: final */
    public final boolean needsDefaultShadow() {
        return isUsingDefaultShadow() && getShadowEnabled();
    }

    public final void setKeepChildForeground(boolean keep) {
        this.mKeepChildForeground = keep;
    }

    public final boolean isKeepChildForeground() {
        return this.mKeepChildForeground;
    }

    /* Access modifiers changed, original: protected */
    public Options createShadowOverlayOptions() {
        return Options.DEFAULT;
    }

    /* Access modifiers changed, original: protected */
    public void onSelectLevelChanged(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder) {
        super.onSelectLevelChanged(holder);
        ViewHolder vh = (ViewHolder) holder;
        int count = vh.mGridView.getChildCount();
        for (int i = 0; i < count; i++) {
            applySelectLevelToChild(vh, vh.mGridView.getChildAt(i));
        }
    }

    /* Access modifiers changed, original: protected */
    public void applySelectLevelToChild(ViewHolder rowViewHolder, View childView) {
        if (this.mShadowOverlayHelper != null && this.mShadowOverlayHelper.needsOverlay()) {
            this.mShadowOverlayHelper.setOverlayColor(childView, rowViewHolder.mColorDimmer.getPaint().getColor());
        }
    }

    public void freeze(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, boolean freeze) {
        ViewHolder vh = (ViewHolder) holder;
        vh.mGridView.setScrollEnabled(freeze ^ 1);
        vh.mGridView.setAnimateChildLayout(freeze ^ 1);
    }

    public void setEntranceTransitionState(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, boolean afterEntrance) {
        super.setEntranceTransitionState(holder, afterEntrance);
        ((ViewHolder) holder).mGridView.setChildrenVisibility(afterEntrance ? 0 : 4);
    }
}
