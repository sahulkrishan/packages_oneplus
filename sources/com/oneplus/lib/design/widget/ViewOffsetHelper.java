package com.oneplus.lib.design.widget;

import android.view.View;

class ViewOffsetHelper {
    private int mLayoutLeft;
    private int mLayoutTop;
    private int mOffsetLeft;
    private int mOffsetTop;
    private final View mView;

    public ViewOffsetHelper(View view) {
        this.mView = view;
    }

    public void onViewLayout() {
        this.mLayoutTop = this.mView.getTop();
        this.mLayoutLeft = this.mView.getLeft();
        updateOffsets();
    }

    private void updateOffsets() {
        this.mView.offsetTopAndBottom(this.mOffsetTop - (this.mView.getTop() - this.mLayoutTop));
        this.mView.offsetLeftAndRight(this.mOffsetLeft - (this.mView.getLeft() - this.mLayoutLeft));
    }

    public boolean setTopAndBottomOffset(int offset) {
        if (this.mOffsetTop == offset) {
            return false;
        }
        this.mOffsetTop = offset;
        updateOffsets();
        return true;
    }

    public boolean setLeftAndRightOffset(int offset) {
        if (this.mOffsetLeft == offset) {
            return false;
        }
        this.mOffsetLeft = offset;
        updateOffsets();
        return true;
    }

    public int getTopAndBottomOffset() {
        return this.mOffsetTop;
    }

    public int getLeftAndRightOffset() {
        return this.mOffsetLeft;
    }

    public int getLayoutTop() {
        return this.mLayoutTop;
    }

    public int getLayoutLeft() {
        return this.mLayoutLeft;
    }
}
