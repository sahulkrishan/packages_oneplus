package com.oneplus.lib.design.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.StyleRes;
import android.support.v4.util.Pools.Pool;
import android.support.v4.util.Pools.SynchronizedPool;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.content.res.AppCompatResources;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.google.common.primitives.Ints;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.menu.MenuBuilder;
import com.oneplus.lib.menu.MenuItemImpl;
import com.oneplus.lib.menu.MenuView;

@RestrictTo({Scope.LIBRARY_GROUP})
public class BottomNavigationMenuView extends ViewGroup implements MenuView {
    private static final long ACTIVE_ANIMATION_DURATION_MS = 115;
    private static final int[] CHECKED_STATE_SET = new int[]{16842912};
    private static final int[] DISABLED_STATE_SET = new int[]{-16842910};
    private final int activeItemMaxWidth;
    private final int activeItemMinWidth;
    private BottomNavigationItemView[] buttons;
    private final int inactiveItemMaxWidth;
    private final int inactiveItemMinWidth;
    private Drawable itemBackground;
    private int itemBackgroundRes;
    private final int itemHeight;
    private boolean itemHorizontalTranslationEnabled;
    @Dimension
    private int itemIconSize;
    private ColorStateList itemIconTint;
    private final Pool<BottomNavigationItemView> itemPool;
    @StyleRes
    private int itemTextAppearanceActive;
    @StyleRes
    private int itemTextAppearanceInactive;
    private final ColorStateList itemTextColorDefault;
    private ColorStateList itemTextColorFromUser;
    private int labelVisibilityMode;
    private MenuBuilder menu;
    private final OnClickListener onClickListener;
    private BottomNavigationPresenter presenter;
    private int selectedItemId;
    private int selectedItemPosition;
    private final TransitionSet set;
    private int[] tempChildWidths;

    public BottomNavigationMenuView(Context context) {
        this(context, null);
    }

