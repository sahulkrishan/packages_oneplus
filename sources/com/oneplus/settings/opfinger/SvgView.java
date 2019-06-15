package com.oneplus.settings.opfinger;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.settings.R;
import com.oneplus.settings.opfinger.SvgHelper.SvgPath;
import java.util.ArrayList;
import java.util.List;

public class SvgView extends View {
    private static final String LOG_TAG = "SvgView";
    private int mDuration;
    private float mFadeFactor;
    private boolean mHaveMoved;
    private Thread mLoader;
    private float mOffsetY;
    private final Paint mPaint;
    private float mParallax;
    private List<SvgPath> mPaths;
    private float mPhase;
    private final SvgHelper mSvg;
    private ObjectAnimator mSvgAnimator;
    private ObjectAnimator mSvgExceptionAnimator;
    private final Object mSvgLock;
    private ObjectAnimator mSvgResetAnimator;
    private int mSvgResource;

    public SvgView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SvgView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPaint = new Paint(1);
        this.mSvg = new SvgHelper(this.mPaint);
        this.mSvgLock = new Object();
        this.mPaths = new ArrayList(0);
        this.mParallax = 1.0f;
        this.mHaveMoved = false;
        this.mPaint.setStyle(Style.STROKE);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SvgView, defStyle, 0);
        if (a != null) {
            try {
                this.mPaint.setStrokeWidth((float) (getResources().getDisplayMetrics().densityDpi / 50));
                this.mPaint.setColor(a.getColor(2, ViewCompat.MEASURED_STATE_MASK));
                this.mPhase = a.getFloat(4, 1.0f);
                this.mDuration = a.getInt(0, 4000);
                this.mFadeFactor = a.getFloat(1, 10.0f);
            } catch (Throwable th) {
                if (a != null) {
                    a.recycle();
                }
            }
        }
        if (a != null) {
            a.recycle();
        }
        this.mPaint.setStrokeCap(Cap.ROUND);
        Shader linearGradient = new LinearGradient(0.0f, 0.0f, 100.0f, 100.0f, new int[]{-65536, -16711936, -16776961, InputDeviceCompat.SOURCE_ANY}, null, TileMode.REPEAT);
    }

    private void updatePathsPhaseLocked() {
        int count = this.mPaths.size();
        for (int i = 0; i < count; i++) {
            SvgPath svgPath = (SvgPath) this.mPaths.get(i);
            svgPath.renderPath.reset();
            svgPath.measure.getSegment(0.0f, svgPath.length * this.mPhase, svgPath.renderPath, true);
            svgPath.renderPath.rLineTo(0.0f, 0.0f);
        }
    }

    public float getParallax() {
        return this.mParallax;
    }

    public void setParallax(float parallax) {
        this.mParallax = parallax;
        invalidate();
    }

    public float getPhase() {
        return this.mPhase;
    }

    public void setPhase(float phase) {
        this.mPhase = phase;
        synchronized (this.mSvgLock) {
            updatePathsPhaseLocked();
        }
        invalidate();
    }

    public int getSvgResource() {
        return this.mSvgResource;
    }

    public void setSvgResource(int svgResource) {
        this.mSvgResource = svgResource;
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mLoader != null) {
            try {
                this.mLoader.join();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Unexpected error", e);
            }
        }
        this.mLoader = new Thread(new Runnable() {
            public void run() {
                SvgView.this.mSvg.load(SvgView.this.getContext(), SvgView.this.mSvgResource);
                synchronized (SvgView.this.mSvgLock) {
                    SvgView.this.mPaths = SvgView.this.mSvg.getPathsForViewport((w - SvgView.this.getPaddingLeft()) - SvgView.this.getPaddingRight(), (h - SvgView.this.getPaddingTop()) - SvgView.this.getPaddingBottom());
                    SvgView.this.updatePathsPhaseLocked();
                }
            }
        }, "SVG Loader");
        this.mLoader.start();
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (this.mSvgLock) {
            canvas.save();
            canvas.translate((float) getPaddingLeft(), ((float) getPaddingTop()) + this.mOffsetY);
            int count = this.mPaths.size();
            for (int i = 0; i < count; i++) {
                SvgPath svgPath = (SvgPath) this.mPaths.get(i);
                svgPath.paint.setAlpha((int) (((float) ((int) (Math.min(this.mPhase * this.mFadeFactor, 1.0f) * 1132396544))) * this.mParallax));
                canvas.drawPath(svgPath.renderPath, svgPath.paint);
            }
            canvas.restore();
        }
    }

    public void reveal(boolean success) {
        AnimatorSet set;
        if (this.mHaveMoved) {
            this.mSvgAnimator = ObjectAnimator.ofFloat(this, "phase", new float[]{0.0f, 1.0f});
            this.mSvgAnimator.setDuration((long) this.mDuration);
            ObjectAnimator.ofFloat(this, "phase", new float[]{1.0f, 0.0f}).setDuration((long) this.mDuration);
            if (success) {
                this.mSvgAnimator.start();
            } else {
                set = new AnimatorSet();
                set.playSequentially(new Animator[]{this.mSvgAnimator, mSvgAnimator1});
                set.start();
            }
        } else {
            this.mSvgAnimator = null;
            this.mSvgAnimator = ObjectAnimator.ofFloat(this, "phase", new float[]{0.0f, 1.0f});
            this.mSvgAnimator.setDuration((long) this.mDuration);
            ObjectAnimator.ofFloat(this, "phase", new float[]{1.0f, 0.0f}).setDuration((long) this.mDuration);
            if (success) {
                this.mSvgAnimator.start();
            } else {
                set = new AnimatorSet();
                set.playSequentially(new Animator[]{this.mSvgAnimator, mSvgAnimator1});
                set.start();
            }
        }
        this.mHaveMoved = true;
    }

    public void reveal(View scroller, int parentBottom) {
        if (this.mSvgAnimator == null) {
            this.mSvgAnimator = ObjectAnimator.ofFloat(this, "phase", new float[]{0.0f, 1.0f});
            this.mSvgAnimator.setDuration((long) this.mDuration);
            this.mSvgAnimator.start();
        }
        float previousOffset = this.mOffsetY;
        this.mOffsetY = (float) Math.min(0, scroller.getHeight() - (parentBottom - scroller.getScrollY()));
        if (previousOffset != this.mOffsetY) {
            invalidate();
        }
    }

    public void reset(boolean force) {
        if (force) {
            this.mSvgResetAnimator = null;
            this.mSvgResetAnimator = ObjectAnimator.ofFloat(this, "phase", new float[]{1.0f, 0.0f});
            this.mSvgResetAnimator.setDuration(0);
            this.mSvgResetAnimator.start();
        } else if (this.mSvgResetAnimator == null) {
            this.mSvgResetAnimator = ObjectAnimator.ofFloat(this, "phase", new float[]{1.0f, 0.0f});
            this.mSvgResetAnimator.setDuration((long) this.mDuration);
            this.mSvgResetAnimator.start();
        }
        this.mHaveMoved = false;
        invalidate();
    }

    public void revealWithoutAnimation() {
        this.mSvgResetAnimator = null;
        this.mSvgAnimator = ObjectAnimator.ofFloat(this, "phase", new float[]{0.0f, 1.0f});
        this.mSvgAnimator.setDuration(0);
        this.mSvgAnimator.start();
    }

    public void resetWithAnimation() {
        this.mSvgResetAnimator = null;
        this.mSvgResetAnimator = ObjectAnimator.ofFloat(this, "phase", new float[]{1.0f, 0.0f});
        this.mSvgResetAnimator.setDuration((long) this.mDuration);
        this.mSvgResetAnimator.start();
        this.mHaveMoved = false;
    }

    public void resetWithoutAnimation() {
        this.mSvgResetAnimator = null;
        this.mSvgResetAnimator = ObjectAnimator.ofFloat(this, "phase", new float[]{1.0f, 0.0f});
        this.mSvgResetAnimator.setDuration(0);
        this.mSvgResetAnimator.start();
        this.mHaveMoved = false;
    }
}
