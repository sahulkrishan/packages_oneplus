package com.android.settings.fingerprint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.settings.R;
import com.android.settingslib.Utils;

public class FingerprintLocationAnimationView extends View implements FingerprintFindSensorAnimation {
    private static final long DELAY_BETWEEN_PHASE = 1000;
    private static final float MAX_PULSE_ALPHA = 0.15f;
    private ValueAnimator mAlphaAnimator;
    private final Paint mDotPaint = new Paint();
    private final int mDotRadius = getResources().getDimensionPixelSize(R.dimen.fingerprint_dot_radius);
    private final Interpolator mFastOutSlowInInterpolator;
    private final float mFractionCenterX = getResources().getFraction(R.fraction.fingerprint_sensor_location_fraction_x, 1, 1);
    private final float mFractionCenterY = getResources().getFraction(R.fraction.fingerprint_sensor_location_fraction_y, 1, 1);
    private final Interpolator mLinearOutSlowInInterpolator;
    private final int mMaxPulseRadius = getResources().getDimensionPixelSize(R.dimen.fingerprint_pulse_radius);
    private final Paint mPulsePaint = new Paint();
    private float mPulseRadius;
    private ValueAnimator mRadiusAnimator;
    private final Runnable mStartPhaseRunnable = new Runnable() {
        public void run() {
            FingerprintLocationAnimationView.this.startPhase();
        }
    };

    public FingerprintLocationAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        int colorAccent = Utils.getColorAccent(context);
        this.mDotPaint.setAntiAlias(true);
        this.mPulsePaint.setAntiAlias(true);
        this.mDotPaint.setColor(colorAccent);
        this.mPulsePaint.setColor(colorAccent);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, AndroidResources.LINEAR_OUT_SLOW_IN);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, AndroidResources.LINEAR_OUT_SLOW_IN);
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        drawPulse(canvas);
        drawDot(canvas);
    }

    private void drawDot(Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), (float) this.mDotRadius, this.mDotPaint);
    }

    private void drawPulse(Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), this.mPulseRadius, this.mPulsePaint);
    }

    private float getCenterX() {
        return (float) (getWidth() / 2);
    }

    private float getCenterY() {
        return (float) (getHeight() / 2);
    }

    public void startAnimation() {
        startPhase();
    }

    public void stopAnimation() {
        removeCallbacks(this.mStartPhaseRunnable);
        if (this.mRadiusAnimator != null) {
            this.mRadiusAnimator.cancel();
        }
        if (this.mAlphaAnimator != null) {
            this.mAlphaAnimator.cancel();
        }
    }

    public void pauseAnimation() {
        stopAnimation();
    }

    private void startPhase() {
        startRadiusAnimation();
        startAlphaAnimation();
    }

    private void startRadiusAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{null, (float) this.mMaxPulseRadius});
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                FingerprintLocationAnimationView.this.mPulseRadius = ((Float) animation.getAnimatedValue()).floatValue();
                FingerprintLocationAnimationView.this.invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            boolean mCancelled;

            public void onAnimationCancel(Animator animation) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animation) {
                FingerprintLocationAnimationView.this.mRadiusAnimator = null;
                if (!this.mCancelled) {
                    FingerprintLocationAnimationView.this.postDelayed(FingerprintLocationAnimationView.this.mStartPhaseRunnable, 1000);
                }
            }
        });
        animator.setDuration(1000);
        animator.setInterpolator(this.mLinearOutSlowInInterpolator);
        animator.start();
        this.mRadiusAnimator = animator;
    }

    /* JADX WARNING: Incorrect type for fill-array insn 0x000a, element type: float, insn element type: null */
    private void startAlphaAnimation() {
        /*
        r3 = this;
        r0 = r3.mPulsePaint;
        r1 = 38;
        r0.setAlpha(r1);
        r0 = 2;
        r0 = new float[r0];
        r0 = {1041865114, 0};
        r0 = android.animation.ValueAnimator.ofFloat(r0);
        r1 = new com.android.settings.fingerprint.FingerprintLocationAnimationView$3;
        r1.<init>();
        r0.addUpdateListener(r1);
        r1 = new com.android.settings.fingerprint.FingerprintLocationAnimationView$4;
        r1.<init>();
        r0.addListener(r1);
        r1 = 750; // 0x2ee float:1.051E-42 double:3.705E-321;
        r0.setDuration(r1);
        r1 = r3.mFastOutSlowInInterpolator;
        r0.setInterpolator(r1);
        r1 = 250; // 0xfa float:3.5E-43 double:1.235E-321;
        r0.setStartDelay(r1);
        r0.start();
        r3.mAlphaAnimator = r0;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fingerprint.FingerprintLocationAnimationView.startAlphaAnimation():void");
    }
}
