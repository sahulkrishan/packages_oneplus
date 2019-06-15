package com.oneplus.lib.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.primitives.Ints;
import com.oneplus.commonctrl.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;

public class OPTabLayout extends HorizontalScrollView {
    private static final int ANIMATION_DURATION = 300;
    private static final int DEFAULT_HEIGHT = 48;
    private static final int FIXED_WRAP_GUTTER_MIN = 16;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_FILL = 0;
    public static final int MODE_FIXED = 1;
    public static final int MODE_SCROLLABLE = 0;
    private static final int MOTION_NON_ADJACENT_OFFSET = 24;
    private static final int TAB_MIN_WIDTH_MARGIN = 56;
    private Interpolator fast_out_slow_in_interpolator;
    private int mContentInsetStart;
    private ValueAnimator mIndicatorAnimator;
    private int mMode;
    private OnTabSelectedListener mOnTabSelectedListener;
    private final int mRequestedTabMaxWidth;
    private ValueAnimator mScrollAnimator;
    private Tab mSelectedTab;
    private final int mTabBackgroundResId;
    private OnClickListener mTabClickListener;
    private int mTabGravity;
    private int mTabHorizontalSpacing;
    private int mTabMaxWidth;
    private int mTabMinWidth;
    private int mTabPaddingBottom;
    private int mTabPaddingEnd;
    private int mTabPaddingStart;
    private int mTabPaddingTop;
    private final SlidingTabStrip mTabStrip;
    private int mTabTextAppearance;
    private ColorStateList mTabTextColors;
    private final ArrayList<Tab> mTabs;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public interface OnTabSelectedListener {
        void onTabReselected(Tab tab);

        void onTabSelected(Tab tab);

        void onTabUnselected(Tab tab);
    }

    private class SlidingTabStrip extends LinearLayout {
        private int mIndicatorLeft = -1;
        private int mIndicatorRight = -1;
        private int mSelectedIndicatorHeight;
        private final Paint mSelectedIndicatorPaint;
        private int mSelectedPosition = -1;
        private float mSelectionOffset;

        SlidingTabStrip(Context context) {
            super(context);
            setWillNotDraw(false);
            this.mSelectedIndicatorPaint = new Paint();
        }

