package android.support.v17.leanback.app;

import android.animation.PropertyValuesHolder;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.R;
import android.support.v17.leanback.graphics.FitWidthBitmapDrawable;
import android.support.v17.leanback.media.PlaybackGlue;
import android.support.v17.leanback.media.PlaybackGlueHost;
import android.support.v17.leanback.widget.DetailsParallaxDrawable;
import android.support.v17.leanback.widget.ParallaxTarget.PropertyValuesHolderTarget;
import android.support.v4.app.Fragment;

public class DetailsSupportFragmentBackgroundController {
    boolean mCanUseHost = false;
    Bitmap mCoverBitmap;
    final DetailsSupportFragment mFragment;
    boolean mInitialControlVisible = false;
    private Fragment mLastVideoSupportFragmentForGlueHost;
    DetailsParallaxDrawable mParallaxDrawable;
    int mParallaxDrawableMaxOffset;
    PlaybackGlue mPlaybackGlue;
    int mSolidColor;
    DetailsBackgroundVideoHelper mVideoHelper;

    public DetailsSupportFragmentBackgroundController(DetailsSupportFragment fragment) {
        if (fragment.mDetailsBackgroundController == null) {
            fragment.mDetailsBackgroundController = this;
            this.mFragment = fragment;
            return;
        }
        throw new IllegalStateException("Each DetailsSupportFragment is allowed to initialize DetailsSupportFragmentBackgroundController once");
    }

    public void enableParallax() {
        int offset = this.mParallaxDrawableMaxOffset;
        if (offset == 0) {
            offset = this.mFragment.getContext().getResources().getDimensionPixelSize(R.dimen.lb_details_cover_drawable_parallax_movement);
        }
        Drawable coverDrawable = new FitWidthBitmapDrawable();
        enableParallax(coverDrawable, new ColorDrawable(), new PropertyValuesHolderTarget(coverDrawable, PropertyValuesHolder.ofInt(FitWidthBitmapDrawable.PROPERTY_VERTICAL_OFFSET, new int[]{0, -offset})));
    }

    public void enableParallax(@NonNull Drawable coverDrawable, @NonNull Drawable bottomDrawable, @Nullable PropertyValuesHolderTarget coverDrawableParallaxTarget) {
        if (this.mParallaxDrawable == null) {
            if (this.mCoverBitmap != null && (coverDrawable instanceof FitWidthBitmapDrawable)) {
                ((FitWidthBitmapDrawable) coverDrawable).setBitmap(this.mCoverBitmap);
            }
            if (this.mSolidColor != 0 && (bottomDrawable instanceof ColorDrawable)) {
                ((ColorDrawable) bottomDrawable).setColor(this.mSolidColor);
            }
            if (this.mPlaybackGlue == null) {
                this.mParallaxDrawable = new DetailsParallaxDrawable(this.mFragment.getContext(), this.mFragment.getParallax(), coverDrawable, bottomDrawable, coverDrawableParallaxTarget);
                this.mFragment.setBackgroundDrawable(this.mParallaxDrawable);
                this.mVideoHelper = new DetailsBackgroundVideoHelper(null, this.mFragment.getParallax(), this.mParallaxDrawable.getCoverDrawable());
                return;
            }
            throw new IllegalStateException("enableParallaxDrawable must be called before enableVideoPlayback");
        }
    }

    public void setupVideoPlayback(@NonNull PlaybackGlue playbackGlue) {
        if (this.mPlaybackGlue != playbackGlue) {
            PlaybackGlueHost playbackGlueHost = null;
            if (this.mPlaybackGlue != null) {
                playbackGlueHost = this.mPlaybackGlue.getHost();
                this.mPlaybackGlue.setHost(null);
            }
            this.mPlaybackGlue = playbackGlue;
            this.mVideoHelper.setPlaybackGlue(this.mPlaybackGlue);
            if (this.mCanUseHost && this.mPlaybackGlue != null) {
                if (playbackGlueHost == null || this.mLastVideoSupportFragmentForGlueHost != findOrCreateVideoSupportFragment()) {
                    this.mPlaybackGlue.setHost(createGlueHost());
                    this.mLastVideoSupportFragmentForGlueHost = findOrCreateVideoSupportFragment();
                } else {
                    this.mPlaybackGlue.setHost(playbackGlueHost);
                }
            }
        }
    }

