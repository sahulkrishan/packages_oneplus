package com.oneplus.lib.design.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import com.google.common.primitives.Ints;
import com.oneplus.lib.design.widget.CoordinatorLayout.LayoutParams;
import com.oneplus.lib.util.MathUtils;
import java.util.List;

abstract class HeaderScrollingViewBehavior extends ViewOffsetBehavior<View> {
    private int mOverlayTop;
    final Rect mTempRect1 = new Rect();
    final Rect mTempRect2 = new Rect();
    private int mVerticalLayoutGap = 0;

    public abstract View findFirstDependency(List<View> list);

    public HeaderScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        View view;
        int childLpHeight = child.getLayoutParams().height;
        if (childLpHeight == -1 || childLpHeight == -2) {
            View header = findFirstDependency(parent.getDependencies(child));
            if (header != null) {
                int heightMeasureSpec;
                if (!ViewCompat.getFitsSystemWindows(header) || ViewCompat.getFitsSystemWindows(child)) {
                    view = child;
                } else {
                    view = child;
                    ViewCompat.setFitsSystemWindows(view, true);
                    if (ViewCompat.getFitsSystemWindows(child)) {
                        child.requestLayout();
                        return true;
                    }
                }
                int availableHeight = MeasureSpec.getSize(parentHeightMeasureSpec);
                if (availableHeight == 0) {
                    availableHeight = parent.getHeight();
                }
                int height = (availableHeight - header.getMeasuredHeight()) + getScrollRange(header);
                if (childLpHeight == -1) {
                    heightMeasureSpec = Ints.MAX_POWER_OF_TWO;
                } else {
                    heightMeasureSpec = Integer.MIN_VALUE;
                }
                parent.onMeasureChild(view, parentWidthMeasureSpec, widthUsed, MeasureSpec.makeMeasureSpec(height, heightMeasureSpec), heightUsed);
                return true;
            }
        }
        view = child;
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void layoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        View header = findFirstDependency(parent.getDependencies(child));
        if (header != null) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            Rect available = this.mTempRect1;
            available.set(parent.getPaddingLeft() + lp.leftMargin, header.getBottom() + lp.topMargin, (parent.getWidth() - parent.getPaddingRight()) - lp.rightMargin, ((parent.getHeight() + header.getBottom()) - parent.getPaddingBottom()) - lp.bottomMargin);
            WindowInsetsCompat parentInsets = parent.getLastWindowInsets();
            if (!(parentInsets == null || !ViewCompat.getFitsSystemWindows(parent) || ViewCompat.getFitsSystemWindows(child))) {
                available.left += parentInsets.getSystemWindowInsetLeft();
                available.right -= parentInsets.getSystemWindowInsetRight();
            }
            Rect out = this.mTempRect2;
            GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(), child.getMeasuredHeight(), available, out, layoutDirection);
            int overlap = getOverlapPixelsForOffset(header);
            child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap);
            this.mVerticalLayoutGap = out.top - header.getBottom();
            return;
        }
        super.layoutChild(parent, child, layoutDirection);
        this.mVerticalLayoutGap = 0;
    }

    /* Access modifiers changed, original: 0000 */
    public float getOverlapRatioForOffset(View header) {
        return 1.0f;
    }

    /* Access modifiers changed, original: final */
    public final int getOverlapPixelsForOffset(View header) {
        return this.mOverlayTop == 0 ? 0 : MathUtils.constrain((int) (getOverlapRatioForOffset(header) * ((float) this.mOverlayTop)), 0, this.mOverlayTop);
    }

    private static int resolveGravity(int gravity) {
        return gravity == 0 ? 8388659 : gravity;
    }

    /* Access modifiers changed, original: 0000 */
    public int getScrollRange(View v) {
        return v.getMeasuredHeight();
    }

    /* Access modifiers changed, original: final */
    public final int getVerticalLayoutGap() {
        return this.mVerticalLayoutGap;
    }

    public final void setOverlayTop(int overlayTop) {
        this.mOverlayTop = overlayTop;
    }

    public final int getOverlayTop() {
        return this.mOverlayTop;
    }
}
