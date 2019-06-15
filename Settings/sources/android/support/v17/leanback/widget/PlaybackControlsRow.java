package android.support.v17.leanback.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.R;
import android.support.v17.leanback.util.MathUtil;
import android.util.TypedValue;
import com.android.settings.security.SecuritySettings;

public class PlaybackControlsRow extends Row {
    private long mBufferedProgressMs;
    private long mCurrentTimeMs;
    private Drawable mImageDrawable;
    private Object mItem;
    private OnPlaybackProgressCallback mListener;
    private ObjectAdapter mPrimaryActionsAdapter;
    private ObjectAdapter mSecondaryActionsAdapter;
    private long mTotalTimeMs;

    public static class OnPlaybackProgressCallback {
        public void onCurrentPositionChanged(PlaybackControlsRow row, long currentTimeMs) {
        }

        public void onDurationChanged(PlaybackControlsRow row, long totalTime) {
        }

        public void onBufferedPositionChanged(PlaybackControlsRow row, long bufferedProgressMs) {
        }
    }

    public static class MoreActions extends Action {
        public MoreActions(Context context) {
            super((long) R.id.lb_control_more_actions);
            setIcon(context.getResources().getDrawable(R.drawable.lb_ic_more));
            setLabel1(context.getString(R.string.lb_playback_controls_more_actions));
        }
    }

    public static abstract class MultiAction extends Action {
        private Drawable[] mDrawables;
        private int mIndex;
        private String[] mLabels;
        private String[] mLabels2;

        public MultiAction(int id) {
            super((long) id);
        }

        public void setDrawables(Drawable[] drawables) {
            this.mDrawables = drawables;
            setIndex(0);
        }

        public void setLabels(String[] labels) {
            this.mLabels = labels;
            setIndex(0);
        }

        public void setSecondaryLabels(String[] labels) {
            this.mLabels2 = labels;
            setIndex(0);
        }

        public int getActionCount() {
            if (this.mDrawables != null) {
                return this.mDrawables.length;
            }
            if (this.mLabels != null) {
                return this.mLabels.length;
            }
            return 0;
        }

        public Drawable getDrawable(int index) {
            return this.mDrawables == null ? null : this.mDrawables[index];
        }

        public String getLabel(int index) {
            return this.mLabels == null ? null : this.mLabels[index];
        }

        public String getSecondaryLabel(int index) {
            return this.mLabels2 == null ? null : this.mLabels2[index];
        }

        public void nextIndex() {
            setIndex(this.mIndex < getActionCount() + -1 ? this.mIndex + 1 : 0);
        }

        public void setIndex(int index) {
            this.mIndex = index;
            if (this.mDrawables != null) {
                setIcon(this.mDrawables[this.mIndex]);
            }
            if (this.mLabels != null) {
                setLabel1(this.mLabels[this.mIndex]);
            }
            if (this.mLabels2 != null) {
                setLabel2(this.mLabels2[this.mIndex]);
            }
        }

        public int getIndex() {
            return this.mIndex;
        }
    }

