package android.support.v17.leanback.widget;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.BaseGridView.OnUnhandledKeyListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;

public class FullWidthDetailsOverviewRowPresenter extends RowPresenter {
    public static final int ALIGN_MODE_MIDDLE = 1;
    public static final int ALIGN_MODE_START = 0;
    static final boolean DEBUG = false;
    public static final int STATE_FULL = 1;
    public static final int STATE_HALF = 0;
    public static final int STATE_SMALL = 2;
    static final String TAG = "FullWidthDetailsRP";
    static final Handler sHandler = new Handler();
    private static Rect sTmpRect = new Rect();
    OnActionClickedListener mActionClickedListener;
    private int mActionsBackgroundColor;
    private boolean mActionsBackgroundColorSet;
    private int mAlignmentMode;
    private int mBackgroundColor;
    private boolean mBackgroundColorSet;
    final DetailsOverviewLogoPresenter mDetailsOverviewLogoPresenter;
    final Presenter mDetailsPresenter;
    protected int mInitialState;
    private Listener mListener;
    private boolean mParticipatingEntranceTransition;

    public static abstract class Listener {
        public void onBindLogo(ViewHolder vh) {
        }
    }

    class ActionsItemBridgeAdapter extends ItemBridgeAdapter {
        ViewHolder mViewHolder;

        ActionsItemBridgeAdapter(ViewHolder viewHolder) {
            this.mViewHolder = viewHolder;
        }

        public void onBind(final android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibvh) {
            if (this.mViewHolder.getOnItemViewClickedListener() != null || FullWidthDetailsOverviewRowPresenter.this.mActionClickedListener != null) {
                ibvh.getPresenter().setOnClickListener(ibvh.getViewHolder(), new OnClickListener() {
                    public void onClick(View v) {
                        if (ActionsItemBridgeAdapter.this.mViewHolder.getOnItemViewClickedListener() != null) {
                            ActionsItemBridgeAdapter.this.mViewHolder.getOnItemViewClickedListener().onItemClicked(ibvh.getViewHolder(), ibvh.getItem(), ActionsItemBridgeAdapter.this.mViewHolder, ActionsItemBridgeAdapter.this.mViewHolder.getRow());
                        }
                        if (FullWidthDetailsOverviewRowPresenter.this.mActionClickedListener != null) {
                            FullWidthDetailsOverviewRowPresenter.this.mActionClickedListener.onActionClicked((Action) ibvh.getItem());
                        }
                    }
                });
            }
        }

        public void onUnbind(android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibvh) {
            if (this.mViewHolder.getOnItemViewClickedListener() != null || FullWidthDetailsOverviewRowPresenter.this.mActionClickedListener != null) {
                ibvh.getPresenter().setOnClickListener(ibvh.getViewHolder(), null);
            }
        }

        public void onAttachedToWindow(android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder viewHolder) {
            viewHolder.itemView.removeOnLayoutChangeListener(this.mViewHolder.mLayoutChangeListener);
            viewHolder.itemView.addOnLayoutChangeListener(this.mViewHolder.mLayoutChangeListener);
        }

        public void onDetachedFromWindow(android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder viewHolder) {
            viewHolder.itemView.removeOnLayoutChangeListener(this.mViewHolder.mLayoutChangeListener);
            this.mViewHolder.checkFirstAndLastPosition(false);
        }
    }