        /* Access modifiers changed, original: 0000 */
        public void setSelectedIndicatorColor(int color) {
            if (this.mSelectedIndicatorPaint.getColor() != color) {
                this.mSelectedIndicatorPaint.setColor(color);
                postInvalidateOnAnimation();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setSelectedIndicatorHeight(int height) {
            if (this.mSelectedIndicatorHeight != height) {
                this.mSelectedIndicatorHeight = height;
                postInvalidateOnAnimation();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public boolean childrenNeedLayout() {
            int z = getChildCount();
            for (int i = 0; i < z; i++) {
                if (getChildAt(i).getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        public void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            this.mSelectedPosition = position;
            this.mSelectionOffset = positionOffset;
            updateIndicatorPosition();
        }

        /* Access modifiers changed, original: protected */
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (MeasureSpec.getMode(widthMeasureSpec) == Ints.MAX_POWER_OF_TWO && OPTabLayout.this.mMode == 1 && OPTabLayout.this.mTabGravity == 1) {
                int count = getChildCount();
                int i = 0;
                int unspecifiedSpec = MeasureSpec.makeMeasureSpec(0, 0);
                int largestTabWidth = 0;
                int z = count;
                for (int i2 = 0; i2 < z; i2++) {
                    View child = getChildAt(i2);
                    child.measure(unspecifiedSpec, heightMeasureSpec);
                    largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
                }
                if (largestTabWidth > 0) {
                    if (largestTabWidth * count <= getMeasuredWidth() - (OPTabLayout.this.dpToPx(16) * 2)) {
                        while (i < count) {
                            LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
                            lp.width = largestTabWidth;
                            lp.weight = 0.0f;
                            i++;
                        }
                    } else {
                        OPTabLayout.this.mTabGravity = 0;
                        OPTabLayout.this.updateTabViewsLayoutParams();
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }

        /* Access modifiers changed, original: protected */
        public void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            updateIndicatorPosition();
        }

        private void updateIndicatorPosition() {
            int right;
            View selectedTitle = getChildAt(this.mSelectedPosition);
            int left = -1;
            if (selectedTitle == null || selectedTitle.getWidth() <= 0) {
                right = -1;
            } else {
                int left2 = selectedTitle.getLeft();
                right = selectedTitle.getRight();
                boolean z = false;
                if (isRtl() ? this.mSelectedPosition <= 0 : this.mSelectedPosition >= getChildCount() - 1) {
                    z = true;
                }
                boolean hasNextTab = z;
                if (this.mSelectionOffset > 0.0f && hasNextTab) {
                    int i = this.mSelectedPosition;
                    if (!isRtl()) {
                        left = 1;
                    }
                    View nextTitle = getChildAt(i + left);
                    left2 = (int) ((this.mSelectionOffset * ((float) nextTitle.getLeft())) + ((1.0f - this.mSelectionOffset) * ((float) left2)));
                    right = (int) ((this.mSelectionOffset * ((float) nextTitle.getRight())) + ((1.0f - this.mSelectionOffset) * ((float) right)));
                }
                left = left2;
            }
            setIndicatorPosition(left, right);
        }

        private boolean isRtl() {
            return getLayoutDirection() == 1;
        }

        private void setIndicatorPosition(int left, int right) {
            if (left != this.mIndicatorLeft || right != this.mIndicatorRight) {
                this.mIndicatorLeft = left;
                this.mIndicatorRight = right;
                postInvalidateOnAnimation();
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void animateIndicatorToPosition(int position, int duration) {
            int startRight;
            int startLeft;
            int startRight2;
            final int i = position;
            boolean isRtl = getLayoutDirection() == 1;
            View targetView = getChildAt(position);
            int targetLeft = targetView.getLeft();
            int targetRight = targetView.getRight();
            int startLeft2;
            if (Math.abs(i - this.mSelectedPosition) <= 1) {
                startLeft2 = this.mIndicatorLeft;
                startRight = this.mIndicatorRight;
                startLeft = startLeft2;
            } else {
                startLeft2 = OPTabLayout.this.dpToPx(24);
                if (i < this.mSelectedPosition) {
                    if (isRtl) {
                        startLeft = targetLeft - startLeft2;
                        startRight2 = startLeft;
                    } else {
                        startLeft = targetRight + startLeft2;
                        startRight2 = startLeft;
                    }
                } else if (isRtl) {
                    startLeft = targetRight + startLeft2;
                    startRight2 = startLeft;
                } else {
                    startLeft = targetLeft - startLeft2;
                    startRight = startLeft;
                }
                startRight = startRight2;
            }
            int startLeft3 = startLeft;
            if (startLeft3 == targetLeft && startRight == targetRight) {
                if (OPTabLayout.this.mIndicatorAnimator != null) {
                    OPTabLayout.this.mIndicatorAnimator.cancel();
                }
                this.mSelectedPosition = i;
                this.mSelectionOffset = 0.0f;
                setIndicatorPosition(targetLeft, targetRight);
                int i2 = duration;
                boolean z = isRtl;
                return;
            }
            ValueAnimator animator = OPTabLayout.this.mIndicatorAnimator = new ValueAnimator();
            animator.setInterpolator(OPTabLayout.this.fast_out_slow_in_interpolator);
            animator.setDuration((long) duration);
            animator.setFloatValues(new float[]{0.0f, 1.0f});
            startRight2 = startLeft3;
            final int i3 = targetLeft;
            final int i4 = startRight;
            AnonymousClass1 anonymousClass1 = r0;
            final int i5 = targetRight;
            AnonymousClass1 anonymousClass12 = new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float fraction = animation.getAnimatedFraction();
                    SlidingTabStrip.this.setIndicatorPosition(OPAnimationUtils.lerp(startRight2, i3, fraction), OPAnimationUtils.lerp(i4, i5, fraction));
                }
            };
            animator.addUpdateListener(anonymousClass1);
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    SlidingTabStrip.this.mSelectedPosition = i;
                    SlidingTabStrip.this.mSelectionOffset = 0.0f;
                }

                public void onAnimationCancel(Animator animation) {
                    SlidingTabStrip.this.mSelectedPosition = i;
                    SlidingTabStrip.this.mSelectionOffset = 0.0f;
                }
            });
            animator.start();
        }

        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (this.mIndicatorLeft >= 0 && this.mIndicatorRight > this.mIndicatorLeft) {
                canvas.drawRect((float) this.mIndicatorLeft, (float) (getHeight() - this.mSelectedIndicatorHeight), (float) this.mIndicatorRight, (float) getHeight(), this.mSelectedIndicatorPaint);
            }
        }
    }

    public static final class Tab {
        public static final int INVALID_POSITION = -1;
        private CharSequence mContentDesc;
        private View mCustomView;
        private Drawable mIcon;
        private final OPTabLayout mParent;
        private int mPosition = -1;
        private Object mTag;
        private CharSequence mText;

        Tab(OPTabLayout parent) {
            this.mParent = parent;
        }

        public Object getTag() {
            return this.mTag;
        }

        public Tab setTag(Object tag) {
            this.mTag = tag;
            return this;
        }

        public View getCustomView() {
            return this.mCustomView;
        }

        public Tab setCustomView(View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setCustomView(int layoutResId) {
            return setCustomView(LayoutInflater.from(this.mParent.getContext()).inflate(layoutResId, null));
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public int getPosition() {
            return this.mPosition;
        }

        /* Access modifiers changed, original: 0000 */
        public void setPosition(int position) {
            this.mPosition = position;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public Tab setIcon(Drawable icon) {
            this.mIcon = icon;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setIcon(int resId) {
            return setIcon(this.mParent.getContext().getDrawable(resId));
        }

        public Tab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setText(int resId) {
            return setText(this.mParent.getResources().getText(resId));
        }

        public void select() {
            this.mParent.selectTab(this);
        }

        public boolean isSelected() {
            return this.mParent.getSelectedTabPosition() == this.mPosition;
        }

        public Tab setContentDescription(int resId) {
            return setContentDescription(this.mParent.getResources().getText(resId));
        }

        public Tab setContentDescription(CharSequence contentDesc) {
            this.mContentDesc = contentDesc;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }
            return this;
        }

        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    class TabView extends LinearLayout implements OnLongClickListener {
        private ImageView mCustomIconView;
        private TextView mCustomTextView;
        private View mCustomView;
        private ImageView mIconView;
        private final Tab mTab;
        private TextView mTextView;

        public TabView(Context context, Tab tab) {
            super(context);
            this.mTab = tab;
            if (OPTabLayout.this.mTabBackgroundResId != 0) {
                setBackgroundDrawable(context.getDrawable(OPTabLayout.this.mTabBackgroundResId));
            }
            setPaddingRelative(OPTabLayout.this.mTabPaddingStart, OPTabLayout.this.mTabPaddingTop, OPTabLayout.this.mTabPaddingEnd, OPTabLayout.this.mTabPaddingBottom);
            setGravity(17);
            update();
        }

        public void setSelected(boolean selected) {
            boolean changed = isSelected() != selected;
            super.setSelected(selected);
            if (changed && selected) {
                sendAccessibilityEvent(4);
                if (this.mTextView != null) {
                    this.mTextView.setSelected(selected);
                }
                if (this.mIconView != null) {
                    this.mIconView.setSelected(selected);
                }
            }
        }

        @TargetApi(14)
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            event.setClassName(android.app.ActionBar.Tab.class.getName());
        }

        @TargetApi(14)
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName(android.app.ActionBar.Tab.class.getName());
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int measuredWidth = getMeasuredWidth();
            if (measuredWidth < OPTabLayout.this.mTabMinWidth || measuredWidth > OPTabLayout.this.mTabMaxWidth) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(OPMathUtils.constrain(measuredWidth, OPTabLayout.this.mTabMinWidth, OPTabLayout.this.mTabMaxWidth), Ints.MAX_POWER_OF_TWO), heightMeasureSpec);
            }
        }

        /* Access modifiers changed, original: final */
        public final void update() {
            Tab tab = this.mTab;
            View custom = tab.getCustomView();
            if (custom != null) {
                TabView customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        customParent.removeView(custom);
                    }
                    addView(custom);
                }
                this.mCustomView = custom;
                if (this.mTextView != null) {
                    this.mTextView.setVisibility(8);
                }
                if (this.mIconView != null) {
                    this.mIconView.setVisibility(8);
                    this.mIconView.setImageDrawable(null);
                }
                this.mCustomTextView = (TextView) custom.findViewById(16908308);
                this.mCustomIconView = (ImageView) custom.findViewById(16908294);
            } else {
                if (this.mCustomView != null) {
                    removeView(this.mCustomView);
                    this.mCustomView = null;
                }
                this.mCustomTextView = null;
                this.mCustomIconView = null;
            }
            if (this.mCustomView == null) {
                if (this.mIconView == null) {
                    ImageView iconView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.op_layout_tab_icon, this, false);
                    addView(iconView, 0);
                    this.mIconView = iconView;
                }
                if (this.mTextView == null) {
                    TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.op_layout_tab_text, this, false);
                    addView(textView);
                    this.mTextView = textView;
                }
                this.mTextView.setTextAppearance(getContext(), OPTabLayout.this.mTabTextAppearance);
                if (OPTabLayout.this.mTabTextColors != null) {
                    this.mTextView.setTextColor(OPTabLayout.this.mTabTextColors);
                }
                updateTextAndIcon(tab, this.mTextView, this.mIconView);
            } else if (this.mCustomTextView != null || this.mCustomIconView != null) {
                updateTextAndIcon(tab, this.mCustomTextView, this.mCustomIconView);
            }
        }

