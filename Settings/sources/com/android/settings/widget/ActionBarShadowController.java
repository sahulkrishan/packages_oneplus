package com.android.settings.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.support.annotation.VisibleForTesting;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.View;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class ActionBarShadowController implements LifecycleObserver, OnStart, OnStop {
    @VisibleForTesting
    static final float ELEVATION_HIGH = 8.0f;
    @VisibleForTesting
    static final float ELEVATION_LOW = 0.0f;
    private boolean isScrollWatcherAttached;
    private RecyclerView mRecyclerView;
    @VisibleForTesting
    ScrollChangeWatcher mScrollChangeWatcher;

    final class ScrollChangeWatcher extends OnScrollListener {
        private final Activity mActivity;
        private final View mAnchorView;

        public ScrollChangeWatcher(Activity activity) {
            this.mActivity = activity;
            this.mAnchorView = null;
        }

        public ScrollChangeWatcher(View anchorView) {
            this.mAnchorView = anchorView;
            this.mActivity = null;
        }

        public void onScrolled(RecyclerView view, int dx, int dy) {
            updateDropShadow(view);
        }

        public void updateDropShadow(View view) {
            boolean shouldShowShadow = view.canScrollVertically(true);
            float f = 0.0f;
            if (this.mAnchorView != null) {
                View view2 = this.mAnchorView;
                if (shouldShowShadow) {
                    f = ActionBarShadowController.ELEVATION_HIGH;
                }
                view2.setElevation(f);
            } else if (this.mActivity != null) {
                ActionBar actionBar = this.mActivity.getActionBar();
                if (actionBar != null) {
                    if (shouldShowShadow) {
                        f = ActionBarShadowController.ELEVATION_HIGH;
                    }
                    actionBar.setElevation(f);
                }
            }
        }
    }

    public static ActionBarShadowController attachToRecyclerView(Activity activity, Lifecycle lifecycle, RecyclerView recyclerView) {
        return new ActionBarShadowController(activity, lifecycle, recyclerView);
    }

    public static ActionBarShadowController attachToRecyclerView(View anchorView, Lifecycle lifecycle, RecyclerView recyclerView) {
        return new ActionBarShadowController(anchorView, lifecycle, recyclerView);
    }

    private ActionBarShadowController(Activity activity, Lifecycle lifecycle, RecyclerView recyclerView) {
        this.mScrollChangeWatcher = new ScrollChangeWatcher(activity);
        this.mRecyclerView = recyclerView;
        attachScrollWatcher();
        lifecycle.addObserver(this);
    }

    private ActionBarShadowController(View anchorView, Lifecycle lifecycle, RecyclerView recyclerView) {
        this.mScrollChangeWatcher = new ScrollChangeWatcher(anchorView);
        this.mRecyclerView = recyclerView;
        attachScrollWatcher();
        lifecycle.addObserver(this);
    }

    public void onStop() {
        detachScrollWatcher();
    }

    private void detachScrollWatcher() {
        this.mRecyclerView.removeOnScrollListener(this.mScrollChangeWatcher);
        this.isScrollWatcherAttached = false;
    }

    public void onStart() {
        attachScrollWatcher();
    }

    private void attachScrollWatcher() {
        if (!this.isScrollWatcherAttached) {
            this.isScrollWatcherAttached = true;
            this.mRecyclerView.addOnScrollListener(this.mScrollChangeWatcher);
            this.mScrollChangeWatcher.updateDropShadow(this.mRecyclerView);
        }
    }
}
