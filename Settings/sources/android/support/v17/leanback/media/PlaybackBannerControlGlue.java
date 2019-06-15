package android.support.v17.leanback.media;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter.ViewHolder;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.PlaybackRowPresenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.KeyEvent;
import android.view.View;
import com.android.settings.security.SecuritySettings;
import com.android.settings.users.UserPreference;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PlaybackBannerControlGlue<T extends PlayerAdapter> extends PlaybackBaseControlGlue<T> {
    public static final int ACTION_CUSTOM_LEFT_FIRST = 1;
    public static final int ACTION_CUSTOM_RIGHT_FIRST = 4096;
    public static final int ACTION_FAST_FORWARD = 128;
    public static final int ACTION_PLAY_PAUSE = 64;
    public static final int ACTION_REWIND = 32;
    public static final int ACTION_SKIP_TO_NEXT = 256;
    public static final int ACTION_SKIP_TO_PREVIOUS = 16;
    private static final int NUMBER_OF_SEEK_SPEEDS = 5;
    public static final int PLAYBACK_SPEED_FAST_L0 = 10;
    public static final int PLAYBACK_SPEED_FAST_L1 = 11;
    public static final int PLAYBACK_SPEED_FAST_L2 = 12;
    public static final int PLAYBACK_SPEED_FAST_L3 = 13;
    public static final int PLAYBACK_SPEED_FAST_L4 = 14;
    public static final int PLAYBACK_SPEED_INVALID = -1;
    public static final int PLAYBACK_SPEED_NORMAL = 1;
    public static final int PLAYBACK_SPEED_PAUSED = 0;
    private static final String TAG = PlaybackBannerControlGlue.class.getSimpleName();
    private FastForwardAction mFastForwardAction;
    private final int[] mFastForwardSpeeds;
    private boolean mIsCustomizedFastForwardSupported;
    private boolean mIsCustomizedRewindSupported;
    private int mPlaybackSpeed;
    private RewindAction mRewindAction;
    private final int[] mRewindSpeeds;
    private SkipNextAction mSkipNextAction;
    private SkipPreviousAction mSkipPreviousAction;
    private long mStartPosition;
    private long mStartTime;

    @RestrictTo({Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ACTION_ {
    }

    public PlaybackBannerControlGlue(Context context, int[] seekSpeeds, T impl) {
        this(context, seekSpeeds, seekSpeeds, impl);
    }

    public PlaybackBannerControlGlue(Context context, int[] fastForwardSpeeds, int[] rewindSpeeds, T impl) {
        super(context, impl);
        this.mPlaybackSpeed = 0;
        this.mStartPosition = 0;
        if (fastForwardSpeeds.length == 0 || fastForwardSpeeds.length > 5) {
            throw new IllegalArgumentException("invalid fastForwardSpeeds array size");
        }
        this.mFastForwardSpeeds = fastForwardSpeeds;
        if (rewindSpeeds.length == 0 || rewindSpeeds.length > 5) {
            throw new IllegalArgumentException("invalid rewindSpeeds array size");
        }
        this.mRewindSpeeds = rewindSpeeds;
        if ((this.mPlayerAdapter.getSupportedActions() & 128) != 0) {
            this.mIsCustomizedFastForwardSupported = true;
        }
        if ((this.mPlayerAdapter.getSupportedActions() & 32) != 0) {
            this.mIsCustomizedRewindSupported = true;
        }
    }

    public void setControlsRow(PlaybackControlsRow controlsRow) {
        super.setControlsRow(controlsRow);
        onUpdatePlaybackState();
    }

    /* Access modifiers changed, original: protected */
    public void onCreatePrimaryActions(ArrayObjectAdapter primaryActionsAdapter) {
        long supportedActions = getSupportedActions();
        if ((supportedActions & 16) != 0 && this.mSkipPreviousAction == null) {
            SkipPreviousAction skipPreviousAction = new SkipPreviousAction(getContext());
            this.mSkipPreviousAction = skipPreviousAction;
            primaryActionsAdapter.add(skipPreviousAction);
        } else if ((16 & supportedActions) == 0 && this.mSkipPreviousAction != null) {
            primaryActionsAdapter.remove(this.mSkipPreviousAction);
            this.mSkipPreviousAction = null;
        }
        if ((supportedActions & 32) != 0 && this.mRewindAction == null) {
            RewindAction rewindAction = new RewindAction(getContext(), this.mRewindSpeeds.length);
            this.mRewindAction = rewindAction;
            primaryActionsAdapter.add(rewindAction);
        } else if ((32 & supportedActions) == 0 && this.mRewindAction != null) {
            primaryActionsAdapter.remove(this.mRewindAction);
            this.mRewindAction = null;
        }
        if ((supportedActions & 64) != 0 && this.mPlayPauseAction == null) {
            this.mPlayPauseAction = new PlayPauseAction(getContext());
            PlayPauseAction playPauseAction = new PlayPauseAction(getContext());
            this.mPlayPauseAction = playPauseAction;
            primaryActionsAdapter.add(playPauseAction);
        } else if ((64 & supportedActions) == 0 && this.mPlayPauseAction != null) {
            primaryActionsAdapter.remove(this.mPlayPauseAction);
            this.mPlayPauseAction = null;
        }
        if ((supportedActions & 128) != 0 && this.mFastForwardAction == null) {
            this.mFastForwardAction = new FastForwardAction(getContext(), this.mFastForwardSpeeds.length);
            FastForwardAction fastForwardAction = new FastForwardAction(getContext(), this.mFastForwardSpeeds.length);
            this.mFastForwardAction = fastForwardAction;
            primaryActionsAdapter.add(fastForwardAction);
        } else if ((128 & supportedActions) == 0 && this.mFastForwardAction != null) {
            primaryActionsAdapter.remove(this.mFastForwardAction);
            this.mFastForwardAction = null;
        }
        if ((supportedActions & 256) != 0 && this.mSkipNextAction == null) {
            SkipNextAction skipNextAction = new SkipNextAction(getContext());
            this.mSkipNextAction = skipNextAction;
            primaryActionsAdapter.add(skipNextAction);
        } else if ((256 & supportedActions) == 0 && this.mSkipNextAction != null) {
            primaryActionsAdapter.remove(this.mSkipNextAction);
            this.mSkipNextAction = null;
        }
    }

    /* Access modifiers changed, original: protected */
    public PlaybackRowPresenter onCreateRowPresenter() {
        return new PlaybackControlsRowPresenter(new AbstractDetailsDescriptionPresenter() {
            /* Access modifiers changed, original: protected */
            public void onBindDescription(ViewHolder viewHolder, Object object) {
                PlaybackBannerControlGlue glue = (PlaybackBannerControlGlue) object;
                viewHolder.getTitle().setText(glue.getTitle());
                viewHolder.getSubtitle().setText(glue.getSubtitle());
            }
        }) {
            /* Access modifiers changed, original: protected */
            public void onBindRowViewHolder(RowPresenter.ViewHolder vh, Object item) {
                super.onBindRowViewHolder(vh, item);
                vh.setOnKeyListener(PlaybackBannerControlGlue.this);
            }

            /* Access modifiers changed, original: protected */
            public void onUnbindRowViewHolder(RowPresenter.ViewHolder vh) {
                super.onUnbindRowViewHolder(vh);
                vh.setOnKeyListener(null);
            }
        };
    }

    public void onActionClicked(Action action) {
        dispatchAction(action, null);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        boolean z = true;
        if (!(keyCode == 4 || keyCode == 111)) {
            switch (keyCode) {
                case 19:
                case 20:
                case 21:
                case 22:
                    break;
                default:
                    Action action = this.mControlsRow.getActionForKeyCode(this.mControlsRow.getPrimaryActionsAdapter(), keyCode);
                    if (action == null) {
                        action = this.mControlsRow.getActionForKeyCode(this.mControlsRow.getSecondaryActionsAdapter(), keyCode);
                    }
                    if (action == null) {
                        return false;
                    }
                    if (event.getAction() == 0) {
                        dispatchAction(action, event);
                    }
                    return true;
            }
        }
        boolean abortSeek = this.mPlaybackSpeed >= 10 || this.mPlaybackSpeed <= -10;
        if (!abortSeek) {
            return false;
        }
        play();
        onUpdatePlaybackStatusAfterUserAction();
        if (!(keyCode == 4 || keyCode == 111)) {
            z = false;
        }
        return z;
    }

    /* Access modifiers changed, original: 0000 */
    public void onUpdatePlaybackStatusAfterUserAction() {
        updatePlaybackState(this.mIsPlaying);
    }

    private void incrementFastForwardPlaybackSpeed() {
        switch (this.mPlaybackSpeed) {
            case 10:
            case 11:
            case 12:
            case 13:
                this.mPlaybackSpeed++;
                return;
            default:
                this.mPlaybackSpeed = 10;
                return;
        }
    }

    private void decrementRewindPlaybackSpeed() {
        switch (this.mPlaybackSpeed) {
            case -13:
            case -12:
            case UserPreference.USERID_GUEST_DEFAULTS /*-11*/:
            case UserPreference.USERID_UNKNOWN /*-10*/:
                this.mPlaybackSpeed--;
                return;
            default:
                this.mPlaybackSpeed = -10;
                return;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean dispatchAction(Action action, KeyEvent keyEvent) {
        if (action == this.mPlayPauseAction) {
            boolean canPause = false;
            boolean canPlay = keyEvent == null || keyEvent.getKeyCode() == 85 || keyEvent.getKeyCode() == SecuritySettings.CHANGE_TRUST_AGENT_SETTINGS;
            if (keyEvent == null || keyEvent.getKeyCode() == 85 || keyEvent.getKeyCode() == 127) {
                canPause = true;
            }
            if (canPause && (!canPlay ? this.mPlaybackSpeed == 0 : this.mPlaybackSpeed != 1)) {
                pause();
            } else if (canPlay && this.mPlaybackSpeed != 1) {
                play();
            }
            onUpdatePlaybackStatusAfterUserAction();
            return true;
        } else if (action == this.mSkipNextAction) {
            next();
            return true;
        } else if (action == this.mSkipPreviousAction) {
            previous();
            return true;
        } else if (action == this.mFastForwardAction) {
            if (this.mPlayerAdapter.isPrepared() && this.mPlaybackSpeed < getMaxForwardSpeedId()) {
                if (this.mIsCustomizedFastForwardSupported) {
                    this.mIsPlaying = true;
                    this.mPlayerAdapter.fastForward();
                } else {
                    fakePause();
                }
                incrementFastForwardPlaybackSpeed();
                onUpdatePlaybackStatusAfterUserAction();
            }
            return true;
        } else if (action != this.mRewindAction) {
            return false;
        } else {
            if (this.mPlayerAdapter.isPrepared() && this.mPlaybackSpeed > (-getMaxRewindSpeedId())) {
                if (this.mIsCustomizedFastForwardSupported) {
                    this.mIsPlaying = true;
                    this.mPlayerAdapter.rewind();
                } else {
                    fakePause();
                }
                decrementRewindPlaybackSpeed();
                onUpdatePlaybackStatusAfterUserAction();
            }
            return true;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onPlayStateChanged() {
        onUpdatePlaybackState();
        super.onPlayStateChanged();
    }

    /* Access modifiers changed, original: protected */
    public void onPlayCompleted() {
        super.onPlayCompleted();
        this.mIsPlaying = false;
        this.mPlaybackSpeed = 0;
        this.mStartPosition = getCurrentPosition();
        this.mStartTime = System.currentTimeMillis();
        onUpdatePlaybackState();
    }

    /* Access modifiers changed, original: 0000 */
    public void onUpdatePlaybackState() {
        updatePlaybackState(this.mIsPlaying);
    }

    private void updatePlaybackState(boolean isPlaying) {
        if (this.mControlsRow != null) {
            int index;
            if (isPlaying) {
                this.mPlayerAdapter.setProgressUpdatingEnabled(true);
            } else {
                onUpdateProgress();
                this.mPlayerAdapter.setProgressUpdatingEnabled(false);
            }
            if (this.mFadeWhenPlaying && getHost() != null) {
                getHost().setControlsOverlayAutoHideEnabled(isPlaying);
            }
            ArrayObjectAdapter primaryActionsAdapter = (ArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter();
            if (this.mPlayPauseAction != null) {
                boolean index2 = isPlaying;
                if (this.mPlayPauseAction.getIndex() != index2) {
                    this.mPlayPauseAction.setIndex(index2);
                    PlaybackBaseControlGlue.notifyItemChanged(primaryActionsAdapter, this.mPlayPauseAction);
                }
            }
            if (this.mFastForwardAction != null) {
                index = 0;
                if (this.mPlaybackSpeed >= 10) {
                    index = (this.mPlaybackSpeed - 10) + 1;
                }
                if (this.mFastForwardAction.getIndex() != index) {
                    this.mFastForwardAction.setIndex(index);
                    PlaybackBaseControlGlue.notifyItemChanged(primaryActionsAdapter, this.mFastForwardAction);
                }
            }
            if (this.mRewindAction != null) {
                index = 0;
                if (this.mPlaybackSpeed <= -10) {
                    index = ((-this.mPlaybackSpeed) - 10) + 1;
                }
                if (this.mRewindAction.getIndex() != index) {
                    this.mRewindAction.setIndex(index);
                    PlaybackBaseControlGlue.notifyItemChanged(primaryActionsAdapter, this.mRewindAction);
                }
            }
        }
    }

    @NonNull
    public int[] getFastForwardSpeeds() {
        return this.mFastForwardSpeeds;
    }

    @NonNull
    public int[] getRewindSpeeds() {
        return this.mRewindSpeeds;
    }

    private int getMaxForwardSpeedId() {
        return 10 + (this.mFastForwardSpeeds.length - 1);
    }

    private int getMaxRewindSpeedId() {
        return 10 + (this.mRewindSpeeds.length - 1);
    }

    public long getCurrentPosition() {
        if (this.mPlaybackSpeed == 0 || this.mPlaybackSpeed == 1) {
            return this.mPlayerAdapter.getCurrentPosition();
        }
        int speed;
        if (this.mPlaybackSpeed >= 10) {
            if (this.mIsCustomizedFastForwardSupported) {
                return this.mPlayerAdapter.getCurrentPosition();
            }
            speed = getFastForwardSpeeds()[this.mPlaybackSpeed - 10];
        } else if (this.mPlaybackSpeed > -10) {
            return -1;
        } else {
            if (this.mIsCustomizedRewindSupported) {
                return this.mPlayerAdapter.getCurrentPosition();
            }
            speed = -getRewindSpeeds()[(-this.mPlaybackSpeed) - 10];
        }
        long position = this.mStartPosition + ((System.currentTimeMillis() - this.mStartTime) * ((long) speed));
        if (position > getDuration()) {
            this.mPlaybackSpeed = 0;
            position = getDuration();
            this.mPlayerAdapter.seekTo(position);
            this.mStartPosition = 0;
            pause();
        } else if (position < 0) {
            this.mPlaybackSpeed = 0;
            position = 0;
            this.mPlayerAdapter.seekTo(0);
            this.mStartPosition = 0;
            pause();
        }
        return position;
    }

    public void play() {
        if (this.mPlayerAdapter.isPrepared()) {
            if (this.mPlaybackSpeed != 0 || this.mPlayerAdapter.getCurrentPosition() < this.mPlayerAdapter.getDuration()) {
                this.mStartPosition = getCurrentPosition();
            } else {
                this.mStartPosition = 0;
            }
            this.mStartTime = System.currentTimeMillis();
            this.mIsPlaying = true;
            this.mPlaybackSpeed = 1;
            this.mPlayerAdapter.seekTo(this.mStartPosition);
            super.play();
            onUpdatePlaybackState();
        }
    }

    public void pause() {
        this.mIsPlaying = false;
        this.mPlaybackSpeed = 0;
        this.mStartPosition = getCurrentPosition();
        this.mStartTime = System.currentTimeMillis();
        super.pause();
        onUpdatePlaybackState();
    }

    private void fakePause() {
        this.mIsPlaying = true;
        this.mStartPosition = getCurrentPosition();
        this.mStartTime = System.currentTimeMillis();
        super.pause();
        onUpdatePlaybackState();
    }
}
