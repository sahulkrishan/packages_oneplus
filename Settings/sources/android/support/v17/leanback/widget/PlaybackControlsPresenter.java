package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R;
import android.support.v17.leanback.util.MathUtil;
import android.support.v17.leanback.widget.ObjectAdapter.DataObserver;
import android.support.v17.leanback.widget.PlaybackControlsRow.MoreActions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.settingslib.accessibility.AccessibilityUtils;

class PlaybackControlsPresenter extends ControlBarPresenter {
    private static int sChildMarginBigger;
    private static int sChildMarginBiggest;
    private boolean mMoreActionsEnabled = true;

    static class BoundData extends BoundData {
        ObjectAdapter secondaryActionsAdapter;

        BoundData() {
        }
    }

    class ViewHolder extends ViewHolder {
        final TextView mCurrentTime;
        long mCurrentTimeInMs = -1;
        int mCurrentTimeMarginStart;
        StringBuilder mCurrentTimeStringBuilder = new StringBuilder();
        ObjectAdapter mMoreActionsAdapter;
        final FrameLayout mMoreActionsDock;
        DataObserver mMoreActionsObserver;
        boolean mMoreActionsShowing;
        android.support.v17.leanback.widget.Presenter.ViewHolder mMoreActionsViewHolder;
        final ProgressBar mProgressBar;
        long mSecondaryProgressInMs = -1;
        final TextView mTotalTime;
        long mTotalTimeInMs = -1;
        int mTotalTimeMarginEnd;
        StringBuilder mTotalTimeStringBuilder = new StringBuilder();

        ViewHolder(View rootView) {
            super(rootView);
            this.mMoreActionsDock = (FrameLayout) rootView.findViewById(R.id.more_actions_dock);
            this.mCurrentTime = (TextView) rootView.findViewById(R.id.current_time);
            this.mTotalTime = (TextView) rootView.findViewById(R.id.total_time);
            this.mProgressBar = (ProgressBar) rootView.findViewById(R.id.playback_progress);
            this.mMoreActionsObserver = new DataObserver(PlaybackControlsPresenter.this) {
                public void onChanged() {
                    if (ViewHolder.this.mMoreActionsShowing) {
                        ViewHolder.this.showControls(ViewHolder.this.mPresenter);
                    }
                }

                public void onItemRangeChanged(int positionStart, int itemCount) {
                    if (ViewHolder.this.mMoreActionsShowing) {
                        for (int i = 0; i < itemCount; i++) {
                            ViewHolder.this.bindControlToAction(positionStart + i, ViewHolder.this.mPresenter);
                        }
                    }
                }
            };
            this.mCurrentTimeMarginStart = ((MarginLayoutParams) this.mCurrentTime.getLayoutParams()).getMarginStart();
            this.mTotalTimeMarginEnd = ((MarginLayoutParams) this.mTotalTime.getLayoutParams()).getMarginEnd();
        }

