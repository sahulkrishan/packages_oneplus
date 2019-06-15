package com.android.setupwizardlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import com.android.setupwizardlib.R;
import com.google.common.primitives.Ints;

public class FillContentLayout extends FrameLayout {
    private int mMaxHeight;
    private int mMaxWidth;

    public FillContentLayout(Context context) {
        this(context, null);
    }

    public FillContentLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.suwFillContentLayoutStyle);
    }

    public FillContentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwFillContentLayout, defStyleAttr, 0);
        this.mMaxHeight = a.getDimensionPixelSize(R.styleable.SuwFillContentLayout_android_maxHeight, -1);
        this.mMaxWidth = a.getDimensionPixelSize(R.styleable.SuwFillContentLayout_android_maxWidth, -1);
        a.recycle();
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            measureIllustrationChild(getChildAt(i), getMeasuredWidth(), getMeasuredHeight());
        }
    }

    private void measureIllustrationChild(View child, int parentWidth, int parentHeight) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        child.measure(getMaxSizeMeasureSpec(Math.min(this.mMaxWidth, parentWidth), ((getPaddingLeft() + getPaddingRight()) + lp.leftMargin) + lp.rightMargin, lp.width), getMaxSizeMeasureSpec(Math.min(this.mMaxHeight, parentHeight), ((getPaddingTop() + getPaddingBottom()) + lp.topMargin) + lp.bottomMargin, lp.height));
    }

    private static int getMaxSizeMeasureSpec(int maxSize, int padding, int childDimension) {
        int size = Math.max(0, maxSize - padding);
        if (childDimension >= 0) {
            return MeasureSpec.makeMeasureSpec(childDimension, Ints.MAX_POWER_OF_TWO);
        }
        if (childDimension == -1) {
            return MeasureSpec.makeMeasureSpec(size, Ints.MAX_POWER_OF_TWO);
        }
        if (childDimension == -2) {
            return MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE);
        }
        return 0;
    }
}
