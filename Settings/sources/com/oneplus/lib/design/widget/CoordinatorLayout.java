package com.oneplus.lib.design.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.util.MathUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoordinatorLayout extends ViewGroup implements NestedScrollingParent {
    static final Class<?>[] CONSTRUCTOR_PARAMS = new Class[]{Context.class, AttributeSet.class};
    static final int EVENT_NESTED_SCROLL = 1;
    static final int EVENT_PRE_DRAW = 0;
    static final int EVENT_VIEW_REMOVED = 2;
    static final String TAG = "CoordinatorLayout";
    static final Comparator<View> TOP_SORTED_CHILDREN_COMPARATOR;
    private static final int TYPE_ON_INTERCEPT = 0;
    private static final int TYPE_ON_TOUCH = 1;
    static final String WIDGET_PACKAGE_NAME;
    static final ThreadLocal<Map<String, Constructor<Behavior>>> sConstructors = new ThreadLocal();
    private OnApplyWindowInsetsListener mApplyWindowInsetsListener;
    private View mBehaviorTouchView;
    private final DirectedAcyclicGraph<View> mChildDag;
    private final List<View> mDependencySortedChildren;
    private boolean mDisallowInterceptReset;
    private boolean mDrawStatusBarBackground;
    private boolean mIsAttachedToWindow;
    private int[] mKeylines;
    private WindowInsetsCompat mLastInsets;
    private boolean mNeedsPreDrawListener;
    private View mNestedScrollingDirectChild;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private View mNestedScrollingTarget;
    OnHierarchyChangeListener mOnHierarchyChangeListener;
    private OnPreDrawListener mOnPreDrawListener;
    private Paint mScrimPaint;
    private Drawable mStatusBarBackground;
    private final List<View> mTempDependenciesList;
    private final int[] mTempIntPair;
    private final List<View> mTempList1;
    private final Rect mTempRect1;
    private final Rect mTempRect2;
    private final Rect mTempRect3;
    private final Rect mTempRect4;
    private final Rect mTempRect5;

    public static abstract class Behavior<V extends View> {
        public Behavior(Context context, AttributeSet attrs) {
        }

        public void onAttachedToLayoutParams(@NonNull LayoutParams params) {
        }

        public void onDetachedFromLayoutParams() {
        }

        public boolean onInterceptTouchEvent(CoordinatorLayout parent, V v, MotionEvent ev) {
            return false;
        }

        public boolean onTouchEvent(CoordinatorLayout parent, V v, MotionEvent ev) {
            return false;
        }

        @ColorInt
        public int getScrimColor(CoordinatorLayout parent, V v) {
            return ViewCompat.MEASURED_STATE_MASK;
        }

        @FloatRange(from = 0.0d, to = 1.0d)
        public float getScrimOpacity(CoordinatorLayout parent, V v) {
            return 0.0f;
        }

        public boolean blocksInteractionBelow(CoordinatorLayout parent, V child) {
            return getScrimOpacity(parent, child) > 0.0f;
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, V v, View dependency) {
            return false;
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, V v, View dependency) {
            return false;
        }

        public void onDependentViewRemoved(CoordinatorLayout parent, V v, View dependency) {
        }

        @Deprecated
        public boolean isDirty(CoordinatorLayout parent, V v) {
            return false;
        }

        public boolean onMeasureChild(CoordinatorLayout parent, V v, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            return false;
        }

        public boolean onLayoutChild(CoordinatorLayout parent, V v, int layoutDirection) {
            return false;
        }

        public static void setTag(View child, Object tag) {
            ((LayoutParams) child.getLayoutParams()).mBehaviorTag = tag;
        }

        public static Object getTag(View child) {
            return ((LayoutParams) child.getLayoutParams()).mBehaviorTag;
        }

        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V v, View directTargetChild, View target, int nestedScrollAxes) {
            return false;
        }

        public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, V v, View directTargetChild, View target, int nestedScrollAxes) {
        }

        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V v, View target) {
        }

        public void onNestedScroll(CoordinatorLayout coordinatorLayout, V v, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        }

        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V v, View target, int dx, int dy, int[] consumed) {
        }

        public boolean onNestedFling(CoordinatorLayout coordinatorLayout, V v, View target, float velocityX, float velocityY, boolean consumed) {
            return false;
        }

        public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V v, View target, float velocityX, float velocityY) {
            return false;
        }

        @NonNull
        public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, V v, WindowInsetsCompat insets) {
            return insets;
        }

        public boolean onRequestChildRectangleOnScreen(CoordinatorLayout coordinatorLayout, V v, Rect rectangle, boolean immediate) {
            return false;
        }

        public void onRestoreInstanceState(CoordinatorLayout parent, V v, Parcelable state) {
        }

        public Parcelable onSaveInstanceState(CoordinatorLayout parent, V v) {
            return BaseSavedState.EMPTY_STATE;
        }

        public boolean getInsetDodgeRect(@NonNull CoordinatorLayout parent, @NonNull V v, @NonNull Rect rect) {
            return false;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultBehavior {
        Class<? extends Behavior> value();
    }

    @RestrictTo({Scope.GROUP_ID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DispatchChangeEvent {
    }

    private class HierarchyChangeListener implements OnHierarchyChangeListener {
        HierarchyChangeListener() {
        }

        public void onChildViewAdded(View parent, View child) {
            if (CoordinatorLayout.this.mOnHierarchyChangeListener != null) {
                CoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        public void onChildViewRemoved(View parent, View child) {
            CoordinatorLayout.this.onChildViewsChanged(2);
            if (CoordinatorLayout.this.mOnHierarchyChangeListener != null) {
                CoordinatorLayout.this.mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        public int anchorGravity = 0;
        public int dodgeInsetEdges = 0;
        public int gravity = 0;
        public int insetEdge = 0;
        public int keyline = -1;
        View mAnchorDirectChild;
        int mAnchorId = -1;
        View mAnchorView;
        Behavior mBehavior;
        boolean mBehaviorResolved = false;
        Object mBehaviorTag;
        private boolean mDidAcceptNestedScroll;
        private boolean mDidBlockInteraction;
        private boolean mDidChangeAfterNestedScroll;
        int mInsetOffsetX;
        int mInsetOffsetY;
        final Rect mLastChildRect = new Rect();

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OpCoordinatorLayout_Layout);
            this.gravity = a.getInteger(R.styleable.OpCoordinatorLayout_Layout_android_layout_gravity, 0);
            this.mAnchorId = a.getResourceId(R.styleable.OpCoordinatorLayout_Layout_op_layout_anchor, -1);
            this.anchorGravity = a.getInteger(R.styleable.OpCoordinatorLayout_Layout_op_layout_anchorGravity, 0);
            this.keyline = a.getInteger(R.styleable.OpCoordinatorLayout_Layout_op_layout_keyline, -1);
            this.insetEdge = a.getInt(R.styleable.OpCoordinatorLayout_Layout_op_layout_insetEdge, 0);
            this.dodgeInsetEdges = a.getInt(R.styleable.OpCoordinatorLayout_Layout_op_layout_dodgeInsetEdges, 0);
            this.mBehaviorResolved = a.hasValue(R.styleable.OpCoordinatorLayout_Layout_op_layout_behavior);
            if (this.mBehaviorResolved) {
                this.mBehavior = CoordinatorLayout.parseBehavior(context, attrs, a.getString(R.styleable.OpCoordinatorLayout_Layout_op_layout_behavior));
            }
            a.recycle();
            if (this.mBehavior != null) {
                this.mBehavior.onAttachedToLayoutParams(this);
            }
        }

        public LayoutParams(LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams p) {
            super(p);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
        }

        @IdRes
        public int getAnchorId() {
            return this.mAnchorId;
        }

        public void setAnchorId(@IdRes int id) {
            invalidateAnchor();
            this.mAnchorId = id;
        }

        @Nullable
        public Behavior getBehavior() {
            return this.mBehavior;
        }

        public void setBehavior(@Nullable Behavior behavior) {
            if (this.mBehavior != behavior) {
                if (this.mBehavior != null) {
                    this.mBehavior.onDetachedFromLayoutParams();
                }
                this.mBehavior = behavior;
                this.mBehaviorTag = null;
                this.mBehaviorResolved = true;
                if (behavior != null) {
                    behavior.onAttachedToLayoutParams(this);
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void setLastChildRect(Rect r) {
            this.mLastChildRect.set(r);
        }

        /* Access modifiers changed, original: 0000 */
        public Rect getLastChildRect() {
            return this.mLastChildRect;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean checkAnchorChanged() {
            return this.mAnchorView == null && this.mAnchorId != -1;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean didBlockInteraction() {
            if (this.mBehavior == null) {
                this.mDidBlockInteraction = false;
            }
            return this.mDidBlockInteraction;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean isBlockingInteractionBelow(CoordinatorLayout parent, View child) {
            if (this.mDidBlockInteraction) {
                return true;
            }
            int blocksInteractionBelow;
            boolean z = this.mDidBlockInteraction;
            if (this.mBehavior != null) {
                blocksInteractionBelow = this.mBehavior.blocksInteractionBelow(parent, child);
            } else {
                blocksInteractionBelow = 0;
            }
            int i = z | blocksInteractionBelow;
            this.mDidBlockInteraction = i;
            return i;
        }

        /* Access modifiers changed, original: 0000 */
        public void resetTouchBehaviorTracking() {
            this.mDidBlockInteraction = false;
        }

        /* Access modifiers changed, original: 0000 */
        public void resetNestedScroll() {
            this.mDidAcceptNestedScroll = false;
        }

        /* Access modifiers changed, original: 0000 */
        public void acceptNestedScroll(boolean accept) {
            this.mDidAcceptNestedScroll = accept;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean isNestedScrollAccepted() {
            return this.mDidAcceptNestedScroll;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean getChangedAfterNestedScroll() {
            return this.mDidChangeAfterNestedScroll;
        }

        /* Access modifiers changed, original: 0000 */
        public void setChangedAfterNestedScroll(boolean changed) {
            this.mDidChangeAfterNestedScroll = changed;
        }

        /* Access modifiers changed, original: 0000 */
        public void resetChangedAfterNestedScroll() {
            this.mDidChangeAfterNestedScroll = false;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean dependsOn(CoordinatorLayout parent, View child, View dependency) {
            return dependency == this.mAnchorDirectChild || shouldDodge(dependency, ViewCompat.getLayoutDirection(parent)) || (this.mBehavior != null && this.mBehavior.layoutDependsOn(parent, child, dependency));
        }

        /* Access modifiers changed, original: 0000 */
        public void invalidateAnchor() {
            this.mAnchorDirectChild = null;
            this.mAnchorView = null;
        }

        /* Access modifiers changed, original: 0000 */
        public View findAnchorView(CoordinatorLayout parent, View forChild) {
            if (this.mAnchorId == -1) {
                this.mAnchorDirectChild = null;
                this.mAnchorView = null;
                return null;
            }
            if (this.mAnchorView == null || !verifyAnchorView(forChild, parent)) {
                resolveAnchorView(forChild, parent);
            }
            return this.mAnchorView;
        }

        private void resolveAnchorView(View forChild, CoordinatorLayout parent) {
            this.mAnchorView = parent.findViewById(this.mAnchorId);
            if (this.mAnchorView != null) {
                if (this.mAnchorView != parent) {
                    View directChild = this.mAnchorView;
                    View p = this.mAnchorView.getParent();
                    while (p != parent && p != null) {
                        if (p != forChild) {
                            if (p instanceof View) {
                                directChild = p;
                            }
                            p = p.getParent();
                        } else if (parent.isInEditMode()) {
                            this.mAnchorDirectChild = null;
                            this.mAnchorView = null;
                            return;
                        } else {
                            throw new IllegalStateException("Anchor must not be a descendant of the anchored view");
                        }
                    }
                    this.mAnchorDirectChild = directChild;
                } else if (parent.isInEditMode()) {
                    this.mAnchorDirectChild = null;
                    this.mAnchorView = null;
                } else {
                    throw new IllegalStateException("View can not be anchored to the the parent CoordinatorLayout");
                }
            } else if (parent.isInEditMode()) {
                this.mAnchorDirectChild = null;
                this.mAnchorView = null;
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Could not find CoordinatorLayout descendant view with id ");
                stringBuilder.append(parent.getResources().getResourceName(this.mAnchorId));
                stringBuilder.append(" to anchor view ");
                stringBuilder.append(forChild);
                throw new IllegalStateException(stringBuilder.toString());
            }
        }

        private boolean verifyAnchorView(View forChild, CoordinatorLayout parent) {
            if (this.mAnchorView.getId() != this.mAnchorId) {
                return false;
            }
            View directChild = this.mAnchorView;
            View p = this.mAnchorView.getParent();
            while (p != parent) {
                if (p == null || p == forChild) {
                    this.mAnchorDirectChild = null;
                    this.mAnchorView = null;
                    return false;
                }
                if (p instanceof View) {
                    directChild = p;
                }
                p = p.getParent();
            }
            this.mAnchorDirectChild = directChild;
            return true;
        }

        private boolean shouldDodge(View other, int layoutDirection) {
            int absInset = GravityCompat.getAbsoluteGravity(((LayoutParams) other.getLayoutParams()).insetEdge, layoutDirection);
            return absInset != 0 && (GravityCompat.getAbsoluteGravity(this.dodgeInsetEdges, layoutDirection) & absInset) == absInset;
        }
    }

    class OnPreDrawListener implements android.view.ViewTreeObserver.OnPreDrawListener {
        OnPreDrawListener() {
        }

        public boolean onPreDraw() {
            CoordinatorLayout.this.onChildViewsChanged(0);
            return true;
        }
    }

    static class ViewElevationComparator implements Comparator<View> {
        ViewElevationComparator() {
        }

        public int compare(View lhs, View rhs) {
            float lz = ViewCompat.getZ(lhs);
            float rz = ViewCompat.getZ(rhs);
            if (lz > rz) {
                return -1;
            }
            if (lz < rz) {
                return 1;
            }
            return 0;
        }
    }

    protected static class SavedState extends AbsSavedState {
        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        });
        SparseArray<Parcelable> behaviorStates;

        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            int size = source.readInt();
            int[] ids = new int[size];
            source.readIntArray(ids);
            Parcelable[] states = source.readParcelableArray(loader);
            this.behaviorStates = new SparseArray(size);
            for (int i = 0; i < size; i++) {
                this.behaviorStates.append(ids[i], states[i]);
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            int i = 0;
            int size = this.behaviorStates != null ? this.behaviorStates.size() : 0;
            dest.writeInt(size);
            int[] ids = new int[size];
            Parcelable[] states = new Parcelable[size];
            while (i < size) {
                ids[i] = this.behaviorStates.keyAt(i);
                states[i] = (Parcelable) this.behaviorStates.valueAt(i);
                i++;
            }
            dest.writeIntArray(ids);
            dest.writeParcelableArray(states, flags);
        }
    }

    static {
        Package pkg = CoordinatorLayout.class.getPackage();
        WIDGET_PACKAGE_NAME = pkg != null ? pkg.getName() : null;
        if (VERSION.SDK_INT >= 21) {
            TOP_SORTED_CHILDREN_COMPARATOR = new ViewElevationComparator();
        } else {
            TOP_SORTED_CHILDREN_COMPARATOR = null;
        }
    }

    public CoordinatorLayout(Context context) {
        this(context, null);
    }

    public CoordinatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mDependencySortedChildren = new ArrayList();
        this.mChildDag = new DirectedAcyclicGraph();
        this.mTempList1 = new ArrayList();
        this.mTempDependenciesList = new ArrayList();
        this.mTempRect1 = new Rect();
        this.mTempRect2 = new Rect();
        this.mTempRect3 = new Rect();
        this.mTempRect4 = new Rect();
        this.mTempRect5 = new Rect();
        this.mTempIntPair = new int[2];
        this.mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OpCoordinatorLayout, defStyleAttr, R.style.Widget_Design_CoordinatorLayout);
        int i = 0;
        int keylineArrayRes = a.getResourceId(R.styleable.OpCoordinatorLayout_opKeylines, 0);
        if (keylineArrayRes != 0) {
            Resources res = context.getResources();
            this.mKeylines = res.getIntArray(keylineArrayRes);
            float density = res.getDisplayMetrics().density;
            int count = this.mKeylines.length;
            while (i < count) {
                int[] iArr = this.mKeylines;
                iArr[i] = (int) (((float) iArr[i]) * density);
                i++;
            }
        }
        this.mStatusBarBackground = a.getDrawable(R.styleable.OpCoordinatorLayout_opStatusBarBackground);
        a.recycle();
        setupForInsets();
        super.setOnHierarchyChangeListener(new HierarchyChangeListener());
    }

    public void setOnHierarchyChangeListener(OnHierarchyChangeListener onHierarchyChangeListener) {
        this.mOnHierarchyChangeListener = onHierarchyChangeListener;
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        resetTouchBehaviors();
        if (this.mNeedsPreDrawListener) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new OnPreDrawListener();
            }
            getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }
        if (this.mLastInsets == null && ViewCompat.getFitsSystemWindows(this)) {
            ViewCompat.requestApplyInsets(this);
        }
        this.mIsAttachedToWindow = true;
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        resetTouchBehaviors();
        if (this.mNeedsPreDrawListener && this.mOnPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }
        if (this.mNestedScrollingTarget != null) {
            onStopNestedScroll(this.mNestedScrollingTarget);
        }
        this.mIsAttachedToWindow = false;
    }

    public void setStatusBarBackground(@Nullable Drawable bg) {
        if (this.mStatusBarBackground != bg) {
            Drawable drawable = null;
            if (this.mStatusBarBackground != null) {
                this.mStatusBarBackground.setCallback(null);
            }
            if (bg != null) {
                drawable = bg.mutate();
            }
            this.mStatusBarBackground = drawable;
            if (this.mStatusBarBackground != null) {
                if (this.mStatusBarBackground.isStateful()) {
                    this.mStatusBarBackground.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(this.mStatusBarBackground, ViewCompat.getLayoutDirection(this));
                this.mStatusBarBackground.setVisible(getVisibility() == 0, false);
                this.mStatusBarBackground.setCallback(this);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Nullable
    public Drawable getStatusBarBackground() {
        return this.mStatusBarBackground;
    }

    /* Access modifiers changed, original: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        int[] state = getDrawableState();
        boolean changed = false;
        Drawable d = this.mStatusBarBackground;
        if (d != null && d.isStateful()) {
            changed = false | d.setState(state);
        }
        if (changed) {
            invalidate();
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mStatusBarBackground;
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        boolean visible = visibility == 0;
        if (this.mStatusBarBackground != null && this.mStatusBarBackground.isVisible() != visible) {
            this.mStatusBarBackground.setVisible(visible, false);
        }
    }

    public void setStatusBarBackgroundResource(@DrawableRes int resId) {
        setStatusBarBackground(resId != 0 ? ContextCompat.getDrawable(getContext(), resId) : null);
    }

    public void setStatusBarBackgroundColor(@ColorInt int color) {
        setStatusBarBackground(new ColorDrawable(color));
    }

    /* Access modifiers changed, original: final */
    public final WindowInsetsCompat setWindowInsets(WindowInsetsCompat insets) {
        if (Utils.objectEquals(this.mLastInsets, insets)) {
            return insets;
        }
        this.mLastInsets = insets;
        boolean z = false;
        boolean z2 = insets != null && insets.getSystemWindowInsetTop() > 0;
        this.mDrawStatusBarBackground = z2;
        if (!this.mDrawStatusBarBackground && getBackground() == null) {
            z = true;
        }
        setWillNotDraw(z);
        insets = dispatchApplyWindowInsetsToBehaviors(insets);
        requestLayout();
        return insets;
    }

    /* Access modifiers changed, original: final */
    public final WindowInsetsCompat getLastWindowInsets() {
        return this.mLastInsets;
    }

    private void resetTouchBehaviors() {
        if (this.mBehaviorTouchView != null) {
            Behavior b = ((LayoutParams) this.mBehaviorTouchView.getLayoutParams()).getBehavior();
            if (b != null) {
                long now = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                b.onTouchEvent(this, this.mBehaviorTouchView, cancelEvent);
                cancelEvent.recycle();
            }
            this.mBehaviorTouchView = null;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((LayoutParams) getChildAt(i).getLayoutParams()).resetTouchBehaviorTracking();
        }
        this.mDisallowInterceptReset = false;
    }

    private void getTopSortedChildren(List<View> out) {
        out.clear();
        boolean useCustomOrder = isChildrenDrawingOrderEnabled();
        int childCount = getChildCount();
        int i = childCount - 1;
        while (i >= 0) {
            out.add(getChildAt(useCustomOrder ? getChildDrawingOrder(childCount, i) : i));
            i--;
        }
        if (TOP_SORTED_CHILDREN_COMPARATOR != null) {
            Collections.sort(out, TOP_SORTED_CHILDREN_COMPARATOR);
        }
    }

    private boolean performIntercept(MotionEvent ev, int type) {
        MotionEvent motionEvent = ev;
        int action = MotionEventCompat.getActionMasked(ev);
        List<View> topmostChildList = this.mTempList1;
        getTopSortedChildren(topmostChildList);
        int childCount = topmostChildList.size();
        MotionEvent cancelEvent = null;
        boolean newBlock = false;
        boolean intercepted = false;
        for (int i = 0; i < childCount; i++) {
            View child = (View) topmostChildList.get(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            Behavior b = lp.getBehavior();
            if ((intercepted || newBlock) && action != 0) {
                if (b != null) {
                    if (cancelEvent == null) {
                        long now = SystemClock.uptimeMillis();
                        cancelEvent = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                    }
                    switch (type) {
                        case 0:
                            b.onInterceptTouchEvent(this, child, cancelEvent);
                            break;
                        case 1:
                            b.onTouchEvent(this, child, cancelEvent);
                            break;
                        default:
                            break;
                    }
                }
            } else {
                if (!(intercepted || b == null)) {
                    switch (type) {
                        case 0:
                            intercepted = b.onInterceptTouchEvent(this, child, motionEvent);
                            break;
                        case 1:
                            intercepted = b.onTouchEvent(this, child, motionEvent);
                            break;
                    }
                    if (intercepted) {
                        this.mBehaviorTouchView = child;
                    }
                }
                boolean wasBlocking = lp.didBlockInteraction();
                boolean isBlocking = lp.isBlockingInteractionBelow(this, child);
                boolean z = isBlocking && !wasBlocking;
                newBlock = z;
                if (isBlocking && !newBlock) {
                    topmostChildList.clear();
                    return intercepted;
                }
            }
        }
        topmostChildList.clear();
        return intercepted;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MotionEvent cancelEvent = null;
        int action = MotionEventCompat.getActionMasked(ev);
        if (action == 0) {
            resetTouchBehaviors();
        }
        boolean intercepted = performIntercept(ev, false);
        if (cancelEvent != null) {
            cancelEvent.recycle();
        }
        if (action == 1 || action == 3) {
            resetTouchBehaviors();
        }
        return intercepted;
    }

    /* JADX WARNING: Missing block: B:3:0x0015, code skipped:
            if (r6 != false) goto L_0x0017;
     */
    public boolean onTouchEvent(android.view.MotionEvent r19) {
        /*
        r18 = this;
        r0 = r18;
        r1 = r19;
        r2 = 0;
        r3 = 0;
        r4 = 0;
        r5 = android.support.v4.view.MotionEventCompat.getActionMasked(r19);
        r6 = r0.mBehaviorTouchView;
        r7 = 1;
        if (r6 != 0) goto L_0x0017;
    L_0x0010:
        r6 = r0.performIntercept(r1, r7);
        r3 = r6;
        if (r6 == 0) goto L_0x002b;
    L_0x0017:
        r6 = r0.mBehaviorTouchView;
        r6 = r6.getLayoutParams();
        r6 = (com.oneplus.lib.design.widget.CoordinatorLayout.LayoutParams) r6;
        r8 = r6.getBehavior();
        if (r8 == 0) goto L_0x002b;
    L_0x0025:
        r9 = r0.mBehaviorTouchView;
        r2 = r8.onTouchEvent(r0, r9, r1);
    L_0x002b:
        r6 = r0.mBehaviorTouchView;
        if (r6 != 0) goto L_0x0035;
    L_0x002f:
        r6 = super.onTouchEvent(r19);
        r2 = r2 | r6;
        goto L_0x004c;
    L_0x0035:
        if (r3 == 0) goto L_0x004c;
    L_0x0037:
        if (r4 != 0) goto L_0x0049;
    L_0x0039:
        r16 = android.os.SystemClock.uptimeMillis();
        r12 = 3;
        r13 = 0;
        r14 = 0;
        r15 = 0;
        r8 = r16;
        r10 = r16;
        r4 = android.view.MotionEvent.obtain(r8, r10, r12, r13, r14, r15);
    L_0x0049:
        super.onTouchEvent(r4);
        if (r4 == 0) goto L_0x0052;
    L_0x004f:
        r4.recycle();
    L_0x0052:
        if (r5 == r7) goto L_0x0057;
    L_0x0054:
        r6 = 3;
        if (r5 != r6) goto L_0x005a;
    L_0x0057:
        r18.resetTouchBehaviors();
    L_0x005a:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.design.widget.CoordinatorLayout.onTouchEvent(android.view.MotionEvent):boolean");
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        if (disallowIntercept && !this.mDisallowInterceptReset) {
            resetTouchBehaviors();
            this.mDisallowInterceptReset = true;
        }
    }

    private int getKeyline(int index) {
        String str;
        StringBuilder stringBuilder;
        if (this.mKeylines == null) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("No keylines defined for ");
            stringBuilder.append(this);
            stringBuilder.append(" - attempted index lookup ");
            stringBuilder.append(index);
            Log.e(str, stringBuilder.toString());
            return 0;
        } else if (index >= 0 && index < this.mKeylines.length) {
            return this.mKeylines[index];
        } else {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Keyline index ");
            stringBuilder.append(index);
            stringBuilder.append(" out of range for ");
            stringBuilder.append(this);
            Log.e(str, stringBuilder.toString());
            return 0;
        }
    }

    static Behavior parseBehavior(Context context, AttributeSet attrs, String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        String fullName;
        if (name.startsWith(".")) {
            fullName = new StringBuilder();
            fullName.append(context.getPackageName());
            fullName.append(name);
            fullName = fullName.toString();
        } else if (name.indexOf(46) >= 0) {
            fullName = name;
        } else if (TextUtils.isEmpty(WIDGET_PACKAGE_NAME)) {
            fullName = name;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(WIDGET_PACKAGE_NAME);
            stringBuilder.append('.');
            stringBuilder.append(name);
            fullName = stringBuilder.toString();
        }
        try {
            Map<String, Constructor<Behavior>> constructors = (Map) sConstructors.get();
            if (constructors == null) {
                constructors = new HashMap();
                sConstructors.set(constructors);
            }
            Constructor<Behavior> c = (Constructor) constructors.get(fullName);
            if (c == null) {
                c = Class.forName(fullName, true, context.getClassLoader()).getConstructor(CONSTRUCTOR_PARAMS);
                c.setAccessible(true);
                constructors.put(fullName, c);
            }
            return (Behavior) c.newInstance(new Object[]{context, attrs});
        } catch (Exception e) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Could not inflate Behavior subclass ");
            stringBuilder2.append(fullName);
            throw new RuntimeException(stringBuilder2.toString(), e);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public LayoutParams getResolvedLayoutParams(View child) {
        LayoutParams result = (LayoutParams) child.getLayoutParams();
        if (!result.mBehaviorResolved) {
            DefaultBehavior defaultBehavior = null;
            for (Class<?> childClass = child.getClass(); childClass != null; childClass = childClass.getSuperclass()) {
                DefaultBehavior defaultBehavior2 = (DefaultBehavior) childClass.getAnnotation(DefaultBehavior.class);
                defaultBehavior = defaultBehavior2;
                if (defaultBehavior2 != null) {
                    break;
                }
            }
            if (defaultBehavior != null) {
                try {
                    result.setBehavior((Behavior) defaultBehavior.value().newInstance());
                } catch (Exception e) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Default behavior class ");
                    stringBuilder.append(defaultBehavior.value().getName());
                    stringBuilder.append(" could not be instantiated. Did you forget a default constructor?");
                    Log.e(str, stringBuilder.toString(), e);
                }
            }
            result.mBehaviorResolved = true;
        }
        return result;
    }

    private void prepareChildren() {
        this.mDependencySortedChildren.clear();
        this.mChildDag.clear();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            getResolvedLayoutParams(view).findAnchorView(this, view);
            this.mChildDag.addNode(view);
            for (int j = 0; j < count; j++) {
                if (j != i) {
                    View other = getChildAt(j);
                    if (getResolvedLayoutParams(other).dependsOn(this, other, view)) {
                        if (!this.mChildDag.contains(other)) {
                            this.mChildDag.addNode(other);
                        }
                        this.mChildDag.addEdge(view, other);
                    }
                }
            }
        }
        this.mDependencySortedChildren.addAll(this.mChildDag.getSortedList());
        Collections.reverse(this.mDependencySortedChildren);
    }

    /* Access modifiers changed, original: 0000 */
    public void getDescendantRect(View descendant, Rect out) {
        Utils.getDescendantRect(this, descendant, out);
    }

    /* Access modifiers changed, original: protected */
    public int getSuggestedMinimumWidth() {
        return Math.max(super.getSuggestedMinimumWidth(), getPaddingLeft() + getPaddingRight());
    }

    /* Access modifiers changed, original: protected */
    public int getSuggestedMinimumHeight() {
        return Math.max(super.getSuggestedMinimumHeight(), getPaddingTop() + getPaddingBottom());
    }

    public void onMeasureChild(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Missing block: B:43:0x0138, code skipped:
            if (r29.onMeasureChild(r7, r4, r21, r23, r26, 0) == false) goto L_0x0149;
     */
    public void onMeasure(int r37, int r38) {
        /*
        r36 = this;
        r7 = r36;
        r36.prepareChildren();
        r36.ensurePreDrawListener();
        r8 = r36.getPaddingLeft();
        r9 = r36.getPaddingTop();
        r10 = r36.getPaddingRight();
        r11 = r36.getPaddingBottom();
        r12 = android.support.v4.view.ViewCompat.getLayoutDirection(r36);
        r0 = 1;
        if (r12 != r0) goto L_0x0021;
    L_0x001f:
        r1 = r0;
        goto L_0x0022;
    L_0x0021:
        r1 = 0;
    L_0x0022:
        r14 = r1;
        r15 = android.view.View.MeasureSpec.getMode(r37);
        r16 = android.view.View.MeasureSpec.getSize(r37);
        r6 = android.view.View.MeasureSpec.getMode(r38);
        r17 = android.view.View.MeasureSpec.getSize(r38);
        r18 = r8 + r10;
        r19 = r9 + r11;
        r1 = r36.getSuggestedMinimumWidth();
        r2 = r36.getSuggestedMinimumHeight();
        r3 = 0;
        r4 = r7.mLastInsets;
        if (r4 == 0) goto L_0x004b;
    L_0x0044:
        r4 = android.support.v4.view.ViewCompat.getFitsSystemWindows(r36);
        if (r4 == 0) goto L_0x004b;
    L_0x004a:
        goto L_0x004c;
    L_0x004b:
        r0 = 0;
    L_0x004c:
        r20 = r0;
        r0 = r7.mDependencySortedChildren;
        r5 = r0.size();
        r4 = r1;
        r0 = 0;
    L_0x0056:
        r1 = r0;
        if (r1 >= r5) goto L_0x0191;
    L_0x0059:
        r0 = r7.mDependencySortedChildren;
        r0 = r0.get(r1);
        r0 = (android.view.View) r0;
        r13 = r0.getVisibility();
        r22 = r1;
        r1 = 8;
        if (r13 != r1) goto L_0x0074;
        r25 = r5;
        r27 = r6;
        r24 = 0;
        goto L_0x0189;
    L_0x0074:
        r1 = r0.getLayoutParams();
        r13 = r1;
        r13 = (com.oneplus.lib.design.widget.CoordinatorLayout.LayoutParams) r13;
        r1 = 0;
        r23 = r1;
        r1 = r13.keyline;
        if (r1 < 0) goto L_0x00c8;
    L_0x0082:
        if (r15 == 0) goto L_0x00c8;
    L_0x0084:
        r1 = r13.keyline;
        r1 = r7.getKeyline(r1);
        r24 = r2;
        r2 = r13.gravity;
        r2 = resolveKeylineGravity(r2);
        r2 = android.support.v4.view.GravityCompat.getAbsoluteGravity(r2, r12);
        r2 = r2 & 7;
        r25 = r3;
        r3 = 3;
        if (r2 != r3) goto L_0x009f;
    L_0x009d:
        if (r14 == 0) goto L_0x00a4;
    L_0x009f:
        r3 = 5;
        if (r2 != r3) goto L_0x00b1;
    L_0x00a2:
        if (r14 == 0) goto L_0x00b1;
    L_0x00a4:
        r3 = r16 - r10;
        r3 = r3 - r1;
        r27 = r4;
        r4 = 0;
        r3 = java.lang.Math.max(r4, r3);
        r23 = r3;
        goto L_0x00cf;
    L_0x00b1:
        r27 = r4;
        if (r2 != r3) goto L_0x00b7;
    L_0x00b5:
        if (r14 == 0) goto L_0x00bc;
    L_0x00b7:
        r3 = 3;
        if (r2 != r3) goto L_0x00c6;
    L_0x00ba:
        if (r14 == 0) goto L_0x00c6;
    L_0x00bc:
        r3 = r1 - r8;
        r4 = 0;
        r1 = java.lang.Math.max(r4, r3);
        r23 = r1;
        goto L_0x00cf;
    L_0x00c6:
        r4 = 0;
        goto L_0x00cf;
    L_0x00c8:
        r24 = r2;
        r25 = r3;
        r27 = r4;
        r4 = 0;
    L_0x00cf:
        r1 = r37;
        r2 = r38;
        if (r20 == 0) goto L_0x0108;
    L_0x00d5:
        r3 = android.support.v4.view.ViewCompat.getFitsSystemWindows(r0);
        if (r3 != 0) goto L_0x0108;
    L_0x00db:
        r3 = r7.mLastInsets;
        r3 = r3.getSystemWindowInsetLeft();
        r4 = r7.mLastInsets;
        r4 = r4.getSystemWindowInsetRight();
        r3 = r3 + r4;
        r4 = r7.mLastInsets;
        r4 = r4.getSystemWindowInsetTop();
        r28 = r0;
        r0 = r7.mLastInsets;
        r0 = r0.getSystemWindowInsetBottom();
        r4 = r4 + r0;
        r0 = r16 - r3;
        r0 = android.view.View.MeasureSpec.makeMeasureSpec(r0, r15);
        r1 = r17 - r4;
        r1 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r6);
        r21 = r0;
        r26 = r1;
        goto L_0x010e;
    L_0x0108:
        r28 = r0;
        r21 = r1;
        r26 = r2;
    L_0x010e:
        r29 = r13.getBehavior();
        if (r29 == 0) goto L_0x013b;
    L_0x0114:
        r30 = 0;
        r4 = r28;
        r0 = r29;
        r1 = r7;
        r3 = r24;
        r2 = r4;
        r32 = r3;
        r31 = r25;
        r3 = r21;
        r33 = r4;
        r34 = r27;
        r24 = 0;
        r4 = r23;
        r25 = r5;
        r5 = r26;
        r27 = r6;
        r6 = r30;
        r0 = r0.onMeasureChild(r1, r2, r3, r4, r5, r6);
        if (r0 != 0) goto L_0x0156;
    L_0x013a:
        goto L_0x0149;
    L_0x013b:
        r32 = r24;
        r31 = r25;
        r34 = r27;
        r33 = r28;
        r24 = 0;
        r25 = r5;
        r27 = r6;
    L_0x0149:
        r5 = 0;
        r0 = r7;
        r1 = r33;
        r2 = r21;
        r3 = r23;
        r4 = r26;
        r0.onMeasureChild(r1, r2, r3, r4, r5);
    L_0x0156:
        r0 = r33;
        r1 = r0.getMeasuredWidth();
        r1 = r18 + r1;
        r2 = r13.leftMargin;
        r1 = r1 + r2;
        r2 = r13.rightMargin;
        r1 = r1 + r2;
        r2 = r34;
        r1 = java.lang.Math.max(r2, r1);
        r2 = r0.getMeasuredHeight();
        r2 = r19 + r2;
        r3 = r13.topMargin;
        r2 = r2 + r3;
        r3 = r13.bottomMargin;
        r2 = r2 + r3;
        r3 = r32;
        r2 = java.lang.Math.max(r3, r2);
        r3 = android.support.v4.view.ViewCompat.getMeasuredState(r0);
        r4 = r31;
        r0 = android.support.v4.view.ViewCompat.combineMeasuredStates(r4, r3);
        r3 = r0;
        r4 = r1;
    L_0x0189:
        r0 = r22 + 1;
        r5 = r25;
        r6 = r27;
        goto L_0x0056;
    L_0x0191:
        r25 = r5;
        r27 = r6;
        r35 = r3;
        r3 = r2;
        r2 = r4;
        r4 = r35;
        r0 = -16777216; // 0xffffffffff000000 float:-1.7014118E38 double:NaN;
        r0 = r0 & r4;
        r1 = r37;
        r0 = android.support.v4.view.ViewCompat.resolveSizeAndState(r2, r1, r0);
        r5 = r4 << 16;
        r6 = r38;
        r5 = android.support.v4.view.ViewCompat.resolveSizeAndState(r3, r6, r5);
        r7.setMeasuredDimension(r0, r5);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.design.widget.CoordinatorLayout.onMeasure(int, int):void");
    }

    private WindowInsetsCompat dispatchApplyWindowInsetsToBehaviors(WindowInsetsCompat insets) {
        if (insets.isConsumed()) {
            return insets;
        }
        int z = getChildCount();
        for (int i = 0; i < z; i++) {
            View child = getChildAt(i);
            if (ViewCompat.getFitsSystemWindows(child)) {
                Behavior b = ((LayoutParams) child.getLayoutParams()).getBehavior();
                if (b != null) {
                    insets = b.onApplyWindowInsets(this, child, insets);
                    if (insets.isConsumed()) {
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        return insets;
    }

    public void onLayoutChild(View child, int layoutDirection) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.checkAnchorChanged()) {
            throw new IllegalStateException("An anchor may not be changed after CoordinatorLayout measurement begins before layout is complete.");
        } else if (lp.mAnchorView != null) {
            layoutChildWithAnchor(child, lp.mAnchorView, layoutDirection);
        } else if (lp.keyline >= 0) {
            layoutChildWithKeyline(child, lp.keyline, layoutDirection);
        } else {
            layoutChild(child, layoutDirection);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int childCount = this.mDependencySortedChildren.size();
        for (int i = 0; i < childCount; i++) {
            View child = (View) this.mDependencySortedChildren.get(i);
            if (child.getVisibility() != 8) {
                Behavior behavior = ((LayoutParams) child.getLayoutParams()).getBehavior();
                if (behavior == null || !behavior.onLayoutChild(this, child, layoutDirection)) {
                    onLayoutChild(child, layoutDirection);
                }
            }
        }
    }

    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (this.mDrawStatusBarBackground && this.mStatusBarBackground != null) {
            int inset = this.mLastInsets != null ? this.mLastInsets.getSystemWindowInsetTop() : 0;
            if (inset > 0) {
                this.mStatusBarBackground.setBounds(0, 0, getWidth(), inset);
                this.mStatusBarBackground.draw(c);
            }
        }
    }

    public void setFitsSystemWindows(boolean fitSystemWindows) {
        super.setFitsSystemWindows(fitSystemWindows);
        setupForInsets();
    }

    /* Access modifiers changed, original: 0000 */
    public void recordLastChildRect(View child, Rect r) {
        ((LayoutParams) child.getLayoutParams()).setLastChildRect(r);
    }

    /* Access modifiers changed, original: 0000 */
    public void getLastChildRect(View child, Rect out) {
        out.set(((LayoutParams) child.getLayoutParams()).getLastChildRect());
    }

    /* Access modifiers changed, original: 0000 */
    public void getChildRect(View child, boolean transform, Rect out) {
        if (child.isLayoutRequested() || child.getVisibility() == 8) {
            out.setEmpty();
            return;
        }
        if (transform) {
            getDescendantRect(child, out);
        } else {
            out.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
        }
    }

    private void getDesiredAnchoredChildRectWithoutConstraints(View child, int layoutDirection, Rect anchorRect, Rect out, LayoutParams lp, int childWidth, int childHeight) {
        int left;
        int top;
        int i = layoutDirection;
        Rect rect = anchorRect;
        LayoutParams layoutParams = lp;
        int absGravity = GravityCompat.getAbsoluteGravity(resolveAnchoredChildGravity(layoutParams.gravity), i);
        int absAnchorGravity = GravityCompat.getAbsoluteGravity(resolveGravity(layoutParams.anchorGravity), i);
        int hgrav = absGravity & 7;
        int vgrav = absGravity & 112;
        int anchorHgrav = absAnchorGravity & 7;
        int anchorVgrav = absAnchorGravity & 112;
        if (anchorHgrav == 1) {
            left = rect.left + (anchorRect.width() / 2);
        } else if (anchorHgrav != 5) {
            left = rect.left;
        } else {
            left = rect.right;
        }
        if (anchorVgrav == 16) {
            top = rect.top + (anchorRect.height() / 2);
        } else if (anchorVgrav != 80) {
            top = rect.top;
        } else {
            top = rect.bottom;
        }
        if (hgrav == 1) {
            left -= childWidth / 2;
        } else if (hgrav != 5) {
            left -= childWidth;
        }
        if (vgrav == 16) {
            top -= childHeight / 2;
        } else if (vgrav != 80) {
            top -= childHeight;
        }
        out.set(left, top, left + childWidth, top + childHeight);
    }

    private void constrainChildRect(LayoutParams lp, Rect out, int childWidth, int childHeight) {
        int width = getWidth();
        int height = getHeight();
        int left = Math.max(getPaddingLeft() + lp.leftMargin, Math.min(out.left, ((width - getPaddingRight()) - childWidth) - lp.rightMargin));
        int top = Math.max(getPaddingTop() + lp.topMargin, Math.min(out.top, ((height - getPaddingBottom()) - childHeight) - lp.bottomMargin));
        out.set(left, top, left + childWidth, top + childHeight);
    }

    /* Access modifiers changed, original: 0000 */
    public void getDesiredAnchoredChildRect(View child, int layoutDirection, Rect anchorRect, Rect out) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        getDesiredAnchoredChildRectWithoutConstraints(child, layoutDirection, anchorRect, out, lp, childWidth, childHeight);
        constrainChildRect(lp, out, childWidth, childHeight);
    }

    private void layoutChildWithAnchor(View child, View anchor, int layoutDirection) {
        child.getLayoutParams();
        Rect anchorRect = this.mTempRect1;
        Rect childRect = this.mTempRect2;
        getDescendantRect(anchor, anchorRect);
        getDesiredAnchoredChildRect(child, layoutDirection, anchorRect, childRect);
        child.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
    }

    private void layoutChildWithKeyline(View child, int keyline, int layoutDirection) {
        int keyline2;
        int i = layoutDirection;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int absGravity = GravityCompat.getAbsoluteGravity(resolveKeylineGravity(lp.gravity), i);
        int hgrav = absGravity & 7;
        int vgrav = absGravity & 112;
        int width = getWidth();
        int height = getHeight();
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        if (i == 1) {
            keyline2 = width - keyline;
        } else {
            keyline2 = keyline;
        }
        int left = getKeyline(keyline2) - childWidth;
        int top = 0;
        if (hgrav == 1) {
            left += childWidth / 2;
        } else if (hgrav == 5) {
            left += childWidth;
        }
        if (vgrav == 16) {
            top = 0 + (childHeight / 2);
        } else if (vgrav == 80) {
            top = 0 + childHeight;
        }
        int left2 = Math.max(getPaddingLeft() + lp.leftMargin, Math.min(left, ((width - getPaddingRight()) - childWidth) - lp.rightMargin));
        left = Math.max(getPaddingTop() + lp.topMargin, Math.min(top, ((height - getPaddingBottom()) - childHeight) - lp.bottomMargin));
        child.layout(left2, left, left2 + childWidth, left + childHeight);
    }

    private void layoutChild(View child, int layoutDirection) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        Rect parent = this.mTempRect1;
        parent.set(getPaddingLeft() + lp.leftMargin, getPaddingTop() + lp.topMargin, (getWidth() - getPaddingRight()) - lp.rightMargin, (getHeight() - getPaddingBottom()) - lp.bottomMargin);
        if (!(this.mLastInsets == null || !ViewCompat.getFitsSystemWindows(this) || ViewCompat.getFitsSystemWindows(child))) {
            parent.left += this.mLastInsets.getSystemWindowInsetLeft();
            parent.top += this.mLastInsets.getSystemWindowInsetTop();
            parent.right -= this.mLastInsets.getSystemWindowInsetRight();
            parent.bottom -= this.mLastInsets.getSystemWindowInsetBottom();
        }
        Rect out = this.mTempRect2;
        GravityCompat.apply(resolveGravity(lp.gravity), child.getMeasuredWidth(), child.getMeasuredHeight(), parent, out, layoutDirection);
        child.layout(out.left, out.top, out.right, out.bottom);
    }

    private static int resolveGravity(int gravity) {
        return gravity == 0 ? 8388659 : gravity;
    }

    private static int resolveKeylineGravity(int gravity) {
        return gravity == 0 ? 8388661 : gravity;
    }

    private static int resolveAnchoredChildGravity(int gravity) {
        return gravity == 0 ? 17 : gravity;
    }

    /* Access modifiers changed, original: protected */
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mBehavior != null) {
            float scrimAlpha = lp.mBehavior.getScrimOpacity(this, child);
            if (scrimAlpha > 0.0f) {
                if (this.mScrimPaint == null) {
                    this.mScrimPaint = new Paint();
                }
                this.mScrimPaint.setColor(lp.mBehavior.getScrimColor(this, child));
                this.mScrimPaint.setAlpha(MathUtils.constrain(Math.round(255.0f * scrimAlpha), 0, 255));
                int saved = canvas.save();
                if (child.isOpaque()) {
                    canvas.clipRect((float) child.getLeft(), (float) child.getTop(), (float) child.getRight(), (float) child.getBottom(), Op.DIFFERENCE);
                }
                canvas.drawRect((float) getPaddingLeft(), (float) getPaddingTop(), (float) (getWidth() - getPaddingRight()), (float) (getHeight() - getPaddingBottom()), this.mScrimPaint);
                canvas.restoreToCount(saved);
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    /* Access modifiers changed, original: final */
    public final void onChildViewsChanged(int type) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int childCount = this.mDependencySortedChildren.size();
        Rect inset = this.mTempRect4;
        inset.setEmpty();
        for (int i = 0; i < childCount; i++) {
            int absInsetEdge;
            View child = (View) this.mDependencySortedChildren.get(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            for (int j = 0; j < i; j++) {
                if (lp.mAnchorDirectChild == ((View) this.mDependencySortedChildren.get(j))) {
                    offsetChildToAnchor(child, layoutDirection);
                }
            }
            Rect drawRect = this.mTempRect1;
            getChildRect(child, true, drawRect);
            if (!(lp.insetEdge == 0 || drawRect.isEmpty())) {
                absInsetEdge = GravityCompat.getAbsoluteGravity(lp.insetEdge, layoutDirection);
                int i2 = absInsetEdge & 112;
                if (i2 == 48) {
                    inset.top = Math.max(inset.top, drawRect.bottom);
                } else if (i2 == 80) {
                    inset.bottom = Math.max(inset.bottom, getHeight() - drawRect.top);
                }
                i2 = absInsetEdge & 7;
                if (i2 == 3) {
                    inset.left = Math.max(inset.left, drawRect.right);
                } else if (i2 == 5) {
                    inset.right = Math.max(inset.right, getWidth() - drawRect.left);
                }
            }
            if (lp.dodgeInsetEdges != 0 && child.getVisibility() == 0) {
                offsetChildByInset(child, inset, layoutDirection);
            }
            if (type == 0) {
                Rect lastDrawRect = this.mTempRect2;
                getLastChildRect(child, lastDrawRect);
                if (lastDrawRect.equals(drawRect)) {
                } else {
                    recordLastChildRect(child, drawRect);
                }
            }
            for (absInsetEdge = i + 1; absInsetEdge < childCount; absInsetEdge++) {
                View checkChild = (View) this.mDependencySortedChildren.get(absInsetEdge);
                LayoutParams checkLp = (LayoutParams) checkChild.getLayoutParams();
                Behavior b = checkLp.getBehavior();
                if (b != null && b.layoutDependsOn(this, checkChild, child)) {
                    if (type == 0 && checkLp.getChangedAfterNestedScroll()) {
                        checkLp.resetChangedAfterNestedScroll();
                    } else {
                        boolean handled;
                        if (type != 2) {
                            handled = b.onDependentViewChanged(this, checkChild, child);
                        } else {
                            b.onDependentViewRemoved(this, checkChild, child);
                            handled = true;
                        }
                        if (type == 1) {
                            checkLp.setChangedAfterNestedScroll(handled);
                        }
                    }
                }
            }
        }
    }

    private void offsetChildByInset(View child, Rect inset, int layoutDirection) {
        if (ViewCompat.isLaidOut(child)) {
            Rect bounds = this.mTempRect5;
            bounds.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            if (!bounds.isEmpty()) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                Behavior behavior = lp.getBehavior();
                Rect dodgeRect = this.mTempRect3;
                dodgeRect.setEmpty();
                if (behavior == null || !behavior.getInsetDodgeRect(this, child, dodgeRect)) {
                    dodgeRect.set(bounds);
                } else if (!bounds.contains(dodgeRect)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Rect should be within the child's bounds. Rect:");
                    stringBuilder.append(dodgeRect.toShortString());
                    stringBuilder.append(" | Bounds:");
                    stringBuilder.append(bounds.toShortString());
                    throw new IllegalArgumentException(stringBuilder.toString());
                }
                if (!dodgeRect.isEmpty()) {
                    int distance;
                    int distance2;
                    int absDodgeInsetEdges = GravityCompat.getAbsoluteGravity(lp.dodgeInsetEdges, layoutDirection);
                    boolean offsetY = false;
                    if ((absDodgeInsetEdges & 48) == 48) {
                        distance = (dodgeRect.top - lp.topMargin) - lp.mInsetOffsetY;
                        if (distance < inset.top) {
                            setInsetOffsetY(child, inset.top - distance);
                            offsetY = true;
                        }
                    }
                    if ((absDodgeInsetEdges & 80) == 80) {
                        distance = ((getHeight() - dodgeRect.bottom) - lp.bottomMargin) + lp.mInsetOffsetY;
                        if (distance < inset.bottom) {
                            setInsetOffsetY(child, distance - inset.bottom);
                            offsetY = true;
                        }
                    }
                    if (!offsetY) {
                        setInsetOffsetY(child, 0);
                    }
                    boolean offsetX = false;
                    if ((absDodgeInsetEdges & 3) == 3) {
                        distance2 = (dodgeRect.left - lp.leftMargin) - lp.mInsetOffsetX;
                        if (distance2 < inset.left) {
                            setInsetOffsetX(child, inset.left - distance2);
                            offsetX = true;
                        }
                    }
                    if ((absDodgeInsetEdges & 5) == 5) {
                        distance2 = ((getWidth() - dodgeRect.right) - lp.rightMargin) + lp.mInsetOffsetX;
                        if (distance2 < inset.right) {
                            setInsetOffsetX(child, distance2 - inset.right);
                            offsetX = true;
                        }
                    }
                    if (!offsetX) {
                        setInsetOffsetX(child, 0);
                    }
                }
            }
        }
    }

    private void setInsetOffsetX(View child, int offsetX) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mInsetOffsetX != offsetX) {
            ViewCompat.offsetLeftAndRight(child, offsetX - lp.mInsetOffsetX);
            lp.mInsetOffsetX = offsetX;
        }
    }

    private void setInsetOffsetY(View child, int offsetY) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mInsetOffsetY != offsetY) {
            ViewCompat.offsetTopAndBottom(child, offsetY - lp.mInsetOffsetY);
            lp.mInsetOffsetY = offsetY;
        }
    }

    public void dispatchDependentViewsChanged(View view) {
        List<View> dependents = this.mChildDag.getIncomingEdges(view);
        if (dependents != null && !dependents.isEmpty()) {
            for (int i = 0; i < dependents.size(); i++) {
                View child = (View) dependents.get(i);
                Behavior b = ((LayoutParams) child.getLayoutParams()).getBehavior();
                if (b != null) {
                    b.onDependentViewChanged(this, child, view);
                }
            }
        }
    }

    @NonNull
    public List<View> getDependencies(@NonNull View child) {
        List<View> dependencies = this.mChildDag.getOutgoingEdges(child);
        this.mTempDependenciesList.clear();
        if (dependencies != null) {
            this.mTempDependenciesList.addAll(dependencies);
        }
        return this.mTempDependenciesList;
    }

    @NonNull
    public List<View> getDependents(@NonNull View child) {
        List<View> edges = this.mChildDag.getIncomingEdges(child);
        this.mTempDependenciesList.clear();
        if (edges != null) {
            this.mTempDependenciesList.addAll(edges);
        }
        return this.mTempDependenciesList;
    }

    /* Access modifiers changed, original: final */
    @VisibleForTesting
    public final List<View> getDependencySortedChildren() {
        prepareChildren();
        return Collections.unmodifiableList(this.mDependencySortedChildren);
    }

    /* Access modifiers changed, original: 0000 */
    public void ensurePreDrawListener() {
        boolean hasDependencies = false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (hasDependencies(getChildAt(i))) {
                hasDependencies = true;
                break;
            }
        }
        if (hasDependencies == this.mNeedsPreDrawListener) {
            return;
        }
        if (hasDependencies) {
            addPreDrawListener();
        } else {
            removePreDrawListener();
        }
    }

    private boolean hasDependencies(View child) {
        return this.mChildDag.hasOutgoingEdges(child);
    }

    /* Access modifiers changed, original: 0000 */
    public void addPreDrawListener() {
        if (this.mIsAttachedToWindow) {
            if (this.mOnPreDrawListener == null) {
                this.mOnPreDrawListener = new OnPreDrawListener();
            }
            getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }
        this.mNeedsPreDrawListener = true;
    }

    /* Access modifiers changed, original: 0000 */
    public void removePreDrawListener() {
        if (this.mIsAttachedToWindow && this.mOnPreDrawListener != null) {
            getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }
        this.mNeedsPreDrawListener = false;
    }

    /* Access modifiers changed, original: 0000 */
    public void offsetChildToAnchor(View child, int layoutDirection) {
        View view = child;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.mAnchorView != null) {
            Rect anchorRect = this.mTempRect1;
            Rect childRect = this.mTempRect2;
            Rect desiredChildRect = this.mTempRect3;
            getDescendantRect(lp.mAnchorView, anchorRect);
            boolean z = false;
            getChildRect(view, false, childRect);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int childHeight2 = childHeight;
            getDesiredAnchoredChildRectWithoutConstraints(view, layoutDirection, anchorRect, desiredChildRect, lp, childWidth, childHeight);
            if (!(desiredChildRect.left == childRect.left && desiredChildRect.top == childRect.top)) {
                z = true;
            }
            boolean changed = z;
            constrainChildRect(lp, desiredChildRect, childWidth, childHeight2);
            int dx = desiredChildRect.left - childRect.left;
            int dy = desiredChildRect.top - childRect.top;
            if (dx != 0) {
                ViewCompat.offsetLeftAndRight(view, dx);
            }
            if (dy != 0) {
                ViewCompat.offsetTopAndBottom(view, dy);
            }
            if (changed) {
                Behavior b = lp.getBehavior();
                if (b != null) {
                    b.onDependentViewChanged(this, view, lp.mAnchorView);
                }
            }
        }
    }

    public boolean isPointInChildBounds(View child, int x, int y) {
        Rect r = this.mTempRect1;
        getDescendantRect(child, r);
        return r.contains(x, y);
    }

    public boolean doViewsOverlap(View first, View second) {
        boolean z = false;
        if (first.getVisibility() != 0 || second.getVisibility() != 0) {
            return false;
        }
        Rect firstRect = this.mTempRect1;
        getChildRect(first, first.getParent() != this, firstRect);
        Rect secondRect = this.mTempRect2;
        getChildRect(second, second.getParent() != this, secondRect);
        if (firstRect.left <= secondRect.right && firstRect.top <= secondRect.bottom && firstRect.right >= secondRect.left && firstRect.bottom >= secondRect.top) {
            z = true;
        }
        return z;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* Access modifiers changed, original: protected */
    public LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
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
        return (p instanceof LayoutParams) && super.checkLayoutParams(p);
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        int childCount = getChildCount();
        boolean handled = false;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            Behavior viewBehavior = lp.getBehavior();
            if (viewBehavior != null) {
                boolean accepted = viewBehavior.onStartNestedScroll(this, view, child, target, nestedScrollAxes);
                handled |= accepted;
                lp.acceptNestedScroll(accepted);
            } else {
                lp.acceptNestedScroll(false);
            }
        }
        return handled;
    }

    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        this.mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        this.mNestedScrollingDirectChild = child;
        this.mNestedScrollingTarget = target;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted()) {
                Behavior viewBehavior = lp.getBehavior();
                if (viewBehavior != null) {
                    viewBehavior.onNestedScrollAccepted(this, view, child, target, nestedScrollAxes);
                }
            }
        }
    }

    public void onStopNestedScroll(View target) {
        this.mNestedScrollingParentHelper.onStopNestedScroll(target);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted()) {
                Behavior viewBehavior = lp.getBehavior();
                if (viewBehavior != null) {
                    viewBehavior.onStopNestedScroll(this, view, target);
                }
                lp.resetNestedScroll();
                lp.resetChangedAfterNestedScroll();
            }
        }
        this.mNestedScrollingDirectChild = null;
        this.mNestedScrollingTarget = null;
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int childCount = getChildCount();
        int i = 0;
        boolean accepted = false;
        while (true) {
            int i2 = i;
            if (i2 >= childCount) {
                break;
            }
            View view = getChildAt(i2);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted()) {
                Behavior viewBehavior = lp.getBehavior();
                if (viewBehavior != null) {
                    viewBehavior.onNestedScroll(this, view, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
                    accepted = true;
                }
            }
            i = i2 + 1;
        }
        if (accepted) {
            onChildViewsChanged(1);
        }
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int childCount = getChildCount();
        int xConsumed = 0;
        int yConsumed = 0;
        boolean accepted = false;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= childCount) {
                break;
            }
            View view = getChildAt(i2);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted()) {
                Behavior viewBehavior = lp.getBehavior();
                if (viewBehavior != null) {
                    int xConsumed2;
                    int yConsumed2;
                    int[] iArr = this.mTempIntPair;
                    this.mTempIntPair[1] = 0;
                    iArr[0] = 0;
                    viewBehavior.onNestedPreScroll(this, view, target, dx, dy, this.mTempIntPair);
                    if (dx > 0) {
                        xConsumed2 = Math.max(xConsumed, this.mTempIntPair[0]);
                    } else {
                        xConsumed2 = Math.min(xConsumed, this.mTempIntPair[0]);
                    }
                    if (dy > 0) {
                        yConsumed2 = Math.max(yConsumed, this.mTempIntPair[1]);
                    } else {
                        yConsumed2 = Math.min(yConsumed, this.mTempIntPair[1]);
                    }
                    xConsumed = xConsumed2;
                    yConsumed = yConsumed2;
                    accepted = true;
                }
            }
            i = i2 + 1;
        }
        consumed[0] = xConsumed;
        consumed[1] = yConsumed;
        if (accepted) {
            onChildViewsChanged(1);
        }
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        int childCount = getChildCount();
        int i = 0;
        boolean handled = false;
        while (true) {
            int i2 = i;
            if (i2 >= childCount) {
                break;
            }
            View view = getChildAt(i2);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted()) {
                Behavior viewBehavior = lp.getBehavior();
                if (viewBehavior != null) {
                    handled = viewBehavior.onNestedFling(this, view, target, velocityX, velocityY, consumed) | handled;
                }
            }
            i = i2 + 1;
        }
        if (handled) {
            onChildViewsChanged(1);
        }
        return handled;
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        boolean handled = false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.isNestedScrollAccepted()) {
                Behavior viewBehavior = lp.getBehavior();
                if (viewBehavior != null) {
                    handled |= viewBehavior.onNestedPreFling(this, view, target, velocityX, velocityY);
                }
            }
        }
        return handled;
    }

    public int getNestedScrollAxes() {
        return this.mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            SparseArray<Parcelable> behaviorStates = ss.behaviorStates;
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                int childId = child.getId();
                Behavior b = getResolvedLayoutParams(child).getBehavior();
                if (!(childId == -1 || b == null)) {
                    Parcelable savedState = (Parcelable) behaviorStates.get(childId);
                    if (savedState != null) {
                        b.onRestoreInstanceState(this, child, savedState);
                    }
                }
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        SparseArray<Parcelable> behaviorStates = new SparseArray();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int childId = child.getId();
            Behavior b = ((LayoutParams) child.getLayoutParams()).getBehavior();
            if (!(childId == -1 || b == null)) {
                Parcelable state = b.onSaveInstanceState(this, child);
                if (state != null) {
                    behaviorStates.append(childId, state);
                }
            }
        }
        ss.behaviorStates = behaviorStates;
        return ss;
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        Behavior behavior = ((LayoutParams) child.getLayoutParams()).getBehavior();
        if (behavior == null || !behavior.onRequestChildRectangleOnScreen(this, child, rectangle, immediate)) {
            return super.requestChildRectangleOnScreen(child, rectangle, immediate);
        }
        return true;
    }

    private void setupForInsets() {
        if (VERSION.SDK_INT >= 21) {
            if (ViewCompat.getFitsSystemWindows(this)) {
                if (this.mApplyWindowInsetsListener == null) {
                    this.mApplyWindowInsetsListener = new OnApplyWindowInsetsListener() {
                        public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                            return CoordinatorLayout.this.setWindowInsets(insets);
                        }
                    };
                }
                ViewCompat.setOnApplyWindowInsetsListener(this, this.mApplyWindowInsetsListener);
                setSystemUiVisibility(1280);
            } else {
                ViewCompat.setOnApplyWindowInsetsListener(this, null);
            }
        }
    }
}