package com.oneplus.lib.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.Build.VERSION;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.google.common.primitives.Ints;
import java.lang.reflect.Field;

public class OPListViewCompat extends ListView {
    public static final int INVALID_POSITION = -1;
    public static final int NO_POSITION = -1;
    private static final int[] STATE_SET_NOTHING = new int[]{0};
    private Field mIsChildViewEnabled;
    protected int mMotionPosition;
    int mSelectionBottomPadding;
    int mSelectionLeftPadding;
    int mSelectionRightPadding;
    int mSelectionTopPadding;
    private GateKeeperDrawable mSelector;
    final Rect mSelectorRect;

    private static class GateKeeperDrawable extends DrawableWrapper {
        private boolean mEnabled = true;

        public GateKeeperDrawable(Drawable drawable) {
            super(drawable);
        }

        /* Access modifiers changed, original: 0000 */
        public void setEnabled(boolean enabled) {
            this.mEnabled = enabled;
        }

        public boolean setState(int[] stateSet) {
            if (this.mEnabled) {
                return super.setState(stateSet);
            }
            return false;
        }

        public void draw(Canvas canvas) {
            if (this.mEnabled) {
                super.draw(canvas);
            }
        }

        public void setHotspot(float x, float y) {
            if (this.mEnabled) {
                super.setHotspot(x, y);
            }
        }

        public void setHotspotBounds(int left, int top, int right, int bottom) {
            if (this.mEnabled) {
                super.setHotspotBounds(left, top, right, bottom);
            }
        }

        public boolean setVisible(boolean visible, boolean restart) {
            if (this.mEnabled) {
                return super.setVisible(visible, restart);
            }
            return false;
        }
    }

    public OPListViewCompat(Context context) {
        this(context, null);
    }

    public OPListViewCompat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OPListViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mSelectorRect = new Rect();
        this.mSelectionLeftPadding = 0;
        this.mSelectionTopPadding = 0;
        this.mSelectionRightPadding = 0;
        this.mSelectionBottomPadding = 0;
        try {
            this.mIsChildViewEnabled = AbsListView.class.getDeclaredField("mIsChildViewEnabled");
            this.mIsChildViewEnabled.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void setSelector(Drawable sel) {
        this.mSelector = sel != null ? new GateKeeperDrawable(sel) : null;
        if (VERSION.SDK_INT > 21) {
            super.setSelector(sel);
        } else {
            super.setSelector(this.mSelector);
        }
        Rect padding = new Rect();
        if (sel != null) {
            sel.getPadding(padding);
        }
        this.mSelectionLeftPadding = padding.left;
        this.mSelectionTopPadding = padding.top;
        this.mSelectionRightPadding = padding.right;
        this.mSelectionBottomPadding = padding.bottom;
    }

    /* Access modifiers changed, original: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        setSelectorEnabled(true);
        updateSelectorStateCompat();
    }

    /* Access modifiers changed, original: protected */
    public void dispatchDraw(Canvas canvas) {
        drawSelectorCompat(canvas);
        super.dispatchDraw(canvas);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            this.mMotionPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
        }
        return super.onTouchEvent(ev);
    }

