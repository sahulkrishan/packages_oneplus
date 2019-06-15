package com.android.settings.graph;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public class BottomLabelLayout extends LinearLayout {
    private static final String TAG = "BottomLabelLayout";

    public BottomLabelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int initialWidthMeasureSpec;
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        boolean isStacked = isStacked();
        boolean needsRemeasure = false;
        if (isStacked || MeasureSpec.getMode(widthMeasureSpec) != Ints.MAX_POWER_OF_TWO) {
            initialWidthMeasureSpec = widthMeasureSpec;
        } else {
            initialWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, Integer.MIN_VALUE);
            needsRemeasure = true;
        }
        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec);
        if (!isStacked && (ViewCompat.MEASURED_STATE_MASK & getMeasuredWidthAndState()) == 16777216) {
            setStacked(true);
            needsRemeasure = true;
        }
        if (needsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setStacked(boolean stacked) {
        setOrientation(stacked);
        setGravity(stacked ? GravityCompat.START : 80);
        View spacer = findViewById(R.id.spacer);
        if (spacer != null) {
            spacer.setVisibility(stacked ? 8 : 0);
        }
    }

    private boolean isStacked() {
        return getOrientation() == 1;
    }
}
