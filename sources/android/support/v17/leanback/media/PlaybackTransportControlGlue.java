package android.support.v17.leanback.media;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter.ViewHolder;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import android.support.v17.leanback.widget.PlaybackRowPresenter;
import android.support.v17.leanback.widget.PlaybackSeekDataProvider;
import android.support.v17.leanback.widget.PlaybackSeekUi;
import android.support.v17.leanback.widget.PlaybackSeekUi.Client;
import android.support.v17.leanback.widget.PlaybackTransportRowPresenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.KeyEvent;
import android.view.View;
import com.android.settings.security.SecuritySettings;
import java.lang.ref.WeakReference;

public class PlaybackTransportControlGlue<T extends PlayerAdapter> extends PlaybackBaseControlGlue<T> {
    static final boolean DEBUG = false;
    static final int MSG_UPDATE_PLAYBACK_STATE = 100;
    static final String TAG = "PlaybackTransportGlue";
    static final int UPDATE_PLAYBACK_STATE_DELAY_MS = 2000;
    static final Handler sHandler = new UpdatePlaybackStateHandler();
    final WeakReference<PlaybackBaseControlGlue> mGlueWeakReference = new WeakReference(this);
    final SeekUiClient mPlaybackSeekUiClient = new SeekUiClient();
    boolean mSeekEnabled;
    PlaybackSeekDataProvider mSeekProvider;

