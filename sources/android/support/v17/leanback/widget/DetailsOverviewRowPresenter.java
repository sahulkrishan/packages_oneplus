package android.support.v17.leanback.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.BaseGridView.OnUnhandledKeyListener;
import android.support.v17.leanback.widget.DetailsOverviewRow.Listener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

@Deprecated
public class DetailsOverviewRowPresenter extends RowPresenter {
    static final boolean DEBUG = false;
    private static final long DEFAULT_TIMEOUT = 5000;
    private static final int MORE_ACTIONS_FADE_MS = 100;
    static final String TAG = "DetailsOverviewRowP";
    OnActionClickedListener mActionClickedListener;
    private int mBackgroundColor = 0;
    private boolean mBackgroundColorSet;
    final Presenter mDetailsPresenter;
    private boolean mIsStyleLarge = true;
    private DetailsOverviewSharedElementHelper mSharedElementHelper;

    class ActionsItemBridgeAdapter extends ItemBridgeAdapter {
        ViewHolder mViewHolder;

        ActionsItemBridgeAdapter(ViewHolder viewHolder) {
            this.mViewHolder = viewHolder;
        }

        public void onBind(final android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibvh) {
            if (this.mViewHolder.getOnItemViewClickedListener() != null || DetailsOverviewRowPresenter.this.mActionClickedListener != null) {
                ibvh.getPresenter().setOnClickListener(ibvh.getViewHolder(), new OnClickListener() {
                    public void onClick(View v) {
                        if (ActionsItemBridgeAdapter.this.mViewHolder.getOnItemViewClickedListener() != null) {
                            ActionsItemBridgeAdapter.this.mViewHolder.getOnItemViewClickedListener().onItemClicked(ibvh.getViewHolder(), ibvh.getItem(), ActionsItemBridgeAdapter.this.mViewHolder, ActionsItemBridgeAdapter.this.mViewHolder.getRow());
                        }
                        if (DetailsOverviewRowPresenter.this.mActionClickedListener != null) {
                            DetailsOverviewRowPresenter.this.mActionClickedListener.onActionClicked((Action) ibvh.getItem());
                        }
                    }
                });
            }
        }

        public void onUnbind(android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder ibvh) {
            if (this.mViewHolder.getOnItemViewClickedListener() != null || DetailsOverviewRowPresenter.this.mActionClickedListener != null) {
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

    public final class ViewHolder extends android.support.v17.leanback.widget.RowPresenter.ViewHolder {
        ItemBridgeAdapter mActionBridgeAdapter;
        final HorizontalGridView mActionsRow;
        final OnChildSelectedListener mChildSelectedListener = new OnChildSelectedListener() {
            public void onChildSelected(ViewGroup parent, View view, int position, long id) {
                ViewHolder.this.dispatchItemSelection(view);
            }
        };
        final FrameLayout mDetailsDescriptionFrame;
        public final android.support.v17.leanback.widget.Presenter.ViewHolder mDetailsDescriptionViewHolder;
        final Handler mHandler = new Handler();
        final ImageView mImageView;
        final OnLayoutChangeListener mLayoutChangeListener = new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                ViewHolder.this.checkFirstAndLastPosition(false);
            }
        };
        final Listener mListener = new Listener() {
            public void onImageDrawableChanged(DetailsOverviewRow row) {
                ViewHolder.this.mHandler.removeCallbacks(ViewHolder.this.mUpdateDrawableCallback);
                ViewHolder.this.mHandler.post(ViewHolder.this.mUpdateDrawableCallback);
            }

            public void onItemChanged(DetailsOverviewRow row) {
                if (ViewHolder.this.mDetailsDescriptionViewHolder != null) {
                    DetailsOverviewRowPresenter.this.mDetailsPresenter.onUnbindViewHolder(ViewHolder.this.mDetailsDescriptionViewHolder);
                }
                DetailsOverviewRowPresenter.this.mDetailsPresenter.onBindViewHolder(ViewHolder.this.mDetailsDescriptionViewHolder, row.getItem());
            }

            public void onActionsAdapterChanged(DetailsOverviewRow row) {
                ViewHolder.this.bindActions(row.getActionsAdapter());
            }
        };
        int mNumItems;
        final FrameLayout mOverviewFrame;
        final ViewGroup mOverviewView;
        final ViewGroup mRightPanel;
        final OnScrollListener mScrollListener = new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                ViewHolder.this.checkFirstAndLastPosition(true);
            }
        };
        boolean mShowMoreLeft;
        boolean mShowMoreRight;
        final Runnable mUpdateDrawableCallback = new Runnable() {
            public void run() {
                DetailsOverviewRowPresenter.this.bindImageDrawable(ViewHolder.this);
            }
        };

