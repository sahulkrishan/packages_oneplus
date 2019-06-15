package com.oneplus.lib.util.loading;

import android.view.View;

public class ViewLoadingHelper extends LoadingHelper {
    View mProgressView;

    public ViewLoadingHelper(View progressView) {
        this.mProgressView = progressView;
    }

    /* Access modifiers changed, original: protected */
    public Object showProgree() {
        if (this.mProgressView != null) {
            this.mProgressView.setVisibility(0);
        }
        return this.mProgressView;
    }

    /* Access modifiers changed, original: protected */
    public void hideProgree(Object progreeView) {
        if (this.mProgressView != null) {
            this.mProgressView.setVisibility(8);
        }
    }
}
