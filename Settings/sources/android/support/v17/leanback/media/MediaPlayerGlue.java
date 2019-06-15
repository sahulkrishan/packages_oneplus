package android.support.v17.leanback.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v17.leanback.media.PlaybackGlue.PlayerCallback;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RepeatAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsDownAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsUpAction;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import java.io.IOException;
import java.util.List;

@RestrictTo({Scope.LIBRARY_GROUP})
@Deprecated
public class MediaPlayerGlue extends PlaybackControlGlue implements OnItemViewSelectedListener {
    public static final int FAST_FORWARD_REWIND_REPEAT_DELAY = 200;
    public static final int FAST_FORWARD_REWIND_STEP = 10000;
    public static final int NO_REPEAT = 0;
    public static final int REPEAT_ALL = 2;
    public static final int REPEAT_ONE = 1;
    private static final String TAG = "MediaPlayerGlue";
    private String mArtist;
    private Drawable mCover;
    private Handler mHandler;
    private boolean mInitialized;
    private long mLastKeyDownEvent;
    private String mMediaSourcePath;
    private Uri mMediaSourceUri;
    private OnCompletionListener mOnCompletionListener;
    MediaPlayer mPlayer;
    private final RepeatAction mRepeatAction;
    private Runnable mRunnable;
    private Action mSelectedAction;
    protected final ThumbsDownAction mThumbsDownAction;
    protected final ThumbsUpAction mThumbsUpAction;
    private String mTitle;