        /* Access modifiers changed, original: 0000 */
        public void showMoreActions(boolean show) {
            if (show) {
                if (this.mMoreActionsViewHolder == null) {
                    Action action = new MoreActions(this.mMoreActionsDock.getContext());
                    this.mMoreActionsViewHolder = this.mPresenter.onCreateViewHolder(this.mMoreActionsDock);
                    this.mPresenter.onBindViewHolder(this.mMoreActionsViewHolder, action);
                    this.mPresenter.setOnClickListener(this.mMoreActionsViewHolder, new OnClickListener() {
                        public void onClick(View v) {
                            ViewHolder.this.toggleMoreActions();
                        }
                    });
                }
                if (this.mMoreActionsViewHolder.view.getParent() == null) {
                    this.mMoreActionsDock.addView(this.mMoreActionsViewHolder.view);
                }
            } else if (this.mMoreActionsViewHolder != null && this.mMoreActionsViewHolder.view.getParent() != null) {
                this.mMoreActionsDock.removeView(this.mMoreActionsViewHolder.view);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void toggleMoreActions() {
            this.mMoreActionsShowing ^= 1;
            showControls(this.mPresenter);
        }

        /* Access modifiers changed, original: 0000 */
        public ObjectAdapter getDisplayedAdapter() {
            return this.mMoreActionsShowing ? this.mMoreActionsAdapter : this.mAdapter;
        }

        /* Access modifiers changed, original: 0000 */
        public int getChildMarginFromCenter(Context context, int numControls) {
            int margin = PlaybackControlsPresenter.this.getControlIconWidth(context);
            if (numControls < 4) {
                return margin + PlaybackControlsPresenter.this.getChildMarginBiggest(context);
            }
            if (numControls < 6) {
                return margin + PlaybackControlsPresenter.this.getChildMarginBigger(context);
            }
            return margin + PlaybackControlsPresenter.this.getChildMarginDefault(context);
        }

        /* Access modifiers changed, original: 0000 */
        public void setTotalTime(long totalTimeMs) {
            if (totalTimeMs <= 0) {
                this.mTotalTime.setVisibility(8);
                this.mProgressBar.setVisibility(8);
                return;
            }
            this.mTotalTime.setVisibility(0);
            this.mProgressBar.setVisibility(0);
            this.mTotalTimeInMs = totalTimeMs;
            PlaybackControlsPresenter.formatTime(totalTimeMs / 1000, this.mTotalTimeStringBuilder);
            this.mTotalTime.setText(this.mTotalTimeStringBuilder.toString());
            this.mProgressBar.setMax(Integer.MAX_VALUE);
        }

        /* Access modifiers changed, original: 0000 */
        public long getTotalTime() {
            return this.mTotalTimeInMs;
        }

        /* Access modifiers changed, original: 0000 */
        public void setCurrentTime(long currentTimeMs) {
            long seconds = currentTimeMs / 1000;
            if (currentTimeMs != this.mCurrentTimeInMs) {
                this.mCurrentTimeInMs = currentTimeMs;
                PlaybackControlsPresenter.formatTime(seconds, this.mCurrentTimeStringBuilder);
                this.mCurrentTime.setText(this.mCurrentTimeStringBuilder.toString());
            }
            this.mProgressBar.setProgress((int) (2.147483647E9d * (((double) this.mCurrentTimeInMs) / ((double) this.mTotalTimeInMs))));
        }

        /* Access modifiers changed, original: 0000 */
        public long getCurrentTime() {
            return this.mTotalTimeInMs;
        }

        /* Access modifiers changed, original: 0000 */
        public void setSecondaryProgress(long progressMs) {
            this.mSecondaryProgressInMs = progressMs;
            this.mProgressBar.setSecondaryProgress((int) (2.147483647E9d * (((double) progressMs) / ((double) this.mTotalTimeInMs))));
        }

        /* Access modifiers changed, original: 0000 */
        public long getSecondaryProgress() {
            return this.mSecondaryProgressInMs;
        }
    }

    static void formatTime(long seconds, StringBuilder sb) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds -= minutes * 60;
        minutes -= 60 * hours;
        sb.setLength(0);
        if (hours > 0) {
            sb.append(hours);
            sb.append(AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
            if (minutes < 10) {
                sb.append('0');
            }
        }
        sb.append(minutes);
        sb.append(AccessibilityUtils.ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        if (seconds < 10) {
            sb.append('0');
        }
        sb.append(seconds);
    }

    public PlaybackControlsPresenter(int layoutResourceId) {
        super(layoutResourceId);
    }

    public void enableSecondaryActions(boolean enable) {
        this.mMoreActionsEnabled = enable;
    }

    public boolean areMoreActionsEnabled() {
        return this.mMoreActionsEnabled;
    }

    public void setProgressColor(ViewHolder vh, @ColorInt int color) {
        ((LayerDrawable) vh.mProgressBar.getProgressDrawable()).setDrawableByLayerId(16908301, new ClipDrawable(new ColorDrawable(color), 3, 1));
    }

    public void setTotalTime(ViewHolder vh, int ms) {
        setTotalTimeLong(vh, (long) ms);
    }

    public void setTotalTimeLong(ViewHolder vh, long ms) {
        vh.setTotalTime(ms);
    }

    public int getTotalTime(ViewHolder vh) {
        return MathUtil.safeLongToInt(getTotalTimeLong(vh));
    }

    public long getTotalTimeLong(ViewHolder vh) {
        return vh.getTotalTime();
    }

    public void setCurrentTime(ViewHolder vh, int ms) {
        setCurrentTimeLong(vh, (long) ms);
    }

    public void setCurrentTimeLong(ViewHolder vh, long ms) {
        vh.setCurrentTime(ms);
    }

    public int getCurrentTime(ViewHolder vh) {
        return MathUtil.safeLongToInt(getCurrentTimeLong(vh));
    }

    public long getCurrentTimeLong(ViewHolder vh) {
        return vh.getCurrentTime();
    }

    public void setSecondaryProgress(ViewHolder vh, int progressMs) {
        setSecondaryProgressLong(vh, (long) progressMs);
    }

    public void setSecondaryProgressLong(ViewHolder vh, long progressMs) {
        vh.setSecondaryProgress(progressMs);
    }

    public int getSecondaryProgress(ViewHolder vh) {
        return MathUtil.safeLongToInt(getSecondaryProgressLong(vh));
    }

    public long getSecondaryProgressLong(ViewHolder vh) {
        return vh.getSecondaryProgress();
    }

    public void showPrimaryActions(ViewHolder vh) {
        if (vh.mMoreActionsShowing) {
            vh.toggleMoreActions();
        }
    }

    public void resetFocus(ViewHolder vh) {
        vh.mControlBar.requestFocus();
    }

    public void enableTimeMargins(ViewHolder vh, boolean enable) {
        MarginLayoutParams lp = (MarginLayoutParams) vh.mCurrentTime.getLayoutParams();
        int i = 0;
        lp.setMarginStart(enable ? vh.mCurrentTimeMarginStart : 0);
        vh.mCurrentTime.setLayoutParams(lp);
        lp = (MarginLayoutParams) vh.mTotalTime.getLayoutParams();
        if (enable) {
            i = vh.mTotalTimeMarginEnd;
        }
        lp.setMarginEnd(i);
        vh.mTotalTime.setLayoutParams(lp);
    }

    public android.support.v17.leanback.widget.Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(getLayoutResourceId(), parent, false));
    }