    public class ViewHolder extends android.support.v17.leanback.widget.RowPresenter.ViewHolder {
        ItemBridgeAdapter mActionBridgeAdapter;
        final HorizontalGridView mActionsRow;
        final OnChildSelectedListener mChildSelectedListener = new OnChildSelectedListener() {
            public void onChildSelected(ViewGroup parent, View view, int position, long id) {
                ViewHolder.this.dispatchItemSelection(view);
            }
        };
        final ViewGroup mDetailsDescriptionFrame;
        final android.support.v17.leanback.widget.Presenter.ViewHolder mDetailsDescriptionViewHolder;
        final android.support.v17.leanback.widget.DetailsOverviewLogoPresenter.ViewHolder mDetailsLogoViewHolder;
        final OnLayoutChangeListener mLayoutChangeListener = new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ViewHolder.this.checkFirstAndLastPosition(false);
            }
        };
        int mNumItems;
        final FrameLayout mOverviewFrame;
        final ViewGroup mOverviewRoot;
        protected final android.support.v17.leanback.widget.DetailsOverviewRow.Listener mRowListener = createRowListener();
        final OnScrollListener mScrollListener = new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                ViewHolder.this.checkFirstAndLastPosition(true);
            }
        };
        int mState = 0;
        final Runnable mUpdateDrawableCallback = new Runnable() {
            public void run() {
                Row row = ViewHolder.this.getRow();
                if (row != null) {
                    FullWidthDetailsOverviewRowPresenter.this.mDetailsOverviewLogoPresenter.onBindViewHolder(ViewHolder.this.mDetailsLogoViewHolder, row);
                }
            }
        };

        public class DetailsOverviewRowListener extends android.support.v17.leanback.widget.DetailsOverviewRow.Listener {
            public void onImageDrawableChanged(DetailsOverviewRow row) {
                FullWidthDetailsOverviewRowPresenter.sHandler.removeCallbacks(ViewHolder.this.mUpdateDrawableCallback);
                FullWidthDetailsOverviewRowPresenter.sHandler.post(ViewHolder.this.mUpdateDrawableCallback);
            }

            public void onItemChanged(DetailsOverviewRow row) {
                if (ViewHolder.this.mDetailsDescriptionViewHolder != null) {
                    FullWidthDetailsOverviewRowPresenter.this.mDetailsPresenter.onUnbindViewHolder(ViewHolder.this.mDetailsDescriptionViewHolder);
                }
                FullWidthDetailsOverviewRowPresenter.this.mDetailsPresenter.onBindViewHolder(ViewHolder.this.mDetailsDescriptionViewHolder, row.getItem());
            }

            public void onActionsAdapterChanged(DetailsOverviewRow row) {
                ViewHolder.this.bindActions(row.getActionsAdapter());
            }
        }

        /* Access modifiers changed, original: protected */
        public android.support.v17.leanback.widget.DetailsOverviewRow.Listener createRowListener() {
            return new DetailsOverviewRowListener();
        }

        /* Access modifiers changed, original: 0000 */
        public void bindActions(ObjectAdapter adapter) {
            this.mActionBridgeAdapter.setAdapter(adapter);
            this.mActionsRow.setAdapter(this.mActionBridgeAdapter);
            this.mNumItems = this.mActionBridgeAdapter.getItemCount();
        }

        /* Access modifiers changed, original: 0000 */
        public void onBind() {
            DetailsOverviewRow row = (DetailsOverviewRow) getRow();
            bindActions(row.getActionsAdapter());
            row.addListener(this.mRowListener);
        }

        /* Access modifiers changed, original: 0000 */
        public void onUnbind() {
            ((DetailsOverviewRow) getRow()).removeListener(this.mRowListener);
            FullWidthDetailsOverviewRowPresenter.sHandler.removeCallbacks(this.mUpdateDrawableCallback);
        }

        /* Access modifiers changed, original: 0000 */
        public void dispatchItemSelection(View view) {
            if (isSelected()) {
                android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibvh;
                if (view != null) {
                    ibvh = this.mActionsRow.getChildViewHolder(view);
                } else {
                    ibvh = this.mActionsRow.findViewHolderForPosition(this.mActionsRow.getSelectedPosition());
                }
                ibvh = ibvh;
                if (ibvh == null) {
                    if (getOnItemViewSelectedListener() != null) {
                        getOnItemViewSelectedListener().onItemSelected(null, null, this, getRow());
                    }
                } else if (getOnItemViewSelectedListener() != null) {
                    getOnItemViewSelectedListener().onItemSelected(ibvh.getViewHolder(), ibvh.getItem(), this, getRow());
                }
            }
        }

        private int getViewCenter(View view) {
            return (view.getRight() - view.getLeft()) / 2;
        }

        /* Access modifiers changed, original: 0000 */
        public void checkFirstAndLastPosition(boolean fromScroll) {
            android.support.v7.widget.RecyclerView.ViewHolder viewHolder = this.mActionsRow.findViewHolderForPosition(this.mNumItems - 1);
            if (viewHolder == null || viewHolder.itemView.getRight() > this.mActionsRow.getWidth()) {
                boolean showRight = true;
            } else {
                int i = 0;
            }
            viewHolder = this.mActionsRow.findViewHolderForPosition(0);
            if (viewHolder == null || viewHolder.itemView.getLeft() < 0) {
                boolean showLeft = true;
            }
        }

        public ViewHolder(View rootView, Presenter detailsPresenter, DetailsOverviewLogoPresenter logoPresenter) {
            super(rootView);
            this.mOverviewRoot = (ViewGroup) rootView.findViewById(R.id.details_root);
            this.mOverviewFrame = (FrameLayout) rootView.findViewById(R.id.details_frame);
            this.mDetailsDescriptionFrame = (ViewGroup) rootView.findViewById(R.id.details_overview_description);
            this.mActionsRow = (HorizontalGridView) this.mOverviewFrame.findViewById(R.id.details_overview_actions);
            this.mActionsRow.setHasOverlappingRendering(false);
            this.mActionsRow.setOnScrollListener(this.mScrollListener);
            this.mActionsRow.setAdapter(this.mActionBridgeAdapter);
            this.mActionsRow.setOnChildSelectedListener(this.mChildSelectedListener);
            int fadeLength = rootView.getResources().getDimensionPixelSize(R.dimen.lb_details_overview_actions_fade_size);
            this.mActionsRow.setFadingRightEdgeLength(fadeLength);
            this.mActionsRow.setFadingLeftEdgeLength(fadeLength);
            this.mDetailsDescriptionViewHolder = detailsPresenter.onCreateViewHolder(this.mDetailsDescriptionFrame);
            this.mDetailsDescriptionFrame.addView(this.mDetailsDescriptionViewHolder.view);
            this.mDetailsLogoViewHolder = (android.support.v17.leanback.widget.DetailsOverviewLogoPresenter.ViewHolder) logoPresenter.onCreateViewHolder(this.mOverviewRoot);
            this.mOverviewRoot.addView(this.mDetailsLogoViewHolder.view);
        }

        public final ViewGroup getOverviewView() {
            return this.mOverviewFrame;
        }

        public final android.support.v17.leanback.widget.DetailsOverviewLogoPresenter.ViewHolder getLogoViewHolder() {
            return this.mDetailsLogoViewHolder;
        }

        public final android.support.v17.leanback.widget.Presenter.ViewHolder getDetailsDescriptionViewHolder() {
            return this.mDetailsDescriptionViewHolder;
        }

        public final ViewGroup getDetailsDescriptionFrame() {
            return this.mDetailsDescriptionFrame;
        }

        public final ViewGroup getActionsRow() {
            return this.mActionsRow;
        }

        public final int getState() {
            return this.mState;
        }
    }

    public FullWidthDetailsOverviewRowPresenter(Presenter detailsPresenter) {
        this(detailsPresenter, new DetailsOverviewLogoPresenter());
    }

    public FullWidthDetailsOverviewRowPresenter(Presenter detailsPresenter, DetailsOverviewLogoPresenter logoPresenter) {
        this.mInitialState = 0;
        this.mBackgroundColor = 0;
        this.mActionsBackgroundColor = 0;
        setHeaderPresenter(null);
        setSelectEffectEnabled(false);
        this.mDetailsPresenter = detailsPresenter;
        this.mDetailsOverviewLogoPresenter = logoPresenter;
    }

    public void setOnActionClickedListener(OnActionClickedListener listener) {
        this.mActionClickedListener = listener;
    }

    public OnActionClickedListener getOnActionClickedListener() {
        return this.mActionClickedListener;
    }

    public final void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
        this.mBackgroundColorSet = true;
    }

    public final int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public final void setActionsBackgroundColor(int color) {
        this.mActionsBackgroundColor = color;
        this.mActionsBackgroundColorSet = true;
    }

    public final int getActionsBackgroundColor() {
        return this.mActionsBackgroundColor;
    }

    public final boolean isParticipatingEntranceTransition() {
        return this.mParticipatingEntranceTransition;
    }

    public final void setParticipatingEntranceTransition(boolean participating) {
        this.mParticipatingEntranceTransition = participating;
    }

    public final void setInitialState(int state) {
        this.mInitialState = state;
    }

    public final int getInitialState() {
        return this.mInitialState;
    }

    public final void setAlignmentMode(int alignmentMode) {
        this.mAlignmentMode = alignmentMode;
    }

    public final int getAlignmentMode() {
        return this.mAlignmentMode;
    }

    /* Access modifiers changed, original: protected */
    public boolean isClippingChildren() {
        return true;
    }

    public final void setListener(Listener listener) {
        this.mListener = listener;
    }

    /* Access modifiers changed, original: protected */
    public int getLayoutResourceId() {
        return R.layout.lb_fullwidth_details_overview;
    }

    /* Access modifiers changed, original: protected */
    public android.support.v17.leanback.widget.RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        final ViewHolder vh = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(getLayoutResourceId(), parent, false), this.mDetailsPresenter, this.mDetailsOverviewLogoPresenter);
        this.mDetailsOverviewLogoPresenter.setContext(vh.mDetailsLogoViewHolder, vh, this);
        setState(vh, this.mInitialState);
        vh.mActionBridgeAdapter = new ActionsItemBridgeAdapter(vh);
        View overview = vh.mOverviewFrame;
        if (this.mBackgroundColorSet) {
            overview.setBackgroundColor(this.mBackgroundColor);
        }
        if (this.mActionsBackgroundColorSet) {
            overview.findViewById(R.id.details_overview_actions_background).setBackgroundColor(this.mActionsBackgroundColor);
        }
        RoundedRectHelper.setClipToRoundedOutline(overview, true);
        if (!getSelectEffectEnabled()) {
            vh.mOverviewFrame.setForeground(null);
        }
        vh.mActionsRow.setOnUnhandledKeyListener(new OnUnhandledKeyListener() {
            public boolean onUnhandledKey(KeyEvent event) {
                if (vh.getOnKeyListener() == null || !vh.getOnKeyListener().onKey(vh.view, event.getKeyCode(), event)) {
                    return false;
                }
                return true;
            }
        });
        return vh;
    }

    private static int getNonNegativeWidth(Drawable drawable) {
        int width = drawable == null ? 0 : drawable.getIntrinsicWidth();
        if (width > 0) {
            return width;
        }
        return 0;
    }

    private static int getNonNegativeHeight(Drawable drawable) {
        int height = drawable == null ? 0 : drawable.getIntrinsicHeight();
        if (height > 0) {
            return height;
        }
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void onBindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        DetailsOverviewRow row = (DetailsOverviewRow) item;
        ViewHolder vh = (ViewHolder) holder;
        this.mDetailsOverviewLogoPresenter.onBindViewHolder(vh.mDetailsLogoViewHolder, row);
        this.mDetailsPresenter.onBindViewHolder(vh.mDetailsDescriptionViewHolder, row.getItem());
        vh.onBind();
    }

    /* Access modifiers changed, original: protected */
    public void onUnbindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder) {
        ViewHolder vh = (ViewHolder) holder;
        vh.onUnbind();
        this.mDetailsPresenter.onUnbindViewHolder(vh.mDetailsDescriptionViewHolder);
        this.mDetailsOverviewLogoPresenter.onUnbindViewHolder(vh.mDetailsLogoViewHolder);
        super.onUnbindRowViewHolder(holder);
    }

    public final boolean isUsingDefaultSelectEffect() {
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void onSelectLevelChanged(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder) {
        super.onSelectLevelChanged(holder);
        if (getSelectEffectEnabled()) {
            ViewHolder vh = (ViewHolder) holder;
            ((ColorDrawable) vh.mOverviewFrame.getForeground().mutate()).setColor(vh.mColorDimmer.getPaint().getColor());
        }
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewAttachedToWindow(android.support.v17.leanback.widget.RowPresenter.ViewHolder vh) {
        super.onRowViewAttachedToWindow(vh);
        ViewHolder viewHolder = (ViewHolder) vh;
        this.mDetailsPresenter.onViewAttachedToWindow(viewHolder.mDetailsDescriptionViewHolder);
        this.mDetailsOverviewLogoPresenter.onViewAttachedToWindow(viewHolder.mDetailsLogoViewHolder);
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewDetachedFromWindow(android.support.v17.leanback.widget.RowPresenter.ViewHolder vh) {
        super.onRowViewDetachedFromWindow(vh);
        ViewHolder viewHolder = (ViewHolder) vh;
        this.mDetailsPresenter.onViewDetachedFromWindow(viewHolder.mDetailsDescriptionViewHolder);
        this.mDetailsOverviewLogoPresenter.onViewDetachedFromWindow(viewHolder.mDetailsLogoViewHolder);
    }

    public final void notifyOnBindLogo(ViewHolder viewHolder) {
        onLayoutOverviewFrame(viewHolder, viewHolder.getState(), true);
        onLayoutLogo(viewHolder, viewHolder.getState(), true);
        if (this.mListener != null) {
            this.mListener.onBindLogo(viewHolder);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onLayoutLogo(ViewHolder viewHolder, int oldState, boolean logoChanged) {
        View v = viewHolder.getLogoViewHolder().view;
        MarginLayoutParams lp = (MarginLayoutParams) v.getLayoutParams();
        if (this.mAlignmentMode != 1) {
            lp.setMarginStart(v.getResources().getDimensionPixelSize(R.dimen.lb_details_v2_logo_margin_start));
        } else {
            lp.setMarginStart(v.getResources().getDimensionPixelSize(R.dimen.lb_details_v2_left) - lp.width);
        }
        int state = viewHolder.getState();
        if (state == 0) {
            lp.topMargin = (v.getResources().getDimensionPixelSize(R.dimen.lb_details_v2_blank_height) + v.getResources().getDimensionPixelSize(R.dimen.lb_details_v2_actions_height)) + v.getResources().getDimensionPixelSize(R.dimen.lb_details_v2_description_margin_top);
        } else if (state != 2) {
            lp.topMargin = v.getResources().getDimensionPixelSize(R.dimen.lb_details_v2_blank_height) - (lp.height / 2);
        } else {
            lp.topMargin = 0;
        }
        v.setLayoutParams(lp);
    }

    /* Access modifiers changed, original: protected */
    public void onLayoutOverviewFrame(ViewHolder viewHolder, int oldState, boolean logoChanged) {
        int i = 0;
        boolean wasBanner = oldState == 2;
        boolean isBanner = viewHolder.getState() == 2;
        if (wasBanner != isBanner || logoChanged) {
            int frameMarginStart;
            int descriptionMarginStart;
            Resources res = viewHolder.view.getResources();
            int logoWidth = 0;
            if (this.mDetailsOverviewLogoPresenter.isBoundToImage(viewHolder.getLogoViewHolder(), (DetailsOverviewRow) viewHolder.getRow())) {
                logoWidth = viewHolder.getLogoViewHolder().view.getLayoutParams().width;
            }
            if (this.mAlignmentMode != 1) {
                if (isBanner) {
                    frameMarginStart = res.getDimensionPixelSize(R.dimen.lb_details_v2_logo_margin_start);
                    descriptionMarginStart = logoWidth;
                } else {
                    frameMarginStart = 0;
                    descriptionMarginStart = logoWidth + res.getDimensionPixelSize(R.dimen.lb_details_v2_logo_margin_start);
                }
            } else if (isBanner) {
                frameMarginStart = res.getDimensionPixelSize(R.dimen.lb_details_v2_left) - logoWidth;
                descriptionMarginStart = logoWidth;
            } else {
                frameMarginStart = 0;
                descriptionMarginStart = res.getDimensionPixelSize(R.dimen.lb_details_v2_left);
            }
            MarginLayoutParams lpFrame = (MarginLayoutParams) viewHolder.getOverviewView().getLayoutParams();
            lpFrame.topMargin = isBanner ? 0 : res.getDimensionPixelSize(R.dimen.lb_details_v2_blank_height);
            lpFrame.rightMargin = frameMarginStart;
            lpFrame.leftMargin = frameMarginStart;
            viewHolder.getOverviewView().setLayoutParams(lpFrame);
            View description = viewHolder.getDetailsDescriptionFrame();
            MarginLayoutParams lpDesc = (MarginLayoutParams) description.getLayoutParams();
            lpDesc.setMarginStart(descriptionMarginStart);
            description.setLayoutParams(lpDesc);
            View action = viewHolder.getActionsRow();
            MarginLayoutParams lpActions = (MarginLayoutParams) action.getLayoutParams();
            lpActions.setMarginStart(descriptionMarginStart);
            if (!isBanner) {
                i = res.getDimensionPixelSize(R.dimen.lb_details_v2_actions_height);
            }
            lpActions.height = i;
            action.setLayoutParams(lpActions);
        }
    }

    public final void setState(ViewHolder viewHolder, int state) {
        if (viewHolder.getState() != state) {
            int oldState = viewHolder.getState();
            viewHolder.mState = state;
            onStateChanged(viewHolder, oldState);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onStateChanged(ViewHolder viewHolder, int oldState) {
        onLayoutOverviewFrame(viewHolder, oldState, false);
        onLayoutLogo(viewHolder, oldState, false);
    }

    public void setEntranceTransitionState(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, boolean afterEntrance) {
        super.setEntranceTransitionState(holder, afterEntrance);
        if (this.mParticipatingEntranceTransition) {
            holder.view.setVisibility(afterEntrance ? 0 : 4);
        }
    }
}
