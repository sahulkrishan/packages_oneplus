package com.oneplus.lib.app.appcompat;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View.MeasureSpec;
import android.view.Window.Callback;
import android.widget.FrameLayout;
import com.google.common.primitives.Ints;
import com.oneplus.lib.menu.MenuPresenter;
import com.oneplus.lib.widget.actionbar.DecorToolbar;
import com.oneplus.lib.widget.actionbar.Toolbar;

public class ContentFrameLayout extends FrameLayout implements DecorContentParent {
    private OnAttachListener mAttachListener;
    private final Rect mDecorPadding;
    private DecorToolbar mDecorToolbar;
    private TypedValue mFixedHeightMajor;
    private TypedValue mFixedHeightMinor;
    private TypedValue mFixedWidthMajor;
    private TypedValue mFixedWidthMinor;
    private TypedValue mMinWidthMajor;
    private TypedValue mMinWidthMinor;
    private Toolbar mToolbar;

    public interface OnAttachListener {
        void onAttachedFromWindow();

        void onDetachedFromWindow();
    }

    public ContentFrameLayout(Context context) {
        this(context, null);
    }

    public ContentFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mDecorPadding = new Rect();
    }

    /* Access modifiers changed, original: 0000 */
    public void pullChildren() {
        this.mDecorToolbar = getDecorToolbar();
    }

    public void setSupportToolbar(Toolbar toolbar) {
        this.mToolbar = toolbar;
    }

    public DecorToolbar getDecorToolbar() {
        if (this.mToolbar == null) {
            return null;
        }
        return this.mToolbar.getSupportWrap();
    }

    public void setWindowCallback(Callback cb) {
        pullChildren();
        if (this.mDecorToolbar != null) {
            this.mDecorToolbar.setWindowCallback(cb);
        }
    }

    public void setWindowTitle(CharSequence title) {
        pullChildren();
        if (this.mDecorToolbar != null) {
            this.mDecorToolbar.setWindowTitle(title);
        }
    }

    public CharSequence getTitle() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            return this.mDecorToolbar.getTitle();
        }
        return "";
    }

    public void initFeature(int windowFeature) {
    }

    public void setUiOptions(int uiOptions) {
    }

    public boolean hasIcon() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            return this.mDecorToolbar.hasIcon();
        }
        return false;
    }

    public boolean hasLogo() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            return this.mDecorToolbar.hasLogo();
        }
        return false;
    }

    public void setIcon(int resId) {
        pullChildren();
        if (this.mDecorToolbar != null) {
            this.mDecorToolbar.setIcon(resId);
        }
    }

    public void setIcon(Drawable d) {
        pullChildren();
        if (this.mDecorToolbar != null) {
            this.mDecorToolbar.setIcon(d);
        }
    }

    public void setLogo(int resId) {
        pullChildren();
        if (this.mDecorToolbar != null) {
            this.mDecorToolbar.setLogo(resId);
        }
    }

    public boolean canShowOverflowMenu() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            return this.mDecorToolbar.canShowOverflowMenu();
        }
        return false;
    }

    public boolean isOverflowMenuShowing() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            return this.mDecorToolbar.isOverflowMenuShowing();
        }
        return false;
    }

    public boolean isOverflowMenuShowPending() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            return this.mDecorToolbar.isOverflowMenuShowPending();
        }
        return false;
    }

    public boolean showOverflowMenu() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            return this.mDecorToolbar.showOverflowMenu();
        }
        return false;
    }

    public boolean hideOverflowMenu() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            return this.mDecorToolbar.hideOverflowMenu();
        }
        return false;
    }

    public void setMenuPrepared() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            this.mDecorToolbar.setMenuPrepared();
        }
    }

    public void setMenu(Menu menu, MenuPresenter.Callback cb) {
        pullChildren();
        if (this.mDecorToolbar != null) {
            this.mDecorToolbar.setMenu(menu, cb);
        }
    }

    public void saveToolbarHierarchyState(SparseArray<Parcelable> sparseArray) {
    }

    public void restoreToolbarHierarchyState(SparseArray<Parcelable> sparseArray) {
    }

    public void dismissPopups() {
        pullChildren();
        if (this.mDecorToolbar != null) {
            this.mDecorToolbar.dismissPopupMenus();
        }
    }

    @RestrictTo({Scope.GROUP_ID})
    public void dispatchFitSystemWindows(Rect insets) {
        fitSystemWindows(insets);
    }

    public void setAttachListener(OnAttachListener attachListener) {
        this.mAttachListener = attachListener;
    }

    @RestrictTo({Scope.GROUP_ID})
    public void setDecorPadding(int left, int top, int right, int bottom) {
        this.mDecorPadding.set(left, top, right, bottom);
        if (ViewCompat.isLaidOut(this)) {
            requestLayout();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        TypedValue tvw;
        int w;
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        boolean isPortrait = metrics.widthPixels < metrics.heightPixels;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean fixedWidth = false;
        if (widthMode == Integer.MIN_VALUE) {
            tvw = isPortrait ? this.mFixedWidthMinor : this.mFixedWidthMajor;
            if (!(tvw == null || tvw.type == 0)) {
                w = 0;
                if (tvw.type == 5) {
                    w = (int) tvw.getDimension(metrics);
                } else if (tvw.type == 6) {
                    w = (int) tvw.getFraction((float) metrics.widthPixels, (float) metrics.widthPixels);
                }
                if (w > 0) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(w - (this.mDecorPadding.left + this.mDecorPadding.right), MeasureSpec.getSize(widthMeasureSpec)), Ints.MAX_POWER_OF_TWO);
                    fixedWidth = true;
                }
            }
        }
        if (heightMode == Integer.MIN_VALUE) {
            tvw = isPortrait ? this.mFixedHeightMajor : this.mFixedHeightMinor;
            if (!(tvw == null || tvw.type == 0)) {
                w = 0;
                if (tvw.type == 5) {
                    w = (int) tvw.getDimension(metrics);
                } else if (tvw.type == 6) {
                    w = (int) tvw.getFraction((float) metrics.heightPixels, (float) metrics.heightPixels);
                }
                if (w > 0) {
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(w - (this.mDecorPadding.top + this.mDecorPadding.bottom), MeasureSpec.getSize(heightMeasureSpec)), Ints.MAX_POWER_OF_TWO);
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        boolean measure = false;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, Ints.MAX_POWER_OF_TWO);
        if (!fixedWidth && widthMode == Integer.MIN_VALUE) {
            TypedValue tv = isPortrait ? this.mMinWidthMinor : this.mMinWidthMajor;
            if (!(tv == null || tv.type == 0)) {
                int min = 0;
                if (tv.type == 5) {
                    min = (int) tv.getDimension(metrics);
                } else if (tv.type == 6) {
                    min = (int) tv.getFraction((float) metrics.widthPixels, (float) metrics.widthPixels);
                }
                if (min > 0) {
                    min -= this.mDecorPadding.left + this.mDecorPadding.right;
                }
                if (width < min) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(min, Ints.MAX_POWER_OF_TWO);
                    measure = true;
                }
            }
        }
        if (measure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public TypedValue getMinWidthMajor() {
        if (this.mMinWidthMajor == null) {
            this.mMinWidthMajor = new TypedValue();
        }
        return this.mMinWidthMajor;
    }

    public TypedValue getMinWidthMinor() {
        if (this.mMinWidthMinor == null) {
            this.mMinWidthMinor = new TypedValue();
        }
        return this.mMinWidthMinor;
    }

    public TypedValue getFixedWidthMajor() {
        if (this.mFixedWidthMajor == null) {
            this.mFixedWidthMajor = new TypedValue();
        }
        return this.mFixedWidthMajor;
    }

    public TypedValue getFixedWidthMinor() {
        if (this.mFixedWidthMinor == null) {
            this.mFixedWidthMinor = new TypedValue();
        }
        return this.mFixedWidthMinor;
    }

    public TypedValue getFixedHeightMajor() {
        if (this.mFixedHeightMajor == null) {
            this.mFixedHeightMajor = new TypedValue();
        }
        return this.mFixedHeightMajor;
    }

    public TypedValue getFixedHeightMinor() {
        if (this.mFixedHeightMinor == null) {
            this.mFixedHeightMinor = new TypedValue();
        }
        return this.mFixedHeightMinor;
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mAttachListener != null) {
            this.mAttachListener.onAttachedFromWindow();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mAttachListener != null) {
            this.mAttachListener.onDetachedFromWindow();
        }
    }
}
