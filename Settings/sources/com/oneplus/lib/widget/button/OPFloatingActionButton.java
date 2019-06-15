package com.oneplus.lib.widget.button;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import com.google.common.primitives.Ints;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.design.widget.AppBarLayout;
import com.oneplus.lib.design.widget.CoordinatorLayout;
import com.oneplus.lib.design.widget.CoordinatorLayout.DefaultBehavior;
import com.oneplus.lib.design.widget.CoordinatorLayout.LayoutParams;
import com.oneplus.lib.design.widget.Utils;
import java.util.List;

@DefaultBehavior(Behavior.class)
public class OPFloatingActionButton extends ImageView {
    private static final int SIZE_MINI = 1;
    private static final int SIZE_NORMAL = 0;
    private ColorStateList mBackgroundTint;
    private Mode mBackgroundTintMode;
    private int mBorderWidth;
    private int mContentPadding;
    private final OPFloatingActionButtonImpl mImpl;
    private int mRippleColor;
    private final Rect mShadowPadding;
    private int mSize;
    private int mUserSetVisibility;

    public static abstract class OnVisibilityChangedListener {
        public void onShown(OPFloatingActionButton fab) {
        }

        public void onHidden(OPFloatingActionButton fab) {
        }
    }

    public static class Behavior extends com.oneplus.lib.design.widget.CoordinatorLayout.Behavior<OPFloatingActionButton> {
        private static final boolean AUTO_HIDE_DEFAULT = true;
        private boolean mAutoHideEnabled;
        private OnVisibilityChangedListener mInternalAutoHideListener;
        private Rect mTmpRect;

        public Behavior() {
            this.mAutoHideEnabled = true;
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OpFloatingActionButton_Behavior_Layout);
            this.mAutoHideEnabled = a.getBoolean(R.styleable.OpFloatingActionButton_Behavior_Layout_op_behavior_autoHide, true);
            a.recycle();
        }

        public void setAutoHideEnabled(boolean autoHide) {
            this.mAutoHideEnabled = autoHide;
        }

        public boolean isAutoHideEnabled() {
            return this.mAutoHideEnabled;
        }

