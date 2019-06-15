package com.oneplus.lib.util.loading;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

public abstract class LoadingHelper {
    private static final long PROMPT_MIN_SHOW_TIME_DEFAULT = 500;
    private static final long WILL_SHOW_PROMPT_TIME_DEFAULT = 300;
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private long mProgreeMinShowTime = PROMPT_MIN_SHOW_TIME_DEFAULT;
    private Object mProgreeView;
    private Runnable mShowProgreeRunnable;
    private long mShowProgreeTime;
    private long mWillShowProgreeTime = WILL_SHOW_PROMPT_TIME_DEFAULT;

    public interface FinishShowCallback {
        void finish(boolean z);
    }

    public abstract void hideProgree(Object obj);

    public abstract Object showProgree();

    public LoadingHelper setWillShowProgreeTime(long willShowProgreeTime) {
        this.mWillShowProgreeTime = willShowProgreeTime;
        return this;
    }

    public LoadingHelper setProgreeMinShowTime(long progreeMinShowTime) {
        this.mProgreeMinShowTime = progreeMinShowTime;
        return this;
    }

    public void beginShowProgress() {
        this.mShowProgreeRunnable = new Runnable() {
            public void run() {
                LoadingHelper.this.mShowProgreeRunnable = null;
                LoadingHelper.this.mProgreeView = LoadingHelper.this.showProgree();
                LoadingHelper.this.mShowProgreeTime = SystemClock.elapsedRealtime();
            }
        };
        mHandler.postDelayed(this.mShowProgreeRunnable, this.mWillShowProgreeTime);
    }

    public void finishShowProgress(final FinishShowCallback callback) {
        if (this.mShowProgreeRunnable != null) {
            mHandler.removeCallbacks(this.mShowProgreeRunnable);
            doFinish(callback, false);
            return;
        }
        long remainShowTime = this.mProgreeMinShowTime - (SystemClock.elapsedRealtime() - this.mShowProgreeTime);
        if (remainShowTime > 0) {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    LoadingHelper.this.doFinish(callback, true);
                }
            }, remainShowTime);
        } else {
            doFinish(callback, true);
        }
    }

    private void doFinish(final FinishShowCallback callback, final boolean shown) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (shown) {
                hideProgree(this.mProgreeView);
            }
            if (callback != null) {
                callback.finish(true);
                return;
            }
            return;
        }
        mHandler.post(new Runnable() {
            public void run() {
                if (shown) {
                    LoadingHelper.this.hideProgree(LoadingHelper.this.mProgreeView);
                }
                if (callback != null) {
                    callback.finish(true);
                }
            }
        });
    }
}
