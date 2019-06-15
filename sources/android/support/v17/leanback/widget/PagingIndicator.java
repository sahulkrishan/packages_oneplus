package android.support.v17.leanback.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.v17.leanback.R;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.animation.DecelerateInterpolator;
import com.google.common.primitives.Ints;

@RestrictTo({Scope.LIBRARY_GROUP})
public class PagingIndicator extends View {
    private static final TimeInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final Property<Dot, Float> DOT_ALPHA = new Property<Dot, Float>(Float.class, "alpha") {
        public Float get(Dot dot) {
            return Float.valueOf(dot.getAlpha());
        }

        public void set(Dot dot, Float value) {
            dot.setAlpha(value.floatValue());
        }
    };
    private static final Property<Dot, Float> DOT_DIAMETER = new Property<Dot, Float>(Float.class, "diameter") {
        public Float get(Dot dot) {
            return Float.valueOf(dot.getDiameter());
        }

        public void set(Dot dot, Float value) {
            dot.setDiameter(value.floatValue());
        }
    };
    private static final Property<Dot, Float> DOT_TRANSLATION_X = new Property<Dot, Float>(Float.class, "translation_x") {
        public Float get(Dot dot) {
            return Float.valueOf(dot.getTranslationX());
        }

        public void set(Dot dot, Float value) {
            dot.setTranslationX(value.floatValue());
        }
    };
    private static final long DURATION_ALPHA = 167;
    private static final long DURATION_DIAMETER = 417;
    private static final long DURATION_TRANSLATION_X = 417;
    private final AnimatorSet mAnimator;
    Bitmap mArrow;
    final int mArrowDiameter;
    private final int mArrowGap;
    Paint mArrowPaint;
    final int mArrowRadius;
    final Rect mArrowRect;
    final float mArrowToBgRatio;
    final Paint mBgPaint;
    private int mCurrentPage;
    int mDotCenterY;
    final int mDotDiameter;
    @ColorInt
    int mDotFgSelectColor;
    private final int mDotGap;
    final int mDotRadius;
    private int[] mDotSelectedNextX;
    private int[] mDotSelectedPrevX;
    private int[] mDotSelectedX;
    private Dot[] mDots;
    final Paint mFgPaint;
    private final AnimatorSet mHideAnimator;
    boolean mIsLtr;
    private int mPageCount;
    private int mPreviousPage;
    private final int mShadowRadius;
    private final AnimatorSet mShowAnimator;

    public class Dot {
        static final float LEFT = -1.0f;
        static final float LTR = 1.0f;
        static final float RIGHT = 1.0f;
        static final float RTL = -1.0f;
        float mAlpha;
        float mArrowImageRadius;
        float mCenterX;
        float mDiameter;
        float mDirection = 1.0f;
        @ColorInt
        int mFgColor;
        float mLayoutDirection;
        float mRadius;
        float mTranslationX;

        public Dot() {
            float f = 1.0f;
            if (!PagingIndicator.this.mIsLtr) {
                f = -1.0f;
            }
            this.mLayoutDirection = f;
        }

        /* Access modifiers changed, original: 0000 */
        public void select() {
            this.mTranslationX = 0.0f;
            this.mCenterX = 0.0f;
            this.mDiameter = (float) PagingIndicator.this.mArrowDiameter;
            this.mRadius = (float) PagingIndicator.this.mArrowRadius;
            this.mArrowImageRadius = this.mRadius * PagingIndicator.this.mArrowToBgRatio;
            this.mAlpha = 1.0f;
            adjustAlpha();
        }

        /* Access modifiers changed, original: 0000 */
        public void deselect() {
            this.mTranslationX = 0.0f;
            this.mCenterX = 0.0f;
            this.mDiameter = (float) PagingIndicator.this.mDotDiameter;
            this.mRadius = (float) PagingIndicator.this.mDotRadius;
            this.mArrowImageRadius = this.mRadius * PagingIndicator.this.mArrowToBgRatio;
            this.mAlpha = 0.0f;
            adjustAlpha();
        }

        public void adjustAlpha() {
            this.mFgColor = Color.argb(Math.round(255.0f * this.mAlpha), Color.red(PagingIndicator.this.mDotFgSelectColor), Color.green(PagingIndicator.this.mDotFgSelectColor), Color.blue(PagingIndicator.this.mDotFgSelectColor));
        }

        public float getAlpha() {
            return this.mAlpha;
        }

