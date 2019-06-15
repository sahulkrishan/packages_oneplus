package com.android.setupwizardlib.template;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;

public class RecyclerViewScrollHandlingDelegate implements ScrollHandlingDelegate {
    private static final String TAG = "RVRequireScrollMixin";
    @Nullable
    private final RecyclerView mRecyclerView;
    @NonNull
    private final RequireScrollMixin mRequireScrollMixin;

    public RecyclerViewScrollHandlingDelegate(@NonNull RequireScrollMixin requireScrollMixin, @Nullable RecyclerView recyclerView) {
        this.mRequireScrollMixin = requireScrollMixin;
        this.mRecyclerView = recyclerView;
    }

    private boolean canScrollDown() {
        boolean z = false;
        if (this.mRecyclerView == null) {
            return false;
        }
        int offset = this.mRecyclerView.computeVerticalScrollOffset();
        int range = this.mRecyclerView.computeVerticalScrollRange() - this.mRecyclerView.computeVerticalScrollExtent();
        if (range != 0 && offset < range - 1) {
            z = true;
        }
        return z;
    }

    public void startListening() {
        if (this.mRecyclerView != null) {
            this.mRecyclerView.addOnScrollListener(new OnScrollListener() {
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    RecyclerViewScrollHandlingDelegate.this.mRequireScrollMixin.notifyScrollabilityChange(RecyclerViewScrollHandlingDelegate.this.canScrollDown());
                }
            });
            if (canScrollDown()) {
                this.mRequireScrollMixin.notifyScrollabilityChange(true);
                return;
            }
            return;
        }
        Log.w(TAG, "Cannot require scroll. Recycler view is null.");
    }

    public void pageScrollDown() {
        if (this.mRecyclerView != null) {
            this.mRecyclerView.smoothScrollBy(0, this.mRecyclerView.getHeight());
        }
    }
}
