package com.android.setupwizardlib.items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.items.ItemInflater.ItemParent;
import java.util.ArrayList;
import java.util.Iterator;

public class ButtonBarItem extends AbstractItem implements ItemParent {
    private final ArrayList<ButtonItem> mButtons = new ArrayList();
    private boolean mVisible = true;

    public ButtonBarItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getCount() {
        return isVisible();
    }

    public boolean isEnabled() {
        return false;
    }

    public int getLayoutResource() {
        return R.layout.suw_items_button_bar;
    }

    public void setVisible(boolean visible) {
        this.mVisible = visible;
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public int getViewId() {
        return getId();
    }

    public void onBindView(View view) {
        ViewGroup layout = (LinearLayout) view;
        layout.removeAllViews();
        Iterator it = this.mButtons.iterator();
        while (it.hasNext()) {
            layout.addView(((ButtonItem) it.next()).createButton(layout));
        }
        view.setId(getViewId());
    }

    public void addChild(ItemHierarchy child) {
        if (child instanceof ButtonItem) {
            this.mButtons.add((ButtonItem) child);
            return;
        }
        throw new UnsupportedOperationException("Cannot add non-button item to Button Bar");
    }

    public ItemHierarchy findItemById(int id) {
        if (getId() == id) {
            return this;
        }
        Iterator it = this.mButtons.iterator();
        while (it.hasNext()) {
            ItemHierarchy item = ((ButtonItem) it.next()).findItemById(id);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
}
