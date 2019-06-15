package android.support.v17.leanback.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.R;
import android.support.v17.leanback.animation.LogAccelerateInterpolator;
import android.support.v17.leanback.animation.LogDecelerateInterpolator;
import android.support.v17.leanback.media.PlaybackGlueHost.HostCallback;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.BaseGridView.OnKeyInterceptListener;
import android.support.v17.leanback.widget.BaseGridView.OnTouchInterceptListener;
import android.support.v17.leanback.widget.BaseOnItemViewClickedListener;
import android.support.v17.leanback.widget.BaseOnItemViewSelectedListener;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ItemAlignmentFacet;
import android.support.v17.leanback.widget.ItemAlignmentFacet.ItemAlignmentDef;
import android.support.v17.leanback.widget.ItemBridgeAdapter.AdapterListener;
import android.support.v17.leanback.widget.ItemBridgeAdapter.ViewHolder;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.PlaybackRowPresenter;
import android.support.v17.leanback.widget.PlaybackSeekDataProvider;
import android.support.v17.leanback.widget.PlaybackSeekUi;
import android.support.v17.leanback.widget.PlaybackSeekUi.Client;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

public class PlaybackSupportFragment extends Fragment {
    private static final int ANIMATING = 1;
    private static final int ANIMATION_MULTIPLIER = 1;
    public static final int BG_DARK = 1;
    public static final int BG_LIGHT = 2;
    public static final int BG_NONE = 0;
    static final String BUNDLE_CONTROL_VISIBLE_ON_CREATEVIEW = "controlvisible_oncreateview";
    private static final boolean DEBUG = false;
    private static final int IDLE = 0;
    private static final int START_FADE_OUT = 1;
    private static final String TAG = "PlaybackSupportFragment";
    ObjectAdapter mAdapter;
    private final AdapterListener mAdapterListener = new AdapterListener() {
        public void onAttachedToWindow(ViewHolder vh) {
            if (!PlaybackSupportFragment.this.mControlVisible) {
                vh.getViewHolder().view.setAlpha(0.0f);
            }
        }

        public void onCreate(ViewHolder vh) {
            Presenter.ViewHolder viewHolder = vh.getViewHolder();
            if (viewHolder instanceof PlaybackSeekUi) {
                ((PlaybackSeekUi) viewHolder).setPlaybackSeekUiClient(PlaybackSupportFragment.this.mChainedClient);
            }
        }

        public void onDetachedFromWindow(ViewHolder vh) {
            vh.getViewHolder().view.setAlpha(1.0f);
            vh.getViewHolder().view.setTranslationY(0.0f);
            vh.getViewHolder().view.setAlpha(1.0f);
        }

        public void onBind(ViewHolder vh) {
        }
    };
    int mAnimationTranslateY;
    int mBackgroundType = 1;
    View mBackgroundView;
    int mBgAlpha;
    int mBgDarkColor;
    ValueAnimator mBgFadeInAnimator;
    ValueAnimator mBgFadeOutAnimator;
    int mBgLightColor;
    final Client mChainedClient = new Client() {
        public boolean isSeekEnabled() {
            return PlaybackSupportFragment.this.mSeekUiClient == null ? false : PlaybackSupportFragment.this.mSeekUiClient.isSeekEnabled();
        }

        public void onSeekStarted() {
            if (PlaybackSupportFragment.this.mSeekUiClient != null) {
                PlaybackSupportFragment.this.mSeekUiClient.onSeekStarted();
            }
            PlaybackSupportFragment.this.setSeekMode(true);
        }

        public PlaybackSeekDataProvider getPlaybackSeekDataProvider() {
            return PlaybackSupportFragment.this.mSeekUiClient == null ? null : PlaybackSupportFragment.this.mSeekUiClient.getPlaybackSeekDataProvider();
        }

        public void onSeekPositionChanged(long pos) {
            if (PlaybackSupportFragment.this.mSeekUiClient != null) {
                PlaybackSupportFragment.this.mSeekUiClient.onSeekPositionChanged(pos);
            }
        }

        public void onSeekFinished(boolean cancelled) {
            if (PlaybackSupportFragment.this.mSeekUiClient != null) {
                PlaybackSupportFragment.this.mSeekUiClient.onSeekFinished(cancelled);
            }
            PlaybackSupportFragment.this.setSeekMode(false);
        }
    };
    ValueAnimator mControlRowFadeInAnimator;
    ValueAnimator mControlRowFadeOutAnimator;
    boolean mControlVisible = true;
    boolean mControlVisibleBeforeOnCreateView = true;
    BaseOnItemViewClickedListener mExternalItemClickedListener;
    BaseOnItemViewSelectedListener mExternalItemSelectedListener;
    OnFadeCompleteListener mFadeCompleteListener;
    private final AnimatorListener mFadeListener = new AnimatorListener() {
        public void onAnimationStart(Animator animation) {
            PlaybackSupportFragment.this.enableVerticalGridAnimations(false);
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (PlaybackSupportFragment.this.mBgAlpha > 0) {
                PlaybackSupportFragment.this.enableVerticalGridAnimations(true);
                if (PlaybackSupportFragment.this.mFadeCompleteListener != null) {
                    PlaybackSupportFragment.this.mFadeCompleteListener.onFadeInComplete();
                    return;
                }
                return;
            }
            VerticalGridView verticalView = PlaybackSupportFragment.this.getVerticalGridView();
            if (verticalView != null && verticalView.getSelectedPosition() == 0) {
                ViewHolder vh = (ViewHolder) verticalView.findViewHolderForAdapterPosition(0);
                if (vh != null && (vh.getPresenter() instanceof PlaybackRowPresenter)) {
                    ((PlaybackRowPresenter) vh.getPresenter()).onReappear((RowPresenter.ViewHolder) vh.getViewHolder());
                }
            }
            if (PlaybackSupportFragment.this.mFadeCompleteListener != null) {
                PlaybackSupportFragment.this.mFadeCompleteListener.onFadeOutComplete();
            }
        }
    };
    boolean mFadingEnabled = true;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 1 && PlaybackSupportFragment.this.mFadingEnabled) {
                PlaybackSupportFragment.this.hideControlsOverlay(true);
            }
        }
    };
    HostCallback mHostCallback;
    boolean mInSeek;
    OnKeyListener mInputEventHandler;
    private TimeInterpolator mLogAccelerateInterpolator = new LogAccelerateInterpolator(100, 0);
    private TimeInterpolator mLogDecelerateInterpolator = new LogDecelerateInterpolator(100, 0);
    int mMajorFadeTranslateY;
    int mMinorFadeTranslateY;
    private final BaseOnItemViewClickedListener mOnItemViewClickedListener = new BaseOnItemViewClickedListener() {
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Object row) {
            if (PlaybackSupportFragment.this.mPlaybackItemClickedListener != null && (rowViewHolder instanceof PlaybackRowPresenter.ViewHolder)) {
                PlaybackSupportFragment.this.mPlaybackItemClickedListener.onItemClicked(itemViewHolder, item, rowViewHolder, row);
            }
            if (PlaybackSupportFragment.this.mExternalItemClickedListener != null) {
                PlaybackSupportFragment.this.mExternalItemClickedListener.onItemClicked(itemViewHolder, item, rowViewHolder, row);
            }
        }
    };
    private final BaseOnItemViewSelectedListener mOnItemViewSelectedListener = new BaseOnItemViewSelectedListener() {
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Object row) {
            if (PlaybackSupportFragment.this.mExternalItemSelectedListener != null) {
                PlaybackSupportFragment.this.mExternalItemSelectedListener.onItemSelected(itemViewHolder, item, rowViewHolder, row);
            }
        }
    };
    private final OnKeyInterceptListener mOnKeyInterceptListener = new OnKeyInterceptListener() {
        public boolean onInterceptKeyEvent(KeyEvent event) {
            return PlaybackSupportFragment.this.onInterceptInputEvent(event);
        }
    };
    private final OnTouchInterceptListener mOnTouchInterceptListener = new OnTouchInterceptListener() {
        public boolean onInterceptTouchEvent(MotionEvent event) {
            return PlaybackSupportFragment.this.onInterceptInputEvent(event);
        }
    };
    ValueAnimator mOtherRowFadeInAnimator;
    ValueAnimator mOtherRowFadeOutAnimator;
    int mOtherRowsCenterToBottom;
    int mPaddingBottom;
    BaseOnItemViewClickedListener mPlaybackItemClickedListener;
    PlaybackRowPresenter mPresenter;
    ProgressBarManager mProgressBarManager = new ProgressBarManager();
    View mRootView;
    Row mRow;
    RowsSupportFragment mRowsSupportFragment;
    Client mSeekUiClient;
    private final SetSelectionRunnable mSetSelectionRunnable = new SetSelectionRunnable(this, null);
    int mShowTimeMs;

    @RestrictTo({Scope.LIBRARY})
    public static class OnFadeCompleteListener {
        public void onFadeInComplete() {
        }

        public void onFadeOutComplete() {
        }
    }

    private class SetSelectionRunnable implements Runnable {
        int mPosition;
        boolean mSmooth;

        private SetSelectionRunnable() {
            this.mSmooth = true;
        }

        /* synthetic */ SetSelectionRunnable(PlaybackSupportFragment x0, AnonymousClass1 x1) {
            this();
        }

        public void run() {
            if (PlaybackSupportFragment.this.mRowsSupportFragment != null) {
                PlaybackSupportFragment.this.mRowsSupportFragment.setSelectedPosition(this.mPosition, this.mSmooth);
            }
        }
    }

    @RestrictTo({Scope.LIBRARY})
    public void resetFocus() {
        ViewHolder vh = (ViewHolder) getVerticalGridView().findViewHolderForAdapterPosition(0);
        if (vh != null && (vh.getPresenter() instanceof PlaybackRowPresenter)) {
            ((PlaybackRowPresenter) vh.getPresenter()).onReappear((RowPresenter.ViewHolder) vh.getViewHolder());
        }
    }

    public ObjectAdapter getAdapter() {
        return this.mAdapter;
    }

    public PlaybackSupportFragment() {
        this.mProgressBarManager.setInitialDelay(500);
    }

    /* Access modifiers changed, original: 0000 */
    public VerticalGridView getVerticalGridView() {
        if (this.mRowsSupportFragment == null) {
            return null;
        }
        return this.mRowsSupportFragment.getVerticalGridView();
    }

    private void setBgAlpha(int alpha) {
        this.mBgAlpha = alpha;
        if (this.mBackgroundView != null) {
            this.mBackgroundView.getBackground().setAlpha(alpha);
        }
    }

    private void enableVerticalGridAnimations(boolean enable) {
        if (getVerticalGridView() != null) {
            getVerticalGridView().setAnimateChildLayout(enable);
        }
    }

    public void setControlsOverlayAutoHideEnabled(boolean enabled) {
        if (enabled != this.mFadingEnabled) {
            this.mFadingEnabled = enabled;
            if (isResumed() && getView().hasFocus()) {
                showControlsOverlay(true);
                if (enabled) {
                    startFadeTimer();
                } else {
                    stopFadeTimer();
                }
            }
        }
    }

    public boolean isControlsOverlayAutoHideEnabled() {
        return this.mFadingEnabled;
    }

    @Deprecated
    public void setFadingEnabled(boolean enabled) {
        setControlsOverlayAutoHideEnabled(enabled);
    }

    @Deprecated
    public boolean isFadingEnabled() {
        return isControlsOverlayAutoHideEnabled();
    }

    @RestrictTo({Scope.LIBRARY})
    public void setFadeCompleteListener(OnFadeCompleteListener listener) {
        this.mFadeCompleteListener = listener;
    }

    @RestrictTo({Scope.LIBRARY})
    public OnFadeCompleteListener getFadeCompleteListener() {
        return this.mFadeCompleteListener;
    }

    public final void setOnKeyInterceptListener(OnKeyListener handler) {
        this.mInputEventHandler = handler;
    }

    public void tickle() {
        stopFadeTimer();
        showControlsOverlay(true);
    }

    private boolean onInterceptInputEvent(InputEvent event) {
        boolean controlsHidden = this.mControlVisible ^ true;
        boolean consumeEvent = false;
        int keyCode = 0;
        int keyAction = 0;
        if (event instanceof KeyEvent) {
            keyCode = ((KeyEvent) event).getKeyCode();
            keyAction = ((KeyEvent) event).getAction();
            if (this.mInputEventHandler != null) {
                consumeEvent = this.mInputEventHandler.onKey(getView(), keyCode, (KeyEvent) event);
            }
        }
        if (keyCode != 4 && keyCode != 111) {
            switch (keyCode) {
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                    if (controlsHidden) {
                        consumeEvent = true;
                    }
                    if (keyAction == 0) {
                        tickle();
                        break;
                    }
                    break;
                default:
                    if (consumeEvent && keyAction == 0) {
                        tickle();
                        break;
                    }
            }
        } else if (this.mInSeek) {
            return false;
        } else {
            if (!controlsHidden) {
                consumeEvent = true;
                if (((KeyEvent) event).getAction() == 1) {
                    hideControlsOverlay(true);
                }
            }
        }
        return consumeEvent;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mControlVisible = true;
        if (!this.mControlVisibleBeforeOnCreateView) {
            showControlsOverlay(false, false);
            this.mControlVisibleBeforeOnCreateView = true;
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mControlVisible && this.mFadingEnabled) {
            startFadeTimer();
        }
        getVerticalGridView().setOnTouchInterceptListener(this.mOnTouchInterceptListener);
        getVerticalGridView().setOnKeyInterceptListener(this.mOnKeyInterceptListener);
        if (this.mHostCallback != null) {
            this.mHostCallback.onHostResume();
        }
    }

    private void stopFadeTimer() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
        }
    }

    private void startFadeTimer() {
        if (this.mHandler != null) {
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessageDelayed(1, (long) this.mShowTimeMs);
        }
    }

    private static ValueAnimator loadAnimator(Context context, int resId) {
        ValueAnimator animator = (ValueAnimator) AnimatorInflater.loadAnimator(context, resId);
        animator.setDuration(animator.getDuration() * 1);
        return animator;
    }

    private void loadBgAnimator() {
        AnimatorUpdateListener listener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator arg0) {
                PlaybackSupportFragment.this.setBgAlpha(((Integer) arg0.getAnimatedValue()).intValue());
            }
        };
        Context context = getContext();
        this.mBgFadeInAnimator = loadAnimator(context, R.animator.lb_playback_bg_fade_in);
        this.mBgFadeInAnimator.addUpdateListener(listener);
        this.mBgFadeInAnimator.addListener(this.mFadeListener);
        this.mBgFadeOutAnimator = loadAnimator(context, R.animator.lb_playback_bg_fade_out);
        this.mBgFadeOutAnimator.addUpdateListener(listener);
        this.mBgFadeOutAnimator.addListener(this.mFadeListener);
    }

    private void loadControlRowAnimator() {
        AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator arg0) {
                if (PlaybackSupportFragment.this.getVerticalGridView() != null) {
                    RecyclerView.ViewHolder vh = PlaybackSupportFragment.this.getVerticalGridView().findViewHolderForAdapterPosition(0);
                    if (vh != null) {
                        View view = vh.itemView;
                        if (view != null) {
                            float fraction = ((Float) arg0.getAnimatedValue()).floatValue();
                            view.setAlpha(fraction);
                            view.setTranslationY(((float) PlaybackSupportFragment.this.mAnimationTranslateY) * (1.0f - fraction));
                        }
                    }
                }
            }
        };
        Context context = getContext();
        this.mControlRowFadeInAnimator = loadAnimator(context, R.animator.lb_playback_controls_fade_in);
        this.mControlRowFadeInAnimator.addUpdateListener(updateListener);
        this.mControlRowFadeInAnimator.setInterpolator(this.mLogDecelerateInterpolator);
        this.mControlRowFadeOutAnimator = loadAnimator(context, R.animator.lb_playback_controls_fade_out);
        this.mControlRowFadeOutAnimator.addUpdateListener(updateListener);
        this.mControlRowFadeOutAnimator.setInterpolator(this.mLogAccelerateInterpolator);
    }

    private void loadOtherRowAnimator() {
        AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator arg0) {
                if (PlaybackSupportFragment.this.getVerticalGridView() != null) {
                    float fraction = ((Float) arg0.getAnimatedValue()).floatValue();
                    int count = PlaybackSupportFragment.this.getVerticalGridView().getChildCount();
                    for (int i = 0; i < count; i++) {
                        View view = PlaybackSupportFragment.this.getVerticalGridView().getChildAt(i);
                        if (PlaybackSupportFragment.this.getVerticalGridView().getChildAdapterPosition(view) > 0) {
                            view.setAlpha(fraction);
                            view.setTranslationY(((float) PlaybackSupportFragment.this.mAnimationTranslateY) * (1.0f - fraction));
                        }
                    }
                }
            }
        };
        Context context = getContext();
        this.mOtherRowFadeInAnimator = loadAnimator(context, R.animator.lb_playback_controls_fade_in);
        this.mOtherRowFadeInAnimator.addUpdateListener(updateListener);
        this.mOtherRowFadeInAnimator.setInterpolator(this.mLogDecelerateInterpolator);
        this.mOtherRowFadeOutAnimator = loadAnimator(context, R.animator.lb_playback_controls_fade_out);
        this.mOtherRowFadeOutAnimator.addUpdateListener(updateListener);
        this.mOtherRowFadeOutAnimator.setInterpolator(new AccelerateInterpolator());
    }

    @Deprecated
    public void fadeOut() {
        showControlsOverlay(false, false);
    }

    public void showControlsOverlay(boolean runAnimation) {
        showControlsOverlay(true, runAnimation);
    }

    public boolean isControlsOverlayVisible() {
        return this.mControlVisible;
    }

    public void hideControlsOverlay(boolean runAnimation) {
        showControlsOverlay(false, runAnimation);
    }

    static void reverseFirstOrStartSecond(ValueAnimator first, ValueAnimator second, boolean runAnimation) {
        if (first.isStarted()) {
            first.reverse();
            if (!runAnimation) {
                first.end();
                return;
            }
            return;
        }
        second.start();
        if (!runAnimation) {
            second.end();
        }
    }

    static void endAll(ValueAnimator first, ValueAnimator second) {
        if (first.isStarted()) {
            first.end();
        } else if (second.isStarted()) {
            second.end();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void showControlsOverlay(boolean show, boolean animation) {
        if (getView() == null) {
            this.mControlVisibleBeforeOnCreateView = show;
            return;
        }
        if (!isResumed()) {
            animation = false;
        }
        if (show == this.mControlVisible) {
            if (!animation) {
                endAll(this.mBgFadeInAnimator, this.mBgFadeOutAnimator);
                endAll(this.mControlRowFadeInAnimator, this.mControlRowFadeOutAnimator);
                endAll(this.mOtherRowFadeInAnimator, this.mOtherRowFadeOutAnimator);
            }
            return;
        }
        this.mControlVisible = show;
        if (!this.mControlVisible) {
            stopFadeTimer();
        }
        int i = (getVerticalGridView() == null || getVerticalGridView().getSelectedPosition() == 0) ? this.mMajorFadeTranslateY : this.mMinorFadeTranslateY;
        this.mAnimationTranslateY = i;
        if (show) {
            reverseFirstOrStartSecond(this.mBgFadeOutAnimator, this.mBgFadeInAnimator, animation);
            reverseFirstOrStartSecond(this.mControlRowFadeOutAnimator, this.mControlRowFadeInAnimator, animation);
            reverseFirstOrStartSecond(this.mOtherRowFadeOutAnimator, this.mOtherRowFadeInAnimator, animation);
        } else {
            reverseFirstOrStartSecond(this.mBgFadeInAnimator, this.mBgFadeOutAnimator, animation);
            reverseFirstOrStartSecond(this.mControlRowFadeInAnimator, this.mControlRowFadeOutAnimator, animation);
            reverseFirstOrStartSecond(this.mOtherRowFadeInAnimator, this.mOtherRowFadeOutAnimator, animation);
        }
        if (animation) {
            getView().announceForAccessibility(getString(show ? R.string.lb_playback_controls_shown : R.string.lb_playback_controls_hidden));
        }
    }

    public void setSelectedPosition(int position) {
        setSelectedPosition(position, true);
    }

    public void setSelectedPosition(int position, boolean smooth) {
        this.mSetSelectionRunnable.mPosition = position;
        this.mSetSelectionRunnable.mSmooth = smooth;
        if (getView() != null && getView().getHandler() != null) {
            getView().getHandler().post(this.mSetSelectionRunnable);
        }
    }

    private void setupChildFragmentLayout() {
        setVerticalGridViewLayout(this.mRowsSupportFragment.getVerticalGridView());
    }

    /* Access modifiers changed, original: 0000 */
    public void setVerticalGridViewLayout(VerticalGridView listview) {
        if (listview != null) {
            listview.setWindowAlignmentOffset(-this.mPaddingBottom);
            listview.setWindowAlignmentOffsetPercent(-1.0f);
            listview.setItemAlignmentOffset(this.mOtherRowsCenterToBottom - this.mPaddingBottom);
            listview.setItemAlignmentOffsetPercent(50.0f);
            listview.setPadding(listview.getPaddingLeft(), listview.getPaddingTop(), listview.getPaddingRight(), this.mPaddingBottom);
            listview.setWindowAlignment(2);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mOtherRowsCenterToBottom = getResources().getDimensionPixelSize(R.dimen.lb_playback_other_rows_center_to_bottom);
        this.mPaddingBottom = getResources().getDimensionPixelSize(R.dimen.lb_playback_controls_padding_bottom);
        this.mBgDarkColor = getResources().getColor(R.color.lb_playback_controls_background_dark);
        this.mBgLightColor = getResources().getColor(R.color.lb_playback_controls_background_light);
        this.mShowTimeMs = getResources().getInteger(R.integer.lb_playback_controls_show_time_ms);
        this.mMajorFadeTranslateY = getResources().getDimensionPixelSize(R.dimen.lb_playback_major_fade_translate_y);
        this.mMinorFadeTranslateY = getResources().getDimensionPixelSize(R.dimen.lb_playback_minor_fade_translate_y);
        loadBgAnimator();
        loadControlRowAnimator();
        loadOtherRowAnimator();
    }

    public void setBackgroundType(int type) {
        switch (type) {
            case 0:
            case 1:
            case 2:
                if (type != this.mBackgroundType) {
                    this.mBackgroundType = type;
                    updateBackground();
                    return;
                }
                return;
            default:
                throw new IllegalArgumentException("Invalid background type");
        }
    }

    public int getBackgroundType() {
        return this.mBackgroundType;
    }

    private void updateBackground() {
        if (this.mBackgroundView != null) {
            int color = this.mBgDarkColor;
            switch (this.mBackgroundType) {
                case 0:
                    color = 0;
                    break;
                case 2:
                    color = this.mBgLightColor;
                    break;
            }
            this.mBackgroundView.setBackground(new ColorDrawable(color));
            setBgAlpha(this.mBgAlpha);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRootView = inflater.inflate(R.layout.lb_playback_fragment, container, false);
        this.mBackgroundView = this.mRootView.findViewById(R.id.playback_fragment_background);
        this.mRowsSupportFragment = (RowsSupportFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_dock);
        if (this.mRowsSupportFragment == null) {
            this.mRowsSupportFragment = new RowsSupportFragment();
            getChildFragmentManager().beginTransaction().replace(R.id.playback_controls_dock, this.mRowsSupportFragment).commit();
        }
        if (this.mAdapter == null) {
            setAdapter(new ArrayObjectAdapter(new ClassPresenterSelector()));
        } else {
            this.mRowsSupportFragment.setAdapter(this.mAdapter);
        }
        this.mRowsSupportFragment.setOnItemViewSelectedListener(this.mOnItemViewSelectedListener);
        this.mRowsSupportFragment.setOnItemViewClickedListener(this.mOnItemViewClickedListener);
        this.mBgAlpha = 255;
        updateBackground();
        this.mRowsSupportFragment.setExternalAdapterListener(this.mAdapterListener);
        ProgressBarManager progressBarManager = getProgressBarManager();
        if (progressBarManager != null) {
            progressBarManager.setRootView((ViewGroup) this.mRootView);
        }
        return this.mRootView;
    }

    public void setHostCallback(HostCallback hostCallback) {
        this.mHostCallback = hostCallback;
    }

    public void onStart() {
        super.onStart();
        setupChildFragmentLayout();
        this.mRowsSupportFragment.setAdapter(this.mAdapter);
        if (this.mHostCallback != null) {
            this.mHostCallback.onHostStart();
        }
    }

    public void onStop() {
        if (this.mHostCallback != null) {
            this.mHostCallback.onHostStop();
        }
        super.onStop();
    }

    public void onPause() {
        if (this.mHostCallback != null) {
            this.mHostCallback.onHostPause();
        }
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
        super.onPause();
    }

    public void setOnItemViewSelectedListener(BaseOnItemViewSelectedListener listener) {
        this.mExternalItemSelectedListener = listener;
    }

    public void setOnItemViewClickedListener(BaseOnItemViewClickedListener listener) {
        this.mExternalItemClickedListener = listener;
    }

    public void setOnPlaybackItemViewClickedListener(BaseOnItemViewClickedListener listener) {
        this.mPlaybackItemClickedListener = listener;
    }

    public void onDestroyView() {
        this.mRootView = null;
        this.mBackgroundView = null;
        super.onDestroyView();
    }

    public void onDestroy() {
        if (this.mHostCallback != null) {
            this.mHostCallback.onHostDestroy();
        }
        super.onDestroy();
    }

    public void setPlaybackRow(Row row) {
        this.mRow = row;
        setupRow();
        setupPresenter();
    }

    public void setPlaybackRowPresenter(PlaybackRowPresenter presenter) {
        this.mPresenter = presenter;
        setupPresenter();
        setPlaybackRowPresenterAlignment();
    }

    /* Access modifiers changed, original: 0000 */
    public void setPlaybackRowPresenterAlignment() {
        if (this.mAdapter != null && this.mAdapter.getPresenterSelector() != null) {
            Presenter[] presenters = this.mAdapter.getPresenterSelector().getPresenters();
            if (presenters != null) {
                int i = 0;
                while (i < presenters.length) {
                    if ((presenters[i] instanceof PlaybackRowPresenter) && presenters[i].getFacet(ItemAlignmentFacet.class) == null) {
                        ItemAlignmentFacet itemAlignment = new ItemAlignmentFacet();
                        ItemAlignmentDef def = new ItemAlignmentDef();
                        def.setItemAlignmentOffset(0);
                        def.setItemAlignmentOffsetPercent(100.0f);
                        itemAlignment.setAlignmentDefs(new ItemAlignmentDef[]{def});
                        presenters[i].setFacet(ItemAlignmentFacet.class, itemAlignment);
                    }
                    i++;
                }
            }
        }
    }

    public void notifyPlaybackRowChanged() {
        if (this.mAdapter != null) {
            this.mAdapter.notifyItemRangeChanged(0, 1);
        }
    }

    public void setAdapter(ObjectAdapter adapter) {
        this.mAdapter = adapter;
        setupRow();
        setupPresenter();
        setPlaybackRowPresenterAlignment();
        if (this.mRowsSupportFragment != null) {
            this.mRowsSupportFragment.setAdapter(adapter);
        }
    }

    private void setupRow() {
        if ((this.mAdapter instanceof ArrayObjectAdapter) && this.mRow != null) {
            ArrayObjectAdapter adapter = this.mAdapter;
            if (adapter.size() == 0) {
                adapter.add(this.mRow);
            } else {
                adapter.replace(0, this.mRow);
            }
        } else if ((this.mAdapter instanceof SparseArrayObjectAdapter) && this.mRow != null) {
            this.mAdapter.set(0, this.mRow);
        }
    }

    private void setupPresenter() {
        if (this.mAdapter != null && this.mRow != null && this.mPresenter != null) {
            PresenterSelector selector = this.mAdapter.getPresenterSelector();
            if (selector == null) {
                selector = new ClassPresenterSelector();
                ((ClassPresenterSelector) selector).addClassPresenter(this.mRow.getClass(), this.mPresenter);
                this.mAdapter.setPresenterSelector(selector);
            } else if (selector instanceof ClassPresenterSelector) {
                ((ClassPresenterSelector) selector).addClassPresenter(this.mRow.getClass(), this.mPresenter);
            }
        }
    }

    public void setPlaybackSeekUiClient(Client client) {
        this.mSeekUiClient = client;
    }

    /* Access modifiers changed, original: 0000 */
    public void setSeekMode(boolean inSeek) {
        if (this.mInSeek != inSeek) {
            this.mInSeek = inSeek;
            getVerticalGridView().setSelectedPosition(0);
            if (this.mInSeek) {
                stopFadeTimer();
            }
            showControlsOverlay(true);
            int count = getVerticalGridView().getChildCount();
            for (int i = 0; i < count; i++) {
                View view = getVerticalGridView().getChildAt(i);
                if (getVerticalGridView().getChildAdapterPosition(view) > 0) {
                    view.setVisibility(this.mInSeek ? 4 : 0);
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onVideoSizeChanged(int videoWidth, int videoHeight) {
    }

    /* Access modifiers changed, original: protected */
    public void onBufferingStateChanged(boolean start) {
        ProgressBarManager progressBarManager = getProgressBarManager();
        if (progressBarManager == null) {
            return;
        }
        if (start) {
            progressBarManager.show();
        } else {
            progressBarManager.hide();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onError(int errorCode, CharSequence errorMessage) {
    }

    public ProgressBarManager getProgressBarManager() {
        return this.mProgressBarManager;
    }
}
