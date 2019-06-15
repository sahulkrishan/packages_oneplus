package com.android.settings.fingerprint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View.MeasureSpec;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public class FingerprintLocationAnimationVideoView extends TextureView implements FingerprintFindSensorAnimation {
    protected float mAspect = 1.0f;
    protected MediaPlayer mMediaPlayer;

    public FingerprintLocationAnimationVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.round(this.mAspect * ((float) MeasureSpec.getSize(widthMeasureSpec))), Ints.MAX_POWER_OF_TWO));
    }

    /* Access modifiers changed, original: protected */
    public Uri getFingerprintLocationAnimation() {
        return resourceEntryToUri(getContext(), R.raw.fingerprint_location_animation);
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setSurfaceTextureListener(new SurfaceTextureListener() {
            private SurfaceTexture mTextureToDestroy = null;

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                FingerprintLocationAnimationVideoView.this.setVisibility(4);
                Uri videoUri = FingerprintLocationAnimationVideoView.this.getFingerprintLocationAnimation();
                if (FingerprintLocationAnimationVideoView.this.mMediaPlayer != null) {
                    FingerprintLocationAnimationVideoView.this.mMediaPlayer.release();
                }
                if (this.mTextureToDestroy != null) {
                    this.mTextureToDestroy.release();
                    this.mTextureToDestroy = null;
                }
                FingerprintLocationAnimationVideoView.this.mMediaPlayer = FingerprintLocationAnimationVideoView.this.createMediaPlayer(FingerprintLocationAnimationVideoView.this.mContext, videoUri);
                if (FingerprintLocationAnimationVideoView.this.mMediaPlayer != null) {
                    FingerprintLocationAnimationVideoView.this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
                    FingerprintLocationAnimationVideoView.this.mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.setLooping(true);
                        }
                    });
                    FingerprintLocationAnimationVideoView.this.mMediaPlayer.setOnInfoListener(new OnInfoListener() {
                        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                            if (what == 3) {
                                FingerprintLocationAnimationVideoView.this.setVisibility(0);
                            }
                            return false;
                        }
                    });
                    FingerprintLocationAnimationVideoView.this.mAspect = ((float) FingerprintLocationAnimationVideoView.this.mMediaPlayer.getVideoHeight()) / ((float) FingerprintLocationAnimationVideoView.this.mMediaPlayer.getVideoWidth());
                    FingerprintLocationAnimationVideoView.this.requestLayout();
                    FingerprintLocationAnimationVideoView.this.startAnimation();
                }
            }

            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                this.mTextureToDestroy = surfaceTexture;
                return false;
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        });
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public MediaPlayer createMediaPlayer(Context context, Uri videoUri) {
        return MediaPlayer.create(this.mContext, videoUri);
    }

    protected static Uri resourceEntryToUri(Context context, int id) {
        Resources res = context.getResources();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("android.resource://");
        stringBuilder.append(res.getResourcePackageName(id));
        stringBuilder.append('/');
        stringBuilder.append(res.getResourceTypeName(id));
        stringBuilder.append('/');
        stringBuilder.append(res.getResourceEntryName(id));
        return Uri.parse(stringBuilder.toString());
    }

    public void startAnimation() {
        if (this.mMediaPlayer != null && !this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.start();
        }
    }

    public void stopAnimation() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
    }

    public void pauseAnimation() {
        if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
        }
    }
}
