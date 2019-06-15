package com.android.settings.display;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import com.android.settings.R;
import com.google.common.primitives.Ints;

public class MessageBubbleBackground extends LinearLayout {
    private final int mSnapWidthPixels;

    public MessageBubbleBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSnapWidthPixels = context.getResources().getDimensionPixelSize(R.dimen.conversation_bubble_width_snap);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthPadding = getPaddingLeft() + getPaddingRight();
        super.onMeasure(MeasureSpec.makeMeasureSpec(Math.min(MeasureSpec.getSize(widthMeasureSpec) - widthPadding, (int) (Math.ceil((double) (((float) (getMeasuredWidth() - widthPadding)) / ((float) this.mSnapWidthPixels))) * ((double) this.mSnapWidthPixels))) + widthPadding, Ints.MAX_POWER_OF_TWO), heightMeasureSpec);
    }
}