    public BottomNavigationMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.itemPool = new SynchronizedPool(5);
        this.selectedItemId = 0;
        this.selectedItemPosition = 0;
        Resources res = getResources();
        this.inactiveItemMaxWidth = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_max_width);
        this.inactiveItemMinWidth = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_min_width);
        this.activeItemMaxWidth = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_max_width);
        this.activeItemMinWidth = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_min_width);
        this.itemHeight = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_height);
        this.itemTextColorDefault = createDefaultColorStateList(16842808);
        this.set = new AutoTransition();
        this.set.setOrdering(0);
        this.set.setDuration(ACTIVE_ANIMATION_DURATION_MS);
        this.set.setInterpolator(new FastOutSlowInInterpolator());
        this.set.addTransition(new TextScale());
        this.onClickListener = new OnClickListener() {
            public void onClick(View v) {
                MenuItem item = ((BottomNavigationItemView) v).getItemData();
                if (!BottomNavigationMenuView.this.menu.performItemAction(item, BottomNavigationMenuView.this.presenter, 0)) {
                    item.setChecked(true);
                }
            }
        };
        this.tempChildWidths = new int[5];
    }

    public void initialize(MenuBuilder menu) {
        this.menu = menu;
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int childWidth;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int visibleCount = this.menu.getVisibleItems().size();
        int totalCount = getChildCount();
        int heightSpec = MeasureSpec.makeMeasureSpec(this.itemHeight, Ints.MAX_POWER_OF_TWO);
        int i2 = 8;
        if (isShifting(this.labelVisibilityMode, visibleCount) && this.itemHorizontalTranslationEnabled) {
            View activeChild = getChildAt(this.selectedItemPosition);
            int activeItemWidth = this.activeItemMinWidth;
            if (activeChild.getVisibility() != 8) {
                activeChild.measure(MeasureSpec.makeMeasureSpec(this.activeItemMaxWidth, Integer.MIN_VALUE), heightSpec);
                activeItemWidth = Math.max(activeItemWidth, activeChild.getMeasuredWidth());
            }
            int inactiveCount = visibleCount - (activeChild.getVisibility() != 8 ? 1 : 0);
            int activeWidth = Math.min(width - (this.inactiveItemMinWidth * inactiveCount), Math.min(activeItemWidth, this.activeItemMaxWidth));
            int inactiveWidth = Math.min((width - activeWidth) / (inactiveCount == 0 ? 1 : inactiveCount), this.inactiveItemMaxWidth);
            int extra = (width - activeWidth) - (inactiveWidth * inactiveCount);
            int i3 = 0;
            while (true) {
                i = i3;
                if (i >= totalCount) {
                    break;
                }
                if (getChildAt(i).getVisibility() != i2) {
                    this.tempChildWidths[i] = i == this.selectedItemPosition ? activeWidth : inactiveWidth;
                    if (extra > 0) {
                        int[] iArr = this.tempChildWidths;
                        iArr[i] = iArr[i] + 1;
                        extra--;
                    }
                } else {
                    this.tempChildWidths[i] = 0;
                }
                i3 = i + 1;
                i2 = 8;
            }
        } else {
            childWidth = Math.min(width / (visibleCount == 0 ? 1 : visibleCount), this.activeItemMaxWidth);
            int extra2 = width - (childWidth * visibleCount);
            for (i2 = 0; i2 < totalCount; i2++) {
                if (getChildAt(i2).getVisibility() != 8) {
                    this.tempChildWidths[i2] = childWidth;
                    if (extra2 > 0) {
                        int[] iArr2 = this.tempChildWidths;
                        iArr2[i2] = iArr2[i2] + 1;
                        extra2--;
                    }
                } else {
                    this.tempChildWidths[i2] = 0;
                }
            }
        }
        childWidth = 0;
        for (i = 0; i < totalCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                child.measure(MeasureSpec.makeMeasureSpec(this.tempChildWidths[i], Ints.MAX_POWER_OF_TWO), heightSpec);
                child.getLayoutParams().width = child.getMeasuredWidth();
                childWidth += child.getMeasuredWidth();
            }
        }
        setMeasuredDimension(View.resolveSizeAndState(childWidth, MeasureSpec.makeMeasureSpec(childWidth, Ints.MAX_POWER_OF_TWO), 0), View.resolveSizeAndState(this.itemHeight, heightSpec, 0));
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        int width = right - left;
        int height = bottom - top;
        int used = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                if (ViewCompat.getLayoutDirection(this) == 1) {
                    child.layout((width - used) - child.getMeasuredWidth(), 0, width - used, height);
                } else {
                    child.layout(used, 0, child.getMeasuredWidth() + used, height);
                }
                used += child.getMeasuredWidth();
            }
        }
    }

    public int getWindowAnimations() {
        return 0;
    }

    public void setIconTintList(ColorStateList tint) {
        this.itemIconTint = tint;
        if (this.buttons != null) {
            for (BottomNavigationItemView item : this.buttons) {
                item.setIconTintList(tint);
            }
        }
    }

    @Nullable
    public ColorStateList getIconTintList() {
        return this.itemIconTint;
    }

    public void setItemIconSize(@Dimension int iconSize) {
        this.itemIconSize = iconSize;
        if (this.buttons != null) {
            for (BottomNavigationItemView item : this.buttons) {
                item.setIconSize(iconSize);
            }
        }
    }

    @Dimension
    public int getItemIconSize() {
        return this.itemIconSize;
    }

    public void setItemTextColor(ColorStateList color) {
        this.itemTextColorFromUser = color;
        if (this.buttons != null) {
            for (BottomNavigationItemView item : this.buttons) {
                item.setTextColor(color);
            }
        }
    }

    public ColorStateList getItemTextColor() {
        return this.itemTextColorFromUser;
    }

    public void setItemTextAppearanceInactive(@StyleRes int textAppearanceRes) {
        this.itemTextAppearanceInactive = textAppearanceRes;
        if (this.buttons != null) {
            for (BottomNavigationItemView item : this.buttons) {
                item.setTextAppearanceInactive(textAppearanceRes);
                if (this.itemTextColorFromUser != null) {
                    item.setTextColor(this.itemTextColorFromUser);
                }
            }
        }
    }

    @StyleRes
    public int getItemTextAppearanceInactive() {
        return this.itemTextAppearanceInactive;
    }

    public void setItemTextAppearanceActive(@StyleRes int textAppearanceRes) {
        this.itemTextAppearanceActive = textAppearanceRes;
        if (this.buttons != null) {
            for (BottomNavigationItemView item : this.buttons) {
                item.setTextAppearanceActive(textAppearanceRes);
                if (this.itemTextColorFromUser != null) {
                    item.setTextColor(this.itemTextColorFromUser);
                }
            }
        }
    }

    @StyleRes
    public int getItemTextAppearanceActive() {
        return this.itemTextAppearanceActive;
    }

    public void setItemBackgroundRes(int background) {
        this.itemBackgroundRes = background;
        if (this.buttons != null) {
            for (BottomNavigationItemView item : this.buttons) {
                item.setItemBackground(background);
            }
        }
    }

    @Deprecated
    public int getItemBackgroundRes() {
        return this.itemBackgroundRes;
    }

    public void setItemBackground(@Nullable Drawable background) {
        this.itemBackground = background;
        if (this.buttons != null) {
            for (BottomNavigationItemView item : this.buttons) {
                item.setItemBackground(background);
            }
        }
    }

    @Nullable
    public Drawable getItemBackground() {
        if (this.buttons == null || this.buttons.length <= 0) {
            return this.itemBackground;
        }
        return this.buttons[0].getBackground();
    }

    public void setLabelVisibilityMode(int labelVisibilityMode) {
        this.labelVisibilityMode = labelVisibilityMode;
    }

    public int getLabelVisibilityMode() {
        return this.labelVisibilityMode;
    }

    public void setItemHorizontalTranslationEnabled(boolean itemHorizontalTranslationEnabled) {
        this.itemHorizontalTranslationEnabled = itemHorizontalTranslationEnabled;
    }

    public boolean isItemHorizontalTranslationEnabled() {
        return this.itemHorizontalTranslationEnabled;
    }

    public ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
        TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
            return null;
        }
        ColorStateList baseColor = AppCompatResources.getColorStateList(getContext(), value.resourceId);
        if (!getContext().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.colorPrimary, value, true)) {
            return null;
        }
        int colorPrimary = value.data;
        int defaultColor = baseColor.getDefaultColor();
        return new ColorStateList(new int[][]{DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET}, new int[]{baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor});
    }

    public void setPresenter(BottomNavigationPresenter presenter) {
        this.presenter = presenter;
    }

    public void buildMenuView() {
        int i;
        removeAllViews();
        if (this.buttons != null) {
            for (BottomNavigationItemView item : this.buttons) {
                if (item != null) {
                    this.itemPool.release(item);
                }
            }
        }
        if (this.menu.size() == 0) {
            this.selectedItemId = 0;
            this.selectedItemPosition = 0;
            this.buttons = null;
            return;
        }
        this.buttons = new BottomNavigationItemView[this.menu.size()];
        boolean shifting = isShifting(this.labelVisibilityMode, this.menu.getVisibleItems().size());
        for (i = 0; i < this.menu.size(); i++) {
            this.presenter.setUpdateSuspended(true);
            this.menu.getItem(i).setCheckable(true);
            this.presenter.setUpdateSuspended(false);
            BottomNavigationItemView child = getNewItem();
            this.buttons[i] = child;
            child.setIconTintList(this.itemIconTint);
            child.setIconSize(this.itemIconSize);
            child.setTextColor(this.itemTextColorDefault);
            child.setTextAppearanceInactive(this.itemTextAppearanceInactive);
            child.setTextAppearanceActive(this.itemTextAppearanceActive);
            child.setTextColor(this.itemTextColorFromUser);
            if (this.itemBackground != null) {
                child.setItemBackground(this.itemBackground);
            } else {
                child.setItemBackground(this.itemBackgroundRes);
            }
            child.setShifting(shifting);
            child.setLabelVisibilityMode(this.labelVisibilityMode);
            child.initialize((MenuItemImpl) this.menu.getItem(i), 0);
            child.setItemPosition(i);
            child.setOnClickListener(this.onClickListener);
            addView(child);
        }
        this.selectedItemPosition = Math.min(this.menu.size() - 1, this.selectedItemPosition);
        this.menu.getItem(this.selectedItemPosition).setChecked(true);
    }

    public void updateMenuView() {
        if (this.menu != null && this.buttons != null) {
            int menuSize = this.menu.size();
            if (menuSize != this.buttons.length) {
                buildMenuView();
                return;
            }
            int previousSelectedId = this.selectedItemId;
            for (int i = 0; i < menuSize; i++) {
                MenuItem item = this.menu.getItem(i);
                if (item.isChecked()) {
                    this.selectedItemId = item.getItemId();
                    this.selectedItemPosition = i;
                }
            }
            if (previousSelectedId != this.selectedItemId) {
                TransitionManager.beginDelayedTransition(this, this.set);
            }
            boolean shifting = isShifting(this.labelVisibilityMode, this.menu.getVisibleItems().size());
            for (int i2 = 0; i2 < menuSize; i2++) {
                this.presenter.setUpdateSuspended(true);
                this.buttons[i2].setLabelVisibilityMode(this.labelVisibilityMode);
                this.buttons[i2].setShifting(shifting);
                this.buttons[i2].initialize((MenuItemImpl) this.menu.getItem(i2), 0);
                this.presenter.setUpdateSuspended(false);
            }
        }
    }

    private BottomNavigationItemView getNewItem() {
        BottomNavigationItemView item = (BottomNavigationItemView) this.itemPool.acquire();
        if (item == null) {
            return new BottomNavigationItemView(getContext());
        }
        return item;
    }

    public int getSelectedItemId() {
        return this.selectedItemId;
    }

    private boolean isShifting(int labelVisibilityMode, int childCount) {
        if (labelVisibilityMode == -1) {
            if (childCount <= 3) {
                return false;
            }
        } else if (labelVisibilityMode != 0) {
            return false;
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void tryRestoreSelectedItemId(int itemId) {
        int size = this.menu.size();
        for (int i = 0; i < size; i++) {
            MenuItem item = this.menu.getItem(i);
            if (itemId == item.getItemId()) {
                this.selectedItemId = itemId;
                this.selectedItemPosition = i;
                item.setChecked(true);
                return;
            }
        }
    }
}
