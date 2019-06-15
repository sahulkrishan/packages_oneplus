package android.support.v17.leanback.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.media.PlaybackGlue;
import android.support.v17.leanback.media.PlaybackGlue.PlayerCallback;
import android.support.v17.leanback.widget.DetailsParallax;
import android.support.v17.leanback.widget.Parallax.IntProperty;
import android.support.v17.leanback.widget.ParallaxEffect;
import android.support.v17.leanback.widget.ParallaxTarget;

final class DetailsBackgroundVideoHelper {
    private static final long BACKGROUND_CROSS_FADE_DURATION = 500;
    private static final long CROSSFADE_DELAY = 1000;
    static final int INITIAL = 0;
    static final int NO_VIDEO = 2;
    static final int PLAY_VIDEO = 1;
    private ValueAnimator mBackgroundAnimator;
    private Drawable mBackgroundDrawable;
    private boolean mBackgroundDrawableVisible;
    PlaybackControlStateCallback mControlStateCallback = new PlaybackControlStateCallback(this, null);
    private int mCurrentState = 0;
    private final DetailsParallax mDetailsParallax;
    private ParallaxEffect mParallaxEffect;
    private PlaybackGlue mPlaybackGlue;

    private class PlaybackControlStateCallback extends PlayerCallback {
        private PlaybackControlStateCallback() {
        }

        /* synthetic */ PlaybackControlStateCallback(DetailsBackgroundVideoHelper x0, AnonymousClass1 x1) {
            this();
        }

        public void onPreparedStateChanged(PlaybackGlue glue) {
            if (glue.isPrepared()) {
                DetailsBackgroundVideoHelper.this.internalStartPlayback();
            }
        }
    }

    DetailsBackgroundVideoHelper(PlaybackGlue playbackGlue, DetailsParallax detailsParallax, Drawable backgroundDrawable) {
        this.mPlaybackGlue = playbackGlue;
        this.mDetailsParallax = detailsParallax;
        this.mBackgroundDrawable = backgroundDrawable;
        this.mBackgroundDrawableVisible = true;
        this.mBackgroundDrawable.setAlpha(255);
        startParallax();
    }

    /* Access modifiers changed, original: 0000 */
    public void startParallax() {
        if (this.mParallaxEffect == null) {
            IntProperty frameTop = this.mDetailsParallax.getOverviewRowTop();
            this.mParallaxEffect = this.mDetailsParallax.addEffect(frameTop.atFraction(1.0f), frameTop.atFraction(0.0f)).target(new ParallaxTarget() {
                public void update(float fraction) {
                    if (fraction == 1.0f) {
                        DetailsBackgroundVideoHelper.this.updateState(2);
                    } else {
                        DetailsBackgroundVideoHelper.this.updateState(1);
                    }
                }
            });
            this.mDetailsParallax.updateValues();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void stopParallax() {
        this.mDetailsParallax.removeEffect(this.mParallaxEffect);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isVideoVisible() {
        return this.mCurrentState == 1;
    }

    private void updateState(int state) {
        if (state != this.mCurrentState) {
            this.mCurrentState = state;
            applyState();
        }
    }

    private void applyState() {
        switch (this.mCurrentState) {
            case 1:
                if (this.mPlaybackGlue == null) {
                    crossFadeBackgroundToVideo(false);
                    return;
                } else if (this.mPlaybackGlue.isPrepared()) {
                    internalStartPlayback();
                    return;
                } else {
                    this.mPlaybackGlue.addPlayerCallback(this.mControlStateCallback);
                    return;
                }
            case 2:
                crossFadeBackgroundToVideo(false);
                if (this.mPlaybackGlue != null) {
                    this.mPlaybackGlue.removePlayerCallback(this.mControlStateCallback);
                    this.mPlaybackGlue.pause();
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setPlaybackGlue(PlaybackGlue playbackGlue) {
        if (this.mPlaybackGlue != null) {
            this.mPlaybackGlue.removePlayerCallback(this.mControlStateCallback);
        }
        this.mPlaybackGlue = playbackGlue;
        applyState();
    }

    private void internalStartPlayback() {
        if (this.mPlaybackGlue != null) {
            this.mPlaybackGlue.play();
        }
        this.mDetailsParallax.getRecyclerView().postDelayed(new Runnable() {
            public void run() {
                DetailsBackgroundVideoHelper.this.crossFadeBackgroundToVideo(true);
            }
        }, 1000);
    }

    /* Access modifiers changed, original: 0000 */
    public void crossFadeBackgroundToVideo(boolean crossFadeToVideo) {
        crossFadeBackgroundToVideo(crossFadeToVideo, false);
    }

    /* Access modifiers changed, original: 0000 */
    public void crossFadeBackgroundToVideo(boolean crossFadeToVideo, boolean immediate) {
        boolean newVisible = crossFadeToVideo ^ 1;
        int i = 255;
        if (this.mBackgroundDrawableVisible == newVisible) {
            if (immediate) {
                if (this.mBackgroundAnimator != null) {
                    this.mBackgroundAnimator.cancel();
                    this.mBackgroundAnimator = null;
                }
                if (this.mBackgroundDrawable != null) {
                    Drawable drawable = this.mBackgroundDrawable;
                    if (crossFadeToVideo) {
                        i = 0;
                    }
                    drawable.setAlpha(i);
                    return;
                }
            }
            return;
        }
        this.mBackgroundDrawableVisible = newVisible;
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
            this.mBackgroundAnimator = null;
        }
        float endAlpha = 0.0f;
        float startAlpha = crossFadeToVideo ? 1.0f : 0.0f;
        if (!crossFadeToVideo) {
            endAlpha = 1.0f;
        }
        if (this.mBackgroundDrawable != null) {
            if (immediate) {
                Drawable drawable2 = this.mBackgroundDrawable;
                if (crossFadeToVideo) {
                    i = 0;
                }
                drawable2.setAlpha(i);
                return;
            }
            this.mBackgroundAnimator = ValueAnimator.ofFloat(new float[]{startAlpha, endAlpha});
            this.mBackgroundAnimator.setDuration(BACKGROUND_CROSS_FADE_DURATION);
            this.mBackgroundAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    DetailsBackgroundVideoHelper.this.mBackgroundDrawable.setAlpha((int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * 255.0f));
                }
            });
            this.mBackgroundAnimator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    DetailsBackgroundVideoHelper.this.mBackgroundAnimator = null;
                }

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }
            });
            this.mBackgroundAnimator.start();
        }
    }
}
