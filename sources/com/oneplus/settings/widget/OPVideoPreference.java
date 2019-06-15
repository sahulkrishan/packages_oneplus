package com.oneplus.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.Uri.Builder;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.ImageView;
import com.android.settings.R;
import com.android.settings.widget.AspectRatioFrameLayout;

public class OPVideoPreference extends Preference {
    private static final String TAG = "OPVideoPreference";
    private ImageView imageView;
    private AspectRatioFrameLayout layout;
    @VisibleForTesting
    boolean mAnimationAvailable;
    private float mAspectRadio = 1.0f;
    private final Context mContext;
    @VisibleForTesting
    MediaPlayer mMediaPlayer;
    private int mPreviewResource;
    private Uri mVideoPath;
    private boolean mVideoPaused;
    private boolean mVideoReady;
    private int pos = 0;

    public OPVideoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VideoPreference, 0, 0);
        try {
            this.mVideoPath = new Builder().scheme("android.resource").authority(context.getPackageName()).appendPath(String.valueOf(attributes.getResourceId(0, 0))).build();
            this.mMediaPlayer = MediaPlayer.create(this.mContext, this.mVideoPath);
            if (this.mMediaPlayer == null || this.mMediaPlayer.getDuration() <= 0) {
                setVisible(false);
            } else {
                setVisible(true);
                setLayoutResource(R.layout.op_video_preference);
                this.mPreviewResource = attributes.getResourceId(1, 0);
                this.mMediaPlayer.setOnSeekCompleteListener(new -$$Lambda$OPVideoPreference$ynCil1Vg3ClpXktrurvZlqx29d4(this));
                this.mMediaPlayer.setOnPreparedListener(-$$Lambda$OPVideoPreference$uc3qMTOa2JodIJYV5EhB8pjdWdg.INSTANCE);
                this.mAnimationAvailable = true;
                updateAspectRatio();
            }
        } catch (Exception e) {
            Log.w(TAG, "Animation resource not found. Will not show animation.");
        } catch (Throwable th) {
            attributes.recycle();
        }
        attributes.recycle();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (this.mAnimationAvailable) {
            TextureView video = (TextureView) holder.findViewById(R.id.video_texture_view);
            this.imageView = (ImageView) holder.findViewById(R.id.video_preview_image);
            this.layout = (AspectRatioFrameLayout) holder.findViewById(R.id.video_container);
            this.imageView.setImageResource(this.mPreviewResource);
            this.layout.setAspectRatio(this.mAspectRadio);
            this.mMediaPlayer.start();
            video.setSurfaceTextureListener(new SurfaceTextureListener() {
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    if (OPVideoPreference.this.mMediaPlayer != null) {
                        OPVideoPreference.this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
                        OPVideoPreference.this.mVideoReady = false;
                        OPVideoPreference.this.mMediaPlayer.seekTo(0);
                    }
                }

                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                }

                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    OPVideoPreference.this.imageView.setVisibility(0);
                    return false;
                }

                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                    if (OPVideoPreference.this.mVideoReady) {
                        if (OPVideoPreference.this.imageView.getVisibility() == 0) {
                            OPVideoPreference.this.imageView.setVisibility(8);
                        }
                        if (!OPVideoPreference.this.mVideoPaused && OPVideoPreference.this.mMediaPlayer != null && !OPVideoPreference.this.mMediaPlayer.isPlaying()) {
                            OPVideoPreference.this.mMediaPlayer.start();
                        }
                    }
                }
            });
        }
    }

    public void onViewVisible(boolean videoPaused) {
        this.mVideoPaused = videoPaused;
        if (this.mVideoReady && this.mMediaPlayer != null && !this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.seekTo(0);
        }
    }

    public void onViewInvisible() {
        if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
        }
    }

    public boolean isVideoPaused() {
        return this.mVideoPaused;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updateAspectRatio() {
        this.mAspectRadio = ((float) this.mMediaPlayer.getVideoWidth()) / ((float) this.mMediaPlayer.getVideoHeight());
    }

    public void setVideoResume() {
        if (!this.mVideoPaused && this.mMediaPlayer != null && !this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.start();
        }
    }

    public void release() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
    }

    public void setVideoPaused() {
        if (this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.pause();
            if (this.imageView != null && this.layout != null) {
                this.imageView.setImageResource(this.mPreviewResource);
                this.imageView.setVisibility(0);
                this.layout.setAspectRatio(this.mAspectRadio);
            }
        }
    }
}
