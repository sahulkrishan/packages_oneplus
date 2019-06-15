package com.android.setupwizardlib.template;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.View;
import com.android.setupwizardlib.DividerItemDecoration;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;
import com.android.setupwizardlib.items.ItemHierarchy;
import com.android.setupwizardlib.items.ItemInflater;
import com.android.setupwizardlib.items.RecyclerItemAdapter;
import com.android.setupwizardlib.util.DrawableLayoutDirectionHelper;
import com.android.setupwizardlib.view.HeaderRecyclerView;
import com.android.setupwizardlib.view.HeaderRecyclerView.HeaderAdapter;

public class RecyclerMixin implements Mixin {
    private Drawable mDefaultDivider;
    private Drawable mDivider;
    @NonNull
    private DividerItemDecoration mDividerDecoration = new DividerItemDecoration(this.mTemplateLayout.getContext());
    private int mDividerInsetEnd;
    private int mDividerInsetStart;
    @Nullable
    private View mHeader;
    @NonNull
    private final RecyclerView mRecyclerView;
    private TemplateLayout mTemplateLayout;

    public RecyclerMixin(@NonNull TemplateLayout layout, @NonNull RecyclerView recyclerView) {
        this.mTemplateLayout = layout;
        this.mRecyclerView = recyclerView;
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this.mTemplateLayout.getContext()));
        if (recyclerView instanceof HeaderRecyclerView) {
            this.mHeader = ((HeaderRecyclerView) recyclerView).getHeader();
        }
        this.mRecyclerView.addItemDecoration(this.mDividerDecoration);
    }

    public void parseAttributes(@Nullable AttributeSet attrs, int defStyleAttr) {
        Context context = this.mTemplateLayout.getContext();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SuwRecyclerMixin, defStyleAttr, 0);
        int entries = a.getResourceId(R.styleable.SuwRecyclerMixin_android_entries, 0);
        if (entries != 0) {
            RecyclerItemAdapter adapter = new RecyclerItemAdapter((ItemHierarchy) new ItemInflater(context).inflate(entries));
            adapter.setHasStableIds(a.getBoolean(R.styleable.SuwRecyclerMixin_suwHasStableIds, false));
            setAdapter(adapter);
        }
        int dividerInset = a.getDimensionPixelSize(R.styleable.SuwRecyclerMixin_suwDividerInset, -1);
        if (dividerInset != -1) {
            setDividerInset(dividerInset);
        } else {
            setDividerInsets(a.getDimensionPixelSize(R.styleable.SuwRecyclerMixin_suwDividerInsetStart, 0), a.getDimensionPixelSize(R.styleable.SuwRecyclerMixin_suwDividerInsetEnd, 0));
        }
        a.recycle();
    }

    public RecyclerView getRecyclerView() {
        return this.mRecyclerView;
    }

    public View getHeader() {
        return this.mHeader;
    }

    public void onLayout() {
        if (this.mDivider == null) {
            updateDivider();
        }
    }

    public Adapter<? extends ViewHolder> getAdapter() {
        Adapter<? extends ViewHolder> adapter = this.mRecyclerView.getAdapter();
        if (adapter instanceof HeaderAdapter) {
            return ((HeaderAdapter) adapter).getWrappedAdapter();
        }
        return adapter;
    }

    public void setAdapter(Adapter<? extends ViewHolder> adapter) {
        this.mRecyclerView.setAdapter(adapter);
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
        boolean shouldUpdate = true;
        if (VERSION.SDK_INT >= 19) {
            shouldUpdate = this.mTemplateLayout.isLayoutDirectionResolved();
        }
        if (shouldUpdate) {
            if (this.mDefaultDivider == null) {
                this.mDefaultDivider = this.mDividerDecoration.getDivider();
            }
            this.mDivider = DrawableLayoutDirectionHelper.createRelativeInsetDrawable(this.mDefaultDivider, this.mDividerInsetStart, 0, this.mDividerInsetEnd, 0, this.mTemplateLayout);
            this.mDividerDecoration.setDivider(this.mDivider);
        }
    }

    public Drawable getDivider() {
        return this.mDivider;
    }

    public void setDividerItemDecoration(@NonNull DividerItemDecoration decoration) {
        this.mRecyclerView.removeItemDecoration(this.mDividerDecoration);
        this.mDividerDecoration = decoration;
        this.mRecyclerView.addItemDecoration(this.mDividerDecoration);
        updateDivider();
    }
}