    class VideoPlayerSurfaceHolderCallback implements Callback {
        VideoPlayerSurfaceHolderCallback() {
        }

        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            MediaPlayerGlue.this.setDisplay(surfaceHolder);
        }

        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        }

        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            MediaPlayerGlue.this.setDisplay(null);
        }
    }

    public void setCover(Drawable cover) {
        this.mCover = cover;
    }

    public void setArtist(String artist) {
        this.mArtist = artist;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setVideoUrl(String videoUrl) {
        setMediaSource(videoUrl);
        onMetadataChanged();
    }

    public MediaPlayerGlue(Context context) {
        this(context, new int[]{1}, new int[]{1});
    }

    public MediaPlayerGlue(Context context, int[] fastForwardSpeeds, int[] rewindSpeeds) {
        super(context, fastForwardSpeeds, rewindSpeeds);
        this.mPlayer = new MediaPlayer();
        this.mHandler = new Handler();
        this.mInitialized = false;
        this.mLastKeyDownEvent = 0;
        this.mMediaSourceUri = null;
        this.mMediaSourcePath = null;
        this.mRepeatAction = new RepeatAction(getContext());
        this.mThumbsDownAction = new ThumbsDownAction(getContext());
        this.mThumbsUpAction = new ThumbsUpAction(getContext());
        this.mThumbsDownAction.setIndex(1);
        this.mThumbsUpAction.setIndex(1);
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToHost(PlaybackGlueHost host) {
        super.onAttachedToHost(host);
        if (host instanceof SurfaceHolderGlueHost) {
            ((SurfaceHolderGlueHost) host).setSurfaceHolderCallback(new VideoPlayerSurfaceHolderCallback());
        }
    }

    public void reset() {
        changeToUnitialized();
        this.mPlayer.reset();
    }

    /* Access modifiers changed, original: 0000 */
    public void changeToUnitialized() {
        if (this.mInitialized) {
            this.mInitialized = false;
            List<PlayerCallback> callbacks = getPlayerCallbacks();
            if (callbacks != null) {
                for (PlayerCallback callback : callbacks) {
                    callback.onPreparedStateChanged(this);
                }
            }
        }
    }

    public void release() {
        changeToUnitialized();
        this.mPlayer.release();
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromHost() {
        if (getHost() instanceof SurfaceHolderGlueHost) {
            ((SurfaceHolderGlueHost) getHost()).setSurfaceHolderCallback(null);
        }
        reset();
        release();
        super.onDetachedFromHost();
    }

    /* Access modifiers changed, original: protected */
    public void onCreateSecondaryActions(ArrayObjectAdapter secondaryActionsAdapter) {
        secondaryActionsAdapter.add(this.mRepeatAction);
        secondaryActionsAdapter.add(this.mThumbsDownAction);
        secondaryActionsAdapter.add(this.mThumbsUpAction);
    }

    public void setDisplay(SurfaceHolder surfaceHolder) {
        this.mPlayer.setDisplay(surfaceHolder);
    }

    public void enableProgressUpdating(boolean enabled) {
        if (this.mRunnable != null) {
            this.mHandler.removeCallbacks(this.mRunnable);
        }
        if (enabled) {
            if (this.mRunnable == null) {
                this.mRunnable = new Runnable() {
                    public void run() {
                        MediaPlayerGlue.this.updateProgress();
                        MediaPlayerGlue.this.mHandler.postDelayed(this, (long) MediaPlayerGlue.this.getUpdatePeriod());
                    }
                };
            }
            this.mHandler.postDelayed(this.mRunnable, (long) getUpdatePeriod());
        }
    }

    public void onActionClicked(Action action) {
        super.onActionClicked(action);
        if (action instanceof RepeatAction) {
            ((RepeatAction) action).nextIndex();
        } else if (action == this.mThumbsUpAction) {
            if (this.mThumbsUpAction.getIndex() == 0) {
                this.mThumbsUpAction.setIndex(1);
            } else {
                this.mThumbsUpAction.setIndex(0);
                this.mThumbsDownAction.setIndex(1);
            }
        } else if (action == this.mThumbsDownAction) {
            if (this.mThumbsDownAction.getIndex() == 0) {
                this.mThumbsDownAction.setIndex(1);
            } else {
                this.mThumbsDownAction.setIndex(0);
                this.mThumbsUpAction.setIndex(1);
            }
        }
        onMetadataChanged();
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        boolean z = false;
        boolean z2 = (this.mSelectedAction instanceof RewindAction) || (this.mSelectedAction instanceof FastForwardAction);
        z2 = z2 && this.mInitialized;
        z2 = z2 && event.getKeyCode() == 23;
        z2 = z2 && event.getAction() == 0;
        if (z2 && System.currentTimeMillis() - this.mLastKeyDownEvent > 200) {
            z = true;
        }
        if (!z) {
            return super.onKey(v, keyCode, event);
        }
        this.mLastKeyDownEvent = System.currentTimeMillis();
        int newPosition = getCurrentPosition() + FAST_FORWARD_REWIND_STEP;
        if (this.mSelectedAction instanceof RewindAction) {
            newPosition = getCurrentPosition() - 10000;
        }
        if (newPosition < 0) {
            newPosition = 0;
        }
        if (newPosition > getMediaDuration()) {
            newPosition = getMediaDuration();
        }
        seekTo(newPosition);
        return true;
    }

    public boolean hasValidMedia() {
        return (this.mTitle == null || (this.mMediaSourcePath == null && this.mMediaSourceUri == null)) ? false : true;
    }

    public boolean isMediaPlaying() {
        return this.mInitialized && this.mPlayer.isPlaying();
    }

    public boolean isPlaying() {
        return isMediaPlaying();
    }

    public CharSequence getMediaTitle() {
        return this.mTitle != null ? this.mTitle : "N/a";
    }

    public CharSequence getMediaSubtitle() {
        return this.mArtist != null ? this.mArtist : "N/a";
    }

    public int getMediaDuration() {
        return this.mInitialized ? this.mPlayer.getDuration() : 0;
    }

    public Drawable getMediaArt() {
        return this.mCover;
    }

    public long getSupportedActions() {
        return 224;
    }

    public int getCurrentSpeedId() {
        return isMediaPlaying();
    }

    public int getCurrentPosition() {
        return this.mInitialized ? this.mPlayer.getCurrentPosition() : 0;
    }

    public void play(int speed) {
        if (this.mInitialized && !this.mPlayer.isPlaying()) {
            this.mPlayer.start();
            onMetadataChanged();
            onStateChanged();
            updateProgress();
        }
    }

    public void pause() {
        if (isMediaPlaying()) {
            this.mPlayer.pause();
            onStateChanged();
        }
    }

    public void setMode(int mode) {
        switch (mode) {
            case 0:
                this.mOnCompletionListener = null;
                return;
            case 1:
                this.mOnCompletionListener = new OnCompletionListener() {
                    public boolean mFirstRepeat;

                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (!this.mFirstRepeat) {
                            this.mFirstRepeat = true;
                            mediaPlayer.setOnCompletionListener(null);
                        }
                        MediaPlayerGlue.this.play();
                    }
                };
                return;
            case 2:
                this.mOnCompletionListener = new OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        MediaPlayerGlue.this.play();
                    }
                };
                return;
            default:
                return;
        }
    }

    /* Access modifiers changed, original: protected */
    public void seekTo(int newPosition) {
        if (this.mInitialized) {
            this.mPlayer.seekTo(newPosition);
        }
    }

    public boolean setMediaSource(Uri uri) {
        if (!this.mMediaSourceUri == null ? !this.mMediaSourceUri.equals(uri) : uri != null) {
            return false;
        }
        this.mMediaSourceUri = uri;
        this.mMediaSourcePath = null;
        prepareMediaForPlaying();
        return true;
    }

    public boolean setMediaSource(String path) {
        if (!this.mMediaSourcePath == null ? !this.mMediaSourcePath.equals(path) : path != null) {
            return false;
        }
        this.mMediaSourceUri = null;
        this.mMediaSourcePath = path;
        prepareMediaForPlaying();
        return true;
    }

    private void prepareMediaForPlaying() {
        reset();
        try {
            if (this.mMediaSourceUri != null) {
                this.mPlayer.setDataSource(getContext(), this.mMediaSourceUri);
            } else if (this.mMediaSourcePath != null) {
                this.mPlayer.setDataSource(this.mMediaSourcePath);
            } else {
                return;
            }
            this.mPlayer.setAudioStreamType(3);
            this.mPlayer.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    MediaPlayerGlue.this.mInitialized = true;
                    List<PlayerCallback> callbacks = MediaPlayerGlue.this.getPlayerCallbacks();
                    if (callbacks != null) {
                        for (PlayerCallback callback : callbacks) {
                            callback.onPreparedStateChanged(MediaPlayerGlue.this);
                        }
                    }
                }
            });
            if (this.mOnCompletionListener != null) {
                this.mPlayer.setOnCompletionListener(this.mOnCompletionListener);
            }
            this.mPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    if (MediaPlayerGlue.this.getControlsRow() != null) {
                        MediaPlayerGlue.this.getControlsRow().setBufferedProgress((int) (((float) mp.getDuration()) * (((float) percent) / 100.0f)));
                    }
                }
            });
            this.mPlayer.prepareAsync();
            onStateChanged();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void onItemSelected(ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if (item instanceof Action) {
            this.mSelectedAction = (Action) item;
        } else {
            this.mSelectedAction = null;
        }
    }

    public boolean isPrepared() {
        return this.mInitialized;
    }
}