        public void setAlpha(float alpha) {
            this.mAlpha = alpha;
            adjustAlpha();
            PagingIndicator.this.invalidate();
        }

        public float getTranslationX() {
            return this.mTranslationX;
        }

        public void setTranslationX(float translationX) {
            this.mTranslationX = (this.mDirection * translationX) * this.mLayoutDirection;
            PagingIndicator.this.invalidate();
        }

        public float getDiameter() {
            return this.mDiameter;
        }

        public void setDiameter(float diameter) {
            this.mDiameter = diameter;
            this.mRadius = diameter / 2.0f;
            this.mArrowImageRadius = (diameter / 2.0f) * PagingIndicator.this.mArrowToBgRatio;
            PagingIndicator.this.invalidate();
        }

        /* Access modifiers changed, original: 0000 */
        public void draw(Canvas canvas) {
            float centerX = this.mCenterX + this.mTranslationX;
            canvas.drawCircle(centerX, (float) PagingIndicator.this.mDotCenterY, this.mRadius, PagingIndicator.this.mBgPaint);
            if (this.mAlpha > 0.0f) {
                PagingIndicator.this.mFgPaint.setColor(this.mFgColor);
                canvas.drawCircle(centerX, (float) PagingIndicator.this.mDotCenterY, this.mRadius, PagingIndicator.this.mFgPaint);
                canvas.drawBitmap(PagingIndicator.this.mArrow, PagingIndicator.this.mArrowRect, new Rect((int) (centerX - this.mArrowImageRadius), (int) (((float) PagingIndicator.this.mDotCenterY) - this.mArrowImageRadius), (int) (this.mArrowImageRadius + centerX), (int) (((float) PagingIndicator.this.mDotCenterY) + this.mArrowImageRadius)), PagingIndicator.this.mArrowPaint);
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void onRtlPropertiesChanged() {
            this.mLayoutDirection = PagingIndicator.this.mIsLtr ? 1.0f : -1.0f;
        }
    }

    public PagingIndicator(Context context) {
        this(context, null, 0);
    }

    public PagingIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagingIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAnimator = new AnimatorSet();
        Resources res = getResources();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PagingIndicator, defStyle, 0);
        this.mDotRadius = getDimensionFromTypedArray(typedArray, R.styleable.PagingIndicator_lbDotRadius, R.dimen.lb_page_indicator_dot_radius);
        this.mDotDiameter = this.mDotRadius * 2;
        this.mArrowRadius = getDimensionFromTypedArray(typedArray, R.styleable.PagingIndicator_arrowRadius, R.dimen.lb_page_indicator_arrow_radius);
        this.mArrowDiameter = this.mArrowRadius * 2;
        this.mDotGap = getDimensionFromTypedArray(typedArray, R.styleable.PagingIndicator_dotToDotGap, R.dimen.lb_page_indicator_dot_gap);
        this.mArrowGap = getDimensionFromTypedArray(typedArray, R.styleable.PagingIndicator_dotToArrowGap, R.dimen.lb_page_indicator_arrow_gap);
        int dotBgColor = getColorFromTypedArray(typedArray, R.styleable.PagingIndicator_dotBgColor, R.color.lb_page_indicator_dot);
        this.mBgPaint = new Paint(1);
        this.mBgPaint.setColor(dotBgColor);
        this.mDotFgSelectColor = getColorFromTypedArray(typedArray, R.styleable.PagingIndicator_arrowBgColor, R.color.lb_page_indicator_arrow_background);
        if (this.mArrowPaint == null && typedArray.hasValue(R.styleable.PagingIndicator_arrowColor)) {
            setArrowColor(typedArray.getColor(R.styleable.PagingIndicator_arrowColor, 0));
        }
        typedArray.recycle();
        this.mIsLtr = res.getConfiguration().getLayoutDirection() == 0;
        int shadowColor = res.getColor(R.color.lb_page_indicator_arrow_shadow);
        this.mShadowRadius = res.getDimensionPixelSize(R.dimen.lb_page_indicator_arrow_shadow_radius);
        this.mFgPaint = new Paint(1);
        int shadowOffset = res.getDimensionPixelSize(R.dimen.lb_page_indicator_arrow_shadow_offset);
        this.mFgPaint.setShadowLayer((float) this.mShadowRadius, (float) shadowOffset, (float) shadowOffset, shadowColor);
        this.mArrow = loadArrow();
        this.mArrowRect = new Rect(0, 0, this.mArrow.getWidth(), this.mArrow.getHeight());
        this.mArrowToBgRatio = ((float) this.mArrow.getWidth()) / ((float) this.mArrowDiameter);
        this.mShowAnimator = new AnimatorSet();
        this.mShowAnimator.playTogether(new Animator[]{createDotAlphaAnimator(0.0f, 1.0f), createDotDiameterAnimator((float) (this.mDotRadius * 2), (float) (this.mArrowRadius * 2)), createDotTranslationXAnimator()});
        this.mHideAnimator = new AnimatorSet();
        this.mHideAnimator.playTogether(new Animator[]{createDotAlphaAnimator(1.0f, 0.0f), createDotDiameterAnimator((float) (this.mArrowRadius * 2), (float) (this.mDotRadius * 2)), createDotTranslationXAnimator()});
        this.mAnimator.playTogether(new Animator[]{this.mShowAnimator, this.mHideAnimator});
        setLayerType(1, null);
    }

