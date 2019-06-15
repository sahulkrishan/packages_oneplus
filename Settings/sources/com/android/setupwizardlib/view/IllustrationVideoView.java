package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Animatable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View.MeasureSpec;
import com.android.setupwizardlib.R;
import com.google.common.primitives.Ints;

@TargetApi(14)
public class IllustrationVideoView extends TextureView implements Animatable, SurfaceTextureListener, OnPreparedListener, OnSeekCompleteListener, OnInfoListener {
    private static final String TAG = "IllustrationVideoView";
    protected float mAspectRatio = 1.0f;
    @Nullable
    protected MediaPlayer mMediaPlayer;
    @VisibleForTesting
    Surface mSurface;
    @RawRes
    private int mVideoResId = 0;

    public IllustrationVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwIllustrationVideoView);
        this.mVideoResId = a.getResourceId(R.styleable.SuwIllustrationVideoView_suwVideo, 0);
        a.recycle();
        setScaleX(0.9999999f);
        setScaleX(0.9999999f);
        setSurfaceTextureListener(this);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (((float) height) < ((float) width) * this.mAspectRatio) {
            width = (int) (((float) height) / this.mAspectRatio);
        } else {
            height = (int) (((float) width) * this.mAspectRatio);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, Ints.MAX_POWER_OF_TWO), MeasureSpec.makeMeasureSpec(height, Ints.MAX_POWER_OF_TWO));
    }

    public void setVideoResource(@RawRes int resId) {
        if (resId != this.mVideoResId) {
            this.mVideoResId = resId;
            createMediaPlayer();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            start();
        } else {
            stop();
        }
    }

    private void createMediaPlayer() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.release();
        }
        if (this.mSurface != null && this.mVideoResId != 0) {
            this.mMediaPlayer = MediaPlayer.create(getContext(), this.mVideoResId);
            if (this.mMediaPlayer != null) {
                this.mMediaPlayer.setSurface(this.mSurface);
                this.mMediaPlayer.setOnPreparedListener(this);
                this.mMediaPlayer.setOnSeekCompleteListener(this);
                this.mMediaPlayer.setOnInfoListener(this);
                float aspectRatio = ((float) this.mMediaPlayer.getVideoHeight()) / ((float) this.mMediaPlayer.getVideoWidth());
                if (this.mAspectRatio != aspectRatio) {
                    this.mAspectRatio = aspectRatio;
                    requestLayout();
                }
            } else {
                Log.wtf(TAG, "Unable to initialize media player for video view");
            }
            if (getWindowVisibility() == 0) {
                start();
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldLoop() {
        return true;
    }

    public void release() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
        if (this.mSurface != null) {
            this.mSurface.release();
            this.mSurface = null;
        }
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        setVisibility(4);
        this.mSurface = new Surface(surfaceTexture);
        createMediaPlayer();
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        release();
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    public void start() {
        if (this.mMediaPlayer != null && !this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.start();
        }
    }

    public void stop() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.pause();
        }
    }

    public boolean isRunning() {
        return this.mMediaPlayer != null && this.mMediaPlayer.isPlaying();
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == 3) {
            setVisibility(0);
        }
        return false;
    }

    public void onPrepared(MediaPlayer mp) {
        mp.setLooping(shouldLoop());
    }

    public void onSeekComplete(MediaPlayer mp) {
        mp.start();
    }

    public int getCurrentPosition() {
        return this.mMediaPlayer == null ? 0 : this.mMediaPlayer.getCurrentPosition();
    }
}
