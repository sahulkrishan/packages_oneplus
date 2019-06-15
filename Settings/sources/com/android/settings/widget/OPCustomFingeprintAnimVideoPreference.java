package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.Settings.System;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import com.android.settings.R;
import com.oneplus.custom.utils.OpCustomizeSettings;
import com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;
import java.io.IOException;

public class OPCustomFingeprintAnimVideoPreference extends Preference implements OnClickListener {
    private static final int ANIM_STYLE_0 = 3;
    private static final int ANIM_STYLE_1 = 0;
    private static final int ANIM_STYLE_2 = 1;
    private static final int ANIM_STYLE_3 = 2;
    private static final String TAG = "VideoPreference";
    private RadioButton mAnimStyleButton_0;
    private RadioButton mAnimStyleButton_1;
    private RadioButton mAnimStyleButton_2;
    private RadioButton mAnimStyleButton_3;
    private View mAnimStyleView_0;
    private View mAnimStyle_0;
    private View mAnimStyle_1;
    private View mAnimStyle_2;
    private View mAnimStyle_3;
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

    public OPCustomFingeprintAnimVideoPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VideoPreference, 0, 0);
        try {
            int animation = attributes.getResourceId(0, 0);
            this.mVideoPath = new Builder().scheme("android.resource").authority(context.getPackageName()).appendPath(String.valueOf(getCustomAnimationId())).build();
            this.mMediaPlayer = MediaPlayer.create(this.mContext, this.mVideoPath);
            if (this.mMediaPlayer == null || this.mMediaPlayer.getDuration() <= 0) {
                setVisible(false);
                attributes.recycle();
            }
            setVisible(true);
            int layoutId = R.layout.op_custom_fingerprint_anim_choose_layout;
            if (CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType())) {
                layoutId = R.layout.op_custom_fingerprint_anim_choose_layout_mcl;
            }
            setLayoutResource(layoutId);
            this.mPreviewResource = attributes.getResourceId(1, 0);
            this.mMediaPlayer.setOnSeekCompleteListener(new -$$Lambda$OPCustomFingeprintAnimVideoPreference$xNjQuqlzU7hQu-C5vLxvtUk8_xY(this));
            this.mMediaPlayer.setOnPreparedListener(-$$Lambda$OPCustomFingeprintAnimVideoPreference$-76bRwQYPctZEhfpNbtqN2ejOb4.INSTANCE);
            this.mAnimationAvailable = true;
            updateAspectRatio();
            attributes.recycle();
        } catch (Exception e) {
            Log.w(TAG, "Animation resource not found. Will not show animation.");
        } catch (Throwable th) {
            attributes.recycle();
        }
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (this.mAnimationAvailable) {
            ((ScrollView) holder.findViewById(R.id.video_container_scrollview)).setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            View group = holder.findViewById(R.id.video_container_group);
            if (OPUtils.isBlackModeOn(this.mContext.getContentResolver())) {
                group.setBackgroundColor(Color.parseColor("#282828"));
            } else {
                group.setBackgroundColor(Color.parseColor("#f5f5f5"));
            }
            TextureView video = (TextureView) holder.findViewById(R.id.video_texture_view);
            final ImageView imageView = (ImageView) holder.findViewById(R.id.video_preview_image);
            final ImageView playButton = (ImageView) holder.findViewById(R.id.video_play_button);
            AspectRatioFrameLayout layout = (AspectRatioFrameLayout) holder.findViewById(R.id.video_container);
            this.mAnimStyleView_0 = holder.findViewById(R.id.custom_fingerprint_anim_style_0);
            this.mAnimStyleButton_0 = (RadioButton) holder.findViewById(R.id.custom_fingerprint_anim_style_0_button);
            this.mAnimStyleButton_1 = (RadioButton) holder.findViewById(R.id.custom_fingerprint_anim_style_1_button);
            this.mAnimStyleButton_2 = (RadioButton) holder.findViewById(R.id.custom_fingerprint_anim_style_2_button);
            this.mAnimStyleButton_3 = (RadioButton) holder.findViewById(R.id.custom_fingerprint_anim_style_3_button);
            this.mAnimStyle_0 = holder.findViewById(R.id.custom_fingerprint_anim_style_0);
            this.mAnimStyle_1 = holder.findViewById(R.id.custom_fingerprint_anim_style_1);
            this.mAnimStyle_2 = holder.findViewById(R.id.custom_fingerprint_anim_style_2);
            this.mAnimStyle_3 = holder.findViewById(R.id.custom_fingerprint_anim_style_3);
            if (CUSTOM_TYPE.MCL.equals(OpCustomizeSettings.getCustomType())) {
                this.mAnimStyleView_0.setVisibility(0);
            } else {
                this.mAnimStyleView_0.setVisibility(8);
            }
            this.mAnimStyle_0.setOnClickListener(this);
            this.mAnimStyle_1.setOnClickListener(this);
            this.mAnimStyle_2.setOnClickListener(this);
            this.mAnimStyle_3.setOnClickListener(this);
            setAnimSelectStatus();
            imageView.setImageResource(this.mPreviewResource);
            layout.setAspectRatio(this.mAspectRadio);
            video.setSurfaceTextureListener(new SurfaceTextureListener() {
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    if (OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer != null) {
                        OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
                        OPCustomFingeprintAnimVideoPreference.this.mVideoReady = false;
                        OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.seekTo(0);
                    }
                }

                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                }

                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    imageView.setVisibility(0);
                    return false;
                }

                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                    if (OPCustomFingeprintAnimVideoPreference.this.mVideoReady) {
                        if (imageView.getVisibility() == 0) {
                            imageView.setVisibility(8);
                        }
                        if (!(OPCustomFingeprintAnimVideoPreference.this.mVideoPaused || OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer == null || OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.isPlaying())) {
                            OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.start();
                            playButton.setVisibility(8);
                        }
                    }
                    if (OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer != null && !OPCustomFingeprintAnimVideoPreference.this.mMediaPlayer.isPlaying()) {
                        playButton.getVisibility();
                    }
                }
            });
        }
    }

    public void onClick(View v) {
        int value = System.getIntForUser(this.mContext.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, 0, -2);
        if (v.getId() == R.id.custom_fingerprint_anim_style_0) {
            if (value != 3) {
                changeAnimStyle(3);
                this.mAnimStyleButton_0.setChecked(true);
                this.mAnimStyleButton_1.setChecked(false);
                this.mAnimStyleButton_2.setChecked(false);
                this.mAnimStyleButton_3.setChecked(false);
            }
        } else if (v.getId() == R.id.custom_fingerprint_anim_style_1) {
            if (value != 0) {
                changeAnimStyle(0);
                this.mAnimStyleButton_0.setChecked(false);
                this.mAnimStyleButton_1.setChecked(true);
                this.mAnimStyleButton_2.setChecked(false);
                this.mAnimStyleButton_3.setChecked(false);
            }
        } else if (v.getId() == R.id.custom_fingerprint_anim_style_2) {
            if (value != 1) {
                changeAnimStyle(1);
                this.mAnimStyleButton_0.setChecked(false);
                this.mAnimStyleButton_1.setChecked(false);
                this.mAnimStyleButton_2.setChecked(true);
                this.mAnimStyleButton_3.setChecked(false);
            }
        } else if (v.getId() == R.id.custom_fingerprint_anim_style_3 && value != 2) {
            changeAnimStyle(2);
            this.mAnimStyleButton_0.setChecked(false);
            this.mAnimStyleButton_1.setChecked(false);
            this.mAnimStyleButton_2.setChecked(false);
            this.mAnimStyleButton_3.setChecked(true);
        }
    }

    private void setAnimSelectStatus() {
        int style = System.getIntForUser(this.mContext.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, 0, -2);
        if (style == 3) {
            this.mAnimStyleButton_0.setChecked(true);
            this.mAnimStyleButton_1.setChecked(false);
            this.mAnimStyleButton_2.setChecked(false);
            this.mAnimStyleButton_3.setChecked(false);
        } else if (style == 0) {
            this.mAnimStyleButton_0.setChecked(false);
            this.mAnimStyleButton_1.setChecked(true);
            this.mAnimStyleButton_2.setChecked(false);
            this.mAnimStyleButton_3.setChecked(false);
        } else if (style == 1) {
            this.mAnimStyleButton_0.setChecked(false);
            this.mAnimStyleButton_1.setChecked(false);
            this.mAnimStyleButton_2.setChecked(true);
            this.mAnimStyleButton_3.setChecked(false);
        } else if (style == 2) {
            this.mAnimStyleButton_0.setChecked(false);
            this.mAnimStyleButton_1.setChecked(false);
            this.mAnimStyleButton_2.setChecked(false);
            this.mAnimStyleButton_3.setChecked(true);
        }
    }

    private int getCustomAnimationId() {
        switch (System.getIntForUser(this.mContext.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, 0, -2)) {
            case 0:
                return R.raw.op_custom_fingerprint_anim_1;
            case 1:
                return R.raw.op_custom_fingerprint_anim_2;
            case 2:
                return R.raw.op_custom_fingerprint_anim_3;
            case 3:
                return R.raw.op_custom_fingerprint_anim_0;
            default:
                return R.raw.op_custom_fingerprint_anim_1;
        }
    }

    private void changeAnimStyle(int value) {
        setAnimStyle(value);
        playAnimByStyle(value);
    }

    private void setAnimStyle(int value) {
        System.putIntForUser(this.mContext.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, value, -2);
        OPUtils.sendAppTrackerForFodAnimStyle();
    }

    private void playAnimByStyle(int value) {
        if (this.mMediaPlayer == null) {
            this.mMediaPlayer = new MediaPlayer();
        }
        this.mVideoPath = new Builder().scheme("android.resource").authority(this.mContext.getPackageName()).appendPath(String.valueOf(getCustomAnimationId())).build();
        try {
            this.mMediaPlayer.reset();
            this.mMediaPlayer.setDataSource(this.mContext, this.mVideoPath);
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
