package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.R;
import android.support.v17.leanback.widget.PlaybackControlsRow.OnPlaybackProgressCallback;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackSeekDataProvider.ResultCallback;
import android.support.v17.leanback.widget.PlaybackSeekUi.Client;
import android.support.v17.leanback.widget.PlaybackTransportRowView.OnUnhandledKeyListener;
import android.support.v17.leanback.widget.SeekBar.AccessibilitySeekListener;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settingslib.accessibility.AccessibilityUtils;
import java.util.Arrays;

public class PlaybackTransportRowPresenter extends PlaybackRowPresenter {
    float mDefaultSeekIncrement = 0.01f;
    Presenter mDescriptionPresenter;
    OnActionClickedListener mOnActionClickedListener;
    private final OnControlClickedListener mOnControlClickedListener = new OnControlClickedListener() {
        public void onControlClicked(android.support.v17.leanback.widget.Presenter.ViewHolder itemViewHolder, Object item, BoundData data) {
            ViewHolder vh = ((BoundData) data).mRowViewHolder;
            if (vh.getOnItemViewClickedListener() != null) {
                vh.getOnItemViewClickedListener().onItemClicked(itemViewHolder, item, vh, vh.getRow());
            }
            if (PlaybackTransportRowPresenter.this.mOnActionClickedListener != null && (item instanceof Action)) {
                PlaybackTransportRowPresenter.this.mOnActionClickedListener.onActionClicked((Action) item);
            }
        }
    };
    private final OnControlSelectedListener mOnControlSelectedListener = new OnControlSelectedListener() {
        public void onControlSelected(android.support.v17.leanback.widget.Presenter.ViewHolder itemViewHolder, Object item, BoundData data) {
            ViewHolder vh = ((BoundData) data).mRowViewHolder;
            if (vh.mSelectedViewHolder != itemViewHolder || vh.mSelectedItem != item) {
                vh.mSelectedViewHolder = itemViewHolder;
                vh.mSelectedItem = item;
                vh.dispatchItemSelection();
            }
        }
    };
    ControlBarPresenter mPlaybackControlsPresenter;
    int mProgressColor = 0;
    boolean mProgressColorSet;
    ControlBarPresenter mSecondaryControlsPresenter;

    static class BoundData extends BoundData {
        ViewHolder mRowViewHolder;

        BoundData() {
        }
    }

    public class ViewHolder extends android.support.v17.leanback.widget.PlaybackRowPresenter.ViewHolder implements PlaybackSeekUi {
        BoundData mControlsBoundData = new BoundData();
        final ViewGroup mControlsDock;
        ViewHolder mControlsVh;
        final TextView mCurrentTime;
        long mCurrentTimeInMs = Long.MIN_VALUE;
        final ViewGroup mDescriptionDock;
        final android.support.v17.leanback.widget.Presenter.ViewHolder mDescriptionViewHolder;
        final ImageView mImageView;
        boolean mInSeek;
        final OnPlaybackProgressCallback mListener = new OnPlaybackProgressCallback() {
            public void onCurrentPositionChanged(PlaybackControlsRow row, long ms) {
                ViewHolder.this.setCurrentPosition(ms);
            }

            public void onDurationChanged(PlaybackControlsRow row, long ms) {
                ViewHolder.this.setTotalTime(ms);
            }

            public void onBufferedPositionChanged(PlaybackControlsRow row, long ms) {
                ViewHolder.this.setBufferedPosition(ms);
            }
        };
        PlayPauseAction mPlayPauseAction;
        long[] mPositions;
        int mPositionsLength;
        final SeekBar mProgressBar;
        BoundData mSecondaryBoundData = new BoundData();
        final ViewGroup mSecondaryControlsDock;
        ViewHolder mSecondaryControlsVh;
        long mSecondaryProgressInMs;
        Client mSeekClient;
        PlaybackSeekDataProvider mSeekDataProvider;
        Object mSelectedItem;
        android.support.v17.leanback.widget.Presenter.ViewHolder mSelectedViewHolder;
        final StringBuilder mTempBuilder = new StringBuilder();
        int mThumbHeroIndex = -1;
        ResultCallback mThumbResult = new ResultCallback() {
            public void onThumbnailLoaded(Bitmap bitmap, int index) {
                int childIndex = index - (ViewHolder.this.mThumbHeroIndex - (ViewHolder.this.mThumbsBar.getChildCount() / 2));
                if (childIndex >= 0 && childIndex < ViewHolder.this.mThumbsBar.getChildCount()) {
                    ViewHolder.this.mThumbsBar.setThumbBitmap(childIndex, bitmap);
                }
            }
        };
        final ThumbsBar mThumbsBar;
        final TextView mTotalTime;
        long mTotalTimeInMs = Long.MIN_VALUE;

