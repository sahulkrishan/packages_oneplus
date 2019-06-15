package com.android.settings.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.Op;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnAttachStateChangeListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.settings.R;
import com.google.common.primitives.Ints;
import java.util.Arrays;

public class DotsPageIndicator extends View implements OnPageChangeListener {
    private static final int DEFAULT_ANIM_DURATION = 400;
    private static final int DEFAULT_DOT_SIZE = 8;
    private static final int DEFAULT_GAP = 12;
    private static final int DEFAULT_SELECTED_COLOUR = -1;
    private static final int DEFAULT_UNSELECTED_COLOUR = -2130706433;
    private static final float INVALID_FRACTION = -1.0f;
    private static final float MINIMAL_REVEAL = 1.0E-5f;
    public static final String TAG = DotsPageIndicator.class.getSimpleName();
    private long animDuration;
    private long animHalfDuration;
    private boolean attachedState;
    private final Path combinedUnselectedPath;
    float controlX1;
    float controlX2;
    float controlY1;
    float controlY2;
    private int currentPage;
    private float dotBottomY;
    private float[] dotCenterX;
    private float dotCenterY;
    private int dotDiameter;
    private float dotRadius;
    private float[] dotRevealFractions;
    private float dotTopY;
    float endX1;
    float endX2;
    float endY1;
    float endY2;
    private int gap;
    private float halfDotRadius;
    private final Interpolator interpolator;
    private AnimatorSet joiningAnimationSet;
    private ValueAnimator[] joiningAnimations;
    private float[] joiningFractions;
    private ValueAnimator moveAnimation;
    private OnPageChangeListener pageChangeListener;
    private int pageCount;
    private final RectF rectF;
    private PendingRetreatAnimator retreatAnimation;
    private float retreatingJoinX1;
    private float retreatingJoinX2;
    private PendingRevealAnimator[] revealAnimations;
    private int selectedColour;
    private boolean selectedDotInPosition;
    private float selectedDotX;
    private final Paint selectedPaint;
    private int unselectedColour;
    private final Path unselectedDotLeftPath;
    private final Path unselectedDotPath;
    private final Path unselectedDotRightPath;
    private final Paint unselectedPaint;
    private ViewPager viewPager;

    public abstract class PendingStartAnimator extends ValueAnimator {
        protected boolean hasStarted = false;
        protected StartPredicate predicate;

        public PendingStartAnimator(StartPredicate predicate) {
            this.predicate = predicate;
        }

        public void startIfNecessary(float currentValue) {
            if (!this.hasStarted && this.predicate.shouldStart(currentValue)) {
                start();
                this.hasStarted = true;
            }
        }
    }

    public abstract class StartPredicate {
        protected float thresholdValue;

        public abstract boolean shouldStart(float f);

        public StartPredicate(float thresholdValue) {
            this.thresholdValue = thresholdValue;
        }
    }

    public class LeftwardStartPredicate extends StartPredicate {
        public LeftwardStartPredicate(float thresholdValue) {
            super(thresholdValue);
        }

        /* Access modifiers changed, original: 0000 */
        public boolean shouldStart(float currentValue) {
            return currentValue < this.thresholdValue;
        }
    }

    public class PendingRetreatAnimator extends PendingStartAnimator {
        final /* synthetic */ DotsPageIndicator this$0;

