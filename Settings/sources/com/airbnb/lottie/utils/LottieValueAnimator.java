package com.airbnb.lottie.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.support.annotation.FloatRange;

public class LottieValueAnimator extends ValueAnimator {
    private boolean isReversed = false;
    private float maxProgress = 1.0f;
    private float minProgress = 0.0f;
    private long originalDuration;
    private float progress = 0.0f;
    private boolean systemAnimationsAreDisabled = false;

    public LottieValueAnimator() {
        setFloatValues(new float[]{0.0f, 1.0f});
        addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                LottieValueAnimator.this.updateValues(LottieValueAnimator.this.minProgress, LottieValueAnimator.this.maxProgress);
            }

            public void onAnimationCancel(Animator animation) {
                LottieValueAnimator.this.updateValues(LottieValueAnimator.this.minProgress, LottieValueAnimator.this.maxProgress);
            }
        });
        addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!LottieValueAnimator.this.systemAnimationsAreDisabled) {
                    LottieValueAnimator.this.progress = ((Float) animation.getAnimatedValue()).floatValue();
                }
            }
        });
    }

    public void start() {
        if (this.systemAnimationsAreDisabled) {
            setProgress(getMaxProgress());
            end();
            return;
        }
        super.start();
    }

    public void systemAnimationsAreDisabled() {
        this.systemAnimationsAreDisabled = true;
    }

    public ValueAnimator setDuration(long duration) {
        this.originalDuration = duration;
        updateValues(this.minProgress, this.maxProgress);
        return this;
    }

    public void setProgress(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
        if (this.progress != progress) {
            setProgressInternal(progress);
        }
    }

    public void forceUpdate() {
        setProgressInternal(getProgress());
    }

    private void setProgressInternal(@FloatRange(from = 0.0d, to = 1.0d) float progress) {
        if (progress < this.minProgress) {
            progress = this.minProgress;
        } else if (progress > this.maxProgress) {
            progress = this.maxProgress;
        }
        this.progress = progress;
        if (getDuration() > 0) {
            setCurrentPlayTime((long) (((float) getDuration()) * ((progress - this.minProgress) / (this.maxProgress - this.minProgress))));
        }
    }

    public float getProgress() {
        return this.progress;
    }

    public void setIsReversed(boolean isReversed) {
        this.isReversed = isReversed;
        updateValues(this.minProgress, this.maxProgress);
    }

    public void setMinProgress(float minProgress) {
        this.minProgress = minProgress;
        updateValues(minProgress, this.maxProgress);
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
        updateValues(this.minProgress, maxProgress);
    }

    public float getMinProgress() {
        return this.minProgress;
    }

    public float getMaxProgress() {
        return this.maxProgress;
    }

    public void resumeAnimation() {
        float startingProgress = this.progress;
        start();
        setProgress(startingProgress);
    }

    public void updateValues(float startProgress, float endProgress) {
        float minValue = Math.min(startProgress, endProgress);
        float maxValue = Math.max(startProgress, endProgress);
        float[] fArr = new float[2];
        fArr[0] = this.isReversed ? maxValue : minValue;
        fArr[1] = this.isReversed ? minValue : maxValue;
        setFloatValues(fArr);
        super.setDuration((long) (((float) this.originalDuration) * (maxValue - minValue)));
        setProgress(getProgress());
    }
}
