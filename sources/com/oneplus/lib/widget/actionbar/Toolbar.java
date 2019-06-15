package com.oneplus.lib.widget.actionbar;

import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.CollapsibleActionView;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.primitives.Ints;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.menu.ActionMenuPresenter;
import com.oneplus.lib.menu.ActionMenuView;
import com.oneplus.lib.menu.ActionMenuView.OnMenuItemClickListener;
import com.oneplus.lib.menu.MenuBuilder;
import com.oneplus.lib.menu.MenuItemImpl;
import com.oneplus.lib.menu.MenuPresenter;
import com.oneplus.lib.menu.MenuPresenter.Callback;
import com.oneplus.lib.menu.MenuView;
import com.oneplus.lib.menu.SubMenuBuilder;
import com.oneplus.lib.widget.util.ViewUtils;
import java.util.ArrayList;
import java.util.List;

public class Toolbar extends android.widget.Toolbar {
    private static final int[] ACTION_BAR_DIVIDER_ATTR = new int[]{R.attr.onePlusActionbarLineColor};
    private static final int MAX_ICON_SIZE = R.dimen.abc_action_menu_icon_size;
    private static final String TAG = "Toolbar";
    private final int ICON_MIN_WIDTH;
    private final int ICON_SIZE_STANDARD;
    private int mActionBarDividerColor;
    private Callback mActionMenuPresenterCallback;
    private int mButtonGravity;
    private ImageButton mCollapseButtonView;
    private CharSequence mCollapseDescription;
    private Drawable mCollapseIcon;
    private boolean mCollapsed;
    private boolean mCollapsible;
    private int mContentInsetEndWithActions;
    private int mContentInsetStartWithNavigation;
    private RtlSpacingHelper mContentInsets;
    private boolean mEatingTouch;
    View mExpandedActionView;
    private ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private int mGravity;
    private boolean mHasActionBarLineColor;
    private final ArrayList<View> mHiddenViews;
    private ImageView mLogoView;
    private int mMaxButtonHeight;
    private int mMaxIconSize;
    private MenuBuilder.Callback mMenuBuilderCallback;
    private ActionMenuView mMenuView;
    private final OnMenuItemClickListener mMenuViewItemClickListener;
    private ImageButton mMyNavButtonView;
    private int mNavButtonStyle;
    private boolean mNeedResetPadding;
    private android.widget.Toolbar.OnMenuItemClickListener mOnMenuItemClickListener;
    private int mOrientation;
    private ActionMenuPresenter mOuterActionMenuPresenter;
    private int mPaddingTopOffset;
    private Context mPopupContext;
    private int mPopupTheme;
    private int mRealPaddingBottom;
    private int mRealTitleMarginBottom;
    private final Runnable mShowOverflowMenuRunnable;
    private int mSubTitleMarginBottom;
    private CharSequence mSubtitleText;
    private int mSubtitleTextAppearance;
    private int mSubtitleTextColor;
    private TextView mSubtitleTextView;
    private final int[] mTempMargins;
    private final ArrayList<View> mTempViews;
    private int mTitleMarginBottom;
    private int mTitleMarginEnd;
    private int mTitleMarginStart;
    private int mTitleMarginTop;
    private CharSequence mTitleText;
    private int mTitleTextAppearance;
    private int mTitleTextColor;
    private TextView mTitleTextView;
    private final int[] mTmpStatesArray;
    private ToolbarWidgetWrapper mWrapper;

    public static class LayoutParams extends android.widget.Toolbar.LayoutParams {
        static final int CUSTOM = 0;
        static final int EXPANDED = 2;
        static final int SYSTEM = 1;
        int mViewType;

        public LayoutParams(@NonNull Context c, AttributeSet attrs) {
            super(c, attrs);
            this.mViewType = 0;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.mViewType = 0;
            this.gravity = 8388627;
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.mViewType = 0;
            this.gravity = gravity;
        }

        public LayoutParams(int gravity) {
            this(-2, -1, gravity);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.mViewType = 0;
            this.mViewType = source.mViewType;
        }

        public LayoutParams(android.app.ActionBar.LayoutParams source) {
            super(source);
            this.mViewType = 0;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            this.mViewType = 0;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.mViewType = 0;
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int expandedMenuItemId;
        public boolean isOverflowOpen;

        public SavedState(Parcel source) {
            super(source);
            this.expandedMenuItemId = source.readInt();
            this.isOverflowOpen = source.readInt() != 0;
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.expandedMenuItemId);
            out.writeInt(this.isOverflowOpen);
        }
    }

    private class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        private ExpandedActionViewMenuPresenter() {
        }

        /* synthetic */ ExpandedActionViewMenuPresenter(Toolbar x0, AnonymousClass1 x1) {
            this();
        }

        public void initForMenu(@NonNull Context context, @Nullable MenuBuilder menu) {
            if (!(this.mMenu == null || this.mCurrentExpandedItem == null)) {
                this.mMenu.collapseItemActionView(this.mCurrentExpandedItem);
            }
            this.mMenu = menu;
        }

        public MenuView getMenuView(ViewGroup root) {
            return null;
        }

