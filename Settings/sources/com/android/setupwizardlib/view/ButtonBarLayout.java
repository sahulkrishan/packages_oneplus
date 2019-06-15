package com.android.setupwizardlib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.android.setupwizardlib.R;
import com.google.common.primitives.Ints;

public class ButtonBarLayout extends LinearLayout {
    private int mOriginalPaddingLeft;
    private int mOriginalPaddingRight;
    private boolean mStacked = false;

    public ButtonBarLayout(Context context) {
        super(context);
    }

    public ButtonBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        setStacked(false);
        boolean needsRemeasure = false;
        int initialWidthMeasureSpec = widthMeasureSpec;
        if (MeasureSpec.getMode(widthMeasureSpec) == Ints.MAX_POWER_OF_TWO) {
            initialWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
            needsRemeasure = true;
        }
        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() > widthSize) {
            setStacked(true);
            needsRemeasure = true;
        }
        if (needsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void setStacked(boolean stacked) {
        if (this.mStacked != stacked) {
            int i;
            this.mStacked = stacked;
            int childCount = getChildCount();
            for (i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                LayoutParams childParams = (LayoutParams) child.getLayoutParams();
                if (stacked) {
                    child.setTag(R.id.suw_original_weight, Float.valueOf(childParams.weight));
                    childParams.weight = 0.0f;
                } else {
                    Float weight = (Float) child.getTag(R.id.suw_original_weight);
                    if (weight != null) {
                        childParams.weight = weight.floatValue();
                    }
                }
                child.setLayoutParams(childParams);
            }
            setOrientation(stacked);
            for (i = childCount - 1; i >= 0; i--) {
                bringChildToFront(getChildAt(i));
            }
            if (stacked) {
                this.mOriginalPaddingLeft = getPaddingLeft();
                this.mOriginalPaddingRight = getPaddingRight();
                i = Math.max(this.mOriginalPaddingLeft, this.mOriginalPaddingRight);
                setPadding(i, getPaddingTop(), i, getPaddingBottom());
            } else {
                setPadding(this.mOriginalPaddingLeft, getPaddingTop(), this.mOriginalPaddingRight, getPaddingBottom());
            }
        }
    }
}
