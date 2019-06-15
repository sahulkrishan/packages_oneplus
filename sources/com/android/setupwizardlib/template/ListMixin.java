package com.android.setupwizardlib.template;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;
import com.android.setupwizardlib.items.ItemAdapter;
import com.android.setupwizardlib.items.ItemGroup;
import com.android.setupwizardlib.items.ItemInflater;
import com.android.setupwizardlib.util.DrawableLayoutDirectionHelper;

public class ListMixin implements Mixin {
    private Drawable mDefaultDivider;
    private Drawable mDivider;
    private int mDividerInsetEnd;
    private int mDividerInsetStart;
    @Nullable
    private ListView mListView;
    private TemplateLayout mTemplateLayout;

    public ListMixin(@NonNull TemplateLayout layout, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this.mTemplateLayout = layout;
        Context context = layout.getContext();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwListMixin, defStyleAttr, 0);
        int entries = a.getResourceId(R.styleable.SuwListMixin_android_entries, 0);
        if (entries != 0) {
            setAdapter(new ItemAdapter((ItemGroup) new ItemInflater(context).inflate(entries)));
        }
        int dividerInset = a.getDimensionPixelSize(R.styleable.SuwListMixin_suwDividerInset, -1);
        if (dividerInset != -1) {
            setDividerInset(dividerInset);
        } else {
            setDividerInsets(a.getDimensionPixelSize(R.styleable.SuwListMixin_suwDividerInsetStart, 0), a.getDimensionPixelSize(R.styleable.SuwListMixin_suwDividerInsetEnd, 0));
        }
        a.recycle();
    }

    public ListView getListView() {
        return getListViewInternal();
    }

    @Nullable
    private ListView getListViewInternal() {
        if (this.mListView == null) {
            View list = this.mTemplateLayout.findManagedViewById(16908298);
            if (list instanceof ListView) {
                this.mListView = (ListView) list;
            }
        }
        return this.mListView;
    }

    public void onLayout() {
        if (this.mDivider == null) {
            updateDivider();
        }
    }

    public ListAdapter getAdapter() {
        ListView listView = getListViewInternal();
        if (listView == null) {
            return null;
        }
        ListAdapter adapter = listView.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        return adapter;
    }

    public void setAdapter(ListAdapter adapter) {
        ListView listView = getListViewInternal();
        if (listView != null) {
            listView.setAdapter(adapter);
        }
    }

    @Deprecated
    public void setDividerInset(int inset) {
        setDividerInsets(inset, 0);
    }

    public void setDividerInsets(int start, int end) {
        this.mDividerInsetStart = start;
        this.mDividerInsetEnd = end;
        updateDivider();
    }

    @Deprecated
    public int getDividerInset() {
        return getDividerInsetStart();
    }

    public int getDividerInsetStart() {
        return this.mDividerInsetStart;
    }

    public int getDividerInsetEnd() {
        return this.mDividerInsetEnd;
    }

    private void updateDivider() {
        ListView listView = getListViewInternal();
        if (listView != null) {
            boolean shouldUpdate = true;
            if (VERSION.SDK_INT >= 19) {
                shouldUpdate = this.mTemplateLayout.isLayoutDirectionResolved();
            }
            if (shouldUpdate) {
                if (this.mDefaultDivider == null) {
                    this.mDefaultDivider = listView.getDivider();
                }
                this.mDivider = DrawableLayoutDirectionHelper.createRelativeInsetDrawable(this.mDefaultDivider, this.mDividerInsetStart, 0, this.mDividerInsetEnd, 0, this.mTemplateLayout);
                listView.setDivider(this.mDivider);
            }
        }
    }

    public Drawable getDivider() {
        return this.mDivider;
    }
}