    public final PlaybackGlue getPlaybackGlue() {
        return this.mPlaybackGlue;
    }

    public boolean canNavigateToVideoSupportFragment() {
        return this.mPlaybackGlue != null;
    }

    /* Access modifiers changed, original: 0000 */
    public void switchToVideoBeforeCreate() {
        this.mVideoHelper.crossFadeBackgroundToVideo(true, true);
        this.mInitialControlVisible = true;
    }

    public final void switchToVideo() {
        this.mFragment.switchToVideo();
    }

    public final void switchToRows() {
        this.mFragment.switchToRows();
    }

    /* Access modifiers changed, original: 0000 */
    public void onStart() {
        if (!this.mCanUseHost) {
            this.mCanUseHost = true;
            if (this.mPlaybackGlue != null) {
                this.mPlaybackGlue.setHost(createGlueHost());
                this.mLastVideoSupportFragmentForGlueHost = findOrCreateVideoSupportFragment();
            }
        }
        if (this.mPlaybackGlue != null && this.mPlaybackGlue.isPrepared()) {
            this.mPlaybackGlue.play();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void onStop() {
        if (this.mPlaybackGlue != null) {
            this.mPlaybackGlue.pause();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean disableVideoParallax() {
        if (this.mVideoHelper == null) {
            return false;
        }
        this.mVideoHelper.stopParallax();
        return this.mVideoHelper.isVideoVisible();
    }

    public final Drawable getCoverDrawable() {
        if (this.mParallaxDrawable == null) {
            return null;
        }
        return this.mParallaxDrawable.getCoverDrawable();
    }

    public final Drawable getBottomDrawable() {
        if (this.mParallaxDrawable == null) {
            return null;
        }
        return this.mParallaxDrawable.getBottomDrawable();
    }

    public Fragment onCreateVideoSupportFragment() {
        return new VideoSupportFragment();
    }

    public PlaybackGlueHost onCreateGlueHost() {
        return new VideoSupportFragmentGlueHost((VideoSupportFragment) findOrCreateVideoSupportFragment());
    }

    /* Access modifiers changed, original: 0000 */
    public PlaybackGlueHost createGlueHost() {
        PlaybackGlueHost host = onCreateGlueHost();
        if (this.mInitialControlVisible) {
            host.showControlsOverlay(false);
        } else {
            host.hideControlsOverlay(false);
        }
        return host;
    }

    public final Fragment findOrCreateVideoSupportFragment() {
        return this.mFragment.findOrCreateVideoSupportFragment();
    }

    public final void setCoverBitmap(Bitmap bitmap) {
        this.mCoverBitmap = bitmap;
        Drawable drawable = getCoverDrawable();
        if (drawable instanceof FitWidthBitmapDrawable) {
            ((FitWidthBitmapDrawable) drawable).setBitmap(this.mCoverBitmap);
        }
    }

    public final Bitmap getCoverBitmap() {
        return this.mCoverBitmap;
    }

    @ColorInt
    public final int getSolidColor() {
        return this.mSolidColor;
    }

    public final void setSolidColor(@ColorInt int color) {
        this.mSolidColor = color;
        Drawable bottomDrawable = getBottomDrawable();
        if (bottomDrawable instanceof ColorDrawable) {
            ((ColorDrawable) bottomDrawable).setColor(color);
        }
    }

    public final void setParallaxDrawableMaxOffset(int offset) {
        if (this.mParallaxDrawable == null) {
            this.mParallaxDrawableMaxOffset = offset;
            return;
        }
        throw new IllegalStateException("enableParallax already called");
    }

    public final int getParallaxDrawableMaxOffset() {
        return this.mParallaxDrawableMaxOffset;
    }
}
