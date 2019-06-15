package com.android.setupwizardlib.items;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.setupwizardlib.items.ItemHierarchy.Observer;
import com.android.setupwizardlib.items.ItemInflater.ItemParent;
import java.util.ArrayList;
import java.util.List;

public class ItemGroup extends AbstractItemHierarchy implements ItemParent, Observer {
    private static final String TAG = "ItemGroup";
    private List<ItemHierarchy> mChildren = new ArrayList();
    private int mCount = 0;
    private boolean mDirty = false;
    private SparseIntArray mHierarchyStart = new SparseIntArray();

    private static int binarySearch(SparseIntArray array, int value) {
        int lo = 0;
        int hi = array.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midVal = array.valueAt(mid);
            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal <= value) {
                return array.keyAt(mid);
            } else {
                hi = mid - 1;
            }
        }
        return array.keyAt(lo - 1);
    }

    private static <T> int identityIndexOf(List<T> list, T object) {
        int count = list.size();
        for (int i = 0; i < count; i++) {
            if (list.get(i) == object) {
                return i;
            }
        }
        return -1;
    }

    public ItemGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addChild(ItemHierarchy child) {
        this.mDirty = true;
        this.mChildren.add(child);
        child.registerObserver(this);
        int count = child.getCount();
        if (count > 0) {
            notifyItemRangeInserted(getChildPosition(child), count);
        }
    }

    public boolean removeChild(ItemHierarchy child) {
        int childIndex = identityIndexOf(this.mChildren, child);
        int childPosition = getChildPosition(childIndex);
        this.mDirty = true;
        if (childIndex == -1) {
            return false;
        }
        int childCount = child.getCount();
        this.mChildren.remove(childIndex);
        child.unregisterObserver(this);
        if (childCount > 0) {
            notifyItemRangeRemoved(childPosition, childCount);
        }
        return true;
    }

    public void clear() {
        if (this.mChildren.size() != 0) {
            int numRemoved = getCount();
            for (ItemHierarchy item : this.mChildren) {
                item.unregisterObserver(this);
            }
            this.mDirty = true;
            this.mChildren.clear();
            notifyItemRangeRemoved(0, numRemoved);
        }
    }

    public int getCount() {
        updateDataIfNeeded();
        return this.mCount;
    }

    public IItem getItemAt(int position) {
        int itemIndex = getItemIndex(position);
        return ((ItemHierarchy) this.mChildren.get(itemIndex)).getItemAt(position - this.mHierarchyStart.get(itemIndex));
    }

    public void onChanged(ItemHierarchy hierarchy) {
        this.mDirty = true;
        notifyChanged();
    }

    private int getChildPosition(ItemHierarchy child) {
        return getChildPosition(identityIndexOf(this.mChildren, child));
    }

    private int getChildPosition(int childIndex) {
        updateDataIfNeeded();
        if (childIndex == -1) {
            return -1;
        }
        int childCount = this.mChildren.size();
        int childPos = -1;
        int i = childIndex;
        while (childPos < 0 && i < childCount) {
            childPos = this.mHierarchyStart.get(i, -1);
            i++;
        }
        if (childPos < 0) {
            childPos = getCount();
        }
        return childPos;
    }

    public void onItemRangeChanged(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        int childPosition = getChildPosition(itemHierarchy);
        if (childPosition >= 0) {
            notifyItemRangeChanged(childPosition + positionStart, itemCount);
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected child change ");
        stringBuilder.append(itemHierarchy);
        Log.e(str, stringBuilder.toString());
    }

    public void onItemRangeInserted(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        this.mDirty = true;
        int childPosition = getChildPosition(itemHierarchy);
        if (childPosition >= 0) {
            notifyItemRangeInserted(childPosition + positionStart, itemCount);
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected child insert ");
        stringBuilder.append(itemHierarchy);
        Log.e(str, stringBuilder.toString());
    }

    public void onItemRangeMoved(ItemHierarchy itemHierarchy, int fromPosition, int toPosition, int itemCount) {
        this.mDirty = true;
        int childPosition = getChildPosition(itemHierarchy);
        if (childPosition >= 0) {
            notifyItemRangeMoved(childPosition + fromPosition, childPosition + toPosition, itemCount);
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected child move ");
        stringBuilder.append(itemHierarchy);
        Log.e(str, stringBuilder.toString());
    }

    public void onItemRangeRemoved(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        this.mDirty = true;
        int childPosition = getChildPosition(itemHierarchy);
        if (childPosition >= 0) {
            notifyItemRangeRemoved(childPosition + positionStart, itemCount);
            return;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected child remove ");
        stringBuilder.append(itemHierarchy);
        Log.e(str, stringBuilder.toString());
    }

    public ItemHierarchy findItemById(int id) {
        if (id == getId()) {
            return this;
        }
        for (ItemHierarchy child : this.mChildren) {
            ItemHierarchy childFindItem = child.findItemById(id);
            if (childFindItem != null) {
                return childFindItem;
            }
        }
        return null;
    }

    private void updateDataIfNeeded() {
        if (this.mDirty) {
            this.mCount = 0;
            this.mHierarchyStart.clear();
            for (int itemIndex = 0; itemIndex < this.mChildren.size(); itemIndex++) {
                ItemHierarchy item = (ItemHierarchy) this.mChildren.get(itemIndex);
                if (item.getCount() > 0) {
                    this.mHierarchyStart.put(itemIndex, this.mCount);
                }
                this.mCount += item.getCount();
            }
            this.mDirty = false;
        }
    }

    private int getItemIndex(int position) {
        updateDataIfNeeded();
        if (position < 0 || position >= this.mCount) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("size=");
            stringBuilder.append(this.mCount);
            stringBuilder.append("; index=");
            stringBuilder.append(position);
            throw new IndexOutOfBoundsException(stringBuilder.toString());
        }
        int result = binarySearch(this.mHierarchyStart, position);
        if (result >= 0) {
            return result;
        }
        throw new IllegalStateException("Cannot have item start index < 0");
    }
}
