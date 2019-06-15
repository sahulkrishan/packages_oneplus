package android.support.v17.leanback.widget;

import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.Grid.Location;
import android.support.v4.util.CircularIntArray;
import android.support.v7.widget.RecyclerView.LayoutManager.LayoutPrefetchRegistry;
import java.io.PrintWriter;

class SingleRow extends Grid {
    private final Location mTmpLocation = new Location(0);

    SingleRow() {
        setNumRows(1);
    }

    public final Location getLocation(int index) {
        return this.mTmpLocation;
    }

    public final void debugPrint(PrintWriter pw) {
        pw.print("SingleRow<");
        pw.print(this.mFirstVisibleIndex);
        pw.print(",");
        pw.print(this.mLastVisibleIndex);
        pw.print(">");
        pw.println();
    }

    /* Access modifiers changed, original: 0000 */
    public int getStartIndexForAppend() {
        if (this.mLastVisibleIndex >= 0) {
            return this.mLastVisibleIndex + 1;
        }
        if (this.mStartIndex != -1) {
            return Math.min(this.mStartIndex, this.mProvider.getCount() - 1);
        }
        return 0;
    }

    /* Access modifiers changed, original: 0000 */
    public int getStartIndexForPrepend() {
        if (this.mFirstVisibleIndex >= 0) {
            return this.mFirstVisibleIndex - 1;
        }
        if (this.mStartIndex != -1) {
            return Math.min(this.mStartIndex, this.mProvider.getCount() - 1);
        }
        return this.mProvider.getCount() - 1;
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean prependVisibleItems(int toLimit, boolean oneColumnMode) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!oneColumnMode && checkPrependOverLimit(toLimit)) {
            return false;
        }
        boolean filledOne = false;
        int minIndex = this.mProvider.getMinIndex();
        for (int index = getStartIndexForPrepend(); index >= minIndex; index--) {
            int edge;
            int size = this.mProvider.createItem(index, false, this.mTmpItem, false);
            if (this.mFirstVisibleIndex < 0 || this.mLastVisibleIndex < 0) {
                edge = this.mReversedFlow ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                this.mFirstVisibleIndex = index;
                this.mLastVisibleIndex = index;
            } else {
                if (this.mReversedFlow) {
                    edge = (this.mProvider.getEdge(index + 1) + this.mSpacing) + size;
                } else {
                    edge = (this.mProvider.getEdge(index + 1) - this.mSpacing) - size;
                }
                this.mFirstVisibleIndex = index;
            }
            this.mProvider.addItem(this.mTmpItem[0], index, size, 0, edge);
            filledOne = true;
            if (oneColumnMode || checkPrependOverLimit(toLimit)) {
                break;
            }
        }
        return filledOne;
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean appendVisibleItems(int toLimit, boolean oneColumnMode) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!oneColumnMode && checkAppendOverLimit(toLimit)) {
            return false;
        }
        boolean filledOne = false;
        for (int index = getStartIndexForAppend(); index < this.mProvider.getCount(); index++) {
            int edge;
            int size = this.mProvider.createItem(index, true, this.mTmpItem, false);
            if (this.mFirstVisibleIndex < 0 || this.mLastVisibleIndex < 0) {
                edge = this.mReversedFlow ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                this.mFirstVisibleIndex = index;
                this.mLastVisibleIndex = index;
            } else {
                if (this.mReversedFlow) {
                    edge = (this.mProvider.getEdge(index - 1) - this.mProvider.getSize(index - 1)) - this.mSpacing;
                } else {
                    edge = (this.mProvider.getEdge(index - 1) + this.mProvider.getSize(index - 1)) + this.mSpacing;
                }
                this.mLastVisibleIndex = index;
            }
            this.mProvider.addItem(this.mTmpItem[0], index, size, 0, edge);
            filledOne = true;
            if (oneColumnMode || checkAppendOverLimit(toLimit)) {
                break;
            }
        }
        return filledOne;
    }

    public void collectAdjacentPrefetchPositions(int fromLimit, int da, @NonNull LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int indexToPrefetch;
        int itemSizeWithSpace;
        if (this.mReversedFlow ? da <= 0 : da >= 0) {
            if (getLastVisibleIndex() != this.mProvider.getCount() - 1) {
                indexToPrefetch = getStartIndexForAppend();
                itemSizeWithSpace = this.mProvider.getSize(this.mLastVisibleIndex) + this.mSpacing;
                itemSizeWithSpace = this.mProvider.getEdge(this.mLastVisibleIndex) + (this.mReversedFlow ? -itemSizeWithSpace : itemSizeWithSpace);
            } else {
                return;
            }
        } else if (getFirstVisibleIndex() != 0) {
            indexToPrefetch = getStartIndexForPrepend();
            itemSizeWithSpace = this.mProvider.getEdge(this.mFirstVisibleIndex) + (this.mReversedFlow ? this.mSpacing : -this.mSpacing);
        } else {
            return;
        }
        layoutPrefetchRegistry.addPosition(indexToPrefetch, Math.abs(itemSizeWithSpace - fromLimit));
    }

    public final CircularIntArray[] getItemPositionsInRows(int startPos, int endPos) {
        this.mTmpItemPositionsInRows[0].clear();
        this.mTmpItemPositionsInRows[0].addLast(startPos);
        this.mTmpItemPositionsInRows[0].addLast(endPos);
        return this.mTmpItemPositionsInRows;
    }

    /* Access modifiers changed, original: protected|final */
    public final int findRowMin(boolean findLarge, int indexLimit, int[] indices) {
        if (indices != null) {
            indices[0] = 0;
            indices[1] = indexLimit;
        }
        if (this.mReversedFlow) {
            return this.mProvider.getEdge(indexLimit) - this.mProvider.getSize(indexLimit);
        }
        return this.mProvider.getEdge(indexLimit);
    }

    /* Access modifiers changed, original: protected|final */
    public final int findRowMax(boolean findLarge, int indexLimit, int[] indices) {
        if (indices != null) {
            indices[0] = 0;
            indices[1] = indexLimit;
        }
        if (this.mReversedFlow) {
            return this.mProvider.getEdge(indexLimit);
        }
        return this.mProvider.getEdge(indexLimit) + this.mProvider.getSize(indexLimit);
    }
}
