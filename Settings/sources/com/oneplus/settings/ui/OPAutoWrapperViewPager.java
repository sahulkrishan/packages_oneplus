package com.oneplus.settings.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.google.common.primitives.Ints;

public class OPAutoWrapperViewPager extends ViewPager {
    public OPAutoWrapperViewPager(Context context) {
        super(context);
    }

    public OPAutoWrapperViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, 0));
            int h = child.getMeasuredHeight();
            if (h > height) {
                height = h;
            }
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, Ints.MAX_POWER_OF_TWO));
    }
}
