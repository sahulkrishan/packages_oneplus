package com.android.setupwizardlib;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import com.android.setupwizardlib.items.ItemAdapter;

@Deprecated
public class SetupWizardItemsLayout extends SetupWizardListLayout {
    public SetupWizardItemsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SetupWizardItemsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Nullable
    public ItemAdapter getAdapter() {
        ListAdapter adapter = super.getAdapter();
        if (adapter instanceof ItemAdapter) {
            return (ItemAdapter) adapter;
        }
        return null;
    }
}
