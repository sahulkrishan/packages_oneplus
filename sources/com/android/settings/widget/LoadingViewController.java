package com.android.settings.widget;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class LoadingViewController {
    private static final long DELAY_SHOW_LOADING_CONTAINER_THRESHOLD_MS = 100;
    public final View mContentView;
    public final Handler mFgHandler;
    public final View mLoadingView;
    private Runnable mShowLoadingContainerRunnable = new Runnable() {
        public void run() {
            LoadingViewController.this.handleLoadingContainer(false, false);
        }
    };

    public LoadingViewController(View loadingView, View contentView) {
        this.mLoadingView = loadingView;
        this.mContentView = contentView;
        this.mFgHandler = new Handler(Looper.getMainLooper());
    }

    public void showContent(boolean animate) {
        this.mFgHandler.removeCallbacks(this.mShowLoadingContainerRunnable);
        handleLoadingContainer(true, animate);
    }

    public void showLoadingViewDelayed() {
        this.mFgHandler.postDelayed(this.mShowLoadingContainerRunnable, DELAY_SHOW_LOADING_CONTAINER_THRESHOLD_MS);
    }

    public void handleLoadingContainer(boolean done, boolean animate) {
        handleLoadingContainer(this.mLoadingView, this.mContentView, done, animate);
    }

    public static void handleLoadingContainer(View loading, View content, boolean done, boolean animate) {
        setViewShown(loading, done ^ 1, animate);
        setViewShown(content, done, animate);
    }

    private static void setViewShown(final View view, boolean shown, boolean animate) {
        int i = 0;
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(view.getContext(), shown ? 17432576 : 17432577);
            if (shown) {
                view.setVisibility(0);
            } else {
                animation.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(4);
                    }
                });
            }
            view.startAnimation(animation);
            return;
        }
        view.clearAnimation();
        if (!shown) {
            i = 4;
        }
        view.setVisibility(i);
    }
}
