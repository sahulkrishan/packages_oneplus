package com.android.settings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import com.android.settings.R;

public class RingProgressBar extends ProgressBar {
    public RingProgressBar(Context context) {
        this(context, null);
    }

    public RingProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.RingProgressBarStyle);
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* Access modifiers changed, original: protected|declared_synchronized */
    public synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int shortSide = Math.min(getMeasuredHeight(), getMeasuredWidth());
        setMeasuredDimension(shortSide, shortSide);
    }
}
