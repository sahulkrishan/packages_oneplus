package com.android.settings.widget;

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
import android.view.View;
import android.widget.ImageView;
import com.android.settings.R;
import com.oneplus.settings.utils.OPUtils;

public class VideoPreference extends Preference {
    private static final String TAG = "VideoPreference";
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

    public VideoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VideoPreference, 0, 0);
        try {
            int animation = attributes.getResourceId(0, 0);
            if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
                animation = R.raw.auto_awesome_battery_dark;
            } else {
                animation = R.raw.auto_awesome_battery;
            }
            this.mVideoPath = new Builder().scheme("android.resource").authority(context.getPackageName()).appendPath(String.valueOf(animation)).build();
            this.mMediaPlayer = MediaPlayer.create(this.mContext, this.mVideoPath);
            if (this.mMediaPlayer == null || this.mMediaPlayer.getDuration() <= 0) {
                setVisible(false);
            } else {
                setVisible(true);
                setLayoutResource(R.layout.video_preference);
                this.mPreviewResource = attributes.getResourceId(1, 0);
                this.mMediaPlayer.setOnSeekCompleteListener(new -$$Lambda$VideoPreference$dH8H9UsxsQzXI7GaCcZWWDvTxoU(this));
                this.mMediaPlayer.setOnPreparedListener(-$$Lambda$VideoPreference$2crRm1Sj4_bqGlDPLY9cVIbC7CU.INSTANCE);
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
            final ImageView imageView = (ImageView) holder.findViewById(R.id.video_preview_image);
            final ImageView playButton = (ImageView) holder.findViewById(R.id.video_play_button);
            AspectRatioFrameLayout layout = (AspectRatioFrameLayout) holder.findViewById(R.id.video_container);
            imageView.setImageResource(this.mPreviewResource);
            layout.setAspectRatio(this.mAspectRadio);
            video.setOnClickListener(new -$$Lambda$VideoPreference$n3lVCTPDzJxvnNXXv__BWcO0YKM(this, playButton));
            video.setSurfaceTextureListener(new SurfaceTextureListener() {
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    if (VideoPreference.this.mMediaPlayer != null) {
                        VideoPreference.this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
                        VideoPreference.this.mVideoReady = false;
                        VideoPreference.this.mMediaPlayer.seekTo(0);
                    }
                }

                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                }

                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    imageView.setVisibility(0);
                    return false;
                }

                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                    if (VideoPreference.this.mVideoReady) {
                        if (imageView.getVisibility() == 0) {
                            imageView.setVisibility(8);
                        }
                        if (!(VideoPreference.this.mVideoPaused || VideoPreference.this.mMediaPlayer == null || VideoPreference.this.mMediaPlayer.isPlaying())) {
                            VideoPreference.this.mMediaPlayer.start();
                            playButton.setVisibility(8);
                        }
                    }
                    if (VideoPreference.this.mMediaPlayer != null && !VideoPreference.this.mMediaPlayer.isPlaying() && playButton.getVisibility() != 0) {
                        playButton.setVisibility(0);
                    }
                }
            });
        }
    }

    public static /* synthetic */ void lambda$onBindViewHolder$2(VideoPreference videoPreference, ImageView playButton, View v) {
        if (videoPreference.mMediaPlayer == null) {
            return;
        }
        if (videoPreference.mMediaPlayer.isPlaying()) {
            videoPreference.mMediaPlayer.pause();
            playButton.setVisibility(0);
            videoPreference.mVideoPaused = true;
            return;
        }
        videoPreference.mMediaPlayer.start();
        playButton.setVisibility(8);
        videoPreference.mVideoPaused = false;
    }

    public void onDetached() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.reset();
            this.mMediaPlayer.release();
        }
        super.onDetached();
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
}