        /* Access modifiers changed, original: 0000 */
        public void updateProgressInSeek(boolean forward) {
            long newPos;
            long pos = this.mCurrentTimeInMs;
            if (this.mPositionsLength > 0) {
                int thumbHeroIndex;
                int i = 0;
                int index = Arrays.binarySearch(this.mPositions, 0, this.mPositionsLength, pos);
                int insertIndex;
                if (!forward) {
                    if (index < 0) {
                        insertIndex = -1 - index;
                        if (insertIndex > 0) {
                            int thumbHeroIndex2 = insertIndex - 1;
                            newPos = this.mPositions[insertIndex - 1];
                            thumbHeroIndex = thumbHeroIndex2;
                        } else {
                            thumbHeroIndex = 0;
                        }
                    } else if (index > 0) {
                        newPos = this.mPositions[index - 1];
                        thumbHeroIndex = index - 1;
                    } else {
                        newPos = 0;
                        thumbHeroIndex = 0;
                    }
                    updateThumbsInSeek(thumbHeroIndex, forward);
                } else if (index >= 0) {
                    if (index < this.mPositionsLength - 1) {
                        newPos = this.mPositions[index + 1];
                        thumbHeroIndex = index + 1;
                    } else {
                        newPos = this.mTotalTimeInMs;
                        thumbHeroIndex = index;
                    }
                    updateThumbsInSeek(thumbHeroIndex, forward);
                } else {
                    insertIndex = -1 - index;
                    if (insertIndex <= this.mPositionsLength - 1) {
                        i = insertIndex;
                        thumbHeroIndex = this.mPositions[insertIndex];
                    } else {
                        thumbHeroIndex = this.mTotalTimeInMs;
                        if (insertIndex > 0) {
                            i = insertIndex - 1;
                        }
                    }
                }
                long j = thumbHeroIndex;
                thumbHeroIndex = i;
                newPos = j;
                updateThumbsInSeek(thumbHeroIndex, forward);
            } else {
                long interval = (long) (((float) this.mTotalTimeInMs) * PlaybackTransportRowPresenter.this.getDefaultSeekIncrement());
                long newPos2 = (forward ? interval : -interval) + pos;
                if (newPos2 > this.mTotalTimeInMs) {
                    newPos2 = this.mTotalTimeInMs;
                } else if (newPos2 < 0) {
                    newPos = 0;
                }
                newPos = newPos2;
            }
            this.mProgressBar.setProgress((int) (2.147483647E9d * (((double) newPos) / ((double) this.mTotalTimeInMs))));
            this.mSeekClient.onSeekPositionChanged(newPos);
        }

