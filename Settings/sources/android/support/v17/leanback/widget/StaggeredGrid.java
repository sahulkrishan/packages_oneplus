package android.support.v17.leanback.widget;

import android.support.v4.util.CircularArray;
import android.support.v4.util.CircularIntArray;
import java.io.PrintWriter;

abstract class StaggeredGrid extends Grid {
    protected int mFirstIndex = -1;
    protected CircularArray<Location> mLocations = new CircularArray(64);
    protected Object mPendingItem;
    protected int mPendingItemSize;

    public static class Location extends android.support.v17.leanback.widget.Grid.Location {
        public int offset;
        public int size;

        public Location(int row, int offset, int size) {
            super(row);
            this.offset = offset;
            this.size = size;
        }
    }

    public abstract boolean appendVisibleItemsWithoutCache(int i, boolean z);

    public abstract boolean prependVisibleItemsWithoutCache(int i, boolean z);

    StaggeredGrid() {
    }

    public final int getFirstIndex() {
        return this.mFirstIndex;
    }

    public final int getLastIndex() {
        return (this.mFirstIndex + this.mLocations.size()) - 1;
    }

    public final int getSize() {
        return this.mLocations.size();
    }

    public final Location getLocation(int index) {
        int indexInArray = index - this.mFirstIndex;
        if (indexInArray < 0 || indexInArray >= this.mLocations.size()) {
            return null;
        }
        return (Location) this.mLocations.get(indexInArray);
    }

