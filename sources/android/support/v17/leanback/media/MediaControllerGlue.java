package android.support.v17.leanback.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaControllerCompat.Callback;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

@Deprecated
public abstract class MediaControllerGlue extends PlaybackControlGlue {
    static final boolean DEBUG = false;
    static final String TAG = "MediaControllerGlue";
    private final Callback mCallback = new Callback() {
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            MediaControllerGlue.this.onMetadataChanged();
        }

        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            MediaControllerGlue.this.onStateChanged();
        }

        public void onSessionDestroyed() {
            MediaControllerGlue.this.mMediaController = null;
        }

        public void onSessionEvent(String event, Bundle extras) {
        }
    };
    MediaControllerCompat mMediaController;

    public MediaControllerGlue(Context context, int[] fastForwardSpeeds, int[] rewindSpeeds) {
        super(context, fastForwardSpeeds, rewindSpeeds);
    }

    public void attachToMediaController(MediaControllerCompat mediaController) {
        if (mediaController != this.mMediaController) {
            detach();
            this.mMediaController = mediaController;
            if (this.mMediaController != null) {
                this.mMediaController.registerCallback(this.mCallback);
            }
            onMetadataChanged();
            onStateChanged();
        }
    }

    public void detach() {
        if (this.mMediaController != null) {
            this.mMediaController.unregisterCallback(this.mCallback);
        }
        this.mMediaController = null;
    }

    public final MediaControllerCompat getMediaController() {
        return this.mMediaController;
    }

    public boolean hasValidMedia() {
        return (this.mMediaController == null || this.mMediaController.getMetadata() == null) ? false : true;
    }

    public boolean isMediaPlaying() {
        return this.mMediaController.getPlaybackState().getState() == 3;
    }

    public int getCurrentSpeedId() {
        int speed = (int) this.mMediaController.getPlaybackState().getPlaybackSpeed();
        int index = 0;
        if (speed == 0) {
            return 0;
        }
        if (speed == 1) {
            return 1;
        }
        int[] seekSpeeds;
        if (speed > 0) {
            seekSpeeds = getFastForwardSpeeds();
            while (index < seekSpeeds.length) {
                if (speed == seekSpeeds[index]) {
                    return 10 + index;
                }
                index++;
            }
        } else {
            seekSpeeds = getRewindSpeeds();
            while (index < seekSpeeds.length) {
                if ((-speed) == seekSpeeds[index]) {
                    return -10 - index;
                }
                index++;
            }
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Couldn't find index for speed ");
        stringBuilder.append(speed);
        Log.w(str, stringBuilder.toString());
        return -1;
    }

    public CharSequence getMediaTitle() {
        return this.mMediaController.getMetadata().getDescription().getTitle();
    }

    public CharSequence getMediaSubtitle() {
        return this.mMediaController.getMetadata().getDescription().getSubtitle();
    }

    public int getMediaDuration() {
        return (int) this.mMediaController.getMetadata().getLong("android.media.metadata.DURATION");
    }

    public int getCurrentPosition() {
        return (int) this.mMediaController.getPlaybackState().getPosition();
    }

    public Drawable getMediaArt() {
        Bitmap bitmap = this.mMediaController.getMetadata().getDescription().getIconBitmap();
        return bitmap == null ? null : new BitmapDrawable(getContext().getResources(), bitmap);
    }

    public long getSupportedActions() {
        long result = 0;
        long actions = this.mMediaController.getPlaybackState().getActions();
        if ((512 & actions) != 0) {
            result = 0 | 64;
        }
        if ((actions & 32) != 0) {
            result |= 256;
        }
        if ((actions & 16) != 0) {
            result |= 16;
        }
        if ((64 & actions) != 0) {
            result |= 128;
        }
        if ((8 & actions) != 0) {
            return result | 32;
        }
        return result;
    }

    public void play(int speed) {
        if (speed == 1) {
            this.mMediaController.getTransportControls().play();
        } else if (speed > 0) {
            this.mMediaController.getTransportControls().fastForward();
        } else {
            this.mMediaController.getTransportControls().rewind();
        }
    }

    public void pause() {
        this.mMediaController.getTransportControls().pause();
    }

    public void next() {
        this.mMediaController.getTransportControls().skipToNext();
    }

    public void previous() {
        this.mMediaController.getTransportControls().skipToPrevious();
    }
}
