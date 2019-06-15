package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public final class AspectRatioFrameLayout extends FrameLayout {
    private static final float ASPECT_RATIO_CHANGE_THREASHOLD = 0.01f;
    @VisibleForTesting
    float mAspectRatio;

    public AspectRatioFrameLayout(Context context) {
        this(context, null);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAspectRatio = 1.0f;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioFrameLayout);
            this.mAspectRatio = array.getFloat(0, 1.0f);
            array.recycle();
        }
    }

    public void setAspectRatio(float aspectRadio) {
        this.mAspectRatio = aspectRadio;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if (width != 0 && height != 0) {
            if (Math.abs(this.mAspectRatio - (((float) width) / ((float) height))) > ASPECT_RATIO_CHANGE_THREASHOLD) {
                super.onMeasure(MeasureSpec.makeMeasureSpec((int) (((float) height) * this.mAspectRatio), Ints.MAX_POWER_OF_TWO), MeasureSpec.makeMeasureSpec(height, Ints.MAX_POWER_OF_TWO));
            }
        }
    }
}
