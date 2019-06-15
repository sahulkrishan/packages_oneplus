package com.oneplus.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import com.google.common.primitives.Ints;
import com.oneplus.commonctrl.R;

public class OPButtonBarLayout extends LinearLayout {
    private boolean mAllowStacking;
    private int mLastWidthSize = -1;

    public OPButtonBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.OPButtonBarLayout);
        this.mAllowStacking = ta.getBoolean(R.styleable.OPButtonBarLayout_op_allowStacking, true);
        ta.recycle();
    }

    public void setAllowStacking(boolean allowStacking) {
        if (this.mAllowStacking != allowStacking) {
            this.mAllowStacking = allowStacking;
            if (!this.mAllowStacking && getOrientation() == 1) {
                setStacked(false);
            }
            requestLayout();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int initialWidthMeasureSpec;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (this.mAllowStacking) {
            if (widthSize > this.mLastWidthSize && isStacked()) {
                setStacked(false);
            }
            this.mLastWidthSize = widthSize;
        }
        boolean needsRemeasure = false;
        if (isStacked() || MeasureSpec.getMode(widthMeasureSpec) != Ints.MAX_POWER_OF_TWO) {
            initialWidthMeasureSpec = widthMeasureSpec;
        } else {
            initialWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, Integer.MIN_VALUE);
            needsRemeasure = true;
        }
        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec);
        if (this.mAllowStacking && !isStacked() && (ViewCompat.MEASURED_STATE_MASK & getMeasuredWidthAndState()) == 16777216) {
            setStacked(true);
            needsRemeasure = true;
        }
        if (needsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void setStacked(boolean stacked) {
        setOrientation(stacked);
        setGravity(stacked ? 5 : 80);
        View spacer = findViewById(R.id.spacer);
        int i = 4;
        if (spacer != null) {
            spacer.setVisibility(4);
        }
        View spacer2 = findViewById(R.id.spacer2);
        if (spacer2 != null) {
            if (stacked) {
                i = 8;
            }
            spacer2.setVisibility(i);
        }
    }

    private boolean isStacked() {
        return getOrientation() == 1;
    }
}
