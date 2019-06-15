package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

@RestrictTo({Scope.LIBRARY_GROUP})
public class ScaleFrameLayout extends FrameLayout {
    private static final int DEFAULT_CHILD_GRAVITY = 8388659;
    private float mChildScale;
    private float mLayoutScaleX;
    private float mLayoutScaleY;

    public ScaleFrameLayout(Context context) {
        this(context, null);
    }

    public ScaleFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLayoutScaleX = 1.0f;
        this.mLayoutScaleY = 1.0f;
        this.mChildScale = 1.0f;
    }

    public void setLayoutScaleX(float scaleX) {
        if (scaleX != this.mLayoutScaleX) {
            this.mLayoutScaleX = scaleX;
            requestLayout();
        }
    }

    public void setLayoutScaleY(float scaleY) {
        if (scaleY != this.mLayoutScaleY) {
            this.mLayoutScaleY = scaleY;
            requestLayout();
        }
    }

    public void setChildScale(float scale) {
        if (this.mChildScale != scale) {
            this.mChildScale = scale;
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setScaleX(scale);
                getChildAt(i).setScaleY(scale);
            }
        }
    }

    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        child.setScaleX(this.mChildScale);
        child.setScaleY(this.mChildScale);
    }

    /* Access modifiers changed, original: protected */
    public boolean addViewInLayout(View child, int index, LayoutParams params, boolean preventRequestLayout) {
        boolean ret = super.addViewInLayout(child, index, params, preventRequestLayout);
        if (ret) {
            child.setScaleX(this.mChildScale);
            child.setScaleY(this.mChildScale);
        }
        return ret;
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float pivotX;
        int parentLeft;
        int parentRight;
        int parentTop;
        int parentBottom;
        ScaleFrameLayout scaleFrameLayout = this;
        int count = getChildCount();
        int layoutDirection = getLayoutDirection();
        if (layoutDirection == 1) {
            pivotX = ((float) getWidth()) - getPivotX();
        } else {
            pivotX = getPivotX();
        }
        if (scaleFrameLayout.mLayoutScaleX != 1.0f) {
            parentLeft = getPaddingLeft() + ((int) ((pivotX - (pivotX / scaleFrameLayout.mLayoutScaleX)) + 0.5f));
            parentRight = ((int) ((((((float) (right - left)) - pivotX) / scaleFrameLayout.mLayoutScaleX) + pivotX) + 0.5f)) - getPaddingRight();
        } else {
            parentLeft = getPaddingLeft();
            parentRight = (right - left) - getPaddingRight();
        }
        float pivotY = getPivotY();
        if (scaleFrameLayout.mLayoutScaleY != 1.0f) {
            parentTop = getPaddingTop() + ((int) ((pivotY - (pivotY / scaleFrameLayout.mLayoutScaleY)) + 0.5f));
            parentBottom = ((int) ((((((float) (bottom - top)) - pivotY) / scaleFrameLayout.mLayoutScaleY) + pivotY) + 0.5f)) - getPaddingBottom();
        } else {
            parentTop = getPaddingTop();
            parentBottom = (bottom - top) - getPaddingBottom();
        }
        int i = 0;
        while (i < count) {
            View child = scaleFrameLayout.getChildAt(i);
            if (child.getVisibility() != 8) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY;
                }
                int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                int verticalGravity = gravity & 112;
                gravity = absoluteGravity & 7;
                if (gravity == 1) {
                    gravity = (((((parentRight - parentLeft) - width) / 2) + parentLeft) + lp.leftMargin) - lp.rightMargin;
                } else if (gravity != 5) {
                    gravity = lp.leftMargin + parentLeft;
                } else {
                    gravity = (parentRight - width) - lp.rightMargin;
                }
                if (verticalGravity == 16) {
                    absoluteGravity = (((((parentBottom - parentTop) - height) / 2) + parentTop) + lp.topMargin) - lp.bottomMargin;
                } else if (verticalGravity == 48) {
                    absoluteGravity = lp.topMargin + parentTop;
                } else if (verticalGravity != 80) {
                    absoluteGravity = lp.topMargin + parentTop;
                    int i2 = verticalGravity;
                } else {
                    absoluteGravity = (parentBottom - height) - lp.bottomMargin;
                }
                child.layout(gravity, absoluteGravity, gravity + width, absoluteGravity + height);
                child.setPivotX(pivotX - ((float) gravity));
                child.setPivotY(pivotY - ((float) absoluteGravity));
            }
            i++;
            scaleFrameLayout = this;
        }
    }

    private static int getScaledMeasureSpec(int measureSpec, float scale) {
        return scale == 1.0f ? measureSpec : MeasureSpec.makeMeasureSpec((int) ((((float) MeasureSpec.getSize(measureSpec)) / scale) + 0.5f), MeasureSpec.getMode(measureSpec));
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mLayoutScaleX == 1.0f && this.mLayoutScaleY == 1.0f) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(getScaledMeasureSpec(widthMeasureSpec, this.mLayoutScaleX), getScaledMeasureSpec(heightMeasureSpec, this.mLayoutScaleY));
        setMeasuredDimension((int) ((((float) getMeasuredWidth()) * this.mLayoutScaleX) + 0.5f), (int) ((((float) getMeasuredHeight()) * this.mLayoutScaleY) + 0.5f));
    }

    public void setForeground(Drawable d) {
        throw new UnsupportedOperationException();
    }
}
