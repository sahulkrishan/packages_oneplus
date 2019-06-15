package com.oneplus.lib.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.Interpolator;
import android.widget.EdgeEffect;
import android.widget.Scroller;
import com.google.common.primitives.Ints;
import com.oneplus.lib.util.MathUtils;
import com.oneplus.lib.widget.util.ViewUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewPager extends ViewGroup {
    private static final int CLOSE_ENOUGH = 2;
    private static final Comparator<ItemInfo> COMPARATOR = new Comparator<ItemInfo>() {
        public int compare(ItemInfo lhs, ItemInfo rhs) {
            return lhs.position - rhs.position;
        }
    };
    private static final boolean DEBUG = false;
    private static final int DEFAULT_GUTTER_SIZE = 16;
    private static final int DEFAULT_OFFSCREEN_PAGES = 1;
    private static final int DRAW_ORDER_DEFAULT = 0;
    private static final int DRAW_ORDER_FORWARD = 1;
    private static final int DRAW_ORDER_REVERSE = 2;
    private static final int INVALID_POINTER = -1;
    private static final int[] LAYOUT_ATTRS = new int[]{16842931};
    private static final int MAX_SCROLL_X = 16777216;
    private static final int MAX_SETTLE_DURATION = 600;
    private static final int MIN_DISTANCE_FOR_FLING = 25;
    private static final int MIN_FLING_VELOCITY = 400;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final String TAG = "ViewPager";
    private static final boolean USE_CACHE = false;
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return ((((t * t) * t) * t) * t) + 1.0f;
        }
    };
    private static final ViewPositionComparator sPositionComparator = new ViewPositionComparator();
    private int mActivePointerId;
    private PagerAdapter mAdapter;
    private OnAdapterChangeListener mAdapterChangeListener;
    private int mBottomPageBounds;
    private boolean mCalledSuper;
    private int mChildHeightMeasureSpec;
    private int mChildWidthMeasureSpec;
    private final int mCloseEnough;
    private int mCurItem;
    private int mDecorChildCount;
    private final int mDefaultGutterSize;
    private int mDrawingOrder;
    private ArrayList<View> mDrawingOrderedChildren;
    private final Runnable mEndScrollRunnable;
    private int mExpectedAdapterCount;
    private boolean mFirstLayout;
    private float mFirstOffset;
    private final int mFlingDistance;
    private int mGutterSize;
    private boolean mInLayout;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private OnPageChangeListener mInternalPageChangeListener;
    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private final ArrayList<ItemInfo> mItems;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mLastOffset;
    private final EdgeEffect mLeftEdge;
    private int mLeftIncr;
    private Drawable mMarginDrawable;
    private final int mMaximumVelocity;
    private final int mMinimumVelocity;
    private PagerObserver mObserver;
    private int mOffscreenPageLimit;
    private OnPageChangeListener mOnPageChangeListener;
    private int mPageMargin;
    private PageTransformer mPageTransformer;
    private boolean mPopulatePending;
    private Parcelable mRestoredAdapterState;
    private ClassLoader mRestoredClassLoader;
    private int mRestoredCurItem;
    private final EdgeEffect mRightEdge;
    private int mScrollState;
    private final Scroller mScroller;
    private boolean mScrollingCacheEnabled;
    private final ItemInfo mTempItem;
    private final Rect mTempRect;
    private int mTopPageBounds;
    private final int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    interface Decor {
    }

    static class ItemInfo {
        Object object;
        float offset;
        int position;
        boolean scrolling;
        float widthFactor;

        ItemInfo() {
        }
    }

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        int childIndex;
        public int gravity;
        public boolean isDecor;
        boolean needsMeasure;
        int position;
        float widthFactor = 0.0f;

        public LayoutParams() {
            super(-1, -1);
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, ViewPager.LAYOUT_ATTRS);
            this.gravity = a.getInteger(0, 48);
            a.recycle();
        }
    }

    interface OnAdapterChangeListener {
        void onAdapterChanged(PagerAdapter pagerAdapter, PagerAdapter pagerAdapter2);
    }

    public interface OnPageChangeListener {
        void onPageScrollStateChanged(int i);

        void onPageScrolled(int i, float f, int i2);

        void onPageSelected(int i);
    }

    public interface PageTransformer {
        void transformPage(View view, float f);
    }

    private class PagerObserver extends DataSetObserver {
        private PagerObserver() {
        }

        /* synthetic */ PagerObserver(ViewPager x0, AnonymousClass1 x1) {
            this();
        }

        public void onChanged() {
            ViewPager.this.dataSetChanged();
        }

        public void onInvalidated() {
            ViewPager.this.dataSetChanged();
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Parcelable adapterState;
        ClassLoader loader;
        int position;

        public SavedState(Parcel source) {
            super(source);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.position);
            out.writeParcelable(this.adapterState, flags);
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("FragmentPager.SavedState{");
            stringBuilder.append(Integer.toHexString(System.identityHashCode(this)));
            stringBuilder.append(" position=");
            stringBuilder.append(this.position);
            stringBuilder.append("}");
            return stringBuilder.toString();
        }

        SavedState(Parcel in, ClassLoader loader) {
            super(in);
            if (loader == null) {
                loader = getClass().getClassLoader();
            }
            this.position = in.readInt();
            this.adapterState = in.readParcelable(loader);
            this.loader = loader;
        }
    }

    static class ViewPositionComparator implements Comparator<View> {
        ViewPositionComparator() {
        }

        public int compare(View lhs, View rhs) {
            LayoutParams llp = (LayoutParams) lhs.getLayoutParams();
            LayoutParams rlp = (LayoutParams) rhs.getLayoutParams();
            if (llp.isDecor == rlp.isDecor) {
                return llp.position - rlp.position;
            }
            return llp.isDecor ? 1 : -1;
        }
    }

    public static class SimpleOnPageChangeListener implements OnPageChangeListener {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
        }

        public void onPageScrollStateChanged(int state) {
        }
    }

    public ViewPager(Context context) {
        this(context, null);
    }

    public ViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ViewPager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mItems = new ArrayList();
        this.mTempItem = new ItemInfo();
        this.mTempRect = new Rect();
        this.mRestoredCurItem = -1;
        this.mRestoredAdapterState = null;
        this.mRestoredClassLoader = null;
        this.mLeftIncr = -1;
        this.mFirstOffset = -3.4028235E38f;
        this.mLastOffset = Float.MAX_VALUE;
        this.mOffscreenPageLimit = 1;
        this.mActivePointerId = -1;
        this.mFirstLayout = true;
        this.mEndScrollRunnable = new Runnable() {
            public void run() {
                ViewPager.this.setScrollState(0);
                ViewPager.this.populate();
            }
        };
        this.mScrollState = 0;
        setWillNotDraw(false);
        setDescendantFocusability(262144);
        setFocusable(true);
        this.mScroller = new Scroller(context, sInterpolator);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        float density = context.getResources().getDisplayMetrics().density;
        this.mTouchSlop = configuration.getScaledPagingTouchSlop();
        this.mMinimumVelocity = (int) (400.0f * density);
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mLeftEdge = new EdgeEffect(context);
        this.mRightEdge = new EdgeEffect(context);
        this.mFlingDistance = (int) (25.0f * density);
        this.mCloseEnough = (int) (2.0f * density);
        this.mDefaultGutterSize = (int) (16.0f * density);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDetachedFromWindow() {
        removeCallbacks(this.mEndScrollRunnable);
        super.onDetachedFromWindow();
    }

    private void setScrollState(int newState) {
        if (this.mScrollState != newState) {
            this.mScrollState = newState;
            if (this.mPageTransformer != null) {
                enableLayers(newState != 0);
            }
            if (this.mOnPageChangeListener != null) {
                this.mOnPageChangeListener.onPageScrollStateChanged(newState);
            }
        }
    }

    public void setAdapter(PagerAdapter adapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mObserver);
            this.mAdapter.startUpdate((ViewGroup) this);
            for (int i = 0; i < this.mItems.size(); i++) {
                ItemInfo ii = (ItemInfo) this.mItems.get(i);
                this.mAdapter.destroyItem((ViewGroup) this, ii.position, ii.object);
            }
            this.mAdapter.finishUpdate((ViewGroup) this);
            this.mItems.clear();
            removeNonDecorViews();
            this.mCurItem = 0;
            scrollTo(0, 0);
        }
        PagerAdapter oldAdapter = this.mAdapter;
        this.mAdapter = adapter;
        this.mExpectedAdapterCount = 0;
        if (this.mAdapter != null) {
            if (this.mObserver == null) {
                this.mObserver = new PagerObserver(this, null);
            }
            this.mAdapter.registerDataSetObserver(this.mObserver);
            this.mPopulatePending = false;
            boolean wasFirstLayout = this.mFirstLayout;
            this.mFirstLayout = true;
            this.mExpectedAdapterCount = this.mAdapter.getCount();
            if (this.mRestoredCurItem >= 0) {
                this.mAdapter.restoreState(this.mRestoredAdapterState, this.mRestoredClassLoader);
                setCurrentItemInternal(this.mRestoredCurItem, false, true);
                this.mRestoredCurItem = -1;
                this.mRestoredAdapterState = null;
                this.mRestoredClassLoader = null;
            } else if (wasFirstLayout) {
                requestLayout();
            } else {
                populate();
            }
        }
        if (this.mAdapterChangeListener != null && oldAdapter != adapter) {
            this.mAdapterChangeListener.onAdapterChanged(oldAdapter, adapter);
        }
    }

    private void removeNonDecorViews() {
        int i = 0;
        while (i < getChildCount()) {
            if (!((LayoutParams) getChildAt(i).getLayoutParams()).isDecor) {
                removeViewAt(i);
                i--;
            }
            i++;
        }
    }

    public PagerAdapter getAdapter() {
        return this.mAdapter;
    }

    /* Access modifiers changed, original: 0000 */
    public void setOnAdapterChangeListener(OnAdapterChangeListener listener) {
        this.mAdapterChangeListener = listener;
    }

    private int getPaddedWidth() {
        return (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight();
    }

    public void setCurrentItem(int item) {
        this.mPopulatePending = false;
        setCurrentItemInternal(item, this.mFirstLayout ^ 1, false);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        this.mPopulatePending = false;
        setCurrentItemInternal(item, smoothScroll, false);
    }

    public int getCurrentItem() {
        return this.mCurItem;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
        return setCurrentItemInternal(item, smoothScroll, always, 0);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
        boolean dispatchSelected = false;
        if (this.mAdapter == null || this.mAdapter.getCount() <= 0) {
            setScrollingCacheEnabled(false);
            return false;
        }
        item = MathUtils.constrain(item, 0, this.mAdapter.getCount() - 1);
        if (always || this.mCurItem != item || this.mItems.size() == 0) {
            int pageLimit = this.mOffscreenPageLimit;
            if (item > this.mCurItem + pageLimit || item < this.mCurItem - pageLimit) {
                for (int i = 0; i < this.mItems.size(); i++) {
                    ((ItemInfo) this.mItems.get(i)).scrolling = true;
                }
            }
            if (this.mCurItem != item) {
                dispatchSelected = true;
            }
            if (this.mFirstLayout) {
                this.mCurItem = item;
                if (dispatchSelected && this.mOnPageChangeListener != null) {
                    this.mOnPageChangeListener.onPageSelected(item);
                }
                if (dispatchSelected && this.mInternalPageChangeListener != null) {
                    this.mInternalPageChangeListener.onPageSelected(item);
                }
                requestLayout();
            } else {
                populate(item);
                scrollToItem(item, smoothScroll, velocity, dispatchSelected);
            }
            return true;
        }
        setScrollingCacheEnabled(false);
        return false;
    }

    private void scrollToItem(int position, boolean smoothScroll, int velocity, boolean dispatchSelected) {
        int destX = getLeftEdgeForItem(position);
        if (smoothScroll) {
            smoothScrollTo(destX, 0, velocity);
            if (dispatchSelected && this.mOnPageChangeListener != null) {
                this.mOnPageChangeListener.onPageSelected(position);
            }
            if (dispatchSelected && this.mInternalPageChangeListener != null) {
                this.mInternalPageChangeListener.onPageSelected(position);
                return;
            }
            return;
        }
        if (dispatchSelected && this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageSelected(position);
        }
        if (dispatchSelected && this.mInternalPageChangeListener != null) {
            this.mInternalPageChangeListener.onPageSelected(position);
        }
        completeScroll(false);
        scrollTo(destX, 0);
        pageScrolled(destX);
    }

    private int getLeftEdgeForItem(int position) {
        ItemInfo info = infoForPosition(position);
        if (info == null) {
            return 0;
        }
        int width = getPaddedWidth();
        int scaledOffset = (int) (((float) width) * MathUtils.constrain(info.offset, this.mFirstOffset, this.mLastOffset));
        if (ViewUtils.isLayoutRtl(this)) {
            return (16777216 - ((int) ((((float) width) * info.widthFactor) + 1056964608))) - scaledOffset;
        }
        return scaledOffset;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mOnPageChangeListener = listener;
    }

    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        int i = 1;
        boolean hasTransformer = transformer != null;
        boolean needsPopulate = hasTransformer != (this.mPageTransformer != null);
        this.mPageTransformer = transformer;
        setChildrenDrawingOrderEnabled(hasTransformer);
        if (hasTransformer) {
            if (reverseDrawingOrder) {
                i = 2;
            }
            this.mDrawingOrder = i;
        } else {
            this.mDrawingOrder = 0;
        }
        if (needsPopulate) {
            populate();
        }
    }

    /* Access modifiers changed, original: protected */
    public int getChildDrawingOrder(int childCount, int i) {
        return ((LayoutParams) ((View) this.mDrawingOrderedChildren.get(this.mDrawingOrder == 2 ? (childCount - 1) - i : i)).getLayoutParams()).childIndex;
    }

    /* Access modifiers changed, original: 0000 */
    public OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener listener) {
        OnPageChangeListener oldListener = this.mInternalPageChangeListener;
        this.mInternalPageChangeListener = listener;
        return oldListener;
    }

    public int getOffscreenPageLimit() {
        return this.mOffscreenPageLimit;
    }

    public void setOffscreenPageLimit(int limit) {
        if (limit < 1) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Requested offscreen page limit ");
            stringBuilder.append(limit);
            stringBuilder.append(" too small; defaulting to ");
            stringBuilder.append(1);
            Log.w(str, stringBuilder.toString());
            limit = 1;
        }
        if (limit != this.mOffscreenPageLimit) {
            this.mOffscreenPageLimit = limit;
            populate();
        }
    }

    public void setPageMargin(int marginPixels) {
        int oldMargin = this.mPageMargin;
        this.mPageMargin = marginPixels;
        int width = getWidth();
        recomputeScrollPosition(width, width, marginPixels, oldMargin);
        requestLayout();
    }

    public int getPageMargin() {
        return this.mPageMargin;
    }

    public void setPageMarginDrawable(Drawable d) {
        this.mMarginDrawable = d;
        if (d != null) {
            refreshDrawableState();
        }
        setWillNotDraw(d == null);
        invalidate();
    }

    public void setPageMarginDrawable(int resId) {
        setPageMarginDrawable(getContext().getDrawable(resId));
    }

    /* Access modifiers changed, original: protected */
    public boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mMarginDrawable;
    }

    /* Access modifiers changed, original: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable marginDrawable = this.mMarginDrawable;
        if (marginDrawable != null && marginDrawable.isStateful() && marginDrawable.setState(getDrawableState())) {
            invalidateDrawable(marginDrawable);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((double) ((float) (((double) (f - 0.5f)) * 0.4712389167638204d)));
    }

    /* Access modifiers changed, original: 0000 */
    public void smoothScrollTo(int x, int y) {
        smoothScrollTo(x, y, 0);
    }

    /* Access modifiers changed, original: 0000 */
    public void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() == 0) {
            setScrollingCacheEnabled(false);
            return;
        }
        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll(false);
            populate();
            setScrollState(0);
            return;
        }
        int duration;
        setScrollingCacheEnabled(true);
        setScrollState(2);
        int width = getPaddedWidth();
        int halfWidth = width / 2;
        float distance = ((float) halfWidth) + (((float) halfWidth) * distanceInfluenceForSnapDuration(Math.min(1.0f, (((float) Math.abs(dx)) * 1.0f) / ((float) width))));
        int velocity2 = Math.abs(velocity);
        if (velocity2 > 0) {
            duration = 4 * Math.round(1000.0f * Math.abs(distance / ((float) velocity2)));
        } else {
            duration = (int) ((1.0f + (((float) Math.abs(dx)) / (((float) this.mPageMargin) + (((float) width) * this.mAdapter.getPageWidth(this.mCurItem))))) * 100.0f);
        }
        this.mScroller.startScroll(sx, sy, dx, dy, Math.min(duration, 600));
        postInvalidateOnAnimation();
    }

    /* Access modifiers changed, original: 0000 */
    public ItemInfo addNewItem(int position, int index) {
        ItemInfo ii = new ItemInfo();
        ii.position = position;
        ii.object = this.mAdapter.instantiateItem((ViewGroup) this, position);
        ii.widthFactor = this.mAdapter.getPageWidth(position);
        if (index < 0 || index >= this.mItems.size()) {
            this.mItems.add(ii);
        } else {
            this.mItems.add(index, ii);
        }
        return ii;
    }

    /* Access modifiers changed, original: 0000 */
    public void dataSetChanged() {
        int adapterCount = this.mAdapter.getCount();
        this.mExpectedAdapterCount = adapterCount;
        boolean needPopulate = this.mItems.size() < (this.mOffscreenPageLimit * 2) + 1 && this.mItems.size() < adapterCount;
        boolean isUpdating = false;
        int newCurrItem = this.mCurItem;
        boolean needPopulate2 = needPopulate;
        int i = 0;
        while (i < this.mItems.size()) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            int newPos = this.mAdapter.getItemPosition(ii.object);
            if (newPos != -1) {
                if (newPos == -2) {
                    this.mItems.remove(i);
                    i--;
                    if (!isUpdating) {
                        this.mAdapter.startUpdate((ViewGroup) this);
                        isUpdating = true;
                    }
                    this.mAdapter.destroyItem((ViewGroup) this, ii.position, ii.object);
                    needPopulate2 = true;
                    if (this.mCurItem == ii.position) {
                        newCurrItem = Math.max(0, Math.min(this.mCurItem, adapterCount - 1));
                        needPopulate2 = true;
                    }
                } else if (ii.position != newPos) {
                    if (ii.position == this.mCurItem) {
                        newCurrItem = newPos;
                    }
                    ii.position = newPos;
                    needPopulate2 = true;
                }
            }
            i++;
        }
        if (isUpdating) {
            this.mAdapter.finishUpdate((ViewGroup) this);
        }
        Collections.sort(this.mItems, COMPARATOR);
        if (needPopulate2) {
            i = getChildCount();
            for (int i2 = 0; i2 < i; i2++) {
                LayoutParams lp = (LayoutParams) getChildAt(i2).getLayoutParams();
                if (!lp.isDecor) {
                    lp.widthFactor = 0.0f;
                }
            }
            setCurrentItemInternal(newCurrItem, false, true);
            requestLayout();
        }
    }

    public void populate() {
        populate(this.mCurItem);
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x01f8  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x01f5  */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x0208  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x023b  */
    public void populate(int r24) {
        /*
        r23 = this;
        r1 = r23;
        r2 = r24;
        r0 = 0;
        r3 = 2;
        r4 = r1.mCurItem;
        if (r4 == r2) goto L_0x001c;
    L_0x000a:
        r4 = r1.mCurItem;
        if (r4 >= r2) goto L_0x0011;
    L_0x000e:
        r4 = 66;
        goto L_0x0013;
    L_0x0011:
        r4 = 17;
    L_0x0013:
        r3 = r4;
        r4 = r1.mCurItem;
        r0 = r1.infoForPosition(r4);
        r1.mCurItem = r2;
    L_0x001c:
        r4 = r3;
        r3 = r0;
        r0 = r1.mAdapter;
        if (r0 != 0) goto L_0x0026;
    L_0x0022:
        r23.sortChildDrawingOrder();
        return;
    L_0x0026:
        r0 = r1.mPopulatePending;
        if (r0 == 0) goto L_0x002e;
    L_0x002a:
        r23.sortChildDrawingOrder();
        return;
    L_0x002e:
        r0 = r23.getWindowToken();
        if (r0 != 0) goto L_0x0035;
    L_0x0034:
        return;
    L_0x0035:
        r0 = r1.mAdapter;
        r0.startUpdate(r1);
        r5 = r1.mOffscreenPageLimit;
        r0 = r1.mCurItem;
        r0 = r0 - r5;
        r6 = 0;
        r7 = java.lang.Math.max(r6, r0);
        r0 = r1.mAdapter;
        r8 = r0.getCount();
        r0 = r8 + -1;
        r9 = r1.mCurItem;
        r9 = r9 + r5;
        r9 = java.lang.Math.min(r0, r9);
        r0 = r1.mExpectedAdapterCount;
        if (r8 != r0) goto L_0x028e;
    L_0x0057:
        r0 = -1;
        r10 = 0;
        r0 = 0;
    L_0x005a:
        r11 = r1.mItems;
        r11 = r11.size();
        if (r0 >= r11) goto L_0x007b;
    L_0x0062:
        r11 = r1.mItems;
        r11 = r11.get(r0);
        r11 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r11;
        r12 = r11.position;
        r13 = r1.mCurItem;
        if (r12 < r13) goto L_0x0078;
    L_0x0070:
        r12 = r11.position;
        r13 = r1.mCurItem;
        if (r12 != r13) goto L_0x007b;
    L_0x0076:
        r10 = r11;
        goto L_0x007b;
    L_0x0078:
        r0 = r0 + 1;
        goto L_0x005a;
    L_0x007b:
        if (r10 != 0) goto L_0x0085;
    L_0x007d:
        if (r8 <= 0) goto L_0x0085;
    L_0x007f:
        r11 = r1.mCurItem;
        r10 = r1.addNewItem(r11, r0);
    L_0x0085:
        if (r10 == 0) goto L_0x01e9;
    L_0x0087:
        r13 = 0;
        r14 = r0 + -1;
        if (r14 < 0) goto L_0x0095;
    L_0x008c:
        r15 = r1.mItems;
        r15 = r15.get(r14);
        r15 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r15;
        goto L_0x0096;
    L_0x0095:
        r15 = 0;
    L_0x0096:
        r6 = r23.getPaddedWidth();
        r17 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r6 > 0) goto L_0x00a2;
    L_0x009e:
        r19 = r0;
        r11 = 0;
        goto L_0x00b0;
    L_0x00a2:
        r12 = r10.widthFactor;
        r12 = r17 - r12;
        r11 = r23.getPaddingLeft();
        r11 = (float) r11;
        r19 = r0;
        r0 = (float) r6;
        r11 = r11 / r0;
        r11 = r11 + r12;
    L_0x00b0:
        r0 = r11;
        r11 = r1.mCurItem;
        r11 = r11 + -1;
        r12 = r19;
    L_0x00b7:
        if (r11 < 0) goto L_0x0127;
    L_0x00b9:
        r19 = (r13 > r0 ? 1 : (r13 == r0 ? 0 : -1));
        if (r19 < 0) goto L_0x00ed;
    L_0x00bd:
        if (r11 >= r7) goto L_0x00ed;
    L_0x00bf:
        if (r15 != 0) goto L_0x00c6;
        r20 = r0;
        goto L_0x0129;
    L_0x00c6:
        r20 = r0;
        r0 = r15.position;
        if (r11 != r0) goto L_0x0120;
    L_0x00cc:
        r0 = r15.scrolling;
        if (r0 != 0) goto L_0x0120;
    L_0x00d0:
        r0 = r1.mItems;
        r0.remove(r14);
        r0 = r1.mAdapter;
        r2 = r15.object;
        r0.destroyItem(r1, r11, r2);
        r14 = r14 + -1;
        r12 = r12 + -1;
        if (r14 < 0) goto L_0x00eb;
    L_0x00e2:
        r0 = r1.mItems;
        r0 = r0.get(r14);
        r0 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r0;
        goto L_0x00ec;
    L_0x00eb:
        r0 = 0;
    L_0x00ec:
        goto L_0x0106;
    L_0x00ed:
        r20 = r0;
        if (r15 == 0) goto L_0x0107;
    L_0x00f1:
        r0 = r15.position;
        if (r11 != r0) goto L_0x0107;
    L_0x00f5:
        r0 = r15.widthFactor;
        r13 = r13 + r0;
        r14 = r14 + -1;
        if (r14 < 0) goto L_0x0105;
    L_0x00fc:
        r0 = r1.mItems;
        r0 = r0.get(r14);
        r0 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r0;
        goto L_0x0106;
    L_0x0105:
        r0 = 0;
    L_0x0106:
        goto L_0x011f;
    L_0x0107:
        r0 = r14 + 1;
        r0 = r1.addNewItem(r11, r0);
        r2 = r0.widthFactor;
        r13 = r13 + r2;
        r12 = r12 + 1;
        if (r14 < 0) goto L_0x011d;
    L_0x0114:
        r2 = r1.mItems;
        r2 = r2.get(r14);
        r2 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r2;
        goto L_0x011e;
    L_0x011d:
        r2 = 0;
    L_0x011e:
        r0 = r2;
    L_0x011f:
        r15 = r0;
    L_0x0120:
        r11 = r11 + -1;
        r0 = r20;
        r2 = r24;
        goto L_0x00b7;
    L_0x0127:
        r20 = r0;
    L_0x0129:
        r0 = r10.widthFactor;
        r2 = r12 + 1;
        r11 = (r0 > r17 ? 1 : (r0 == r17 ? 0 : -1));
        if (r11 >= 0) goto L_0x01e1;
    L_0x0131:
        r11 = r1.mItems;
        r11 = r11.size();
        if (r2 >= r11) goto L_0x0142;
    L_0x0139:
        r11 = r1.mItems;
        r11 = r11.get(r2);
        r11 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r11;
        goto L_0x0143;
    L_0x0142:
        r11 = 0;
    L_0x0143:
        if (r6 > 0) goto L_0x0147;
    L_0x0145:
        r14 = 0;
        goto L_0x0150;
    L_0x0147:
        r14 = r23.getPaddingRight();
        r14 = (float) r14;
        r15 = (float) r6;
        r14 = r14 / r15;
        r14 = r14 + r17;
    L_0x0150:
        r15 = r1.mCurItem;
        r15 = r15 + 1;
    L_0x0154:
        if (r15 >= r8) goto L_0x01db;
    L_0x0156:
        r17 = (r0 > r14 ? 1 : (r0 == r14 ? 0 : -1));
        if (r17 < 0) goto L_0x0194;
    L_0x015a:
        if (r15 <= r9) goto L_0x0194;
    L_0x015c:
        if (r11 != 0) goto L_0x0165;
        r21 = r5;
        r22 = r6;
        goto L_0x01df;
    L_0x0165:
        r21 = r5;
        r5 = r11.position;
        if (r15 != r5) goto L_0x0191;
    L_0x016b:
        r5 = r11.scrolling;
        if (r5 != 0) goto L_0x0191;
    L_0x016f:
        r5 = r1.mItems;
        r5.remove(r2);
        r5 = r1.mAdapter;
        r22 = r6;
        r6 = r11.object;
        r5.destroyItem(r1, r15, r6);
        r5 = r1.mItems;
        r5 = r5.size();
        if (r2 >= r5) goto L_0x018e;
    L_0x0185:
        r5 = r1.mItems;
        r5 = r5.get(r2);
        r5 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r5;
        goto L_0x018f;
    L_0x018e:
        r5 = 0;
    L_0x018f:
        r11 = r5;
        goto L_0x01d3;
    L_0x0191:
        r22 = r6;
        goto L_0x01d3;
    L_0x0194:
        r21 = r5;
        r22 = r6;
        if (r11 == 0) goto L_0x01b7;
    L_0x019a:
        r5 = r11.position;
        if (r15 != r5) goto L_0x01b7;
    L_0x019e:
        r5 = r11.widthFactor;
        r0 = r0 + r5;
        r2 = r2 + 1;
        r5 = r1.mItems;
        r5 = r5.size();
        if (r2 >= r5) goto L_0x01b4;
    L_0x01ab:
        r5 = r1.mItems;
        r5 = r5.get(r2);
        r5 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r5;
        goto L_0x01b5;
    L_0x01b4:
        r5 = 0;
    L_0x01b5:
        r11 = r5;
        goto L_0x01d3;
    L_0x01b7:
        r5 = r1.addNewItem(r15, r2);
        r2 = r2 + 1;
        r6 = r5.widthFactor;
        r0 = r0 + r6;
        r6 = r1.mItems;
        r6 = r6.size();
        if (r2 >= r6) goto L_0x01d1;
    L_0x01c8:
        r6 = r1.mItems;
        r6 = r6.get(r2);
        r6 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r6;
        goto L_0x01d2;
    L_0x01d1:
        r6 = 0;
    L_0x01d2:
        r11 = r6;
    L_0x01d3:
        r15 = r15 + 1;
        r5 = r21;
        r6 = r22;
        goto L_0x0154;
    L_0x01db:
        r21 = r5;
        r22 = r6;
    L_0x01df:
        r15 = r11;
        goto L_0x01e5;
    L_0x01e1:
        r21 = r5;
        r22 = r6;
    L_0x01e5:
        r1.calculatePageOffsets(r10, r12, r3);
        goto L_0x01ef;
    L_0x01e9:
        r19 = r0;
        r21 = r5;
        r12 = r19;
    L_0x01ef:
        r0 = r1.mAdapter;
        r2 = r1.mCurItem;
        if (r10 == 0) goto L_0x01f8;
    L_0x01f5:
        r5 = r10.object;
        goto L_0x01f9;
    L_0x01f8:
        r5 = 0;
    L_0x01f9:
        r0.setPrimaryItem(r1, r2, r5);
        r0 = r1.mAdapter;
        r0.finishUpdate(r1);
        r0 = r23.getChildCount();
        r2 = 0;
    L_0x0206:
        if (r2 >= r0) goto L_0x0232;
    L_0x0208:
        r5 = r1.getChildAt(r2);
        r6 = r5.getLayoutParams();
        r6 = (com.oneplus.lib.widget.ViewPager.LayoutParams) r6;
        r6.childIndex = r2;
        r11 = r6.isDecor;
        if (r11 != 0) goto L_0x022e;
    L_0x0218:
        r11 = r6.widthFactor;
        r13 = 0;
        r11 = (r11 > r13 ? 1 : (r11 == r13 ? 0 : -1));
        if (r11 != 0) goto L_0x022f;
    L_0x021f:
        r11 = r1.infoForChild(r5);
        if (r11 == 0) goto L_0x022f;
    L_0x0225:
        r14 = r11.widthFactor;
        r6.widthFactor = r14;
        r14 = r11.position;
        r6.position = r14;
        goto L_0x022f;
    L_0x022e:
        r13 = 0;
    L_0x022f:
        r2 = r2 + 1;
        goto L_0x0206;
    L_0x0232:
        r23.sortChildDrawingOrder();
        r2 = r23.hasFocus();
        if (r2 == 0) goto L_0x028d;
    L_0x023b:
        r2 = r23.findFocus();
        if (r2 == 0) goto L_0x0248;
    L_0x0241:
        r5 = r1.infoForAnyChild(r2);
        r18 = r5;
        goto L_0x024a;
    L_0x0248:
        r18 = 0;
    L_0x024a:
        r5 = r18;
        if (r5 == 0) goto L_0x0254;
    L_0x024e:
        r6 = r5.position;
        r11 = r1.mCurItem;
        if (r6 == r11) goto L_0x028d;
    L_0x0254:
        r16 = 0;
    L_0x0256:
        r6 = r16;
        r11 = r23.getChildCount();
        if (r6 >= r11) goto L_0x028d;
    L_0x025e:
        r11 = r1.getChildAt(r6);
        r5 = r1.infoForChild(r11);
        if (r5 == 0) goto L_0x028a;
    L_0x0268:
        r13 = r5.position;
        r14 = r1.mCurItem;
        if (r13 != r14) goto L_0x028a;
    L_0x026e:
        if (r2 != 0) goto L_0x0272;
    L_0x0270:
        r13 = 0;
        goto L_0x0283;
    L_0x0272:
        r13 = r1.mTempRect;
        r14 = r1.mTempRect;
        r2.getFocusedRect(r14);
        r14 = r1.mTempRect;
        r1.offsetDescendantRectToMyCoords(r2, r14);
        r14 = r1.mTempRect;
        r1.offsetRectIntoDescendantCoords(r11, r14);
    L_0x0283:
        r14 = r11.requestFocus(r4, r13);
        if (r14 == 0) goto L_0x028a;
    L_0x0289:
        goto L_0x028d;
    L_0x028a:
        r16 = r6 + 1;
        goto L_0x0256;
    L_0x028d:
        return;
    L_0x028e:
        r21 = r5;
        r0 = r23.getResources();	 Catch:{ NotFoundException -> 0x029d }
        r2 = r23.getId();	 Catch:{ NotFoundException -> 0x029d }
        r0 = r0.getResourceName(r2);	 Catch:{ NotFoundException -> 0x029d }
        goto L_0x02a6;
    L_0x029d:
        r0 = move-exception;
        r2 = r23.getId();
        r0 = java.lang.Integer.toHexString(r2);
    L_0x02a6:
        r2 = new java.lang.IllegalStateException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: ";
        r5.append(r6);
        r6 = r1.mExpectedAdapterCount;
        r5.append(r6);
        r6 = ", found: ";
        r5.append(r6);
        r5.append(r8);
        r6 = " Pager id: ";
        r5.append(r6);
        r5.append(r0);
        r6 = " Pager class: ";
        r5.append(r6);
        r6 = r23.getClass();
        r5.append(r6);
        r6 = " Problematic adapter: ";
        r5.append(r6);
        r6 = r1.mAdapter;
        r6 = r6.getClass();
        r5.append(r6);
        r5 = r5.toString();
        r2.<init>(r5);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.widget.ViewPager.populate(int):void");
    }

    private void sortChildDrawingOrder() {
        if (this.mDrawingOrder != 0) {
            if (this.mDrawingOrderedChildren == null) {
                this.mDrawingOrderedChildren = new ArrayList();
            } else {
                this.mDrawingOrderedChildren.clear();
            }
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mDrawingOrderedChildren.add(getChildAt(i));
            }
            Collections.sort(this.mDrawingOrderedChildren, sPositionComparator);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0056 A:{LOOP_END, LOOP:2: B:18:0x0052->B:20:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009f A:{LOOP_END, LOOP:5: B:33:0x009b->B:35:0x009f} */
    private void calculatePageOffsets(com.oneplus.lib.widget.ViewPager.ItemInfo r12, int r13, com.oneplus.lib.widget.ViewPager.ItemInfo r14) {
        /*
        r11 = this;
        r0 = r11.mAdapter;
        r0 = r0.getCount();
        r1 = r11.getPaddedWidth();
        if (r1 <= 0) goto L_0x0012;
    L_0x000c:
        r2 = r11.mPageMargin;
        r2 = (float) r2;
        r3 = (float) r1;
        r2 = r2 / r3;
        goto L_0x0013;
    L_0x0012:
        r2 = 0;
    L_0x0013:
        if (r14 == 0) goto L_0x00b3;
    L_0x0015:
        r3 = r14.position;
        r4 = r12.position;
        if (r3 >= r4) goto L_0x006b;
    L_0x001b:
        r4 = 0;
        r5 = r14.offset;
        r6 = r14.widthFactor;
        r5 = r5 + r6;
        r5 = r5 + r2;
        r6 = r3 + 1;
    L_0x0024:
        r7 = r12.position;
        if (r6 > r7) goto L_0x006a;
    L_0x0028:
        r7 = r11.mItems;
        r7 = r7.size();
        if (r4 >= r7) goto L_0x006a;
    L_0x0030:
        r7 = r11.mItems;
        r7 = r7.get(r4);
        r7 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r7;
    L_0x0038:
        r8 = r7.position;
        if (r6 <= r8) goto L_0x0052;
    L_0x003c:
        r8 = r11.mItems;
        r8 = r8.size();
        r8 = r8 + -1;
        if (r4 >= r8) goto L_0x0052;
    L_0x0046:
        r4 = r4 + 1;
        r8 = r11.mItems;
        r8 = r8.get(r4);
        r7 = r8;
        r7 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r7;
        goto L_0x0038;
    L_0x0052:
        r8 = r7.position;
        if (r6 >= r8) goto L_0x0061;
    L_0x0056:
        r8 = r11.mAdapter;
        r8 = r8.getPageWidth(r6);
        r8 = r8 + r2;
        r5 = r5 + r8;
        r6 = r6 + 1;
        goto L_0x0052;
    L_0x0061:
        r7.offset = r5;
        r8 = r7.widthFactor;
        r8 = r8 + r2;
        r5 = r5 + r8;
        r6 = r6 + 1;
        goto L_0x0024;
    L_0x006a:
        goto L_0x00b3;
    L_0x006b:
        r4 = r12.position;
        if (r3 <= r4) goto L_0x00b3;
    L_0x006f:
        r4 = r11.mItems;
        r4 = r4.size();
        r4 = r4 + -1;
        r5 = r14.offset;
        r6 = r3 + -1;
    L_0x007b:
        r7 = r12.position;
        if (r6 < r7) goto L_0x00b3;
    L_0x007f:
        if (r4 < 0) goto L_0x00b3;
    L_0x0081:
        r7 = r11.mItems;
        r7 = r7.get(r4);
        r7 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r7;
    L_0x0089:
        r8 = r7.position;
        if (r6 >= r8) goto L_0x009b;
    L_0x008d:
        if (r4 <= 0) goto L_0x009b;
    L_0x008f:
        r4 = r4 + -1;
        r8 = r11.mItems;
        r8 = r8.get(r4);
        r7 = r8;
        r7 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r7;
        goto L_0x0089;
    L_0x009b:
        r8 = r7.position;
        if (r6 <= r8) goto L_0x00aa;
    L_0x009f:
        r8 = r11.mAdapter;
        r8 = r8.getPageWidth(r6);
        r8 = r8 + r2;
        r5 = r5 - r8;
        r6 = r6 + -1;
        goto L_0x009b;
    L_0x00aa:
        r8 = r7.widthFactor;
        r8 = r8 + r2;
        r5 = r5 - r8;
        r7.offset = r5;
        r6 = r6 + -1;
        goto L_0x007b;
    L_0x00b3:
        r3 = r11.mItems;
        r3 = r3.size();
        r4 = r12.offset;
        r5 = r12.position;
        r5 = r5 + -1;
        r6 = r12.position;
        if (r6 != 0) goto L_0x00c6;
    L_0x00c3:
        r6 = r12.offset;
        goto L_0x00c9;
    L_0x00c6:
        r6 = -8388609; // 0xffffffffff7fffff float:-3.4028235E38 double:NaN;
    L_0x00c9:
        r11.mFirstOffset = r6;
        r6 = r12.position;
        r7 = r0 + -1;
        r8 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        if (r6 != r7) goto L_0x00da;
    L_0x00d3:
        r6 = r12.offset;
        r7 = r12.widthFactor;
        r6 = r6 + r7;
        r6 = r6 - r8;
        goto L_0x00dd;
    L_0x00da:
        r6 = 2139095039; // 0x7f7fffff float:3.4028235E38 double:1.056853372E-314;
    L_0x00dd:
        r11.mLastOffset = r6;
        r6 = r13 + -1;
    L_0x00e1:
        if (r6 < 0) goto L_0x010c;
    L_0x00e3:
        r7 = r11.mItems;
        r7 = r7.get(r6);
        r7 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r7;
    L_0x00eb:
        r9 = r7.position;
        if (r5 <= r9) goto L_0x00fb;
    L_0x00ef:
        r9 = r11.mAdapter;
        r10 = r5 + -1;
        r5 = r9.getPageWidth(r5);
        r5 = r5 + r2;
        r4 = r4 - r5;
        r5 = r10;
        goto L_0x00eb;
    L_0x00fb:
        r9 = r7.widthFactor;
        r9 = r9 + r2;
        r4 = r4 - r9;
        r7.offset = r4;
        r9 = r7.position;
        if (r9 != 0) goto L_0x0107;
    L_0x0105:
        r11.mFirstOffset = r4;
    L_0x0107:
        r6 = r6 + -1;
        r5 = r5 + -1;
        goto L_0x00e1;
    L_0x010c:
        r6 = r12.offset;
        r7 = r12.widthFactor;
        r6 = r6 + r7;
        r6 = r6 + r2;
        r4 = r12.position;
        r4 = r4 + 1;
        r5 = r13 + 1;
    L_0x0118:
        if (r5 >= r3) goto L_0x0149;
    L_0x011a:
        r7 = r11.mItems;
        r7 = r7.get(r5);
        r7 = (com.oneplus.lib.widget.ViewPager.ItemInfo) r7;
    L_0x0122:
        r9 = r7.position;
        if (r4 >= r9) goto L_0x0132;
    L_0x0126:
        r9 = r11.mAdapter;
        r10 = r4 + 1;
        r4 = r9.getPageWidth(r4);
        r4 = r4 + r2;
        r6 = r6 + r4;
        r4 = r10;
        goto L_0x0122;
    L_0x0132:
        r9 = r7.position;
        r10 = r0 + -1;
        if (r9 != r10) goto L_0x013e;
    L_0x0138:
        r9 = r7.widthFactor;
        r9 = r9 + r6;
        r9 = r9 - r8;
        r11.mLastOffset = r9;
    L_0x013e:
        r7.offset = r6;
        r9 = r7.widthFactor;
        r9 = r9 + r2;
        r6 = r6 + r9;
        r5 = r5 + 1;
        r4 = r4 + 1;
        goto L_0x0118;
    L_0x0149:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.widget.ViewPager.calculatePageOffsets(com.oneplus.lib.widget.ViewPager$ItemInfo, int, com.oneplus.lib.widget.ViewPager$ItemInfo):void");
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.position = this.mCurItem;
        if (this.mAdapter != null) {
            ss.adapterState = this.mAdapter.saveState();
        }
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (this.mAdapter != null) {
                this.mAdapter.restoreState(ss.adapterState, ss.loader);
                setCurrentItemInternal(ss.position, false, true);
            } else {
                this.mRestoredCurItem = ss.position;
                this.mRestoredAdapterState = ss.adapterState;
                this.mRestoredClassLoader = ss.loader;
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (!checkLayoutParams(params)) {
            params = generateLayoutParams(params);
        }
        LayoutParams lp = (LayoutParams) params;
        lp.isDecor |= child instanceof Decor;
        if (!this.mInLayout) {
            super.addView(child, index, params);
        } else if (lp == null || !lp.isDecor) {
            lp.needsMeasure = true;
            addViewInLayout(child, index, params);
        } else {
            throw new IllegalStateException("Cannot add pager decor view during layout");
        }
    }

    public Object getCurrent() {
        ItemInfo itemInfo = infoForPosition(getCurrentItem());
        return itemInfo == null ? null : itemInfo.object;
    }

    public void removeView(View view) {
        if (this.mInLayout) {
            removeViewInLayout(view);
        } else {
            super.removeView(view);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public ItemInfo infoForChild(View child) {
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (this.mAdapter.isViewFromObject(child, ii.object)) {
                return ii;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public ItemInfo infoForAnyChild(View child) {
        while (true) {
            Object parent = child.getParent();
            ViewParent parent2 = parent;
            if (parent == this) {
                return infoForChild(child);
            }
            if (parent2 != null && (parent2 instanceof View)) {
                child = (View) parent2;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    public ItemInfo infoForPosition(int position) {
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (ii.position == position) {
                return ii;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00cd  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c6  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00cd  */
    public void onMeasure(int r23, int r24) {
        /*
        r22 = this;
        r0 = r22;
        r1 = 0;
        r2 = r23;
        r3 = getDefaultSize(r1, r2);
        r4 = r24;
        r5 = getDefaultSize(r1, r4);
        r0.setMeasuredDimension(r3, r5);
        r3 = r22.getMeasuredWidth();
        r5 = r3 / 10;
        r6 = r0.mDefaultGutterSize;
        r6 = java.lang.Math.min(r5, r6);
        r0.mGutterSize = r6;
        r6 = r22.getPaddingLeft();
        r6 = r3 - r6;
        r7 = r22.getPaddingRight();
        r6 = r6 - r7;
        r7 = r22.getMeasuredHeight();
        r8 = r22.getPaddingTop();
        r7 = r7 - r8;
        r8 = r22.getPaddingBottom();
        r7 = r7 - r8;
        r8 = r22.getChildCount();
        r9 = r7;
        r7 = r6;
        r6 = r1;
    L_0x0040:
        r10 = 8;
        if (r6 >= r8) goto L_0x00e7;
    L_0x0044:
        r12 = r0.getChildAt(r6);
        r13 = r12.getVisibility();
        if (r13 == r10) goto L_0x00d6;
    L_0x004e:
        r10 = r12.getLayoutParams();
        r10 = (com.oneplus.lib.widget.ViewPager.LayoutParams) r10;
        if (r10 == 0) goto L_0x00d6;
    L_0x0056:
        r13 = r10.isDecor;
        if (r13 == 0) goto L_0x00d6;
    L_0x005a:
        r13 = r10.gravity;
        r13 = r13 & 7;
        r14 = r10.gravity;
        r14 = r14 & 112;
        r15 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r16 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r1 = 48;
        if (r14 == r1) goto L_0x0071;
    L_0x006a:
        r1 = 80;
        if (r14 != r1) goto L_0x006f;
    L_0x006e:
        goto L_0x0071;
    L_0x006f:
        r1 = 0;
        goto L_0x0072;
    L_0x0071:
        r1 = 1;
    L_0x0072:
        r11 = 3;
        if (r13 == r11) goto L_0x007c;
    L_0x0075:
        r11 = 5;
        if (r13 != r11) goto L_0x0079;
    L_0x0078:
        goto L_0x007c;
    L_0x0079:
        r17 = 0;
        goto L_0x007e;
    L_0x007c:
        r17 = 1;
    L_0x007e:
        r11 = r17;
        if (r1 == 0) goto L_0x0085;
    L_0x0082:
        r15 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        goto L_0x0089;
    L_0x0085:
        if (r11 == 0) goto L_0x0089;
    L_0x0087:
        r16 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
    L_0x0089:
        r17 = r7;
        r18 = r9;
        r2 = r10.width;
        r19 = r3;
        r3 = -2;
        if (r2 == r3) goto L_0x009e;
    L_0x0094:
        r15 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r2 = r10.width;
        r3 = -1;
        if (r2 == r3) goto L_0x009e;
    L_0x009b:
        r2 = r10.width;
        goto L_0x00a0;
    L_0x009e:
        r2 = r17;
    L_0x00a0:
        r3 = r10.height;
        r4 = -2;
        if (r3 == r4) goto L_0x00b1;
    L_0x00a5:
        r16 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r3 = r10.height;
        r4 = -1;
        if (r3 == r4) goto L_0x00b1;
    L_0x00ac:
        r3 = r10.height;
        r4 = r16;
        goto L_0x00b5;
    L_0x00b1:
        r4 = r16;
        r3 = r18;
    L_0x00b5:
        r20 = r5;
        r5 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r15);
        r21 = r2;
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r3, r4);
        r12.measure(r5, r2);
        if (r1 == 0) goto L_0x00cd;
    L_0x00c6:
        r16 = r12.getMeasuredHeight();
        r9 = r9 - r16;
        goto L_0x00da;
    L_0x00cd:
        if (r11 == 0) goto L_0x00da;
    L_0x00cf:
        r16 = r12.getMeasuredWidth();
        r7 = r7 - r16;
        goto L_0x00da;
    L_0x00d6:
        r19 = r3;
        r20 = r5;
    L_0x00da:
        r6 = r6 + 1;
        r3 = r19;
        r5 = r20;
        r1 = 0;
        r2 = r23;
        r4 = r24;
        goto L_0x0040;
    L_0x00e7:
        r19 = r3;
        r20 = r5;
        r1 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r7, r1);
        r0.mChildWidthMeasureSpec = r2;
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r9, r1);
        r0.mChildHeightMeasureSpec = r2;
        r2 = 1;
        r0.mInLayout = r2;
        r22.populate();
        r2 = 0;
        r0.mInLayout = r2;
        r3 = r22.getChildCount();
    L_0x0107:
        if (r2 >= r3) goto L_0x0130;
    L_0x0109:
        r4 = r0.getChildAt(r2);
        r5 = r4.getVisibility();
        if (r5 == r10) goto L_0x012d;
    L_0x0113:
        r5 = r4.getLayoutParams();
        r5 = (com.oneplus.lib.widget.ViewPager.LayoutParams) r5;
        if (r5 == 0) goto L_0x011f;
    L_0x011b:
        r6 = r5.isDecor;
        if (r6 != 0) goto L_0x012d;
    L_0x011f:
        r6 = (float) r7;
        r8 = r5.widthFactor;
        r6 = r6 * r8;
        r6 = (int) r6;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r6, r1);
        r8 = r0.mChildHeightMeasureSpec;
        r4.measure(r6, r8);
    L_0x012d:
        r2 = r2 + 1;
        goto L_0x0107;
    L_0x0130:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.widget.ViewPager.onMeasure(int, int):void");
    }

    /* Access modifiers changed, original: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            recomputeScrollPosition(w, oldw, this.mPageMargin, this.mPageMargin);
        }
    }

    private void recomputeScrollPosition(int width, int oldWidth, int margin, int oldMargin) {
        int i = width;
        if (oldWidth <= 0 || this.mItems.isEmpty()) {
            ItemInfo ii = infoForPosition(this.mCurItem);
            int scrollPos = (int) (((float) ((i - getPaddingLeft()) - getPaddingRight())) * (ii != null ? Math.min(ii.offset, this.mLastOffset) : 0.0f));
            if (scrollPos != getScrollX()) {
                completeScroll(false);
                scrollTo(scrollPos, getScrollY());
                return;
            }
            return;
        }
        int newOffsetPixels = (int) (((float) (((i - getPaddingLeft()) - getPaddingRight()) + margin)) * (((float) getScrollX()) / ((float) (((oldWidth - getPaddingLeft()) - getPaddingRight()) + oldMargin))));
        scrollTo(newOffsetPixels, getScrollY());
        if (!this.mScroller.isFinished()) {
            int newDuration = this.mScroller.getDuration() - this.mScroller.timePassed();
            ItemInfo targetInfo = infoForPosition(this.mCurItem);
            this.mScroller.startScroll(newOffsetPixels, 0, (int) (targetInfo.offset * ((float) i)), 0, newDuration);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int hgrav;
        int childLeft;
        int width;
        int childWidth;
        boolean z;
        int count = getChildCount();
        int width2 = r - l;
        int height = b - t;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int scrollX = getScrollX();
        int decorCount = 0;
        int paddingBottom2 = paddingBottom;
        paddingBottom = paddingTop;
        paddingTop = paddingLeft;
        for (paddingLeft = 0; paddingLeft < count; paddingLeft++) {
            View child = getChildAt(paddingLeft);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int childLeft2 = 0;
                if (lp.isDecor != 0) {
                    hgrav = lp.gravity & 7;
                    int vgrav = lp.gravity & 112;
                    if (hgrav == 1) {
                        childLeft = Math.max((width2 - child.getMeasuredWidth()) / 2, paddingTop);
                    } else if (hgrav == 3) {
                        childLeft = paddingTop;
                        paddingTop += child.getMeasuredWidth();
                    } else if (hgrav != 5) {
                        childLeft = paddingTop;
                    } else {
                        childLeft = (width2 - paddingRight) - child.getMeasuredWidth();
                        paddingRight += child.getMeasuredWidth();
                    }
                    if (vgrav == 16) {
                        hgrav = Math.max((height - child.getMeasuredHeight()) / 2, paddingBottom);
                    } else if (vgrav == 48) {
                        hgrav = paddingBottom;
                        paddingBottom += child.getMeasuredHeight();
                    } else if (vgrav != 80) {
                        hgrav = paddingBottom;
                    } else {
                        hgrav = (height - paddingBottom2) - child.getMeasuredHeight();
                        paddingBottom2 += child.getMeasuredHeight();
                    }
                    childLeft += scrollX;
                    child.layout(childLeft, hgrav, childLeft + child.getMeasuredWidth(), hgrav + child.getMeasuredHeight());
                    decorCount++;
                }
            }
        }
        childLeft = (width2 - paddingTop) - paddingRight;
        hgrav = 0;
        while (hgrav < count) {
            int count2;
            View child2 = getChildAt(hgrav);
            if (child2.getVisibility() != 8) {
                LayoutParams lp2 = (LayoutParams) child2.getLayoutParams();
                if (!lp2.isDecor) {
                    ItemInfo ii = infoForChild(child2);
                    if (ii != null) {
                        if (lp2.needsMeasure) {
                            lp2.needsMeasure = false;
                            count2 = count;
                            width = width2;
                            child2.measure(MeasureSpec.makeMeasureSpec((int) (((float) childLeft) * lp2.widthFactor), Ints.MAX_POWER_OF_TWO), MeasureSpec.makeMeasureSpec((height - paddingBottom) - paddingBottom2, Ints.MAX_POWER_OF_TWO));
                        } else {
                            count2 = count;
                            width = width2;
                        }
                        count = child2.getMeasuredWidth();
                        width2 = (int) (((float) childLeft) * ii.offset);
                        if (ViewUtils.isLayoutRtl(this)) {
                            paddingLeft = ((16777216 - paddingRight) - width2) - count;
                        } else {
                            paddingLeft = paddingTop + width2;
                        }
                        childWidth = childLeft;
                        childLeft = paddingLeft + count;
                        int childMeasuredWidth = count;
                        count = paddingBottom;
                        child2.layout(paddingLeft, count, childLeft, count + child2.getMeasuredHeight());
                        hgrav++;
                        count = count2;
                        width2 = width;
                        childLeft = childWidth;
                    }
                }
            }
            count2 = count;
            childWidth = childLeft;
            width = width2;
            hgrav++;
            count = count2;
            width2 = width;
            childLeft = childWidth;
        }
        childWidth = childLeft;
        width = width2;
        this.mTopPageBounds = paddingBottom;
        this.mBottomPageBounds = height - paddingBottom2;
        this.mDecorChildCount = decorCount;
        if (this.mFirstLayout) {
            z = false;
            scrollToItem(this.mCurItem, false, 0, false);
        } else {
            z = false;
        }
        this.mFirstLayout = z;
    }

    public void computeScroll() {
        if (this.mScroller.isFinished() || !this.mScroller.computeScrollOffset()) {
            completeScroll(true);
            return;
        }
        int oldX = getScrollX();
        int oldY = getScrollY();
        int x = this.mScroller.getCurrX();
        int y = this.mScroller.getCurrY();
        if (!(oldX == x && oldY == y)) {
            scrollTo(x, y);
            if (!pageScrolled(x)) {
                this.mScroller.abortAnimation();
                scrollTo(0, y);
            }
        }
        postInvalidateOnAnimation();
    }

    private boolean pageScrolled(int scrollX) {
        if (this.mItems.size() == 0) {
            this.mCalledSuper = false;
            onPageScrolled(0, 0.0f, 0);
            if (this.mCalledSuper) {
                return false;
            }
            throw new IllegalStateException("onPageScrolled did not call superclass implementation");
        }
        int scrollStart;
        if (ViewUtils.isLayoutRtl(this)) {
            scrollStart = 16777216 - scrollX;
        } else {
            scrollStart = scrollX;
        }
        ItemInfo ii = infoForFirstVisiblePage();
        int width = getPaddedWidth();
        int widthWithMargin = this.mPageMargin + width;
        float marginOffset = ((float) this.mPageMargin) / ((float) width);
        int currentPage = ii.position;
        float pageOffset = ((((float) scrollStart) / ((float) width)) - ii.offset) / (ii.widthFactor + marginOffset);
        int offsetPixels = (int) (((float) widthWithMargin) * pageOffset);
        this.mCalledSuper = false;
        onPageScrolled(currentPage, pageOffset, offsetPixels);
        if (this.mCalledSuper) {
            return true;
        }
        throw new IllegalStateException("onPageScrolled did not call superclass implementation");
    }

    /* Access modifiers changed, original: protected */
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        int scrollX;
        int paddingLeft;
        int childLeft;
        int i = position;
        float f = offset;
        int i2 = offsetPixels;
        if (this.mDecorChildCount > 0) {
            scrollX = getScrollX();
            paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int width = getWidth();
            int childCount = getChildCount();
            int paddingRight2 = paddingRight;
            paddingRight = paddingLeft;
            for (paddingLeft = 0; paddingLeft < childCount; paddingLeft++) {
                View child = getChildAt(paddingLeft);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.isDecor) {
                    int hgrav = lp.gravity & 7;
                    if (hgrav == 1) {
                        childLeft = Math.max((width - child.getMeasuredWidth()) / 2, paddingRight);
                    } else if (hgrav == 3) {
                        childLeft = paddingRight;
                        paddingRight += child.getWidth();
                    } else if (hgrav != 5) {
                        childLeft = paddingRight;
                    } else {
                        childLeft = (width - paddingRight2) - child.getMeasuredWidth();
                        paddingRight2 += child.getMeasuredWidth();
                    }
                    int childOffset = (childLeft + scrollX) - child.getLeft();
                    if (childOffset != 0) {
                        child.offsetLeftAndRight(childOffset);
                    }
                }
            }
        }
        if (this.mOnPageChangeListener != null) {
            this.mOnPageChangeListener.onPageScrolled(i, f, i2);
        }
        if (this.mInternalPageChangeListener != null) {
            this.mInternalPageChangeListener.onPageScrolled(i, f, i2);
        }
        if (this.mPageTransformer != null) {
            scrollX = getScrollX();
            childLeft = getChildCount();
            int i3 = 0;
            while (true) {
                paddingLeft = i3;
                if (paddingLeft >= childLeft) {
                    break;
                }
                View child2 = getChildAt(paddingLeft);
                if (!((LayoutParams) child2.getLayoutParams()).isDecor) {
                    this.mPageTransformer.transformPage(child2, ((float) (child2.getLeft() - scrollX)) / ((float) getPaddedWidth()));
                }
                i3 = paddingLeft + 1;
            }
        }
        this.mCalledSuper = true;
    }

    private void completeScroll(boolean postEvents) {
        boolean needPopulate = this.mScrollState == 2;
        if (needPopulate) {
            setScrollingCacheEnabled(false);
            this.mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            if (!(oldX == x && oldY == y)) {
                scrollTo(x, y);
            }
        }
        this.mPopulatePending = false;
        boolean needPopulate2 = needPopulate;
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (ii.scrolling) {
                needPopulate2 = true;
                ii.scrolling = false;
            }
        }
        if (!needPopulate2) {
            return;
        }
        if (postEvents) {
            postOnAnimation(this.mEndScrollRunnable);
        } else {
            this.mEndScrollRunnable.run();
        }
    }

    private boolean isGutterDrag(float x, float dx) {
        return (x < ((float) this.mGutterSize) && dx > 0.0f) || (x > ((float) (getWidth() - this.mGutterSize)) && dx < 0.0f);
    }

    private void enableLayers(boolean enable) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setLayerType(enable ? 2 : 0, null);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MotionEvent motionEvent = ev;
        int action = ev.getAction() & 255;
        if (action == 3 || action == 1) {
            this.mIsBeingDragged = false;
            this.mIsUnableToDrag = false;
            this.mActivePointerId = -1;
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
            return false;
        }
        if (action != 0) {
            if (this.mIsBeingDragged) {
                return true;
            }
            if (this.mIsUnableToDrag) {
                return false;
            }
        }
        float x;
        if (action == 0) {
            x = ev.getX();
            this.mInitialMotionX = x;
            this.mLastMotionX = x;
            x = ev.getY();
            this.mInitialMotionY = x;
            this.mLastMotionY = x;
            this.mActivePointerId = motionEvent.getPointerId(0);
            this.mIsUnableToDrag = false;
            this.mScroller.computeScrollOffset();
            if (this.mScrollState != 2 || Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX()) <= this.mCloseEnough) {
                completeScroll(false);
                this.mIsBeingDragged = false;
            } else {
                this.mScroller.abortAnimation();
                this.mPopulatePending = false;
                populate();
                this.mIsBeingDragged = true;
                requestParentDisallowInterceptTouchEvent(true);
                setScrollState(1);
            }
        } else if (action == 2) {
            int activePointerId = this.mActivePointerId;
            if (activePointerId != -1) {
                float y;
                int pointerIndex = motionEvent.findPointerIndex(activePointerId);
                float x2 = motionEvent.getX(pointerIndex);
                float dx = x2 - this.mLastMotionX;
                float xDiff = Math.abs(dx);
                float y2 = motionEvent.getY(pointerIndex);
                float yDiff = Math.abs(y2 - this.mInitialMotionY);
                if (dx == 0.0f || isGutterDrag(this.mLastMotionX, dx)) {
                    y = y2;
                } else {
                    y = y2;
                    if (canScroll(this, false, (int) dx, (int) x2, (int) y2)) {
                        this.mLastMotionX = x2;
                        this.mLastMotionY = y;
                        this.mIsUnableToDrag = true;
                        return false;
                    }
                }
                if (xDiff > ((float) this.mTouchSlop) && 0.5f * xDiff > yDiff) {
                    this.mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                    setScrollState(1);
                    if (dx > 0.0f) {
                        x = this.mInitialMotionX + ((float) this.mTouchSlop);
                    } else {
                        x = this.mInitialMotionX - ((float) this.mTouchSlop);
                    }
                    this.mLastMotionX = x;
                    this.mLastMotionY = y;
                    setScrollingCacheEnabled(true);
                } else if (yDiff > ((float) this.mTouchSlop)) {
                    this.mIsUnableToDrag = true;
                }
                if (this.mIsBeingDragged && performDrag(x2)) {
                    postInvalidateOnAnimation();
                }
            }
        } else if (action == 6) {
            onSecondaryPointerUp(ev);
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        MotionEvent motionEvent = ev;
        if ((ev.getAction() == 0 && ev.getEdgeFlags() != 0) || this.mAdapter == null || this.mAdapter.getCount() == 0) {
            return false;
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        boolean needsInvalidate = false;
        float x;
        float scrolledPages;
        int pointerIndex;
        switch (ev.getAction() & 255) {
            case 0:
                this.mScroller.abortAnimation();
                this.mPopulatePending = false;
                populate();
                x = ev.getX();
                this.mInitialMotionX = x;
                this.mLastMotionX = x;
                x = ev.getY();
                this.mInitialMotionY = x;
                this.mLastMotionY = x;
                this.mActivePointerId = motionEvent.getPointerId(0);
                break;
            case 1:
                if (this.mIsBeingDragged) {
                    float nextPageOffset;
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
                    this.mPopulatePending = true;
                    scrolledPages = ((float) getScrollStart()) / ((float) getPaddedWidth());
                    ItemInfo ii = infoForFirstVisiblePage();
                    int currentPage = ii.position;
                    if (ViewUtils.isLayoutRtl(this)) {
                        nextPageOffset = (ii.offset - scrolledPages) / ii.widthFactor;
                    } else {
                        nextPageOffset = (scrolledPages - ii.offset) / ii.widthFactor;
                    }
                    setCurrentItemInternal(determineTargetPage(currentPage, nextPageOffset, initialVelocity, (int) (motionEvent.getX(motionEvent.findPointerIndex(this.mActivePointerId)) - this.mInitialMotionX)), true, true, initialVelocity);
                    this.mActivePointerId = -1;
                    endDrag();
                    this.mLeftEdge.onRelease();
                    this.mRightEdge.onRelease();
                    needsInvalidate = true;
                    break;
                }
                break;
            case 2:
                if (!this.mIsBeingDragged) {
                    pointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                    x = motionEvent.getX(pointerIndex);
                    float xDiff = Math.abs(x - this.mLastMotionX);
                    float y = motionEvent.getY(pointerIndex);
                    scrolledPages = Math.abs(y - this.mLastMotionY);
                    if (xDiff > ((float) this.mTouchSlop) && xDiff > scrolledPages) {
                        float f;
                        this.mIsBeingDragged = true;
                        requestParentDisallowInterceptTouchEvent(true);
                        if (x - this.mInitialMotionX > 0.0f) {
                            f = this.mInitialMotionX + ((float) this.mTouchSlop);
                        } else {
                            f = this.mInitialMotionX - ((float) this.mTouchSlop);
                        }
                        this.mLastMotionX = f;
                        this.mLastMotionY = y;
                        setScrollState(1);
                        setScrollingCacheEnabled(true);
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }
                if (this.mIsBeingDragged) {
                    needsInvalidate = false | performDrag(motionEvent.getX(motionEvent.findPointerIndex(this.mActivePointerId)));
                    break;
                }
                break;
            case 3:
                if (this.mIsBeingDragged) {
                    scrollToItem(this.mCurItem, true, 0, false);
                    this.mActivePointerId = -1;
                    endDrag();
                    this.mLeftEdge.onRelease();
                    this.mRightEdge.onRelease();
                    needsInvalidate = true;
                    break;
                }
                break;
            case 5:
                pointerIndex = ev.getActionIndex();
                this.mLastMotionX = motionEvent.getX(pointerIndex);
                this.mActivePointerId = motionEvent.getPointerId(pointerIndex);
                break;
            case 6:
                onSecondaryPointerUp(ev);
                this.mLastMotionX = motionEvent.getX(motionEvent.findPointerIndex(this.mActivePointerId));
                break;
        }
        if (needsInvalidate) {
            postInvalidateOnAnimation();
        }
        return true;
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private boolean performDrag(float x) {
        EdgeEffect startEdge;
        EdgeEffect endEdge;
        float scrollStart;
        float startBound;
        float endBound;
        float targetScrollX;
        float f = x;
        boolean needsInvalidate = false;
        int width = getPaddedWidth();
        float deltaX = this.mLastMotionX - f;
        this.mLastMotionX = f;
        if (ViewUtils.isLayoutRtl(this)) {
            startEdge = this.mRightEdge;
            endEdge = this.mLeftEdge;
        } else {
            startEdge = this.mLeftEdge;
            endEdge = this.mRightEdge;
        }
        float nextScrollX = ((float) getScrollX()) + deltaX;
        if (ViewUtils.isLayoutRtl(this)) {
            scrollStart = 1.6777216E7f - nextScrollX;
        } else {
            scrollStart = nextScrollX;
        }
        ItemInfo startItem = (ItemInfo) this.mItems.get(0);
        boolean z = true;
        boolean startAbsolute = startItem.position == 0;
        if (startAbsolute) {
            startBound = startItem.offset * ((float) width);
        } else {
            startBound = ((float) width) * this.mFirstOffset;
        }
        ItemInfo endItem = (ItemInfo) this.mItems.get(this.mItems.size() - 1);
        if (endItem.position != this.mAdapter.getCount() - 1) {
            z = false;
        }
        boolean endAbsolute = z;
        if (endAbsolute) {
            endBound = endItem.offset * ((float) width);
        } else {
            endBound = ((float) width) * this.mLastOffset;
        }
        if (scrollStart < startBound) {
            if (startAbsolute) {
                startEdge.onPull(Math.abs(startBound - scrollStart) / ((float) width));
                needsInvalidate = true;
            }
            f = startBound;
        } else if (scrollStart > endBound) {
            if (endAbsolute) {
                f = scrollStart - endBound;
                endEdge.onPull(Math.abs(f) / ((float) width));
                needsInvalidate = true;
            }
            f = endBound;
        } else {
            f = scrollStart;
        }
        if (ViewUtils.isLayoutRtl(this)) {
            targetScrollX = 1.6777216E7f - f;
        } else {
            targetScrollX = f;
        }
        this.mLastMotionX += targetScrollX - ((float) ((int) targetScrollX));
        scrollTo((int) targetScrollX, getScrollY());
        pageScrolled((int) targetScrollX);
        return needsInvalidate;
    }

    private ItemInfo infoForFirstVisiblePage() {
        int startOffset = getScrollStart();
        int width = getPaddedWidth();
        float marginOffset = 0.0f;
        float scrollOffset = width > 0 ? ((float) startOffset) / ((float) width) : 0.0f;
        if (width > 0) {
            marginOffset = ((float) this.mPageMargin) / ((float) width);
        }
        int lastPos = -1;
        float lastOffset = 0.0f;
        float lastWidth = 0.0f;
        boolean first = true;
        ItemInfo lastItem = null;
        int N = this.mItems.size();
        int i = 0;
        while (i < N) {
            ItemInfo ii = (ItemInfo) this.mItems.get(i);
            if (!(first || ii.position == lastPos + 1)) {
                ii = this.mTempItem;
                ii.offset = (lastOffset + lastWidth) + marginOffset;
                ii.position = lastPos + 1;
                ii.widthFactor = this.mAdapter.getPageWidth(ii.position);
                i--;
            }
            float offset = ii.offset;
            float startBound = offset;
            if (!first && scrollOffset < startBound) {
                return lastItem;
            }
            if (scrollOffset >= (ii.widthFactor + offset) + marginOffset) {
                int startOffset2 = startOffset;
                if (i != this.mItems.size() - 1) {
                    first = false;
                    lastPos = ii.position;
                    lastOffset = offset;
                    lastWidth = ii.widthFactor;
                    lastItem = ii;
                    i++;
                    startOffset = startOffset2;
                }
            }
            return ii;
        }
        return lastItem;
    }

    private int getScrollStart() {
        if (ViewUtils.isLayoutRtl(this)) {
            return 16777216 - getScrollX();
        }
        return getScrollX();
    }

    private int determineTargetPage(int currentPage, float pageOffset, int velocity, int deltaX) {
        int targetPage;
        if (Math.abs(deltaX) <= this.mFlingDistance || Math.abs(velocity) <= this.mMinimumVelocity) {
            targetPage = (int) (((float) currentPage) - (((float) this.mLeftIncr) * (pageOffset + (currentPage >= this.mCurItem ? 0.4f : 0.6f))));
        } else {
            targetPage = currentPage - (velocity < 0 ? this.mLeftIncr : 0);
        }
        if (this.mItems.size() <= 0) {
            return targetPage;
        }
        return MathUtils.constrain(targetPage, ((ItemInfo) this.mItems.get(0)).position, ((ItemInfo) this.mItems.get(this.mItems.size() - 1)).position);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        boolean needsInvalidate = false;
        int overScrollMode = getOverScrollMode();
        if (overScrollMode == 0 || (overScrollMode == 1 && this.mAdapter != null && this.mAdapter.getCount() > 1)) {
            int restoreCount;
            int height;
            int width;
            if (!this.mLeftEdge.isFinished()) {
                restoreCount = canvas.save();
                height = (getHeight() - getPaddingTop()) - getPaddingBottom();
                width = getWidth();
                canvas.rotate(270.0f);
                canvas.translate((float) ((-height) + getPaddingTop()), this.mFirstOffset * ((float) width));
                this.mLeftEdge.setSize(height, width);
                needsInvalidate = false | this.mLeftEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (!this.mRightEdge.isFinished()) {
                restoreCount = canvas.save();
                height = getWidth();
                width = (getHeight() - getPaddingTop()) - getPaddingBottom();
                canvas.rotate(90.0f);
                canvas.translate((float) (-getPaddingTop()), (-(this.mLastOffset + 1.0f)) * ((float) height));
                this.mRightEdge.setSize(width, height);
                needsInvalidate |= this.mRightEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
        } else {
            this.mLeftEdge.finish();
            this.mRightEdge.finish();
        }
        if (needsInvalidate) {
            postInvalidateOnAnimation();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onDraw(Canvas canvas) {
        Canvas ii;
        super.onDraw(canvas);
        if (this.mPageMargin > 0 && this.mMarginDrawable != null && this.mItems.size() > 0 && this.mAdapter != null) {
            int scrollX = getScrollX();
            int width = getWidth();
            float marginOffset = ((float) this.mPageMargin) / ((float) width);
            ItemInfo ii2 = (ItemInfo) this.mItems.get(0);
            float offset = ii2.offset;
            int itemCount = this.mItems.size();
            int firstPos = ii2.position;
            int lastPos = ((ItemInfo) this.mItems.get(itemCount - 1)).position;
            float offset2 = offset;
            int itemIndex = 0;
            int pos = firstPos;
            while (pos < lastPos) {
                float itemOffset;
                float widthFactor;
                float left;
                ItemInfo ii3;
                int itemIndex2;
                int itemCount2;
                while (pos > ii2.position && itemIndex < itemCount) {
                    itemIndex++;
                    ii2 = (ItemInfo) this.mItems.get(itemIndex);
                }
                if (pos == ii2.position) {
                    itemOffset = ii2.offset;
                    widthFactor = ii2.widthFactor;
                } else {
                    itemOffset = offset2;
                    widthFactor = this.mAdapter.getPageWidth(pos);
                }
                float scaledOffset = ((float) width) * itemOffset;
                if (ViewUtils.isLayoutRtl(this)) {
                    left = 1.6777216E7f - scaledOffset;
                } else {
                    left = (((float) width) * widthFactor) + scaledOffset;
                }
                offset2 = (itemOffset + widthFactor) + marginOffset;
                float marginOffset2 = marginOffset;
                if (((float) this.mPageMargin) + left > ((float) scrollX)) {
                    ii3 = ii2;
                    itemIndex2 = itemIndex;
                    itemCount2 = itemCount;
                    this.mMarginDrawable.setBounds((int) left, this.mTopPageBounds, (int) ((((float) this.mPageMargin) + left) + 0.5f), this.mBottomPageBounds);
                    this.mMarginDrawable.draw(canvas);
                } else {
                    ii3 = ii2;
                    itemIndex2 = itemIndex;
                    itemCount2 = itemCount;
                    ii = canvas;
                }
                if (left <= ((float) (scrollX + width))) {
                    pos++;
                    marginOffset = marginOffset2;
                    ii2 = ii3;
                    itemIndex = itemIndex2;
                    itemCount = itemCount2;
                } else {
                    return;
                }
            }
        }
        ii = canvas;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mLastMotionX = ev.getX(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = false;
        this.mIsUnableToDrag = false;
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        if (this.mScrollingCacheEnabled != enabled) {
            this.mScrollingCacheEnabled = enabled;
        }
    }

    public boolean canScrollHorizontally(int direction) {
        boolean z = false;
        if (this.mAdapter == null) {
            return false;
        }
        int width = getPaddedWidth();
        int scrollX = getScrollX();
        if (direction < 0) {
            if (scrollX > ((int) (((float) width) * this.mFirstOffset))) {
                z = true;
            }
            return z;
        } else if (direction <= 0) {
            return false;
        } else {
            if (scrollX < ((int) (((float) width) * this.mLastOffset))) {
                z = true;
            }
            return z;
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Missing block: B:18:0x0065, code skipped:
            if (r0.canScrollHorizontally(-r17) != false) goto L_0x006b;
     */
    public boolean canScroll(android.view.View r15, boolean r16, int r17, int r18, int r19) {
        /*
        r14 = this;
        r0 = r15;
        r1 = r0 instanceof android.view.ViewGroup;
        r2 = 1;
        if (r1 == 0) goto L_0x005c;
    L_0x0006:
        r1 = r0;
        r1 = (android.view.ViewGroup) r1;
        r3 = r0.getScrollX();
        r4 = r0.getScrollY();
        r5 = r1.getChildCount();
        r6 = r5 + -1;
    L_0x0017:
        if (r6 < 0) goto L_0x005c;
    L_0x0019:
        r13 = r1.getChildAt(r6);
        r7 = r18 + r3;
        r8 = r13.getLeft();
        if (r7 < r8) goto L_0x0059;
    L_0x0025:
        r7 = r18 + r3;
        r8 = r13.getRight();
        if (r7 >= r8) goto L_0x0059;
    L_0x002d:
        r7 = r19 + r4;
        r8 = r13.getTop();
        if (r7 < r8) goto L_0x0059;
    L_0x0035:
        r7 = r19 + r4;
        r8 = r13.getBottom();
        if (r7 >= r8) goto L_0x0059;
    L_0x003d:
        r9 = 1;
        r7 = r18 + r3;
        r8 = r13.getLeft();
        r11 = r7 - r8;
        r7 = r19 + r4;
        r8 = r13.getTop();
        r12 = r7 - r8;
        r7 = r14;
        r8 = r13;
        r10 = r17;
        r7 = r7.canScroll(r8, r9, r10, r11, r12);
        if (r7 == 0) goto L_0x0059;
    L_0x0058:
        return r2;
    L_0x0059:
        r6 = r6 + -1;
        goto L_0x0017;
    L_0x005c:
        if (r16 == 0) goto L_0x0068;
    L_0x005e:
        r3 = r17;
        r4 = -r3;
        r4 = r0.canScrollHorizontally(r4);
        if (r4 == 0) goto L_0x006a;
    L_0x0067:
        goto L_0x006b;
    L_0x0068:
        r3 = r17;
    L_0x006a:
        r2 = 0;
    L_0x006b:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.widget.ViewPager.canScroll(android.view.View, boolean, int, int, int):boolean");
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    public boolean executeKeyEvent(KeyEvent event) {
        if (event.getAction() != 0) {
            return false;
        }
        int keyCode = event.getKeyCode();
        if (keyCode != 61) {
            switch (keyCode) {
                case 21:
                    return arrowScroll(17);
                case 22:
                    return arrowScroll(66);
                default:
                    return false;
            }
        } else if (event.hasNoModifiers()) {
            return arrowScroll(2);
        } else {
            if (event.hasModifiers(1)) {
                return arrowScroll(1);
            }
            return false;
        }
    }

    public boolean arrowScroll(int direction) {
        boolean isChild;
        View currentFocused = findFocus();
        if (currentFocused == this) {
            currentFocused = null;
        } else if (currentFocused != null) {
            isChild = false;
            for (ViewPager parent = currentFocused.getParent(); parent instanceof ViewGroup; parent = parent.getParent()) {
                if (parent == this) {
                    isChild = true;
                    break;
                }
            }
            if (!isChild) {
                StringBuilder sb = new StringBuilder();
                sb.append(currentFocused.getClass().getSimpleName());
                for (ViewParent parent2 = currentFocused.getParent(); parent2 instanceof ViewGroup; parent2 = parent2.getParent()) {
                    sb.append(" => ");
                    sb.append(parent2.getClass().getSimpleName());
                }
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("arrowScroll tried to find focus based on non-child current focused view ");
                stringBuilder.append(sb.toString());
                Log.e(str, stringBuilder.toString());
                currentFocused = null;
            }
        }
        isChild = false;
        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
        if (nextFocused == null || nextFocused == currentFocused) {
            if (direction == 17 || direction == 1) {
                isChild = pageLeft();
            } else if (direction == 66 || direction == 2) {
                isChild = pageRight();
            }
        } else if (direction == 17) {
            isChild = (currentFocused == null || getChildRectInPagerCoordinates(this.mTempRect, nextFocused).left < getChildRectInPagerCoordinates(this.mTempRect, currentFocused).left) ? nextFocused.requestFocus() : pageLeft();
        } else if (direction == 66) {
            isChild = (currentFocused == null || getChildRectInPagerCoordinates(this.mTempRect, nextFocused).left > getChildRectInPagerCoordinates(this.mTempRect, currentFocused).left) ? nextFocused.requestFocus() : pageRight();
        }
        if (isChild) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
        }
        return isChild;
    }

    private Rect getChildRectInPagerCoordinates(Rect outRect, View child) {
        if (outRect == null) {
            outRect = new Rect();
        }
        if (child == null) {
            outRect.set(0, 0, 0, 0);
            return outRect;
        }
        outRect.left = child.getLeft();
        outRect.right = child.getRight();
        outRect.top = child.getTop();
        outRect.bottom = child.getBottom();
        ViewGroup parent = child.getParent();
        while ((parent instanceof ViewGroup) && parent != this) {
            ViewGroup group = parent;
            outRect.left += group.getLeft();
            outRect.right += group.getRight();
            outRect.top += group.getTop();
            outRect.bottom += group.getBottom();
            parent = group.getParent();
        }
        return outRect;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean pageLeft() {
        return setCurrentItemInternal(this.mCurItem + this.mLeftIncr, true, false);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean pageRight() {
        return setCurrentItemInternal(this.mCurItem - this.mLeftIncr, true, false);
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        if (layoutDirection == 0) {
            this.mLeftIncr = -1;
        } else {
            this.mLeftIncr = 1;
        }
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        int focusableCount = views.size();
        int descendantFocusability = getDescendantFocusability();
        if (descendantFocusability != 393216) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == 0) {
                    ItemInfo ii = infoForChild(child);
                    if (ii != null && ii.position == this.mCurItem) {
                        child.addFocusables(views, direction, focusableMode);
                    }
                }
            }
        }
        if ((descendantFocusability == 262144 && focusableCount != views.size()) || !isFocusable()) {
            return;
        }
        if (!(((focusableMode & 1) == 1 && isInTouchMode() && !isFocusableInTouchMode()) || views == null)) {
            views.add(this);
        }
    }

    public void addTouchables(ArrayList<View> views) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                ItemInfo ii = infoForChild(child);
                if (ii != null && ii.position == this.mCurItem) {
                    child.addTouchables(views);
                }
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int index;
        int increment;
        int end;
        int count = getChildCount();
        if ((direction & 2) != 0) {
            index = 0;
            increment = 1;
            end = count;
        } else {
            index = count - 1;
            increment = -1;
            end = -1;
        }
        for (int i = index; i != end; i += increment) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0) {
                ItemInfo ii = infoForChild(child);
                if (ii != null && ii.position == this.mCurItem && child.requestFocus(direction, previouslyFocusedRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* Access modifiers changed, original: protected */
    public android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    /* Access modifiers changed, original: protected */
    public android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return generateDefaultLayoutParams();
    }

    /* Access modifiers changed, original: protected */
    public boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return (p instanceof LayoutParams) && super.checkLayoutParams(p);
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(ViewPager.class.getName());
        event.setScrollable(canScroll());
        if (event.getEventType() == 4096 && this.mAdapter != null) {
            event.setItemCount(this.mAdapter.getCount());
            event.setFromIndex(this.mCurItem);
            event.setToIndex(this.mCurItem);
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(ViewPager.class.getName());
        info.setScrollable(canScroll());
        if (canScrollHorizontally(1)) {
            info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD);
            if (VERSION.SDK_INT >= 23) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_RIGHT);
            }
        }
        if (canScrollHorizontally(-1)) {
            info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD);
            if (VERSION.SDK_INT >= 23) {
                info.addAction(AccessibilityAction.ACTION_SCROLL_LEFT);
            }
        }
    }

    public boolean performAccessibilityAction(int action, Bundle args) {
        if (super.performAccessibilityAction(action, args)) {
            return true;
        }
        if (action != 4096) {
            if (action == 8192 || action == 16908345) {
                if (!canScrollHorizontally(-1)) {
                    return false;
                }
                setCurrentItem(this.mCurItem - 1);
                return true;
            } else if (action != 16908347) {
                return false;
            }
        }
        if (!canScrollHorizontally(1)) {
            return false;
        }
        setCurrentItem(this.mCurItem + 1);
        return true;
    }

    private boolean canScroll() {
        return this.mAdapter != null && this.mAdapter.getCount() > 1;
    }
}
