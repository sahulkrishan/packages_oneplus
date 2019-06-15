package com.android.setupwizardlib.items;

import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.setupwizardlib.items.ItemHierarchy.Observer;

public class ItemAdapter extends BaseAdapter implements Observer {
    private final ItemHierarchy mItemHierarchy;
    private ViewTypes mViewTypes = new ViewTypes();

    private static class ViewTypes {
        private SparseIntArray mPositionMap;
        private int nextPosition;

        private ViewTypes() {
            this.mPositionMap = new SparseIntArray();
            this.nextPosition = 0;
        }

        public int add(int id) {
            if (this.mPositionMap.indexOfKey(id) < 0) {
                this.mPositionMap.put(id, this.nextPosition);
                this.nextPosition++;
            }
            return this.mPositionMap.get(id);
        }

        public int size() {
            return this.mPositionMap.size();
        }

        public int get(int id) {
            return this.mPositionMap.get(id);
        }
    }

    public ItemAdapter(ItemHierarchy hierarchy) {
        this.mItemHierarchy = hierarchy;
        this.mItemHierarchy.registerObserver(this);
        refreshViewTypes();
    }

    public int getCount() {
        return this.mItemHierarchy.getCount();
    }

    public IItem getItem(int position) {
        return this.mItemHierarchy.getItemAt(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemViewType(int position) {
        return this.mViewTypes.get(getItem(position).getLayoutResource());
    }

    public int getViewTypeCount() {
        return this.mViewTypes.size();
    }

    private void refreshViewTypes() {
        for (int i = 0; i < getCount(); i++) {
            this.mViewTypes.add(getItem(i).getLayoutResource());
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        IItem item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(item.getLayoutResource(), parent, false);
        }
        item.onBindView(convertView);
        return convertView;
    }

    public void onChanged(ItemHierarchy hierarchy) {
        refreshViewTypes();
        notifyDataSetChanged();
    }

    public void onItemRangeChanged(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        onChanged(itemHierarchy);
    }

    public void onItemRangeInserted(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        onChanged(itemHierarchy);
    }

    public void onItemRangeMoved(ItemHierarchy itemHierarchy, int fromPosition, int toPosition, int itemCount) {
        onChanged(itemHierarchy);
    }

    public void onItemRangeRemoved(ItemHierarchy itemHierarchy, int positionStart, int itemCount) {
        onChanged(itemHierarchy);
    }

    public boolean isEnabled(int position) {
        return getItem(position).isEnabled();
    }

    public ItemHierarchy findItemById(int id) {
        return this.mItemHierarchy.findItemById(id);
    }

    public ItemHierarchy getRootItemHierarchy() {
        return this.mItemHierarchy;
    }
}
