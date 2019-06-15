package com.oneplus.settings.better;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.widget.AspectRatioFrameLayout;
import com.android.settingslib.SliceBroadcastRelay;
import com.oneplus.settings.BaseActivity;
import java.util.ArrayList;

public class OPFnaticWallPapers extends BaseActivity {
    public static final int ACTIVITED_MODE = 1;
    public static final int DEFAULT_MODE = 0;
    public static final String FNATIC_MODE_EGG_ACTIVATED = "fnatic_mode_egg_activated";
    public static final String TAG = "OPFnaticWallPapers";
    public static final int TAPS_TO_ACTIVE_HIDDEN_WALLPAPERS = 5;
    private int endColor = ViewCompat.MEASURED_STATE_MASK;
    private ImageView imageView;
    private AspectRatioFrameLayout layout;
    boolean mAnimationAvailable;
    private float mAspectRadio = 1.0f;
    private TextView mHeadView;
    private MediaPlayer mMediaPlayer;
    private Button mNextButton;
    PagerAdapter mPagerAdapter = new PagerAdapter() {
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        public int getCount() {
            return OPFnaticWallPapers.this.mWallPaperViews.size();
        }

        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView((View) OPFnaticWallPapers.this.mWallPaperViews.get(position));
        }

        public Object instantiateItem(View container, int position) {
            ((ViewPager) container).addView((View) OPFnaticWallPapers.this.mWallPaperViews.get(position));
            return OPFnaticWallPapers.this.mWallPaperViews.get(position);
        }
    };
    private View mVideoLayout;
    private Uri mVideoPath;
    private boolean mVideoPaused;
    private boolean mVideoReady;
    private ViewPager mViewPager;
    private View mWallPaperLayout;
    private ArrayList<View> mWallPaperViews = new ArrayList();
    private int[] mWallPapers = new int[]{R.drawable.fnatic_mode_wallpaper_01, R.drawable.fnatic_mode_wallpaper_02, R.drawable.fnatic_mode_wallpaper_03};
    private int startColor = 0;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.op_fnatic_wallpapers_activity);
        getWindow().getDecorView().setSystemUiVisibility(0);
        getActionBar().setTitle("");
        initViews();
        initMediaPlayer();
    }

    private void initViews() {
        this.mWallPaperLayout = findViewById(R.id.fnatic_wallpaper_layout);
        this.mHeadView = (TextView) findViewById(R.id.head_title);
        this.mHeadView.setText(getString(R.string.oneplus_fnatic_mode_unlock_wallpapers, new Object[]{String.valueOf(this.mWallPapers.length)}));
        this.mNextButton = (Button) findViewById(R.id.next_button);
        this.mNextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (OPFnaticWallPapers.this.getCurrentState() == 0) {
                    OPFnaticWallPapers.this.activeFnaticWallPapers();
                    Toast.makeText(OPFnaticWallPapers.this, OPFnaticWallPapers.this.getString(R.string.oneplus_fnatic_mode_wallpapers_added, new Object[]{String.valueOf(OPFnaticWallPapers.this.mWallPapers.length)}), 1).show();
                    OPFnaticWallPapers.this.updateNextButtonState();
                    return;
                }
                OPFnaticWallPapers.this.gotoWallPaperPickerActivity();
            }
        });
        this.mViewPager = (ViewPager) findViewById(R.id.fnatic_wallpapers_viewpager);
        initWallPapers();
    }

    public void onResume() {
        super.onResume();
        if (isInMultiWindowMode()) {
            try {
                Context systemUIContext = createPackageContext(SliceBroadcastRelay.SYSTEMUI_PACKAGE, 0);
                Toast.makeText(this, systemUIContext.getResources().getString(systemUIContext.getResources().getIdentifier("dock_forced_resizable", "string", SliceBroadcastRelay.SYSTEMUI_PACKAGE)), 0).show();
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setVideoResume();
        updateNextButtonState();
    }

    private void gotoWallPaperPickerActivity() {
        Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
        intent.setPackage("net.oneplus.launcher");
        intent.putExtra("from_easter_egg_page", true);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void activeFnaticWallPapers() {
        System.putStringForUser(getContentResolver(), FNATIC_MODE_EGG_ACTIVATED, "1", -2);
    }

    private int getCurrentState() {
        return System.getIntForUser(getContentResolver(), FNATIC_MODE_EGG_ACTIVATED, 0, -2);
    }

    private void updateNextButtonState() {
        if (getCurrentState() == 1) {
            this.mNextButton.setText(R.string.oneplus_fnatic_mode_view_in_wallpapers);
        } else {
            this.mNextButton.setText(getString(R.string.oneplus_fnatic_mode_add_to_wallpapers, new Object[]{String.valueOf(this.mWallPapers.length)}));
        }
    }

    public void onPause() {
        super.onPause();
        setVideoPaused();
    }

    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void runAlphaAnim() {
        ObjectAnimator alpahAnimator = ObjectAnimator.ofInt(this.mWallPaperLayout, "backgroundColor", new int[]{this.startColor, this.endColor});
        alpahAnimator.setDuration(300);
        alpahAnimator.setEvaluator(new ArgbEvaluator());
        alpahAnimator.start();
    }

    private void initWallPapers() {
        LayoutInflater mLi = LayoutInflater.from(this);
        this.mWallPaperViews.clear();
        for (int backgroundResource : this.mWallPapers) {
            View itemView = mLi.inflate(R.layout.op_fnatic_wallpaper_item_layout, null);
            ((ImageView) itemView.findViewById(R.id.fnatic_wallpaper_image_view)).setBackgroundResource(backgroundResource);
            this.mWallPaperViews.add(itemView);
        }
    }

    private void initMediaPlayer() {
        this.mVideoLayout = findViewById(R.id.fnatic_video_view);
        AlphaAnimation anima = new AlphaAnimation(0.0f, 1.0f);
        anima.setDuration(1000);
        this.mVideoLayout.startAnimation(anima);
        try {
            this.mVideoPath = new Builder().scheme("android.resource").authority(getPackageName()).appendPath(String.valueOf(R.raw.fnatic_mode_video_1440_1440)).build();
            this.mMediaPlayer = MediaPlayer.create(this, this.mVideoPath);
            if (this.mMediaPlayer == null || this.mMediaPlayer.getDuration() <= 0) {
                setVisible(false);
            } else {
                this.mMediaPlayer.setOnSeekCompleteListener(new -$$Lambda$OPFnaticWallPapers$addNcANW3El0e9XKqAj5iPLWAoU(this));
                this.mMediaPlayer.setOnPreparedListener(-$$Lambda$OPFnaticWallPapers$ceFLfLCph-KLYewvMbZCy7tA5p0.INSTANCE);
                this.mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        OPFnaticWallPapers.this.mVideoLayout.setVisibility(8);
                        AlphaAnimation anima = new AlphaAnimation(0.0f, 1.0f);
                        anima.setDuration(300);
                        OPFnaticWallPapers.this.mWallPaperLayout.startAnimation(anima);
                        OPFnaticWallPapers.this.mWallPaperLayout.setVisibility(0);
                    }
                });
                this.mAnimationAvailable = true;
                updateAspectRatio();
            }
        } catch (Exception e) {
            Log.w(TAG, "Animation resource not found. Will not show animation.");
        }
        TextureView video = (TextureView) findViewById(R.id.video_texture_view);
        this.imageView = (ImageView) findViewById(R.id.video_preview_image);
        this.layout = (AspectRatioFrameLayout) findViewById(R.id.video_container);
        this.layout.setAspectRatio(this.mAspectRadio);
        video.setSurfaceTextureListener(new SurfaceTextureListener() {
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                if (OPFnaticWallPapers.this.mMediaPlayer != null) {
                    OPFnaticWallPapers.this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
                    OPFnaticWallPapers.this.mVideoReady = false;
                    OPFnaticWallPapers.this.mMediaPlayer.seekTo(0);
                }
            }

            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                OPFnaticWallPapers.this.imageView.setVisibility(0);
                return false;
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                if (OPFnaticWallPapers.this.mVideoReady && OPFnaticWallPapers.this.imageView.getVisibility() == 0) {
                    OPFnaticWallPapers.this.imageView.setVisibility(8);
                }
            }
        });
    }

    public void setVideoResume() {
        if (!((this.mWallPaperLayout != null && this.mWallPaperLayout.getVisibility() == 0) || this.mVideoPaused || this.mMediaPlayer == null || this.mMediaPlayer.isPlaying())) {
            this.mMediaPlayer.start();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateAspectRatio() {
        this.mAspectRadio = ((float) this.mMediaPlayer.getVideoWidth()) / ((float) this.mMediaPlayer.getVideoHeight());
    }

    public void releaseMediaPlayer() {
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
        }
        if (this.layout != null) {
            this.layout.setAspectRatio(this.mAspectRadio);
        }
    }
}