    public final void debugPrint(PrintWriter pw) {
        int size = this.mLocations.size();
        for (int i = 0; i < size; i++) {
            Location loc = (Location) this.mLocations.get(i);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<");
            stringBuilder.append(this.mFirstIndex + i);
            stringBuilder.append(",");
            stringBuilder.append(loc.row);
            stringBuilder.append(">");
            pw.print(stringBuilder.toString());
            pw.print(" ");
            pw.println();
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean prependVisibleItems(int toLimit, boolean oneColumnMode) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!oneColumnMode && checkPrependOverLimit(toLimit)) {
            return false;
        }
        try {
            boolean z;
            if (prependVisbleItemsWithCache(toLimit, oneColumnMode)) {
                z = true;
                this.mTmpItem[0] = null;
            } else {
                z = prependVisibleItemsWithoutCache(toLimit, oneColumnMode);
                this.mTmpItem[0] = null;
            }
            this.mPendingItem = null;
            return z;
        } catch (Throwable th) {
            this.mTmpItem[0] = null;
            this.mPendingItem = null;
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean prependVisbleItemsWithCache(int toLimit, boolean oneColumnMode) {
        if (this.mLocations.size() == 0) {
            return false;
        }
        int edge;
        int offset;
        int itemIndex;
        if (this.mFirstVisibleIndex >= 0) {
            edge = this.mProvider.getEdge(this.mFirstVisibleIndex);
            offset = getLocation(this.mFirstVisibleIndex).offset;
            itemIndex = this.mFirstVisibleIndex - 1;
        } else {
            edge = Integer.MAX_VALUE;
            offset = 0;
            itemIndex = this.mStartIndex != -1 ? this.mStartIndex : 0;
            if (itemIndex > getLastIndex() || itemIndex < getFirstIndex() - 1) {
                this.mLocations.clear();
                return false;
            } else if (itemIndex < getFirstIndex()) {
                return false;
            }
        }
        int firstIndex = Math.max(this.mProvider.getMinIndex(), this.mFirstIndex);
        for (itemIndex = 
/*
Method generation error in method: android.support.v17.leanback.widget.StaggeredGrid.prependVisbleItemsWithCache(int, boolean):boolean, dex: classes5.dex
jadx.core.utils.exceptions.CodegenException: Error generate insn: PHI: (r5_6 'itemIndex' int) = (r5_1 'itemIndex' int), (r5_5 'itemIndex' int) binds: {(r5_1 'itemIndex' int)=B:5:0x0011, (r5_5 'itemIndex' int)=B:15:0x0045} in method: android.support.v17.leanback.widget.StaggeredGrid.prependVisbleItemsWithCache(int, boolean):boolean, dex: classes5.dex
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:228)
	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:185)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:63)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:89)
	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:183)
	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:321)
	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:259)
	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:221)
	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
	at jadx.core.codegen.CodeGen.visit(CodeGen.java:10)
	at jadx.core.ProcessClass.process(ProcessClass.java:38)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
Caused by: jadx.core.utils.exceptions.CodegenException: PHI can be used only in fallback mode
	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:539)
	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:511)
	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:222)
	... 19 more

*/

    private int calculateOffsetAfterLastItem(int row) {
        int offset;
        int cachedIndex = getLastIndex();
        boolean foundCachedItemInSameRow = false;
        while (cachedIndex >= this.mFirstIndex) {
            if (getLocation(cachedIndex).row == row) {
                foundCachedItemInSameRow = true;
                break;
            }
            cachedIndex--;
        }
        if (!foundCachedItemInSameRow) {
            cachedIndex = getLastIndex();
        }
        if (isReversedFlow()) {
            offset = (-getLocation(cachedIndex).size) - this.mSpacing;
        } else {
            offset = getLocation(cachedIndex).size + this.mSpacing;
        }
        for (int i = cachedIndex + 1; i <= getLastIndex(); i++) {
            offset -= getLocation(i).offset;
        }
        return offset;
    }

    /* Access modifiers changed, original: protected|final */
    public final int prependVisibleItemToRow(int itemIndex, int rowIndex, int edge) {
        if (this.mFirstVisibleIndex < 0 || (this.mFirstVisibleIndex == getFirstIndex() && this.mFirstVisibleIndex == itemIndex + 1)) {
            Object item;
            Location oldFirstLoc = this.mFirstIndex >= 0 ? getLocation(this.mFirstIndex) : null;
            int oldFirstEdge = this.mProvider.getEdge(this.mFirstIndex);
            Location loc = new Location(rowIndex, 0, 0);
            this.mLocations.addFirst(loc);
            if (this.mPendingItem != null) {
                loc.size = this.mPendingItemSize;
                item = this.mPendingItem;
                this.mPendingItem = null;
            } else {
                loc.size = this.mProvider.createItem(itemIndex, false, this.mTmpItem, false);
                item = this.mTmpItem[0];
            }
            Object item2 = item;
            this.mFirstVisibleIndex = itemIndex;
            this.mFirstIndex = itemIndex;
            if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = itemIndex;
            }
            int thisEdge = !this.mReversedFlow ? edge - loc.size : loc.size + edge;
            if (oldFirstLoc != null) {
                oldFirstLoc.offset = oldFirstEdge - thisEdge;
            }
            this.mProvider.addItem(item2, itemIndex, loc.size, rowIndex, thisEdge);
            return loc.size;
        }
        throw new IllegalStateException();
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean appendVisibleItems(int toLimit, boolean oneColumnMode) {
        if (this.mProvider.getCount() == 0) {
            return false;
        }
        if (!oneColumnMode && checkAppendOverLimit(toLimit)) {
            return false;
        }
        try {
            boolean z;
            if (appendVisbleItemsWithCache(toLimit, oneColumnMode)) {
                z = true;
                this.mTmpItem[0] = null;
            } else {
                z = appendVisibleItemsWithoutCache(toLimit, oneColumnMode);
                this.mTmpItem[0] = null;
            }
            this.mPendingItem = null;
            return z;
        } catch (Throwable th) {
            this.mTmpItem[0] = null;
            this.mPendingItem = null;
        }
    }

    /* Access modifiers changed, original: protected|final */
    public final boolean appendVisbleItemsWithCache(int toLimit, boolean oneColumnMode) {
        boolean z = false;
        if (this.mLocations.size() == 0) {
            return false;
        }
        int itemIndex;
        int edge;
        int count = this.mProvider.getCount();
        if (this.mLastVisibleIndex >= 0) {
            itemIndex = this.mLastVisibleIndex + 1;
            edge = this.mProvider.getEdge(this.mLastVisibleIndex);
        } else {
            edge = Integer.MAX_VALUE;
            itemIndex = this.mStartIndex != -1 ? this.mStartIndex : 0;
            if (itemIndex > getLastIndex() + 1 || itemIndex < getFirstIndex()) {
                this.mLocations.clear();
                return false;
            } else if (itemIndex > getLastIndex()) {
                return false;
            }
        }
        int lastIndex = getLastIndex();
        while (itemIndex < count && itemIndex <= lastIndex) {
            Location loc = getLocation(itemIndex);
            if (edge != Integer.MAX_VALUE) {
                edge += loc.offset;
            }
            int rowIndex = loc.row;
            int size = this.mProvider.createItem(itemIndex, true, this.mTmpItem, z);
            if (size != loc.size) {
                loc.size = size;
                this.mLocations.removeFromEnd(lastIndex - itemIndex);
                lastIndex = itemIndex;
            }
            this.mLastVisibleIndex = itemIndex;
            if (this.mFirstVisibleIndex < 0) {
                this.mFirstVisibleIndex = itemIndex;
            }
            int rowIndex2 = rowIndex;
            this.mProvider.addItem(this.mTmpItem[z], itemIndex, size, rowIndex, edge);
            if (!oneColumnMode && checkAppendOverLimit(toLimit)) {
                return true;
            }
            if (edge == Integer.MAX_VALUE) {
                edge = this.mProvider.getEdge(itemIndex);
            }
            if (rowIndex2 == this.mNumRows - 1 && oneColumnMode) {
                return true;
            }
            itemIndex++;
            z = false;
        }
        return false;
    }

    /* Access modifiers changed, original: protected|final */
    public final int appendVisibleItemToRow(int itemIndex, int rowIndex, int location) {
        if (this.mLastVisibleIndex < 0 || (this.mLastVisibleIndex == getLastIndex() && this.mLastVisibleIndex == itemIndex - 1)) {
            int offset;
            Object item;
            if (this.mLastVisibleIndex >= 0) {
                offset = location - this.mProvider.getEdge(this.mLastVisibleIndex);
            } else if (this.mLocations.size() <= 0 || itemIndex != getLastIndex() + 1) {
                offset = 0;
            } else {
                offset = calculateOffsetAfterLastItem(rowIndex);
            }
            Location loc = new Location(rowIndex, offset, 0);
            this.mLocations.addLast(loc);
            if (this.mPendingItem != null) {
                loc.size = this.mPendingItemSize;
                item = this.mPendingItem;
                this.mPendingItem = null;
            } else {
                loc.size = this.mProvider.createItem(itemIndex, true, this.mTmpItem, false);
                item = this.mTmpItem[0];
            }
            Object item2 = item;
            if (this.mLocations.size() == 1) {
                this.mLastVisibleIndex = itemIndex;
                this.mFirstVisibleIndex = itemIndex;
                this.mFirstIndex = itemIndex;
            } else if (this.mLastVisibleIndex < 0) {
                this.mLastVisibleIndex = itemIndex;
                this.mFirstVisibleIndex = itemIndex;
            } else {
                this.mLastVisibleIndex++;
            }
            this.mProvider.addItem(item2, itemIndex, loc.size, rowIndex, location);
            return loc.size;
        }
        throw new IllegalStateException();
    }

    public final CircularIntArray[] getItemPositionsInRows(int startPos, int endPos) {
        int i;
        for (i = 0; i < this.mNumRows; i++) {
            this.mTmpItemPositionsInRows[i].clear();
        }
        if (startPos >= 0) {
            i = startPos;
            while (i <= endPos) {
                CircularIntArray row = this.mTmpItemPositionsInRows[getLocation(i).row];
                if (row.size() <= 0 || row.getLast() != i - 1) {
                    row.addLast(i);
                    row.addLast(i);
                } else {
                    row.popLast();
                    row.addLast(i);
                }
                i++;
            }
        }
        return this.mTmpItemPositionsInRows;
    }

    public void invalidateItemsAfter(int index) {
        super.invalidateItemsAfter(index);
        this.mLocations.removeFromEnd((getLastIndex() - index) + 1);
        if (this.mLocations.size() == 0) {
            this.mFirstIndex = -1;
        }
    }
}