        public PendingRetreatAnimator(DotsPageIndicator this$0, int was, int now, int steps, StartPredicate predicate) {
            float min;
            final DotsPageIndicator dotsPageIndicator = this$0;
            int i = was;
            int i2 = now;
            int i3 = steps;
            this.this$0 = dotsPageIndicator;
            super(predicate);
            setDuration(this$0.animHalfDuration);
            setInterpolator(this$0.interpolator);
            if (i2 > i) {
                min = Math.min(this$0.dotCenterX[i], this$0.selectedDotX) - this$0.dotRadius;
            } else {
                min = this$0.dotCenterX[i2] - this$0.dotRadius;
            }
            float initialX1 = min;
            if (i2 > i) {
                min = this$0.dotCenterX[i2] - this$0.dotRadius;
            } else {
                min = this$0.dotCenterX[i2] - this$0.dotRadius;
            }
            float finalX1 = min;
            if (i2 > i) {
                min = this$0.dotCenterX[i2] + this$0.dotRadius;
            } else {
                min = Math.max(this$0.dotCenterX[i], this$0.selectedDotX) + this$0.dotRadius;
            }
            float initialX2 = min;
            if (i2 > i) {
                min = this$0.dotCenterX[i2] + this$0.dotRadius;
            } else {
                min = this$0.dotCenterX[i2] + this$0.dotRadius;
            }
            float finalX2 = min;
            dotsPageIndicator.revealAnimations = new PendingRevealAnimator[i3];
            int[] dotsToHide = new int[i3];
            int i4 = 0;
            int i5;
            if (initialX1 != finalX1) {
                setFloatValues(new float[]{initialX1, finalX1});
                while (true) {
                    i5 = i4;
                    if (i5 >= i3) {
                        break;
                    }
                    this$0.revealAnimations[i5] = new PendingRevealAnimator(i + i5, new RightwardStartPredicate(this$0.dotCenterX[i + i5]));
                    dotsToHide[i5] = i + i5;
                    i4 = i5 + 1;
                    i2 = now;
                }
                addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        PendingRetreatAnimator.this.this$0.retreatingJoinX1 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        PendingRetreatAnimator.this.this$0.postInvalidateOnAnimation();
                        for (PendingRevealAnimator pendingReveal : PendingRetreatAnimator.this.this$0.revealAnimations) {
                            pendingReveal.startIfNecessary(PendingRetreatAnimator.this.this$0.retreatingJoinX1);
                        }
                    }
                });
            } else {
                setFloatValues(new float[]{initialX2, finalX2});
                while (true) {
                    i5 = i4;
                    if (i5 >= i3) {
                        break;
                    }
                    this$0.revealAnimations[i5] = new PendingRevealAnimator(i - i5, new LeftwardStartPredicate(this$0.dotCenterX[i - i5]));
                    dotsToHide[i5] = i - i5;
                    i4 = i5 + 1;
                }
                addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        PendingRetreatAnimator.this.this$0.retreatingJoinX2 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        PendingRetreatAnimator.this.this$0.postInvalidateOnAnimation();
                        for (PendingRevealAnimator pendingReveal : PendingRetreatAnimator.this.this$0.revealAnimations) {
                            pendingReveal.startIfNecessary(PendingRetreatAnimator.this.this$0.retreatingJoinX2);
                        }
                    }
                });
            }
            final DotsPageIndicator dotsPageIndicator2 = dotsPageIndicator;
            final int[] iArr = dotsToHide;
            final float f = initialX1;
            dotsToHide = initialX2;
            addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    PendingRetreatAnimator.this.this$0.cancelJoiningAnimations();
                    PendingRetreatAnimator.this.this$0.clearJoiningFractions();
                    for (int dot : iArr) {
                        PendingRetreatAnimator.this.this$0.setDotRevealFraction(dot, DotsPageIndicator.MINIMAL_REVEAL);
                    }
                    PendingRetreatAnimator.this.this$0.retreatingJoinX1 = f;
                    PendingRetreatAnimator.this.this$0.retreatingJoinX2 = dotsToHide;
                    PendingRetreatAnimator.this.this$0.postInvalidateOnAnimation();
                }

                public void onAnimationEnd(Animator animation) {
                    PendingRetreatAnimator.this.this$0.retreatingJoinX1 = -1.0f;
                    PendingRetreatAnimator.this.this$0.retreatingJoinX2 = -1.0f;
                    PendingRetreatAnimator.this.this$0.postInvalidateOnAnimation();
                }
            });
        }
    }

    public class PendingRevealAnimator extends PendingStartAnimator {
        private final int dot;

        public PendingRevealAnimator(int dot, StartPredicate predicate) {
            super(predicate);
            this.dot = dot;
            setFloatValues(new float[]{DotsPageIndicator.MINIMAL_REVEAL, 1.0f});
            setDuration(DotsPageIndicator.this.animHalfDuration);
            setInterpolator(DotsPageIndicator.this.interpolator);
            addUpdateListener(new AnimatorUpdateListener(DotsPageIndicator.this) {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    DotsPageIndicator.this.setDotRevealFraction(PendingRevealAnimator.this.dot, ((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            addListener(new AnimatorListenerAdapter(DotsPageIndicator.this) {
                public void onAnimationEnd(Animator animation) {
                    DotsPageIndicator.this.setDotRevealFraction(PendingRevealAnimator.this.dot, 0.0f);
                    DotsPageIndicator.this.postInvalidateOnAnimation();
                }
            });
        }
    }

    public class RightwardStartPredicate extends StartPredicate {
        public RightwardStartPredicate(float thresholdValue) {
            super(thresholdValue);
        }

        /* Access modifiers changed, original: 0000 */
        public boolean shouldStart(float currentValue) {
            return currentValue > this.thresholdValue;
        }
    }

    public DotsPageIndicator(Context context) {
        this(context, null, 0);
    }

    public DotsPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotsPageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int scaledDensity = (int) context.getResources().getDisplayMetrics().scaledDensity;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DotsPageIndicator, defStyle, 0);
        this.dotDiameter = typedArray.getDimensionPixelSize(2, 8 * scaledDensity);
        this.dotRadius = (float) (this.dotDiameter / 2);
        this.halfDotRadius = this.dotRadius / 2.0f;
        this.gap = typedArray.getDimensionPixelSize(3, 12 * scaledDensity);
        this.animDuration = (long) typedArray.getInteger(0, 400);
        this.animHalfDuration = this.animDuration / 2;
        this.unselectedColour = typedArray.getColor(4, DEFAULT_UNSELECTED_COLOUR);
        this.selectedColour = typedArray.getColor(1, -1);
        typedArray.recycle();
        this.unselectedPaint = new Paint(1);
        this.unselectedPaint.setColor(this.unselectedColour);
        this.selectedPaint = new Paint(1);
        this.selectedPaint.setColor(this.selectedColour);
        if (VERSION.SDK_INT >= 21) {
            this.interpolator = AnimationUtils.loadInterpolator(context, AndroidResources.FAST_OUT_SLOW_IN);
        } else {
            this.interpolator = AnimationUtils.loadInterpolator(context, 17432580);
        }
        this.combinedUnselectedPath = new Path();
        this.unselectedDotPath = new Path();
        this.unselectedDotLeftPath = new Path();
        this.unselectedDotRightPath = new Path();
        this.rectF = new RectF();
        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                DotsPageIndicator.this.attachedState = true;
            }

            public void onViewDetachedFromWindow(View v) {
                DotsPageIndicator.this.attachedState = false;
            }
        });
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.setOnPageChangeListener(this);
        setPageCount(viewPager.getAdapter().getCount());
        viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                DotsPageIndicator.this.setPageCount(DotsPageIndicator.this.viewPager.getAdapter().getCount());
            }
        });
        setCurrentPageImmediate();
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.pageChangeListener = onPageChangeListener;
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (this.pageChangeListener != null) {
            this.pageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    public void onPageSelected(int position) {
        if (this.attachedState) {
            setSelectedPage(position);
        } else {
            setCurrentPageImmediate();
        }
        if (this.pageChangeListener != null) {
            this.pageChangeListener.onPageSelected(position);
        }
    }

    public void onPageScrollStateChanged(int state) {
        if (this.pageChangeListener != null) {
            this.pageChangeListener.onPageScrollStateChanged(state);
        }
    }

    private void setPageCount(int pages) {
        this.pageCount = pages;
        calculateDotPositions();
        resetState();
    }

    private void calculateDotPositions() {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        float startLeft = ((float) (((((getWidth() - getPaddingRight()) - left) - getRequiredWidth()) / 2) + left)) + this.dotRadius;
        this.dotCenterX = new float[this.pageCount];
        for (int i = 0; i < this.pageCount; i++) {
            this.dotCenterX[i] = ((float) ((this.dotDiameter + this.gap) * i)) + startLeft;
        }
        this.dotTopY = (float) top;
        this.dotCenterY = ((float) top) + this.dotRadius;
        this.dotBottomY = (float) (this.dotDiameter + top);
        setCurrentPageImmediate();
    }

    private void setCurrentPageImmediate() {
        if (this.viewPager != null) {
            this.currentPage = this.viewPager.getCurrentItem();
        } else {
            this.currentPage = 0;
        }
        if (this.pageCount > 0) {
            this.selectedDotX = this.dotCenterX[this.currentPage];
        }
    }

    private void resetState() {
        if (this.pageCount > 0) {
            this.joiningFractions = new float[(this.pageCount - 1)];
            Arrays.fill(this.joiningFractions, 0.0f);
            this.dotRevealFractions = new float[this.pageCount];
            Arrays.fill(this.dotRevealFractions, 0.0f);
            this.retreatingJoinX1 = -1.0f;
            this.retreatingJoinX2 = -1.0f;
            this.selectedDotInPosition = true;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int desiredHeight = getDesiredHeight();
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == Integer.MIN_VALUE) {
            mode = Math.min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec));
        } else if (mode != Ints.MAX_POWER_OF_TWO) {
            mode = desiredHeight;
        } else {
            mode = MeasureSpec.getSize(heightMeasureSpec);
        }
        int desiredWidth = getDesiredWidth();
        int mode2 = MeasureSpec.getMode(widthMeasureSpec);
        if (mode2 == Integer.MIN_VALUE) {
            width = Math.min(desiredWidth, MeasureSpec.getSize(widthMeasureSpec));
        } else if (mode2 != Ints.MAX_POWER_OF_TWO) {
            width = desiredWidth;
        } else {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        setMeasuredDimension(width, mode);
        calculateDotPositions();
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        setMeasuredDimension(width, height);
        calculateDotPositions();
    }

    public void clearAnimation() {
        super.clearAnimation();
        if (VERSION.SDK_INT >= 16) {
            cancelRunningAnimations();
        }
    }

    private int getDesiredHeight() {
        return (getPaddingTop() + this.dotDiameter) + getPaddingBottom();
    }

    private int getRequiredWidth() {
        return (this.pageCount * this.dotDiameter) + ((this.pageCount - 1) * this.gap);
    }

    private int getDesiredWidth() {
        return (getPaddingLeft() + getRequiredWidth()) + getPaddingRight();
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        if (this.viewPager != null && this.pageCount != 0) {
            drawUnselected(canvas);
            drawSelected(canvas);
        }
    }

    private void drawUnselected(Canvas canvas) {
        this.combinedUnselectedPath.rewind();
        int page = 0;
        while (page < this.pageCount) {
            int nextXIndex = page == this.pageCount + -1 ? page : page + 1;
            if (VERSION.SDK_INT >= 21) {
                this.combinedUnselectedPath.op(getUnselectedPath(page, this.dotCenterX[page], this.dotCenterX[nextXIndex], page == this.pageCount + -1 ? -1.0f : this.joiningFractions[page], this.dotRevealFractions[page]), Op.UNION);
            } else {
                canvas.drawCircle(this.dotCenterX[page], this.dotCenterY, this.dotRadius, this.unselectedPaint);
            }
            page++;
        }
        if (this.retreatingJoinX1 != -1.0f && VERSION.SDK_INT >= 21) {
            this.combinedUnselectedPath.op(getRetreatingJoinPath(), Op.UNION);
        }
        canvas.drawPath(this.combinedUnselectedPath, this.unselectedPaint);
    }

    private Path getUnselectedPath(int page, float centerX, float nextCenterX, float joiningFraction, float dotRevealFraction) {
        int i = page;
        float f = centerX;
        float f2 = nextCenterX;
        this.unselectedDotPath.rewind();
        if ((joiningFraction == 0.0f || joiningFraction == -1.0f) && dotRevealFraction == 0.0f && !(i == this.currentPage && this.selectedDotInPosition)) {
            this.unselectedDotPath.addCircle(this.dotCenterX[i], this.dotCenterY, this.dotRadius, Direction.CW);
        }
        if (joiningFraction > 0.0f && joiningFraction < 0.5f && this.retreatingJoinX1 == -1.0f) {
            this.unselectedDotLeftPath.rewind();
            this.unselectedDotLeftPath.moveTo(f, this.dotBottomY);
            this.rectF.set(f - this.dotRadius, this.dotTopY, this.dotRadius + f, this.dotBottomY);
            this.unselectedDotLeftPath.arcTo(this.rectF, 90.0f, 180.0f, true);
            this.endX1 = (this.dotRadius + f) + (((float) this.gap) * joiningFraction);
            this.endY1 = this.dotCenterY;
            this.controlX1 = this.halfDotRadius + f;
            this.controlY1 = this.dotTopY;
            this.controlX2 = this.endX1;
            this.controlY2 = this.endY1 - this.halfDotRadius;
            this.unselectedDotLeftPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX1, this.endY1);
            this.endX2 = f;
            this.endY2 = this.dotBottomY;
            this.controlX1 = this.endX1;
            this.controlY1 = this.endY1 + this.halfDotRadius;
            this.controlX2 = this.halfDotRadius + f;
            this.controlY2 = this.dotBottomY;
            this.unselectedDotLeftPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX2, this.endY2);
            if (VERSION.SDK_INT >= 21) {
                this.unselectedDotPath.op(this.unselectedDotLeftPath, Op.UNION);
            }
            this.unselectedDotRightPath.rewind();
            this.unselectedDotRightPath.moveTo(f2, this.dotBottomY);
            this.rectF.set(f2 - this.dotRadius, this.dotTopY, this.dotRadius + f2, this.dotBottomY);
            this.unselectedDotRightPath.arcTo(this.rectF, 90.0f, -180.0f, true);
            this.endX1 = (f2 - this.dotRadius) - (((float) this.gap) * joiningFraction);
            this.endY1 = this.dotCenterY;
            this.controlX1 = f2 - this.halfDotRadius;
            this.controlY1 = this.dotTopY;
            this.controlX2 = this.endX1;
            this.controlY2 = this.endY1 - this.halfDotRadius;
            this.unselectedDotRightPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX1, this.endY1);
            this.endX2 = f2;
            this.endY2 = this.dotBottomY;
            this.controlX1 = this.endX1;
            this.controlY1 = this.endY1 + this.halfDotRadius;
            this.controlX2 = this.endX2 - this.halfDotRadius;
            this.controlY2 = this.dotBottomY;
            this.unselectedDotRightPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX2, this.endY2);
            if (VERSION.SDK_INT >= 21) {
                this.unselectedDotPath.op(this.unselectedDotRightPath, Op.UNION);
            }
        }
        if (joiningFraction > 0.5f && joiningFraction < 1.0f && this.retreatingJoinX1 == -1.0f) {
            this.unselectedDotPath.moveTo(f, this.dotBottomY);
            this.rectF.set(f - this.dotRadius, this.dotTopY, this.dotRadius + f, this.dotBottomY);
            this.unselectedDotPath.arcTo(this.rectF, 90.0f, 180.0f, true);
            this.endX1 = (this.dotRadius + f) + ((float) (this.gap / 2));
            this.endY1 = this.dotCenterY - (this.dotRadius * joiningFraction);
            this.controlX1 = this.endX1 - (this.dotRadius * joiningFraction);
            this.controlY1 = this.dotTopY;
            this.controlX2 = this.endX1 - ((1.0f - joiningFraction) * this.dotRadius);
            this.controlY2 = this.endY1;
            this.unselectedDotPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX1, this.endY1);
            this.endX2 = f2;
            this.endY2 = this.dotTopY;
            this.controlX1 = this.endX1 + ((1.0f - joiningFraction) * this.dotRadius);
            this.controlY1 = this.endY1;
            this.controlX2 = this.endX1 + (this.dotRadius * joiningFraction);
            this.controlY2 = this.dotTopY;
            this.unselectedDotPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX2, this.endY2);
            this.rectF.set(f2 - this.dotRadius, this.dotTopY, this.dotRadius + f2, this.dotBottomY);
            this.unselectedDotPath.arcTo(this.rectF, 270.0f, 180.0f, true);
            this.endY1 = this.dotCenterY + (this.dotRadius * joiningFraction);
            this.controlX1 = this.endX1 + (this.dotRadius * joiningFraction);
            this.controlY1 = this.dotBottomY;
            this.controlX2 = this.endX1 + ((1.0f - joiningFraction) * this.dotRadius);
            this.controlY2 = this.endY1;
            this.unselectedDotPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX1, this.endY1);
            this.endX2 = f;
            this.endY2 = this.dotBottomY;
            this.controlX1 = this.endX1 - ((1.0f - joiningFraction) * this.dotRadius);
            this.controlY1 = this.endY1;
            this.controlX2 = this.endX1 - (this.dotRadius * joiningFraction);
            this.controlY2 = this.endY2;
            this.unselectedDotPath.cubicTo(this.controlX1, this.controlY1, this.controlX2, this.controlY2, this.endX2, this.endY2);
        }
        if (joiningFraction == 1.0f && this.retreatingJoinX1 == -1.0f) {
            this.rectF.set(f - this.dotRadius, this.dotTopY, this.dotRadius + f2, this.dotBottomY);
            this.unselectedDotPath.addRoundRect(this.rectF, this.dotRadius, this.dotRadius, Direction.CW);
        }
        if (dotRevealFraction > MINIMAL_REVEAL) {
            this.unselectedDotPath.addCircle(f, this.dotCenterY, this.dotRadius * dotRevealFraction, Direction.CW);
        }
        return this.unselectedDotPath;
    }

    private Path getRetreatingJoinPath() {
        this.unselectedDotPath.rewind();
        this.rectF.set(this.retreatingJoinX1, this.dotTopY, this.retreatingJoinX2, this.dotBottomY);
        this.unselectedDotPath.addRoundRect(this.rectF, this.dotRadius, this.dotRadius, Direction.CW);
        return this.unselectedDotPath;
    }

    private void drawSelected(Canvas canvas) {
        canvas.drawCircle(this.selectedDotX, this.dotCenterY, this.dotRadius, this.selectedPaint);
    }

    private void setSelectedPage(int now) {
        if (now != this.currentPage && this.pageCount != 0) {
            int was = this.currentPage;
            this.currentPage = now;
            if (VERSION.SDK_INT >= 16) {
                cancelRunningAnimations();
                int steps = Math.abs(now - was);
                this.moveAnimation = createMoveSelectedAnimator(this.dotCenterX[now], was, now, steps);
                this.joiningAnimations = new ValueAnimator[steps];
                int i = 0;
                while (i < steps) {
                    this.joiningAnimations[i] = createJoiningAnimator(now > was ? was + i : (was - 1) - i, ((long) i) * (this.animDuration / 8));
                    i++;
                }
                this.moveAnimation.start();
                startJoiningAnimations();
            } else {
                setCurrentPageImmediate();
                invalidate();
            }
        }
    }

    private ValueAnimator createMoveSelectedAnimator(float moveTo, int was, int now, int steps) {
        StartPredicate rightwardStartPredicate;
        ValueAnimator moveSelected = ValueAnimator.ofFloat(new float[]{this.selectedDotX, moveTo});
        if (now > was) {
            rightwardStartPredicate = new RightwardStartPredicate(moveTo - ((moveTo - this.selectedDotX) * 0.25f));
        } else {
            rightwardStartPredicate = new LeftwardStartPredicate(((this.selectedDotX - moveTo) * 0.25f) + moveTo);
        }
        this.retreatAnimation = new PendingRetreatAnimator(this, was, now, steps, rightwardStartPredicate);
        moveSelected.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                DotsPageIndicator.this.selectedDotX = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                DotsPageIndicator.this.retreatAnimation.startIfNecessary(DotsPageIndicator.this.selectedDotX);
                DotsPageIndicator.this.postInvalidateOnAnimation();
            }
        });
        moveSelected.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                DotsPageIndicator.this.selectedDotInPosition = false;
            }

            public void onAnimationEnd(Animator animation) {
                DotsPageIndicator.this.selectedDotInPosition = true;
            }
        });
        moveSelected.setStartDelay(this.selectedDotInPosition ? this.animDuration / 4 : 0);
        moveSelected.setDuration((this.animDuration * 3) / 4);
        moveSelected.setInterpolator(this.interpolator);
        return moveSelected;
    }

    /* JADX WARNING: Incorrect type for fill-array insn 0x0003, element type: float, insn element type: null */
    private android.animation.ValueAnimator createJoiningAnimator(final int r4, long r5) {
        /*
        r3 = this;
        r0 = 2;
        r0 = new float[r0];
        r0 = {0, 1065353216};
        r0 = android.animation.ValueAnimator.ofFloat(r0);
        r1 = new com.android.settings.widget.DotsPageIndicator$5;
        r1.<init>(r4);
        r0.addUpdateListener(r1);
        r1 = r3.animHalfDuration;
        r0.setDuration(r1);
        r0.setStartDelay(r5);
        r1 = r3.interpolator;
        r0.setInterpolator(r1);
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.widget.DotsPageIndicator.createJoiningAnimator(int, long):android.animation.ValueAnimator");
    }

    private void setJoiningFraction(int leftDot, float fraction) {
        this.joiningFractions[leftDot] = fraction;
        postInvalidateOnAnimation();
    }

    private void clearJoiningFractions() {
        Arrays.fill(this.joiningFractions, 0.0f);
        postInvalidateOnAnimation();
    }

    private void setDotRevealFraction(int dot, float fraction) {
        this.dotRevealFractions[dot] = fraction;
        postInvalidateOnAnimation();
    }

    private void cancelRunningAnimations() {
        cancelMoveAnimation();
        cancelJoiningAnimations();
        cancelRetreatAnimation();
        cancelRevealAnimations();
        resetState();
    }

    private void cancelMoveAnimation() {
        if (this.moveAnimation != null && this.moveAnimation.isRunning()) {
            this.moveAnimation.cancel();
        }
    }

    private void startJoiningAnimations() {
        this.joiningAnimationSet = new AnimatorSet();
        this.joiningAnimationSet.playTogether(this.joiningAnimations);
        this.joiningAnimationSet.start();
    }

    private void cancelJoiningAnimations() {
        if (this.joiningAnimationSet != null && this.joiningAnimationSet.isRunning()) {
            this.joiningAnimationSet.cancel();
        }
    }

    private void cancelRetreatAnimation() {
        if (this.retreatAnimation != null && this.retreatAnimation.isRunning()) {
            this.retreatAnimation.cancel();
        }
    }

    private void cancelRevealAnimations() {
        if (this.revealAnimations != null) {
            for (PendingRevealAnimator reveal : this.revealAnimations) {
                reveal.cancel();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getUnselectedColour() {
        return this.unselectedColour;
    }

    /* Access modifiers changed, original: 0000 */
    public int getSelectedColour() {
        return this.selectedColour;
    }

    /* Access modifiers changed, original: 0000 */
    public float getDotCenterY() {
        return this.dotCenterY;
    }

    /* Access modifiers changed, original: 0000 */
    public float getDotCenterX(int page) {
        return this.dotCenterX[page];
    }

    /* Access modifiers changed, original: 0000 */
    public float getSelectedDotX() {
        return this.selectedDotX;
    }

    /* Access modifiers changed, original: 0000 */
    public int getCurrentPage() {
        return this.currentPage;
    }
}
