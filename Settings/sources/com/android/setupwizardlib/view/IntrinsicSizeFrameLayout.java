package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.android.setupwizardlib.R;
import com.google.common.primitives.Ints;

public class IntrinsicSizeFrameLayout extends FrameLayout {
    private int mIntrinsicHeight = 0;
    private int mIntrinsicWidth = 0;

    public IntrinsicSizeFrameLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public IntrinsicSizeFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    @TargetApi(11)
    public IntrinsicSizeFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwIntrinsicSizeFrameLayout, defStyleAttr, 0);
        this.mIntrinsicHeight = a.getDimensionPixelSize(R.styleable.SuwIntrinsicSizeFrameLayout_android_height, 0);
        this.mIntrinsicWidth = a.getDimensionPixelSize(R.styleable.SuwIntrinsicSizeFrameLayout_android_width, 0);
        a.recycle();
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(getIntrinsicMeasureSpec(widthMeasureSpec, this.mIntrinsicWidth), getIntrinsicMeasureSpec(heightMeasureSpec, this.mIntrinsicHeight));
    }

    private int getIntrinsicMeasureSpec(int measureSpec, int intrinsicSize) {
        if (intrinsicSize <= 0) {
            return measureSpec;
        }
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == 0) {
            return MeasureSpec.makeMeasureSpec(this.mIntrinsicHeight, Ints.MAX_POWER_OF_TWO);
        }
        if (mode == Integer.MIN_VALUE) {
            return MeasureSpec.makeMeasureSpec(Math.min(size, this.mIntrinsicHeight), Ints.MAX_POWER_OF_TWO);
        }
        return measureSpec;
    }
}
