package android.support.v17.leanback.widget;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.PlaybackControlsRow.OnPlaybackProgressCallback;
import android.support.v17.leanback.widget.PlaybackControlsRowView.OnUnhandledKeyListener;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PlaybackControlsRowPresenter extends PlaybackRowPresenter {
    static float sShadowZ;
    private int mBackgroundColor;
    private boolean mBackgroundColorSet;
    private Presenter mDescriptionPresenter;
    OnActionClickedListener mOnActionClickedListener;
    private final OnControlClickedListener mOnControlClickedListener;
    private final OnControlSelectedListener mOnControlSelectedListener;
    PlaybackControlsPresenter mPlaybackControlsPresenter;
    private int mProgressColor;
    private boolean mProgressColorSet;
    private boolean mSecondaryActionsHidden;
    private ControlBarPresenter mSecondaryControlsPresenter;

    static class BoundData extends BoundData {
        ViewHolder mRowViewHolder;

        BoundData() {
        }
    }

    public class ViewHolder extends android.support.v17.leanback.widget.PlaybackRowPresenter.ViewHolder {
        View mBgView;
        final View mBottomSpacer;
        final ViewGroup mCard;
        final ViewGroup mCardRightPanel;
        BoundData mControlsBoundData = new BoundData();
        final ViewGroup mControlsDock;
        int mControlsDockMarginEnd;
        int mControlsDockMarginStart;
        ViewHolder mControlsVh;
        final ViewGroup mDescriptionDock;
        public final android.support.v17.leanback.widget.Presenter.ViewHolder mDescriptionViewHolder;
        final ImageView mImageView;
        final OnPlaybackProgressCallback mListener = new OnPlaybackProgressCallback() {
            public void onCurrentPositionChanged(PlaybackControlsRow row, long ms) {
                PlaybackControlsRowPresenter.this.mPlaybackControlsPresenter.setCurrentTimeLong(ViewHolder.this.mControlsVh, ms);
            }

            public void onDurationChanged(PlaybackControlsRow row, long ms) {
                PlaybackControlsRowPresenter.this.mPlaybackControlsPresenter.setTotalTimeLong(ViewHolder.this.mControlsVh, ms);
            }

            public void onBufferedPositionChanged(PlaybackControlsRow row, long ms) {
                PlaybackControlsRowPresenter.this.mPlaybackControlsPresenter.setSecondaryProgressLong(ViewHolder.this.mControlsVh, ms);
            }
        };
        BoundData mSecondaryBoundData = new BoundData();
        final ViewGroup mSecondaryControlsDock;
        android.support.v17.leanback.widget.Presenter.ViewHolder mSecondaryControlsVh;
        Object mSelectedItem;
        android.support.v17.leanback.widget.Presenter.ViewHolder mSelectedViewHolder;
        final View mSpacer;

        ViewHolder(View rootView, Presenter descriptionPresenter) {
            android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder;
            super(rootView);
            this.mCard = (ViewGroup) rootView.findViewById(R.id.controls_card);
            this.mCardRightPanel = (ViewGroup) rootView.findViewById(R.id.controls_card_right_panel);
            this.mImageView = (ImageView) rootView.findViewById(R.id.image);
            this.mDescriptionDock = (ViewGroup) rootView.findViewById(R.id.description_dock);
            this.mControlsDock = (ViewGroup) rootView.findViewById(R.id.controls_dock);
            this.mSecondaryControlsDock = (ViewGroup) rootView.findViewById(R.id.secondary_controls_dock);
            this.mSpacer = rootView.findViewById(R.id.spacer);
            this.mBottomSpacer = rootView.findViewById(R.id.bottom_spacer);
            if (descriptionPresenter == null) {
                viewHolder = null;
            } else {
                viewHolder = descriptionPresenter.onCreateViewHolder(this.mDescriptionDock);
            }
            this.mDescriptionViewHolder = viewHolder;
            if (this.mDescriptionViewHolder != null) {
                this.mDescriptionDock.addView(this.mDescriptionViewHolder.view);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void dispatchItemSelection() {
            if (isSelected()) {
                if (this.mSelectedViewHolder == null) {
                    if (getOnItemViewSelectedListener() != null) {
                        getOnItemViewSelectedListener().onItemSelected(null, null, this, getRow());
                    }
                } else if (getOnItemViewSelectedListener() != null) {
                    getOnItemViewSelectedListener().onItemSelected(this.mSelectedViewHolder, this.mSelectedItem, this, getRow());
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public Presenter getPresenter(boolean primary) {
            ObjectAdapter adapter;
            if (primary) {
                adapter = ((PlaybackControlsRow) getRow()).getPrimaryActionsAdapter();
            } else {
                adapter = ((PlaybackControlsRow) getRow()).getSecondaryActionsAdapter();
            }
            Object obj = null;
            if (adapter == null) {
                return null;
            }
            if (adapter.getPresenterSelector() instanceof ControlButtonPresenterSelector) {
                Presenter primaryPresenter;
                ControlButtonPresenterSelector selector = (ControlButtonPresenterSelector) adapter.getPresenterSelector();
                if (primary) {
                    primaryPresenter = selector.getPrimaryPresenter();
                } else {
                    primaryPresenter = selector.getSecondaryPresenter();
                }
                return primaryPresenter;
            }
            if (adapter.size() > 0) {
                obj = adapter.get(0);
            }
            return adapter.getPresenter(obj);
        }

        /* Access modifiers changed, original: 0000 */
        public void setOutline(View view) {
            if (this.mBgView != null) {
                RoundedRectHelper.setClipToRoundedOutline(this.mBgView, false);
                ViewCompat.setZ(this.mBgView, 0.0f);
            }
            this.mBgView = view;
            RoundedRectHelper.setClipToRoundedOutline(view, true);
            if (PlaybackControlsRowPresenter.sShadowZ == 0.0f) {
                PlaybackControlsRowPresenter.sShadowZ = (float) view.getResources().getDimensionPixelSize(R.dimen.lb_playback_controls_z);
            }
            ViewCompat.setZ(view, PlaybackControlsRowPresenter.sShadowZ);
        }
    }

    public PlaybackControlsRowPresenter(Presenter descriptionPresenter) {
        this.mBackgroundColor = 0;
        this.mProgressColor = 0;
        this.mOnControlSelectedListener = new OnControlSelectedListener() {
            public void onControlSelected(android.support.v17.leanback.widget.Presenter.ViewHolder itemViewHolder, Object item, BoundData data) {
                ViewHolder vh = ((BoundData) data).mRowViewHolder;
                if (vh.mSelectedViewHolder != itemViewHolder || vh.mSelectedItem != item) {
                    vh.mSelectedViewHolder = itemViewHolder;
                    vh.mSelectedItem = item;
                    vh.dispatchItemSelection();
                }
            }
        };
        this.mOnControlClickedListener = new OnControlClickedListener() {
            public void onControlClicked(android.support.v17.leanback.widget.Presenter.ViewHolder itemViewHolder, Object item, BoundData data) {
                ViewHolder vh = ((BoundData) data).mRowViewHolder;
                if (vh.getOnItemViewClickedListener() != null) {
                    vh.getOnItemViewClickedListener().onItemClicked(itemViewHolder, item, vh, vh.getRow());
                }
                if (PlaybackControlsRowPresenter.this.mOnActionClickedListener != null && (item instanceof Action)) {
                    PlaybackControlsRowPresenter.this.mOnActionClickedListener.onActionClicked((Action) item);
                }
            }
        };
        setHeaderPresenter(null);
        setSelectEffectEnabled(false);
        this.mDescriptionPresenter = descriptionPresenter;
        this.mPlaybackControlsPresenter = new PlaybackControlsPresenter(R.layout.lb_playback_controls);
        this.mSecondaryControlsPresenter = new ControlBarPresenter(R.layout.lb_control_bar);
        this.mPlaybackControlsPresenter.setOnControlSelectedListener(this.mOnControlSelectedListener);
        this.mSecondaryControlsPresenter.setOnControlSelectedListener(this.mOnControlSelectedListener);
        this.mPlaybackControlsPresenter.setOnControlClickedListener(this.mOnControlClickedListener);
        this.mSecondaryControlsPresenter.setOnControlClickedListener(this.mOnControlClickedListener);
    }

    public PlaybackControlsRowPresenter() {
        this(null);
    }

    public void setOnActionClickedListener(OnActionClickedListener listener) {
        this.mOnActionClickedListener = listener;
    }

    public OnActionClickedListener getOnActionClickedListener() {
        return this.mOnActionClickedListener;
    }

    public void setBackgroundColor(@ColorInt int color) {
        this.mBackgroundColor = color;
        this.mBackgroundColorSet = true;
    }

    @ColorInt
    public int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public void setProgressColor(@ColorInt int color) {
        this.mProgressColor = color;
        this.mProgressColorSet = true;
    }

    @ColorInt
    public int getProgressColor() {
        return this.mProgressColor;
    }

    public void setSecondaryActionsHidden(boolean hidden) {
        this.mSecondaryActionsHidden = hidden;
    }

    public boolean areSecondaryActionsHidden() {
        return this.mSecondaryActionsHidden;
    }

    public void showBottomSpace(ViewHolder vh, boolean show) {
        vh.mBottomSpacer.setVisibility(show ? 0 : 8);
    }

    public void showPrimaryActions(ViewHolder vh) {
        this.mPlaybackControlsPresenter.showPrimaryActions(vh.mControlsVh);
        if (vh.view.hasFocus()) {
            this.mPlaybackControlsPresenter.resetFocus(vh.mControlsVh);
        }
    }

    public void onReappear(android.support.v17.leanback.widget.RowPresenter.ViewHolder rowViewHolder) {
        showPrimaryActions((ViewHolder) rowViewHolder);
    }

    private int getDefaultBackgroundColor(Context context) {
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.defaultBrandColor, outValue, true)) {
            return context.getResources().getColor(outValue.resourceId);
        }
        return context.getResources().getColor(R.color.lb_default_brand_color);
    }

    private int getDefaultProgressColor(Context context) {
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.playbackProgressPrimaryColor, outValue, true)) {
            return context.getResources().getColor(outValue.resourceId);
        }
        return context.getResources().getColor(R.color.lb_playback_progress_color_no_theme);
    }

    /* Access modifiers changed, original: protected */
    public android.support.v17.leanback.widget.RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
        ViewHolder vh = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_playback_controls_row, parent, false), this.mDescriptionPresenter);
        initRow(vh);
        return vh;
    }

    private void initRow(final ViewHolder vh) {
        int i;
        MarginLayoutParams lp = (MarginLayoutParams) vh.mControlsDock.getLayoutParams();
        vh.mControlsDockMarginStart = lp.getMarginStart();
        vh.mControlsDockMarginEnd = lp.getMarginEnd();
        vh.mControlsVh = (ViewHolder) this.mPlaybackControlsPresenter.onCreateViewHolder(vh.mControlsDock);
        PlaybackControlsPresenter playbackControlsPresenter = this.mPlaybackControlsPresenter;
        ViewHolder viewHolder = vh.mControlsVh;
        if (this.mProgressColorSet) {
            i = this.mProgressColor;
        } else {
            i = getDefaultProgressColor(vh.mControlsDock.getContext());
        }
        playbackControlsPresenter.setProgressColor(viewHolder, i);
        playbackControlsPresenter = this.mPlaybackControlsPresenter;
        viewHolder = vh.mControlsVh;
        if (this.mBackgroundColorSet) {
            i = this.mBackgroundColor;
        } else {
            i = getDefaultBackgroundColor(vh.view.getContext());
        }
        playbackControlsPresenter.setBackgroundColor(viewHolder, i);
        vh.mControlsDock.addView(vh.mControlsVh.view);
        vh.mSecondaryControlsVh = this.mSecondaryControlsPresenter.onCreateViewHolder(vh.mSecondaryControlsDock);
        if (!this.mSecondaryActionsHidden) {
            vh.mSecondaryControlsDock.addView(vh.mSecondaryControlsVh.view);
        }
        ((PlaybackControlsRowView) vh.view).setOnUnhandledKeyListener(new OnUnhandledKeyListener() {
            public boolean onUnhandledKey(KeyEvent event) {
                if (vh.getOnKeyListener() == null || !vh.getOnKeyListener().onKey(vh.view, event.getKeyCode(), event)) {
                    return false;
                }
                return true;
            }
        });
    }

    /* Access modifiers changed, original: protected */
    public void onBindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder, Object item) {
        super.onBindRowViewHolder(holder, item);
        ViewHolder vh = (ViewHolder) holder;
        PlaybackControlsRow row = (PlaybackControlsRow) vh.getRow();
        this.mPlaybackControlsPresenter.enableSecondaryActions(this.mSecondaryActionsHidden);
        if (row.getItem() == null) {
            vh.mDescriptionDock.setVisibility(8);
            vh.mSpacer.setVisibility(8);
        } else {
            vh.mDescriptionDock.setVisibility(0);
            if (vh.mDescriptionViewHolder != null) {
                this.mDescriptionPresenter.onBindViewHolder(vh.mDescriptionViewHolder, row.getItem());
            }
            vh.mSpacer.setVisibility(0);
        }
        if (row.getImageDrawable() == null || row.getItem() == null) {
            vh.mImageView.setImageDrawable(null);
            updateCardLayout(vh, -2);
        } else {
            vh.mImageView.setImageDrawable(row.getImageDrawable());
            updateCardLayout(vh, vh.mImageView.getLayoutParams().height);
        }
        vh.mControlsBoundData.adapter = row.getPrimaryActionsAdapter();
        vh.mControlsBoundData.secondaryActionsAdapter = row.getSecondaryActionsAdapter();
        vh.mControlsBoundData.presenter = vh.getPresenter(true);
        vh.mControlsBoundData.mRowViewHolder = vh;
        this.mPlaybackControlsPresenter.onBindViewHolder(vh.mControlsVh, vh.mControlsBoundData);
        vh.mSecondaryBoundData.adapter = row.getSecondaryActionsAdapter();
        vh.mSecondaryBoundData.presenter = vh.getPresenter(false);
        vh.mSecondaryBoundData.mRowViewHolder = vh;
        this.mSecondaryControlsPresenter.onBindViewHolder(vh.mSecondaryControlsVh, vh.mSecondaryBoundData);
        this.mPlaybackControlsPresenter.setTotalTime(vh.mControlsVh, row.getTotalTime());
        this.mPlaybackControlsPresenter.setCurrentTime(vh.mControlsVh, row.getCurrentTime());
        this.mPlaybackControlsPresenter.setSecondaryProgress(vh.mControlsVh, row.getBufferedProgress());
        row.setOnPlaybackProgressChangedListener(vh.mListener);
    }

    private void updateCardLayout(ViewHolder vh, int height) {
        LayoutParams lp = vh.mCardRightPanel.getLayoutParams();
        lp.height = height;
        vh.mCardRightPanel.setLayoutParams(lp);
        MarginLayoutParams mlp = (MarginLayoutParams) vh.mControlsDock.getLayoutParams();
        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) vh.mDescriptionDock.getLayoutParams();
        if (height == -2) {
            llp.height = -2;
            mlp.setMarginStart(0);
            mlp.setMarginEnd(0);
            vh.mCard.setBackground(null);
            vh.setOutline(vh.mControlsDock);
            this.mPlaybackControlsPresenter.enableTimeMargins(vh.mControlsVh, true);
        } else {
            int i;
            llp.height = 0;
            llp.weight = 1.0f;
            mlp.setMarginStart(vh.mControlsDockMarginStart);
            mlp.setMarginEnd(vh.mControlsDockMarginEnd);
            ViewGroup viewGroup = vh.mCard;
            if (this.mBackgroundColorSet) {
                i = this.mBackgroundColor;
            } else {
                i = getDefaultBackgroundColor(vh.mCard.getContext());
            }
            viewGroup.setBackgroundColor(i);
            vh.setOutline(vh.mCard);
            this.mPlaybackControlsPresenter.enableTimeMargins(vh.mControlsVh, false);
        }
        vh.mDescriptionDock.setLayoutParams(llp);
        vh.mControlsDock.setLayoutParams(mlp);
    }

    /* Access modifiers changed, original: protected */
    public void onUnbindRowViewHolder(android.support.v17.leanback.widget.RowPresenter.ViewHolder holder) {
        ViewHolder vh = (ViewHolder) holder;
        PlaybackControlsRow row = (PlaybackControlsRow) vh.getRow();
        if (vh.mDescriptionViewHolder != null) {
            this.mDescriptionPresenter.onUnbindViewHolder(vh.mDescriptionViewHolder);
        }
        this.mPlaybackControlsPresenter.onUnbindViewHolder(vh.mControlsVh);
        this.mSecondaryControlsPresenter.onUnbindViewHolder(vh.mSecondaryControlsVh);
        row.setOnPlaybackProgressChangedListener(null);
        super.onUnbindRowViewHolder(holder);
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewSelected(android.support.v17.leanback.widget.RowPresenter.ViewHolder vh, boolean selected) {
        super.onRowViewSelected(vh, selected);
        if (selected) {
            ((ViewHolder) vh).dispatchItemSelection();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewAttachedToWindow(android.support.v17.leanback.widget.RowPresenter.ViewHolder vh) {
        super.onRowViewAttachedToWindow(vh);
        if (this.mDescriptionPresenter != null) {
            this.mDescriptionPresenter.onViewAttachedToWindow(((ViewHolder) vh).mDescriptionViewHolder);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onRowViewDetachedFromWindow(android.support.v17.leanback.widget.RowPresenter.ViewHolder vh) {
        super.onRowViewDetachedFromWindow(vh);
        if (this.mDescriptionPresenter != null) {
            this.mDescriptionPresenter.onViewDetachedFromWindow(((ViewHolder) vh).mDescriptionViewHolder);
        }
    }
}
