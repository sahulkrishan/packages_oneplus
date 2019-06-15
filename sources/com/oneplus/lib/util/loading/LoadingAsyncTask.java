package com.oneplus.lib.util.loading;

import android.os.AsyncTask;
import com.oneplus.lib.util.loading.LoadingHelper.FinishShowCallback;

public abstract class LoadingAsyncTask<Param, Progress, Result> extends AsyncTask<Param, Progress, Result> {
    private LoadingHelper mProgressHelper = new LoadingHelper() {
        /* Access modifiers changed, original: protected */
        public Object showProgree() {
            return LoadingAsyncTask.this.showProgree();
        }

        /* Access modifiers changed, original: protected */
        public void hideProgree(Object progreeView) {
            LoadingAsyncTask.this.hideProgree(progreeView);
        }
    };

    public abstract void hideProgree(Object obj);

    public abstract Object showProgree();

    public LoadingAsyncTask<Param, Progress, Result> setWillShowProgreeTime(long willShowProgreeTime) {
        this.mProgressHelper.setWillShowProgreeTime(willShowProgreeTime);
        return this;
    }

    public LoadingAsyncTask<Param, Progress, Result> setProgreeMinShowTime(long progreeMinShowTime) {
        this.mProgressHelper.setProgreeMinShowTime(progreeMinShowTime);
        return this;
    }

    /* Access modifiers changed, original: protected */
    public void onPreExecuteExtend() {
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecuteExtend(Result result) {
    }

    /* Access modifiers changed, original: protected */
    public void onCancelledExtend(Result result) {
    }

    /* Access modifiers changed, original: protected|final */
    public final void onPreExecute() {
        this.mProgressHelper.beginShowProgress();
        onPreExecuteExtend();
    }

    /* Access modifiers changed, original: protected|final */
    public final void onPostExecute(Result result) {
        onFinish(result);
    }

    /* Access modifiers changed, original: protected|final */
    public final void onCancelled(Result result) {
        onFinish(result);
        onCancelledExtend(result);
    }

    /* Access modifiers changed, original: protected|final */
    public final void onCancelled() {
        super.onCancelled();
    }

    private void onFinish(final Result result) {
        this.mProgressHelper.finishShowProgress(new FinishShowCallback() {
            public void finish(boolean shown) {
                if (!LoadingAsyncTask.this.isCancelled()) {
                    LoadingAsyncTask.this.onPostExecuteExtend(result);
                }
            }
        });
    }
}
