package com.android.setupwizardlib.template;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public class ListViewScrollHandlingDelegate implements ScrollHandlingDelegate, OnScrollListener {
    private static final int SCROLL_DURATION = 500;
    private static final String TAG = "ListViewDelegate";
    @Nullable
    private final ListView mListView;
    @NonNull
    private final RequireScrollMixin mRequireScrollMixin;

    public ListViewScrollHandlingDelegate(@NonNull RequireScrollMixin requireScrollMixin, @Nullable ListView listView) {
        this.mRequireScrollMixin = requireScrollMixin;
        this.mListView = listView;
    }

    public void startListening() {
        if (this.mListView != null) {
            this.mListView.setOnScrollListener(this);
            if (this.mListView.getLastVisiblePosition() < this.mListView.getAdapter().getCount()) {
                this.mRequireScrollMixin.notifyScrollabilityChange(true);
                return;
            }
            return;
        }
        Log.w(TAG, "Cannot require scroll. List view is null");
    }

    public void pageScrollDown() {
        if (this.mListView != null) {
            this.mListView.smoothScrollBy(this.mListView.getHeight(), SCROLL_DURATION);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount >= totalItemCount) {
            this.mRequireScrollMixin.notifyScrollabilityChange(false);
        } else {
            this.mRequireScrollMixin.notifyScrollabilityChange(true);
        }
    }
}