        /* Access modifiers changed, original: 0000 */
        public void bindActions(ObjectAdapter adapter) {
            this.mActionBridgeAdapter.setAdapter(adapter);
            this.mActionsRow.setAdapter(this.mActionBridgeAdapter);
            this.mNumItems = this.mActionBridgeAdapter.getItemCount();
            this.mShowMoreRight = false;
            this.mShowMoreLeft = true;
            showMoreLeft(false);
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
            boolean showLeft = false;
            boolean showRight = viewHolder == null || viewHolder.itemView.getRight() > this.mActionsRow.getWidth();
            viewHolder = this.mActionsRow.findViewHolderForPosition(0);
            if (viewHolder == null || viewHolder.itemView.getLeft() < 0) {
                showLeft = true;
            }
            showMoreRight(showRight);
            showMoreLeft(showLeft);
        }

        private void showMoreLeft(boolean show) {
            if (show != this.mShowMoreLeft) {
                this.mActionsRow.setFadingLeftEdge(show);
                this.mShowMoreLeft = show;
            }
        }

        private void showMoreRight(boolean show) {
            if (show != this.mShowMoreRight) {
                this.mActionsRow.setFadingRightEdge(show);
                this.mShowMoreRight = show;
            }
        }

        public ViewHolder(View rootView, Presenter detailsPresenter) {
            super(rootView);
            this.mOverviewFrame = (FrameLayout) rootView.findViewById(R.id.details_frame);
            this.mOverviewView = (ViewGroup) rootView.findViewById(R.id.details_overview);
            this.mImageView = (ImageView) rootView.findViewById(R.id.details_overview_image);
            this.mRightPanel = (ViewGroup) rootView.findViewById(R.id.details_overview_right_panel);
            this.mDetailsDescriptionFrame = (FrameLayout) this.mRightPanel.findViewById(R.id.details_overview_description);
            this.mActionsRow = (HorizontalGridView) this.mRightPanel.findViewById(R.id.details_overview_actions);
            this.mActionsRow.setHasOverlappingRendering(false);
            this.mActionsRow.setOnScrollListener(this.mScrollListener);
            this.mActionsRow.setAdapter(this.mActionBridgeAdapter);
            this.mActionsRow.setOnChildSelectedListener(this.mChildSelectedListener);
            int fadeLength = rootView.getResources().getDimensionPixelSize(R.dimen.lb_details_overview_actions_fade_size);
            this.mActionsRow.setFadingRightEdgeLength(fadeLength);
            this.mActionsRow.setFadingLeftEdgeLength(fadeLength);
            this.mDetailsDescriptionViewHolder = detailsPresenter.onCreateViewHolder(this.mDetailsDescriptionFrame);
            this.mDetailsDescriptionFrame.addView(this.mDetailsDescriptionViewHolder.view);
        }
    }

    public DetailsOverviewRowPresenter(Presenter detailsPresenter) {
        setHeaderPresenter(null);
        setSelectEffectEnabled(false);
        this.mDetailsPresenter = detailsPresenter;
    }

    public void setOnActionClickedListener(OnActionClickedListener listener) {
        this.mActionClickedListener = listener;
    }

    public OnActionClickedListener getOnActionClickedListener() {
        return this.mActionClickedListener;
    }

    public void setBackgroundColor(@ColorInt int color) {
        this.mBackgroundColor = color;
        this.mBackgroundColorSet = true;
    }

    @ColorInt
    public int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public void setStyleLarge(boolean large) {
        this.mIsStyleLarge = large;
    }

    public boolean isStyleLarge() {
        return this.mIsStyleLarge;
    }

    public final void setSharedElementEnterTransition(Activity activity, String sharedElementName, long timeoutMs) {
        if (this.mSharedElementHelper == null) {
            this.mSharedElementHelper = new DetailsOverviewSharedElementHelper();
        }
        this.mSharedElementHelper.setSharedElementEnterTransition(activity, sharedElementName, timeoutMs);
    }

    public final void setSharedElementEnterTransition(Activity activity, String sharedElementName) {
        setSharedElementEnterTransition(activity, sharedElementName, DEFAULT_TIMEOUT);
    }

    private int getDefaultBackgroundColor(Context context) {
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.defaultBrandColor, outValue, true)) {
            return context.getResources().getColor(outValue.resourceId);
        }
        return context.getResources().getColor(R.color.lb_default_brand_color);
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewSelected(android.support.v17.leanback.widget.RowPresenter.ViewHolder vh, boolean selected) {
        super.onRowViewSelected(vh, selected);
        if (selected) {
            ((ViewHolder) vh).dispatchItemSelection(null);
        }
    }

    /* Access modifiers changed, original: protected */
    public android.support.v17.leanback.widget.RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        ViewHolder vh = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_details_overview, parent, false), this.mDetailsPresenter);
        initDetailsOverview(vh);
        return vh;
    }

    private int getCardHeight(Context context) {
        return context.getResources().getDimensionPixelSize(this.mIsStyleLarge ? R.dimen.lb_details_overview_height_large : R.dimen.lb_details_overview_height_small);
    }

    private void initDetailsOverview(final ViewHolder vh) {
        vh.mActionBridgeAdapter = new ActionsItemBridgeAdapter(vh);
        View overview = vh.mOverviewFrame;
        LayoutParams lp = overview.getLayoutParams();
        lp.height = getCardHeight(overview.getContext());
        overview.setLayoutParams(lp);
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

    /* Access modifiers changed, original: 0000 */
    public void bindImageDrawable(ViewHolder vh) {
        int bgColor;
        DetailsOverviewRow row = (DetailsOverviewRow) vh.getRow();
        MarginLayoutParams layoutParams = (MarginLayoutParams) vh.mImageView.getLayoutParams();
        int cardHeight = getCardHeight(vh.mImageView.getContext());
        int verticalMargin = vh.mImageView.getResources().getDimensionPixelSize(R.dimen.lb_details_overview_image_margin_vertical);
        int horizontalMargin = vh.mImageView.getResources().getDimensionPixelSize(R.dimen.lb_details_overview_image_margin_horizontal);
        int drawableWidth = getNonNegativeWidth(row.getImageDrawable());
        int drawableHeight = getNonNegativeHeight(row.getImageDrawable());
        boolean scaleImage = row.isImageScaleUpAllowed();
        boolean useMargin = false;
        if (row.getImageDrawable() != null) {
            boolean landscape = false;
            if (drawableWidth > drawableHeight) {
                landscape = true;
                if (this.mIsStyleLarge) {
                    useMargin = true;
                }
            }
            if ((landscape && drawableWidth > cardHeight) || (!landscape && drawableHeight > cardHeight)) {
                scaleImage = true;
            }
            if (!scaleImage) {
                useMargin = true;
            }
            if (useMargin && !scaleImage) {
                if (landscape && drawableWidth > cardHeight - horizontalMargin) {
                    scaleImage = true;
                } else if (!landscape && drawableHeight > cardHeight - (2 * verticalMargin)) {
                    scaleImage = true;
                }
            }
        }
        if (this.mBackgroundColorSet) {
            bgColor = this.mBackgroundColor;
        } else {
            bgColor = getDefaultBackgroundColor(vh.mOverviewView.getContext());
        }
        if (useMargin) {
            layoutParams.setMarginStart(horizontalMargin);
            layoutParams.bottomMargin = verticalMargin;
            layoutParams.topMargin = verticalMargin;
            vh.mOverviewFrame.setBackgroundColor(bgColor);
            vh.mRightPanel.setBackground(null);
            vh.mImageView.setBackground(null);
        } else {
            layoutParams.bottomMargin = 0;
            layoutParams.topMargin = 0;
            layoutParams.leftMargin = 0;
            vh.mRightPanel.setBackgroundColor(bgColor);
            vh.mImageView.setBackgroundColor(bgColor);
            vh.mOverviewFrame.setBackground(null);
        }
        RoundedRectHelper.setClipToRoundedOutline(vh.mOverviewFrame, true);
        if (scaleImage) {
            vh.mImageView.setScaleType(ScaleType.FIT_START);
            vh.mImageView.setAdjustViewBounds(true);
            vh.mImageView.setMaxWidth(cardHeight);
            layoutParams.height = -1;
            layoutParams.width = -2;
        } else {
            vh.mImageView.setScaleType(ScaleType.CENTER);
            vh.mImageView.setAdjustViewBounds(false);
            layoutParams.height = -2;
            layoutParams.width = Math.min(cardHeight, drawableWidth);
        }
        vh.mImageView.setLayoutParams(layoutParams);
        vh.mImageView.setImageDrawable(row.getImageDrawable());
        if (row.getImageDrawable() != null && this.mSharedElementHelper != null) {
            this.mSharedElementHelper.onBindToDrawable(vh);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onBindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        DetailsOverviewRow row = (DetailsOverviewRow) item;
        ViewHolder vh = (ViewHolder) holder;
        bindImageDrawable(vh);
        this.mDetailsPresenter.onBindViewHolder(vh.mDetailsDescriptionViewHolder, row.getItem());
        vh.bindActions(row.getActionsAdapter());
        row.addListener(vh.mListener);
    }

    /* Access modifiers changed, original: protected */
    public void onUnbindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder) {
        ViewHolder vh = (ViewHolder) holder;
        ((DetailsOverviewRow) vh.getRow()).removeListener(vh.mListener);
        if (vh.mDetailsDescriptionViewHolder != null) {
            this.mDetailsPresenter.onUnbindViewHolder(vh.mDetailsDescriptionViewHolder);
        }
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
        if (this.mDetailsPresenter != null) {
            this.mDetailsPresenter.onViewAttachedToWindow(((ViewHolder) vh).mDetailsDescriptionViewHolder);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewDetachedFromWindow(android.support.v17.leanback.widget.RowPresenter.ViewHolder vh) {
        super.onRowViewDetachedFromWindow(vh);
        if (this.mDetailsPresenter != null) {
            this.mDetailsPresenter.onViewDetachedFromWindow(((ViewHolder) vh).mDetailsDescriptionViewHolder);
        }
    }
}
