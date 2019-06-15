package android.support.v17.leanback.widget;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.CircularIntArray;
import android.support.v7.widget.RecyclerView.LayoutManager.LayoutPrefetchRegistry;
import android.util.SparseIntArray;
import java.io.PrintWriter;
import java.util.Arrays;

abstract class Grid {
    public static final int START_DEFAULT = -1;
    protected int mFirstVisibleIndex = -1;
    protected int mLastVisibleIndex = -1;
    protected int mNumRows;
    protected Provider mProvider;
    protected boolean mReversedFlow;
    protected int mSpacing;
    protected int mStartIndex = -1;
    Object[] mTmpItem = new Object[1];
    protected CircularIntArray[] mTmpItemPositionsInRows;

    public static class Location {
        public int row;

        public Location(int row) {
            this.row = row;
        }
    }

    public interface Provider {
        void addItem(Object obj, int i, int i2, int i3, int i4);

        int createItem(int i, boolean z, Object[] objArr, boolean z2);

        int getCount();

        int getEdge(int i);

        int getMinIndex();

        int getSize(int i);

        void removeItem(int i);
    }

    public abstract boolean appendVisibleItems(int i, boolean z);

    public abstract void debugPrint(PrintWriter printWriter);

    public abstract int findRowMax(boolean z, int i, int[] iArr);

    public abstract int findRowMin(boolean z, int i, int[] iArr);

    public abstract CircularIntArray[] getItemPositionsInRows(int i, int i2);

    public abstract Location getLocation(int i);

    public abstract boolean prependVisibleItems(int i, boolean z);

    Grid() {
    }

    public static Grid createGrid(int rows) {
        if (rows == 1) {
            return new SingleRow();
        }
        Grid grid = new StaggeredGridDefault();
        grid.setNumRows(rows);
        return grid;
    }

    public final void setSpacing(int spacing) {
        this.mSpacing = spacing;
    }

    public final void setReversedFlow(boolean reversedFlow) {
        this.mReversedFlow = reversedFlow;
    }

    public boolean isReversedFlow() {
        return this.mReversedFlow;
    }

    public void setProvider(Provider provider) {
        this.mProvider = provider;
    }

    public void setStart(int startIndex) {
        this.mStartIndex = startIndex;
    }

    public int getNumRows() {
        return this.mNumRows;
    }

    /* Access modifiers changed, original: 0000 */
    public void setNumRows(int numRows) {
        if (numRows <= 0) {
            throw new IllegalArgumentException();
        } else if (this.mNumRows != numRows) {
            this.mNumRows = numRows;
            this.mTmpItemPositionsInRows = new CircularIntArray[this.mNumRows];
            for (int i = 0; i < this.mNumRows; i++) {
                this.mTmpItemPositionsInRows[i] = new CircularIntArray();
            }
        }
    }

    public final int getFirstVisibleIndex() {
        return this.mFirstVisibleIndex;
    }

    public final int getLastVisibleIndex() {
        return this.mLastVisibleIndex;
    }

    public void resetVisibleIndex() {
        this.mLastVisibleIndex = -1;
        this.mFirstVisibleIndex = -1;
    }

    public void invalidateItemsAfter(int index) {
        if (index >= 0 && this.mLastVisibleIndex >= 0) {
            if (this.mLastVisibleIndex >= index) {
                this.mLastVisibleIndex = index - 1;
            }
            resetVisibleIndexIfEmpty();
            if (getFirstVisibleIndex() < 0) {
                setStart(index);
            }
        }
    }

    public final int getRowIndex(int index) {
        Location location = getLocation(index);
        if (location == null) {
            return -1;
        }
        return location.row;
    }

    public final int findRowMin(boolean findLarge, @Nullable int[] indices) {
        return findRowMin(findLarge, this.mReversedFlow ? this.mLastVisibleIndex : this.mFirstVisibleIndex, indices);
    }