    private int getDimensionFromTypedArray(TypedArray typedArray, int attr, int defaultId) {
        return typedArray.getDimensionPixelOffset(attr, getResources().getDimensionPixelOffset(defaultId));
    }

    private int getColorFromTypedArray(TypedArray typedArray, int attr, int defaultId) {
        return typedArray.getColor(attr, getResources().getColor(defaultId));
    }

    private Bitmap loadArrow() {
        Bitmap arrow = BitmapFactory.decodeResource(getResources(), R.drawable.lb_ic_nav_arrow);
        if (this.mIsLtr) {
            return arrow;
        }
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(arrow, 0, 0, arrow.getWidth(), arrow.getHeight(), matrix, false);
    }

    public void setArrowColor(@ColorInt int color) {
        if (this.mArrowPaint == null) {
            this.mArrowPaint = new Paint();
        }
        this.mArrowPaint.setColorFilter(new PorterDuffColorFilter(color, Mode.SRC_IN));
    }

    public void setDotBackgroundColor(@ColorInt int color) {
        this.mBgPaint.setColor(color);
    }

    public void setArrowBackgroundColor(@ColorInt int color) {
        this.mDotFgSelectColor = color;
    }

    private Animator createDotAlphaAnimator(float from, float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(null, DOT_ALPHA, new float[]{from, to});
        animator.setDuration(DURATION_ALPHA);
        animator.setInterpolator(DECELERATE_INTERPOLATOR);
        return animator;
    }

    private Animator createDotDiameterAnimator(float from, float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(null, DOT_DIAMETER, new float[]{from, to});
        animator.setDuration(417);
        animator.setInterpolator(DECELERATE_INTERPOLATOR);
        return animator;
    }

