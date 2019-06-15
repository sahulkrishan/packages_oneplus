package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import java.util.ArrayList;

class ControlBar extends LinearLayout {
    private int mChildMarginFromCenter;
    boolean mDefaultFocusToMiddle = true;
    int mLastFocusIndex = -1;
    private OnChildFocusedListener mOnChildFocusedListener;

    public interface OnChildFocusedListener {
        void onChildFocusedListener(View view, View view2);
    }

    public ControlBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* Access modifiers changed, original: 0000 */
    public void setDefaultFocusToMiddle(boolean defaultFocusToMiddle) {
        this.mDefaultFocusToMiddle = defaultFocusToMiddle;
    }

    /* Access modifiers changed, original: 0000 */
    public int getDefaultFocusIndex() {
        return this.mDefaultFocusToMiddle ? getChildCount() / 2 : 0;
    }

    /* Access modifiers changed, original: protected */
    public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (getChildCount() > 0) {
            int index;
            if (this.mLastFocusIndex < 0 || this.mLastFocusIndex >= getChildCount()) {
                index = getDefaultFocusIndex();
            } else {
                index = this.mLastFocusIndex;
            }
            if (getChildAt(index).requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (direction != 33 && direction != 130) {
            super.addFocusables(views, direction, focusableMode);
        } else if (this.mLastFocusIndex >= 0 && this.mLastFocusIndex < getChildCount()) {
            views.add(getChildAt(this.mLastFocusIndex));
        } else if (getChildCount() > 0) {
            views.add(getChildAt(getDefaultFocusIndex()));
        }
    }

    public void setOnChildFocusedListener(OnChildFocusedListener listener) {
        this.mOnChildFocusedListener = listener;
    }

    public void setChildMarginFromCenter(int marginFromCenter) {
        this.mChildMarginFromCenter = marginFromCenter;
    }

    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        this.mLastFocusIndex = indexOfChild(child);
        if (this.mOnChildFocusedListener != null) {
            this.mOnChildFocusedListener.onChildFocusedListener(child, focused);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mChildMarginFromCenter > 0) {
            int totalExtraMargin = 0;
            for (int i = 0; i < getChildCount() - 1; i++) {
                View first = getChildAt(i);
                View second = getChildAt(i + 1);
                int marginStart = this.mChildMarginFromCenter - ((first.getMeasuredWidth() + second.getMeasuredWidth()) / 2);
                LayoutParams lp = (LayoutParams) second.getLayoutParams();
                int extraMargin = marginStart - lp.getMarginStart();
                lp.setMarginStart(marginStart);
                second.setLayoutParams(lp);
                totalExtraMargin += extraMargin;
            }
            setMeasuredDimension(getMeasuredWidth() + totalExtraMargin, getMeasuredHeight());
        }
    }
}