        public void onAttachedToLayoutParams(@NonNull LayoutParams lp) {
            if (lp.dodgeInsetEdges == 0) {
                lp.dodgeInsetEdges = 80;
            }
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, OPFloatingActionButton child, View dependency) {
            if (dependency instanceof AppBarLayout) {
                updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child);
            } else if (isBottomSheet(dependency)) {
                updateFabVisibilityForBottomSheet(dependency, child);
            }
            return false;
        }

        private static boolean isBottomSheet(@NonNull View view) {
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        @VisibleForTesting
        public void setInternalAutoHideListener(OnVisibilityChangedListener listener) {
            this.mInternalAutoHideListener = listener;
        }

        private boolean shouldUpdateVisibility(View dependency, OPFloatingActionButton child) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (this.mAutoHideEnabled && lp.getAnchorId() == dependency.getId() && child.getUserSetVisibility() == 0) {
                return true;
            }
            return false;
        }

        private boolean updateFabVisibilityForAppBarLayout(CoordinatorLayout parent, AppBarLayout appBarLayout, OPFloatingActionButton child) {
            if (!shouldUpdateVisibility(appBarLayout, child)) {
                return false;
            }
            if (this.mTmpRect == null) {
                this.mTmpRect = new Rect();
            }
            Rect rect = this.mTmpRect;
            Utils.getDescendantRect(parent, appBarLayout, rect);
            if (rect.bottom <= appBarLayout.getMinimumHeightForVisibleOverlappingContent()) {
                child.hide(false);
            } else {
                child.show(false);
            }
            return true;
        }

        private boolean updateFabVisibilityForBottomSheet(View bottomSheet, OPFloatingActionButton child) {
            if (!shouldUpdateVisibility(bottomSheet, child)) {
                return false;
            }
            if (bottomSheet.getTop() < (child.getHeight() / 2) + ((LayoutParams) child.getLayoutParams()).topMargin) {
                child.hide(false);
            } else {
                child.show(false);
            }
            return true;
        }

        public boolean onLayoutChild(CoordinatorLayout parent, OPFloatingActionButton child, int layoutDirection) {
            List<View> dependencies = parent.getDependencies(child);
            int count = dependencies.size();
            for (int i = 0; i < count; i++) {
                View dependency = (View) dependencies.get(i);
                if (!(dependency instanceof AppBarLayout)) {
                    if (isBottomSheet(dependency) && updateFabVisibilityForBottomSheet(dependency, child)) {
                        break;
                    }
                } else if (updateFabVisibilityForAppBarLayout(parent, (AppBarLayout) dependency, child)) {
                    break;
                }
            }
            parent.onLayoutChild(child, layoutDirection);
            offsetIfNeeded(parent, child);
            return true;
        }

        public boolean getInsetDodgeRect(@NonNull CoordinatorLayout parent, @NonNull OPFloatingActionButton child, @NonNull Rect rect) {
            Rect shadowPadding = child.mShadowPadding;
            rect.set(child.getLeft() + shadowPadding.left, child.getTop() + shadowPadding.top, child.getRight() - shadowPadding.right, child.getBottom() - shadowPadding.bottom);
            return true;
        }

        private void offsetIfNeeded(CoordinatorLayout parent, OPFloatingActionButton fab) {
            Rect padding = fab.mShadowPadding;
            if (padding != null && padding.centerX() > 0 && padding.centerY() > 0) {
                LayoutParams lp = (LayoutParams) fab.getLayoutParams();
                int offsetTB = 0;
                int offsetLR = 0;
                if (fab.getRight() >= parent.getWidth() - lp.rightMargin) {
                    offsetLR = padding.right;
                } else if (fab.getLeft() <= lp.leftMargin) {
                    offsetLR = -padding.left;
                }
                if (fab.getBottom() >= parent.getHeight() - lp.bottomMargin) {
                    offsetTB = padding.bottom;
                } else if (fab.getTop() <= lp.topMargin) {
                    offsetTB = -padding.top;
                }
                if (offsetTB != 0) {
                    ViewCompat.offsetTopAndBottom(fab, offsetTB);
                }
                if (offsetLR != 0) {
                    ViewCompat.offsetLeftAndRight(fab, offsetLR);
                }
            }
        }
    }

    public OPFloatingActionButton(Context context) {
        this(context, null);
    }

    public OPFloatingActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.OPFloatingActionButtonStyle);
    }

    public OPFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mUserSetVisibility = getVisibility();
        this.mShadowPadding = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OPFloatingActionButton, defStyleAttr, R.style.OnePlus_Widget_Design_FloatingActionButton);
        Drawable background = a.getDrawable(R.styleable.OPFloatingActionButton_android_background);
        this.mBackgroundTint = a.getColorStateList(R.styleable.OPFloatingActionButton_op_backgroundTint);
        this.mBackgroundTintMode = parseTintMode(a.getInt(R.styleable.OPFloatingActionButton_op_backgroundTintMode, -1), null);
        this.mRippleColor = a.getColor(R.styleable.OPFloatingActionButton_op_rippleColor, 0);
        this.mSize = a.getInt(R.styleable.OPFloatingActionButton_op_fabSize, 0);
        this.mBorderWidth = a.getDimensionPixelSize(R.styleable.OPFloatingActionButton_op_borderWidth, 0);
        float op_elevation = a.getDimension(R.styleable.OPFloatingActionButton_op_elevation, 0.0f);
        float op_pressedTranslationZ = a.getDimension(R.styleable.OPFloatingActionButton_op_pressedTranslationZ, 0.0f);
        a.recycle();
        this.mImpl = new OPFloatingActionButtonImpl(this, new OPShadowViewDelegate() {
            public float getRadius() {
                return ((float) OPFloatingActionButton.this.getSizeDimension()) / 2.0f;
            }

            public void setShadowPadding(int left, int top, int right, int bottom) {
                OPFloatingActionButton.this.mShadowPadding.set(left, top, right, bottom);
                OPFloatingActionButton.this.setPadding(OPFloatingActionButton.this.mContentPadding + left, OPFloatingActionButton.this.mContentPadding + top, OPFloatingActionButton.this.mContentPadding + right, OPFloatingActionButton.this.mContentPadding + bottom);
            }

            public void setBackground(Drawable background) {
                super.setBackground(background);
            }
        });
        this.mContentPadding = (getSizeDimension() - ((int) getResources().getDimension(R.dimen.design_fab_content_size))) / 2;
        this.mImpl.setBackground(background, this.mBackgroundTint, this.mBackgroundTintMode, this.mRippleColor, this.mBorderWidth);
        this.mImpl.setElevation(op_elevation);
        this.mImpl.setPressedTranslationZ(op_pressedTranslationZ);
        setClickable(true);
    }

    public void setVisibility(int visibility) {
        internalSetVisibility(visibility, true);
    }

    /* Access modifiers changed, original: final */
    public final void internalSetVisibility(int visibility, boolean fromUser) {
        super.setVisibility(visibility);
        if (fromUser) {
            this.mUserSetVisibility = visibility;
        }
    }

    /* Access modifiers changed, original: final */
    public final int getUserSetVisibility() {
        return this.mUserSetVisibility;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int preferredSize = getSizeDimension();
        int d = Math.min(resolveAdjustedSize(preferredSize, widthMeasureSpec), resolveAdjustedSize(preferredSize, heightMeasureSpec));
        setMeasuredDimension((this.mShadowPadding.left + d) + this.mShadowPadding.right, (this.mShadowPadding.top + d) + this.mShadowPadding.bottom);
    }

    public void setRippleColor(int color) {
        if (this.mRippleColor != color) {
            this.mRippleColor = color;
            this.mImpl.setRippleColor(color);
        }
    }

    public ColorStateList getBackgroundTintList() {
        return this.mBackgroundTint;
    }

    public void setBackgroundTintList(ColorStateList tint) {
        if (this.mBackgroundTint != tint) {
            this.mBackgroundTint = tint;
            this.mImpl.setBackgroundTintList(tint);
        }
    }

    public Mode getBackgroundTintMode() {
        return this.mBackgroundTintMode;
    }

    public void setBackgroundTintMode(Mode tintMode) {
        if (this.mBackgroundTintMode != tintMode) {
            this.mBackgroundTintMode = tintMode;
            this.mImpl.setBackgroundTintMode(tintMode);
        }
    }

    public void setBackground(Drawable background) {
        if (this.mImpl != null) {
            this.mImpl.setBackground(background, this.mBackgroundTint, this.mBackgroundTintMode, this.mRippleColor, this.mBorderWidth);
        }
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean fromUser) {
        this.mImpl.show(fromUser);
    }

    public void hide(boolean fromUser) {
        this.mImpl.hide(fromUser);
    }

    /* Access modifiers changed, original: final */
    public final int getSizeDimension() {
        if (this.mSize != 1) {
            return getResources().getDimensionPixelSize(R.dimen.design_fab_size_normal);
        }
        return getResources().getDimensionPixelSize(R.dimen.design_fab_size_mini);
    }

    /* Access modifiers changed, original: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        this.mImpl.onDrawableStateChanged(getDrawableState());
    }

    @TargetApi(11)
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        this.mImpl.jumpDrawableToCurrentState();
    }

    private static int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(desiredSize, specSize);
        }
        if (specMode == 0) {
            return desiredSize;
        }
        if (specMode != Ints.MAX_POWER_OF_TWO) {
            return result;
        }
        return specSize;
    }

    static Mode parseTintMode(int value, Mode defaultMode) {
        if (value == 3) {
            return Mode.SRC_OVER;
        }
        if (value == 5) {
            return Mode.SRC_IN;
        }
        if (value == 9) {
            return Mode.SRC_ATOP;
        }
        switch (value) {
            case 14:
                return Mode.MULTIPLY;
            case 15:
                return Mode.SCREEN;
            default:
                return defaultMode;
        }
    }
}