    private Animator createDotTranslationXAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(null, DOT_TRANSLATION_X, new float[]{(float) ((-this.mArrowGap) + this.mDotGap), 0.0f});
        animator.setDuration(417);
        animator.setInterpolator(DECELERATE_INTERPOLATOR);
        return animator;
    }

    public void setPageCount(int pages) {
        if (pages > 0) {
            this.mPageCount = pages;
            this.mDots = new Dot[this.mPageCount];
            for (int i = 0; i < this.mPageCount; i++) {
                this.mDots[i] = new Dot();
            }
            calculateDotPositions();
            setSelectedPage(0);
            return;
        }
        throw new IllegalArgumentException("The page count should be a positive integer");
    }

    public void onPageSelected(int pageIndex, boolean withAnimation) {
        if (this.mCurrentPage != pageIndex) {
            if (this.mAnimator.isStarted()) {
                this.mAnimator.end();
            }
            this.mPreviousPage = this.mCurrentPage;
            if (withAnimation) {
                this.mHideAnimator.setTarget(this.mDots[this.mPreviousPage]);
                this.mShowAnimator.setTarget(this.mDots[pageIndex]);
                this.mAnimator.start();
            }
            setSelectedPage(pageIndex);
        }
    }

    private void calculateDotPositions() {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int requiredWidth = getRequiredWidth();
        int mid = (left + right) / 2;
        this.mDotSelectedX = new int[this.mPageCount];
        this.mDotSelectedPrevX = new int[this.mPageCount];
        this.mDotSelectedNextX = new int[this.mPageCount];
        int i = 1;
        int startRight;
        int i2;
        if (!this.mIsLtr) {
            startRight = (requiredWidth / 2) + mid;
            this.mDotSelectedX[0] = ((startRight - this.mDotRadius) + this.mDotGap) - this.mArrowGap;
            this.mDotSelectedPrevX[0] = startRight - this.mDotRadius;
            this.mDotSelectedNextX[0] = ((startRight - this.mDotRadius) + (this.mDotGap * 2)) - (2 * this.mArrowGap);
            while (true) {
                i2 = i;
                if (i2 >= this.mPageCount) {
                    break;
                }
                this.mDotSelectedX[i2] = this.mDotSelectedPrevX[i2 - 1] - this.mArrowGap;
                this.mDotSelectedPrevX[i2] = this.mDotSelectedPrevX[i2 - 1] - this.mDotGap;
                this.mDotSelectedNextX[i2] = this.mDotSelectedX[i2 - 1] - this.mArrowGap;
                i = i2 + 1;
            }
        } else {
            startRight = mid - (requiredWidth / 2);
            this.mDotSelectedX[0] = ((this.mDotRadius + startRight) - this.mDotGap) + this.mArrowGap;
            this.mDotSelectedPrevX[0] = this.mDotRadius + startRight;
            this.mDotSelectedNextX[0] = ((this.mDotRadius + startRight) - (this.mDotGap * 2)) + (2 * this.mArrowGap);
            while (true) {
                i2 = i;
                if (i2 >= this.mPageCount) {
                    break;
                }
                this.mDotSelectedX[i2] = this.mDotSelectedPrevX[i2 - 1] + this.mArrowGap;
                this.mDotSelectedPrevX[i2] = this.mDotSelectedPrevX[i2 - 1] + this.mDotGap;
                this.mDotSelectedNextX[i2] = this.mDotSelectedX[i2 - 1] + this.mArrowGap;
                i = i2 + 1;
            }
        }
        this.mDotCenterY = this.mArrowRadius + top;
        adjustDotPosition();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int getPageCount() {
        return this.mPageCount;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int[] getDotSelectedX() {
        return this.mDotSelectedX;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int[] getDotSelectedLeftX() {
        return this.mDotSelectedPrevX;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public int[] getDotSelectedRightX() {
        return this.mDotSelectedNextX;
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
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        setMeasuredDimension(width, height);
        calculateDotPositions();
    }

    private int getDesiredHeight() {
        return ((getPaddingTop() + this.mArrowDiameter) + getPaddingBottom()) + this.mShadowRadius;
    }

    private int getRequiredWidth() {
        return ((this.mDotRadius * 2) + (2 * this.mArrowGap)) + ((this.mPageCount - 3) * this.mDotGap);
    }

    private int getDesiredWidth() {
        return (getPaddingLeft() + getRequiredWidth()) + getPaddingRight();
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < this.mPageCount; i++) {
            this.mDots[i].draw(canvas);
        }
    }

    private void setSelectedPage(int now) {
        if (now != this.mCurrentPage) {
            this.mCurrentPage = now;
            adjustDotPosition();
        }
    }

    private void adjustDotPosition() {
        float f;
        int i = 0;
        while (true) {
            f = -1.0f;
            if (i >= this.mCurrentPage) {
                break;
            }
            this.mDots[i].deselect();
            Dot dot = this.mDots[i];
            if (i != this.mPreviousPage) {
                f = 1.0f;
            }
            dot.mDirection = f;
            this.mDots[i].mCenterX = (float) this.mDotSelectedPrevX[i];
            i++;
        }
        this.mDots[this.mCurrentPage].select();
        Dot dot2 = this.mDots[this.mCurrentPage];
        if (this.mPreviousPage >= this.mCurrentPage) {
            f = 1.0f;
        }
        dot2.mDirection = f;
        this.mDots[this.mCurrentPage].mCenterX = (float) this.mDotSelectedX[this.mCurrentPage];
        i = this.mCurrentPage;
        while (true) {
            i++;
            if (i < this.mPageCount) {
                this.mDots[i].deselect();
                this.mDots[i].mDirection = 1.0f;
                this.mDots[i].mCenterX = (float) this.mDotSelectedNextX[i];
            } else {
                return;
            }
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        int i = 0;
        boolean isLtr = layoutDirection == 0;
        if (this.mIsLtr != isLtr) {
            this.mIsLtr = isLtr;
            this.mArrow = loadArrow();
            if (this.mDots != null) {
                Dot[] dotArr = this.mDots;
                int length = dotArr.length;
                while (i < length) {
                    dotArr[i].onRtlPropertiesChanged();
                    i++;
                }
            }
            calculateDotPositions();
            invalidate();
        }
    }
}
