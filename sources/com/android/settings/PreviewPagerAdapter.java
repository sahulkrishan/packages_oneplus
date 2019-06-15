package com.android.settings;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import java.lang.reflect.Array;

public class PreviewPagerAdapter extends PagerAdapter {
    private static final long CROSS_FADE_DURATION_MS = 400;
    private static final Interpolator FADE_IN_INTERPOLATOR = new DecelerateInterpolator();
    private static final Interpolator FADE_OUT_INTERPOLATOR = new AccelerateInterpolator();
    private int mAnimationCounter;
    private Runnable mAnimationEndAction;
    private boolean mIsLayoutRtl;
    private FrameLayout[] mPreviewFrames;
    private boolean[][] mViewStubInflated;

    private class PreviewFrameAnimatorListener implements AnimatorListener {
        private PreviewFrameAnimatorListener() {
        }

        /* synthetic */ PreviewFrameAnimatorListener(PreviewPagerAdapter x0, AnonymousClass1 x1) {
            this();
        }

        public void onAnimationStart(Animator animation) {
            PreviewPagerAdapter.this.mAnimationCounter = PreviewPagerAdapter.this.mAnimationCounter + 1;
        }

        public void onAnimationEnd(Animator animation) {
            PreviewPagerAdapter.this.mAnimationCounter = PreviewPagerAdapter.this.mAnimationCounter - 1;
            PreviewPagerAdapter.this.runAnimationEndAction();
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public PreviewPagerAdapter(Context context, boolean isLayoutRtl, int[] previewSampleResIds, Configuration[] configurations) {
        this.mIsLayoutRtl = isLayoutRtl;
        this.mPreviewFrames = new FrameLayout[previewSampleResIds.length];
        this.mViewStubInflated = (boolean[][]) Array.newInstance(boolean.class, new int[]{previewSampleResIds.length, configurations.length});
        int i = 0;
        while (i < previewSampleResIds.length) {
            int p = this.mIsLayoutRtl ? (previewSampleResIds.length - 1) - i : i;
            this.mPreviewFrames[p] = new FrameLayout(context);
            this.mPreviewFrames[p].setLayoutParams(new LayoutParams(-1, -1));
            for (int j = 0; j < configurations.length; j++) {
                Context configContext = context.createConfigurationContext(configurations[j]);
                configContext.getTheme().setTo(context.getTheme());
                LayoutInflater configInflater = LayoutInflater.from(configContext);
                ViewStub sampleViewStub = new ViewStub(configContext);
                sampleViewStub.setLayoutResource(previewSampleResIds[i]);
                final int fi = i;
                final int fj = j;
                sampleViewStub.setOnInflateListener(new OnInflateListener() {
                    public void onInflate(ViewStub stub, View inflated) {
                        inflated.setVisibility(stub.getVisibility());
                        PreviewPagerAdapter.this.mViewStubInflated[fi][fj] = true;
                    }
                });
                this.mPreviewFrames[p].addView(sampleViewStub);
            }
            i++;
        }
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public int getCount() {
        return this.mPreviewFrames.length;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(this.mPreviewFrames[position]);
        return this.mPreviewFrames[position];
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isAnimating() {
        return this.mAnimationCounter > 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void setAnimationEndAction(Runnable action) {
        this.mAnimationEndAction = action;
    }

    /* Access modifiers changed, original: 0000 */
    public void setPreviewLayer(int newLayerIndex, int currentLayerIndex, int currentFrameIndex, boolean animate) {
        for (FrameLayout previewFrame : this.mPreviewFrames) {
            View lastLayer;
            if (currentLayerIndex >= 0) {
                lastLayer = previewFrame.getChildAt(currentLayerIndex);
                if (this.mViewStubInflated[currentFrameIndex][currentLayerIndex]) {
                    if (previewFrame == this.mPreviewFrames[currentFrameIndex]) {
                        setVisibility(lastLayer, 4, animate);
                    } else {
                        setVisibility(lastLayer, 4, false);
                    }
                }
            }
            lastLayer = previewFrame.getChildAt(newLayerIndex);
            if (previewFrame == this.mPreviewFrames[currentFrameIndex]) {
                if (!this.mViewStubInflated[currentFrameIndex][newLayerIndex]) {
                    lastLayer = ((ViewStub) lastLayer).inflate();
                    lastLayer.setAlpha(0.0f);
                }
                setVisibility(lastLayer, 0, animate);
            } else {
                setVisibility(lastLayer, 0, false);
            }
        }
    }

    private void setVisibility(final View view, final int visibility, boolean animate) {
        float alpha = visibility == 0 ? 1.0f : 0.0f;
        if (animate) {
            Interpolator interpolator;
            if (visibility == 0) {
                interpolator = FADE_IN_INTERPOLATOR;
            } else {
                interpolator = FADE_OUT_INTERPOLATOR;
            }
            if (visibility == 0) {
                view.animate().alpha(alpha).setInterpolator(FADE_IN_INTERPOLATOR).setDuration(CROSS_FADE_DURATION_MS).setListener(new PreviewFrameAnimatorListener(this, null)).withStartAction(new Runnable() {
                    public void run() {
                        view.setVisibility(visibility);
                    }
                });
                return;
            } else {
                view.animate().alpha(alpha).setInterpolator(FADE_OUT_INTERPOLATOR).setDuration(CROSS_FADE_DURATION_MS).setListener(new PreviewFrameAnimatorListener(this, null)).withEndAction(new Runnable() {
                    public void run() {
                        view.setVisibility(visibility);
                    }
                });
                return;
            }
        }
        view.setAlpha(alpha);
        view.setVisibility(visibility);
    }

    private void runAnimationEndAction() {
        if (this.mAnimationEndAction != null && !isAnimating()) {
            this.mAnimationEndAction.run();
            this.mAnimationEndAction = null;
        }
    }
}
