package com.android.setupwizardlib.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private boolean mChecked = false;

    public CheckableLinearLayout(Context context) {
        super(context);
        setFocusable(true);
    }

    public CheckableLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
    }

    @TargetApi(11)
    public CheckableLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
    }

    @TargetApi(21)
    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFocusable(true);
    }

    /* Access modifiers changed, original: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        if (!this.mChecked) {
            return super.onCreateDrawableState(extraSpace);
        }
        return mergeDrawableStates(super.onCreateDrawableState(extraSpace + 1), new int[]{16842912});
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
        refreshDrawableState();
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public void toggle() {
        setChecked(isChecked() ^ 1);
    }
}
