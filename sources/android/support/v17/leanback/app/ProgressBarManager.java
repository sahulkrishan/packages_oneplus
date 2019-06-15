package android.support.v17.leanback.app;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

public final class ProgressBarManager {
    private static final long DEFAULT_PROGRESS_BAR_DELAY = 1000;
    boolean mEnableProgressBar = true;
    private Handler mHandler = new Handler();
    private long mInitialDelay = 1000;
    boolean mIsShowing;
    View mProgressBarView;
    boolean mUserProvidedProgressBar;
    ViewGroup rootView;
    private Runnable runnable = new Runnable() {
        /* JADX WARNING: Missing block: B:15:0x0059, code skipped:
            return;
     */
        public void run() {
            /*
            r5 = this;
            r0 = android.support.v17.leanback.app.ProgressBarManager.this;
            r0 = r0.mEnableProgressBar;
            if (r0 == 0) goto L_0x0059;
        L_0x0006:
            r0 = android.support.v17.leanback.app.ProgressBarManager.this;
            r0 = r0.mUserProvidedProgressBar;
            if (r0 != 0) goto L_0x0013;
        L_0x000c:
            r0 = android.support.v17.leanback.app.ProgressBarManager.this;
            r0 = r0.rootView;
            if (r0 != 0) goto L_0x0013;
        L_0x0012:
            goto L_0x0059;
        L_0x0013:
            r0 = android.support.v17.leanback.app.ProgressBarManager.this;
            r0 = r0.mIsShowing;
            if (r0 == 0) goto L_0x0058;
        L_0x0019:
            r0 = android.support.v17.leanback.app.ProgressBarManager.this;
            r0 = r0.mProgressBarView;
            if (r0 != 0) goto L_0x004a;
        L_0x001f:
            r0 = android.support.v17.leanback.app.ProgressBarManager.this;
            r1 = new android.widget.ProgressBar;
            r2 = android.support.v17.leanback.app.ProgressBarManager.this;
            r2 = r2.rootView;
            r2 = r2.getContext();
            r3 = 0;
            r4 = 16842874; // 0x101007a float:2.36939E-38 double:8.3214854E-317;
            r1.<init>(r2, r3, r4);
            r0.mProgressBarView = r1;
            r0 = new android.widget.FrameLayout$LayoutParams;
            r1 = -2;
            r0.<init>(r1, r1);
            r1 = 17;
            r0.gravity = r1;
            r1 = android.support.v17.leanback.app.ProgressBarManager.this;
            r1 = r1.rootView;
            r2 = android.support.v17.leanback.app.ProgressBarManager.this;
            r2 = r2.mProgressBarView;
            r1.addView(r2, r0);
            goto L_0x0058;
        L_0x004a:
            r0 = android.support.v17.leanback.app.ProgressBarManager.this;
            r0 = r0.mUserProvidedProgressBar;
            if (r0 == 0) goto L_0x0058;
        L_0x0050:
            r0 = android.support.v17.leanback.app.ProgressBarManager.this;
            r0 = r0.mProgressBarView;
            r1 = 0;
            r0.setVisibility(r1);
        L_0x0058:
            return;
        L_0x0059:
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v17.leanback.app.ProgressBarManager$AnonymousClass1.run():void");
        }
    };

    public void setRootView(ViewGroup rootView) {
        this.rootView = rootView;
    }

    public void show() {
        if (this.mEnableProgressBar) {
            this.mIsShowing = true;
            this.mHandler.postDelayed(this.runnable, this.mInitialDelay);
        }
    }

    public void hide() {
        this.mIsShowing = false;
        if (this.mUserProvidedProgressBar) {
            this.mProgressBarView.setVisibility(4);
        } else if (this.mProgressBarView != null) {
            this.rootView.removeView(this.mProgressBarView);
            this.mProgressBarView = null;
        }
        this.mHandler.removeCallbacks(this.runnable);
    }

    public void setProgressBarView(View progressBarView) {
        if (progressBarView.getParent() != null) {
            this.mProgressBarView = progressBarView;
            this.mProgressBarView.setVisibility(4);
            this.mUserProvidedProgressBar = true;
            return;
        }
        throw new IllegalArgumentException("Must have a parent");
    }

    public long getInitialDelay() {
        return this.mInitialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.mInitialDelay = initialDelay;
    }

    public void disableProgressBar() {
        this.mEnableProgressBar = false;
    }

    public void enableProgressBar() {
        this.mEnableProgressBar = true;
    }
}
