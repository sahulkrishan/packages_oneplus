package android.support.v17.leanback.widget;

import android.support.v17.leanback.widget.StaggeredGrid.Location;

final class StaggeredGridDefault extends StaggeredGrid {
    StaggeredGridDefault() {
    }

    /* Access modifiers changed, original: 0000 */
    public int getRowMax(int rowIndex) {
        if (this.mFirstVisibleIndex < 0) {
            return Integer.MIN_VALUE;
        }
        int edge;
        if (!this.mReversedFlow) {
            edge = this.mProvider.getEdge(this.mLastVisibleIndex);
            Location loc = getLocation((int) this.mLastVisibleIndex);
            if (loc.row != rowIndex) {
                int i = this.mLastVisibleIndex;
                while (true) {
                    i--;
                    if (i < getFirstIndex()) {
                        break;
                    }
                    edge -= loc.offset;
                    loc = getLocation(i);
                    if (loc.row == rowIndex) {
                        return loc.size + edge;
                    }
                }
            } else {
                return loc.size + edge;
            }
        }
        edge = this.mProvider.getEdge(this.mFirstVisibleIndex);
        if (getLocation(this.mFirstVisibleIndex).row != rowIndex) {
            int i2 = this.mFirstVisibleIndex;
            while (true) {
                i2++;
                if (i2 > getLastIndex()) {
                    break;
                }
                Location loc2 = getLocation(i2);
                edge += loc2.offset;
                if (loc2.row == rowIndex) {
                    return edge;
                }
            }
        } else {
            return edge;
        }
        return Integer.MIN_VALUE;
    }

    /* Access modifiers changed, original: 0000 */
    public int getRowMin(int rowIndex) {
        if (this.mFirstVisibleIndex < 0) {
            return Integer.MAX_VALUE;
        }
        int edge;
        if (!this.mReversedFlow) {
            edge = this.mProvider.getEdge(this.mFirstVisibleIndex);
            if (getLocation(this.mFirstVisibleIndex).row != rowIndex) {
                int i = this.mFirstVisibleIndex;
                while (true) {
                    i++;
                    if (i > getLastIndex()) {
                        break;
                    }
                    Location loc = getLocation(i);
                    edge += loc.offset;
                    if (loc.row == rowIndex) {
                        return edge;
                    }
                }
            } else {
                return edge;
            }
        }
        edge = this.mProvider.getEdge(this.mLastVisibleIndex);
        Location loc2 = getLocation((int) this.mLastVisibleIndex);
        if (loc2.row != rowIndex) {
            int i2 = this.mLastVisibleIndex;
            while (true) {
                i2--;
                if (i2 < getFirstIndex()) {
                    break;
                }
                edge -= loc2.offset;
                loc2 = getLocation(i2);
                if (loc2.row == rowIndex) {
                    return edge - loc2.size;
                }
            }
        } else {
            return edge - loc2.size;
        }
        return Integer.MAX_VALUE;
    }

    public int findRowMax(boolean findLarge, int indexLimit, int[] indices) {
        int value;
        int edge = this.mProvider.getEdge(indexLimit);
        Location loc = getLocation(indexLimit);
        int row = loc.row;
        int index = indexLimit;
        int visitedRows = 1;
        int visitRow = row;
        int i;
        if (this.mReversedFlow) {
            value = edge;
            i = indexLimit + 1;
            while (visitedRows < this.mNumRows && i <= this.mLastVisibleIndex) {
                loc = getLocation(i);
                edge += loc.offset;
                if (loc.row != visitRow) {
                    visitRow = loc.row;
                    visitedRows++;
                    if (findLarge) {
                        if (edge <= value) {
                        }
                    } else if (edge >= value) {
                    }
                    row = visitRow;
                    value = edge;
                    index = i;
                }
                i++;
            }
        } else {
            value = this.mProvider.getSize(indexLimit) + edge;
            i = indexLimit - 1;
            while (visitedRows < this.mNumRows && i >= this.mFirstVisibleIndex) {
                edge -= loc.offset;
                loc = getLocation(i);
                if (loc.row != visitRow) {
                    visitRow = loc.row;
                    visitedRows++;
                    int newValue = this.mProvider.getSize(i) + edge;
                    if (findLarge) {
                        if (newValue <= value) {
                        }
                    } else if (newValue >= value) {
                    }
                    row = visitRow;
                    value = newValue;
                    index = i;
                }
                i--;
            }
        }
        if (indices != null) {
            indices[0] = row;
            indices[1] = index;
        }
        return value;
    }