        public void updateMenuView(boolean cleared) {
            if (this.mCurrentExpandedItem != null) {
                boolean found = false;
                if (this.mMenu != null) {
                    int count = this.mMenu.size();
                    for (int i = 0; i < count; i++) {
                        if (this.mMenu.getItem(i) == this.mCurrentExpandedItem) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
                }
            }
        }

        public void setCallback(Callback cb) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            return false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean flagActionItems() {
            return false;
        }

        public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
            Toolbar.this.ensureCollapseButtonView();
            if (Toolbar.this.mCollapseButtonView.getParent() != Toolbar.this) {
                Toolbar.this.addView(Toolbar.this.mCollapseButtonView);
            }
            Toolbar.this.mExpandedActionView = item.getActionView();
            this.mCurrentExpandedItem = item;
            if (Toolbar.this.mExpandedActionView.getParent() != Toolbar.this) {
                LayoutParams lp = Toolbar.this.generateDefaultLayoutParams();
                lp.gravity = GravityCompat.START | (Toolbar.this.mButtonGravity & 16);
                lp.mViewType = 2;
                Toolbar.this.mExpandedActionView.setLayoutParams(lp);
                Toolbar.this.addView(Toolbar.this.mExpandedActionView);
            }
            Toolbar.this.removeChildrenForExpandedActionView();
            Toolbar.this.requestLayout();
            item.setActionViewExpanded(true);
            if (Toolbar.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) Toolbar.this.mExpandedActionView).onActionViewExpanded();
            }
            return true;
        }

        public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
            if (Toolbar.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) Toolbar.this.mExpandedActionView).onActionViewCollapsed();
            }
            Toolbar.this.removeView(Toolbar.this.mExpandedActionView);
            Toolbar.this.removeView(Toolbar.this.mCollapseButtonView);
            Toolbar.this.mExpandedActionView = null;
            Toolbar.this.addChildrenForExpandedActionView();
            this.mCurrentExpandedItem = null;
            Toolbar.this.requestLayout();
            item.setActionViewExpanded(false);
            return true;
        }

        public int getId() {
            return 0;
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onRestoreInstanceState(Parcelable state) {
        }
    }

    public Toolbar(Context context) {
        this(context, null);
    }

    public Toolbar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.toolbarStyle);
    }

    public Toolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Toolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        int navDesc;
        Context context2 = context;
        super(context, attrs, defStyleAttr, defStyleRes);
        this.ICON_MIN_WIDTH = R.dimen.toolbar_icon_min_width;
        this.ICON_SIZE_STANDARD = R.dimen.oneplus_contorl_icon_size_button;
        this.mNeedResetPadding = true;
        this.mGravity = 8388627;
        this.mTempViews = new ArrayList();
        this.mHiddenViews = new ArrayList();
        this.mTempMargins = new int[2];
        this.mTmpStatesArray = new int[2];
        this.mMenuViewItemClickListener = new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (Toolbar.this.mOnMenuItemClickListener != null) {
                    return Toolbar.this.mOnMenuItemClickListener.onMenuItemClick(item);
                }
                return false;
            }
        };
        this.mShowOverflowMenuRunnable = new Runnable() {
            public void run() {
                Toolbar.this.showOverflowMenu();
            }
        };
        this.mActionBarDividerColor = getDividerColor(context2, ACTION_BAR_DIVIDER_ATTR[0]);
        TypedArray a = context2.obtainStyledAttributes(attrs, R.styleable.Toolbar, defStyleAttr, defStyleRes);
        this.mCollapsible = a.getBoolean(R.styleable.Toolbar_op_collapsible, false);
        if (this.mCollapsible && VERSION.SDK_INT >= 21) {
            setStateListAnimator(AnimatorInflater.loadStateListAnimator(context2, R.anim.op_design_appbar_state_list_animator));
            setCollapsedState(false);
        }
        this.mOrientation = context.getResources().getConfiguration().orientation;
        this.mTitleTextAppearance = a.getResourceId(R.styleable.Toolbar_titleTextAppearance, 0);
        this.mSubtitleTextAppearance = a.getResourceId(R.styleable.Toolbar_subtitleTextAppearance, 0);
        this.mNavButtonStyle = a.getResourceId(R.styleable.Toolbar_opNavigationButtonStyle, 0);
        this.mGravity = a.getInteger(R.styleable.Toolbar_android_gravity, this.mGravity);
        this.mButtonGravity = a.getInteger(R.styleable.Toolbar_opButtonGravity, 48);
        int dimensionPixelOffset = a.getDimensionPixelOffset(R.styleable.Toolbar_titleMargin, 0);
        this.mTitleMarginBottom = dimensionPixelOffset;
        this.mTitleMarginTop = dimensionPixelOffset;
        this.mTitleMarginEnd = dimensionPixelOffset;
        this.mTitleMarginStart = dimensionPixelOffset;
        this.mMaxIconSize = getResources().getDimensionPixelSize(MAX_ICON_SIZE);
        dimensionPixelOffset = a.getDimensionPixelOffset(R.styleable.Toolbar_titleMarginStart, -1);
        if (dimensionPixelOffset >= 0) {
            this.mTitleMarginStart = dimensionPixelOffset;
        }
        int marginEnd = a.getDimensionPixelOffset(R.styleable.Toolbar_titleMarginEnd, -1);
        if (marginEnd >= 0) {
            this.mTitleMarginEnd = marginEnd;
        }
        int marginTop = a.getDimensionPixelOffset(R.styleable.Toolbar_titleMarginTop, -1);
        if (marginTop >= 0) {
            this.mTitleMarginTop = marginTop;
        }
        int marginBottom = a.getDimensionPixelOffset(R.styleable.Toolbar_titleMarginBottom, -1);
        if (marginBottom >= 0) {
            this.mTitleMarginBottom = marginBottom;
        }
        int subtitleMarginBottom = a.getDimensionPixelOffset(R.styleable.Toolbar_subTitleMarginBottom, -1);
        if (subtitleMarginBottom > 0) {
            this.mSubTitleMarginBottom = subtitleMarginBottom;
        }
        this.mRealPaddingBottom = a.getDimensionPixelOffset(R.styleable.Toolbar_realPaddingBottom, 0);
        this.mRealTitleMarginBottom = a.getDimensionPixelOffset(R.styleable.Toolbar_realTitleMarginBottom, 0);
        this.mMaxButtonHeight = a.getDimensionPixelSize(R.styleable.Toolbar_maxButtonHeight, -1);
        int contentInsetStart = a.getDimensionPixelOffset(R.styleable.Toolbar_contentInsetStart, Integer.MIN_VALUE);
        int contentInsetEnd = a.getDimensionPixelOffset(R.styleable.Toolbar_contentInsetEnd, Integer.MIN_VALUE);
        int contentInsetLeft = a.getDimensionPixelSize(R.styleable.Toolbar_contentInsetLeft, 0);
        int contentInsetRight = a.getDimensionPixelSize(R.styleable.Toolbar_contentInsetRight, 0);
        ensureContentInsets();
        this.mContentInsets.setAbsolute(contentInsetLeft, contentInsetRight);
        if (!(contentInsetStart == Integer.MIN_VALUE && contentInsetEnd == Integer.MIN_VALUE)) {
            this.mContentInsets.setRelative(contentInsetStart, contentInsetEnd);
        }
        this.mContentInsetStartWithNavigation = a.getDimensionPixelOffset(R.styleable.Toolbar_contentInsetStartWithNavigation, Integer.MIN_VALUE);
        this.mContentInsetEndWithActions = a.getDimensionPixelOffset(R.styleable.Toolbar_contentInsetEndWithActions, Integer.MIN_VALUE);
        this.mCollapseIcon = a.getDrawable(R.styleable.Toolbar_collapseIcon);
        this.mCollapseDescription = a.getText(R.styleable.Toolbar_collapseContentDescription);
        CharSequence title = a.getText(R.styleable.Toolbar_title);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        CharSequence subtitle = a.getText(R.styleable.Toolbar_subtitle);
        if (!TextUtils.isEmpty(subtitle)) {
            setSubtitle(subtitle);
        }
        this.mPopupContext = getContext();
        setPopupTheme(a.getResourceId(R.styleable.Toolbar_android_popupTheme, null));
        Drawable navIcon = a.getDrawable(R.styleable.Toolbar_navigationIcon);
        if (navIcon != null) {
            setNavigationIcon(navIcon);
        }
        subtitle = a.getText(R.styleable.Toolbar_navigationContentDescription);
        if (!TextUtils.isEmpty(subtitle)) {
            setNavigationContentDescription(subtitle);
        }
        navIcon = a.getDrawable(R.styleable.Toolbar_android_logo);
        if (navIcon != null) {
            setLogo(navIcon);
        }
        title = a.getText(R.styleable.Toolbar_logoDescription);
        if (!TextUtils.isEmpty(title)) {
            setLogoDescription(title);
        }
        if (a.hasValue(R.styleable.Toolbar_titleTextColor)) {
            navDesc = -1;
            setTitleTextColor(a.getColor(R.styleable.Toolbar_titleTextColor, -1));
        } else {
            navDesc = -1;
        }
        if (a.hasValue(R.styleable.Toolbar_subtitleTextColor)) {
            setSubtitleTextColor(a.getColor(R.styleable.Toolbar_subtitleTextColor, navDesc));
        }
        a.recycle();
    }

    public int getDividerColor(Context context, int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        if (typedValue.resourceId != 0) {
            try {
                int color = getResources().getColor(typedValue.resourceId);
                this.mHasActionBarLineColor = true;
                return color;
            } catch (NotFoundException e) {
                this.mHasActionBarLineColor = false;
            }
        }
        return typedValue.data;
    }

    /* Access modifiers changed, original: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mOrientation != newConfig.orientation) {
            this.mOrientation = newConfig.orientation;
            TypedValue tv = new TypedValue();
            int height = getHeight();
            if (getContext().getTheme().resolveAttribute(16843499, tv, true)) {
                try {
                    height = getResources().getDimensionPixelSize(tv.resourceId);
                } catch (NotFoundException e) {
                }
            }
            android.view.ViewGroup.LayoutParams params = getLayoutParams();
            params.height = height;
            setLayoutParams(params);
        }
    }

    /* Access modifiers changed, original: protected */
    public int[] onCreateDrawableState(int extraSpace) {
        if (!this.mCollapsible) {
            return super.onCreateDrawableState(extraSpace);
        }
        int[] extraStates = this.mTmpStatesArray;
        int[] states = super.onCreateDrawableState(extraStates.length + extraSpace);
        extraStates[0] = this.mCollapsible ? R.attr.op_state_collapsible : -R.attr.op_state_collapsible;
        int i = (this.mCollapsible && this.mCollapsed) ? -R.attr.op_state_collapsed : R.attr.op_state_collapsed;
        extraStates[1] = i;
        return mergeDrawableStates(states, extraStates);
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public DecorToolbar getSupportWrap() {
        if (this.mWrapper == null) {
            this.mWrapper = new ToolbarWidgetWrapper(this, true);
        }
        return this.mWrapper;
    }

    public void setPopupTheme(@StyleRes int resId) {
        if (this.mPopupTheme != resId) {
            this.mPopupTheme = resId;
            if (resId == 0) {
                this.mPopupContext = getContext();
            } else {
                this.mPopupContext = new ContextThemeWrapper(getContext(), resId);
            }
        }
    }

    public int getPopupTheme() {
        return this.mPopupTheme;
    }

    public void setTitleMargin(int start, int top, int end, int bottom) {
        this.mTitleMarginStart = start;
        this.mTitleMarginTop = top;
        this.mTitleMarginEnd = end;
        this.mTitleMarginBottom = bottom;
        requestLayout();
    }

    public int getTitleMarginStart() {
        return this.mTitleMarginStart;
    }

    public void setTitleMarginStart(int margin) {
        this.mTitleMarginStart = margin;
        requestLayout();
    }

    public int getTitleMarginTop() {
        return this.mTitleMarginTop;
    }

    public int getTitleTop() {
        return this.mTitleTextView != null ? this.mTitleTextView.getTop() : 0;
    }

    public int getTitieTopWithoutOffset() {
        return this.mTitleTextView != null ? this.mTitleTextView.getTop() - (2 * this.mPaddingTopOffset) : 0;
    }

    public void setTitleMarginTop(int margin) {
        this.mTitleMarginTop = margin;
        requestLayout();
    }

    public int getTitleMarginEnd() {
        return this.mTitleMarginEnd;
    }

    public void setTitleMarginEnd(int margin) {
        this.mTitleMarginEnd = margin;
        requestLayout();
    }

    public int getTitleMarginBottom() {
        return this.mTitleMarginBottom;
    }

    public void setTitleMarginBottom(int margin) {
        this.mTitleMarginBottom = margin;
        requestLayout();
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        ensureContentInsets();
        RtlSpacingHelper rtlSpacingHelper = this.mContentInsets;
        boolean z = true;
        if (layoutDirection != 1) {
            z = false;
        }
        rtlSpacingHelper.setDirection(z);
    }

    public void setLogo(@DrawableRes int resId) {
        setLogo(getContext().getDrawable(resId));
    }

    public boolean canShowOverflowMenu() {
        return getVisibility() == 0 && this.mMenuView != null && this.mMenuView.isOverflowReserved();
    }

    public boolean isOverflowMenuShowing() {
        return this.mMenuView != null && this.mMenuView.isOverflowMenuShowing();
    }

    public boolean isOverflowMenuShowPending() {
        return this.mMenuView != null && this.mMenuView.isOverflowMenuShowPending();
    }

    public boolean showOverflowMenu() {
        return this.mMenuView != null && this.mMenuView.showOverflowMenu();
    }

    public boolean hideOverflowMenu() {
        return this.mMenuView != null && this.mMenuView.hideOverflowMenu();
    }

    public void setMenu(MenuBuilder menu, ActionMenuPresenter outerPresenter) {
        if (menu != null || this.mMenuView != null) {
            ensureMenuView();
            MenuBuilder oldMenu = this.mMenuView.peekMenu();
            if (oldMenu != menu) {
                if (oldMenu != null) {
                    oldMenu.removeMenuPresenter(this.mOuterActionMenuPresenter);
                    oldMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
                }
                if (this.mExpandedMenuPresenter == null) {
                    this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter(this, null);
                }
                outerPresenter.setExpandedActionViewsExclusive(true);
                if (menu != null) {
                    menu.addMenuPresenter(outerPresenter, this.mPopupContext);
                    menu.addMenuPresenter(this.mExpandedMenuPresenter, this.mPopupContext);
                } else {
                    outerPresenter.initForMenu(this.mPopupContext, null);
                    this.mExpandedMenuPresenter.initForMenu(this.mPopupContext, null);
                    outerPresenter.updateMenuView(true);
                    this.mExpandedMenuPresenter.updateMenuView(true);
                }
                this.mMenuView.setPopupTheme(this.mPopupTheme);
                this.mMenuView.setPresenter(outerPresenter);
                this.mOuterActionMenuPresenter = outerPresenter;
            }
        }
    }

    public void dismissPopupMenus() {
        if (this.mMenuView != null) {
            this.mMenuView.dismissPopupMenus();
        }
    }

    public boolean isTitleTruncated() {
        if (this.mTitleTextView == null) {
            return false;
        }
        Layout titleLayout = this.mTitleTextView.getLayout();
        if (titleLayout == null) {
            return false;
        }
        int lineCount = titleLayout.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            if (titleLayout.getEllipsisCount(i) > 0) {
                return true;
            }
        }
        return false;
    }

    public void setLogo(Drawable drawable) {
        if (drawable != null) {
            ensureLogoView();
            if (!isChildOrHidden(this.mLogoView)) {
                addSystemView(this.mLogoView, true);
            }
        } else if (this.mLogoView != null && isChildOrHidden(this.mLogoView)) {
            removeView(this.mLogoView);
            this.mHiddenViews.remove(this.mLogoView);
        }
        if (this.mLogoView != null) {
            this.mLogoView.setImageDrawable(drawable);
        }
    }

    public Drawable getLogo() {
        return this.mLogoView != null ? this.mLogoView.getDrawable() : null;
    }

    public void setLogoDescription(@StringRes int resId) {
        setLogoDescription(getContext().getText(resId));
    }

    public void setLogoDescription(CharSequence description) {
        if (!TextUtils.isEmpty(description)) {
            ensureLogoView();
        }
        if (this.mLogoView != null) {
            this.mLogoView.setContentDescription(description);
        }
    }

    public CharSequence getLogoDescription() {
        return this.mLogoView != null ? this.mLogoView.getContentDescription() : null;
    }

    private void ensureLogoView() {
        if (this.mLogoView == null) {
            this.mLogoView = new ImageView(getContext());
        }
    }

    public boolean hasExpandedActionView() {
        return (this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null) ? false : true;
    }

    public void collapseActionView() {
        MenuItemImpl item;
        if (this.mExpandedMenuPresenter == null) {
            item = null;
        } else {
            item = this.mExpandedMenuPresenter.mCurrentExpandedItem;
        }
        if (item != null) {
            item.collapseActionView();
        }
    }

    public CharSequence getTitle() {
        return this.mTitleText;
    }

    public void setTitle(@StringRes int resId) {
        setTitle(getContext().getText(resId));
    }

    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            if (this.mTitleTextView == null) {
                this.mTitleTextView = new TextView(getContext());
                this.mTitleTextView.setSingleLine();
                this.mTitleTextView.setEllipsize(TruncateAt.END);
                if (this.mTitleTextAppearance != 0) {
                    this.mTitleTextView.setTextAppearance(getContext(), this.mTitleTextAppearance);
                }
                if (this.mTitleTextColor != 0) {
                    this.mTitleTextView.setTextColor(this.mTitleTextColor);
                }
            }
            if (!isChildOrHidden(this.mTitleTextView)) {
                addSystemView(this.mTitleTextView, true);
            }
        } else if (this.mTitleTextView != null && isChildOrHidden(this.mTitleTextView)) {
            removeView(this.mTitleTextView);
            this.mHiddenViews.remove(this.mTitleTextView);
        }
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setText(title);
        }
        this.mTitleText = title;
    }

    public CharSequence getSubtitle() {
        return this.mSubtitleText;
    }

    public void setSubtitle(@StringRes int resId) {
        setSubtitle(getContext().getText(resId));
    }

    public void setSubtitle(CharSequence subtitle) {
        if (!TextUtils.isEmpty(subtitle)) {
            if (this.mSubtitleTextView == null) {
                this.mSubtitleTextView = new TextView(getContext());
                this.mSubtitleTextView.setSingleLine();
                this.mSubtitleTextView.setEllipsize(TruncateAt.END);
                if (this.mSubtitleTextAppearance != 0) {
                    this.mSubtitleTextView.setTextAppearance(getContext(), this.mSubtitleTextAppearance);
                }
                if (this.mSubtitleTextColor != 0) {
                    this.mSubtitleTextView.setTextColor(this.mSubtitleTextColor);
                }
            }
            if (!isChildOrHidden(this.mSubtitleTextView)) {
                addSystemView(this.mSubtitleTextView, true);
            }
        } else if (this.mSubtitleTextView != null && isChildOrHidden(this.mSubtitleTextView)) {
            removeView(this.mSubtitleTextView);
            this.mHiddenViews.remove(this.mSubtitleTextView);
        }
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setText(subtitle);
        }
        this.mSubtitleText = subtitle;
    }

    public void setTitleTextAppearance(Context context, @StyleRes int resId) {
        this.mTitleTextAppearance = resId;
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setTextAppearance(getContext(), resId);
        }
    }

    public void setSubtitleTextAppearance(Context context, @StyleRes int resId) {
        this.mSubtitleTextAppearance = resId;
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setTextAppearance(getContext(), resId);
        }
    }

    public void setTitleTextColor(@ColorInt int color) {
        this.mTitleTextColor = color;
        if (this.mTitleTextView != null) {
            this.mTitleTextView.setTextColor(color);
        }
    }

    public void setSubtitleTextColor(@ColorInt int color) {
        this.mSubtitleTextColor = color;
        if (this.mSubtitleTextView != null) {
            this.mSubtitleTextView.setTextColor(color);
        }
    }

    @Nullable
    public CharSequence getNavigationContentDescription() {
        return this.mMyNavButtonView != null ? this.mMyNavButtonView.getContentDescription() : null;
    }

    public void setNavigationContentDescription(@StringRes int resId) {
        setNavigationContentDescription(resId != 0 ? getContext().getText(resId) : null);
    }

    public void setNavigationContentDescription(@Nullable CharSequence description) {
        if (!TextUtils.isEmpty(description)) {
            ensureNavButtonView();
        }
        if (this.mMyNavButtonView != null) {
            this.mMyNavButtonView.setContentDescription(description);
        }
    }

    public void setNavigationIcon(@DrawableRes int resId) {
        setNavigationIcon(getContext().getDrawable(resId));
    }

    public void setNavigationIcon(@Nullable Drawable icon) {
        if (icon != null) {
            ensureNavButtonView();
            if (!isChildOrHidden(this.mMyNavButtonView)) {
                addSystemView(this.mMyNavButtonView, true);
            }
        } else if (this.mMyNavButtonView != null && isChildOrHidden(this.mMyNavButtonView)) {
            removeView(this.mMyNavButtonView);
            this.mHiddenViews.remove(this.mMyNavButtonView);
        }
        if (this.mMyNavButtonView != null) {
            this.mMyNavButtonView.setImageDrawable(icon);
        }
    }

    private void resetNavgationIconBound(Drawable icon) {
        if (icon != null) {
            float scale;
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if (width > this.mMaxIconSize) {
                scale = ((float) this.mMaxIconSize) / ((float) width);
                width = this.mMaxIconSize;
                height = (int) (((float) width) * scale);
            }
            if (height > this.mMaxIconSize) {
                scale = ((float) this.mMaxIconSize) / ((float) height);
                height = this.mMaxIconSize;
                width = (int) (((float) width) * scale);
            }
            icon.setBounds(0, 0, width, height);
        }
    }

    @Nullable
    public Drawable getNavigationIcon() {
        return this.mMyNavButtonView != null ? this.mMyNavButtonView.getDrawable() : null;
    }

    public void setNavigationOnClickListener(OnClickListener listener) {
        ensureNavButtonView();
        this.mMyNavButtonView.setOnClickListener(listener);
    }

    @Nullable
    public View getNavigationView() {
        return this.mMyNavButtonView;
    }

    public Menu getMenu() {
        ensureMenu();
        return this.mMenuView.getMenu();
    }

    public void setOverflowIcon(@Nullable Drawable icon) {
        ensureMenu();
        this.mMenuView.setOverflowIcon(icon);
    }

    @Nullable
    public Drawable getOverflowIcon() {
        ensureMenu();
        return this.mMenuView.getOverflowIcon();
    }

    private void ensureMenu() {
        ensureMenuView();
        if (this.mMenuView.peekMenu() == null) {
            MenuBuilder menu = (MenuBuilder) this.mMenuView.getMenu();
            if (this.mExpandedMenuPresenter == null) {
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter(this, null);
            }
            this.mMenuView.setExpandedActionViewsExclusive(true);
            menu.addMenuPresenter(this.mExpandedMenuPresenter, this.mPopupContext);
        }
    }

    private void ensureMenuView() {
        if (this.mMenuView == null) {
            this.mMenuView = new ActionMenuView(getContext());
            this.mMenuView.setToolbar(this);
            this.mMenuView.setPopupTheme(this.mPopupTheme);
            this.mMenuView.setOnMenuItemClickListener(this.mMenuViewItemClickListener);
            this.mMenuView.setMenuCallbacks(this.mActionMenuPresenterCallback, this.mMenuBuilderCallback);
            LayoutParams lp = generateDefaultLayoutParams();
            lp.gravity = GravityCompat.END | (this.mButtonGravity & 16);
            this.mMenuView.setLayoutParams(lp);
            addSystemView(this.mMenuView, false);
        }
    }

    private MenuInflater getMenuInflater() {
        return new MenuInflater(getContext());
    }

    public void inflateMenu(@MenuRes int resId) {
        getMenuInflater().inflate(resId, getMenu());
    }

    public void setOnMenuItemClickListener(android.widget.Toolbar.OnMenuItemClickListener listener) {
        super.setOnMenuItemClickListener(listener);
        this.mOnMenuItemClickListener = listener;
    }

    public void setContentInsetsRelative(int contentInsetStart, int contentInsetEnd) {
        ensureContentInsets();
        this.mContentInsets.setRelative(contentInsetStart, contentInsetEnd);
    }

    public int getContentInsetStart() {
        return this.mContentInsets != null ? this.mContentInsets.getStart() : 0;
    }

    public int getContentInsetEnd() {
        return this.mContentInsets != null ? this.mContentInsets.getEnd() : 0;
    }

    public void setContentInsetsAbsolute(int contentInsetLeft, int contentInsetRight) {
        ensureContentInsets();
        this.mContentInsets.setAbsolute(contentInsetLeft, contentInsetRight);
    }

    public int getContentInsetLeft() {
        return this.mContentInsets != null ? this.mContentInsets.getLeft() : 0;
    }

    public int getContentInsetRight() {
        return this.mContentInsets != null ? this.mContentInsets.getRight() : 0;
    }

    public int getContentInsetStartWithNavigation() {
        if (this.mContentInsetStartWithNavigation != Integer.MIN_VALUE) {
            return this.mContentInsetStartWithNavigation;
        }
        return getContentInsetStart();
    }

    public void setContentInsetStartWithNavigation(int insetStartWithNavigation) {
        if (insetStartWithNavigation < 0) {
            insetStartWithNavigation = Integer.MIN_VALUE;
        }
        if (insetStartWithNavigation != this.mContentInsetStartWithNavigation) {
            this.mContentInsetStartWithNavigation = insetStartWithNavigation;
            if (getNavigationIcon() != null) {
                requestLayout();
            }
        }
    }

    public int getContentInsetEndWithActions() {
        if (this.mContentInsetEndWithActions != Integer.MIN_VALUE) {
            return this.mContentInsetEndWithActions;
        }
        return getContentInsetEnd();
    }

    public void setContentInsetEndWithActions(int insetEndWithActions) {
        if (insetEndWithActions < 0) {
            insetEndWithActions = Integer.MIN_VALUE;
        }
        if (insetEndWithActions != this.mContentInsetEndWithActions) {
            this.mContentInsetEndWithActions = insetEndWithActions;
            if (getNavigationIcon() != null) {
                requestLayout();
            }
        }
    }

    public int getCurrentContentInsetStart() {
        if (getNavigationIcon() != null) {
            return Math.max(getContentInsetStart(), Math.max(this.mContentInsetStartWithNavigation, 0));
        }
        return getContentInsetStart();
    }

    public int getCurrentContentInsetEnd() {
        boolean hasActions = false;
        if (this.mMenuView != null) {
            MenuBuilder mb = this.mMenuView.peekMenu();
            boolean z = mb != null && mb.hasVisibleItems();
            hasActions = z;
        }
        if (hasActions) {
            return Math.max(getContentInsetEnd(), Math.max(this.mContentInsetEndWithActions, 0));
        }
        return getContentInsetEnd();
    }

    public int getCurrentContentInsetLeft() {
        if (ViewUtils.isLayoutRtl(this)) {
            return getCurrentContentInsetEnd();
        }
        return getCurrentContentInsetStart();
    }

    public int getCurrentContentInsetRight() {
        if (ViewUtils.isLayoutRtl(this)) {
            return getCurrentContentInsetStart();
        }
        return getCurrentContentInsetEnd();
    }

    private void ensureNavButtonView() {
        if (this.mMyNavButtonView == null) {
            this.mMyNavButtonView = new ImageButton(getContext(), null, 0, this.mNavButtonStyle);
            LayoutParams lp = generateDefaultLayoutParams();
            lp.gravity = GravityCompat.START | (this.mButtonGravity & 16);
            this.mMyNavButtonView.setLayoutParams(lp);
        }
    }

    private void ensureCollapseButtonView() {
        if (this.mCollapseButtonView == null) {
            this.mCollapseButtonView = new ImageButton(getContext(), null, 0, this.mNavButtonStyle);
            this.mCollapseButtonView.setImageDrawable(this.mCollapseIcon);
            this.mCollapseButtonView.setContentDescription(this.mCollapseDescription);
            LayoutParams lp = generateDefaultLayoutParams();
            lp.gravity = GravityCompat.START | (this.mButtonGravity & 16);
            lp.mViewType = 2;
            this.mCollapseButtonView.setLayoutParams(lp);
            this.mCollapseButtonView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Toolbar.this.collapseActionView();
                }
            });
        }
    }

    private void addSystemView(View v, boolean allowHide) {
        LayoutParams lp;
        android.view.ViewGroup.LayoutParams vlp = v.getLayoutParams();
        if (vlp == null) {
            lp = generateDefaultLayoutParams();
        } else if (checkLayoutParams(vlp)) {
            lp = (LayoutParams) vlp;
        } else {
            lp = generateLayoutParams(vlp);
        }
        lp.mViewType = 1;
        if (!allowHide || this.mExpandedActionView == null) {
            addView(v, lp);
            return;
        }
        v.setLayoutParams(lp);
        this.mHiddenViews.add(v);
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        if (!(this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null)) {
            state.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        state.isOverflowOpen = isOverflowMenuShowing();
        return state;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        Menu menu = this.mMenuView != null ? this.mMenuView.peekMenu() : null;
        if (!(ss.expandedMenuItemId == 0 || this.mExpandedMenuPresenter == null || menu == null)) {
            MenuItem item = menu.findItem(ss.expandedMenuItemId);
            if (item != null) {
                item.expandActionView();
            }
        }
        if (ss.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }

    private void postShowOverflowMenu() {
        removeCallbacks(this.mShowOverflowMenuRunnable);
        post(this.mShowOverflowMenuRunnable);
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mShowOverflowMenuRunnable);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == 0) {
            this.mEatingTouch = false;
        }
        if (!this.mEatingTouch) {
            boolean handled = super.onTouchEvent(ev);
            if (action == 0 && !handled) {
                this.mEatingTouch = true;
            }
        }
        if (action == 1 || action == 3) {
            this.mEatingTouch = false;
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void onSetLayoutParams(View child, android.view.ViewGroup.LayoutParams lp) {
        if (!checkLayoutParams(lp)) {
            child.setLayoutParams(generateLayoutParams(lp));
        }
    }

    private void measureChildConstrained(View child, int parentWidthSpec, int widthUsed, int parentHeightSpec, int heightUsed, int heightConstraint) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int childWidthSpec = getChildMeasureSpec(parentWidthSpec, (((getPaddingLeft() + getPaddingRight()) + lp.leftMargin) + lp.rightMargin) + widthUsed, lp.width);
        int childHeightSpec = getChildMeasureSpec(parentHeightSpec, ((((getPaddingTop() + this.mPaddingTopOffset) + getPaddingBottom()) + lp.topMargin) + lp.bottomMargin) + heightUsed, lp.height);
        int childHeightMode = MeasureSpec.getMode(childHeightSpec);
        if (childHeightMode != Ints.MAX_POWER_OF_TWO && heightConstraint >= 0) {
            int size;
            if (childHeightMode != 0) {
                size = Math.min(MeasureSpec.getSize(childHeightSpec), heightConstraint);
            } else {
                size = heightConstraint;
            }
            childHeightSpec = MeasureSpec.makeMeasureSpec(size, Ints.MAX_POWER_OF_TWO);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private int measureSearchChildCollapseMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed, int[] collapsingMargins) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int leftDiff = lp.leftMargin - collapsingMargins[0];
        int rightDiff = lp.rightMargin - collapsingMargins[1];
        int hMargins = Math.max(0, leftDiff) + Math.max(0, rightDiff);
        collapsingMargins[0] = Math.max(0, -leftDiff);
        collapsingMargins[1] = Math.max(0, -rightDiff);
        int totalUsedWidth = ((getPaddingLeft() + getPaddingRight()) + hMargins) + widthUsed;
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, totalUsedWidth, (getMeasuredWidth() - totalUsedWidth) + Math.abs(rightDiff)), getChildMeasureSpec(parentHeightMeasureSpec, ((((getPaddingTop() + this.mPaddingTopOffset) + getPaddingBottom()) + lp.topMargin) + lp.bottomMargin) + heightUsed, lp.height));
        return child.getMeasuredWidth() + hMargins;
    }

    private int measureChildCollapseMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed, int[] collapsingMargins) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int leftDiff = lp.leftMargin - collapsingMargins[0];
        int rightDiff = lp.rightMargin - collapsingMargins[1];
        int hMargins = Math.max(0, leftDiff) + Math.max(0, rightDiff);
        collapsingMargins[0] = Math.max(0, -leftDiff);
        collapsingMargins[1] = Math.max(0, -rightDiff);
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, ((getPaddingLeft() + getPaddingRight()) + hMargins) + widthUsed, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, ((((getPaddingTop() + this.mPaddingTopOffset) + getPaddingBottom()) + lp.topMargin) + lp.bottomMargin) + heightUsed, lp.height));
        return child.getMeasuredWidth() + hMargins;
    }

    private boolean shouldCollapse() {
        if (!this.mCollapsible) {
            return false;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (shouldLayout(child) && child.getMeasuredWidth() > 0 && child.getMeasuredHeight() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean resetedPadding() {
        return this.mNeedResetPadding;
    }

    private int getPaddingTopOffset(int spaceBelow) {
        int finalPaddingTop = getPaddingTop();
        int desginPaddingBottom = this.mRealPaddingBottom;
        if (spaceBelow < desginPaddingBottom) {
            return finalPaddingTop - (desginPaddingBottom - spaceBelow);
        }
        return finalPaddingTop + (spaceBelow - desginPaddingBottom);
    }

    /* Access modifiers changed, original: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int marginEndIndex;
        int marginStartIndex;
        int i;
        int i2;
        int i3;
        int menuWidth;
        int navWidth = 0;
        int height = 0;
        boolean shouldLayoutNavButtonView = shouldLayout(this.mMyNavButtonView);
        int[] collapsingMargins = this.mTempMargins;
        if (ViewUtils.isLayoutRtl(this)) {
            marginEndIndex = 0;
            marginStartIndex = 1;
        } else {
            marginStartIndex = 0;
            marginEndIndex = 1;
        }
        int marginEndIndex2 = marginEndIndex;
        int navWidth2 = 0;
        if (shouldLayoutNavButtonView) {
            measureChildConstrained(this.mMyNavButtonView, widthMeasureSpec, 0, heightMeasureSpec, 0, this.mMaxButtonHeight);
            navWidth2 = this.mMyNavButtonView.getMeasuredWidth() + getHorizontalMargins(this.mMyNavButtonView);
            navWidth = Math.max(0, this.mMyNavButtonView.getMeasuredHeight() + getVerticalMargins(this.mMyNavButtonView));
            height = combineMeasuredStates(0, this.mMyNavButtonView.getMeasuredState());
        }
        if (shouldLayout(this.mCollapseButtonView)) {
            measureChildConstrained(this.mCollapseButtonView, widthMeasureSpec, 0, heightMeasureSpec, 0, this.mMaxButtonHeight);
            navWidth2 = this.mCollapseButtonView.getMeasuredWidth() + getHorizontalMargins(this.mCollapseButtonView);
            navWidth = Math.max(navWidth, this.mCollapseButtonView.getMeasuredHeight() + getVerticalMargins(this.mCollapseButtonView));
            height = combineMeasuredStates(height, this.mCollapseButtonView.getMeasuredState());
        }
        int childState = height;
        height = navWidth;
        navWidth = navWidth2;
        int contentInsetStart = getCurrentContentInsetStart();
        int width = 0 + Math.max(contentInsetStart, navWidth);
        collapsingMargins[marginStartIndex] = Math.max(0, contentInsetStart - navWidth);
        if (shouldLayout(this.mMenuView)) {
            i = 0;
            i = childState;
            measureChildConstrained(this.mMenuView, widthMeasureSpec, width, heightMeasureSpec, 0, this.mMaxButtonHeight);
            navWidth2 = this.mMenuView.getMeasuredWidth() + getHorizontalMargins(this.mMenuView);
            height = Math.max(height, this.mMenuView.getMeasuredHeight() + getVerticalMargins(this.mMenuView));
            childState = combineMeasuredStates(i, this.mMenuView.getMeasuredState());
            i = height;
            height = navWidth2;
        } else {
            i = childState;
            i = height;
            height = 0;
        }
        contentInsetStart = getCurrentContentInsetEnd();
        width += Math.max(contentInsetStart, height);
        collapsingMargins[marginEndIndex2] = Math.max(0, contentInsetStart - height);
        int i4;
        int i5;
        if (shouldLayout(this.mExpandedActionView)) {
            if ((this.mExpandedActionView instanceof CollapsibleActionView) && this.mMenuView.getChildCount() == 1) {
                i2 = 0;
                navWidth = childState;
                width += measureSearchChildCollapseMargins(this.mExpandedActionView, widthMeasureSpec, width, heightMeasureSpec, 0, collapsingMargins);
            } else {
                i2 = 0;
                i4 = contentInsetStart;
                i5 = navWidth;
                navWidth = childState;
                width += measureChildCollapseMargins(this.mExpandedActionView, widthMeasureSpec, width, heightMeasureSpec, 0, collapsingMargins);
            }
            i = Math.max(i, this.mExpandedActionView.getMeasuredHeight() + getVerticalMargins(this.mExpandedActionView));
            navWidth = combineMeasuredStates(navWidth, this.mExpandedActionView.getMeasuredState());
        } else {
            i2 = 0;
            i4 = contentInsetStart;
            i5 = navWidth;
            navWidth = childState;
        }
        if (shouldLayout(this.mLogoView)) {
            width += measureChildCollapseMargins(this.mLogoView, widthMeasureSpec, width, heightMeasureSpec, 0, collapsingMargins);
            i = Math.max(i, this.mLogoView.getMeasuredHeight() + getVerticalMargins(this.mLogoView));
            navWidth = combineMeasuredStates(navWidth, this.mLogoView.getMeasuredState());
        }
        childState = getChildCount();
        int i6 = i2;
        while (true) {
            contentInsetStart = i6;
            if (contentInsetStart >= childState) {
                break;
            }
            View child = getChildAt(contentInsetStart);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.mViewType != 0) {
                i3 = contentInsetStart;
                navWidth2 = childState;
                menuWidth = height;
            } else if (shouldLayout(child)) {
                menuWidth = height;
                height = child;
                i3 = contentInsetStart;
                navWidth2 = childState;
                width += measureChildCollapseMargins(child, widthMeasureSpec, width, heightMeasureSpec, 0, collapsingMargins);
                i = Math.max(i, height.getMeasuredHeight() + getVerticalMargins(height));
                navWidth = combineMeasuredStates(navWidth, height.getMeasuredState());
            } else {
                i3 = contentInsetStart;
                navWidth2 = childState;
                menuWidth = height;
            }
            i6 = i3 + 1;
            childState = navWidth2;
            height = menuWidth;
        }
        menuWidth = height;
        height = 0;
        int titleHeight = 0;
        i3 = this.mTitleMarginTop + this.mTitleMarginBottom;
        int titleHorizMargins = this.mTitleMarginStart + this.mTitleMarginEnd;
        if (shouldLayout(this.mTitleTextView)) {
            i6 = measureChildCollapseMargins(this.mTitleTextView, widthMeasureSpec, width + titleHorizMargins, heightMeasureSpec, i3, collapsingMargins);
            height = this.mTitleTextView.getMeasuredWidth() + getHorizontalMargins(this.mTitleTextView);
            titleHeight = this.mTitleTextView.getMeasuredHeight() + getVerticalMargins(this.mTitleTextView);
            navWidth = combineMeasuredStates(navWidth, this.mTitleTextView.getMeasuredState());
        }
        if (shouldLayout(this.mSubtitleTextView)) {
            height = Math.max(height, measureChildCollapseMargins(this.mSubtitleTextView, widthMeasureSpec, width + titleHorizMargins, heightMeasureSpec, titleHeight + i3, collapsingMargins));
            titleHeight += this.mSubtitleTextView.getMeasuredHeight() + getVerticalMargins(this.mSubtitleTextView);
            navWidth = combineMeasuredStates(navWidth, this.mSubtitleTextView.getMeasuredState());
        }
        i6 = titleHeight;
        marginEndIndex = Math.max(i, i6) + (getPaddingTop() + getPaddingBottom());
        int measuredWidth = resolveSizeAndState(Math.max((width + height) + (getPaddingLeft() + getPaddingRight()), getSuggestedMinimumWidth()), widthMeasureSpec, ViewCompat.MEASURED_STATE_MASK & navWidth);
        int measuredHeight = resolveSizeAndState(Math.max(marginEndIndex, getSuggestedMinimumHeight()), heightMeasureSpec, navWidth << 16);
        if (this.mNeedResetPadding) {
            this.mPaddingTopOffset = (measuredHeight - this.mMaxButtonHeight) / 2;
            if (this.mPaddingTopOffset < this.mRealPaddingBottom) {
                this.mPaddingTopOffset += this.mRealPaddingBottom - this.mPaddingTopOffset;
            } else {
                this.mPaddingTopOffset -= this.mPaddingTopOffset - this.mRealPaddingBottom;
            }
        }
        i6 = shouldCollapse() ? i2 : getFitsSystemWindows() ? this.mPaddingTopOffset + measuredHeight : measuredHeight;
        setMeasuredDimension(measuredWidth, i6);
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x01d1  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0184  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x026d  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01e6  */
    /* JADX WARNING: Missing block: B:49:0x0166, code skipped:
            if (r0.mTitleTextView.getMeasuredWidth() <= 0) goto L_0x016b;
     */
    public void onLayout(boolean r46, int r47, int r48, int r49, int r50) {
        /*
        r45 = this;
        r0 = r45;
        r1 = r45.getLayoutDirection();
        r2 = 1;
        r3 = 0;
        if (r1 != r2) goto L_0x000c;
    L_0x000a:
        r1 = r2;
        goto L_0x000d;
    L_0x000c:
        r1 = r3;
    L_0x000d:
        r4 = r0.mMyNavButtonView;
        r4 = r0.shouldLayout(r4);
        r5 = r0.mCollapseButtonView;
        r5 = r0.shouldLayout(r5);
        r6 = r45.getPaddingLeft();
        r7 = r45.getPaddingRight();
        r8 = r45.getPaddingBottom();
        r9 = r45.getWidth();
        r10 = r45.getHeight();
        r11 = r45.getPaddingTop();
        r12 = r0.mPaddingTopOffset;
        r11 = r11 + r12;
        r12 = r45.getPaddingLeft();
        r13 = r9 - r7;
        r14 = r0.mTempMargins;
        r14[r2] = r3;
        r14[r3] = r3;
        r15 = r45.getMinimumHeight();
        if (r15 < 0) goto L_0x004d;
    L_0x0046:
        r2 = r50 - r48;
        r2 = java.lang.Math.min(r15, r2);
        goto L_0x004e;
    L_0x004d:
        r2 = r3;
    L_0x004e:
        if (r4 == 0) goto L_0x005f;
    L_0x0050:
        if (r1 == 0) goto L_0x0059;
    L_0x0052:
        r3 = r0.mMyNavButtonView;
        r13 = r0.layoutChildRight(r3, r13, r14, r2);
        goto L_0x005f;
    L_0x0059:
        r3 = r0.mMyNavButtonView;
        r12 = r0.layoutChildLeft(r3, r12, r14, r2);
    L_0x005f:
        if (r5 == 0) goto L_0x0070;
    L_0x0061:
        if (r1 == 0) goto L_0x006a;
    L_0x0063:
        r3 = r0.mCollapseButtonView;
        r13 = r0.layoutChildRight(r3, r13, r14, r2);
        goto L_0x0070;
    L_0x006a:
        r3 = r0.mCollapseButtonView;
        r12 = r0.layoutChildLeft(r3, r12, r14, r2);
    L_0x0070:
        r3 = r0.mMenuView;
        r3 = r0.shouldLayout(r3);
        if (r3 == 0) goto L_0x0087;
    L_0x0078:
        if (r1 == 0) goto L_0x0081;
    L_0x007a:
        r3 = r0.mMenuView;
        r12 = r0.layoutChildLeft(r3, r12, r14, r2);
        goto L_0x0087;
    L_0x0081:
        r3 = r0.mMenuView;
        r13 = r0.layoutChildRight(r3, r13, r14, r2);
    L_0x0087:
        r3 = r45.getCurrentContentInsetLeft();
        r17 = r45.getCurrentContentInsetRight();
        r18 = r4;
        r4 = r3 - r12;
        r19 = r5;
        r5 = 0;
        r4 = java.lang.Math.max(r5, r4);
        r14[r5] = r4;
        r4 = r9 - r7;
        r4 = r4 - r13;
        r4 = r17 - r4;
        r4 = java.lang.Math.max(r5, r4);
        r5 = 1;
        r14[r5] = r4;
        r4 = java.lang.Math.max(r12, r3);
        r5 = r9 - r7;
        r5 = r5 - r17;
        r5 = java.lang.Math.min(r13, r5);
        r12 = r0.mExpandedActionView;
        r12 = r0.shouldLayout(r12);
        if (r12 == 0) goto L_0x00cb;
    L_0x00bc:
        if (r1 == 0) goto L_0x00c5;
    L_0x00be:
        r12 = r0.mExpandedActionView;
        r5 = r0.layoutChildRight(r12, r5, r14, r2);
        goto L_0x00cb;
    L_0x00c5:
        r12 = r0.mExpandedActionView;
        r4 = r0.layoutChildLeft(r12, r4, r14, r2);
    L_0x00cb:
        r12 = r0.mLogoView;
        r12 = r0.shouldLayout(r12);
        if (r12 == 0) goto L_0x00e2;
    L_0x00d3:
        if (r1 == 0) goto L_0x00dc;
    L_0x00d5:
        r12 = r0.mLogoView;
        r5 = r0.layoutChildRight(r12, r5, r14, r2);
        goto L_0x00e2;
    L_0x00dc:
        r12 = r0.mLogoView;
        r4 = r0.layoutChildLeft(r12, r4, r14, r2);
    L_0x00e2:
        r12 = r0.mTitleTextView;
        r12 = r0.shouldLayout(r12);
        r13 = r0.mSubtitleTextView;
        r13 = r0.shouldLayout(r13);
        r20 = 0;
        if (r12 == 0) goto L_0x010f;
    L_0x00f2:
        r21 = r3;
        r3 = r0.mTitleTextView;
        r3 = r3.getLayoutParams();
        r3 = (com.oneplus.lib.widget.actionbar.Toolbar.LayoutParams) r3;
        r22 = r15;
        r15 = r3.topMargin;
        r23 = r7;
        r7 = r0.mTitleTextView;
        r7 = r7.getMeasuredHeight();
        r15 = r15 + r7;
        r7 = r3.bottomMargin;
        r15 = r15 + r7;
        r20 = r20 + r15;
        goto L_0x0115;
    L_0x010f:
        r21 = r3;
        r23 = r7;
        r22 = r15;
    L_0x0115:
        if (r13 == 0) goto L_0x012d;
    L_0x0117:
        r3 = r0.mSubtitleTextView;
        r3 = r3.getLayoutParams();
        r3 = (com.oneplus.lib.widget.actionbar.Toolbar.LayoutParams) r3;
        r7 = r3.topMargin;
        r15 = r0.mSubtitleTextView;
        r15 = r15.getMeasuredHeight();
        r7 = r7 + r15;
        r15 = r3.bottomMargin;
        r7 = r7 + r15;
        r20 = r20 + r7;
    L_0x012d:
        if (r12 != 0) goto L_0x013e;
    L_0x012f:
        if (r13 == 0) goto L_0x0132;
    L_0x0131:
        goto L_0x013e;
    L_0x0132:
        r32 = r1;
        r30 = r2;
        r39 = r5;
        r27 = r6;
        r28 = r9;
        goto L_0x02ed;
    L_0x013e:
        if (r12 == 0) goto L_0x0143;
    L_0x0140:
        r3 = r0.mTitleTextView;
        goto L_0x0145;
    L_0x0143:
        r3 = r0.mSubtitleTextView;
    L_0x0145:
        if (r13 == 0) goto L_0x014a;
    L_0x0147:
        r7 = r0.mSubtitleTextView;
        goto L_0x014c;
    L_0x014a:
        r7 = r0.mTitleTextView;
    L_0x014c:
        r15 = r3.getLayoutParams();
        r15 = (com.oneplus.lib.widget.actionbar.Toolbar.LayoutParams) r15;
        r24 = r7.getLayoutParams();
        r25 = r3;
        r3 = r24;
        r3 = (com.oneplus.lib.widget.actionbar.Toolbar.LayoutParams) r3;
        if (r12 == 0) goto L_0x0169;
    L_0x015e:
        r26 = r7;
        r7 = r0.mTitleTextView;
        r7 = r7.getMeasuredWidth();
        if (r7 > 0) goto L_0x0175;
    L_0x0168:
        goto L_0x016b;
    L_0x0169:
        r26 = r7;
    L_0x016b:
        if (r13 == 0) goto L_0x0177;
    L_0x016d:
        r7 = r0.mSubtitleTextView;
        r7 = r7.getMeasuredWidth();
        if (r7 <= 0) goto L_0x0177;
    L_0x0175:
        r7 = 1;
        goto L_0x0178;
    L_0x0177:
        r7 = 0;
    L_0x0178:
        r27 = r6;
        r6 = r0.mGravity;
        r6 = r6 & 16;
        r28 = r9;
        r9 = 48;
        if (r6 == r9) goto L_0x01d1;
    L_0x0184:
        r9 = 80;
        if (r6 == r9) goto L_0x01c2;
    L_0x0188:
        r6 = r10 - r11;
        r6 = r6 - r8;
        r9 = r6 - r20;
        r9 = r9 / 2;
        r29 = r6;
        r6 = r15.topMargin;
        r30 = r2;
        r2 = r0.mTitleMarginTop;
        r6 = r6 + r2;
        if (r9 >= r6) goto L_0x01a3;
    L_0x019a:
        r2 = r15.topMargin;
        r6 = r0.mTitleMarginTop;
        r9 = r2 + r6;
        r31 = r4;
        goto L_0x01bf;
    L_0x01a3:
        r2 = r10 - r8;
        r2 = r2 - r20;
        r2 = r2 - r9;
        r2 = r2 - r11;
        r6 = r15.bottomMargin;
        r31 = r4;
        r4 = r0.mTitleMarginBottom;
        r6 = r6 + r4;
        if (r2 >= r6) goto L_0x01bf;
    L_0x01b2:
        r4 = r3.bottomMargin;
        r6 = r0.mTitleMarginBottom;
        r4 = r4 + r6;
        r4 = r4 - r2;
        r4 = r9 - r4;
        r6 = 0;
        r9 = java.lang.Math.max(r6, r4);
    L_0x01bf:
        r2 = r11 + r9;
        goto L_0x01e3;
    L_0x01c2:
        r30 = r2;
        r31 = r4;
        r2 = r10 - r8;
        r4 = r3.bottomMargin;
        r2 = r2 - r4;
        r4 = r0.mTitleMarginBottom;
        r2 = r2 - r4;
        r2 = r2 - r20;
        goto L_0x01e3;
    L_0x01d1:
        r30 = r2;
        r31 = r4;
        r2 = r45.getPaddingTop();
        r4 = r0.mPaddingTopOffset;
        r2 = r2 + r4;
        r4 = r15.topMargin;
        r2 = r2 + r4;
        r4 = r0.mTitleMarginTop;
        r2 = r2 + r4;
        if (r1 == 0) goto L_0x026d;
    L_0x01e6:
        if (r7 == 0) goto L_0x01eb;
    L_0x01e8:
        r4 = r0.mTitleMarginStart;
        goto L_0x01ec;
    L_0x01eb:
        r4 = 0;
    L_0x01ec:
        r6 = 1;
        r9 = r14[r6];
        r4 = r4 - r9;
        r9 = 0;
        r16 = java.lang.Math.max(r9, r4);
        r5 = r5 - r16;
        r32 = r1;
        r1 = -r4;
        r1 = java.lang.Math.max(r9, r1);
        r14[r6] = r1;
        r1 = r5;
        r6 = r5;
        if (r12 == 0) goto L_0x022f;
    L_0x0204:
        r9 = r0.mTitleTextView;
        r9 = r9.getLayoutParams();
        r9 = (com.oneplus.lib.widget.actionbar.Toolbar.LayoutParams) r9;
        r33 = r3;
        r3 = r0.mTitleTextView;
        r3 = r3.getMeasuredWidth();
        r3 = r1 - r3;
        r34 = r4;
        r4 = r0.mTitleTextView;
        r4 = r4.getMeasuredHeight();
        r4 = r4 + r2;
        r35 = r5;
        r5 = r0.mTitleTextView;
        r5.layout(r3, r2, r1, r4);
        r5 = r0.mTitleMarginEnd;
        r1 = r3 - r5;
        r5 = r9.bottomMargin;
        r2 = r4 + r5;
        goto L_0x0235;
    L_0x022f:
        r33 = r3;
        r34 = r4;
        r35 = r5;
    L_0x0235:
        if (r13 == 0) goto L_0x025d;
    L_0x0237:
        r3 = r0.mSubtitleTextView;
        r3 = r3.getLayoutParams();
        r3 = (com.oneplus.lib.widget.actionbar.Toolbar.LayoutParams) r3;
        r4 = r3.topMargin;
        r2 = r2 + r4;
        r4 = r0.mSubtitleTextView;
        r4 = r4.getMeasuredWidth();
        r4 = r6 - r4;
        r5 = r0.mSubtitleTextView;
        r5 = r5.getMeasuredHeight();
        r5 = r5 + r2;
        r9 = r0.mSubtitleTextView;
        r9.layout(r4, r2, r6, r5);
        r9 = r0.mTitleMarginEnd;
        r6 = r6 - r9;
        r9 = r3.bottomMargin;
        r2 = r5 + r9;
    L_0x025d:
        if (r7 == 0) goto L_0x0265;
    L_0x025f:
        r1 = java.lang.Math.min(r1, r6);
        r5 = r1;
        goto L_0x0267;
    L_0x0265:
        r5 = r35;
    L_0x0267:
        r39 = r5;
        r4 = r31;
        goto L_0x02ed;
    L_0x026d:
        r32 = r1;
        r33 = r3;
        if (r7 == 0) goto L_0x0276;
    L_0x0273:
        r3 = r0.mTitleMarginStart;
        goto L_0x0277;
    L_0x0276:
        r3 = 0;
    L_0x0277:
        r1 = 0;
        r4 = r14[r1];
        r3 = r3 - r4;
        r4 = java.lang.Math.max(r1, r3);
        r4 = r31 + r4;
        r6 = -r3;
        r6 = java.lang.Math.max(r1, r6);
        r14[r1] = r6;
        r6 = r4;
        r9 = r4;
        if (r12 == 0) goto L_0x02b6;
    L_0x028c:
        r1 = r0.mTitleTextView;
        r1 = r1.getLayoutParams();
        r1 = (com.oneplus.lib.widget.actionbar.Toolbar.LayoutParams) r1;
        r37 = r3;
        r3 = r0.mTitleTextView;
        r3 = r3.getMeasuredWidth();
        r3 = r3 + r6;
        r38 = r4;
        r4 = r0.mTitleTextView;
        r4 = r4.getMeasuredHeight();
        r4 = r4 + r2;
        r39 = r5;
        r5 = r0.mTitleTextView;
        r5.layout(r6, r2, r3, r4);
        r5 = r0.mTitleMarginEnd;
        r6 = r3 + r5;
        r5 = r1.bottomMargin;
        r2 = r4 + r5;
        goto L_0x02bc;
    L_0x02b6:
        r37 = r3;
        r38 = r4;
        r39 = r5;
    L_0x02bc:
        if (r13 == 0) goto L_0x02e4;
    L_0x02be:
        r1 = r0.mSubtitleTextView;
        r1 = r1.getLayoutParams();
        r1 = (com.oneplus.lib.widget.actionbar.Toolbar.LayoutParams) r1;
        r3 = r1.topMargin;
        r2 = r2 + r3;
        r3 = r0.mSubtitleTextView;
        r3 = r3.getMeasuredWidth();
        r3 = r3 + r9;
        r4 = r0.mSubtitleTextView;
        r4 = r4.getMeasuredHeight();
        r4 = r4 + r2;
        r5 = r0.mSubtitleTextView;
        r5.layout(r9, r2, r3, r4);
        r5 = r0.mTitleMarginEnd;
        r9 = r3 + r5;
        r5 = r1.bottomMargin;
        r2 = r4 + r5;
    L_0x02e4:
        if (r7 == 0) goto L_0x02eb;
    L_0x02e6:
        r4 = java.lang.Math.max(r6, r9);
        goto L_0x02ed;
    L_0x02eb:
        r4 = r38;
    L_0x02ed:
        r1 = r0.mTempViews;
        r2 = 3;
        r0.addCustomViewsWithGravity(r1, r2);
        r1 = r0.mTempViews;
        r1 = r1.size();
        r2 = 0;
    L_0x02fa:
        if (r2 >= r1) goto L_0x030d;
    L_0x02fc:
        r3 = r0.mTempViews;
        r3 = r3.get(r2);
        r3 = (android.view.View) r3;
        r5 = r30;
        r4 = r0.layoutChildLeft(r3, r4, r14, r5);
        r2 = r2 + 1;
        goto L_0x02fa;
    L_0x030d:
        r5 = r30;
        r2 = r0.mTempViews;
        r3 = 5;
        r0.addCustomViewsWithGravity(r2, r3);
        r2 = r0.mTempViews;
        r2 = r2.size();
        r6 = r39;
        r3 = 0;
    L_0x031e:
        if (r3 >= r2) goto L_0x032f;
    L_0x0320:
        r7 = r0.mTempViews;
        r7 = r7.get(r3);
        r7 = (android.view.View) r7;
        r6 = r0.layoutChildRight(r7, r6, r14, r5);
        r3 = r3 + 1;
        goto L_0x031e;
    L_0x032f:
        r3 = r0.mTempViews;
        r7 = 1;
        r0.addCustomViewsWithGravity(r3, r7);
        r3 = r0.mTempViews;
        r3 = r0.getViewListMeasuredWidth(r3, r14);
        r9 = r28 - r27;
        r9 = r9 - r23;
        r9 = r9 / 2;
        r7 = r27 + r9;
        r9 = r3 / 2;
        r15 = r7 - r9;
        r40 = r1;
        r1 = r15 + r3;
        if (r15 >= r4) goto L_0x034f;
    L_0x034d:
        r15 = r4;
        goto L_0x0355;
    L_0x034f:
        if (r1 <= r6) goto L_0x0355;
    L_0x0351:
        r16 = r1 - r6;
        r15 = r15 - r16;
    L_0x0355:
        r41 = r1;
        r1 = r0.mTempViews;
        r1 = r1.size();
        r36 = 0;
    L_0x035f:
        r42 = r36;
        r43 = r2;
        r2 = r42;
        if (r2 >= r1) goto L_0x037c;
    L_0x0367:
        r44 = r1;
        r1 = r0.mTempViews;
        r1 = r1.get(r2);
        r1 = (android.view.View) r1;
        r15 = r0.layoutChildLeft(r1, r15, r14, r5);
        r36 = r2 + 1;
        r2 = r43;
        r1 = r44;
        goto L_0x035f;
    L_0x037c:
        r44 = r1;
        r1 = r0.mTempViews;
        r1.clear();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.widget.actionbar.Toolbar.onLayout(boolean, int, int, int, int):void");
    }

    private int getViewListMeasuredWidth(List<View> views, int[] collapsingMargins) {
        int collapseLeft = collapsingMargins[0];
        int collapseRight = collapsingMargins[1];
        int count = views.size();
        int width = 0;
        int collapseRight2 = collapseRight;
        collapseRight = collapseLeft;
        for (collapseLeft = 0; collapseLeft < count; collapseLeft++) {
            View v = (View) views.get(collapseLeft);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            int l = lp.leftMargin - collapseRight;
            int r = lp.rightMargin - collapseRight2;
            int leftMargin = Math.max(0, l);
            int rightMargin = Math.max(0, r);
            collapseRight = Math.max(0, -l);
            collapseRight2 = Math.max(0, -r);
            width += (v.getMeasuredWidth() + leftMargin) + rightMargin;
        }
        return width;
    }

    private int layoutChildLeft(View child, int left, int[] collapsingMargins, int alignmentHeight) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int l = lp.leftMargin - collapsingMargins[0];
        left += Math.max(0, l);
        collapsingMargins[0] = Math.max(0, -l);
        int top = getChildTop(child, alignmentHeight);
        int childWidth = child.getMeasuredWidth();
        child.layout(left, top, left + childWidth, child.getMeasuredHeight() + top);
        return left + (lp.rightMargin + childWidth);
    }

    private int layoutChildRight(View child, int right, int[] collapsingMargins, int alignmentHeight) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int r = lp.rightMargin - collapsingMargins[1];
        right -= Math.max(0, r);
        collapsingMargins[1] = Math.max(0, -r);
        int top = getChildTop(child, alignmentHeight);
        int childWidth = child.getMeasuredWidth();
        child.layout(right - childWidth, top, right, child.getMeasuredHeight() + top);
        return right - (lp.leftMargin + childWidth);
    }

    private int getChildTop(View child, int alignmentHeight) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int childHeight = child.getMeasuredHeight();
        int alignmentOffset = alignmentHeight > 0 ? (childHeight - alignmentHeight) / 2 : 0;
        int childVerticalGravity = getChildVerticalGravity(lp.gravity);
        if (childVerticalGravity == 48) {
            return (getPaddingTop() + this.mPaddingTopOffset) - alignmentOffset;
        }
        if (childVerticalGravity == 80) {
            return (((getHeight() - getPaddingBottom()) - childHeight) - lp.bottomMargin) - alignmentOffset;
        }
        childVerticalGravity = getPaddingTop() + this.mPaddingTopOffset;
        int paddingBottom = getPaddingBottom();
        int height = getHeight();
        int spaceAbove = (((height - childVerticalGravity) - paddingBottom) - childHeight) / 2;
        if (spaceAbove < lp.topMargin) {
            spaceAbove = lp.topMargin;
        } else {
            int spaceBelow = (((height - paddingBottom) - childHeight) - spaceAbove) - childVerticalGravity;
            if (spaceBelow < lp.bottomMargin) {
                spaceAbove = Math.max(0, spaceAbove - (lp.bottomMargin - spaceBelow));
            }
        }
        return childVerticalGravity + spaceAbove;
    }

    private int getChildVerticalGravity(int gravity) {
        int vgrav = gravity & 16;
        if (vgrav == 16 || vgrav == 48 || vgrav == 80) {
            return vgrav;
        }
        return 16 & this.mGravity;
    }

    private void addCustomViewsWithGravity(List<View> views, int gravity) {
        int i = 0;
        boolean z = true;
        if (getLayoutDirection() != 1) {
            z = false;
        }
        boolean isRtl = z;
        int childCount = getChildCount();
        int absGrav = Gravity.getAbsoluteGravity(gravity, getLayoutDirection());
        views.clear();
        View child;
        LayoutParams lp;
        if (isRtl) {
            for (i = childCount - 1; i >= 0; i--) {
                child = getChildAt(i);
                lp = (LayoutParams) child.getLayoutParams();
                if (lp.mViewType == 0 && shouldLayout(child) && getChildHorizontalGravity(lp.gravity) == absGrav) {
                    views.add(child);
                }
            }
            return;
        }
        while (i < childCount) {
            child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            if (lp.mViewType == 0 && shouldLayout(child) && getChildHorizontalGravity(lp.gravity) == absGrav) {
                views.add(child);
            }
            i++;
        }
    }

    private int getChildHorizontalGravity(int gravity) {
        int ld = getLayoutDirection();
        int hGrav = Gravity.getAbsoluteGravity(gravity, ld) & 7;
        if (hGrav != 1) {
            int i = 3;
            if (!(hGrav == 3 || hGrav == 5)) {
                if (ld == 1) {
                    i = 5;
                }
                return i;
            }
        }
        return hGrav;
    }

    private boolean shouldLayout(View view) {
        return (view == null || view.getParent() != this || view.getVisibility() == 8) ? false : true;
    }

    private int getHorizontalMargins(View v) {
        MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
        return mlp.getMarginStart() + mlp.getMarginEnd();
    }

    private int getVerticalMargins(View v) {
        MarginLayoutParams mlp = (MarginLayoutParams) v.getLayoutParams();
        return mlp.topMargin + mlp.bottomMargin;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        }
        if (p instanceof android.app.ActionBar.LayoutParams) {
            return new LayoutParams((android.app.ActionBar.LayoutParams) p);
        }
        if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    /* Access modifiers changed, original: protected */
    public boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p) && (p instanceof LayoutParams);
    }

    private static boolean isCustomView(View child) {
        return ((LayoutParams) child.getLayoutParams()).mViewType == 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void removeChildrenForExpandedActionView() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (!(((LayoutParams) child.getLayoutParams()).mViewType == 2 || child == this.mMenuView)) {
                removeViewAt(i);
                this.mHiddenViews.add(child);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void addChildrenForExpandedActionView() {
        for (int i = this.mHiddenViews.size() - 1; i >= 0; i--) {
            addView((View) this.mHiddenViews.get(i));
        }
        this.mHiddenViews.clear();
    }

    private boolean isChildOrHidden(View child) {
        boolean z = false;
        if (this.mHiddenViews == null) {
            return false;
        }
        if (child.getParent() == this || this.mHiddenViews.contains(child)) {
            z = true;
        }
        return z;
    }

    public void setCollapsible(boolean collapsible) {
        this.mCollapsible = collapsible;
        requestLayout();
    }

    public boolean setCollapsedState(boolean collapsed) {
        int i = 0;
        if (!this.mHasActionBarLineColor || this.mCollapsed == collapsed) {
            return false;
        }
        this.mCollapsed = collapsed;
        Drawable background = getBackground();
        if (background instanceof LayerDrawable) {
            LayerDrawable layerBackground = (LayerDrawable) background;
            Drawable dividerLayer = layerBackground.getDrawable(1);
            if (dividerLayer != null && layerBackground.getId(1) == R.id.actionbar_divider) {
                if (collapsed) {
                    i = this.mActionBarDividerColor;
                }
                dividerLayer.setColorFilter(i, Mode.SRC);
            }
        }
        refreshDrawableState();
        jumpDrawablesToCurrentState();
        return true;
    }

    public void setMenuCallbacks(Callback pcb, MenuBuilder.Callback mcb) {
        this.mActionMenuPresenterCallback = pcb;
        this.mMenuBuilderCallback = mcb;
        if (this.mMenuView != null) {
            this.mMenuView.setMenuCallbacks(pcb, mcb);
        }
    }

    private void ensureContentInsets() {
        if (this.mContentInsets == null) {
            this.mContentInsets = new RtlSpacingHelper();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public ActionMenuPresenter getOuterActionMenuPresenter() {
        return this.mOuterActionMenuPresenter;
    }

    /* Access modifiers changed, original: 0000 */
    public Context getPopupContext() {
        return this.mPopupContext;
    }
}
