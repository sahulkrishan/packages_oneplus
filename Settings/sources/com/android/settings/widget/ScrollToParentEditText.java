package com.android.settings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

public class ScrollToParentEditText extends ImeAwareEditText {
    private Rect mRect = new Rect();

    public ScrollToParentEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        ViewParent parent = getParent();
        if (!(parent instanceof View)) {
            return super.requestRectangleOnScreen(rectangle, immediate);
        }
        ((View) parent).getDrawingRect(this.mRect);
        return ((View) parent).requestRectangleOnScreen(this.mRect, immediate);
    }
}