    public int findRowMin(boolean findLarge, int indexLimit, int[] indices) {
        int value;
        int edge = this.mProvider.getEdge(indexLimit);
        Location loc = getLocation(indexLimit);
        int row = loc.row;
        int index = indexLimit;
        int visitedRows = 1;
        int visitRow = row;
        int i;
        if (this.mReversedFlow) {
            value = edge - this.mProvider.getSize(indexLimit);
            i = indexLimit - 1;
            while (visitedRows < this.mNumRows && i >= this.mFirstVisibleIndex) {
                edge -= loc.offset;
                loc = getLocation(i);
                if (loc.row != visitRow) {
                    visitRow = loc.row;
                    visitedRows++;
                    int newValue = edge - this.mProvider.getSize(i);
                    if (findLarge) {
                        if (newValue <= value) {
                        }
                    } else if (newValue >= value) {
                    }
                    value = newValue;
                    row = visitRow;
                    index = i;
                }
                i--;
            }
        } else {
            value = edge;
            i = indexLimit + 1;
            while (visitedRows < this.mNumRows && i <= this.mLastVisibleIndex) {
                loc = getLocation(i);
                edge += loc.offset;
                if (loc.row != visitRow) {
                    visitRow = loc.row;
                    visitedRows++;
                    if (findLarge) {
                        if (edge <= value) {
                        }
                    } else if (edge >= value) {
                    }
                    value = edge;
                    row = visitRow;
                    index = i;
                }
                i++;
            }
        }
        if (indices != null) {
            indices[0] = row;
            indices[1] = index;
        }
        return value;
    }

