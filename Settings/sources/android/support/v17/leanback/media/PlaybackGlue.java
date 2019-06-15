package android.support.v17.leanback.media;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.v17.leanback.media.PlaybackGlueHost.HostCallback;
import java.util.ArrayList;
import java.util.List;

public abstract class PlaybackGlue {
    private final Context mContext;
    private PlaybackGlueHost mPlaybackGlueHost;
    ArrayList<PlayerCallback> mPlayerCallbacks;

    public static abstract class PlayerCallback {
        public void onPreparedStateChanged(PlaybackGlue glue) {
        }

        public void onPlayStateChanged(PlaybackGlue glue) {
        }

        public void onPlayCompleted(PlaybackGlue glue) {
        }
    }

    public PlaybackGlue(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isPrepared() {
        return true;
    }

    public void addPlayerCallback(PlayerCallback playerCallback) {
        if (this.mPlayerCallbacks == null) {
            this.mPlayerCallbacks = new ArrayList();
        }
        this.mPlayerCallbacks.add(playerCallback);
    }

    public void removePlayerCallback(PlayerCallback callback) {
        if (this.mPlayerCallbacks != null) {
            this.mPlayerCallbacks.remove(callback);
        }
    }

    /* Access modifiers changed, original: protected */
    public List<PlayerCallback> getPlayerCallbacks() {
        if (this.mPlayerCallbacks == null) {
            return null;
        }
        return new ArrayList(this.mPlayerCallbacks);
    }

    public boolean isPlaying() {
        return false;
    }

    public void play() {
    }

    public void playWhenPrepared() {
        if (isPrepared()) {
            play();
        } else {
            addPlayerCallback(new PlayerCallback() {
                public void onPreparedStateChanged(PlaybackGlue glue) {
                    if (glue.isPrepared()) {
                        PlaybackGlue.this.removePlayerCallback(this);
                        PlaybackGlue.this.play();
                    }
                }
            });
        }
    }

    public void pause() {
    }

    public void next() {
    }

    public void previous() {
    }

    public final void setHost(PlaybackGlueHost host) {
        if (this.mPlaybackGlueHost != host) {
            if (this.mPlaybackGlueHost != null) {
                this.mPlaybackGlueHost.attachToGlue(null);
            }
            this.mPlaybackGlueHost = host;
            if (this.mPlaybackGlueHost != null) {
                this.mPlaybackGlueHost.attachToGlue(this);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void onHostStart() {
    }

    /* Access modifiers changed, original: protected */
    public void onHostStop() {
    }

    /* Access modifiers changed, original: protected */
    public void onHostResume() {
    }

    /* Access modifiers changed, original: protected */
    public void onHostPause() {
    }

    /* Access modifiers changed, original: protected */
    @CallSuper
    public void onAttachedToHost(PlaybackGlueHost host) {
        this.mPlaybackGlueHost = host;
        this.mPlaybackGlueHost.setHostCallback(new HostCallback() {
            public void onHostStart() {
                PlaybackGlue.this.onHostStart();
            }

            public void onHostStop() {
                PlaybackGlue.this.onHostStop();
            }

            public void onHostResume() {
                PlaybackGlue.this.onHostResume();
            }

            public void onHostPause() {
                PlaybackGlue.this.onHostPause();
            }

            public void onHostDestroy() {
                PlaybackGlue.this.setHost(null);
            }
        });
    }

    /* Access modifiers changed, original: protected */
    @CallSuper
    public void onDetachedFromHost() {
        if (this.mPlaybackGlueHost != null) {
            this.mPlaybackGlueHost.setHostCallback(null);
            this.mPlaybackGlueHost = null;
        }
    }

    public PlaybackGlueHost getHost() {
        return this.mPlaybackGlueHost;
    }
}
