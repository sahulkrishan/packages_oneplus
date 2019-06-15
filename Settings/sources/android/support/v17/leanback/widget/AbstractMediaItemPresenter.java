package android.support.v17.leanback.widget;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Rect;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.MultiActionsProvider.MultiAction;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.ViewFlipper;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMediaItemPresenter extends RowPresenter {
    public static final int PLAY_STATE_INITIAL = 0;
    public static final int PLAY_STATE_PAUSED = 1;
    public static final int PLAY_STATE_PLAYING = 2;
    static final Rect sTempRect = new Rect();
    private int mBackgroundColor;
    private boolean mBackgroundColorSet;
    private Presenter mMediaItemActionPresenter;
    private boolean mMediaRowSeparator;
    private int mThemeId;

    public static class ViewHolder extends android.support.v17.leanback.widget.RowPresenter.ViewHolder {
        private final List<android.support.v17.leanback.widget.Presenter.ViewHolder> mActionViewHolders = new ArrayList();
        ValueAnimator mFocusViewAnimator;
        private final ViewGroup mMediaItemActionsContainer;
        private final View mMediaItemDetailsView;
        private final TextView mMediaItemDurationView;
        private final TextView mMediaItemNameView;
        final TextView mMediaItemNumberView;
        final ViewFlipper mMediaItemNumberViewFlipper;
        final View mMediaItemPausedView;
        final View mMediaItemPlayingView;
        MultiAction[] mMediaItemRowActions;
        private final View mMediaItemRowSeparator;
        final View mMediaRowView;
        AbstractMediaItemPresenter mRowPresenter;
        final View mSelectorView;

        public ViewHolder(View view) {
            super(view);
            this.mSelectorView = view.findViewById(R.id.mediaRowSelector);
            this.mMediaRowView = view.findViewById(R.id.mediaItemRow);
            this.mMediaItemDetailsView = view.findViewById(R.id.mediaItemDetails);
            this.mMediaItemNameView = (TextView) view.findViewById(R.id.mediaItemName);
            this.mMediaItemDurationView = (TextView) view.findViewById(R.id.mediaItemDuration);
            this.mMediaItemRowSeparator = view.findViewById(R.id.mediaRowSeparator);
            this.mMediaItemActionsContainer = (ViewGroup) view.findViewById(R.id.mediaItemActionsContainer);
            getMediaItemDetailsView().setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (ViewHolder.this.getOnItemViewClickedListener() != null) {
                        ViewHolder.this.getOnItemViewClickedListener().onItemClicked(null, null, ViewHolder.this, ViewHolder.this.getRowObject());
                    }
                }
            });
            getMediaItemDetailsView().setOnFocusChangeListener(new OnFocusChangeListener() {
                public void onFocusChange(View view, boolean hasFocus) {
                    ViewHolder.this.mFocusViewAnimator = AbstractMediaItemPresenter.updateSelector(ViewHolder.this.mSelectorView, view, ViewHolder.this.mFocusViewAnimator, true);
                }
            });
            this.mMediaItemNumberViewFlipper = (ViewFlipper) view.findViewById(R.id.mediaItemNumberViewFlipper);
            TypedValue typedValue = new TypedValue();
            View mergeView = LayoutInflater.from(view.getContext()).inflate(view.getContext().getTheme().resolveAttribute(R.attr.playbackMediaItemNumberViewFlipperLayout, typedValue, true) ? typedValue.resourceId : R.layout.lb_media_item_number_view_flipper, this.mMediaItemNumberViewFlipper, true);
            this.mMediaItemNumberView = (TextView) mergeView.findViewById(R.id.initial);
            this.mMediaItemPausedView = mergeView.findViewById(R.id.paused);
            this.mMediaItemPlayingView = mergeView.findViewById(R.id.playing);
        }

        public void onBindRowActions() {
            int i = getMediaItemActionsContainer().getChildCount();
            while (true) {
                i--;
                if (i < this.mActionViewHolders.size()) {
                    break;
                }
                getMediaItemActionsContainer().removeViewAt(i);
                this.mActionViewHolders.remove(i);
            }
            this.mMediaItemRowActions = null;
            Object rowObject = getRowObject();
            if (rowObject instanceof MultiActionsProvider) {
                MultiAction[] actionList = ((MultiActionsProvider) rowObject).getActions();
                Presenter actionPresenter = this.mRowPresenter.getActionPresenter();
                if (actionPresenter != null) {
                    int i2;
                    this.mMediaItemRowActions = actionList;
                    for (i2 = this.mActionViewHolders.size(); i2 < actionList.length; i2++) {
                        final int actionIndex = i2;
                        final android.support.v17.leanback.widget.Presenter.ViewHolder actionViewHolder = actionPresenter.onCreateViewHolder(getMediaItemActionsContainer());
                        getMediaItemActionsContainer().addView(actionViewHolder.view);
                        this.mActionViewHolders.add(actionViewHolder);
                        actionViewHolder.view.setOnFocusChangeListener(new OnFocusChangeListener() {
                            public void onFocusChange(View view, boolean hasFocus) {
                                ViewHolder.this.mFocusViewAnimator = AbstractMediaItemPresenter.updateSelector(ViewHolder.this.mSelectorView, view, ViewHolder.this.mFocusViewAnimator, false);
                            }
                        });
                        actionViewHolder.view.setOnClickListener(new OnClickListener() {
                            public void onClick(View view) {
                                if (ViewHolder.this.getOnItemViewClickedListener() != null) {
                                    ViewHolder.this.getOnItemViewClickedListener().onItemClicked(actionViewHolder, ViewHolder.this.mMediaItemRowActions[actionIndex], ViewHolder.this, ViewHolder.this.getRowObject());
                                }
                            }
                        });
                    }
                    if (this.mMediaItemActionsContainer != null) {
                        for (i2 = 0; i2 < actionList.length; i2++) {
                            android.support.v17.leanback.widget.Presenter.ViewHolder avh = (android.support.v17.leanback.widget.Presenter.ViewHolder) this.mActionViewHolders.get(i2);
                            actionPresenter.onUnbindViewHolder(avh);
                            actionPresenter.onBindViewHolder(avh, this.mMediaItemRowActions[i2]);
                        }
                    }
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public int findActionIndex(MultiAction action) {
            if (this.mMediaItemRowActions != null) {
                for (int i = 0; i < this.mMediaItemRowActions.length; i++) {
                    if (this.mMediaItemRowActions[i] == action) {
                        return i;
                    }
                }
            }
            return -1;
        }

        public void notifyActionChanged(MultiAction action) {
            Presenter actionPresenter = this.mRowPresenter.getActionPresenter();
            if (actionPresenter != null) {
                int actionIndex = findActionIndex(action);
                if (actionIndex >= 0) {
                    android.support.v17.leanback.widget.Presenter.ViewHolder actionViewHolder = (android.support.v17.leanback.widget.Presenter.ViewHolder) this.mActionViewHolders.get(actionIndex);
                    actionPresenter.onUnbindViewHolder(actionViewHolder);
                    actionPresenter.onBindViewHolder(actionViewHolder, action);
                }
            }
        }

        public void notifyDetailsChanged() {
            this.mRowPresenter.onUnbindMediaDetails(this);
            this.mRowPresenter.onBindMediaDetails(this, getRowObject());
        }

        public void notifyPlayStateChanged() {
            this.mRowPresenter.onBindMediaPlayState(this);
        }

        public View getSelectorView() {
            return this.mSelectorView;
        }

        public ViewFlipper getMediaItemNumberViewFlipper() {
            return this.mMediaItemNumberViewFlipper;
        }

        public TextView getMediaItemNumberView() {
            return this.mMediaItemNumberView;
        }

        public View getMediaItemPausedView() {
            return this.mMediaItemPausedView;
        }

        public View getMediaItemPlayingView() {
            return this.mMediaItemPlayingView;
        }

        public void setSelectedMediaItemNumberView(int position) {
            if (position >= 0 && position < this.mMediaItemNumberViewFlipper.getChildCount()) {
                this.mMediaItemNumberViewFlipper.setDisplayedChild(position);
            }
        }

        public TextView getMediaItemNameView() {
            return this.mMediaItemNameView;
        }

        public TextView getMediaItemDurationView() {
            return this.mMediaItemDurationView;
        }

        public View getMediaItemDetailsView() {
            return this.mMediaItemDetailsView;
        }

        public View getMediaItemRowSeparator() {
            return this.mMediaItemRowSeparator;
        }

        public ViewGroup getMediaItemActionsContainer() {
            return this.mMediaItemActionsContainer;
        }

        public MultiAction[] getMediaItemRowActions() {
            return this.mMediaItemRowActions;
        }
    }

    public abstract void onBindMediaDetails(ViewHolder viewHolder, Object obj);

    public AbstractMediaItemPresenter() {
        this(0);
    }

    public AbstractMediaItemPresenter(int themeId) {
        this.mBackgroundColor = 0;
        this.mMediaItemActionPresenter = new MediaItemActionPresenter();
        this.mThemeId = themeId;
        setHeaderPresenter(null);
    }

    public void setThemeId(int themeId) {
        this.mThemeId = themeId;
    }

    public int getThemeId() {
        return this.mThemeId;
    }

    public void setActionPresenter(Presenter actionPresenter) {
        this.mMediaItemActionPresenter = actionPresenter;
    }

    public Presenter getActionPresenter() {
        return this.mMediaItemActionPresenter;
    }

    /* Access modifiers changed, original: protected */
    public android.support.v17.leanback.widget.RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        if (this.mThemeId != 0) {
            context = new ContextThemeWrapper(context, this.mThemeId);
        }
        ViewHolder vh = new ViewHolder(LayoutInflater.from(context).inflate(R.layout.lb_row_media_item, parent, false));
        vh.mRowPresenter = this;
        if (this.mBackgroundColorSet) {
            vh.mMediaRowView.setBackgroundColor(this.mBackgroundColor);
        }
        return vh;
    }

    public boolean isUsingDefaultSelectEffect() {
        return false;
    }

    /* Access modifiers changed, original: protected */
    public boolean isClippingChildren() {
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onBindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder vh, Object item) {
        super.onBindRowViewHolder(vh, item);
        ViewHolder mvh = (ViewHolder) vh;
        onBindRowActions(mvh);
        mvh.getMediaItemRowSeparator().setVisibility(hasMediaRowSeparator() ? 0 : 8);
        onBindMediaPlayState(mvh);
        onBindMediaDetails((ViewHolder) vh, item);
    }

    /* Access modifiers changed, original: protected */
    public void onBindRowActions(ViewHolder vh) {
        vh.onBindRowActions();
    }

    public void setBackgroundColor(int color) {
        this.mBackgroundColorSet = true;
        this.mBackgroundColor = color;
    }

    public void setHasMediaRowSeparator(boolean hasSeparator) {
        this.mMediaRowSeparator = hasSeparator;
    }

    public boolean hasMediaRowSeparator() {
        return this.mMediaRowSeparator;
    }

    /* Access modifiers changed, original: protected */
    public void onUnbindMediaDetails(ViewHolder vh) {
    }

    public void onBindMediaPlayState(ViewHolder vh) {
        int childIndex = calculateMediaItemNumberFlipperIndex(vh);
        if (childIndex != -1 && vh.mMediaItemNumberViewFlipper.getDisplayedChild() != childIndex) {
            vh.mMediaItemNumberViewFlipper.setDisplayedChild(childIndex);
        }
    }

    static int calculateMediaItemNumberFlipperIndex(ViewHolder vh) {
        int i = -1;
        switch (vh.mRowPresenter.getMediaPlayState(vh.getRowObject())) {
            case 0:
                if (vh.mMediaItemNumberView != null) {
                    i = vh.mMediaItemNumberViewFlipper.indexOfChild(vh.mMediaItemNumberView);
                }
                return i;
            case 1:
                if (vh.mMediaItemPausedView != null) {
                    i = vh.mMediaItemNumberViewFlipper.indexOfChild(vh.mMediaItemPausedView);
                }
                return i;
            case 2:
                if (vh.mMediaItemPlayingView != null) {
                    i = vh.mMediaItemNumberViewFlipper.indexOfChild(vh.mMediaItemPlayingView);
                }
                return i;
            default:
                return -1;
        }
    }

    public void onUnbindMediaPlayState(ViewHolder vh) {
    }

    /* Access modifiers changed, original: protected */
    public int getMediaPlayState(Object item) {
        return 0;
    }

    static ValueAnimator updateSelector(View selectorView, View focusChangedView, ValueAnimator layoutAnimator, boolean isDetails) {
        int animationDuration = focusChangedView.getContext().getResources().getInteger(17694720);
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        int layoutDirection = ViewCompat.getLayoutDirection(selectorView);
        if (focusChangedView.hasFocus()) {
            ValueAnimator layoutAnimator2;
            float deltaLeft;
            float deltaWidth;
            if (layoutAnimator != null) {
                layoutAnimator.cancel();
                layoutAnimator2 = null;
            } else {
                layoutAnimator2 = layoutAnimator;
            }
            float currentAlpha = selectorView.getAlpha();
            selectorView.animate().alpha(1.0f).setDuration((long) animationDuration).setInterpolator(interpolator).start();
            MarginLayoutParams lp = (MarginLayoutParams) selectorView.getLayoutParams();
            ViewGroup rootView = (ViewGroup) selectorView.getParent();
            sTempRect.set(0, 0, focusChangedView.getWidth(), focusChangedView.getHeight());
            rootView.offsetDescendantRectToMyCoords(focusChangedView, sTempRect);
            if (isDetails) {
                Rect rect;
                if (layoutDirection == 1) {
                    rect = sTempRect;
                    rect.right += rootView.getHeight();
                    rect = sTempRect;
                    rect.left -= rootView.getHeight() / 2;
                } else {
                    rect = sTempRect;
                    rect.left -= rootView.getHeight();
                    rect = sTempRect;
                    rect.right += rootView.getHeight() / 2;
                }
            }
            int targetLeft = sTempRect.left;
            int targetWidth = sTempRect.width();
            float deltaWidth2 = (float) (lp.width - targetWidth);
            final float f = (float) (lp.leftMargin - targetLeft);
            if (!(f == 0.0f && deltaWidth2 == 0.0f)) {
                if (currentAlpha == 0.0f) {
                    lp.width = targetWidth;
                    lp.leftMargin = targetLeft;
                    selectorView.requestLayout();
                } else {
                    layoutAnimator2 = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                    layoutAnimator2.setDuration((long) animationDuration);
                    layoutAnimator2.setInterpolator(interpolator);
                    final MarginLayoutParams marginLayoutParams = lp;
                    final int i = targetLeft;
                    deltaLeft = f;
                    deltaWidth = deltaWidth2;
                    final int i2 = targetWidth;
                    final float f2 = deltaWidth;
                    targetLeft = selectorView;
                    layoutAnimator2.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float fractionToEnd = 1.0f - valueAnimator.getAnimatedFraction();
                            marginLayoutParams.leftMargin = Math.round(((float) i) + (f * fractionToEnd));
                            marginLayoutParams.width = Math.round(((float) i2) + (f2 * fractionToEnd));
                            targetLeft.requestLayout();
                        }
                    });
                    layoutAnimator2.start();
                    return layoutAnimator2;
                }
            }
            deltaLeft = f;
            deltaWidth = deltaWidth2;
            int i3 = targetWidth;
            int i4 = targetLeft;
            return layoutAnimator2;
        }
        selectorView.animate().cancel();
        selectorView.animate().alpha(0.0f).setDuration((long) animationDuration).setInterpolator(interpolator).start();
        return layoutAnimator;
    }
}