    private int findRowEdgeLimitSearchIndex(boolean append) {
        boolean wrapped = false;
        int index;
        int row;
        if (append) {
            for (index = this.mLastVisibleIndex; index >= this.mFirstVisibleIndex; index--) {
                row = getLocation(index).row;
                if (row == 0) {
                    wrapped = true;
                } else if (wrapped && row == this.mNumRows - 1) {
                    return index;
                }
            }
        } else {
            for (index = this.mFirstVisibleIndex; index <= this.mLastVisibleIndex; index++) {
                row = getLocation(index).row;
                if (row == this.mNumRows - 1) {
                    wrapped = true;
                } else if (wrapped && row == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    /* Access modifiers changed, original: protected */
    public boolean appendVisibleItemsWithoutCache(int toLimit, boolean oneColumnMode) {
        int itemIndex;
        int rowIndex;
        int edgeLimit;
        boolean edgeLimitIsValid;
        int edgeLimitSearchIndex;
        int count = this.mProvider.getCount();
        if (this.mLastVisibleIndex < 0) {
            itemIndex = this.mStartIndex != -1 ? this.mStartIndex : 0;
            rowIndex = (this.mLocations.size() > 0 ? getLocation(getLastIndex()).row + 1 : itemIndex) % this.mNumRows;
            edgeLimit = 0;
            edgeLimitIsValid = false;
        } else if (this.mLastVisibleIndex < getLastIndex()) {
            return false;
        } else {
            int edgeLimit2;
            itemIndex = this.mLastVisibleIndex + 1;
            rowIndex = getLocation(this.mLastVisibleIndex).row;
            edgeLimitSearchIndex = findRowEdgeLimitSearchIndex(true);
            if (edgeLimitSearchIndex < 0) {
                edgeLimit2 = Integer.MIN_VALUE;
                edgeLimit = 0;
                while (edgeLimit < this.mNumRows) {
                    edgeLimit2 = this.mReversedFlow ? getRowMin(edgeLimit) : getRowMax(edgeLimit);
                    if (edgeLimit2 != Integer.MIN_VALUE) {
                        break;
                    }
                    edgeLimit++;
                }
            } else {
                if (this.mReversedFlow) {
                    edgeLimit = findRowMin(false, edgeLimitSearchIndex, null);
                } else {
                    edgeLimit = findRowMax(true, edgeLimitSearchIndex, null);
                }
                edgeLimit2 = edgeLimit;
            }
            edgeLimit = edgeLimit2;
            if (this.mReversedFlow ? getRowMin(rowIndex) > edgeLimit : getRowMax(rowIndex) < edgeLimit) {
                rowIndex++;
                if (rowIndex == this.mNumRows) {
                    rowIndex = 0;
                    edgeLimit = this.mReversedFlow ? findRowMin(false, null) : findRowMax(true, null);
                }
            }
            edgeLimitIsValid = true;
        }
        boolean edgeLimitIsValid2 = edgeLimitIsValid;
        edgeLimitSearchIndex = itemIndex;
        boolean filledOne = false;
        loop1:
        while (true) {
            if (rowIndex < this.mNumRows) {
                if (edgeLimitSearchIndex == count || (!oneColumnMode && checkAppendOverLimit(toLimit))) {
                    return filledOne;
                }
                int location = this.mReversedFlow ? getRowMin(rowIndex) : getRowMax(rowIndex);
                if (location != Integer.MAX_VALUE && location != Integer.MIN_VALUE) {
                    location += this.mReversedFlow ? -this.mSpacing : this.mSpacing;
                } else if (rowIndex == 0) {
                    location = this.mReversedFlow ? getRowMin(this.mNumRows - 1) : getRowMax(this.mNumRows - 1);
                    if (!(location == Integer.MAX_VALUE || location == Integer.MIN_VALUE)) {
                        location += this.mReversedFlow ? -this.mSpacing : this.mSpacing;
                    }
                } else {
                    location = this.mReversedFlow ? getRowMax(rowIndex - 1) : getRowMin(rowIndex - 1);
                }
                int itemIndex2 = edgeLimitSearchIndex + 1;
                edgeLimitSearchIndex = appendVisibleItemToRow(edgeLimitSearchIndex, rowIndex, location);
                filledOne = true;
                if (edgeLimitIsValid2) {
                    while (true) {
                        if (!this.mReversedFlow) {
                            if (location + edgeLimitSearchIndex >= edgeLimit) {
                                break;
                            }
                        } else if (location - edgeLimitSearchIndex <= edgeLimit) {
                            break;
                        }
                        if (itemIndex2 == count || (!oneColumnMode && checkAppendOverLimit(toLimit))) {
                            return true;
                        }
                        location += this.mReversedFlow ? (-edgeLimitSearchIndex) - this.mSpacing : this.mSpacing + edgeLimitSearchIndex;
                        int itemIndex3 = itemIndex2 + 1;
                        edgeLimitSearchIndex = appendVisibleItemToRow(itemIndex2, rowIndex, location);
                        itemIndex2 = itemIndex3;
                    }
                } else {
                    edgeLimitIsValid2 = true;
                    edgeLimit = this.mReversedFlow ? getRowMin(rowIndex) : getRowMax(rowIndex);
                }
                edgeLimitSearchIndex = itemIndex2;
                rowIndex++;
            } else if (oneColumnMode) {
                return filledOne;
            } else {
                edgeLimit = this.mReversedFlow ? findRowMin(false, null) : findRowMax(true, null);
                rowIndex = 0;
            }
        }
        return filledOne;
    }

    /* Access modifiers changed, original: protected */
    public boolean prependVisibleItemsWithoutCache(int toLimit, boolean oneColumnMode) {
        int itemIndex;
        int rowIndex;
        int edgeLimit;
        boolean edgeLimitIsValid;
        int edgeLimitSearchIndex;
        if (this.mFirstVisibleIndex < 0) {
            itemIndex = this.mStartIndex != -1 ? this.mStartIndex : 0;
            rowIndex = (this.mLocations.size() > 0 ? (getLocation(getFirstIndex()).row + this.mNumRows) - 1 : itemIndex) % this.mNumRows;
            edgeLimit = 0;
            edgeLimitIsValid = false;
        } else if (this.mFirstVisibleIndex > getFirstIndex()) {
            return false;
        } else {
            int i;
            itemIndex = this.mFirstVisibleIndex - 1;
            rowIndex = getLocation(this.mFirstVisibleIndex).row;
            edgeLimitSearchIndex = findRowEdgeLimitSearchIndex(false);
            if (edgeLimitSearchIndex < 0) {
                rowIndex--;
                edgeLimit = Integer.MAX_VALUE;
                i = this.mNumRows - 1;
                while (i >= 0) {
                    edgeLimit = this.mReversedFlow ? getRowMax(i) : getRowMin(i);
                    if (edgeLimit != Integer.MAX_VALUE) {
                        break;
                    }
                    i--;
                }
            } else if (this.mReversedFlow) {
                edgeLimit = findRowMax(true, edgeLimitSearchIndex, null);
            } else {
                edgeLimit = findRowMin(false, edgeLimitSearchIndex, null);
            }
            if (this.mReversedFlow ? getRowMax(rowIndex) < edgeLimit : getRowMin(rowIndex) > edgeLimit) {
                rowIndex--;
                if (rowIndex < 0) {
                    rowIndex = this.mNumRows - 1;
                    if (this.mReversedFlow) {
                        i = findRowMax(true, null);
                    } else {
                        i = findRowMin(false, null);
                    }
                    edgeLimit = i;
                }
            }
            edgeLimitIsValid = true;
        }
        boolean edgeLimitIsValid2 = edgeLimitIsValid;
        edgeLimitSearchIndex = itemIndex;
        boolean filledOne = false;
        loop1:
        while (true) {
            if (rowIndex >= 0) {
                if (edgeLimitSearchIndex < 0 || (!oneColumnMode && checkPrependOverLimit(toLimit))) {
                    return filledOne;
                }
                int location = this.mReversedFlow ? getRowMax(rowIndex) : getRowMin(rowIndex);
                if (location != Integer.MAX_VALUE && location != Integer.MIN_VALUE) {
                    location += this.mReversedFlow ? this.mSpacing : -this.mSpacing;
                } else if (rowIndex == this.mNumRows - 1) {
                    location = this.mReversedFlow ? getRowMax(0) : getRowMin(0);
                    if (!(location == Integer.MAX_VALUE || location == Integer.MIN_VALUE)) {
                        location += this.mReversedFlow ? this.mSpacing : -this.mSpacing;
                    }
                } else {
                    location = this.mReversedFlow ? getRowMin(rowIndex + 1) : getRowMax(rowIndex + 1);
                }
                int itemIndex2 = edgeLimitSearchIndex - 1;
                edgeLimitSearchIndex = prependVisibleItemToRow(edgeLimitSearchIndex, rowIndex, location);
                filledOne = true;
                if (edgeLimitIsValid2) {
                    while (true) {
                        if (!this.mReversedFlow) {
                            if (location - edgeLimitSearchIndex <= edgeLimit) {
                                break;
                            }
                        } else if (location + edgeLimitSearchIndex >= edgeLimit) {
                            break;
                        }
                        if (itemIndex2 < 0 || (!oneColumnMode && checkPrependOverLimit(toLimit))) {
                            return true;
                        }
                        location += this.mReversedFlow ? this.mSpacing + edgeLimitSearchIndex : (-edgeLimitSearchIndex) - this.mSpacing;
                        int itemIndex3 = itemIndex2 - 1;
                        edgeLimitSearchIndex = prependVisibleItemToRow(itemIndex2, rowIndex, location);
                        itemIndex2 = itemIndex3;
                    }
                } else {
                    edgeLimitIsValid2 = true;
                    edgeLimit = this.mReversedFlow ? getRowMax(rowIndex) : getRowMin(rowIndex);
                }
                edgeLimitSearchIndex = itemIndex2;
                rowIndex--;
            } else if (oneColumnMode) {
                return filledOne;
            } else {
                edgeLimit = this.mReversedFlow ? findRowMax(true, null) : findRowMin(false, null);
                rowIndex = this.mNumRows - 1;
            }
        }
        return filledOne;
    }
}