    public final int findRowMax(boolean findLarge, @Nullable int[] indices) {
        return findRowMax(findLarge, this.mReversedFlow ? this.mFirstVisibleIndex : this.mLastVisibleIndex, indices);
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean checkAppendOverLimit(int toLimit) {
        boolean z = false;
        if (this.mLastVisibleIndex < 0) {
            return false;
        }
        if (this.mReversedFlow ? findRowMin(true, null) > this.mSpacing + toLimit : findRowMax(false, null) < toLimit - this.mSpacing) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean checkPrependOverLimit(int toLimit) {
        boolean z = false;
        if (this.mLastVisibleIndex < 0) {
            return false;
        }
        if (this.mReversedFlow ? findRowMax(false, null) < toLimit - this.mSpacing : findRowMin(true, null) > this.mSpacing + toLimit) {
            z = true;
        }
        return z;
    }

    public final CircularIntArray[] getItemPositionsInRows() {
        return getItemPositionsInRows(getFirstVisibleIndex(), getLastVisibleIndex());
    }

    public final boolean prependOneColumnVisibleItems() {
        return prependVisibleItems(this.mReversedFlow ? Integer.MIN_VALUE : Integer.MAX_VALUE, true);
    }

    public final void prependVisibleItems(int toLimit) {
        prependVisibleItems(toLimit, false);
    }

    public boolean appendOneColumnVisibleItems() {
        return appendVisibleItems(this.mReversedFlow ? Integer.MAX_VALUE : Integer.MIN_VALUE, true);
    }

    public final void appendVisibleItems(int toLimit) {
        appendVisibleItems(toLimit, false);
    }

    public void removeInvisibleItemsAtEnd(int aboveIndex, int toLimit) {
        while (this.mLastVisibleIndex >= this.mFirstVisibleIndex && this.mLastVisibleIndex > aboveIndex) {
            boolean z = false;
            if (this.mReversedFlow ? this.mProvider.getEdge(this.mLastVisibleIndex) > toLimit : this.mProvider.getEdge(this.mLastVisibleIndex) < toLimit) {
                z = true;
            }
            if (!z) {
                break;
            }
            this.mProvider.removeItem(this.mLastVisibleIndex);
            this.mLastVisibleIndex--;
        }
        resetVisibleIndexIfEmpty();
    }

    public void removeInvisibleItemsAtFront(int belowIndex, int toLimit) {
        while (this.mLastVisibleIndex >= this.mFirstVisibleIndex && this.mFirstVisibleIndex < belowIndex) {
            int size = this.mProvider.getSize(this.mFirstVisibleIndex);
            boolean z = false;
            if (this.mReversedFlow ? this.mProvider.getEdge(this.mFirstVisibleIndex) - size < toLimit : this.mProvider.getEdge(this.mFirstVisibleIndex) + size > toLimit) {
                z = true;
            }
            if (!z) {
                break;
            }
            this.mProvider.removeItem(this.mFirstVisibleIndex);
            this.mFirstVisibleIndex++;
        }
        resetVisibleIndexIfEmpty();
    }

    private void resetVisibleIndexIfEmpty() {
        if (this.mLastVisibleIndex < this.mFirstVisibleIndex) {
            resetVisibleIndex();
        }
    }

    public void fillDisappearingItems(int[] positions, int positionsLength, SparseIntArray positionToRow) {
        int firstDisappearingIndex;
        int edge;
        int disappearingIndex;
        int disappearingRow;
        int[] iArr = positions;
        int i = positionsLength;
        SparseIntArray sparseIntArray = positionToRow;
        int lastPos = getLastVisibleIndex();
        int resultSearchLast = lastPos >= 0 ? Arrays.binarySearch(iArr, 0, i, lastPos) : 0;
        if (resultSearchLast < 0) {
            firstDisappearingIndex = (-resultSearchLast) - 1;
            if (this.mReversedFlow) {
                edge = (this.mProvider.getEdge(lastPos) - this.mProvider.getSize(lastPos)) - this.mSpacing;
            } else {
                edge = (this.mProvider.getEdge(lastPos) + this.mProvider.getSize(lastPos)) + this.mSpacing;
            }
            int edge2 = edge;
            for (edge = firstDisappearingIndex; edge < i; edge++) {
                disappearingIndex = iArr[edge];
                disappearingRow = sparseIntArray.get(disappearingIndex);
                if (disappearingRow < 0) {
                    disappearingRow = 0;
                }
                int disappearingRow2 = disappearingRow;
                int size = this.mProvider.createItem(disappearingIndex, true, this.mTmpItem, true);
                this.mProvider.addItem(this.mTmpItem[0], disappearingIndex, size, disappearingRow2, edge2);
                if (this.mReversedFlow) {
                    disappearingRow = (edge2 - size) - this.mSpacing;
                } else {
                    disappearingRow = (edge2 + size) + this.mSpacing;
                }
                edge2 = disappearingRow;
            }
        }
        firstDisappearingIndex = getFirstVisibleIndex();
        edge = firstDisappearingIndex >= 0 ? Arrays.binarySearch(iArr, 0, i, firstDisappearingIndex) : 0;
        if (edge < 0) {
            int edge3;
            disappearingRow = (-edge) - 2;
            if (this.mReversedFlow) {
                edge3 = this.mProvider.getEdge(firstDisappearingIndex);
            } else {
                edge3 = this.mProvider.getEdge(firstDisappearingIndex);
            }
            int edge4 = edge3;
            for (edge3 = disappearingRow; edge3 >= 0; edge3--) {
                disappearingIndex = iArr[edge3];
                int disappearingRow3 = sparseIntArray.get(disappearingIndex);
                if (disappearingRow3 < 0) {
                    disappearingRow3 = 0;
                }
                int disappearingRow4 = disappearingRow3;
                int size2 = this.mProvider.createItem(disappearingIndex, false, this.mTmpItem, true);
                if (this.mReversedFlow) {
                    disappearingRow3 = (this.mSpacing + edge4) + size2;
                } else {
                    disappearingRow3 = (edge4 - this.mSpacing) - size2;
                }
                edge4 = disappearingRow3;
                int disappearingIndex2 = disappearingIndex;
                this.mProvider.addItem(this.mTmpItem[0], disappearingIndex, size2, disappearingRow4, edge4);
            }
        }
    }

    public void collectAdjacentPrefetchPositions(int fromLimit, int da, @NonNull LayoutPrefetchRegistry layoutPrefetchRegistry) {
    }
}
