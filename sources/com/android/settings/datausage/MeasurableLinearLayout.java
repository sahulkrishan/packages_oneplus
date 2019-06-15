package com.android.settings.datausage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class MeasurableLinearLayout extends LinearLayout {
    private View mDisposableView;
    private View mFixedView;

    public MeasurableLinearLayout(Context context) {
        super(context, null);
    }

    public MeasurableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MeasurableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    public MeasurableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mDisposableView != null && getMeasuredWidth() - this.mFixedView.getMeasuredWidth() < this.mDisposableView.getMeasuredWidth()) {
            this.mDisposableView.setVisibility(8);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else if (this.mDisposableView != null && this.mDisposableView.getVisibility() != 0) {
            this.mDisposableView.setVisibility(0);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setChildren(View fixedView, View disposableView) {
        this.mFixedView = fixedView;
        this.mDisposableView = disposableView;
    }
}