    /* Access modifiers changed, original: protected */
    public void updateSelectorStateCompat() {
        Drawable selector = getSelector();
        if (selector != null && shouldShowSelectorCompat()) {
            selector.setState(getDrawableState());
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldShowSelectorCompat() {
        return touchModeDrawsInPressedStateCompat() && isPressed();
    }

    /* Access modifiers changed, original: protected */
    public boolean touchModeDrawsInPressedStateCompat() {
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void drawSelectorCompat(Canvas canvas) {
        if (!this.mSelectorRect.isEmpty()) {
            Drawable selector = getSelector();
            if (selector != null) {
                selector.setBounds(this.mSelectorRect);
                selector.draw(canvas);
            }
        }
    }

    public int lookForSelectablePosition(int position, boolean lookDown) {
        ListAdapter adapter = getAdapter();
        if (adapter == null || isInTouchMode()) {
            return -1;
        }
        int count = adapter.getCount();
        if (!getAdapter().areAllItemsEnabled()) {
            if (lookDown) {
                position = Math.max(0, position);
                while (position < count && !adapter.isEnabled(position)) {
                    position++;
                }
            } else {
                position = Math.min(position, count - 1);
                while (position >= 0 && !adapter.isEnabled(position)) {
                    position--;
                }
            }
            if (position < 0 || position >= count) {
                return -1;
            }
            return position;
        } else if (position < 0 || position >= count) {
            return -1;
        } else {
            return position;
        }
    }

    /* Access modifiers changed, original: protected */
    public void positionSelectorLikeTouchCompat(int position, View sel, float x, float y) {
        positionSelectorLikeFocusCompat(position, sel);
        Drawable selector = getSelector();
        if (selector != null && position != -1) {
            DrawableCompat.setHotspot(selector, x, y);
        }
    }

    /* Access modifiers changed, original: protected */
    public void positionSelectorLikeFocusCompat(int position, View sel) {
        Drawable selector = getSelector();
        boolean z = true;
        boolean manageState = (selector == null || position == -1) ? false : true;
        if (manageState) {
            selector.setVisible(false, false);
        }
        positionSelectorCompat(position, sel);
        if (manageState) {
            Rect bounds = this.mSelectorRect;
            float x = bounds.exactCenterX();
            float y = bounds.exactCenterY();
            if (getVisibility() != 0) {
                z = false;
            }
            selector.setVisible(z, false);
            DrawableCompat.setHotspot(selector, x, y);
        }
    }

    /* Access modifiers changed, original: protected */
    public void positionSelectorCompat(int position, View sel) {
        Rect selectorRect = this.mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        selectorRect.left -= this.mSelectionLeftPadding;
        selectorRect.top -= this.mSelectionTopPadding;
        selectorRect.right += this.mSelectionRightPadding;
        selectorRect.bottom += this.mSelectionBottomPadding;
        try {
            boolean isChildViewEnabled = this.mIsChildViewEnabled.getBoolean(this);
            if (sel.isEnabled() != isChildViewEnabled) {
                this.mIsChildViewEnabled.set(this, Boolean.valueOf(!isChildViewEnabled));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (position != -1) {
            refreshDrawableState();
        }
    }

    public int measureHeightOfChildrenCompat(int widthMeasureSpec, int startPosition, int endPosition, int maxHeight, int disallowPartialChildPosition) {
        int i = maxHeight;
        int i2 = disallowPartialChildPosition;
        int paddingTop = getListPaddingTop();
        int paddingBottom = getListPaddingBottom();
        int paddingLeft = getListPaddingLeft();
        int paddingRight = getListPaddingRight();
        int reportedDividerHeight = getDividerHeight();
        Drawable divider = getDivider();
        ListAdapter adapter = getAdapter();
        if (adapter == null) {
            return paddingTop + paddingBottom;
        }
        int paddingBottom2;
        int returnedHeight = paddingTop + paddingBottom;
        int dividerHeight = (reportedDividerHeight <= 0 || divider == null) ? 0 : reportedDividerHeight;
        View child = null;
        int viewType = 0;
        int count = adapter.getCount();
        int prevHeightWithoutPartialChild = 0;
        int returnedHeight2 = returnedHeight;
        returnedHeight = 0;
        while (returnedHeight < count) {
            LayoutParams childLp;
            int newType = adapter.getItemViewType(returnedHeight);
            if (newType != viewType) {
                child = null;
                viewType = newType;
            }
            int paddingTop2 = paddingTop;
            child = adapter.getView(returnedHeight, child, this);
            LayoutParams childLp2 = child.getLayoutParams();
            if (childLp2 == null) {
                paddingBottom2 = paddingBottom;
                childLp = generateDefaultLayoutParams();
                child.setLayoutParams(childLp);
            } else {
                paddingBottom2 = paddingBottom;
                childLp = childLp2;
            }
            if (childLp.height > 0) {
                paddingBottom = MeasureSpec.makeMeasureSpec(childLp.height, Ints.MAX_POWER_OF_TWO);
            } else {
                paddingBottom = MeasureSpec.makeMeasureSpec(0, 0);
            }
            child.measure(widthMeasureSpec, paddingBottom);
            child.forceLayout();
            if (returnedHeight > 0) {
                returnedHeight2 += dividerHeight;
            }
            returnedHeight2 += child.getMeasuredHeight();
            if (returnedHeight2 >= i) {
                int i3 = (i2 < 0 || returnedHeight <= i2 || prevHeightWithoutPartialChild <= 0 || returnedHeight2 == i) ? i : prevHeightWithoutPartialChild;
                return i3;
            }
            if (i2 >= 0 && returnedHeight >= i2) {
                prevHeightWithoutPartialChild = returnedHeight2;
            }
            returnedHeight++;
            paddingTop = paddingTop2;
            paddingBottom = paddingBottom2;
        }
        paddingBottom2 = paddingBottom;
        paddingTop = widthMeasureSpec;
        return returnedHeight2;
    }

    /* Access modifiers changed, original: protected */
    public void setSelectorEnabled(boolean enabled) {
        if (this.mSelector != null) {
            this.mSelector.setEnabled(enabled);
        }
    }
}