    public static class PictureInPictureAction extends Action {
        public PictureInPictureAction(Context context) {
            super((long) R.id.lb_control_picture_in_picture);
            setIcon(PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_picture_in_picture));
            setLabel1(context.getString(R.string.lb_playback_controls_picture_in_picture));
            addKeyCode(171);
        }
    }

    public static class SkipNextAction extends Action {
        public SkipNextAction(Context context) {
            super((long) R.id.lb_control_skip_next);
            setIcon(PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_skip_next));
            setLabel1(context.getString(R.string.lb_playback_controls_skip_next));
            addKeyCode(87);
        }
    }

    public static class SkipPreviousAction extends Action {
        public SkipPreviousAction(Context context) {
            super((long) R.id.lb_control_skip_previous);
            setIcon(PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_skip_previous));
            setLabel1(context.getString(R.string.lb_playback_controls_skip_previous));
            addKeyCode(88);
        }
    }

    public static class ClosedCaptioningAction extends MultiAction {
        public static final int INDEX_OFF = 0;
        public static final int INDEX_ON = 1;
        @Deprecated
        public static final int OFF = 0;
        @Deprecated
        public static final int ON = 1;

        public ClosedCaptioningAction(Context context) {
            this(context, PlaybackControlsRow.getIconHighlightColor(context));
        }

        public ClosedCaptioningAction(Context context, int highlightColor) {
            super(R.id.lb_control_closed_captioning);
            BitmapDrawable uncoloredDrawable = (BitmapDrawable) PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_closed_captioning);
            Drawable[] drawables = new Drawable[]{uncoloredDrawable, new BitmapDrawable(context.getResources(), PlaybackControlsRow.createBitmap(uncoloredDrawable.getBitmap(), highlightColor))};
            setDrawables(drawables);
            String[] labels = new String[drawables.length];
            labels[0] = context.getString(R.string.lb_playback_controls_closed_captioning_enable);
            labels[1] = context.getString(R.string.lb_playback_controls_closed_captioning_disable);
            setLabels(labels);
        }
    }

    public static class FastForwardAction extends MultiAction {
        public FastForwardAction(Context context) {
            this(context, 1);
        }

        public FastForwardAction(Context context, int numSpeeds) {
            super(R.id.lb_control_fast_forward);
            if (numSpeeds >= 1) {
                Drawable[] drawables = new Drawable[(numSpeeds + 1)];
                drawables[0] = PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_fast_forward);
                setDrawables(drawables);
                String[] labels = new String[getActionCount()];
                labels[0] = context.getString(R.string.lb_playback_controls_fast_forward);
                String[] labels2 = new String[getActionCount()];
                labels2[0] = labels[0];
                for (int i = 1; i <= numSpeeds; i++) {
                    int multiplier = i + 1;
                    labels[i] = context.getResources().getString(R.string.lb_control_display_fast_forward_multiplier, new Object[]{Integer.valueOf(multiplier)});
                    labels2[i] = context.getResources().getString(R.string.lb_playback_controls_fast_forward_multiplier, new Object[]{Integer.valueOf(multiplier)});
                }
                setLabels(labels);
                setSecondaryLabels(labels2);
                addKeyCode(90);
                return;
            }
            throw new IllegalArgumentException("numSpeeds must be > 0");
        }
    }

    public static class HighQualityAction extends MultiAction {
        public static final int INDEX_OFF = 0;
        public static final int INDEX_ON = 1;
        @Deprecated
        public static final int OFF = 0;
        @Deprecated
        public static final int ON = 1;

        public HighQualityAction(Context context) {
            this(context, PlaybackControlsRow.getIconHighlightColor(context));
        }

        public HighQualityAction(Context context, int highlightColor) {
            super(R.id.lb_control_high_quality);
            BitmapDrawable uncoloredDrawable = (BitmapDrawable) PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_high_quality);
            Drawable[] drawables = new Drawable[]{uncoloredDrawable, new BitmapDrawable(context.getResources(), PlaybackControlsRow.createBitmap(uncoloredDrawable.getBitmap(), highlightColor))};
            setDrawables(drawables);
            String[] labels = new String[drawables.length];
            labels[0] = context.getString(R.string.lb_playback_controls_high_quality_enable);
            labels[1] = context.getString(R.string.lb_playback_controls_high_quality_disable);
            setLabels(labels);
        }
    }

    public static class PlayPauseAction extends MultiAction {
        public static final int INDEX_PAUSE = 1;
        public static final int INDEX_PLAY = 0;
        @Deprecated
        public static final int PAUSE = 1;
        @Deprecated
        public static final int PLAY = 0;

        public PlayPauseAction(Context context) {
            super(R.id.lb_control_play_pause);
            Drawable[] drawables = new Drawable[]{PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_play), PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_pause)};
            setDrawables(drawables);
            String[] labels = new String[drawables.length];
            labels[0] = context.getString(R.string.lb_playback_controls_play);
            labels[1] = context.getString(R.string.lb_playback_controls_pause);
            setLabels(labels);
            addKeyCode(85);
            addKeyCode(SecuritySettings.CHANGE_TRUST_AGENT_SETTINGS);
            addKeyCode(127);
        }
    }

    public static class RepeatAction extends MultiAction {
        @Deprecated
        public static final int ALL = 1;
        public static final int INDEX_ALL = 1;
        public static final int INDEX_NONE = 0;
        public static final int INDEX_ONE = 2;
        @Deprecated
        public static final int NONE = 0;
        @Deprecated
        public static final int ONE = 2;

        public RepeatAction(Context context) {
            this(context, PlaybackControlsRow.getIconHighlightColor(context));
        }

        public RepeatAction(Context context, int highlightColor) {
            this(context, highlightColor, highlightColor);
        }

        public RepeatAction(Context context, int repeatAllColor, int repeatOneColor) {
            super(R.id.lb_control_repeat);
            drawables = new Drawable[3];
            BitmapDrawable repeatDrawable = (BitmapDrawable) PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_repeat);
            BitmapDrawable repeatOneDrawable = (BitmapDrawable) PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_repeat_one);
            drawables[0] = repeatDrawable;
            BitmapDrawable bitmapDrawable = null;
            drawables[1] = repeatDrawable == null ? null : new BitmapDrawable(context.getResources(), PlaybackControlsRow.createBitmap(repeatDrawable.getBitmap(), repeatAllColor));
            if (repeatOneDrawable != null) {
                bitmapDrawable = new BitmapDrawable(context.getResources(), PlaybackControlsRow.createBitmap(repeatOneDrawable.getBitmap(), repeatOneColor));
            }
            drawables[2] = bitmapDrawable;
            setDrawables(drawables);
            String[] labels = new String[drawables.length];
            labels[0] = context.getString(R.string.lb_playback_controls_repeat_all);
            labels[1] = context.getString(R.string.lb_playback_controls_repeat_one);
            labels[2] = context.getString(R.string.lb_playback_controls_repeat_none);
            setLabels(labels);
        }
    }

    public static class RewindAction extends MultiAction {
        public RewindAction(Context context) {
            this(context, 1);
        }

        public RewindAction(Context context, int numSpeeds) {
            super(R.id.lb_control_fast_rewind);
            if (numSpeeds >= 1) {
                Drawable[] drawables = new Drawable[(numSpeeds + 1)];
                drawables[0] = PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_rewind);
                setDrawables(drawables);
                String[] labels = new String[getActionCount()];
                labels[0] = context.getString(R.string.lb_playback_controls_rewind);
                String[] labels2 = new String[getActionCount()];
                labels2[0] = labels[0];
                for (int i = 1; i <= numSpeeds; i++) {
                    int multiplier = i + 1;
                    String string = context.getResources().getString(R.string.lb_control_display_rewind_multiplier, new Object[]{Integer.valueOf(multiplier)});
                    labels[i] = string;
                    labels[i] = string;
                    labels2[i] = context.getResources().getString(R.string.lb_playback_controls_rewind_multiplier, new Object[]{Integer.valueOf(multiplier)});
                }
                setLabels(labels);
                setSecondaryLabels(labels2);
                addKeyCode(89);
                return;
            }
            throw new IllegalArgumentException("numSpeeds must be > 0");
        }
    }

    public static class ShuffleAction extends MultiAction {
        public static final int INDEX_OFF = 0;
        public static final int INDEX_ON = 1;
        @Deprecated
        public static final int OFF = 0;
        @Deprecated
        public static final int ON = 1;

        public ShuffleAction(Context context) {
            this(context, PlaybackControlsRow.getIconHighlightColor(context));
        }

        public ShuffleAction(Context context, int highlightColor) {
            super(R.id.lb_control_shuffle);
            BitmapDrawable uncoloredDrawable = (BitmapDrawable) PlaybackControlsRow.getStyledDrawable(context, R.styleable.lbPlaybackControlsActionIcons_shuffle);
            Drawable[] drawables = new Drawable[]{uncoloredDrawable, new BitmapDrawable(context.getResources(), PlaybackControlsRow.createBitmap(uncoloredDrawable.getBitmap(), highlightColor))};
            setDrawables(drawables);
            String[] labels = new String[drawables.length];
            labels[0] = context.getString(R.string.lb_playback_controls_shuffle_enable);
            labels[1] = context.getString(R.string.lb_playback_controls_shuffle_disable);
            setLabels(labels);
        }
    }

    public static abstract class ThumbsAction extends MultiAction {
        public static final int INDEX_OUTLINE = 1;
        public static final int INDEX_SOLID = 0;
        @Deprecated
        public static final int OUTLINE = 1;
        @Deprecated
        public static final int SOLID = 0;

        public ThumbsAction(int id, Context context, int solidIconIndex, int outlineIconIndex) {
            super(id);
            setDrawables(new Drawable[]{PlaybackControlsRow.getStyledDrawable(context, solidIconIndex), PlaybackControlsRow.getStyledDrawable(context, outlineIconIndex)});
        }
    }

    public static class ThumbsDownAction extends ThumbsAction {
        public ThumbsDownAction(Context context) {
            super(R.id.lb_control_thumbs_down, context, R.styleable.lbPlaybackControlsActionIcons_thumb_down, R.styleable.lbPlaybackControlsActionIcons_thumb_down_outline);
            String[] labels = new String[getActionCount()];
            labels[0] = context.getString(R.string.lb_playback_controls_thumb_down);
            labels[1] = context.getString(R.string.lb_playback_controls_thumb_down_outline);
            setLabels(labels);
        }
    }

    public static class ThumbsUpAction extends ThumbsAction {
        public ThumbsUpAction(Context context) {
            super(R.id.lb_control_thumbs_up, context, R.styleable.lbPlaybackControlsActionIcons_thumb_up, R.styleable.lbPlaybackControlsActionIcons_thumb_up_outline);
            String[] labels = new String[getActionCount()];
            labels[0] = context.getString(R.string.lb_playback_controls_thumb_up);
            labels[1] = context.getString(R.string.lb_playback_controls_thumb_up_outline);
            setLabels(labels);
        }
    }

    static Bitmap createBitmap(Bitmap bitmap, int color) {
        Bitmap dst = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(dst);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return dst;
    }

    static int getIconHighlightColor(Context context) {
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.playbackControlsIconHighlightColor, outValue, true)) {
            return outValue.data;
        }
        return context.getResources().getColor(R.color.lb_playback_icon_highlight_no_theme);
    }

    static Drawable getStyledDrawable(Context context, int index) {
        TypedValue outValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(R.attr.playbackControlsActionIcons, outValue, false)) {
            return null;
        }
        TypedArray array = context.getTheme().obtainStyledAttributes(outValue.data, R.styleable.lbPlaybackControlsActionIcons);
        Drawable drawable = array.getDrawable(index);
        array.recycle();
        return drawable;
    }

    public PlaybackControlsRow(Object item) {
        this.mItem = item;
    }

    public final Object getItem() {
        return this.mItem;
    }

    public final void setImageDrawable(Drawable drawable) {
        this.mImageDrawable = drawable;
    }

    public final void setImageBitmap(Context context, Bitmap bm) {
        this.mImageDrawable = new BitmapDrawable(context.getResources(), bm);
    }

    public final Drawable getImageDrawable() {
        return this.mImageDrawable;
    }

    public final void setPrimaryActionsAdapter(ObjectAdapter adapter) {
        this.mPrimaryActionsAdapter = adapter;
    }

    public final void setSecondaryActionsAdapter(ObjectAdapter adapter) {
        this.mSecondaryActionsAdapter = adapter;
    }

    public final ObjectAdapter getPrimaryActionsAdapter() {
        return this.mPrimaryActionsAdapter;
    }

    public final ObjectAdapter getSecondaryActionsAdapter() {
        return this.mSecondaryActionsAdapter;
    }

    @Deprecated
    public void setTotalTime(int ms) {
        setDuration((long) ms);
    }

    @Deprecated
    public void setTotalTimeLong(long ms) {
        setDuration(ms);
    }

    public void setDuration(long ms) {
        if (this.mTotalTimeMs != ms) {
            this.mTotalTimeMs = ms;
            if (this.mListener != null) {
                this.mListener.onDurationChanged(this, this.mTotalTimeMs);
            }
        }
    }

    @Deprecated
    public int getTotalTime() {
        return MathUtil.safeLongToInt(getTotalTimeLong());
    }

    @Deprecated
    public long getTotalTimeLong() {
        return this.mTotalTimeMs;
    }

    public long getDuration() {
        return this.mTotalTimeMs;
    }

    @Deprecated
    public void setCurrentTime(int ms) {
        setCurrentTimeLong((long) ms);
    }

    @Deprecated
    public void setCurrentTimeLong(long ms) {
        setCurrentPosition(ms);
    }

    public void setCurrentPosition(long ms) {
        if (this.mCurrentTimeMs != ms) {
            this.mCurrentTimeMs = ms;
            if (this.mListener != null) {
                this.mListener.onCurrentPositionChanged(this, this.mCurrentTimeMs);
            }
        }
    }

    @Deprecated
    public int getCurrentTime() {
        return MathUtil.safeLongToInt(getCurrentTimeLong());
    }

    @Deprecated
    public long getCurrentTimeLong() {
        return this.mCurrentTimeMs;
    }

    public long getCurrentPosition() {
        return this.mCurrentTimeMs;
    }

    @Deprecated
    public void setBufferedProgress(int ms) {
        setBufferedPosition((long) ms);
    }

    @Deprecated
    public void setBufferedProgressLong(long ms) {
        setBufferedPosition(ms);
    }

    public void setBufferedPosition(long ms) {
        if (this.mBufferedProgressMs != ms) {
            this.mBufferedProgressMs = ms;
            if (this.mListener != null) {
                this.mListener.onBufferedPositionChanged(this, this.mBufferedProgressMs);
            }
        }
    }

    @Deprecated
    public int getBufferedProgress() {
        return MathUtil.safeLongToInt(getBufferedPosition());
    }

    @Deprecated
    public long getBufferedProgressLong() {
        return this.mBufferedProgressMs;
    }

    public long getBufferedPosition() {
        return this.mBufferedProgressMs;
    }

    public Action getActionForKeyCode(int keyCode) {
        Action action = getActionForKeyCode(getPrimaryActionsAdapter(), keyCode);
        if (action != null) {
            return action;
        }
        return getActionForKeyCode(getSecondaryActionsAdapter(), keyCode);
    }

    public Action getActionForKeyCode(ObjectAdapter adapter, int keyCode) {
        if (adapter == this.mPrimaryActionsAdapter || adapter == this.mSecondaryActionsAdapter) {
            for (int i = 0; i < adapter.size(); i++) {
                Action action = (Action) adapter.get(i);
                if (action.respondsToKeyCode(keyCode)) {
                    return action;
                }
            }
            return null;
        }
        throw new IllegalArgumentException("Invalid adapter");
    }

    public void setOnPlaybackProgressChangedListener(OnPlaybackProgressCallback listener) {
        this.mListener = listener;
    }
}
