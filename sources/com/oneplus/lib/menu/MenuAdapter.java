package com.oneplus.lib.menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.menu.MenuView.ItemView;
import java.util.ArrayList;

public class MenuAdapter extends BaseAdapter {
    static final int ITEM_LAYOUT = R.layout.op_abc_popup_menu_item_layout;
    static final int PADDING_BOTTOM = R.dimen.oneplus_contorl_margin_bottom1;
    static final int PADDING_TOP = R.dimen.oneplus_contorl_margin_top1;
    MenuBuilder mAdapterMenu;
    private int mExpandedIndex = -1;
    private boolean mForceShowIcon;
    private final LayoutInflater mInflater;
    private final boolean mOverflowOnly;
    private int mPaddingBottom;
    private int mPaddingTop;

    public MenuAdapter(MenuBuilder menu, LayoutInflater inflater, boolean overflowOnly) {
        this.mOverflowOnly = overflowOnly;
        this.mInflater = inflater;
        this.mAdapterMenu = menu;
        if (inflater.getContext() != null) {
            this.mPaddingBottom = inflater.getContext().getResources().getDimensionPixelSize(PADDING_BOTTOM);
            this.mPaddingTop = inflater.getContext().getResources().getDimensionPixelSize(PADDING_TOP);
        }
        findExpandedIndex();
    }

    public boolean getForceShowIcon() {
        return this.mForceShowIcon;
    }

    public void setForceShowIcon(boolean forceShow) {
        this.mForceShowIcon = forceShow;
    }

    public int getCount() {
        ArrayList<MenuItemImpl> items = this.mOverflowOnly ? this.mAdapterMenu.getNonActionItems() : this.mAdapterMenu.getVisibleItems();
        if (this.mExpandedIndex < 0) {
            return items.size();
        }
        return items.size() - 1;
    }

    public MenuBuilder getAdapterMenu() {
        return this.mAdapterMenu;
    }

    public MenuItemImpl getItem(int position) {
        ArrayList<MenuItemImpl> items = this.mOverflowOnly ? this.mAdapterMenu.getNonActionItems() : this.mAdapterMenu.getVisibleItems();
        if (this.mExpandedIndex >= 0 && position >= this.mExpandedIndex) {
            position++;
        }
        return (MenuItemImpl) items.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.mInflater.inflate(ITEM_LAYOUT, parent, false);
        }
        ItemView itemView = (ItemView) convertView;
        if (this.mForceShowIcon) {
            ((ListMenuItemView) convertView).setForceShowIcon(true);
        }
        itemView.initialize(getItem(position), 0);
        return convertView;
    }

    /* Access modifiers changed, original: 0000 */
    public void findExpandedIndex() {
        MenuItemImpl expandedItem = this.mAdapterMenu.getExpandedItem();
        if (expandedItem != null) {
            ArrayList<MenuItemImpl> items = this.mAdapterMenu.getNonActionItems();
            int count = items.size();
            for (int i = 0; i < count; i++) {
                if (((MenuItemImpl) items.get(i)) == expandedItem) {
                    this.mExpandedIndex = i;
                    return;
                }
            }
        }
        this.mExpandedIndex = -1;
    }

    public void notifyDataSetChanged() {
        findExpandedIndex();
        super.notifyDataSetChanged();
    }
}