        /* Access modifiers changed, original: 0000 */
        public void updateThumbsInSeek(int thumbHeroIndex, boolean forward) {
            int i = thumbHeroIndex;
            if (this.mThumbHeroIndex != i) {
                int totalNum = this.mThumbsBar.getChildCount();
                if (totalNum < 0 || (totalNum & 1) == 0) {
                    throw new RuntimeException();
                }
                int newRequestStart;
                int newRequestEnd;
                boolean forward2;
                int newRequestEnd2;
                int heroChildIndex = totalNum / 2;
                int start = Math.max(i - (totalNum / 2), 0);
                int end = Math.min((totalNum / 2) + i, this.mPositionsLength - 1);
                if (this.mThumbHeroIndex < 0) {
                    newRequestStart = start;
                    newRequestEnd = end;
                    forward2 = forward;
                } else {
                    forward2 = i > this.mThumbHeroIndex;
                    int oldStart = Math.max(this.mThumbHeroIndex - (totalNum / 2), 0);
                    int oldEnd = Math.min(this.mThumbHeroIndex + (totalNum / 2), this.mPositionsLength - 1);
                    if (forward2) {
                        newRequestStart = Math.max(oldEnd + 1, start);
                        newRequestEnd = end;
                        for (int i2 = start; i2 <= newRequestStart - 1; i2++) {
                            this.mThumbsBar.setThumbBitmap((i2 - i) + heroChildIndex, this.mThumbsBar.getThumbBitmap((i2 - this.mThumbHeroIndex) + heroChildIndex));
                        }
                    } else {
                        newRequestEnd2 = Math.min(oldStart - 1, end);
                        int newRequestStart2 = start;
                        for (newRequestStart = end; newRequestStart >= newRequestEnd2 + 1; newRequestStart--) {
                            this.mThumbsBar.setThumbBitmap((newRequestStart - i) + heroChildIndex, this.mThumbsBar.getThumbBitmap((newRequestStart - this.mThumbHeroIndex) + heroChildIndex));
                        }
                        newRequestEnd = newRequestEnd2;
                        newRequestStart = newRequestStart2;
                    }
                }
                this.mThumbHeroIndex = i;
                if (forward2) {
                    for (newRequestEnd2 = newRequestStart; newRequestEnd2 <= newRequestEnd; newRequestEnd2++) {
                        this.mSeekDataProvider.getThumbnail(newRequestEnd2, this.mThumbResult);
                    }
                } else {
                    for (newRequestEnd2 = newRequestEnd; newRequestEnd2 >= newRequestStart; newRequestEnd2--) {
                        this.mSeekDataProvider.getThumbnail(newRequestEnd2, this.mThumbResult);
                    }
                }
                int childIndex = 0;
                while (true) {
                    newRequestEnd2 = childIndex;
                    if (newRequestEnd2 >= (heroChildIndex - this.mThumbHeroIndex) + start) {
                        break;
                    }
                    this.mThumbsBar.setThumbBitmap(newRequestEnd2, null);
                    childIndex = newRequestEnd2 + 1;
                }
                for (newRequestEnd2 = ((heroChildIndex + end) - this.mThumbHeroIndex) + 1; newRequestEnd2 < totalNum; newRequestEnd2++) {
                    this.mThumbsBar.setThumbBitmap(newRequestEnd2, null);
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public boolean onForward() {
            if (!startSeek()) {
                return false;
            }
            updateProgressInSeek(true);
            return true;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean onBackward() {
            if (!startSeek()) {
                return false;
            }
            updateProgressInSeek(false);
            return true;
        }

        public ViewHolder(View rootView, Presenter descriptionPresenter) {
            android.support.v17.leanback.widget.Presenter.ViewHolder viewHolder;
            super(rootView);
            this.mImageView = (ImageView) rootView.findViewById(R.id.image);
            this.mDescriptionDock = (ViewGroup) rootView.findViewById(R.id.description_dock);
            this.mCurrentTime = (TextView) rootView.findViewById(R.id.current_time);
            this.mTotalTime = (TextView) rootView.findViewById(R.id.total_time);
            this.mProgressBar = (SeekBar) rootView.findViewById(R.id.playback_progress);
            this.mProgressBar.setOnClickListener(new OnClickListener(PlaybackTransportRowPresenter.this) {
                public void onClick(View view) {
                    PlaybackTransportRowPresenter.this.onProgressBarClicked(ViewHolder.this);
                }
            });
            this.mProgressBar.setOnKeyListener(new OnKeyListener(PlaybackTransportRowPresenter.this) {
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    boolean z = false;
                    if (keyCode != 4) {
                        if (keyCode != 66) {
                            if (keyCode != 69) {
                                if (keyCode != 81) {
                                    if (keyCode != 111) {
                                        switch (keyCode) {
                                            case 19:
                                            case 20:
                                                return ViewHolder.this.mInSeek;
                                            case 21:
                                                break;
                                            case 22:
                                                break;
                                            case 23:
                                                break;
                                            default:
                                                switch (keyCode) {
                                                    case 89:
                                                        break;
                                                    case 90:
                                                        break;
                                                    default:
                                                        return false;
                                                }
                                        }
                                    }
                                }
                                if (keyEvent.getAction() == 0) {
                                    ViewHolder.this.onForward();
                                }
                                return true;
                            }
                            if (keyEvent.getAction() == 0) {
                                ViewHolder.this.onBackward();
                            }
                            return true;
                        }
                        if (!ViewHolder.this.mInSeek) {
                            return false;
                        }
                        if (keyEvent.getAction() == 1) {
                            ViewHolder.this.stopSeek(false);
                        }
                        return true;
                    }
                    if (!ViewHolder.this.mInSeek) {
                        return false;
                    }
                    if (keyEvent.getAction() == 1) {
                        ViewHolder viewHolder = ViewHolder.this;
                        if (VERSION.SDK_INT < 21 || !ViewHolder.this.mProgressBar.isAccessibilityFocused()) {
                            z = true;
                        }
                        viewHolder.stopSeek(z);
                    }
                    return true;
                }
            });
            this.mProgressBar.setAccessibilitySeekListener(new AccessibilitySeekListener(PlaybackTransportRowPresenter.this) {
                public boolean onAccessibilitySeekForward() {
                    return ViewHolder.this.onForward();
                }

                public boolean onAccessibilitySeekBackward() {
                    return ViewHolder.this.onBackward();
                }
            });
            this.mProgressBar.setMax(Integer.MAX_VALUE);
            this.mControlsDock = (ViewGroup) rootView.findViewById(R.id.controls_dock);
            this.mSecondaryControlsDock = (ViewGroup) rootView.findViewById(R.id.secondary_controls_dock);
            if (descriptionPresenter == null) {
                viewHolder = null;
            } else {
                viewHolder = descriptionPresenter.onCreateViewHolder(this.mDescriptionDock);
            }
            this.mDescriptionViewHolder = viewHolder;
            if (this.mDescriptionViewHolder != null) {
                this.mDescriptionDock.addView(this.mDescriptionViewHolder.view);
            }
            this.mThumbsBar = (ThumbsBar) rootView.findViewById(R.id.thumbs_row);
        }

        public final android.support.v17.leanback.widget.Presenter.ViewHolder getDescriptionViewHolder() {
            return this.mDescriptionViewHolder;
        }

        public void setPlaybackSeekUiClient(Client client) {
            this.mSeekClient = client;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean startSeek() {
            if (this.mInSeek) {
                return true;
            }
            if (this.mSeekClient == null || !this.mSeekClient.isSeekEnabled() || this.mTotalTimeInMs <= 0) {
                return false;
            }
            this.mInSeek = true;
            this.mSeekClient.onSeekStarted();
            this.mSeekDataProvider = this.mSeekClient.getPlaybackSeekDataProvider();
            this.mPositions = this.mSeekDataProvider != null ? this.mSeekDataProvider.getSeekPositions() : null;
            if (this.mPositions != null) {
                int pos = Arrays.binarySearch(this.mPositions, this.mTotalTimeInMs);
                if (pos >= 0) {
                    this.mPositionsLength = pos + 1;
                } else {
                    this.mPositionsLength = -1 - pos;
                }
            } else {
                this.mPositionsLength = 0;
            }
            this.mControlsVh.view.setVisibility(8);
            this.mSecondaryControlsVh.view.setVisibility(4);
            this.mDescriptionViewHolder.view.setVisibility(4);
            this.mThumbsBar.setVisibility(0);
            return true;
        }

        /* Access modifiers changed, original: 0000 */
        public void stopSeek(boolean cancelled) {
            if (this.mInSeek) {
                this.mInSeek = false;
                this.mSeekClient.onSeekFinished(cancelled);
                if (this.mSeekDataProvider != null) {
                    this.mSeekDataProvider.reset();
                }
                this.mThumbHeroIndex = -1;
                this.mThumbsBar.clearThumbBitmaps();
                this.mSeekDataProvider = null;
                this.mPositions = null;
                this.mPositionsLength = 0;
                this.mControlsVh.view.setVisibility(0);
                this.mSecondaryControlsVh.view.setVisibility(0);
                this.mDescriptionViewHolder.view.setVisibility(0);
                this.mThumbsBar.setVisibility(4);
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
                return ((ControlButtonPresenterSelector) adapter.getPresenterSelector()).getSecondaryPresenter();
            }
            if (adapter.size() > 0) {
                obj = adapter.get(0);
            }
            return adapter.getPresenter(obj);
        }

        public final TextView getDurationView() {
            return this.mTotalTime;
        }

        /* Access modifiers changed, original: protected */
        public void onSetDurationLabel(long totalTimeMs) {
            if (this.mTotalTime != null) {
                PlaybackTransportRowPresenter.formatTime(totalTimeMs, this.mTempBuilder);
                this.mTotalTime.setText(this.mTempBuilder.toString());
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setTotalTime(long totalTimeMs) {
            if (this.mTotalTimeInMs != totalTimeMs) {
                this.mTotalTimeInMs = totalTimeMs;
                onSetDurationLabel(totalTimeMs);
            }
        }

        public final TextView getCurrentPositionView() {
            return this.mCurrentTime;
        }

        /* Access modifiers changed, original: protected */
        public void onSetCurrentPositionLabel(long currentTimeMs) {
            if (this.mCurrentTime != null) {
                PlaybackTransportRowPresenter.formatTime(currentTimeMs, this.mTempBuilder);
                this.mCurrentTime.setText(this.mTempBuilder.toString());
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setCurrentPosition(long currentTimeMs) {
            if (currentTimeMs != this.mCurrentTimeInMs) {
                this.mCurrentTimeInMs = currentTimeMs;
                onSetCurrentPositionLabel(currentTimeMs);
            }
            if (!this.mInSeek) {
                int progressRatio = 0;
                if (this.mTotalTimeInMs > 0) {
                    progressRatio = (int) (2.147483647E9d * (((double) this.mCurrentTimeInMs) / ((double) this.mTotalTimeInMs)));
                }
                this.mProgressBar.setProgress(progressRatio);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setBufferedPosition(long progressMs) {
            this.mSecondaryProgressInMs = progressMs;
            this.mProgressBar.setSecondaryProgress((int) (2.147483647E9d * (((double) progressMs) / ((double) this.mTotalTimeInMs))));
        }
    }

    static void formatTime(long ms, StringBuilder sb) {
        sb.setLength(0);
        if (ms < 0) {
            sb.append("--");
            return;
        }
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds -= minutes * 60;
        minutes -= 60 * hours;
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

    public PlaybackTransportRowPresenter() {
        setHeaderPresenter(null);
        setSelectEffectEnabled(false);
        this.mPlaybackControlsPresenter = new ControlBarPresenter(R.layout.lb_control_bar);
        this.mPlaybackControlsPresenter.setDefaultFocusToMiddle(false);
        this.mSecondaryControlsPresenter = new ControlBarPresenter(R.layout.lb_control_bar);
        this.mSecondaryControlsPresenter.setDefaultFocusToMiddle(false);
        this.mPlaybackControlsPresenter.setOnControlSelectedListener(this.mOnControlSelectedListener);
        this.mSecondaryControlsPresenter.setOnControlSelectedListener(this.mOnControlSelectedListener);
        this.mPlaybackControlsPresenter.setOnControlClickedListener(this.mOnControlClickedListener);
        this.mSecondaryControlsPresenter.setOnControlClickedListener(this.mOnControlClickedListener);
    }

    public void setDescriptionPresenter(Presenter descriptionPresenter) {
        this.mDescriptionPresenter = descriptionPresenter;
    }

    public void setOnActionClickedListener(OnActionClickedListener listener) {
        this.mOnActionClickedListener = listener;
    }

    public OnActionClickedListener getOnActionClickedListener() {
        return this.mOnActionClickedListener;
    }

    public void setProgressColor(@ColorInt int color) {
        this.mProgressColor = color;
        this.mProgressColorSet = true;
    }

    @ColorInt
    public int getProgressColor() {
        return this.mProgressColor;
    }

    public void onReappear(android.support.v17.leanback.widget.RowPresenter.ViewHolder rowViewHolder) {
        ViewHolder vh = (ViewHolder) rowViewHolder;
        if (vh.view.hasFocus()) {
            vh.mProgressBar.requestFocus();
        }
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
        ViewHolder vh = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_playback_transport_controls_row, parent, false), this.mDescriptionPresenter);
        initRow(vh);
        return vh;
    }

    private void initRow(final ViewHolder vh) {
        int i;
        vh.mControlsVh = (ViewHolder) this.mPlaybackControlsPresenter.onCreateViewHolder(vh.mControlsDock);
        SeekBar seekBar = vh.mProgressBar;
        if (this.mProgressColorSet) {
            i = this.mProgressColor;
        } else {
            i = getDefaultProgressColor(vh.mControlsDock.getContext());
        }
        seekBar.setProgressColor(i);
        vh.mControlsDock.addView(vh.mControlsVh.view);
        vh.mSecondaryControlsVh = (ViewHolder) this.mSecondaryControlsPresenter.onCreateViewHolder(vh.mSecondaryControlsDock);
        vh.mSecondaryControlsDock.addView(vh.mSecondaryControlsVh.view);
        ((PlaybackTransportRowView) vh.view.findViewById(R.id.transport_row)).setOnUnhandledKeyListener(new OnUnhandledKeyListener() {
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
        if (row.getItem() == null) {
            vh.mDescriptionDock.setVisibility(8);
        } else {
            vh.mDescriptionDock.setVisibility(0);
            if (vh.mDescriptionViewHolder != null) {
                this.mDescriptionPresenter.onBindViewHolder(vh.mDescriptionViewHolder, row.getItem());
            }
        }
        if (row.getImageDrawable() == null) {
            vh.mImageView.setVisibility(8);
        } else {
            vh.mImageView.setVisibility(0);
        }
        vh.mImageView.setImageDrawable(row.getImageDrawable());
        vh.mControlsBoundData.adapter = row.getPrimaryActionsAdapter();
        vh.mControlsBoundData.presenter = vh.getPresenter(true);
        vh.mControlsBoundData.mRowViewHolder = vh;
        this.mPlaybackControlsPresenter.onBindViewHolder(vh.mControlsVh, vh.mControlsBoundData);
        vh.mSecondaryBoundData.adapter = row.getSecondaryActionsAdapter();
        vh.mSecondaryBoundData.presenter = vh.getPresenter(false);
        vh.mSecondaryBoundData.mRowViewHolder = vh;
        this.mSecondaryControlsPresenter.onBindViewHolder(vh.mSecondaryControlsVh, vh.mSecondaryBoundData);
        vh.setTotalTime(row.getDuration());
        vh.setCurrentPosition(row.getCurrentPosition());
        vh.setBufferedPosition(row.getBufferedPosition());
        row.setOnPlaybackProgressChangedListener(vh.mListener);
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
    public void onProgressBarClicked(ViewHolder vh) {
        if (vh != null) {
            if (vh.mPlayPauseAction == null) {
                vh.mPlayPauseAction = new PlayPauseAction(vh.view.getContext());
            }
            if (vh.getOnItemViewClickedListener() != null) {
                vh.getOnItemViewClickedListener().onItemClicked(vh, vh.mPlayPauseAction, vh, vh.getRow());
            }
            if (this.mOnActionClickedListener != null) {
                this.mOnActionClickedListener.onActionClicked(vh.mPlayPauseAction);
            }
        }
    }

    public void setDefaultSeekIncrement(float ratio) {
        this.mDefaultSeekIncrement = ratio;
    }

    public float getDefaultSeekIncrement() {
        return this.mDefaultSeekIncrement;
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
