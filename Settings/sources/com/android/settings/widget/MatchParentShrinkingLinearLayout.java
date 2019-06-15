package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.FlagToString;
import android.view.ViewDebug.IntToString;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewHierarchyEncoder;
import com.android.internal.R;
import com.google.common.primitives.Ints;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MatchParentShrinkingLinearLayout extends ViewGroup {
    public static final int HORIZONTAL = 0;
    private static final int INDEX_BOTTOM = 2;
    private static final int INDEX_CENTER_VERTICAL = 0;
    private static final int INDEX_FILL = 3;
    private static final int INDEX_TOP = 1;
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    public static final int SHOW_DIVIDER_END = 4;
    public static final int SHOW_DIVIDER_MIDDLE = 2;
    public static final int SHOW_DIVIDER_NONE = 0;
    public static final int VERTICAL = 1;
    private static final int VERTICAL_GRAVITY_COUNT = 4;
    @ExportedProperty(category = "layout")
    private boolean mBaselineAligned;
    @ExportedProperty(category = "layout")
    private int mBaselineAlignedChildIndex;
    @ExportedProperty(category = "measurement")
    private int mBaselineChildTop;
    private Drawable mDivider;
    private int mDividerHeight;
    private int mDividerPadding;
    private int mDividerWidth;
    @ExportedProperty(category = "measurement", flagMapping = {@FlagToString(equals = -1, mask = -1, name = "NONE"), @FlagToString(equals = 0, mask = 0, name = "NONE"), @FlagToString(equals = 48, mask = 48, name = "TOP"), @FlagToString(equals = 80, mask = 80, name = "BOTTOM"), @FlagToString(equals = 3, mask = 3, name = "LEFT"), @FlagToString(equals = 5, mask = 5, name = "RIGHT"), @FlagToString(equals = 8388611, mask = 8388611, name = "START"), @FlagToString(equals = 8388613, mask = 8388613, name = "END"), @FlagToString(equals = 16, mask = 16, name = "CENTER_VERTICAL"), @FlagToString(equals = 112, mask = 112, name = "FILL_VERTICAL"), @FlagToString(equals = 1, mask = 1, name = "CENTER_HORIZONTAL"), @FlagToString(equals = 7, mask = 7, name = "FILL_HORIZONTAL"), @FlagToString(equals = 17, mask = 17, name = "CENTER"), @FlagToString(equals = 119, mask = 119, name = "FILL"), @FlagToString(equals = 8388608, mask = 8388608, name = "RELATIVE")}, formatToHexString = true)
    private int mGravity;
    private int mLayoutDirection;
    private int[] mMaxAscent;
    private int[] mMaxDescent;
    @ExportedProperty(category = "measurement")
    private int mOrientation;
    private int mShowDividers;
    @ExportedProperty(category = "measurement")
    private int mTotalLength;
    @ExportedProperty(category = "layout")
    private boolean mUseLargestChild;
    @ExportedProperty(category = "layout")
    private float mWeightSum;

    @Retention(RetentionPolicy.SOURCE)
    public @interface DividerMode {
    }

    public static class LayoutParams extends MarginLayoutParams {
        @ExportedProperty(category = "layout", mapping = {@IntToString(from = -1, to = "NONE"), @IntToString(from = 0, to = "NONE"), @IntToString(from = 48, to = "TOP"), @IntToString(from = 80, to = "BOTTOM"), @IntToString(from = 3, to = "LEFT"), @IntToString(from = 5, to = "RIGHT"), @IntToString(from = 8388611, to = "START"), @IntToString(from = 8388613, to = "END"), @IntToString(from = 16, to = "CENTER_VERTICAL"), @IntToString(from = 112, to = "FILL_VERTICAL"), @IntToString(from = 1, to = "CENTER_HORIZONTAL"), @IntToString(from = 7, to = "FILL_HORIZONTAL"), @IntToString(from = 17, to = "CENTER"), @IntToString(from = 119, to = "FILL")})
        public int gravity;
        @ExportedProperty(category = "layout")
        public float weight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.gravity = -1;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.LinearLayout_Layout);
            this.weight = a.getFloat(3, 0.0f);
            this.gravity = a.getInt(0, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.gravity = -1;
            this.weight = 0.0f;
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height);
            this.gravity = -1;
            this.weight = weight;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
            this.gravity = -1;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            this.gravity = -1;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = -1;
            this.weight = source.weight;
            this.gravity = source.gravity;
        }

        public String debug(String output) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(output);
            stringBuilder.append("MatchParentShrinkingLinearLayout.LayoutParams={width=");
            stringBuilder.append(sizeToString(this.width));
            stringBuilder.append(", height=");
            stringBuilder.append(sizeToString(this.height));
            stringBuilder.append(" weight=");
            stringBuilder.append(this.weight);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }

        /* Access modifiers changed, original: protected */
        public void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("layout:weight", this.weight);
            encoder.addProperty("layout:gravity", this.gravity);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface OrientationMode {
    }

    public MatchParentShrinkingLinearLayout(Context context) {
        this(context, null);
    }

    public MatchParentShrinkingLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MatchParentShrinkingLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MatchParentShrinkingLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mBaselineAligned = true;
        this.mBaselineAlignedChildIndex = -1;
        this.mBaselineChildTop = 0;
        this.mGravity = 8388659;
        this.mLayoutDirection = -1;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LinearLayout, defStyleAttr, defStyleRes);
        int index = a.getInt(1, -1);
        if (index >= 0) {
            setOrientation(index);
        }
        index = a.getInt(0, -1);
        if (index >= 0) {
            setGravity(index);
        }
        boolean baselineAligned = a.getBoolean(2, true);
        if (!baselineAligned) {
            setBaselineAligned(baselineAligned);
        }
        this.mWeightSum = a.getFloat(4, -1.0f);
        this.mBaselineAlignedChildIndex = a.getInt(3, -1);
        this.mUseLargestChild = a.getBoolean(6, false);
        setDividerDrawable(a.getDrawable(5));
        this.mShowDividers = a.getInt(7, 0);
        this.mDividerPadding = a.getDimensionPixelSize(8, 0);
        a.recycle();
    }

    public void setShowDividers(int showDividers) {
        if (showDividers != this.mShowDividers) {
            requestLayout();
        }
        this.mShowDividers = showDividers;
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public int getShowDividers() {
        return this.mShowDividers;
    }

    public Drawable getDividerDrawable() {
        return this.mDivider;
    }

    public void setDividerDrawable(Drawable divider) {
        if (divider != this.mDivider) {
            this.mDivider = divider;
            boolean z = false;
            if (divider != null) {
                this.mDividerWidth = divider.getIntrinsicWidth();
                this.mDividerHeight = divider.getIntrinsicHeight();
            } else {
                this.mDividerWidth = 0;
                this.mDividerHeight = 0;
            }
            if (divider == null) {
                z = true;
            }
            setWillNotDraw(z);
            requestLayout();
        }
    }

    public void setDividerPadding(int padding) {
        this.mDividerPadding = padding;
    }

    public int getDividerPadding() {
        return this.mDividerPadding;
    }

    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        if (this.mDivider != null) {
            if (this.mOrientation == 1) {
                drawDividersVertical(canvas);
            } else {
                drawDividersHorizontal(canvas);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void drawDividersVertical(Canvas canvas) {
        int count = getVirtualChildCount();
        int i = 0;
        while (i < count) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                drawHorizontalDivider(canvas, (child.getTop() - ((LayoutParams) child.getLayoutParams()).topMargin) - this.mDividerHeight);
            }
            i++;
        }
        if (hasDividerBeforeChildAt(count)) {
            int bottom;
            View child2 = getVirtualChildAt(count - 1);
            if (child2 == null) {
                bottom = (getHeight() - getPaddingBottom()) - this.mDividerHeight;
            } else {
                bottom = child2.getBottom() + ((LayoutParams) child2.getLayoutParams()).bottomMargin;
            }
            drawHorizontalDivider(canvas, bottom);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void drawDividersHorizontal(Canvas canvas) {
        int count = getVirtualChildCount();
        boolean isLayoutRtl = isLayoutRtl();
        int i = 0;
        while (i < count) {
            View child = getVirtualChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !hasDividerBeforeChildAt(i))) {
                int position;
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (isLayoutRtl) {
                    position = child.getRight() + lp.rightMargin;
                } else {
                    position = (child.getLeft() - lp.leftMargin) - this.mDividerWidth;
                }
                drawVerticalDivider(canvas, position);
            }
            i++;
        }
        if (hasDividerBeforeChildAt(count)) {
            int position2;
            View child2 = getVirtualChildAt(count - 1);
            if (child2 != null) {
                LayoutParams lp2 = (LayoutParams) child2.getLayoutParams();
                if (isLayoutRtl) {
                    position2 = (child2.getLeft() - lp2.leftMargin) - this.mDividerWidth;
                } else {
                    position2 = child2.getRight() + lp2.rightMargin;
                }
            } else if (isLayoutRtl) {
                position2 = getPaddingLeft();
            } else {
                position2 = (getWidth() - getPaddingRight()) - this.mDividerWidth;
            }
            drawVerticalDivider(canvas, position2);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void drawHorizontalDivider(Canvas canvas, int top) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, top, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + top);
        this.mDivider.draw(canvas);
    }

    /* Access modifiers changed, original: 0000 */
    public void drawVerticalDivider(Canvas canvas, int left) {
        this.mDivider.setBounds(left, getPaddingTop() + this.mDividerPadding, this.mDividerWidth + left, (getHeight() - getPaddingBottom()) - this.mDividerPadding);
        this.mDivider.draw(canvas);
    }

    public boolean isBaselineAligned() {
        return this.mBaselineAligned;
    }

    @RemotableViewMethod
    public void setBaselineAligned(boolean baselineAligned) {
        this.mBaselineAligned = baselineAligned;
    }

    public boolean isMeasureWithLargestChildEnabled() {
        return this.mUseLargestChild;
    }

    @RemotableViewMethod
    public void setMeasureWithLargestChildEnabled(boolean enabled) {
        this.mUseLargestChild = enabled;
    }

    public int getBaseline() {
        if (this.mBaselineAlignedChildIndex < 0) {
            return super.getBaseline();
        }
        if (getChildCount() > this.mBaselineAlignedChildIndex) {
            View child = getChildAt(this.mBaselineAlignedChildIndex);
            int childBaseline = child.getBaseline();
            if (childBaseline != -1) {
                int childTop = this.mBaselineChildTop;
                if (this.mOrientation == 1) {
                    int majorGravity = this.mGravity & 112;
                    if (majorGravity != 48) {
                        if (majorGravity == 16) {
                            childTop += ((((this.mBottom - this.mTop) - this.mPaddingTop) - this.mPaddingBottom) - this.mTotalLength) / 2;
                        } else if (majorGravity == 80) {
                            childTop = ((this.mBottom - this.mTop) - this.mPaddingBottom) - this.mTotalLength;
                        }
                    }
                }
                return (((LayoutParams) child.getLayoutParams()).topMargin + childTop) + childBaseline;
            } else if (this.mBaselineAlignedChildIndex == 0) {
                return -1;
            } else {
                throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout points to a View that doesn't know how to get its baseline.");
            }
        }
        throw new RuntimeException("mBaselineAlignedChildIndex of LinearLayout set to an index that is out of bounds.");
    }

    public int getBaselineAlignedChildIndex() {
        return this.mBaselineAlignedChildIndex;
    }

    @RemotableViewMethod
    public void setBaselineAlignedChildIndex(int i) {
        if (i < 0 || i >= getChildCount()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("base aligned child index out of range (0, ");
            stringBuilder.append(getChildCount());
            stringBuilder.append(")");
            throw new IllegalArgumentException(stringBuilder.toString());
        }
        this.mBaselineAlignedChildIndex = i;
    }

    /* Access modifiers changed, original: 0000 */
    public View getVirtualChildAt(int index) {
        return getChildAt(index);
    }

    /* Access modifiers changed, original: 0000 */
    public int getVirtualChildCount() {
        return getChildCount();
    }

    public float getWeightSum() {
        return this.mWeightSum;
    }

    @RemotableViewMethod
    public void setWeightSum(float weightSum) {
        this.mWeightSum = Math.max(0.0f, weightSum);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mOrientation == 1) {
            measureVertical(widthMeasureSpec, heightMeasureSpec);
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean hasDividerBeforeChildAt(int childIndex) {
        boolean hasVisibleViewBefore = false;
        if (childIndex == 0) {
            if ((this.mShowDividers & 1) != 0) {
                hasVisibleViewBefore = true;
            }
            return hasVisibleViewBefore;
        } else if (childIndex == getChildCount()) {
            if ((this.mShowDividers & 4) != 0) {
                hasVisibleViewBefore = true;
            }
            return hasVisibleViewBefore;
        } else if ((this.mShowDividers & 2) == 0) {
            return false;
        } else {
            hasVisibleViewBefore = false;
            for (int i = childIndex - 1; i >= 0; i--) {
                if (getChildAt(i).getVisibility() != 8) {
                    hasVisibleViewBefore = true;
                    break;
                }
            }
            return hasVisibleViewBefore;
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:200:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x048a  */
    public void measureVertical(int r59, int r60) {
        /*
        r58 = this;
        r7 = r58;
        r8 = r59;
        r9 = r60;
        r10 = 0;
        r7.mTotalLength = r10;
        r0 = 0;
        r1 = 0;
        r2 = 0;
        r3 = 0;
        r4 = 1;
        r5 = 0;
        r11 = r58.getVirtualChildCount();
        r12 = android.view.View.MeasureSpec.getMode(r59);
        r13 = android.view.View.MeasureSpec.getMode(r60);
        r6 = 0;
        r14 = 0;
        r15 = r7.mBaselineAlignedChildIndex;
        r10 = r7.mUseLargestChild;
        r17 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r18 = r6;
        r6 = r2;
        r2 = r0;
        r0 = 0;
        r57 = r4;
        r4 = r3;
        r3 = r17;
        r17 = r57;
    L_0x002f:
        r19 = r4;
        r22 = 1;
        r23 = 0;
        if (r0 >= r11) goto L_0x01be;
    L_0x0037:
        r4 = r7.getVirtualChildAt(r0);
        if (r4 != 0) goto L_0x0054;
    L_0x003d:
        r25 = r1;
        r1 = r7.mTotalLength;
        r20 = r7.measureNullChild(r0);
        r1 = r1 + r20;
        r7.mTotalLength = r1;
        r34 = r11;
        r33 = r13;
        r4 = r19;
        r1 = r25;
        goto L_0x01b2;
    L_0x0054:
        r25 = r1;
        r1 = r4.getVisibility();
        r26 = r2;
        r2 = 8;
        if (r1 != r2) goto L_0x0072;
    L_0x0060:
        r1 = r7.getChildrenSkipCount(r4, r0);
        r0 = r0 + r1;
        r34 = r11;
        r33 = r13;
        r4 = r19;
        r1 = r25;
        r2 = r26;
        goto L_0x01b2;
    L_0x0072:
        r1 = r7.hasDividerBeforeChildAt(r0);
        if (r1 == 0) goto L_0x007f;
    L_0x0078:
        r1 = r7.mTotalLength;
        r2 = r7.mDividerHeight;
        r1 = r1 + r2;
        r7.mTotalLength = r1;
    L_0x007f:
        r1 = r4.getLayoutParams();
        r2 = r1;
        r2 = (com.android.settings.widget.MatchParentShrinkingLinearLayout.LayoutParams) r2;
        r1 = r2.weight;
        r21 = r5 + r1;
        r5 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r13 != r5) goto L_0x00bd;
    L_0x008e:
        r1 = r2.height;
        if (r1 != 0) goto L_0x00bd;
    L_0x0092:
        r1 = r2.weight;
        r1 = (r1 > r23 ? 1 : (r1 == r23 ? 0 : -1));
        if (r1 <= 0) goto L_0x00bd;
    L_0x0098:
        r1 = r7.mTotalLength;
        r5 = r2.topMargin;
        r5 = r5 + r1;
        r27 = r0;
        r0 = r2.bottomMargin;
        r5 = r5 + r0;
        r0 = java.lang.Math.max(r1, r5);
        r7.mTotalLength = r0;
        r14 = 1;
        r0 = r2;
        r8 = r4;
        r9 = r6;
        r34 = r11;
        r33 = r13;
        r30 = r14;
        r35 = r19;
        r14 = r25;
        r32 = r26;
        r29 = r27;
        goto L_0x012f;
    L_0x00bd:
        r27 = r0;
        r0 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r1 = r2.height;
        if (r1 != 0) goto L_0x00cf;
    L_0x00c5:
        r1 = r2.weight;
        r1 = (r1 > r23 ? 1 : (r1 == r23 ? 0 : -1));
        if (r1 <= 0) goto L_0x00cf;
    L_0x00cb:
        r0 = 0;
        r1 = -2;
        r2.height = r1;
    L_0x00cf:
        r5 = r0;
        r24 = 0;
        r0 = (r21 > r23 ? 1 : (r21 == r23 ? 0 : -1));
        if (r0 != 0) goto L_0x00db;
    L_0x00d6:
        r0 = r7.mTotalLength;
        r28 = r0;
        goto L_0x00de;
        r28 = 0;
    L_0x00de:
        r1 = r27;
        r0 = r7;
        r29 = r1;
        r30 = r14;
        r14 = r25;
        r1 = r4;
        r31 = r2;
        r32 = r26;
        r2 = r29;
        r33 = r13;
        r13 = r3;
        r3 = r8;
        r8 = r4;
        r34 = r11;
        r35 = r19;
        r11 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r4 = r24;
        r11 = r5;
        r5 = r9;
        r9 = r6;
        r6 = r28;
        r0.measureChildBeforeLayout(r1, r2, r3, r4, r5, r6);
        r0 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        if (r11 == r0) goto L_0x010c;
    L_0x0107:
        r0 = r31;
        r0.height = r11;
        goto L_0x010e;
    L_0x010c:
        r0 = r31;
    L_0x010e:
        r1 = r8.getMeasuredHeight();
        r2 = r7.mTotalLength;
        r3 = r2 + r1;
        r4 = r0.topMargin;
        r3 = r3 + r4;
        r4 = r0.bottomMargin;
        r3 = r3 + r4;
        r4 = r7.getNextLocationOffset(r8);
        r3 = r3 + r4;
        r3 = java.lang.Math.max(r2, r3);
        r7.mTotalLength = r3;
        if (r10 == 0) goto L_0x012e;
    L_0x0129:
        r3 = java.lang.Math.max(r1, r13);
        goto L_0x012f;
    L_0x012e:
        r3 = r13;
    L_0x012f:
        if (r15 < 0) goto L_0x013c;
    L_0x0131:
        r1 = r29;
        r2 = r1 + 1;
        if (r15 != r2) goto L_0x013e;
    L_0x0137:
        r2 = r7.mTotalLength;
        r7.mBaselineChildTop = r2;
        goto L_0x013e;
    L_0x013c:
        r1 = r29;
    L_0x013e:
        if (r1 >= r15) goto L_0x014f;
    L_0x0140:
        r2 = r0.weight;
        r2 = (r2 > r23 ? 1 : (r2 == r23 ? 0 : -1));
        if (r2 > 0) goto L_0x0147;
    L_0x0146:
        goto L_0x014f;
    L_0x0147:
        r2 = new java.lang.RuntimeException;
        r4 = "A child of LinearLayout with index less than mBaselineAlignedChildIndex has weight > 0, which won't work.  Either remove the weight, or don't set mBaselineAlignedChildIndex.";
        r2.<init>(r4);
        throw r2;
    L_0x014f:
        r2 = 0;
        r4 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r12 == r4) goto L_0x015d;
    L_0x0154:
        r4 = r0.width;
        r6 = -1;
        if (r4 != r6) goto L_0x015e;
    L_0x0159:
        r18 = 1;
        r2 = 1;
        goto L_0x015e;
    L_0x015d:
        r6 = -1;
    L_0x015e:
        r4 = r0.leftMargin;
        r5 = r0.rightMargin;
        r4 = r4 + r5;
        r5 = r8.getMeasuredWidth();
        r5 = r5 + r4;
        r11 = r32;
        r11 = java.lang.Math.max(r11, r5);
        r13 = r8.getMeasuredState();
        r13 = combineMeasuredStates(r14, r13);
        if (r17 == 0) goto L_0x017f;
    L_0x0178:
        r14 = r0.width;
        if (r14 != r6) goto L_0x017f;
    L_0x017c:
        r6 = r22;
        goto L_0x0180;
    L_0x017f:
        r6 = 0;
    L_0x0180:
        r14 = r0.weight;
        r14 = (r14 > r23 ? 1 : (r14 == r23 ? 0 : -1));
        if (r14 <= 0) goto L_0x0195;
        if (r2 == 0) goto L_0x018b;
    L_0x0189:
        r14 = r4;
        goto L_0x018c;
    L_0x018b:
        r14 = r5;
    L_0x018c:
        r36 = r6;
        r6 = r35;
        r6 = java.lang.Math.max(r6, r14);
        goto L_0x01a2;
    L_0x0195:
        r36 = r6;
        r6 = r35;
        if (r2 == 0) goto L_0x019d;
    L_0x019b:
        r14 = r4;
        goto L_0x019e;
    L_0x019d:
        r14 = r5;
    L_0x019e:
        r9 = java.lang.Math.max(r9, r14);
    L_0x01a2:
        r14 = r7.getChildrenSkipCount(r8, r1);
        r0 = r1 + r14;
        r4 = r6;
        r6 = r9;
        r2 = r11;
        r1 = r13;
        r5 = r21;
        r14 = r30;
        r17 = r36;
    L_0x01b2:
        r0 = r0 + 1;
        r13 = r33;
        r11 = r34;
        r8 = r59;
        r9 = r60;
        goto L_0x002f;
    L_0x01be:
        r9 = r6;
        r34 = r11;
        r33 = r13;
        r30 = r14;
        r6 = r19;
        r0 = -1;
        r14 = r1;
        r11 = r2;
        r13 = r3;
        r1 = r7.mTotalLength;
        if (r1 <= 0) goto L_0x01df;
    L_0x01cf:
        r1 = r34;
        r2 = r7.hasDividerBeforeChildAt(r1);
        if (r2 == 0) goto L_0x01e1;
    L_0x01d7:
        r2 = r7.mTotalLength;
        r3 = r7.mDividerHeight;
        r2 = r2 + r3;
        r7.mTotalLength = r2;
        goto L_0x01e1;
    L_0x01df:
        r1 = r34;
    L_0x01e1:
        if (r10 == 0) goto L_0x0238;
    L_0x01e3:
        r2 = r33;
        r3 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        if (r2 == r3) goto L_0x01eb;
    L_0x01e9:
        if (r2 != 0) goto L_0x023a;
    L_0x01eb:
        r3 = 0;
        r7.mTotalLength = r3;
        r3 = 0;
    L_0x01ef:
        if (r3 >= r1) goto L_0x023a;
    L_0x01f1:
        r4 = r7.getVirtualChildAt(r3);
        if (r4 != 0) goto L_0x0202;
    L_0x01f7:
        r8 = r7.mTotalLength;
        r19 = r7.measureNullChild(r3);
        r8 = r8 + r19;
        r7.mTotalLength = r8;
        goto L_0x0210;
    L_0x0202:
        r8 = r4.getVisibility();
        r0 = 8;
        if (r8 != r0) goto L_0x0213;
    L_0x020a:
        r0 = r7.getChildrenSkipCount(r4, r3);
        r3 = r3 + r0;
    L_0x0210:
        r37 = r3;
        goto L_0x0234;
        r0 = r4.getLayoutParams();
        r0 = (com.android.settings.widget.MatchParentShrinkingLinearLayout.LayoutParams) r0;
        r8 = r7.mTotalLength;
        r19 = r8 + r13;
        r37 = r3;
        r3 = r0.topMargin;
        r19 = r19 + r3;
        r3 = r0.bottomMargin;
        r19 = r19 + r3;
        r3 = r7.getNextLocationOffset(r4);
        r3 = r19 + r3;
        r3 = java.lang.Math.max(r8, r3);
        r7.mTotalLength = r3;
    L_0x0234:
        r3 = r37 + 1;
        r0 = -1;
        goto L_0x01ef;
    L_0x0238:
        r2 = r33;
    L_0x023a:
        r0 = r7.mTotalLength;
        r3 = r7.mPaddingTop;
        r4 = r7.mPaddingBottom;
        r3 = r3 + r4;
        r0 = r0 + r3;
        r7.mTotalLength = r0;
        r0 = r7.mTotalLength;
        r3 = r58.getSuggestedMinimumHeight();
        r0 = java.lang.Math.max(r0, r3);
        r3 = r60;
        r4 = 0;
        r8 = resolveSizeAndState(r0, r3, r4);
        r4 = 16777215; // 0xffffff float:2.3509886E-38 double:8.2890456E-317;
        r0 = r8 & r4;
        r4 = r7.mTotalLength;
        r4 = r0 - r4;
        if (r30 != 0) goto L_0x02f2;
    L_0x0260:
        if (r4 == 0) goto L_0x0270;
    L_0x0262:
        r19 = (r5 > r23 ? 1 : (r5 == r23 ? 0 : -1));
        if (r19 <= 0) goto L_0x0270;
    L_0x0266:
        r38 = r0;
        r39 = r4;
        r40 = r5;
        r41 = r6;
        goto L_0x02fa;
    L_0x0270:
        r9 = java.lang.Math.max(r9, r6);
        if (r10 == 0) goto L_0x02dc;
    L_0x0276:
        r38 = r0;
        r0 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r2 == r0) goto L_0x02d3;
    L_0x027c:
        r16 = 0;
    L_0x027e:
        r0 = r16;
        if (r0 >= r1) goto L_0x02d3;
    L_0x0282:
        r39 = r4;
        r4 = r7.getVirtualChildAt(r0);
        if (r4 == 0) goto L_0x02c2;
    L_0x028a:
        r40 = r5;
        r5 = r4.getVisibility();
        r41 = r6;
        r6 = 8;
        if (r5 != r6) goto L_0x029a;
        r44 = r9;
        goto L_0x02c8;
    L_0x029a:
        r5 = r4.getLayoutParams();
        r5 = (com.android.settings.widget.MatchParentShrinkingLinearLayout.LayoutParams) r5;
        r6 = r5.weight;
        r16 = (r6 > r23 ? 1 : (r6 == r23 ? 0 : -1));
        if (r16 <= 0) goto L_0x02bf;
        r42 = r5;
        r5 = r4.getMeasuredWidth();
        r43 = r6;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r5 = android.view.View.MeasureSpec.makeMeasureSpec(r5, r6);
        r44 = r9;
        r9 = android.view.View.MeasureSpec.makeMeasureSpec(r13, r6);
        r4.measure(r5, r9);
        goto L_0x02c8;
    L_0x02bf:
        r44 = r9;
        goto L_0x02c8;
    L_0x02c2:
        r40 = r5;
        r41 = r6;
        r44 = r9;
    L_0x02c8:
        r16 = r0 + 1;
        r4 = r39;
        r5 = r40;
        r6 = r41;
        r9 = r44;
        goto L_0x027e;
    L_0x02d3:
        r39 = r4;
        r40 = r5;
        r41 = r6;
        r44 = r9;
        goto L_0x02e6;
    L_0x02dc:
        r38 = r0;
        r39 = r4;
        r40 = r5;
        r41 = r6;
        r44 = r9;
    L_0x02e6:
        r51 = r2;
        r45 = r10;
        r46 = r13;
        r47 = r15;
        r13 = r59;
        goto L_0x046b;
    L_0x02f2:
        r38 = r0;
        r39 = r4;
        r40 = r5;
        r41 = r6;
    L_0x02fa:
        r0 = r7.mWeightSum;
        r0 = (r0 > r23 ? 1 : (r0 == r23 ? 0 : -1));
        if (r0 <= 0) goto L_0x0303;
    L_0x0300:
        r5 = r7.mWeightSum;
        goto L_0x0305;
    L_0x0303:
        r5 = r40;
    L_0x0305:
        r0 = r5;
        r4 = 0;
        r7.mTotalLength = r4;
        r0 = r4;
        r6 = r39;
    L_0x030c:
        if (r0 >= r1) goto L_0x0450;
    L_0x030e:
        r4 = r7.getVirtualChildAt(r0);
        r45 = r10;
        r10 = r4.getVisibility();
        r46 = r13;
        r13 = 8;
        if (r10 != r13) goto L_0x0327;
        r51 = r2;
        r47 = r15;
        r13 = r59;
        goto L_0x0443;
    L_0x0327:
        r10 = r4.getLayoutParams();
        r10 = (com.android.settings.widget.MatchParentShrinkingLinearLayout.LayoutParams) r10;
        r13 = r10.weight;
        r16 = (r13 > r23 ? 1 : (r13 == r23 ? 0 : -1));
        if (r16 <= 0) goto L_0x039a;
    L_0x0333:
        if (r6 <= 0) goto L_0x039a;
    L_0x0335:
        r47 = r15;
        r15 = (float) r6;
        r15 = r15 * r13;
        r15 = r15 / r5;
        r15 = (int) r15;
        r5 = r5 - r13;
        r6 = r6 - r15;
        r48 = r5;
        r5 = r7.mPaddingLeft;
        r49 = r6;
        r6 = r7.mPaddingRight;
        r5 = r5 + r6;
        r6 = r10.leftMargin;
        r5 = r5 + r6;
        r6 = r10.rightMargin;
        r5 = r5 + r6;
        r6 = r10.width;
        r50 = r13;
        r13 = r59;
        r5 = getChildMeasureSpec(r13, r5, r6);
        r6 = r10.height;
        if (r6 != 0) goto L_0x0375;
    L_0x035a:
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r2 == r6) goto L_0x0361;
    L_0x035e:
        r51 = r2;
        goto L_0x0377;
        if (r15 <= 0) goto L_0x0368;
    L_0x0364:
        r51 = r2;
        r2 = r15;
        goto L_0x036b;
    L_0x0368:
        r51 = r2;
        r2 = 0;
    L_0x036b:
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r6);
        r4.measure(r5, r2);
        r52 = r15;
        goto L_0x038c;
    L_0x0375:
        r51 = r2;
    L_0x0377:
        r2 = r4.getMeasuredHeight();
        r2 = r2 + r15;
        if (r2 >= 0) goto L_0x037f;
    L_0x037e:
        r2 = 0;
        r52 = r15;
        r6 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r15 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r6);
        r4.measure(r5, r15);
    L_0x038c:
        r2 = r4.getMeasuredState();
        r2 = r2 & -256;
        r14 = combineMeasuredStates(r14, r2);
        r53 = r48;
        goto L_0x03e9;
    L_0x039a:
        r51 = r2;
        r50 = r13;
        r47 = r15;
        r13 = r59;
        if (r6 >= 0) goto L_0x03e5;
    L_0x03a4:
        r2 = r10.height;
        r15 = -1;
        if (r2 != r15) goto L_0x03e5;
    L_0x03a9:
        r2 = r7.mPaddingLeft;
        r15 = r7.mPaddingRight;
        r2 = r2 + r15;
        r15 = r10.leftMargin;
        r2 = r2 + r15;
        r15 = r10.rightMargin;
        r2 = r2 + r15;
        r15 = r10.width;
        r2 = getChildMeasureSpec(r13, r2, r15);
        r15 = r4.getMeasuredHeight();
        r15 = r15 + r6;
        if (r15 >= 0) goto L_0x03c2;
    L_0x03c1:
        r15 = 0;
    L_0x03c2:
        r16 = r4.getMeasuredHeight();
        r16 = r15 - r16;
        r6 = r6 - r16;
        r53 = r5;
        r54 = r6;
        r5 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r15, r5);
        r4.measure(r2, r6);
        r5 = r4.getMeasuredState();
        r5 = r5 & -256;
        r14 = combineMeasuredStates(r14, r5);
        r49 = r54;
        goto L_0x03e9;
    L_0x03e5:
        r53 = r5;
        r49 = r6;
    L_0x03e9:
        r2 = r10.leftMargin;
        r5 = r10.rightMargin;
        r2 = r2 + r5;
        r5 = r4.getMeasuredWidth();
        r5 = r5 + r2;
        r6 = java.lang.Math.max(r11, r5);
        r11 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r12 == r11) goto L_0x0403;
    L_0x03fb:
        r11 = r10.width;
        r15 = -1;
        if (r11 != r15) goto L_0x0403;
    L_0x0400:
        r11 = r22;
        goto L_0x0404;
    L_0x0403:
        r11 = 0;
        if (r11 == 0) goto L_0x0409;
    L_0x0407:
        r15 = r2;
        goto L_0x040a;
    L_0x0409:
        r15 = r5;
    L_0x040a:
        r9 = java.lang.Math.max(r9, r15);
        if (r17 == 0) goto L_0x041a;
    L_0x0410:
        r15 = r10.width;
        r55 = r2;
        r2 = -1;
        if (r15 != r2) goto L_0x041d;
    L_0x0417:
        r15 = r22;
        goto L_0x041e;
    L_0x041a:
        r55 = r2;
        r2 = -1;
    L_0x041d:
        r15 = 0;
    L_0x041e:
        r2 = r7.mTotalLength;
        r16 = r4.getMeasuredHeight();
        r16 = r2 + r16;
        r56 = r5;
        r5 = r10.topMargin;
        r16 = r16 + r5;
        r5 = r10.bottomMargin;
        r16 = r16 + r5;
        r5 = r7.getNextLocationOffset(r4);
        r5 = r16 + r5;
        r5 = java.lang.Math.max(r2, r5);
        r7.mTotalLength = r5;
        r11 = r6;
        r17 = r15;
        r6 = r49;
        r5 = r53;
    L_0x0443:
        r0 = r0 + 1;
        r10 = r45;
        r13 = r46;
        r15 = r47;
        r2 = r51;
        r4 = 0;
        goto L_0x030c;
    L_0x0450:
        r51 = r2;
        r53 = r5;
        r45 = r10;
        r46 = r13;
        r47 = r15;
        r13 = r59;
        r0 = r7.mTotalLength;
        r2 = r7.mPaddingTop;
        r4 = r7.mPaddingBottom;
        r2 = r2 + r4;
        r0 = r0 + r2;
        r7.mTotalLength = r0;
        r39 = r6;
        r44 = r9;
    L_0x046b:
        if (r17 != 0) goto L_0x0473;
    L_0x046d:
        r0 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r12 == r0) goto L_0x0473;
    L_0x0471:
        r11 = r44;
    L_0x0473:
        r0 = r7.mPaddingLeft;
        r2 = r7.mPaddingRight;
        r0 = r0 + r2;
        r11 = r11 + r0;
        r0 = r58.getSuggestedMinimumWidth();
        r0 = java.lang.Math.max(r11, r0);
        r2 = resolveSizeAndState(r0, r13, r14);
        r7.setMeasuredDimension(r2, r8);
        if (r18 == 0) goto L_0x048d;
    L_0x048a:
        r7.forceUniformWidth(r1, r3);
    L_0x048d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.widget.MatchParentShrinkingLinearLayout.measureVertical(int, int):void");
    }

    private void forceUniformWidth(int count, int heightMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), Ints.MAX_POWER_OF_TWO);
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.width == -1) {
                    int oldHeight = lp.height;
                    lp.height = child.getMeasuredHeight();
                    measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0);
                    lp.height = oldHeight;
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
        throw new IllegalStateException("horizontal mode not supported.");
    }

    private void forceUniformHeight(int count, int widthMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), Ints.MAX_POWER_OF_TWO);
        for (int i = 0; i < count; i++) {
            View child = getVirtualChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.height == -1) {
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getChildrenSkipCount(View child, int index) {
        return 0;
    }

    /* Access modifiers changed, original: 0000 */
    public int measureNullChild(int childIndex) {
        return 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        measureChildWithMargins(child, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    /* Access modifiers changed, original: 0000 */
    public int getLocationOffset(View child) {
        return 0;
    }

    /* Access modifiers changed, original: 0000 */
    public int getNextLocationOffset(View child) {
        return 0;
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mOrientation == 1) {
            layoutVertical(l, t, r, b);
        } else {
            layoutHorizontal(l, t, r, b);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void layoutVertical(int left, int top, int right, int bottom) {
        int childTop;
        int paddingLeft = this.mPaddingLeft;
        int width = right - left;
        int childRight = width - this.mPaddingRight;
        int childSpace = (width - paddingLeft) - this.mPaddingRight;
        int count = getVirtualChildCount();
        int majorGravity = this.mGravity & 112;
        int minorGravity = this.mGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if (majorGravity == 16) {
            childTop = this.mPaddingTop + (((bottom - top) - this.mTotalLength) / 2);
        } else if (majorGravity != 80) {
            childTop = this.mPaddingTop;
        } else {
            childTop = ((this.mPaddingTop + bottom) - top) - this.mTotalLength;
        }
        int i = 0;
        while (true) {
            int i2 = i;
            int paddingLeft2;
            if (i2 < count) {
                int majorGravity2;
                View child = getVirtualChildAt(i2);
                if (child == null) {
                    childTop += measureNullChild(i2);
                    majorGravity2 = majorGravity;
                    paddingLeft2 = paddingLeft;
                } else if (child.getVisibility() != 8) {
                    int childWidth = child.getMeasuredWidth();
                    int childHeight = child.getMeasuredHeight();
                    LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    int gravity = lp.gravity;
                    if (gravity < 0) {
                        gravity = minorGravity;
                    }
                    int layoutDirection = getLayoutDirection();
                    int gravity2 = gravity;
                    gravity = Gravity.getAbsoluteGravity(gravity, layoutDirection) & 7;
                    majorGravity2 = majorGravity;
                    gravity = gravity != 1 ? gravity != 5 ? lp.leftMargin + paddingLeft : (childRight - childWidth) - lp.rightMargin : ((((childSpace - childWidth) / 2) + paddingLeft) + lp.leftMargin) - lp.rightMargin;
                    if (hasDividerBeforeChildAt(i2) != 0) {
                        childTop += this.mDividerHeight;
                    }
                    gravity2 = childTop + lp.topMargin;
                    LayoutParams lp2 = lp;
                    View child2 = child;
                    paddingLeft2 = paddingLeft;
                    paddingLeft = i2;
                    setChildFrame(child, gravity, gravity2 + getLocationOffset(child), childWidth, childHeight);
                    i2 = paddingLeft + getChildrenSkipCount(child2, paddingLeft);
                    childTop = gravity2 + ((childHeight + lp2.bottomMargin) + getNextLocationOffset(child2));
                } else {
                    majorGravity2 = majorGravity;
                    paddingLeft2 = paddingLeft;
                    paddingLeft = i2;
                }
                i = i2 + 1;
                majorGravity = majorGravity2;
                paddingLeft = paddingLeft2;
            } else {
                paddingLeft2 = paddingLeft;
                return;
            }
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (layoutDirection != this.mLayoutDirection) {
            this.mLayoutDirection = layoutDirection;
            if (this.mOrientation == 0) {
                requestLayout();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00b7  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00ef  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00c3  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0102  */
    public void layoutHorizontal(int r32, int r33, int r34, int r35) {
        /*
        r31 = this;
        r6 = r31;
        r9 = r31.isLayoutRtl();
        r10 = r6.mPaddingTop;
        r13 = r35 - r33;
        r0 = r6.mPaddingBottom;
        r14 = r13 - r0;
        r0 = r13 - r10;
        r1 = r6.mPaddingBottom;
        r15 = r0 - r1;
        r5 = r31.getVirtualChildCount();
        r0 = r6.mGravity;
        r1 = 8388615; // 0x800007 float:1.1754953E-38 double:4.1445265E-317;
        r4 = r0 & r1;
        r0 = r6.mGravity;
        r16 = r0 & 112;
        r2 = r6.mBaselineAligned;
        r1 = r6.mMaxAscent;
        r0 = r6.mMaxDescent;
        r3 = r31.getLayoutDirection();
        r11 = android.view.Gravity.getAbsoluteGravity(r4, r3);
        r17 = 2;
        r12 = 1;
        if (r11 == r12) goto L_0x0048;
    L_0x0036:
        r12 = 5;
        if (r11 == r12) goto L_0x003e;
    L_0x0039:
        r11 = r6.mPaddingLeft;
    L_0x003b:
        r18 = r3;
        goto L_0x0055;
    L_0x003e:
        r11 = r6.mPaddingLeft;
        r11 = r11 + r34;
        r11 = r11 - r32;
        r12 = r6.mTotalLength;
        r11 = r11 - r12;
        goto L_0x003b;
    L_0x0048:
        r11 = r6.mPaddingLeft;
        r12 = r34 - r32;
        r18 = r3;
        r3 = r6.mTotalLength;
        r12 = r12 - r3;
        r12 = r12 / 2;
        r11 = r11 + r12;
    L_0x0055:
        r3 = r11;
        r11 = 0;
        r12 = 1;
        if (r9 == 0) goto L_0x005d;
    L_0x005a:
        r11 = r5 + -1;
        r12 = -1;
    L_0x005d:
        r19 = 0;
        r20 = r3;
    L_0x0061:
        r3 = r19;
        if (r3 >= r5) goto L_0x0153;
    L_0x0065:
        r19 = r12 * r3;
        r7 = r11 + r19;
        r8 = r6.getVirtualChildAt(r7);
        if (r8 != 0) goto L_0x0083;
    L_0x006f:
        r19 = r6.measureNullChild(r7);
        r20 = r20 + r19;
        r26 = r0;
        r28 = r1;
        r25 = r2;
        r22 = r4;
        r27 = r5;
        r30 = r9;
        goto L_0x0142;
    L_0x0083:
        r21 = r3;
        r3 = r8.getVisibility();
        r22 = r4;
        r4 = 8;
        if (r3 == r4) goto L_0x0136;
    L_0x008f:
        r19 = r8.getMeasuredWidth();
        r23 = r8.getMeasuredHeight();
        r3 = -1;
        r4 = r8.getLayoutParams();
        r4 = (com.android.settings.widget.MatchParentShrinkingLinearLayout.LayoutParams) r4;
        r24 = r3;
        r3 = -1;
        if (r2 == 0) goto L_0x00af;
    L_0x00a4:
        r25 = r2;
        r2 = r4.height;
        if (r2 == r3) goto L_0x00b1;
    L_0x00aa:
        r2 = r8.getBaseline();
        goto L_0x00b3;
    L_0x00af:
        r25 = r2;
    L_0x00b1:
        r2 = r24;
    L_0x00b3:
        r3 = r4.gravity;
        if (r3 >= 0) goto L_0x00b9;
    L_0x00b7:
        r3 = r16;
    L_0x00b9:
        r24 = r3;
        r3 = r24 & 112;
        r27 = r5;
        r5 = 16;
        if (r3 == r5) goto L_0x00ef;
    L_0x00c3:
        r5 = 48;
        if (r3 == r5) goto L_0x00e1;
    L_0x00c7:
        r5 = 80;
        if (r3 == r5) goto L_0x00cd;
    L_0x00cb:
        r3 = r10;
        goto L_0x00fb;
    L_0x00cd:
        r3 = r14 - r23;
        r5 = r4.bottomMargin;
        r3 = r3 - r5;
        r5 = -1;
        if (r2 == r5) goto L_0x00fb;
    L_0x00d5:
        r5 = r8.getMeasuredHeight();
        r5 = r5 - r2;
        r26 = r0[r17];
        r26 = r26 - r5;
        r3 = r3 - r26;
        goto L_0x00fb;
    L_0x00e1:
        r3 = r4.topMargin;
        r3 = r3 + r10;
        r5 = -1;
        if (r2 == r5) goto L_0x00fb;
    L_0x00e7:
        r5 = 1;
        r26 = r1[r5];
        r26 = r26 - r2;
        r3 = r3 + r26;
        goto L_0x00fb;
    L_0x00ef:
        r3 = r15 - r23;
        r3 = r3 / 2;
        r3 = r3 + r10;
        r5 = r4.topMargin;
        r3 = r3 + r5;
        r5 = r4.bottomMargin;
        r3 = r3 - r5;
        r5 = r6.hasDividerBeforeChildAt(r7);
        if (r5 == 0) goto L_0x0106;
    L_0x0102:
        r5 = r6.mDividerWidth;
        r20 = r20 + r5;
    L_0x0106:
        r5 = r4.leftMargin;
        r20 = r20 + r5;
        r5 = r6.getLocationOffset(r8);
        r5 = r20 + r5;
        r26 = r0;
        r0 = r6;
        r28 = r1;
        r1 = r8;
        r29 = r2;
        r2 = r5;
        r5 = r4;
        r4 = r19;
        r30 = r9;
        r9 = r5;
        r5 = r23;
        r0.setChildFrame(r1, r2, r3, r4, r5);
        r0 = r9.rightMargin;
        r0 = r19 + r0;
        r1 = r6.getNextLocationOffset(r8);
        r0 = r0 + r1;
        r20 = r20 + r0;
        r0 = r6.getChildrenSkipCount(r8, r7);
        r3 = r21 + r0;
        goto L_0x0142;
    L_0x0136:
        r26 = r0;
        r28 = r1;
        r25 = r2;
        r27 = r5;
        r30 = r9;
        r3 = r21;
    L_0x0142:
        r0 = 1;
        r19 = r3 + 1;
        r4 = r22;
        r2 = r25;
        r0 = r26;
        r5 = r27;
        r1 = r28;
        r9 = r30;
        goto L_0x0061;
    L_0x0153:
        r26 = r0;
        r28 = r1;
        r25 = r2;
        r22 = r4;
        r27 = r5;
        r30 = r9;
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.widget.MatchParentShrinkingLinearLayout.layoutHorizontal(int, int, int, int):void");
    }

    private void setChildFrame(View child, int left, int top, int width, int height) {
        child.layout(left, top, left + width, top + height);
    }

    public void setOrientation(int orientation) {
        if (this.mOrientation != orientation) {
            this.mOrientation = orientation;
            requestLayout();
        }
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    @RemotableViewMethod
    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            if ((GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK & gravity) == 0) {
                gravity |= GravityCompat.START;
            }
            if ((gravity & 112) == 0) {
                gravity |= 48;
            }
            this.mGravity = gravity;
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setHorizontalGravity(int horizontalGravity) {
        int gravity = horizontalGravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if ((GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK & this.mGravity) != gravity) {
            this.mGravity = (this.mGravity & -8388616) | gravity;
            requestLayout();
        }
    }

    @RemotableViewMethod
    public void setVerticalGravity(int verticalGravity) {
        int gravity = verticalGravity & 112;
        if ((this.mGravity & 112) != gravity) {
            this.mGravity = (this.mGravity & -113) | gravity;
            requestLayout();
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateDefaultLayoutParams() {
        if (this.mOrientation == 0) {
            return new LayoutParams(-2, -2);
        }
        if (this.mOrientation == 1) {
            return new LayoutParams(-1, -2);
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /* Access modifiers changed, original: protected */
    public boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public CharSequence getAccessibilityClassName() {
        return MatchParentShrinkingLinearLayout.class.getName();
    }

    /* Access modifiers changed, original: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("layout:baselineAligned", this.mBaselineAligned);
        encoder.addProperty("layout:baselineAlignedChildIndex", this.mBaselineAlignedChildIndex);
        encoder.addProperty("measurement:baselineChildTop", this.mBaselineChildTop);
        encoder.addProperty("measurement:orientation", this.mOrientation);
        encoder.addProperty("measurement:gravity", this.mGravity);
        encoder.addProperty("measurement:totalLength", this.mTotalLength);
        encoder.addProperty("layout:totalLength", this.mTotalLength);
        encoder.addProperty("layout:useLargestChild", this.mUseLargestChild);
    }
}