    public void onBindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder holder, Object item) {
        ViewHolder vh = (ViewHolder) holder;
        BoundData data = (BoundData) item;
        if (vh.mMoreActionsAdapter != data.secondaryActionsAdapter) {
            vh.mMoreActionsAdapter = data.secondaryActionsAdapter;
            vh.mMoreActionsAdapter.registerObserver(vh.mMoreActionsObserver);
            vh.mMoreActionsShowing = false;
        }
        super.onBindViewHolder(holder, item);
        vh.showMoreActions(this.mMoreActionsEnabled);
    }

    public void onUnbindViewHolder(android.support.v17.leanback.widget.Presenter.ViewHolder holder) {
        super.onUnbindViewHolder(holder);
        ViewHolder vh = (ViewHolder) holder;
        if (vh.mMoreActionsAdapter != null) {
            vh.mMoreActionsAdapter.unregisterObserver(vh.mMoreActionsObserver);
            vh.mMoreActionsAdapter = null;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getChildMarginBigger(Context context) {
        if (sChildMarginBigger == 0) {
            sChildMarginBigger = context.getResources().getDimensionPixelSize(R.dimen.lb_playback_controls_child_margin_bigger);
        }
        return sChildMarginBigger;
    }

    /* Access modifiers changed, original: 0000 */
    public int getChildMarginBiggest(Context context) {
        if (sChildMarginBiggest == 0) {
            sChildMarginBiggest = context.getResources().getDimensionPixelSize(R.dimen.lb_playback_controls_child_margin_biggest);
        }
        return sChildMarginBiggest;
    }
}
