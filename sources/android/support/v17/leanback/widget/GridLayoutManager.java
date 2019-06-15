package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.VisibleForTesting;
import android.support.v17.leanback.widget.Grid.Location;
import android.support.v17.leanback.widget.Grid.Provider;
import android.support.v17.leanback.widget.ItemAlignmentFacet.ItemAlignmentDef;
import android.support.v17.leanback.widget.WindowAlignment.Axis;
import android.support.v4.util.CircularIntArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager.LayoutPrefetchRegistry;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.SmoothScroller;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.FocusFinder;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class GridLayoutManager extends LayoutManager {
    static final boolean DEBUG = false;
    static final int DEFAULT_MAX_PENDING_MOVES = 10;
    static final int MIN_MS_SMOOTH_SCROLL_MAIN_SCREEN = 30;
    private static final int NEXT_ITEM = 1;
    private static final int NEXT_ROW = 3;
    static final int PF_FAST_RELAYOUT = 4;
    static final int PF_FAST_RELAYOUT_UPDATED_SELECTED_POSITION = 8;
    static final int PF_FOCUS_OUT_END = 4096;
    static final int PF_FOCUS_OUT_FRONT = 2048;
    static final int PF_FOCUS_OUT_MASKS = 6144;
    static final int PF_FOCUS_OUT_SIDE_END = 16384;
    static final int PF_FOCUS_OUT_SIDE_MASKS = 24576;
    static final int PF_FOCUS_OUT_SIDE_START = 8192;
    static final int PF_FOCUS_SEARCH_DISABLED = 32768;
    static final int PF_FORCE_FULL_LAYOUT = 256;
    static final int PF_IN_LAYOUT_SEARCH_FOCUS = 16;
    static final int PF_IN_SELECTION = 32;
    static final int PF_LAYOUT_EATEN_IN_SLIDING = 128;
    static final int PF_LAYOUT_ENABLED = 512;
    static final int PF_PRUNE_CHILD = 65536;
    static final int PF_REVERSE_FLOW_MASK = 786432;
    static final int PF_REVERSE_FLOW_PRIMARY = 262144;
    static final int PF_REVERSE_FLOW_SECONDARY = 524288;
    static final int PF_ROW_SECONDARY_SIZE_REFRESH = 1024;
    static final int PF_SCROLL_ENABLED = 131072;
    static final int PF_SLIDING = 64;
    static final int PF_STAGE_LAYOUT = 1;
    static final int PF_STAGE_MASK = 3;
    static final int PF_STAGE_SCROLL = 2;
    private static final int PREV_ITEM = 0;
    private static final int PREV_ROW = 2;
    private static final String TAG = "GridLayoutManager";
    static final boolean TRACE = false;
    private static final Rect sTempRect = new Rect();
    static int[] sTwoInts = new int[2];
    final BaseGridView mBaseGridView;
    OnChildLaidOutListener mChildLaidOutListener = null;
    private OnChildSelectedListener mChildSelectedListener = null;
    private ArrayList<OnChildViewHolderSelectedListener> mChildViewHolderSelectedListeners = null;
    int mChildVisibility;
    final ViewsStateBundle mChildrenStates = new ViewsStateBundle();
    GridLinearSmoothScroller mCurrentSmoothScroller;
    int[] mDisappearingPositions;
    private int mExtraLayoutSpace;
    int mExtraLayoutSpaceInPreLayout;
    private FacetProviderAdapter mFacetProviderAdapter;
    private int mFixedRowSizeSecondary;
    int mFlag = 221696;
    int mFocusPosition = -1;
    private int mFocusPositionOffset = 0;
    private int mFocusScrollStrategy = 0;
    private int mGravity = 8388659;
    Grid mGrid;
    private Provider mGridProvider = new Provider() {
        public int getMinIndex() {
            return GridLayoutManager.this.mPositionDeltaInPreLayout;
        }

        public int getCount() {
            return GridLayoutManager.this.mState.getItemCount() + GridLayoutManager.this.mPositionDeltaInPreLayout;
        }

        public int createItem(int index, boolean append, Object[] item, boolean disappearingItem) {
            View v = GridLayoutManager.this.getViewForPosition(index - GridLayoutManager.this.mPositionDeltaInPreLayout);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            lp.setItemAlignmentFacet((ItemAlignmentFacet) GridLayoutManager.this.getFacet(GridLayoutManager.this.mBaseGridView.getChildViewHolder(v), ItemAlignmentFacet.class));
            if (!lp.isItemRemoved()) {
                if (disappearingItem) {
                    if (append) {
                        GridLayoutManager.this.addDisappearingView(v);
                    } else {
                        GridLayoutManager.this.addDisappearingView(v, 0);
                    }
                } else if (append) {
                    GridLayoutManager.this.addView(v);
                } else {
                    GridLayoutManager.this.addView(v, 0);
                }
                if (GridLayoutManager.this.mChildVisibility != -1) {
                    v.setVisibility(GridLayoutManager.this.mChildVisibility);
                }
                if (GridLayoutManager.this.mPendingMoveSmoothScroller != null) {
                    GridLayoutManager.this.mPendingMoveSmoothScroller.consumePendingMovesBeforeLayout();
                }
                int subindex = GridLayoutManager.this.getSubPositionByView(v, v.findFocus());
                if ((GridLayoutManager.this.mFlag & 3) != 1) {
                    if (index == GridLayoutManager.this.mFocusPosition && subindex == GridLayoutManager.this.mSubFocusPosition && GridLayoutManager.this.mPendingMoveSmoothScroller == null) {
                        GridLayoutManager.this.dispatchChildSelected();
                    }
                } else if ((GridLayoutManager.this.mFlag & 4) == 0) {
                    if ((GridLayoutManager.this.mFlag & 16) == 0 && index == GridLayoutManager.this.mFocusPosition && subindex == GridLayoutManager.this.mSubFocusPosition) {
                        GridLayoutManager.this.dispatchChildSelected();
                    } else if ((GridLayoutManager.this.mFlag & 16) != 0 && index >= GridLayoutManager.this.mFocusPosition && v.hasFocusable()) {
                        GridLayoutManager.this.mFocusPosition = index;
                        GridLayoutManager.this.mSubFocusPosition = subindex;
                        GridLayoutManager gridLayoutManager = GridLayoutManager.this;
                        gridLayoutManager.mFlag &= -17;
                        GridLayoutManager.this.dispatchChildSelected();
                    }
                }
                GridLayoutManager.this.measureChild(v);
            }
            item[0] = v;
            if (GridLayoutManager.this.mOrientation == 0) {
                return GridLayoutManager.this.getDecoratedMeasuredWidthWithMargin(v);
            }
            return GridLayoutManager.this.getDecoratedMeasuredHeightWithMargin(v);
        }

        public void addItem(Object item, int index, int length, int rowIndex, int edge) {
            int start;
            int end;
            int edge2 = edge;
            View v = (View) item;
            if (edge2 == Integer.MIN_VALUE || edge2 == Integer.MAX_VALUE) {
                int size;
                if (GridLayoutManager.this.mGrid.isReversedFlow()) {
                    size = GridLayoutManager.this.mWindowAlignment.mainAxis().getSize() - GridLayoutManager.this.mWindowAlignment.mainAxis().getPaddingMax();
                } else {
                    size = GridLayoutManager.this.mWindowAlignment.mainAxis().getPaddingMin();
                }
                edge2 = size;
            }
            if (GridLayoutManager.this.mGrid.isReversedFlow() ^ 1) {
                start = edge2;
                end = edge2 + length;
            } else {
                end = edge2;
                start = edge2 - length;
            }
            int i = rowIndex;
            int startSecondary = (GridLayoutManager.this.getRowStartSecondary(i) + GridLayoutManager.this.mWindowAlignment.secondAxis().getPaddingMin()) - GridLayoutManager.this.mScrollOffsetSecondary;
            int i2 = index;
            GridLayoutManager.this.mChildrenStates.loadView(v, i2);
            GridLayoutManager.this.layoutChild(i, v, start, end, startSecondary);
            if (!GridLayoutManager.this.mState.isPreLayout()) {
                GridLayoutManager.this.updateScrollLimits();
            }
            if (!((GridLayoutManager.this.mFlag & 3) == 1 || GridLayoutManager.this.mPendingMoveSmoothScroller == null)) {
                GridLayoutManager.this.mPendingMoveSmoothScroller.consumePendingMovesAfterLayout();
            }
            if (GridLayoutManager.this.mChildLaidOutListener != null) {
                long j;
                ViewHolder vh = GridLayoutManager.this.mBaseGridView.getChildViewHolder(v);
                OnChildLaidOutListener onChildLaidOutListener = GridLayoutManager.this.mChildLaidOutListener;
                BaseGridView baseGridView = GridLayoutManager.this.mBaseGridView;
                if (vh == null) {
                    j = -1;
                } else {
                    j = vh.getItemId();
                }
                onChildLaidOutListener.onChildLaidOut(baseGridView, v, i2, j);
            }
        }

        public void removeItem(int index) {
            View v = GridLayoutManager.this.findViewByPosition(index - GridLayoutManager.this.mPositionDeltaInPreLayout);
            if ((GridLayoutManager.this.mFlag & 3) == 1) {
                GridLayoutManager.this.detachAndScrapView(v, GridLayoutManager.this.mRecycler);
            } else {
                GridLayoutManager.this.removeAndRecycleView(v, GridLayoutManager.this.mRecycler);
            }
        }

        public int getEdge(int index) {
            View v = GridLayoutManager.this.findViewByPosition(index - GridLayoutManager.this.mPositionDeltaInPreLayout);
            return (GridLayoutManager.this.mFlag & 262144) != 0 ? GridLayoutManager.this.getViewMax(v) : GridLayoutManager.this.getViewMin(v);
        }

        public int getSize(int index) {
            return GridLayoutManager.this.getViewPrimarySize(GridLayoutManager.this.findViewByPosition(index - GridLayoutManager.this.mPositionDeltaInPreLayout));
        }
    };
    private int mHorizontalSpacing;
    private final ItemAlignment mItemAlignment = new ItemAlignment();
    @VisibleForTesting
    OnLayoutCompleteListener mLayoutCompleteListener;
    int mMaxPendingMoves = 10;
    private int mMaxSizeSecondary;
    private int[] mMeasuredDimension = new int[2];
    int mNumRows;
    private int mNumRowsRequested = 1;
    int mOrientation = 0;
    private OrientationHelper mOrientationHelper = OrientationHelper.createHorizontalHelper(this);
    PendingMoveSmoothScroller mPendingMoveSmoothScroller;
    int mPositionDeltaInPreLayout;
    final SparseIntArray mPositionToRowInPostLayout = new SparseIntArray();
    private int mPrimaryScrollExtra;
    Recycler mRecycler;
    private final Runnable mRequestLayoutRunnable = new Runnable() {
        public void run() {
            GridLayoutManager.this.requestLayout();
        }
    };
    private int[] mRowSizeSecondary;
    private int mRowSizeSecondaryRequested;
    int mScrollOffsetSecondary;
    private int mSizePrimary;
    private int mSpacingPrimary;
    private int mSpacingSecondary;
    State mState;
    int mSubFocusPosition = 0;
    private int mVerticalSpacing;
    final WindowAlignment mWindowAlignment = new WindowAlignment();

    @VisibleForTesting
    public static class OnLayoutCompleteListener {
        public void onLayoutCompleted(State state) {
        }
    }

    static final class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle childStates = Bundle.EMPTY;
        int index;

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.index);
            out.writeBundle(this.childStates);
        }

        public int describeContents() {
            return 0;
        }

        SavedState(Parcel in) {
            this.index = in.readInt();
            this.childStates = in.readBundle(GridLayoutManager.class.getClassLoader());
        }

        SavedState() {
        }
    }

    static final class LayoutParams extends android.support.v7.widget.RecyclerView.LayoutParams {
        private int[] mAlignMultiple;
        private int mAlignX;
        private int mAlignY;
        private ItemAlignmentFacet mAlignmentFacet;
        int mBottomInset;
        int mLeftInset;
        int mRightInset;
        int mTopInset;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(android.support.v7.widget.RecyclerView.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((android.support.v7.widget.RecyclerView.LayoutParams) source);
        }

        /* Access modifiers changed, original: 0000 */
        public int getAlignX() {
            return this.mAlignX;
        }

        /* Access modifiers changed, original: 0000 */
        public int getAlignY() {
            return this.mAlignY;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalLeft(View view) {
            return view.getLeft() + this.mLeftInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalTop(View view) {
            return view.getTop() + this.mTopInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalRight(View view) {
            return view.getRight() - this.mRightInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalBottom(View view) {
            return view.getBottom() - this.mBottomInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalWidth(View view) {
            return (view.getWidth() - this.mLeftInset) - this.mRightInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalHeight(View view) {
            return (view.getHeight() - this.mTopInset) - this.mBottomInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalLeftInset() {
            return this.mLeftInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalRightInset() {
            return this.mRightInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalTopInset() {
            return this.mTopInset;
        }

        /* Access modifiers changed, original: 0000 */
        public int getOpticalBottomInset() {
            return this.mBottomInset;
        }

        /* Access modifiers changed, original: 0000 */
        public void setAlignX(int alignX) {
            this.mAlignX = alignX;
        }

        /* Access modifiers changed, original: 0000 */
        public void setAlignY(int alignY) {
            this.mAlignY = alignY;
        }

        /* Access modifiers changed, original: 0000 */
        public void setItemAlignmentFacet(ItemAlignmentFacet facet) {
            this.mAlignmentFacet = facet;
        }

        /* Access modifiers changed, original: 0000 */
        public ItemAlignmentFacet getItemAlignmentFacet() {
            return this.mAlignmentFacet;
        }

        /* Access modifiers changed, original: 0000 */
        public void calculateItemAlignments(int orientation, View view) {
            ItemAlignmentDef[] defs = this.mAlignmentFacet.getAlignmentDefs();
            if (this.mAlignMultiple == null || this.mAlignMultiple.length != defs.length) {
                this.mAlignMultiple = new int[defs.length];
            }
            for (int i = 0; i < defs.length; i++) {
                this.mAlignMultiple[i] = ItemAlignmentFacetHelper.getAlignmentPosition(view, defs[i], orientation);
            }
            if (orientation == 0) {
                this.mAlignX = this.mAlignMultiple[0];
            } else {
                this.mAlignY = this.mAlignMultiple[0];
            }
        }

        /* Access modifiers changed, original: 0000 */
        public int[] getAlignMultiple() {
            return this.mAlignMultiple;
        }

        /* Access modifiers changed, original: 0000 */
        public void setOpticalInsets(int leftInset, int topInset, int rightInset, int bottomInset) {
            this.mLeftInset = leftInset;
            this.mTopInset = topInset;
            this.mRightInset = rightInset;
            this.mBottomInset = bottomInset;
        }
    }

    abstract class GridLinearSmoothScroller extends LinearSmoothScroller {
        boolean mSkipOnStopInternal;

        GridLinearSmoothScroller() {
            super(GridLayoutManager.this.mBaseGridView.getContext());
        }

        /* Access modifiers changed, original: protected */
        public void onStop() {
            super.onStop();
            if (!this.mSkipOnStopInternal) {
                onStopInternal();
            }
            if (GridLayoutManager.this.mCurrentSmoothScroller == this) {
                GridLayoutManager.this.mCurrentSmoothScroller = null;
            }
            if (GridLayoutManager.this.mPendingMoveSmoothScroller == this) {
                GridLayoutManager.this.mPendingMoveSmoothScroller = null;
            }
        }

        /* Access modifiers changed, original: protected */
        public void onStopInternal() {
            View targetView = findViewByPosition(getTargetPosition());
            if (targetView == null) {
                if (getTargetPosition() >= 0) {
                    GridLayoutManager.this.scrollToSelection(getTargetPosition(), 0, false, 0);
                }
                return;
            }
            if (GridLayoutManager.this.mFocusPosition != getTargetPosition()) {
                GridLayoutManager.this.mFocusPosition = getTargetPosition();
            }
            if (GridLayoutManager.this.hasFocus()) {
                GridLayoutManager gridLayoutManager = GridLayoutManager.this;
                gridLayoutManager.mFlag |= 32;
                targetView.requestFocus();
                gridLayoutManager = GridLayoutManager.this;
                gridLayoutManager.mFlag &= -33;
            }
            GridLayoutManager.this.dispatchChildSelected();
            GridLayoutManager.this.dispatchChildSelectedAndPositioned();
        }

        /* Access modifiers changed, original: protected */
        public int calculateTimeForScrolling(int dx) {
            int ms = super.calculateTimeForScrolling(dx);
            if (GridLayoutManager.this.mWindowAlignment.mainAxis().getSize() <= 0) {
                return ms;
            }
            float minMs = (30.0f / ((float) GridLayoutManager.this.mWindowAlignment.mainAxis().getSize())) * ((float) dx);
            if (((float) ms) < minMs) {
                return (int) minMs;
            }
            return ms;
        }

        /* Access modifiers changed, original: protected */
        public void onTargetFound(View targetView, State state, Action action) {
            if (GridLayoutManager.this.getScrollPosition(targetView, null, GridLayoutManager.sTwoInts)) {
                int dx;
                int dy;
                if (GridLayoutManager.this.mOrientation == 0) {
                    dx = GridLayoutManager.sTwoInts[0];
                    dy = GridLayoutManager.sTwoInts[1];
                } else {
                    dx = GridLayoutManager.sTwoInts[1];
                    dy = GridLayoutManager.sTwoInts[0];
                }
                action.update(dx, dy, calculateTimeForDeceleration((int) Math.sqrt((double) ((dx * dx) + (dy * dy)))), this.mDecelerateInterpolator);
            }
        }
    }

    final class PendingMoveSmoothScroller extends GridLinearSmoothScroller {
        static final int TARGET_UNDEFINED = -2;
        private int mPendingMoves;
        private final boolean mStaggeredGrid;

        PendingMoveSmoothScroller(int initialPendingMoves, boolean staggeredGrid) {
            super();
            this.mPendingMoves = initialPendingMoves;
            this.mStaggeredGrid = staggeredGrid;
            setTargetPosition(-2);
        }

        /* Access modifiers changed, original: 0000 */
        public void increasePendingMoves() {
            if (this.mPendingMoves < GridLayoutManager.this.mMaxPendingMoves) {
                this.mPendingMoves++;
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void decreasePendingMoves() {
            if (this.mPendingMoves > (-GridLayoutManager.this.mMaxPendingMoves)) {
                this.mPendingMoves--;
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void consumePendingMovesBeforeLayout() {
            if (!this.mStaggeredGrid && this.mPendingMoves != 0) {
                View newSelected = null;
                int pos = this.mPendingMoves > 0 ? GridLayoutManager.this.mFocusPosition + GridLayoutManager.this.mNumRows : GridLayoutManager.this.mFocusPosition - GridLayoutManager.this.mNumRows;
                while (this.mPendingMoves != 0) {
                    View v = findViewByPosition(pos);
                    if (v == null) {
                        break;
                    }
                    if (GridLayoutManager.this.canScrollTo(v)) {
                        newSelected = v;
                        GridLayoutManager.this.mFocusPosition = pos;
                        GridLayoutManager.this.mSubFocusPosition = 0;
                        if (this.mPendingMoves > 0) {
                            this.mPendingMoves--;
                        } else {
                            this.mPendingMoves++;
                        }
                    }
                    pos = this.mPendingMoves > 0 ? GridLayoutManager.this.mNumRows + pos : pos - GridLayoutManager.this.mNumRows;
                }
                if (newSelected != null && GridLayoutManager.this.hasFocus()) {
                    GridLayoutManager gridLayoutManager = GridLayoutManager.this;
                    gridLayoutManager.mFlag |= 32;
                    newSelected.requestFocus();
                    gridLayoutManager = GridLayoutManager.this;
                    gridLayoutManager.mFlag &= -33;
                }
            }
        }

        /* Access modifiers changed, original: 0000 */
        public void consumePendingMovesAfterLayout() {
            if (this.mStaggeredGrid && this.mPendingMoves != 0) {
                this.mPendingMoves = GridLayoutManager.this.processSelectionMoves(true, this.mPendingMoves);
            }
            if (this.mPendingMoves == 0 || ((this.mPendingMoves > 0 && GridLayoutManager.this.hasCreatedLastItem()) || (this.mPendingMoves < 0 && GridLayoutManager.this.hasCreatedFirstItem()))) {
                setTargetPosition(GridLayoutManager.this.mFocusPosition);
                stop();
            }
        }

        /* Access modifiers changed, original: protected */
        public void updateActionForInterimTarget(Action action) {
            if (this.mPendingMoves != 0) {
                super.updateActionForInterimTarget(action);
            }
        }

        public PointF computeScrollVectorForPosition(int targetPosition) {
            if (this.mPendingMoves == 0) {
                return null;
            }
            int direction = ((GridLayoutManager.this.mFlag & 262144) == 0 ? this.mPendingMoves >= 0 : this.mPendingMoves <= 0) ? 1 : -1;
            if (GridLayoutManager.this.mOrientation == 0) {
                return new PointF((float) direction, 0.0f);
            }
            return new PointF(0.0f, (float) direction);
        }

        /* Access modifiers changed, original: protected */
        public void onStopInternal() {
            super.onStopInternal();
            this.mPendingMoves = 0;
            View v = findViewByPosition(getTargetPosition());
            if (v != null) {
                GridLayoutManager.this.scrollToView(v, true);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public String getTag() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("GridLayoutManager:");
        stringBuilder.append(this.mBaseGridView.getId());
        return stringBuilder.toString();
    }

    public GridLayoutManager(BaseGridView baseGridView) {
        this.mBaseGridView = baseGridView;
        this.mChildVisibility = -1;
        setItemPrefetchEnabled(false);
    }

    public void setOrientation(int orientation) {
        if (orientation == 0 || orientation == 1) {
            this.mOrientation = orientation;
            this.mOrientationHelper = OrientationHelper.createOrientationHelper(this, this.mOrientation);
            this.mWindowAlignment.setOrientation(orientation);
            this.mItemAlignment.setOrientation(orientation);
            this.mFlag |= 256;
        }
    }

    public void onRtlPropertiesChanged(int layoutDirection) {
        boolean z = false;
        int flags = this.mOrientation == 0 ? layoutDirection == 1 ? 262144 : 0 : layoutDirection == 1 ? 524288 : 0;
        if ((this.mFlag & PF_REVERSE_FLOW_MASK) != flags) {
            this.mFlag = (this.mFlag & -786433) | flags;
            this.mFlag |= 256;
            Axis axis = this.mWindowAlignment.horizontal;
            if (layoutDirection == 1) {
                z = true;
            }
            axis.setReversedFlow(z);
        }
    }

    public int getFocusScrollStrategy() {
        return this.mFocusScrollStrategy;
    }

    public void setFocusScrollStrategy(int focusScrollStrategy) {
        this.mFocusScrollStrategy = focusScrollStrategy;
    }

    public void setWindowAlignment(int windowAlignment) {
        this.mWindowAlignment.mainAxis().setWindowAlignment(windowAlignment);
    }

    public int getWindowAlignment() {
        return this.mWindowAlignment.mainAxis().getWindowAlignment();
    }

    public void setWindowAlignmentOffset(int alignmentOffset) {
        this.mWindowAlignment.mainAxis().setWindowAlignmentOffset(alignmentOffset);
    }

    public int getWindowAlignmentOffset() {
        return this.mWindowAlignment.mainAxis().getWindowAlignmentOffset();
    }

    public void setWindowAlignmentOffsetPercent(float offsetPercent) {
        this.mWindowAlignment.mainAxis().setWindowAlignmentOffsetPercent(offsetPercent);
    }

    public float getWindowAlignmentOffsetPercent() {
        return this.mWindowAlignment.mainAxis().getWindowAlignmentOffsetPercent();
    }

    public void setItemAlignmentOffset(int alignmentOffset) {
        this.mItemAlignment.mainAxis().setItemAlignmentOffset(alignmentOffset);
        updateChildAlignments();
    }

    public int getItemAlignmentOffset() {
        return this.mItemAlignment.mainAxis().getItemAlignmentOffset();
    }

    public void setItemAlignmentOffsetWithPadding(boolean withPadding) {
        this.mItemAlignment.mainAxis().setItemAlignmentOffsetWithPadding(withPadding);
        updateChildAlignments();
    }

    public boolean isItemAlignmentOffsetWithPadding() {
        return this.mItemAlignment.mainAxis().isItemAlignmentOffsetWithPadding();
    }

    public void setItemAlignmentOffsetPercent(float offsetPercent) {
        this.mItemAlignment.mainAxis().setItemAlignmentOffsetPercent(offsetPercent);
        updateChildAlignments();
    }

    public float getItemAlignmentOffsetPercent() {
        return this.mItemAlignment.mainAxis().getItemAlignmentOffsetPercent();
    }

    public void setItemAlignmentViewId(int viewId) {
        this.mItemAlignment.mainAxis().setItemAlignmentViewId(viewId);
        updateChildAlignments();
    }

    public int getItemAlignmentViewId() {
        return this.mItemAlignment.mainAxis().getItemAlignmentViewId();
    }

    public void setFocusOutAllowed(boolean throughFront, boolean throughEnd) {
        int i = 0;
        int i2 = (this.mFlag & -6145) | (throughFront ? 2048 : 0);
        if (throughEnd) {
            i = 4096;
        }
        this.mFlag = i2 | i;
    }

    public void setFocusOutSideAllowed(boolean throughStart, boolean throughEnd) {
        int i = 0;
        int i2 = (this.mFlag & -24577) | (throughStart ? 8192 : 0);
        if (throughEnd) {
            i = 16384;
        }
        this.mFlag = i2 | i;
    }

    public void setNumRows(int numRows) {
        if (numRows >= 0) {
            this.mNumRowsRequested = numRows;
            return;
        }
        throw new IllegalArgumentException();
    }

    public void setRowHeight(int height) {
        if (height >= 0 || height == -2) {
            this.mRowSizeSecondaryRequested = height;
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid row height: ");
        stringBuilder.append(height);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public void setItemSpacing(int space) {
        this.mHorizontalSpacing = space;
        this.mVerticalSpacing = space;
        this.mSpacingSecondary = space;
        this.mSpacingPrimary = space;
    }

    public void setVerticalSpacing(int space) {
        if (this.mOrientation == 1) {
            this.mVerticalSpacing = space;
            this.mSpacingPrimary = space;
            return;
        }
        this.mVerticalSpacing = space;
        this.mSpacingSecondary = space;
    }

    public void setHorizontalSpacing(int space) {
        if (this.mOrientation == 0) {
            this.mHorizontalSpacing = space;
            this.mSpacingPrimary = space;
            return;
        }
        this.mHorizontalSpacing = space;
        this.mSpacingSecondary = space;
    }

    public int getVerticalSpacing() {
        return this.mVerticalSpacing;
    }

    public int getHorizontalSpacing() {
        return this.mHorizontalSpacing;
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    /* Access modifiers changed, original: protected */
    public boolean hasDoneFirstLayout() {
        return this.mGrid != null;
    }

    public void setOnChildSelectedListener(OnChildSelectedListener listener) {
        this.mChildSelectedListener = listener;
    }

    public void setOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        if (listener == null) {
            this.mChildViewHolderSelectedListeners = null;
            return;
        }
        if (this.mChildViewHolderSelectedListeners == null) {
            this.mChildViewHolderSelectedListeners = new ArrayList();
        } else {
            this.mChildViewHolderSelectedListeners.clear();
        }
        this.mChildViewHolderSelectedListeners.add(listener);
    }

    public void addOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        if (this.mChildViewHolderSelectedListeners == null) {
            this.mChildViewHolderSelectedListeners = new ArrayList();
        }
        this.mChildViewHolderSelectedListeners.add(listener);
    }

    public void removeOnChildViewHolderSelectedListener(OnChildViewHolderSelectedListener listener) {
        if (this.mChildViewHolderSelectedListeners != null) {
            this.mChildViewHolderSelectedListeners.remove(listener);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasOnChildViewHolderSelectedListener() {
        return this.mChildViewHolderSelectedListeners != null && this.mChildViewHolderSelectedListeners.size() > 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void fireOnChildViewHolderSelected(RecyclerView parent, ViewHolder child, int position, int subposition) {
        if (this.mChildViewHolderSelectedListeners != null) {
            for (int i = this.mChildViewHolderSelectedListeners.size() - 1; i >= 0; i--) {
                ((OnChildViewHolderSelectedListener) this.mChildViewHolderSelectedListeners.get(i)).onChildViewHolderSelected(parent, child, position, subposition);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void fireOnChildViewHolderSelectedAndPositioned(RecyclerView parent, ViewHolder child, int position, int subposition) {
        if (this.mChildViewHolderSelectedListeners != null) {
            for (int i = this.mChildViewHolderSelectedListeners.size() - 1; i >= 0; i--) {
                ((OnChildViewHolderSelectedListener) this.mChildViewHolderSelectedListeners.get(i)).onChildViewHolderSelectedAndPositioned(parent, child, position, subposition);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setOnChildLaidOutListener(OnChildLaidOutListener listener) {
        this.mChildLaidOutListener = listener;
    }

    private int getAdapterPositionByView(View view) {
        if (view == null) {
            return -1;
        }
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (params == null || params.isItemRemoved()) {
            return -1;
        }
        return params.getViewAdapterPosition();
    }

    /* Access modifiers changed, original: 0000 */
    public int getSubPositionByView(View view, View childView) {
        if (view == null || childView == null) {
            return 0;
        }
        ItemAlignmentFacet facet = ((LayoutParams) view.getLayoutParams()).getItemAlignmentFacet();
        if (facet != null) {
            ItemAlignmentDef[] defs = facet.getAlignmentDefs();
            if (defs.length > 1) {
                while (childView != view) {
                    int id = childView.getId();
                    if (id != -1) {
                        for (int i = 1; i < defs.length; i++) {
                            if (defs[i].getItemAlignmentFocusViewId() == id) {
                                return i;
                            }
                        }
                        continue;
                    }
                    childView = (View) childView.getParent();
                }
            }
        }
        return 0;
    }

    private int getAdapterPositionByIndex(int index) {
        return getAdapterPositionByView(getChildAt(index));
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchChildSelected() {
        if (this.mChildSelectedListener != null || hasOnChildViewHolderSelectedListener()) {
            View view = this.mFocusPosition == -1 ? null : findViewByPosition(this.mFocusPosition);
            int i = 0;
            if (view != null) {
                ViewHolder vh = this.mBaseGridView.getChildViewHolder(view);
                if (this.mChildSelectedListener != null) {
                    long j;
                    OnChildSelectedListener onChildSelectedListener = this.mChildSelectedListener;
                    BaseGridView baseGridView = this.mBaseGridView;
                    int i2 = this.mFocusPosition;
                    if (vh == null) {
                        j = -1;
                    } else {
                        j = vh.getItemId();
                    }
                    onChildSelectedListener.onChildSelected(baseGridView, view, i2, j);
                }
                fireOnChildViewHolderSelected(this.mBaseGridView, vh, this.mFocusPosition, this.mSubFocusPosition);
            } else {
                if (this.mChildSelectedListener != null) {
                    this.mChildSelectedListener.onChildSelected(this.mBaseGridView, null, -1, -1);
                }
                fireOnChildViewHolderSelected(this.mBaseGridView, null, -1, 0);
            }
            if ((this.mFlag & 3) != 1 && !this.mBaseGridView.isLayoutRequested()) {
                int childCount = getChildCount();
                while (true) {
                    int i3 = i;
                    if (i3 >= childCount) {
                        break;
                    } else if (getChildAt(i3).isLayoutRequested()) {
                        forceRequestLayout();
                        break;
                    } else {
                        i = i3 + 1;
                    }
                }
            }
        }
    }

    private void dispatchChildSelectedAndPositioned() {
        if (hasOnChildViewHolderSelectedListener()) {
            View view = this.mFocusPosition == -1 ? null : findViewByPosition(this.mFocusPosition);
            if (view != null) {
                fireOnChildViewHolderSelectedAndPositioned(this.mBaseGridView, this.mBaseGridView.getChildViewHolder(view), this.mFocusPosition, this.mSubFocusPosition);
            } else {
                if (this.mChildSelectedListener != null) {
                    this.mChildSelectedListener.onChildSelected(this.mBaseGridView, null, -1, -1);
                }
                fireOnChildViewHolderSelectedAndPositioned(this.mBaseGridView, null, -1, 0);
            }
        }
    }

    public boolean canScrollHorizontally() {
        return this.mOrientation == 0 || this.mNumRows > 1;
    }

    public boolean canScrollVertically() {
        return this.mOrientation == 1 || this.mNumRows > 1;
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateLayoutParams(Context context, AttributeSet attrs) {
        return new LayoutParams(context, attrs);
    }

    public android.support.v7.widget.RecyclerView.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        if (lp instanceof android.support.v7.widget.RecyclerView.LayoutParams) {
            return new LayoutParams((android.support.v7.widget.RecyclerView.LayoutParams) lp);
        }
        if (lp instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) lp);
        }
        return new LayoutParams(lp);
    }

    /* Access modifiers changed, original: protected */
    public View getViewForPosition(int position) {
        return this.mRecycler.getViewForPosition(position);
    }

    /* Access modifiers changed, original: final */
    public final int getOpticalLeft(View v) {
        return ((LayoutParams) v.getLayoutParams()).getOpticalLeft(v);
    }

    /* Access modifiers changed, original: final */
    public final int getOpticalRight(View v) {
        return ((LayoutParams) v.getLayoutParams()).getOpticalRight(v);
    }

    /* Access modifiers changed, original: final */
    public final int getOpticalTop(View v) {
        return ((LayoutParams) v.getLayoutParams()).getOpticalTop(v);
    }

    /* Access modifiers changed, original: final */
    public final int getOpticalBottom(View v) {
        return ((LayoutParams) v.getLayoutParams()).getOpticalBottom(v);
    }

    public int getDecoratedLeft(View child) {
        return super.getDecoratedLeft(child) + ((LayoutParams) child.getLayoutParams()).mLeftInset;
    }

    public int getDecoratedTop(View child) {
        return super.getDecoratedTop(child) + ((LayoutParams) child.getLayoutParams()).mTopInset;
    }

    public int getDecoratedRight(View child) {
        return super.getDecoratedRight(child) - ((LayoutParams) child.getLayoutParams()).mRightInset;
    }

    public int getDecoratedBottom(View child) {
        return super.getDecoratedBottom(child) - ((LayoutParams) child.getLayoutParams()).mBottomInset;
    }

    public void getDecoratedBoundsWithMargins(View view, Rect outBounds) {
        super.getDecoratedBoundsWithMargins(view, outBounds);
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        outBounds.left += params.mLeftInset;
        outBounds.top += params.mTopInset;
        outBounds.right -= params.mRightInset;
        outBounds.bottom -= params.mBottomInset;
    }

    /* Access modifiers changed, original: 0000 */
    public int getViewMin(View v) {
        return this.mOrientationHelper.getDecoratedStart(v);
    }

    /* Access modifiers changed, original: 0000 */
    public int getViewMax(View v) {
        return this.mOrientationHelper.getDecoratedEnd(v);
    }

    /* Access modifiers changed, original: 0000 */
    public int getViewPrimarySize(View view) {
        getDecoratedBoundsWithMargins(view, sTempRect);
        return this.mOrientation == 0 ? sTempRect.width() : sTempRect.height();
    }

    private int getViewCenter(View view) {
        return this.mOrientation == 0 ? getViewCenterX(view) : getViewCenterY(view);
    }

    private int getAdjustedViewCenter(View view) {
        if (view.hasFocus()) {
            View child = view.findFocus();
            if (!(child == null || child == view)) {
                return getAdjustedPrimaryAlignedScrollDistance(getViewCenter(view), view, child);
            }
        }
        return getViewCenter(view);
    }

    private int getViewCenterSecondary(View view) {
        return this.mOrientation == 0 ? getViewCenterY(view) : getViewCenterX(view);
    }

    private int getViewCenterX(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        return p.getOpticalLeft(v) + p.getAlignX();
    }

    private int getViewCenterY(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        return p.getOpticalTop(v) + p.getAlignY();
    }

    private void saveContext(Recycler recycler, State state) {
        if (!(this.mRecycler == null && this.mState == null)) {
            Log.e(TAG, "Recycler information was not released, bug!");
        }
        this.mRecycler = recycler;
        this.mState = state;
        this.mPositionDeltaInPreLayout = 0;
        this.mExtraLayoutSpaceInPreLayout = 0;
    }

    private void leaveContext() {
        this.mRecycler = null;
        this.mState = null;
        this.mPositionDeltaInPreLayout = 0;
        this.mExtraLayoutSpaceInPreLayout = 0;
    }

    /* JADX WARNING: Missing block: B:31:0x007b, code skipped:
            if (((r6.mFlag & 262144) != 0) != r6.mGrid.isReversedFlow()) goto L_0x007d;
     */
    private boolean layoutInit() {
        /*
        r6 = this;
        r0 = r6.mState;
        r0 = r0.getItemCount();
        r1 = -1;
        r2 = 0;
        if (r0 != 0) goto L_0x000f;
    L_0x000a:
        r6.mFocusPosition = r1;
        r6.mSubFocusPosition = r2;
        goto L_0x0024;
    L_0x000f:
        r3 = r6.mFocusPosition;
        if (r3 < r0) goto L_0x001a;
    L_0x0013:
        r1 = r0 + -1;
        r6.mFocusPosition = r1;
        r6.mSubFocusPosition = r2;
        goto L_0x0024;
    L_0x001a:
        r3 = r6.mFocusPosition;
        if (r3 != r1) goto L_0x0024;
    L_0x001e:
        if (r0 <= 0) goto L_0x0024;
    L_0x0020:
        r6.mFocusPosition = r2;
        r6.mSubFocusPosition = r2;
    L_0x0024:
        r1 = r6.mState;
        r1 = r1.didStructureChange();
        r3 = 1;
        if (r1 != 0) goto L_0x0057;
    L_0x002d:
        r1 = r6.mGrid;
        if (r1 == 0) goto L_0x0057;
    L_0x0031:
        r1 = r6.mGrid;
        r1 = r1.getFirstVisibleIndex();
        if (r1 < 0) goto L_0x0057;
    L_0x0039:
        r1 = r6.mFlag;
        r1 = r1 & 256;
        if (r1 != 0) goto L_0x0057;
    L_0x003f:
        r1 = r6.mGrid;
        r1 = r1.getNumRows();
        r4 = r6.mNumRows;
        if (r1 != r4) goto L_0x0057;
    L_0x0049:
        r6.updateScrollController();
        r6.updateSecondaryScrollLimits();
        r1 = r6.mGrid;
        r2 = r6.mSpacingPrimary;
        r1.setSpacing(r2);
        return r3;
    L_0x0057:
        r1 = r6.mFlag;
        r1 = r1 & -257;
        r6.mFlag = r1;
        r1 = r6.mGrid;
        r4 = 262144; // 0x40000 float:3.67342E-40 double:1.295163E-318;
        if (r1 == 0) goto L_0x007d;
    L_0x0063:
        r1 = r6.mNumRows;
        r5 = r6.mGrid;
        r5 = r5.getNumRows();
        if (r1 != r5) goto L_0x007d;
    L_0x006d:
        r1 = r6.mFlag;
        r1 = r1 & r4;
        if (r1 == 0) goto L_0x0074;
    L_0x0072:
        r1 = r3;
        goto L_0x0075;
    L_0x0074:
        r1 = r2;
    L_0x0075:
        r5 = r6.mGrid;
        r5 = r5.isReversedFlow();
        if (r1 == r5) goto L_0x0098;
    L_0x007d:
        r1 = r6.mNumRows;
        r1 = android.support.v17.leanback.widget.Grid.createGrid(r1);
        r6.mGrid = r1;
        r1 = r6.mGrid;
        r5 = r6.mGridProvider;
        r1.setProvider(r5);
        r1 = r6.mGrid;
        r5 = r6.mFlag;
        r4 = r4 & r5;
        if (r4 == 0) goto L_0x0094;
    L_0x0093:
        goto L_0x0095;
    L_0x0094:
        r3 = r2;
    L_0x0095:
        r1.setReversedFlow(r3);
    L_0x0098:
        r6.initScrollController();
        r6.updateSecondaryScrollLimits();
        r1 = r6.mGrid;
        r3 = r6.mSpacingPrimary;
        r1.setSpacing(r3);
        r1 = r6.mRecycler;
        r6.detachAndScrapAttachedViews(r1);
        r1 = r6.mGrid;
        r1.resetVisibleIndex();
        r1 = r6.mWindowAlignment;
        r1 = r1.mainAxis();
        r1.invalidateScrollMin();
        r1 = r6.mWindowAlignment;
        r1 = r1.mainAxis();
        r1.invalidateScrollMax();
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.widget.GridLayoutManager.layoutInit():boolean");
    }

    private int getRowSizeSecondary(int rowIndex) {
        if (this.mFixedRowSizeSecondary != 0) {
            return this.mFixedRowSizeSecondary;
        }
        if (this.mRowSizeSecondary == null) {
            return 0;
        }
        return this.mRowSizeSecondary[rowIndex];
    }

    /* Access modifiers changed, original: 0000 */
    public int getRowStartSecondary(int rowIndex) {
        int start = 0;
        int i;
        if ((this.mFlag & 524288) != 0) {
            for (i = this.mNumRows - 1; i > rowIndex; i--) {
                start += getRowSizeSecondary(i) + this.mSpacingSecondary;
            }
        } else {
            for (i = 0; i < rowIndex; i++) {
                start += getRowSizeSecondary(i) + this.mSpacingSecondary;
            }
        }
        return start;
    }

    private int getSizeSecondary() {
        int rightmostIndex = (this.mFlag & 524288) != 0 ? 0 : this.mNumRows - 1;
        return getRowStartSecondary(rightmostIndex) + getRowSizeSecondary(rightmostIndex);
    }

    /* Access modifiers changed, original: 0000 */
    public int getDecoratedMeasuredWidthWithMargin(View v) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        return (getDecoratedMeasuredWidth(v) + lp.leftMargin) + lp.rightMargin;
    }

    /* Access modifiers changed, original: 0000 */
    public int getDecoratedMeasuredHeightWithMargin(View v) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        return (getDecoratedMeasuredHeight(v) + lp.topMargin) + lp.bottomMargin;
    }

    private void measureScrapChild(int position, int widthSpec, int heightSpec, int[] measuredDimension) {
        View view = this.mRecycler.getViewForPosition(position);
        if (view != null) {
            LayoutParams p = (LayoutParams) view.getLayoutParams();
            calculateItemDecorationsForChild(view, sTempRect);
            view.measure(ViewGroup.getChildMeasureSpec(widthSpec, (getPaddingLeft() + getPaddingRight()) + (((p.leftMargin + p.rightMargin) + sTempRect.left) + sTempRect.right), p.width), ViewGroup.getChildMeasureSpec(heightSpec, (getPaddingTop() + getPaddingBottom()) + (((p.topMargin + p.bottomMargin) + sTempRect.top) + sTempRect.bottom), p.height));
            measuredDimension[0] = getDecoratedMeasuredWidthWithMargin(view);
            measuredDimension[1] = getDecoratedMeasuredHeightWithMargin(view);
            this.mRecycler.recycleView(view);
        }
    }

    private boolean processRowSizeSecondary(boolean measure) {
        if (this.mFixedRowSizeSecondary != 0 || this.mRowSizeSecondary == null) {
            return false;
        }
        CircularIntArray[] rows = this.mGrid == null ? null : this.mGrid.getItemPositionsInRows();
        int scrapeChildSize = -1;
        boolean changed = false;
        int rowIndex = 0;
        while (rowIndex < this.mNumRows) {
            int rowIndexStart;
            int rowSize;
            int secondarySize;
            CircularIntArray row = rows == null ? null : rows[rowIndex];
            int rowItemsPairCount = row == null ? 0 : row.size();
            int rowSize2 = -1;
            int rowItemPairIndex = 0;
            while (rowItemPairIndex < rowItemsPairCount) {
                rowIndexStart = row.get(rowItemPairIndex);
                int rowIndexEnd = row.get(rowItemPairIndex + 1);
                rowSize = rowSize2;
                for (rowSize2 = rowIndexStart; rowSize2 <= rowIndexEnd; rowSize2++) {
                    View view = findViewByPosition(rowSize2 - this.mPositionDeltaInPreLayout);
                    if (view != null) {
                        if (measure) {
                            measureChild(view);
                        }
                        if (this.mOrientation == 0) {
                            secondarySize = getDecoratedMeasuredHeightWithMargin(view);
                        } else {
                            secondarySize = getDecoratedMeasuredWidthWithMargin(view);
                        }
                        if (secondarySize > rowSize) {
                            rowSize = secondarySize;
                        }
                    }
                }
                rowItemPairIndex += 2;
                rowSize2 = rowSize;
            }
            secondarySize = this.mState.getItemCount();
            if (!this.mBaseGridView.hasFixedSize() && measure && rowSize2 < 0 && secondarySize > 0) {
                if (scrapeChildSize < 0) {
                    rowItemPairIndex = this.mFocusPosition;
                    if (rowItemPairIndex < 0) {
                        rowItemPairIndex = 0;
                    } else if (rowItemPairIndex >= secondarySize) {
                        rowItemPairIndex = secondarySize - 1;
                    }
                    if (getChildCount() > 0) {
                        rowIndexStart = this.mBaseGridView.getChildViewHolder(getChildAt(0)).getLayoutPosition();
                        rowSize = this.mBaseGridView.getChildViewHolder(getChildAt(getChildCount() - 1)).getLayoutPosition();
                        if (rowItemPairIndex >= rowIndexStart && rowItemPairIndex <= rowSize) {
                            rowItemPairIndex = rowItemPairIndex - rowIndexStart <= rowSize - rowItemPairIndex ? rowIndexStart - 1 : rowSize + 1;
                            if (rowItemPairIndex < 0 && rowSize < secondarySize - 1) {
                                rowItemPairIndex = rowSize + 1;
                            } else if (rowItemPairIndex >= secondarySize && rowIndexStart > 0) {
                                rowItemPairIndex = rowIndexStart - 1;
                            }
                        }
                    }
                    if (rowItemPairIndex >= 0 && rowItemPairIndex < secondarySize) {
                        measureScrapChild(rowItemPairIndex, MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0), this.mMeasuredDimension);
                        scrapeChildSize = this.mOrientation == 0 ? this.mMeasuredDimension[1] : this.mMeasuredDimension[0];
                    }
                }
                if (scrapeChildSize >= 0) {
                    rowSize2 = scrapeChildSize;
                }
            }
            if (rowSize2 < 0) {
                rowSize2 = 0;
            }
            if (this.mRowSizeSecondary[rowIndex] != rowSize2) {
                this.mRowSizeSecondary[rowIndex] = rowSize2;
                changed = true;
            }
            rowIndex++;
        }
        return changed;
    }

    private void updateRowSecondarySizeRefresh() {
        int i = this.mFlag & -1025;
        int i2 = 0;
        if (processRowSizeSecondary(false)) {
            i2 = 1024;
        }
        this.mFlag = i | i2;
        if ((this.mFlag & 1024) != 0) {
            forceRequestLayout();
        }
    }

    private void forceRequestLayout() {
        ViewCompat.postOnAnimation(this.mBaseGridView, this.mRequestLayoutRunnable);
    }

    public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
        int sizePrimary;
        int sizeSecondary;
        int modeSecondary;
        int paddingSecondary;
        int measuredSizeSecondary;
        saveContext(recycler, state);
        if (this.mOrientation == 0) {
            sizePrimary = MeasureSpec.getSize(widthSpec);
            sizeSecondary = MeasureSpec.getSize(heightSpec);
            modeSecondary = MeasureSpec.getMode(heightSpec);
            paddingSecondary = getPaddingTop() + getPaddingBottom();
        } else {
            sizeSecondary = MeasureSpec.getSize(widthSpec);
            sizePrimary = MeasureSpec.getSize(heightSpec);
            modeSecondary = MeasureSpec.getMode(widthSpec);
            paddingSecondary = getPaddingLeft() + getPaddingRight();
        }
        this.mMaxSizeSecondary = sizeSecondary;
        if (this.mRowSizeSecondaryRequested == -2) {
            this.mNumRows = this.mNumRowsRequested == 0 ? 1 : this.mNumRowsRequested;
            this.mFixedRowSizeSecondary = 0;
            if (this.mRowSizeSecondary == null || this.mRowSizeSecondary.length != this.mNumRows) {
                this.mRowSizeSecondary = new int[this.mNumRows];
            }
            if (this.mState.isPreLayout()) {
                updatePositionDeltaInPreLayout();
            }
            processRowSizeSecondary(true);
            if (modeSecondary == Integer.MIN_VALUE) {
                measuredSizeSecondary = Math.min(getSizeSecondary() + paddingSecondary, this.mMaxSizeSecondary);
            } else if (modeSecondary == 0) {
                measuredSizeSecondary = getSizeSecondary() + paddingSecondary;
            } else if (modeSecondary == Ints.MAX_POWER_OF_TWO) {
                measuredSizeSecondary = this.mMaxSizeSecondary;
            } else {
                throw new IllegalStateException("wrong spec");
            }
        }
        if (modeSecondary != Integer.MIN_VALUE) {
            if (modeSecondary == 0) {
                this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested == 0 ? sizeSecondary - paddingSecondary : this.mRowSizeSecondaryRequested;
                this.mNumRows = this.mNumRowsRequested == 0 ? 1 : this.mNumRowsRequested;
                measuredSizeSecondary = ((this.mFixedRowSizeSecondary * this.mNumRows) + (this.mSpacingSecondary * (this.mNumRows - 1))) + paddingSecondary;
            } else if (modeSecondary != Ints.MAX_POWER_OF_TWO) {
                throw new IllegalStateException("wrong spec");
            }
        }
        if (this.mNumRowsRequested == 0 && this.mRowSizeSecondaryRequested == 0) {
            this.mNumRows = 1;
            this.mFixedRowSizeSecondary = sizeSecondary - paddingSecondary;
        } else if (this.mNumRowsRequested == 0) {
            this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested;
            this.mNumRows = (this.mSpacingSecondary + sizeSecondary) / (this.mRowSizeSecondaryRequested + this.mSpacingSecondary);
        } else if (this.mRowSizeSecondaryRequested == 0) {
            this.mNumRows = this.mNumRowsRequested;
            this.mFixedRowSizeSecondary = ((sizeSecondary - paddingSecondary) - (this.mSpacingSecondary * (this.mNumRows - 1))) / this.mNumRows;
        } else {
            this.mNumRows = this.mNumRowsRequested;
            this.mFixedRowSizeSecondary = this.mRowSizeSecondaryRequested;
        }
        measuredSizeSecondary = sizeSecondary;
        if (modeSecondary == Integer.MIN_VALUE) {
            int childrenSize = ((this.mFixedRowSizeSecondary * this.mNumRows) + (this.mSpacingSecondary * (this.mNumRows - 1))) + paddingSecondary;
            if (childrenSize < measuredSizeSecondary) {
                measuredSizeSecondary = childrenSize;
            }
        }
        if (this.mOrientation == 0) {
            setMeasuredDimension(sizePrimary, measuredSizeSecondary);
        } else {
            setMeasuredDimension(measuredSizeSecondary, sizePrimary);
        }
        leaveContext();
    }

    /* Access modifiers changed, original: 0000 */
    public void measureChild(View child) {
        int secondarySpec;
        int widthSpec;
        int heightSpec;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        calculateItemDecorationsForChild(child, sTempRect);
        int widthUsed = ((lp.leftMargin + lp.rightMargin) + sTempRect.left) + sTempRect.right;
        int heightUsed = ((lp.topMargin + lp.bottomMargin) + sTempRect.top) + sTempRect.bottom;
        if (this.mRowSizeSecondaryRequested == -2) {
            secondarySpec = MeasureSpec.makeMeasureSpec(0, 0);
        } else {
            secondarySpec = MeasureSpec.makeMeasureSpec(this.mFixedRowSizeSecondary, Ints.MAX_POWER_OF_TWO);
        }
        if (this.mOrientation == 0) {
            widthSpec = ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), widthUsed, lp.width);
            heightSpec = ViewGroup.getChildMeasureSpec(secondarySpec, heightUsed, lp.height);
        } else {
            heightSpec = ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), heightUsed, lp.height);
            widthSpec = ViewGroup.getChildMeasureSpec(secondarySpec, widthUsed, lp.width);
        }
        child.measure(widthSpec, heightSpec);
    }

    /* Access modifiers changed, original: 0000 */
    public <E> E getFacet(ViewHolder vh, Class<? extends E> facetClass) {
        E facet = null;
        if (vh instanceof FacetProvider) {
            facet = ((FacetProvider) vh).getFacet(facetClass);
        }
        if (facet != null || this.mFacetProviderAdapter == null) {
            return facet;
        }
        FacetProvider p = this.mFacetProviderAdapter.getFacetProvider(vh.getItemViewType());
        if (p != null) {
            return p.getFacet(facetClass);
        }
        return facet;
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x007e  */
    public void layoutChild(int r18, android.view.View r19, int r20, int r21, int r22) {
        /*
        r17 = this;
        r6 = r17;
        r7 = r19;
        r1 = r6.mOrientation;
        if (r1 != 0) goto L_0x000d;
    L_0x0008:
        r1 = r6.getDecoratedMeasuredHeightWithMargin(r7);
        goto L_0x0011;
    L_0x000d:
        r1 = r6.getDecoratedMeasuredWidthWithMargin(r7);
    L_0x0011:
        r2 = r6.mFixedRowSizeSecondary;
        if (r2 <= 0) goto L_0x001b;
    L_0x0015:
        r2 = r6.mFixedRowSizeSecondary;
        r1 = java.lang.Math.min(r1, r2);
    L_0x001b:
        r8 = r1;
        r1 = r6.mGravity;
        r9 = r1 & 112;
        r1 = r6.mFlag;
        r2 = 786432; // 0xc0000 float:1.102026E-39 double:3.88549E-318;
        r1 = r1 & r2;
        r2 = 1;
        if (r1 == 0) goto L_0x0033;
    L_0x0028:
        r1 = r6.mGravity;
        r3 = 8388615; // 0x800007 float:1.1754953E-38 double:4.1445265E-317;
        r1 = r1 & r3;
        r1 = android.view.Gravity.getAbsoluteGravity(r1, r2);
        goto L_0x0037;
    L_0x0033:
        r1 = r6.mGravity;
        r1 = r1 & 7;
    L_0x0037:
        r10 = r1;
        r1 = r6.mOrientation;
        if (r1 != 0) goto L_0x0040;
    L_0x003c:
        r1 = 48;
        if (r9 == r1) goto L_0x0078;
    L_0x0040:
        r1 = r6.mOrientation;
        if (r1 != r2) goto L_0x0048;
    L_0x0044:
        r1 = 3;
        if (r10 != r1) goto L_0x0048;
    L_0x0047:
        goto L_0x0078;
    L_0x0048:
        r1 = r6.mOrientation;
        if (r1 != 0) goto L_0x0050;
    L_0x004c:
        r1 = 80;
        if (r9 == r1) goto L_0x0057;
    L_0x0050:
        r1 = r6.mOrientation;
        if (r1 != r2) goto L_0x0060;
    L_0x0054:
        r1 = 5;
        if (r10 != r1) goto L_0x0060;
    L_0x0057:
        r1 = r17.getRowSizeSecondary(r18);
        r1 = r1 - r8;
        r0 = r22 + r1;
    L_0x005e:
        r11 = r0;
        goto L_0x007a;
    L_0x0060:
        r1 = r6.mOrientation;
        if (r1 != 0) goto L_0x0068;
    L_0x0064:
        r1 = 16;
        if (r9 == r1) goto L_0x006e;
    L_0x0068:
        r1 = r6.mOrientation;
        if (r1 != r2) goto L_0x0078;
    L_0x006c:
        if (r10 != r2) goto L_0x0078;
    L_0x006e:
        r1 = r17.getRowSizeSecondary(r18);
        r1 = r1 - r8;
        r1 = r1 / 2;
        r0 = r22 + r1;
        goto L_0x005e;
    L_0x0078:
        r11 = r22;
    L_0x007a:
        r0 = r6.mOrientation;
        if (r0 != 0) goto L_0x0089;
    L_0x007e:
        r0 = r20;
        r1 = r11;
        r2 = r21;
        r3 = r11 + r8;
        r12 = r0;
        r13 = r1;
        r14 = r3;
        goto L_0x0094;
    L_0x0089:
        r0 = r20;
        r1 = r11;
        r2 = r21;
        r3 = r11 + r8;
        r13 = r0;
        r12 = r1;
        r14 = r2;
        r2 = r3;
    L_0x0094:
        r15 = r2;
        r0 = r19.getLayoutParams();
        r5 = r0;
        r5 = (android.support.v17.leanback.widget.GridLayoutManager.LayoutParams) r5;
        r0 = r6;
        r1 = r7;
        r2 = r12;
        r3 = r13;
        r4 = r15;
        r16 = r8;
        r8 = r5;
        r5 = r14;
        r0.layoutDecoratedWithMargins(r1, r2, r3, r4, r5);
        r0 = sTempRect;
        super.getDecoratedBoundsWithMargins(r7, r0);
        r0 = sTempRect;
        r0 = r0.left;
        r0 = r12 - r0;
        r1 = sTempRect;
        r1 = r1.top;
        r1 = r13 - r1;
        r2 = sTempRect;
        r2 = r2.right;
        r2 = r2 - r15;
        r3 = sTempRect;
        r3 = r3.bottom;
        r3 = r3 - r14;
        r8.setOpticalInsets(r0, r1, r2, r3);
        r6.updateChildAlignments(r7);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.widget.GridLayoutManager.layoutChild(int, android.view.View, int, int, int):void");
    }

    private void updateChildAlignments(View v) {
        LayoutParams p = (LayoutParams) v.getLayoutParams();
        if (p.getItemAlignmentFacet() == null) {
            p.setAlignX(this.mItemAlignment.horizontal.getAlignmentPosition(v));
            p.setAlignY(this.mItemAlignment.vertical.getAlignmentPosition(v));
            return;
        }
        p.calculateItemAlignments(this.mOrientation, v);
        if (this.mOrientation == 0) {
            p.setAlignY(this.mItemAlignment.vertical.getAlignmentPosition(v));
        } else {
            p.setAlignX(this.mItemAlignment.horizontal.getAlignmentPosition(v));
        }
    }

    private void updateChildAlignments() {
        int c = getChildCount();
        for (int i = 0; i < c; i++) {
            updateChildAlignments(getChildAt(i));
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setExtraLayoutSpace(int extraLayoutSpace) {
        if (this.mExtraLayoutSpace != extraLayoutSpace) {
            if (this.mExtraLayoutSpace >= 0) {
                this.mExtraLayoutSpace = extraLayoutSpace;
                requestLayout();
                return;
            }
            throw new IllegalArgumentException("ExtraLayoutSpace must >= 0");
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getExtraLayoutSpace() {
        return this.mExtraLayoutSpace;
    }

    private void removeInvisibleViewsAtEnd() {
        if ((this.mFlag & 65600) == 65536) {
            this.mGrid.removeInvisibleItemsAtEnd(this.mFocusPosition, (this.mFlag & 262144) != 0 ? -this.mExtraLayoutSpace : this.mSizePrimary + this.mExtraLayoutSpace);
        }
    }

    private void removeInvisibleViewsAtFront() {
        if ((this.mFlag & 65600) == 65536) {
            this.mGrid.removeInvisibleItemsAtFront(this.mFocusPosition, (this.mFlag & 262144) != 0 ? this.mSizePrimary + this.mExtraLayoutSpace : -this.mExtraLayoutSpace);
        }
    }

    private boolean appendOneColumnVisibleItems() {
        return this.mGrid.appendOneColumnVisibleItems();
    }

    /* Access modifiers changed, original: 0000 */
    public void slideIn() {
        if ((this.mFlag & 64) != 0) {
            this.mFlag &= -65;
            if (this.mFocusPosition >= 0) {
                scrollToSelection(this.mFocusPosition, this.mSubFocusPosition, true, this.mPrimaryScrollExtra);
            } else {
                this.mFlag &= -129;
                requestLayout();
            }
            if ((this.mFlag & 128) != 0) {
                this.mFlag &= -129;
                if (this.mBaseGridView.getScrollState() != 0 || isSmoothScrolling()) {
                    this.mBaseGridView.addOnScrollListener(new OnScrollListener() {
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            if (newState == 0) {
                                GridLayoutManager.this.mBaseGridView.removeOnScrollListener(this);
                                GridLayoutManager.this.requestLayout();
                            }
                        }
                    });
                } else {
                    requestLayout();
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getSlideOutDistance() {
        int distance;
        int top;
        if (this.mOrientation == 1) {
            distance = -getHeight();
            if (getChildCount() <= 0) {
                return distance;
            }
            top = getChildAt(0).getTop();
            if (top < 0) {
                return distance + top;
            }
            return distance;
        } else if ((this.mFlag & 262144) != 0) {
            distance = getWidth();
            if (getChildCount() <= 0) {
                return distance;
            }
            top = getChildAt(0).getRight();
            if (top > distance) {
                return top;
            }
            return distance;
        } else {
            distance = -getWidth();
            if (getChildCount() <= 0) {
                return distance;
            }
            top = getChildAt(0).getLeft();
            if (top < 0) {
                return distance + top;
            }
            return distance;
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isSlidingChildViews() {
        return (this.mFlag & 64) != 0;
    }

    /* Access modifiers changed, original: 0000 */
    public void slideOut() {
        if ((this.mFlag & 64) == 0) {
            this.mFlag |= 64;
            if (getChildCount() != 0) {
                if (this.mOrientation == 1) {
                    this.mBaseGridView.smoothScrollBy(0, getSlideOutDistance(), new AccelerateDecelerateInterpolator());
                } else {
                    this.mBaseGridView.smoothScrollBy(getSlideOutDistance(), 0, new AccelerateDecelerateInterpolator());
                }
            }
        }
    }

    private boolean prependOneColumnVisibleItems() {
        return this.mGrid.prependOneColumnVisibleItems();
    }

    private void appendVisibleItems() {
        this.mGrid.appendVisibleItems((this.mFlag & 262144) != 0 ? (-this.mExtraLayoutSpace) - this.mExtraLayoutSpaceInPreLayout : (this.mSizePrimary + this.mExtraLayoutSpace) + this.mExtraLayoutSpaceInPreLayout);
    }

    private void prependVisibleItems() {
        this.mGrid.prependVisibleItems((this.mFlag & 262144) != 0 ? (this.mSizePrimary + this.mExtraLayoutSpace) + this.mExtraLayoutSpaceInPreLayout : (-this.mExtraLayoutSpace) - this.mExtraLayoutSpaceInPreLayout);
    }

    private void fastRelayout() {
        int i;
        boolean invalidateAfter = false;
        int childCount = getChildCount();
        int position = this.mGrid.getFirstVisibleIndex();
        this.mFlag &= -9;
        int position2 = position;
        int index = 0;
        while (index < childCount) {
            View view = getChildAt(index);
            if (position2 != getAdapterPositionByView(view)) {
                invalidateAfter = true;
                break;
            }
            Location location = this.mGrid.getLocation(position2);
            if (location == null) {
                invalidateAfter = true;
                break;
            }
            boolean primarySize;
            int startSecondary = (getRowStartSecondary(location.row) + this.mWindowAlignment.secondAxis().getPaddingMin()) - this.mScrollOffsetSecondary;
            int start = getViewMin(view);
            boolean oldPrimarySize = getViewPrimarySize(view);
            if (((LayoutParams) view.getLayoutParams()).viewNeedsUpdate()) {
                this.mFlag |= 8;
                detachAndScrapView(view, this.mRecycler);
                view = getViewForPosition(position2);
                addView(view, index);
            }
            View view2 = view;
            measureChild(view2);
            if (this.mOrientation == 0) {
                primarySize = getDecoratedMeasuredWidthWithMargin(view2);
                i = start + primarySize;
            } else {
                primarySize = getDecoratedMeasuredHeightWithMargin(view2);
                i = start + primarySize;
            }
            boolean primarySize2 = primarySize;
            boolean invalidateAfter2 = invalidateAfter;
            invalidateAfter = primarySize2;
            layoutChild(location.row, view2, start, i, startSecondary);
            if (oldPrimarySize != invalidateAfter) {
                invalidateAfter = true;
                break;
            }
            index++;
            position2++;
            invalidateAfter = invalidateAfter2;
        }
        if (invalidateAfter) {
            position = this.mGrid.getLastVisibleIndex();
            for (i = childCount - 1; i >= index; i--) {
                detachAndScrapView(getChildAt(i), this.mRecycler);
            }
            this.mGrid.invalidateItemsAfter(position2);
            if ((this.mFlag & 65536) != 0) {
                appendVisibleItems();
                if (this.mFocusPosition >= 0 && this.mFocusPosition <= position) {
                    while (this.mGrid.getLastVisibleIndex() < this.mFocusPosition) {
                        this.mGrid.appendOneColumnVisibleItems();
                    }
                }
            } else {
                while (this.mGrid.appendOneColumnVisibleItems() && this.mGrid.getLastVisibleIndex() < position) {
                }
            }
        }
        updateScrollLimits();
        updateSecondaryScrollLimits();
    }

    public void removeAndRecycleAllViews(Recycler recycler) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            removeAndRecycleViewAt(i, recycler);
        }
    }

    private void focusToViewInLayout(boolean hadFocus, boolean alignToView, int extraDelta, int extraDeltaSecondary) {
        View focusView = findViewByPosition(this.mFocusPosition);
        if (focusView != null && alignToView) {
            scrollToView(focusView, false, extraDelta, extraDeltaSecondary);
        }
        if (focusView != null && hadFocus && !focusView.hasFocus()) {
            focusView.requestFocus();
        } else if (!hadFocus && !this.mBaseGridView.hasFocus()) {
            if (focusView == null || !focusView.hasFocusable()) {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    focusView = getChildAt(i);
                    if (focusView != null && focusView.hasFocusable()) {
                        this.mBaseGridView.focusableViewAvailable(focusView);
                        break;
                    }
                }
            } else {
                this.mBaseGridView.focusableViewAvailable(focusView);
            }
            if (alignToView && focusView != null && focusView.hasFocus()) {
                scrollToView(focusView, false, extraDelta, extraDeltaSecondary);
            }
        }
    }

    public void onLayoutCompleted(State state) {
        if (this.mLayoutCompleteListener != null) {
            this.mLayoutCompleteListener.onLayoutCompleted(state);
        }
    }

    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public void updatePositionToRowMapInPostLayout() {
        this.mPositionToRowInPostLayout.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            int position = this.mBaseGridView.getChildViewHolder(getChildAt(i)).getOldPosition();
            if (position >= 0) {
                Location loc = this.mGrid.getLocation(position);
                if (loc != null) {
                    this.mPositionToRowInPostLayout.put(position, loc.row);
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void fillScrapViewsInPostLayout() {
        List<ViewHolder> scrapList = this.mRecycler.getScrapList();
        int scrapSize = scrapList.size();
        if (scrapSize != 0) {
            int length;
            if (this.mDisappearingPositions == null || scrapSize > this.mDisappearingPositions.length) {
                length = this.mDisappearingPositions == null ? 16 : this.mDisappearingPositions.length;
                while (length < scrapSize) {
                    length <<= 1;
                }
                this.mDisappearingPositions = new int[length];
            }
            int totalItems = 0;
            for (length = 0; length < scrapSize; length++) {
                int pos = ((ViewHolder) scrapList.get(length)).getAdapterPosition();
                if (pos >= 0) {
                    int totalItems2 = totalItems + 1;
                    this.mDisappearingPositions[totalItems] = pos;
                    totalItems = totalItems2;
                }
            }
            if (totalItems > 0) {
                Arrays.sort(this.mDisappearingPositions, 0, totalItems);
                this.mGrid.fillDisappearingItems(this.mDisappearingPositions, totalItems, this.mPositionToRowInPostLayout);
            }
            this.mPositionToRowInPostLayout.clear();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updatePositionDeltaInPreLayout() {
        if (getChildCount() > 0) {
            this.mPositionDeltaInPreLayout = this.mGrid.getFirstVisibleIndex() - ((LayoutParams) getChildAt(0).getLayoutParams()).getViewLayoutPosition();
            return;
        }
        this.mPositionDeltaInPreLayout = 0;
    }

    public void onLayoutChildren(Recycler recycler, State state) {
        if (this.mNumRows == 0 || state.getItemCount() < 0) {
            return;
        }
        if ((this.mFlag & 64) != 0 && getChildCount() > 0) {
            this.mFlag |= 128;
        } else if ((this.mFlag & 512) == 0) {
            discardLayoutInfo();
            removeAndRecycleAllViews(recycler);
        } else {
            boolean z = true;
            this.mFlag = (this.mFlag & -4) | 1;
            saveContext(recycler, state);
            int i = 0;
            int maxChangeEdge;
            int minOldAdapterPosition;
            int maxOldAdapterPosition;
            int newAdapterPosition;
            if (state.isPreLayout()) {
                updatePositionDeltaInPreLayout();
                int childCount = getChildCount();
                if (this.mGrid != null && childCount > 0) {
                    int minChangedEdge = Integer.MAX_VALUE;
                    maxChangeEdge = Integer.MIN_VALUE;
                    minOldAdapterPosition = this.mBaseGridView.getChildViewHolder(getChildAt(0)).getOldPosition();
                    maxOldAdapterPosition = this.mBaseGridView.getChildViewHolder(getChildAt(childCount - 1)).getOldPosition();
                    while (i < childCount) {
                        View view = getChildAt(i);
                        LayoutParams lp = (LayoutParams) view.getLayoutParams();
                        newAdapterPosition = this.mBaseGridView.getChildAdapterPosition(view);
                        if (lp.isItemChanged() || lp.isItemRemoved() || view.isLayoutRequested() || ((!view.hasFocus() && this.mFocusPosition == lp.getViewAdapterPosition()) || ((view.hasFocus() && this.mFocusPosition != lp.getViewAdapterPosition()) || newAdapterPosition < minOldAdapterPosition || newAdapterPosition > maxOldAdapterPosition))) {
                            minChangedEdge = Math.min(minChangedEdge, getViewMin(view));
                            maxChangeEdge = Math.max(maxChangeEdge, getViewMax(view));
                        }
                        i++;
                    }
                    if (maxChangeEdge > minChangedEdge) {
                        this.mExtraLayoutSpaceInPreLayout = maxChangeEdge - minChangedEdge;
                    }
                    appendVisibleItems();
                    prependVisibleItems();
                }
                this.mFlag &= -4;
                leaveContext();
                return;
            }
            int deltaPrimary;
            int deltaSecondary;
            if (state.willRunPredictiveAnimations()) {
                updatePositionToRowMapInPostLayout();
            }
            if (isSmoothScrolling() || this.mFocusScrollStrategy != 0) {
                z = false;
            }
            boolean scrollToFocus = z;
            if (!(this.mFocusPosition == -1 || this.mFocusPositionOffset == Integer.MIN_VALUE)) {
                this.mFocusPosition += this.mFocusPositionOffset;
                this.mSubFocusPosition = 0;
            }
            this.mFocusPositionOffset = 0;
            View savedFocusView = findViewByPosition(this.mFocusPosition);
            minOldAdapterPosition = this.mFocusPosition;
            maxOldAdapterPosition = this.mSubFocusPosition;
            boolean hadFocus = this.mBaseGridView.hasFocus();
            int firstVisibleIndex = this.mGrid != null ? this.mGrid.getFirstVisibleIndex() : -1;
            newAdapterPosition = this.mGrid != null ? this.mGrid.getLastVisibleIndex() : -1;
            if (this.mOrientation == 0) {
                deltaPrimary = state.getRemainingScrollHorizontal();
                deltaSecondary = state.getRemainingScrollVertical();
            } else {
                deltaSecondary = state.getRemainingScrollHorizontal();
                deltaPrimary = state.getRemainingScrollVertical();
            }
            if (layoutInit()) {
                this.mFlag |= 4;
                this.mGrid.setStart(this.mFocusPosition);
                fastRelayout();
            } else {
                this.mFlag &= -5;
                int i2 = this.mFlag & -17;
                if (hadFocus) {
                    i = 16;
                }
                this.mFlag = i | i2;
                if (!scrollToFocus || (firstVisibleIndex >= 0 && this.mFocusPosition <= newAdapterPosition && this.mFocusPosition >= firstVisibleIndex)) {
                    i = firstVisibleIndex;
                    i2 = newAdapterPosition;
                } else {
                    i = this.mFocusPosition;
                    i2 = i;
                }
                this.mGrid.setStart(i);
                if (i2 != -1) {
                    while (appendOneColumnVisibleItems() && findViewByPosition(i2) == null) {
                    }
                }
            }
            while (true) {
                updateScrollLimits();
                i = this.mGrid.getFirstVisibleIndex();
                maxChangeEdge = this.mGrid.getLastVisibleIndex();
                focusToViewInLayout(hadFocus, scrollToFocus, -deltaPrimary, -deltaSecondary);
                appendVisibleItems();
                prependVisibleItems();
                if (this.mGrid.getFirstVisibleIndex() == i && this.mGrid.getLastVisibleIndex() == maxChangeEdge) {
                    break;
                }
            }
            removeInvisibleViewsAtFront();
            removeInvisibleViewsAtEnd();
            if (state.willRunPredictiveAnimations()) {
                fillScrapViewsInPostLayout();
            }
            if ((this.mFlag & 1024) != 0) {
                this.mFlag &= -1025;
            } else {
                updateRowSecondarySizeRefresh();
            }
            if ((this.mFlag & 4) != 0 && (this.mFocusPosition != minOldAdapterPosition || this.mSubFocusPosition != maxOldAdapterPosition || findViewByPosition(this.mFocusPosition) != savedFocusView || (this.mFlag & 8) != 0)) {
                dispatchChildSelected();
            } else if ((this.mFlag & 20) == 16) {
                dispatchChildSelected();
            }
            dispatchChildSelectedAndPositioned();
            if ((this.mFlag & 64) != 0) {
                scrollDirectionPrimary(getSlideOutDistance());
            }
            this.mFlag &= -4;
            leaveContext();
        }
    }

    private void offsetChildrenSecondary(int increment) {
        int childCount = getChildCount();
        int i = 0;
        int i2;
        if (this.mOrientation == 0) {
            while (true) {
                i2 = i;
                if (i2 < childCount) {
                    getChildAt(i2).offsetTopAndBottom(increment);
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
        while (true) {
            i2 = i;
            if (i2 < childCount) {
                getChildAt(i2).offsetLeftAndRight(increment);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    private void offsetChildrenPrimary(int increment) {
        int childCount = getChildCount();
        int i = 0;
        int i2;
        if (this.mOrientation == 1) {
            while (true) {
                i2 = i;
                if (i2 < childCount) {
                    getChildAt(i2).offsetTopAndBottom(increment);
                    i = i2 + 1;
                } else {
                    return;
                }
            }
        }
        while (true) {
            i2 = i;
            if (i2 < childCount) {
                getChildAt(i2).offsetLeftAndRight(increment);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
        if ((this.mFlag & 512) == 0 || !hasDoneFirstLayout()) {
            return 0;
        }
        int result;
        saveContext(recycler, state);
        this.mFlag = (this.mFlag & -4) | 2;
        if (this.mOrientation == 0) {
            result = scrollDirectionPrimary(dx);
        } else {
            result = scrollDirectionSecondary(dx);
        }
        leaveContext();
        this.mFlag &= -4;
        return result;
    }

    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        if ((this.mFlag & 512) == 0 || !hasDoneFirstLayout()) {
            return 0;
        }
        int result;
        this.mFlag = (this.mFlag & -4) | 2;
        saveContext(recycler, state);
        if (this.mOrientation == 1) {
            result = scrollDirectionPrimary(dy);
        } else {
            result = scrollDirectionSecondary(dy);
        }
        leaveContext();
        this.mFlag &= -4;
        return result;
    }

    private int scrollDirectionPrimary(int da) {
        if ((this.mFlag & 64) == 0 && (this.mFlag & 3) != 1) {
            int maxScroll;
            if (da > 0) {
                if (!this.mWindowAlignment.mainAxis().isMaxUnknown()) {
                    maxScroll = this.mWindowAlignment.mainAxis().getMaxScroll();
                    if (da > maxScroll) {
                        da = maxScroll;
                    }
                }
            } else if (da < 0 && !this.mWindowAlignment.mainAxis().isMinUnknown()) {
                maxScroll = this.mWindowAlignment.mainAxis().getMinScroll();
                if (da < maxScroll) {
                    da = maxScroll;
                }
            }
        }
        boolean updated = false;
        if (da == 0) {
            return 0;
        }
        offsetChildrenPrimary(-da);
        if ((this.mFlag & 3) == 1) {
            updateScrollLimits();
            return da;
        }
        int childCount = getChildCount();
        if ((this.mFlag & 262144) == 0 ? da >= 0 : da <= 0) {
            appendVisibleItems();
        } else {
            prependVisibleItems();
        }
        boolean updated2 = getChildCount() > childCount;
        childCount = getChildCount();
        if ((262144 & this.mFlag) == 0 ? da >= 0 : da <= 0) {
            removeInvisibleViewsAtFront();
        } else {
            removeInvisibleViewsAtEnd();
        }
        if (getChildCount() < childCount) {
            updated = true;
        }
        if (updated | updated2) {
            updateRowSecondarySizeRefresh();
        }
        this.mBaseGridView.invalidate();
        updateScrollLimits();
        return da;
    }

    private int scrollDirectionSecondary(int dy) {
        if (dy == 0) {
            return 0;
        }
        offsetChildrenSecondary(-dy);
        this.mScrollOffsetSecondary += dy;
        updateSecondaryScrollLimits();
        this.mBaseGridView.invalidate();
        return dy;
    }

    public void collectAdjacentPrefetchPositions(int dx, int dy, State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        try {
            saveContext(null, state);
            int da = this.mOrientation == 0 ? dx : dy;
            if (getChildCount() != 0) {
                if (da != 0) {
                    this.mGrid.collectAdjacentPrefetchPositions(da < 0 ? -this.mExtraLayoutSpace : this.mSizePrimary + this.mExtraLayoutSpace, da, layoutPrefetchRegistry);
                    leaveContext();
                    return;
                }
            }
            leaveContext();
        } catch (Throwable th) {
            leaveContext();
        }
    }

    public void collectInitialPrefetchPositions(int adapterItemCount, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int numToPrefetch = this.mBaseGridView.mInitialPrefetchItemCount;
        if (adapterItemCount != 0 && numToPrefetch != 0) {
            int initialPos = Math.max(0, Math.min(this.mFocusPosition - ((numToPrefetch - 1) / 2), adapterItemCount - numToPrefetch));
            int i = initialPos;
            while (i < adapterItemCount && i < initialPos + numToPrefetch) {
                layoutPrefetchRegistry.addPosition(i, 0);
                i++;
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void updateScrollLimits() {
        if (this.mState.getItemCount() != 0) {
            int highVisiblePos;
            int highMaxPos;
            int lowVisiblePos;
            int lowMinPos;
            if ((this.mFlag & 262144) == 0) {
                highVisiblePos = this.mGrid.getLastVisibleIndex();
                highMaxPos = this.mState.getItemCount() - 1;
                lowVisiblePos = this.mGrid.getFirstVisibleIndex();
                lowMinPos = 0;
            } else {
                highVisiblePos = this.mGrid.getFirstVisibleIndex();
                highMaxPos = 0;
                lowVisiblePos = this.mGrid.getLastVisibleIndex();
                lowMinPos = this.mState.getItemCount() - 1;
            }
            if (highVisiblePos >= 0 && lowVisiblePos >= 0) {
                boolean highAvailable = highVisiblePos == highMaxPos;
                boolean lowAvailable = lowVisiblePos == lowMinPos;
                if (highAvailable || !this.mWindowAlignment.mainAxis().isMaxUnknown() || lowAvailable || !this.mWindowAlignment.mainAxis().isMinUnknown()) {
                    int maxEdge;
                    int maxViewCenter;
                    int minEdge;
                    int minViewCenter;
                    if (highAvailable) {
                        maxEdge = this.mGrid.findRowMax(true, sTwoInts);
                        View maxChild = findViewByPosition(sTwoInts[1]);
                        maxViewCenter = getViewCenter(maxChild);
                        int[] multipleAligns = ((LayoutParams) maxChild.getLayoutParams()).getAlignMultiple();
                        if (multipleAligns != null && multipleAligns.length > 0) {
                            maxViewCenter += multipleAligns[multipleAligns.length - 1] - multipleAligns[0];
                        }
                    } else {
                        maxEdge = Integer.MAX_VALUE;
                        maxViewCenter = Integer.MAX_VALUE;
                    }
                    int maxViewCenter2 = maxViewCenter;
                    if (lowAvailable) {
                        minEdge = this.mGrid.findRowMin(false, sTwoInts);
                        minViewCenter = getViewCenter(findViewByPosition(sTwoInts[1]));
                    } else {
                        minEdge = Integer.MIN_VALUE;
                        minViewCenter = Integer.MIN_VALUE;
                    }
                    this.mWindowAlignment.mainAxis().updateMinMax(minEdge, maxEdge, minViewCenter, maxViewCenter2);
                }
            }
        }
    }

    private void updateSecondaryScrollLimits() {
        Axis secondAxis = this.mWindowAlignment.secondAxis();
        int minEdge = secondAxis.getPaddingMin() - this.mScrollOffsetSecondary;
        int maxEdge = getSizeSecondary() + minEdge;
        secondAxis.updateMinMax(minEdge, maxEdge, minEdge, maxEdge);
    }

    private void initScrollController() {
        this.mWindowAlignment.reset();
        this.mWindowAlignment.horizontal.setSize(getWidth());
        this.mWindowAlignment.vertical.setSize(getHeight());
        this.mWindowAlignment.horizontal.setPadding(getPaddingLeft(), getPaddingRight());
        this.mWindowAlignment.vertical.setPadding(getPaddingTop(), getPaddingBottom());
        this.mSizePrimary = this.mWindowAlignment.mainAxis().getSize();
        this.mScrollOffsetSecondary = 0;
    }

    private void updateScrollController() {
        this.mWindowAlignment.horizontal.setSize(getWidth());
        this.mWindowAlignment.vertical.setSize(getHeight());
        this.mWindowAlignment.horizontal.setPadding(getPaddingLeft(), getPaddingRight());
        this.mWindowAlignment.vertical.setPadding(getPaddingTop(), getPaddingBottom());
        this.mSizePrimary = this.mWindowAlignment.mainAxis().getSize();
    }

    public void scrollToPosition(int position) {
        setSelection(position, 0, false, 0);
    }

    public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
        setSelection(position, 0, true, 0);
    }

    public void setSelection(int position, int primaryScrollExtra) {
        setSelection(position, 0, false, primaryScrollExtra);
    }

    public void setSelectionSmooth(int position) {
        setSelection(position, 0, true, 0);
    }

    public void setSelectionWithSub(int position, int subposition, int primaryScrollExtra) {
        setSelection(position, subposition, false, primaryScrollExtra);
    }

    public void setSelectionSmoothWithSub(int position, int subposition) {
        setSelection(position, subposition, true, 0);
    }

    public int getSelection() {
        return this.mFocusPosition;
    }

    public int getSubSelection() {
        return this.mSubFocusPosition;
    }

    public void setSelection(int position, int subposition, boolean smooth, int primaryScrollExtra) {
        if ((this.mFocusPosition != position && position != -1) || subposition != this.mSubFocusPosition || primaryScrollExtra != this.mPrimaryScrollExtra) {
            scrollToSelection(position, subposition, smooth, primaryScrollExtra);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void scrollToSelection(int position, int subposition, boolean smooth, int primaryScrollExtra) {
        this.mPrimaryScrollExtra = primaryScrollExtra;
        View view = findViewByPosition(position);
        boolean notSmoothScrolling = isSmoothScrolling() ^ 1;
        if (notSmoothScrolling && !this.mBaseGridView.isLayoutRequested() && view != null && getAdapterPositionByView(view) == position) {
            this.mFlag |= 32;
            scrollToView(view, smooth);
            this.mFlag &= -33;
        } else if ((this.mFlag & 512) == 0 || (this.mFlag & 64) != 0) {
            this.mFocusPosition = position;
            this.mSubFocusPosition = subposition;
            this.mFocusPositionOffset = Integer.MIN_VALUE;
        } else if (!smooth || this.mBaseGridView.isLayoutRequested()) {
            if (!notSmoothScrolling) {
                skipSmoothScrollerOnStopInternal();
                this.mBaseGridView.stopScroll();
            }
            if (this.mBaseGridView.isLayoutRequested() || view == null || getAdapterPositionByView(view) != position) {
                this.mFocusPosition = position;
                this.mSubFocusPosition = subposition;
                this.mFocusPositionOffset = Integer.MIN_VALUE;
                this.mFlag |= 256;
                requestLayout();
            } else {
                this.mFlag |= 32;
                scrollToView(view, smooth);
                this.mFlag &= -33;
            }
        } else {
            this.mFocusPosition = position;
            this.mSubFocusPosition = subposition;
            this.mFocusPositionOffset = Integer.MIN_VALUE;
            if (hasDoneFirstLayout()) {
                position = startPositionSmoothScroller(position);
                if (position != this.mFocusPosition) {
                    this.mFocusPosition = position;
                    this.mSubFocusPosition = 0;
                }
            } else {
                Log.w(getTag(), "setSelectionSmooth should not be called before first layout pass");
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int startPositionSmoothScroller(int position) {
        LinearSmoothScroller linearSmoothScroller = new GridLinearSmoothScroller() {
            public PointF computeScrollVectorForPosition(int targetPosition) {
                if (getChildCount() == 0) {
                    return null;
                }
                boolean z = false;
                int firstChildPos = GridLayoutManager.this.getPosition(GridLayoutManager.this.getChildAt(0));
                int i = 1;
                if ((GridLayoutManager.this.mFlag & 262144) == 0 ? targetPosition >= firstChildPos : targetPosition <= firstChildPos) {
                    z = true;
                }
                if (z) {
                    i = -1;
                }
                int direction = i;
                if (GridLayoutManager.this.mOrientation == 0) {
                    return new PointF((float) direction, 0.0f);
                }
                return new PointF(0.0f, (float) direction);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
        return linearSmoothScroller.getTargetPosition();
    }

    /* Access modifiers changed, original: 0000 */
    public void skipSmoothScrollerOnStopInternal() {
        if (this.mCurrentSmoothScroller != null) {
            this.mCurrentSmoothScroller.mSkipOnStopInternal = true;
        }
    }

    public void startSmoothScroll(SmoothScroller smoothScroller) {
        skipSmoothScrollerOnStopInternal();
        super.startSmoothScroll(smoothScroller);
        if (smoothScroller.isRunning() && (smoothScroller instanceof GridLinearSmoothScroller)) {
            this.mCurrentSmoothScroller = (GridLinearSmoothScroller) smoothScroller;
            if (this.mCurrentSmoothScroller instanceof PendingMoveSmoothScroller) {
                this.mPendingMoveSmoothScroller = (PendingMoveSmoothScroller) this.mCurrentSmoothScroller;
                return;
            } else {
                this.mPendingMoveSmoothScroller = null;
                return;
            }
        }
        this.mCurrentSmoothScroller = null;
        this.mPendingMoveSmoothScroller = null;
    }

    private void processPendingMovement(boolean forward) {
        if (forward ? !hasCreatedLastItem() : !hasCreatedFirstItem()) {
            if (this.mPendingMoveSmoothScroller == null) {
                this.mBaseGridView.stopScroll();
                boolean z = true;
                int i = forward ? 1 : -1;
                if (this.mNumRows <= 1) {
                    z = false;
                }
                PendingMoveSmoothScroller linearSmoothScroller = new PendingMoveSmoothScroller(i, z);
                this.mFocusPositionOffset = 0;
                startSmoothScroll(linearSmoothScroller);
            } else if (forward) {
                this.mPendingMoveSmoothScroller.increasePendingMoves();
            } else {
                this.mPendingMoveSmoothScroller.decreasePendingMoves();
            }
        }
    }

    public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        if (!(this.mFocusPosition == -1 || this.mGrid == null || this.mGrid.getFirstVisibleIndex() < 0 || this.mFocusPositionOffset == Integer.MIN_VALUE || positionStart > this.mFocusPosition + this.mFocusPositionOffset)) {
            this.mFocusPositionOffset += itemCount;
        }
        this.mChildrenStates.clear();
    }

    public void onItemsChanged(RecyclerView recyclerView) {
        this.mFocusPositionOffset = 0;
        this.mChildrenStates.clear();
    }

    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        if (!(this.mFocusPosition == -1 || this.mGrid == null || this.mGrid.getFirstVisibleIndex() < 0 || this.mFocusPositionOffset == Integer.MIN_VALUE)) {
            int pos = this.mFocusPosition + this.mFocusPositionOffset;
            if (positionStart <= pos) {
                if (positionStart + itemCount > pos) {
                    this.mFocusPositionOffset += positionStart - pos;
                    this.mFocusPosition += this.mFocusPositionOffset;
                    this.mFocusPositionOffset = Integer.MIN_VALUE;
                } else {
                    this.mFocusPositionOffset -= itemCount;
                }
            }
        }
        this.mChildrenStates.clear();
    }

    public void onItemsMoved(RecyclerView recyclerView, int fromPosition, int toPosition, int itemCount) {
        if (!(this.mFocusPosition == -1 || this.mFocusPositionOffset == Integer.MIN_VALUE)) {
            int pos = this.mFocusPosition + this.mFocusPositionOffset;
            if (fromPosition <= pos && pos < fromPosition + itemCount) {
                this.mFocusPositionOffset += toPosition - fromPosition;
            } else if (fromPosition < pos && toPosition > pos - itemCount) {
                this.mFocusPositionOffset -= itemCount;
            } else if (fromPosition > pos && toPosition < pos) {
                this.mFocusPositionOffset += itemCount;
            }
        }
        this.mChildrenStates.clear();
    }

    public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        int end = positionStart + itemCount;
        for (int i = positionStart; i < end; i++) {
            this.mChildrenStates.remove(i);
        }
    }

    public boolean onRequestChildFocus(RecyclerView parent, View child, View focused) {
        if ((this.mFlag & 32768) == 0 && getAdapterPositionByView(child) != -1 && (this.mFlag & 35) == 0) {
            scrollToView(child, focused, true);
        }
        return true;
    }

    public boolean requestChildRectangleOnScreen(RecyclerView parent, View view, Rect rect, boolean immediate) {
        return false;
    }

    public void getViewSelectedOffsets(View view, int[] offsets) {
        if (this.mOrientation == 0) {
            offsets[0] = getPrimaryAlignedScrollDistance(view);
            offsets[1] = getSecondaryScrollDistance(view);
            return;
        }
        offsets[1] = getPrimaryAlignedScrollDistance(view);
        offsets[0] = getSecondaryScrollDistance(view);
    }

    private int getPrimaryAlignedScrollDistance(View view) {
        return this.mWindowAlignment.mainAxis().getScroll(getViewCenter(view));
    }

    private int getAdjustedPrimaryAlignedScrollDistance(int scrollPrimary, View view, View childView) {
        int subindex = getSubPositionByView(view, childView);
        if (subindex == 0) {
            return scrollPrimary;
        }
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        return scrollPrimary + (lp.getAlignMultiple()[subindex] - lp.getAlignMultiple()[0]);
    }

    private int getSecondaryScrollDistance(View view) {
        return this.mWindowAlignment.secondAxis().getScroll(getViewCenterSecondary(view));
    }

    /* Access modifiers changed, original: 0000 */
    public void scrollToView(View view, boolean smooth) {
        scrollToView(view, view == null ? null : view.findFocus(), smooth);
    }

    /* Access modifiers changed, original: 0000 */
    public void scrollToView(View view, boolean smooth, int extraDelta, int extraDeltaSecondary) {
        scrollToView(view, view == null ? null : view.findFocus(), smooth, extraDelta, extraDeltaSecondary);
    }

    private void scrollToView(View view, View childView, boolean smooth) {
        scrollToView(view, childView, smooth, 0, 0);
    }

    private void scrollToView(View view, View childView, boolean smooth, int extraDelta, int extraDeltaSecondary) {
        if ((this.mFlag & 64) == 0) {
            int newFocusPosition = getAdapterPositionByView(view);
            int newSubFocusPosition = getSubPositionByView(view, childView);
            if (!(newFocusPosition == this.mFocusPosition && newSubFocusPosition == this.mSubFocusPosition)) {
                this.mFocusPosition = newFocusPosition;
                this.mSubFocusPosition = newSubFocusPosition;
                this.mFocusPositionOffset = 0;
                if ((this.mFlag & 3) != 1) {
                    dispatchChildSelected();
                }
                if (this.mBaseGridView.isChildrenDrawingOrderEnabledInternal()) {
                    this.mBaseGridView.invalidate();
                }
            }
            if (view != null) {
                if (!view.hasFocus() && this.mBaseGridView.hasFocus()) {
                    view.requestFocus();
                }
                if ((this.mFlag & 131072) != 0 || !smooth) {
                    if (!(!getScrollPosition(view, childView, sTwoInts) && extraDelta == 0 && extraDeltaSecondary == 0)) {
                        scrollGrid(sTwoInts[0] + extraDelta, sTwoInts[1] + extraDeltaSecondary, smooth);
                    }
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean getScrollPosition(View view, View childView, int[] deltas) {
        switch (this.mFocusScrollStrategy) {
            case 1:
            case 2:
                return getNoneAlignedPosition(view, deltas);
            default:
                return getAlignedPosition(view, childView, deltas);
        }
    }

    private boolean getNoneAlignedPosition(View view, int[] deltas) {
        View secondaryAlignedView;
        int pos = getAdapterPositionByView(view);
        int viewMin = getViewMin(view);
        int viewMax = getViewMax(view);
        View firstView = null;
        View lastView = null;
        int paddingMin = this.mWindowAlignment.mainAxis().getPaddingMin();
        int clientSize = this.mWindowAlignment.mainAxis().getClientSize();
        int row = this.mGrid.getRowIndex(pos);
        if (viewMin < paddingMin) {
            firstView = view;
            if (this.mFocusScrollStrategy == 2) {
                while (prependOneColumnVisibleItems()) {
                    CircularIntArray positions = this.mGrid.getItemPositionsInRows(this.mGrid.getFirstVisibleIndex(), pos)[row];
                    firstView = findViewByPosition(positions.get(0));
                    if (viewMax - getViewMin(firstView) > clientSize) {
                        if (positions.size() > 2) {
                            firstView = findViewByPosition(positions.get(2));
                        }
                    }
                }
            }
        } else if (viewMax > clientSize + paddingMin) {
            if (this.mFocusScrollStrategy == 2) {
                View firstView2 = view;
                do {
                    CircularIntArray positions2 = this.mGrid.getItemPositionsInRows(pos, this.mGrid.getLastVisibleIndex())[row];
                    lastView = findViewByPosition(positions2.get(positions2.size() - 1));
                    if (getViewMax(lastView) - viewMin > clientSize) {
                        lastView = null;
                        break;
                    }
                } while (appendOneColumnVisibleItems());
                firstView = lastView != null ? null : firstView2;
            } else {
                lastView = view;
            }
        }
        int scrollPrimary = 0;
        if (firstView != null) {
            scrollPrimary = getViewMin(firstView) - paddingMin;
        } else if (lastView != null) {
            scrollPrimary = getViewMax(lastView) - (paddingMin + clientSize);
        }
        if (firstView != null) {
            secondaryAlignedView = firstView;
        } else if (lastView != null) {
            secondaryAlignedView = lastView;
        } else {
            secondaryAlignedView = view;
        }
        int scrollSecondary = getSecondaryScrollDistance(secondaryAlignedView);
        if (scrollPrimary == 0 && scrollSecondary == 0) {
            return false;
        }
        deltas[0] = scrollPrimary;
        deltas[1] = scrollSecondary;
        return true;
    }

    private boolean getAlignedPosition(View view, View childView, int[] deltas) {
        int scrollPrimary = getPrimaryAlignedScrollDistance(view);
        if (childView != null) {
            scrollPrimary = getAdjustedPrimaryAlignedScrollDistance(scrollPrimary, view, childView);
        }
        int scrollSecondary = getSecondaryScrollDistance(view);
        scrollPrimary += this.mPrimaryScrollExtra;
        if (scrollPrimary == 0 && scrollSecondary == 0) {
            deltas[0] = 0;
            deltas[1] = 0;
            return false;
        }
        deltas[0] = scrollPrimary;
        deltas[1] = scrollSecondary;
        return true;
    }

    private void scrollGrid(int scrollPrimary, int scrollSecondary, boolean smooth) {
        if ((this.mFlag & 3) == 1) {
            scrollDirectionPrimary(scrollPrimary);
            scrollDirectionSecondary(scrollSecondary);
            return;
        }
        int scrollX;
        int scrollY;
        if (this.mOrientation == 0) {
            scrollX = scrollPrimary;
            scrollY = scrollSecondary;
        } else {
            scrollX = scrollSecondary;
            scrollY = scrollPrimary;
        }
        if (smooth) {
            this.mBaseGridView.smoothScrollBy(scrollX, scrollY);
            return;
        }
        this.mBaseGridView.scrollBy(scrollX, scrollY);
        dispatchChildSelectedAndPositioned();
    }

    public void setPruneChild(boolean pruneChild) {
        int i = 65536;
        if (((this.mFlag & 65536) != 0) != pruneChild) {
            int i2 = this.mFlag & -65537;
            if (!pruneChild) {
                i = 0;
            }
            this.mFlag = i2 | i;
            if (pruneChild) {
                requestLayout();
            }
        }
    }

    public boolean getPruneChild() {
        return (this.mFlag & 65536) != 0;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        int i = 0;
        if (((this.mFlag & 131072) != 0) != scrollEnabled) {
            int i2 = this.mFlag & -131073;
            if (scrollEnabled) {
                i = 131072;
            }
            this.mFlag = i2 | i;
            if ((this.mFlag & 131072) != 0 && this.mFocusScrollStrategy == 0 && this.mFocusPosition != -1) {
                scrollToSelection(this.mFocusPosition, this.mSubFocusPosition, true, this.mPrimaryScrollExtra);
            }
        }
    }

    public boolean isScrollEnabled() {
        return (this.mFlag & 131072) != 0;
    }

    private int findImmediateChildIndex(View view) {
        if (!(this.mBaseGridView == null || view == this.mBaseGridView)) {
            view = findContainingItemView(view);
            if (view != null) {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    if (getChildAt(i) == view) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /* Access modifiers changed, original: 0000 */
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        if (gainFocus) {
            int i = this.mFocusPosition;
            while (true) {
                View view = findViewByPosition(i);
                if (view != null) {
                    if (view.getVisibility() == 0 && view.hasFocusable()) {
                        view.requestFocus();
                        return;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setFocusSearchDisabled(boolean disabled) {
        this.mFlag = (this.mFlag & -32769) | (disabled ? 32768 : 0);
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isFocusSearchDisabled() {
        return (this.mFlag & 32768) != 0;
    }

    public View onInterceptFocusSearch(View focused, int direction) {
        if ((this.mFlag & 32768) != 0) {
            return focused;
        }
        FocusFinder ff = FocusFinder.getInstance();
        View result = null;
        if (direction == 2 || direction == 1) {
            if (canScrollVertically()) {
                result = ff.findNextFocus(this.mBaseGridView, focused, direction == 2 ? 130 : 33);
            }
            if (canScrollHorizontally()) {
                result = ff.findNextFocus(this.mBaseGridView, focused, ((direction == 2 ? 1 : 0) ^ (getLayoutDirection() == 1)) != 0 ? 66 : 17);
            }
        } else {
            result = ff.findNextFocus(this.mBaseGridView, focused, direction);
        }
        if (result != null) {
            return result;
        }
        if (this.mBaseGridView.getDescendantFocusability() == 393216) {
            return this.mBaseGridView.getParent().focusSearch(focused, direction);
        }
        int movement = getMovement(direction);
        boolean isScroll = this.mBaseGridView.getScrollState() != 0;
        if (movement == 1) {
            if (isScroll || (this.mFlag & 4096) == 0) {
                result = focused;
            }
            if (!((this.mFlag & 131072) == 0 || hasCreatedLastItem())) {
                processPendingMovement(true);
                result = focused;
            }
        } else if (movement == 0) {
            if (isScroll || (this.mFlag & 2048) == 0) {
                result = focused;
            }
            if (!((this.mFlag & 131072) == 0 || hasCreatedFirstItem())) {
                processPendingMovement(false);
                result = focused;
            }
        } else if (movement == 3) {
            if (isScroll || (this.mFlag & 16384) == 0) {
                result = focused;
            }
        } else if (movement == 2 && (isScroll || (this.mFlag & 8192) == 0)) {
            result = focused;
        }
        if (result != null) {
            return result;
        }
        result = this.mBaseGridView.getParent().focusSearch(focused, direction);
        if (result != null) {
            return result;
        }
        return focused != null ? focused : this.mBaseGridView;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasPreviousViewInSameRow(int pos) {
        if (this.mGrid == null || pos == -1 || this.mGrid.getFirstVisibleIndex() < 0) {
            return false;
        }
        if (this.mGrid.getFirstVisibleIndex() > 0) {
            return true;
        }
        int focusedRow = this.mGrid.getLocation(pos).row;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            int position = getAdapterPositionByIndex(i);
            Location loc = this.mGrid.getLocation(position);
            if (loc != null && loc.row == focusedRow && position < pos) {
                return true;
            }
        }
        return false;
    }

    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
        ArrayList<View> arrayList = views;
        int i = direction;
        int i2 = focusableMode;
        if ((this.mFlag & 32768) != 0) {
            return true;
        }
        boolean z;
        int focusableCount;
        int count;
        int i3;
        View view;
        RecyclerView recyclerView2;
        if (!recyclerView.hasFocus()) {
            focusableCount = views.size();
            if (this.mFocusScrollStrategy != 0) {
                View child;
                int left = this.mWindowAlignment.mainAxis().getPaddingMin();
                int right = this.mWindowAlignment.mainAxis().getClientSize() + left;
                count = getChildCount();
                for (i3 = 0; i3 < count; i3++) {
                    child = getChildAt(i3);
                    if (child.getVisibility() == 0 && getViewMin(child) >= left && getViewMax(child) <= right) {
                        child.addFocusables(arrayList, i, i2);
                    }
                }
                if (views.size() == focusableCount) {
                    count = getChildCount();
                    for (i3 = 0; i3 < count; i3++) {
                        child = getChildAt(i3);
                        if (child.getVisibility() == 0) {
                            child.addFocusables(arrayList, i, i2);
                        }
                    }
                }
            } else {
                view = findViewByPosition(this.mFocusPosition);
                if (view != null) {
                    view.addFocusables(arrayList, i, i2);
                }
            }
            if (views.size() != focusableCount) {
                return true;
            }
            z = true;
            if (recyclerView.isFocusable()) {
                arrayList.add(recyclerView);
            } else {
                recyclerView2 = recyclerView;
            }
        } else if (this.mPendingMoveSmoothScroller != null) {
            return true;
        } else {
            View immediateFocusedChild;
            focusableCount = getMovement(i);
            View focused = recyclerView.findFocus();
            i3 = findImmediateChildIndex(focused);
            count = getAdapterPositionByIndex(i3);
            if (count == -1) {
                immediateFocusedChild = null;
            } else {
                immediateFocusedChild = findViewByPosition(count);
            }
            if (immediateFocusedChild != null) {
                immediateFocusedChild.addFocusables(arrayList, i, i2);
            }
            View view2;
            int i4;
            if (this.mGrid == null) {
                view2 = focused;
                i4 = i3;
            } else if (getChildCount() == 0) {
                view2 = focused;
                i4 = i3;
            } else if ((focusableCount == 3 || focusableCount == 2) && this.mGrid.getNumRows() <= 1) {
                return true;
            } else {
                int loop_start;
                int focusedRow = (this.mGrid == null || immediateFocusedChild == null) ? -1 : this.mGrid.getLocation(count).row;
                int focusableCount2 = views.size();
                int inc = (focusableCount == 1 || focusableCount == 3) ? 1 : -1;
                int i5 = 0;
                int loop_end = inc > 0 ? getChildCount() - 1 : 0;
                if (i3 == -1) {
                    if (inc <= 0) {
                        i5 = getChildCount() - 1;
                    }
                    loop_start = i5;
                } else {
                    loop_start = i3 + inc;
                }
                i5 = loop_start;
                while (true) {
                    int loop_end2;
                    int i6;
                    int loop_start2;
                    int i7 = i5;
                    if (inc <= 0) {
                        loop_end2 = loop_end;
                        i6 = i7;
                        if (i6 < loop_end2) {
                            break;
                        }
                    }
                    loop_end2 = loop_end;
                    i6 = i7;
                    if (i6 > loop_end2) {
                        break;
                    }
                    view = getChildAt(i6);
                    if (view.getVisibility() == 0) {
                        if (!view.hasFocusable()) {
                            view2 = focused;
                        } else if (immediateFocusedChild == null) {
                            view.addFocusables(arrayList, i, i2);
                            view2 = focused;
                            if (views.size() > focusableCount2) {
                                break;
                            }
                        } else {
                            view2 = focused;
                            focused = getAdapterPositionByIndex(i6);
                            i4 = i3;
                            i3 = this.mGrid.getLocation(focused);
                            if (i3 != 0) {
                                loop_start2 = loop_start;
                                if (focusableCount == 1) {
                                    if (i3.row == focusedRow && focused > count) {
                                        view.addFocusables(arrayList, i, i2);
                                        if (views.size() > focusableCount2) {
                                            break;
                                        }
                                    }
                                } else if (focusableCount == 0) {
                                    if (i3.row == focusedRow && focused < count) {
                                        view.addFocusables(arrayList, i, i2);
                                        if (views.size() > focusableCount2) {
                                            break;
                                        }
                                    }
                                } else if (focusableCount == 3) {
                                    if (i3.row == focusedRow) {
                                        continue;
                                    } else if (i3.row < focusedRow) {
                                        break;
                                    } else {
                                        view.addFocusables(arrayList, i, i2);
                                    }
                                } else if (focusableCount == 2 && i3.row != focusedRow) {
                                    if (i3.row > focusedRow) {
                                        break;
                                    }
                                    view.addFocusables(arrayList, i, i2);
                                }
                            }
                            loop_start2 = loop_start;
                        }
                        i4 = i3;
                        loop_start2 = loop_start;
                    } else {
                        view2 = focused;
                        i4 = i3;
                        loop_start2 = loop_start;
                    }
                    i5 = i6 + inc;
                    loop_end = loop_end2;
                    focused = view2;
                    i3 = i4;
                    loop_start = loop_start2;
                }
                recyclerView2 = recyclerView;
                z = true;
            }
            return true;
        }
        return z;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasCreatedLastItem() {
        int count = getItemCount();
        return count == 0 || this.mBaseGridView.findViewHolderForAdapterPosition(count - 1) != null;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean hasCreatedFirstItem() {
        return getItemCount() == 0 || this.mBaseGridView.findViewHolderForAdapterPosition(0) != null;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean isItemFullyVisible(int pos) {
        ViewHolder vh = this.mBaseGridView.findViewHolderForAdapterPosition(pos);
        boolean z = false;
        if (vh == null) {
            return false;
        }
        if (vh.itemView.getLeft() >= 0 && vh.itemView.getRight() <= this.mBaseGridView.getWidth() && vh.itemView.getTop() >= 0 && vh.itemView.getBottom() <= this.mBaseGridView.getHeight()) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean canScrollTo(View view) {
        return view.getVisibility() == 0 && (!hasFocus() || view.hasFocusable());
    }

    /* Access modifiers changed, original: 0000 */
    public boolean gridOnRequestFocusInDescendants(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
        switch (this.mFocusScrollStrategy) {
            case 1:
            case 2:
                return gridOnRequestFocusInDescendantsUnaligned(recyclerView, direction, previouslyFocusedRect);
            default:
                return gridOnRequestFocusInDescendantsAligned(recyclerView, direction, previouslyFocusedRect);
        }
    }

    private boolean gridOnRequestFocusInDescendantsAligned(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
        View view = findViewByPosition(this.mFocusPosition);
        if (view != null) {
            return view.requestFocus(direction, previouslyFocusedRect);
        }
        return false;
    }

    private boolean gridOnRequestFocusInDescendantsUnaligned(RecyclerView recyclerView, int direction, Rect previouslyFocusedRect) {
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
        int left = this.mWindowAlignment.mainAxis().getPaddingMin();
        int right = this.mWindowAlignment.mainAxis().getClientSize() + left;
        for (int i = index; i != end; i += increment) {
            View child = getChildAt(i);
            if (child.getVisibility() == 0 && getViewMin(child) >= left && getViewMax(child) <= right && child.requestFocus(direction, previouslyFocusedRect)) {
                return true;
            }
        }
        return false;
    }

    private int getMovement(int direction) {
        int i;
        if (this.mOrientation == 0) {
            i = 0;
            if (direction == 17) {
                if ((this.mFlag & 262144) != 0) {
                    i = 1;
                }
                return i;
            } else if (direction == 33) {
                return 2;
            } else {
                if (direction == 66) {
                    if ((this.mFlag & 262144) == 0) {
                        i = 1;
                    }
                    return i;
                } else if (direction != 130) {
                    return 17;
                } else {
                    return 3;
                }
            }
        } else if (this.mOrientation != 1) {
            return 17;
        } else {
            i = 2;
            if (direction == 17) {
                if ((this.mFlag & 524288) != 0) {
                    i = 3;
                }
                return i;
            } else if (direction == 33) {
                return 0;
            } else {
                if (direction == 66) {
                    if ((this.mFlag & 524288) == 0) {
                        i = 3;
                    }
                    return i;
                } else if (direction != 130) {
                    return 17;
                } else {
                    return 1;
                }
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getChildDrawingOrder(RecyclerView recyclerView, int childCount, int i) {
        View view = findViewByPosition(this.mFocusPosition);
        if (view == null) {
            return i;
        }
        int focusIndex = recyclerView.indexOfChild(view);
        if (i < focusIndex) {
            return i;
        }
        if (i < childCount - 1) {
            return ((focusIndex + childCount) - 1) - i;
        }
        return focusIndex;
    }

    public void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter) {
        if (oldAdapter != null) {
            discardLayoutInfo();
            this.mFocusPosition = -1;
            this.mFocusPositionOffset = 0;
            this.mChildrenStates.clear();
        }
        if (newAdapter instanceof FacetProviderAdapter) {
            this.mFacetProviderAdapter = (FacetProviderAdapter) newAdapter;
        } else {
            this.mFacetProviderAdapter = null;
        }
        super.onAdapterChanged(oldAdapter, newAdapter);
    }

    private void discardLayoutInfo() {
        this.mGrid = null;
        this.mRowSizeSecondary = null;
        this.mFlag &= -1025;
    }

    public void setLayoutEnabled(boolean layoutEnabled) {
        int i = 512;
        if (((this.mFlag & 512) != 0) != layoutEnabled) {
            int i2 = this.mFlag & -513;
            if (!layoutEnabled) {
                i = 0;
            }
            this.mFlag = i2 | i;
            requestLayout();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void setChildrenVisibility(int visibility) {
        this.mChildVisibility = visibility;
        if (this.mChildVisibility != -1) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).setVisibility(this.mChildVisibility);
            }
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState();
        ss.index = getSelection();
        Bundle bundle = this.mChildrenStates.saveAsBundle();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            int position = getAdapterPositionByView(view);
            if (position != -1) {
                bundle = this.mChildrenStates.saveOnScreenView(bundle, view, position);
            }
        }
        ss.childStates = bundle;
        return ss;
    }

    /* Access modifiers changed, original: 0000 */
    public void onChildRecycled(ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (position != -1) {
            this.mChildrenStates.saveOffscreenView(holder.itemView, position);
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState loadingState = (SavedState) state;
            this.mFocusPosition = loadingState.index;
            this.mFocusPositionOffset = 0;
            this.mChildrenStates.loadFromBundle(loadingState.childStates);
            this.mFlag |= 256;
            requestLayout();
        }
    }

    public int getRowCountForAccessibility(Recycler recycler, State state) {
        if (this.mOrientation != 0 || this.mGrid == null) {
            return super.getRowCountForAccessibility(recycler, state);
        }
        return this.mGrid.getNumRows();
    }

    public int getColumnCountForAccessibility(Recycler recycler, State state) {
        if (this.mOrientation != 1 || this.mGrid == null) {
            return super.getColumnCountForAccessibility(recycler, state);
        }
        return this.mGrid.getNumRows();
    }

    public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View host, AccessibilityNodeInfoCompat info) {
        android.view.ViewGroup.LayoutParams lp = host.getLayoutParams();
        if (this.mGrid != null && (lp instanceof LayoutParams)) {
            int position = ((LayoutParams) lp).getViewAdapterPosition();
            int rowIndex = position >= 0 ? this.mGrid.getRowIndex(position) : -1;
            if (rowIndex >= 0) {
                int guessSpanIndex = position / this.mGrid.getNumRows();
                if (this.mOrientation == 0) {
                    info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(rowIndex, 1, guessSpanIndex, 1, false, false));
                } else {
                    info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(guessSpanIndex, 1, rowIndex, 1, false, false));
                }
            }
        }
    }

    public boolean performAccessibilityAction(Recycler recycler, State state, int action, Bundle args) {
        if (!isScrollEnabled()) {
            return true;
        }
        saveContext(recycler, state);
        int translatedAction = action;
        boolean reverseFlowPrimary = (this.mFlag & 262144) != 0;
        if (VERSION.SDK_INT >= 23) {
            if (this.mOrientation == 0) {
                if (action == AccessibilityActionCompat.ACTION_SCROLL_LEFT.getId()) {
                    translatedAction = reverseFlowPrimary ? 4096 : 8192;
                } else if (action == AccessibilityActionCompat.ACTION_SCROLL_RIGHT.getId()) {
                    translatedAction = reverseFlowPrimary ? 8192 : 4096;
                }
            } else if (action == AccessibilityActionCompat.ACTION_SCROLL_UP.getId()) {
                translatedAction = 8192;
            } else if (action == AccessibilityActionCompat.ACTION_SCROLL_DOWN.getId()) {
                translatedAction = 4096;
            }
        }
        if (translatedAction == 4096) {
            processPendingMovement(true);
            processSelectionMoves(false, 1);
        } else if (translatedAction == 8192) {
            processPendingMovement(false);
            processSelectionMoves(false, -1);
        }
        leaveContext();
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public int processSelectionMoves(boolean preventScroll, int moves) {
        if (this.mGrid == null) {
            return moves;
        }
        int focusPosition = this.mFocusPosition;
        int focusedRow = focusPosition != -1 ? this.mGrid.getRowIndex(focusPosition) : -1;
        View newSelected = null;
        int i = 0;
        int count = getChildCount();
        while (i < count && moves != 0) {
            int index = moves > 0 ? i : (count - 1) - i;
            View child = getChildAt(index);
            if (canScrollTo(child)) {
                int position = getAdapterPositionByIndex(index);
                int rowIndex = this.mGrid.getRowIndex(position);
                if (focusedRow == -1) {
                    focusPosition = position;
                    newSelected = child;
                    focusedRow = rowIndex;
                } else if (rowIndex == focusedRow && ((moves > 0 && position > focusPosition) || (moves < 0 && position < focusPosition))) {
                    focusPosition = position;
                    newSelected = child;
                    if (moves > 0) {
                        moves--;
                    } else {
                        moves++;
                    }
                }
            }
            i++;
        }
        if (newSelected != null) {
            if (preventScroll) {
                if (hasFocus()) {
                    this.mFlag |= 32;
                    newSelected.requestFocus();
                    this.mFlag &= -33;
                }
                this.mFocusPosition = focusPosition;
                this.mSubFocusPosition = 0;
            } else {
                scrollToView(newSelected, true);
            }
        }
        return moves;
    }

    public void onInitializeAccessibilityNodeInfo(Recycler recycler, State state, AccessibilityNodeInfoCompat info) {
        saveContext(recycler, state);
        int count = state.getItemCount();
        boolean reverseFlowPrimary = (this.mFlag & 262144) != 0;
        if (count > 1 && !isItemFullyVisible(0)) {
            if (VERSION.SDK_INT < 23) {
                info.addAction(8192);
            } else if (this.mOrientation == 0) {
                info.addAction(reverseFlowPrimary ? AccessibilityActionCompat.ACTION_SCROLL_RIGHT : AccessibilityActionCompat.ACTION_SCROLL_LEFT);
            } else {
                info.addAction(AccessibilityActionCompat.ACTION_SCROLL_UP);
            }
            info.setScrollable(true);
        }
        if (count > 1 && !isItemFullyVisible(count - 1)) {
            if (VERSION.SDK_INT < 23) {
                info.addAction(4096);
            } else if (this.mOrientation == 0) {
                info.addAction(reverseFlowPrimary ? AccessibilityActionCompat.ACTION_SCROLL_LEFT : AccessibilityActionCompat.ACTION_SCROLL_RIGHT);
            } else {
                info.addAction(AccessibilityActionCompat.ACTION_SCROLL_DOWN);
            }
            info.setScrollable(true);
        }
        info.setCollectionInfo(CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state)));
        leaveContext();
    }
}