        private void updateTextAndIcon(Tab tab, TextView textView, ImageView iconView) {
            Drawable icon = tab.getIcon();
            CharSequence text = tab.getText();
            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(0);
                    setVisibility(0);
                } else {
                    iconView.setVisibility(8);
                    iconView.setImageDrawable(null);
                }
                iconView.setContentDescription(tab.getContentDescription());
            }
            boolean hasText = TextUtils.isEmpty(text) ^ 1;
            if (textView != null) {
                if (hasText) {
                    textView.setText(text);
                    textView.setContentDescription(tab.getContentDescription());
                    textView.setVisibility(0);
                    setVisibility(0);
                } else {
                    textView.setVisibility(8);
                    textView.setText(null);
                }
            }
            if (hasText || TextUtils.isEmpty(tab.getContentDescription())) {
                setOnLongClickListener(null);
                setLongClickable(false);
                return;
            }
            setOnLongClickListener(this);
        }

        public boolean onLongClick(View v) {
            int[] screenPos = new int[2];
            getLocationOnScreen(screenPos);
            Context context = getContext();
            int width = getWidth();
            int height = getHeight();
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            Toast cheatSheet = Toast.makeText(context, this.mTab.getContentDescription(), 0);
            cheatSheet.setGravity(49, (screenPos[0] + (width / 2)) - (screenWidth / 2), height);
            cheatSheet.show();
            return true;
        }

        public Tab getTab() {
            return this.mTab;
        }
    }

    public OPTabLayout(Context context) {
        this(context, null);
    }

    public OPTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.OPTabLayoutStyle);
    }

    public OPTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mTabs = new ArrayList();
        this.mTabMaxWidth = Integer.MAX_VALUE;
        this.fast_out_slow_in_interpolator = AnimationUtils.loadInterpolator(context, AndroidResources.FAST_OUT_SLOW_IN);
        setHorizontalScrollBarEnabled(false);
        setFillViewport(true);
        this.mTabStrip = new SlidingTabStrip(context);
        addView(this.mTabStrip, -2, -1);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OPTabLayout, defStyleAttr, R.style.Oneplus_Widget_Design_OPTabLayout);
        this.mTabStrip.setSelectedIndicatorHeight(a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabIndicatorHeight, 0));
        this.mTabStrip.setSelectedIndicatorColor(a.getColor(R.styleable.OPTabLayout_op_tabIndicatorColor, 0));
        this.mTabTextAppearance = a.getResourceId(R.styleable.OPTabLayout_op_tabTextAppearance, R.style.Oneplus_TextAppearance_Design_Tab);
        int dimensionPixelSize = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabPadding, 0);
        this.mTabPaddingBottom = dimensionPixelSize;
        this.mTabPaddingEnd = dimensionPixelSize;
        this.mTabPaddingTop = dimensionPixelSize;
        this.mTabPaddingStart = dimensionPixelSize;
        this.mTabPaddingStart = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabPaddingStart, this.mTabPaddingStart);
        this.mTabPaddingTop = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabPaddingTop, this.mTabPaddingTop);
        this.mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabPaddingEnd, this.mTabPaddingEnd);
        this.mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabPaddingBottom, this.mTabPaddingBottom);
        this.mTabTextColors = loadTextColorFromTextAppearance(this.mTabTextAppearance);
        if (a.hasValue(R.styleable.OPTabLayout_op_tabTextColor)) {
            this.mTabTextColors = a.getColorStateList(R.styleable.OPTabLayout_op_tabTextColor);
        }
        if (a.hasValue(R.styleable.OPTabLayout_op_tabSelectedTextColor)) {
            this.mTabTextColors = createColorStateList(this.mTabTextColors.getDefaultColor(), a.getColor(R.styleable.OPTabLayout_op_tabSelectedTextColor, 0));
        }
        this.mTabMinWidth = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabMinWidth, 0);
        this.mRequestedTabMaxWidth = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabMaxWidth, 0);
        this.mTabBackgroundResId = a.getResourceId(R.styleable.OPTabLayout_op_tabBackground, 0);
        this.mContentInsetStart = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_tabContentStart, 0);
        this.mTabHorizontalSpacing = a.getDimensionPixelSize(R.styleable.OPTabLayout_op_horizontalSpacing, 0);
        this.mMode = a.getInt(R.styleable.OPTabLayout_op_tabMode, 1);
        this.mTabGravity = a.getInt(R.styleable.OPTabLayout_op_tabGravity, 0);
        a.recycle();
        applyModeAndGravity();
    }

    public void setSelectedTabIndicatorColor(int color) {
        this.mTabStrip.setSelectedIndicatorColor(color);
    }

    public void setSelectedTabIndicatorHeight(int height) {
        this.mTabStrip.setSelectedIndicatorHeight(height);
    }

    public void setTabMinWidth(int minWidth) {
        this.mTabMinWidth = minWidth;
    }

    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        if ((this.mIndicatorAnimator == null || !this.mIndicatorAnimator.isRunning()) && position >= 0 && position < this.mTabStrip.getChildCount()) {
            this.mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset);
            scrollTo(calculateScrollXForTab(position, positionOffset), 0);
            if (updateSelectedText) {
                setSelectedTabView(Math.round(((float) position) + positionOffset));
            }
        }
    }

    public void addTab(Tab tab) {
        addTab(tab, this.mTabs.isEmpty());
    }

    public void addTab(Tab tab, int position) {
        addTab(tab, position, this.mTabs.isEmpty());
    }

    public void addTab(Tab tab, boolean setSelected) {
        if (tab.mParent == this) {
            addTabView(tab, setSelected);
            configureTab(tab, this.mTabs.size());
            if (setSelected) {
                tab.select();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Tab belongs to a different OPTabLayout.");
    }

    public void addTab(Tab tab, int position, boolean setSelected) {
        if (tab.mParent == this) {
            addTabView(tab, position, setSelected);
            configureTab(tab, position);
            if (setSelected) {
                tab.select();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Tab belongs to a different OPTabLayout.");
    }

    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        this.mOnTabSelectedListener = onTabSelectedListener;
    }

    public Tab newTab() {
        return new Tab(this);
    }

    public int getTabCount() {
        return this.mTabs.size();
    }

    public Tab getTabAt(int index) {
        return (Tab) this.mTabs.get(index);
    }

    public int getSelectedTabPosition() {
        return this.mSelectedTab != null ? this.mSelectedTab.getPosition() : -1;
    }

    public void removeTab(Tab tab) {
        if (tab.mParent == this) {
            removeTabAt(tab.getPosition());
            return;
        }
        throw new IllegalArgumentException("Tab does not belong to this OPTabLayout.");
    }

    public void removeTabAt(int position) {
        int selectedTabPosition = this.mSelectedTab != null ? this.mSelectedTab.getPosition() : 0;
        removeTabViewAt(position);
        Tab removedTab = (Tab) this.mTabs.remove(position);
        if (removedTab != null) {
            removedTab.setPosition(-1);
        }
        int newTabCount = this.mTabs.size();
        for (int i = position; i < newTabCount; i++) {
            ((Tab) this.mTabs.get(i)).setPosition(i);
        }
        if (selectedTabPosition == position) {
            selectTab(this.mTabs.isEmpty() ? null : (Tab) this.mTabs.get(Math.max(0, position - 1)));
        }
    }

    public void removeAllTabs() {
        this.mTabStrip.removeAllViews();
        Iterator<Tab> i = this.mTabs.iterator();
        while (i.hasNext()) {
            ((Tab) i.next()).setPosition(-1);
            i.remove();
        }
        this.mSelectedTab = null;
    }

    public void setTabMode(int mode) {
        if (mode != this.mMode) {
            this.mMode = mode;
            applyModeAndGravity();
        }
    }

    public int getTabMode() {
        return this.mMode;
    }

    public void setTabGravity(int gravity) {
        if (this.mTabGravity != gravity) {
            this.mTabGravity = gravity;
            applyModeAndGravity();
        }
    }

    public int getTabGravity() {
        return this.mTabGravity;
    }

    public void setTabTextColors(ColorStateList textColor) {
        if (this.mTabTextColors != textColor) {
            this.mTabTextColors = textColor;
            updateAllTabs();
        }
    }

    public ColorStateList getTabTextColors() {
        return this.mTabTextColors;
    }

    public void setTabTextColors(int normalColor, int selectedColor) {
        setTabTextColors(createColorStateList(normalColor, selectedColor));
    }

    private void updateAllTabs() {
        int z = this.mTabStrip.getChildCount();
        for (int i = 0; i < z; i++) {
            updateTab(i);
        }
    }

    private TabView createTabView(Tab tab) {
        TabView tabView = new TabView(getContext(), tab);
        tabView.setFocusable(true);
        if (this.mTabClickListener == null) {
            this.mTabClickListener = new OnClickListener() {
                public void onClick(View view) {
                    ((TabView) view).getTab().select();
                }
            };
        }
        tabView.setOnClickListener(this.mTabClickListener);
        return tabView;
    }

    private void configureTab(Tab tab, int position) {
        tab.setPosition(position);
        this.mTabs.add(position, tab);
        int count = this.mTabs.size();
        for (int i = position + 1; i < count; i++) {
            ((Tab) this.mTabs.get(i)).setPosition(i);
        }
    }

    private void updateTab(int position) {
        TabView view = (TabView) this.mTabStrip.getChildAt(position);
        if (view != null) {
            view.update();
        }
    }

    private void addTabView(Tab tab, boolean setSelected) {
        TabView tabView = createTabView(tab);
        this.mTabStrip.addView(tabView, createLayoutParamsForTabs());
        updateTabViewsMargin();
        if (setSelected) {
            tabView.setSelected(true);
        }
    }

    private void addTabView(Tab tab, int position, boolean setSelected) {
        TabView tabView = createTabView(tab);
        this.mTabStrip.addView(tabView, position, createLayoutParamsForTabs());
        updateTabViewsMargin();
        if (setSelected) {
            tabView.setSelected(true);
        }
    }

    private void updateTabViewsMargin() {
        if (this.mTabStrip.getChildCount() > 0) {
            ((LayoutParams) this.mTabStrip.getChildAt(0).getLayoutParams()).setMarginStart(0);
        }
    }

    private LayoutParams createLayoutParamsForTabs() {
        LayoutParams lp = new LayoutParams(-2, -1);
        lp.setMarginStart(this.mTabHorizontalSpacing);
        updateTabViewLayoutParams(lp);
        return lp;
    }

    private void updateTabViewLayoutParams(LayoutParams lp) {
        if (this.mMode == 1 && this.mTabGravity == 0) {
            lp.width = 0;
            lp.weight = 1.0f;
            return;
        }
        lp.width = -2;
        lp.weight = 0.0f;
    }

    private int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * ((float) dps));
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int idealHeight = (getContext().getResources().getDimensionPixelSize(R.dimen.tab_layout_default_height_material) + getPaddingTop()) + getPaddingBottom();
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == Integer.MIN_VALUE) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(idealHeight, MeasureSpec.getSize(heightMeasureSpec)), Ints.MAX_POWER_OF_TWO);
        } else if (mode == 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight, Ints.MAX_POWER_OF_TWO);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mMode == 1 && getChildCount() == 1) {
            View child = getChildAt(null);
            width = getMeasuredWidth();
            if (child.getMeasuredWidth() > width) {
                child.measure(MeasureSpec.makeMeasureSpec(width, Ints.MAX_POWER_OF_TWO), getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), child.getLayoutParams().height));
            }
        }
        mode = this.mRequestedTabMaxWidth;
        width = getMeasuredWidth() - getDefaultMaxWidth();
        if (mode == 0 || mode > width) {
            mode = width;
        }
        if (this.mTabMaxWidth != mode) {
            this.mTabMaxWidth = mode;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int getDefaultMaxWidth() {
        if (getTabCount() == 0) {
            return this.mRequestedTabMaxWidth;
        }
        return getMeasuredWidth() / getTabCount();
    }

    private void removeTabViewAt(int position) {
        this.mTabStrip.removeViewAt(position);
        updateTabViewsMargin();
        requestLayout();
    }

    private void animateToTab(int newPosition) {
        if (newPosition != -1) {
            if (getWindowToken() == null || !isLaidOut() || this.mTabStrip.childrenNeedLayout()) {
                setScrollPosition(newPosition, 0.0f, true);
                return;
            }
            if (getScrollX() != calculateScrollXForTab(newPosition, 0.0f)) {
                if (this.mScrollAnimator == null) {
                    this.mScrollAnimator = new ValueAnimator();
                    this.mScrollAnimator.setInterpolator(this.fast_out_slow_in_interpolator);
                    this.mScrollAnimator.setDuration(300);
                    this.mScrollAnimator.addUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            OPTabLayout.this.scrollTo(((Integer) animation.getAnimatedValue()).intValue(), 0);
                        }
                    });
                }
                this.mScrollAnimator.setIntValues(new int[]{startScrollX, targetScrollX});
                this.mScrollAnimator.start();
            }
            this.mTabStrip.animateIndicatorToPosition(newPosition, 300);
        }
    }

    private void setSelectedTabView(int position) {
        int tabCount = this.mTabStrip.getChildCount();
        if (position < tabCount && !this.mTabStrip.getChildAt(position).isSelected()) {
            int i = 0;
            while (i < tabCount) {
                this.mTabStrip.getChildAt(i).setSelected(i == position);
                i++;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void selectTab(Tab tab) {
        selectTab(tab, true);
    }

    /* Access modifiers changed, original: 0000 */
    public void selectTab(Tab tab, boolean updateIndicator) {
        if (this.mSelectedTab != tab) {
            int newPosition = tab != null ? tab.getPosition() : -1;
            setSelectedTabView(newPosition);
            if (updateIndicator) {
                if ((this.mSelectedTab == null || this.mSelectedTab.getPosition() == -1) && newPosition != -1) {
                    setScrollPosition(newPosition, 0.0f, true);
                } else {
                    animateToTab(newPosition);
                }
            }
            if (!(this.mSelectedTab == null || this.mOnTabSelectedListener == null)) {
                this.mOnTabSelectedListener.onTabUnselected(this.mSelectedTab);
            }
            this.mSelectedTab = tab;
            if (this.mSelectedTab != null && this.mOnTabSelectedListener != null) {
                this.mOnTabSelectedListener.onTabSelected(this.mSelectedTab);
            }
        } else if (this.mSelectedTab != null) {
            if (this.mOnTabSelectedListener != null) {
                this.mOnTabSelectedListener.onTabReselected(this.mSelectedTab);
            }
            animateToTab(tab.getPosition());
        }
    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        int nextWidth = 0;
        if (this.mMode != 0) {
            return 0;
        }
        View nextChild;
        View selectedChild = this.mTabStrip.getChildAt(position);
        if (position + 1 < this.mTabStrip.getChildCount()) {
            nextChild = this.mTabStrip.getChildAt(position + 1);
        } else {
            nextChild = null;
        }
        int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
        if (nextChild != null) {
            nextWidth = nextChild.getWidth();
        }
        return ((selectedChild.getLeft() + ((int) ((((float) (selectedWidth + nextWidth)) * positionOffset) * 0.5f))) + (selectedChild.getWidth() / 2)) - (getWidth() / 2);
    }

    private void applyModeAndGravity() {
        int paddingStart = 0;
        if (this.mMode == 0) {
            paddingStart = this.mContentInsetStart;
        }
        this.mTabStrip.setPaddingRelative(paddingStart, 0, 0, 0);
        switch (this.mMode) {
            case 0:
                this.mTabStrip.setGravity(GravityCompat.START);
                break;
            case 1:
                this.mTabStrip.setGravity(1);
                break;
        }
        updateTabViewsLayoutParams();
    }

    private void updateTabViewsLayoutParams() {
        for (int i = 0; i < this.mTabStrip.getChildCount(); i++) {
            View child = this.mTabStrip.getChildAt(i);
            updateTabViewLayoutParams((LayoutParams) child.getLayoutParams());
            child.requestLayout();
        }
    }

    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        states = new int[2][];
        int[] colors = new int[]{SELECTED_STATE_SET, selectedColor};
        int i = 0 + 1;
        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i++;
        return new ColorStateList(states, colors);
    }

    private ColorStateList loadTextColorFromTextAppearance(int textAppearanceResId) {
        TypedArray a = getContext().obtainStyledAttributes(textAppearanceResId, R.styleable.TextAppearance);
        try {
            ColorStateList colorStateList = a.getColorStateList(R.styleable.TextAppearance_android_textColor);
            return colorStateList;
        } finally {
            a.recycle();
        }
    }
}