    static class UpdatePlaybackStateHandler extends Handler {
        UpdatePlaybackStateHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                PlaybackTransportControlGlue glue = (PlaybackTransportControlGlue) ((WeakReference) msg.obj).get();
                if (glue != null) {
                    glue.onUpdatePlaybackState();
                }
            }
        }
    }

    class SeekUiClient extends Client {
        boolean mIsSeek;
        long mLastUserPosition;
        boolean mPausedBeforeSeek;
        long mPositionBeforeSeek;

        SeekUiClient() {
        }

        public PlaybackSeekDataProvider getPlaybackSeekDataProvider() {
            return PlaybackTransportControlGlue.this.mSeekProvider;
        }

        public boolean isSeekEnabled() {
            return PlaybackTransportControlGlue.this.mSeekProvider != null || PlaybackTransportControlGlue.this.mSeekEnabled;
        }

        public void onSeekStarted() {
            this.mIsSeek = true;
            this.mPausedBeforeSeek = PlaybackTransportControlGlue.this.isPlaying() ^ 1;
            PlaybackTransportControlGlue.this.mPlayerAdapter.setProgressUpdatingEnabled(true);
            this.mPositionBeforeSeek = PlaybackTransportControlGlue.this.mSeekProvider == null ? PlaybackTransportControlGlue.this.mPlayerAdapter.getCurrentPosition() : -1;
            this.mLastUserPosition = -1;
            PlaybackTransportControlGlue.this.pause();
        }

        public void onSeekPositionChanged(long pos) {
            if (PlaybackTransportControlGlue.this.mSeekProvider == null) {
                PlaybackTransportControlGlue.this.mPlayerAdapter.seekTo(pos);
            } else {
                this.mLastUserPosition = pos;
            }
            if (PlaybackTransportControlGlue.this.mControlsRow != null) {
                PlaybackTransportControlGlue.this.mControlsRow.setCurrentPosition(pos);
            }
        }

        public void onSeekFinished(boolean cancelled) {
            if (cancelled) {
                if (this.mPositionBeforeSeek >= 0) {
                    PlaybackTransportControlGlue.this.seekTo(this.mPositionBeforeSeek);
                }
            } else if (this.mLastUserPosition >= 0) {
                PlaybackTransportControlGlue.this.seekTo(this.mLastUserPosition);
            }
            this.mIsSeek = false;
            if (this.mPausedBeforeSeek) {
                PlaybackTransportControlGlue.this.mPlayerAdapter.setProgressUpdatingEnabled(false);
                PlaybackTransportControlGlue.this.onUpdateProgress();
                return;
            }
            PlaybackTransportControlGlue.this.play();
        }
    }

    public PlaybackTransportControlGlue(Context context, T impl) {
        super(context, impl);
    }

    public void setControlsRow(PlaybackControlsRow controlsRow) {
        super.setControlsRow(controlsRow);
        sHandler.removeMessages(100, this.mGlueWeakReference);
        onUpdatePlaybackState();
    }

    /* Access modifiers changed, original: protected */
    public void onCreatePrimaryActions(ArrayObjectAdapter primaryActionsAdapter) {
        PlayPauseAction playPauseAction = new PlayPauseAction(getContext());
        this.mPlayPauseAction = playPauseAction;
        primaryActionsAdapter.add(playPauseAction);
    }

    /* Access modifiers changed, original: protected */
    public PlaybackRowPresenter onCreateRowPresenter() {
        AbstractDetailsDescriptionPresenter detailsPresenter = new AbstractDetailsDescriptionPresenter() {
            /* Access modifiers changed, original: protected */
            public void onBindDescription(ViewHolder viewHolder, Object obj) {
                PlaybackBaseControlGlue glue = (PlaybackBaseControlGlue) obj;
                viewHolder.getTitle().setText(glue.getTitle());
                viewHolder.getSubtitle().setText(glue.getSubtitle());
            }
        };
        PlaybackTransportRowPresenter rowPresenter = new PlaybackTransportRowPresenter() {
            /* Access modifiers changed, original: protected */
            public void onBindRowViewHolder(RowPresenter.ViewHolder vh, Object item) {
                super.onBindRowViewHolder(vh, item);
                vh.setOnKeyListener(PlaybackTransportControlGlue.this);
            }

            /* Access modifiers changed, original: protected */
            public void onUnbindRowViewHolder(RowPresenter.ViewHolder vh) {
                super.onUnbindRowViewHolder(vh);
                vh.setOnKeyListener(null);
            }
        };
        rowPresenter.setDescriptionPresenter(detailsPresenter);
        return rowPresenter;
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToHost(PlaybackGlueHost host) {
        super.onAttachedToHost(host);
        if (host instanceof PlaybackSeekUi) {
            ((PlaybackSeekUi) host).setPlaybackSeekUiClient(this.mPlaybackSeekUiClient);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromHost() {
        super.onDetachedFromHost();
        if (getHost() instanceof PlaybackSeekUi) {
            ((PlaybackSeekUi) getHost()).setPlaybackSeekUiClient(null);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onUpdateProgress() {
        if (!this.mPlaybackSeekUiClient.mIsSeek) {
            super.onUpdateProgress();
        }
    }

    public void onActionClicked(Action action) {
        dispatchAction(action, null);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
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
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public void onUpdatePlaybackStatusAfterUserAction() {
        updatePlaybackState(this.mIsPlaying);
        sHandler.removeMessages(100, this.mGlueWeakReference);
        sHandler.sendMessageDelayed(sHandler.obtainMessage(100, this.mGlueWeakReference), 2000);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean dispatchAction(Action action, KeyEvent keyEvent) {
        if (action instanceof PlayPauseAction) {
            boolean canPlay = keyEvent == null || keyEvent.getKeyCode() == 85 || keyEvent.getKeyCode() == SecuritySettings.CHANGE_TRUST_AGENT_SETTINGS;
            boolean canPause = keyEvent == null || keyEvent.getKeyCode() == 85 || keyEvent.getKeyCode() == 127;
            if (canPause && this.mIsPlaying) {
                this.mIsPlaying = false;
                pause();
            } else if (canPlay && !this.mIsPlaying) {
                this.mIsPlaying = true;
                play();
            }
            onUpdatePlaybackStatusAfterUserAction();
            return true;
        } else if (action instanceof SkipNextAction) {
            next();
            return true;
        } else if (!(action instanceof SkipPreviousAction)) {
            return false;
        } else {
            previous();
            return true;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onPlayStateChanged() {
        if (sHandler.hasMessages(100, this.mGlueWeakReference)) {
            sHandler.removeMessages(100, this.mGlueWeakReference);
            if (this.mPlayerAdapter.isPlaying() != this.mIsPlaying) {
                sHandler.sendMessageDelayed(sHandler.obtainMessage(100, this.mGlueWeakReference), 2000);
            } else {
                onUpdatePlaybackState();
            }
        } else {
            onUpdatePlaybackState();
        }
        super.onPlayStateChanged();
    }

    /* Access modifiers changed, original: 0000 */
    public void onUpdatePlaybackState() {
        this.mIsPlaying = this.mPlayerAdapter.isPlaying();
        updatePlaybackState(this.mIsPlaying);
    }

    private void updatePlaybackState(boolean isPlaying) {
        if (this.mControlsRow != null) {
            if (isPlaying) {
                this.mPlayerAdapter.setProgressUpdatingEnabled(true);
            } else {
                onUpdateProgress();
                this.mPlayerAdapter.setProgressUpdatingEnabled(this.mPlaybackSeekUiClient.mIsSeek);
            }
            if (this.mFadeWhenPlaying && getHost() != null) {
                getHost().setControlsOverlayAutoHideEnabled(isPlaying);
            }
            if (this.mPlayPauseAction != null) {
                boolean index = isPlaying;
                if (this.mPlayPauseAction.getIndex() != index) {
                    this.mPlayPauseAction.setIndex(index);
                    PlaybackBaseControlGlue.notifyItemChanged((ArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter(), this.mPlayPauseAction);
                }
            }
        }
    }

    public final void setSeekProvider(PlaybackSeekDataProvider seekProvider) {
        this.mSeekProvider = seekProvider;
    }

    public final PlaybackSeekDataProvider getSeekProvider() {
        return this.mSeekProvider;
    }

    public final void setSeekEnabled(boolean seekEnabled) {
        this.mSeekEnabled = seekEnabled;
    }

    public final boolean isSeekEnabled() {
        return this.mSeekEnabled;
    }
}
