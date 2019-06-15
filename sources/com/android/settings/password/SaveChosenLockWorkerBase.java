package com.android.settings.password;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserManager;
import com.android.internal.widget.LockPatternUtils;

abstract class SaveChosenLockWorkerBase extends Fragment {
    private boolean mBlocking;
    protected long mChallenge;
    private boolean mFinished;
    protected boolean mHasChallenge;
    private Listener mListener;
    private Intent mResultData;
    protected int mUserId;
    protected LockPatternUtils mUtils;
    protected boolean mWasSecureBefore;

    interface Listener {
        void onChosenLockSaveFinished(boolean z, Intent intent);
    }

    private class Task extends AsyncTask<Void, Void, Intent> {
        private Task() {
        }

        /* Access modifiers changed, original: protected|varargs */
        public Intent doInBackground(Void... params) {
            return SaveChosenLockWorkerBase.this.saveAndVerifyInBackground();
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Intent resultData) {
            SaveChosenLockWorkerBase.this.finish(resultData);
        }
    }

    public abstract Intent saveAndVerifyInBackground();

    SaveChosenLockWorkerBase() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setListener(Listener listener) {
        if (this.mListener != listener) {
            this.mListener = listener;
            if (this.mFinished && this.mListener != null) {
                this.mListener.onChosenLockSaveFinished(this.mWasSecureBefore, this.mResultData);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void prepare(LockPatternUtils utils, boolean credentialRequired, boolean hasChallenge, long challenge, int userId) {
        this.mUtils = utils;
        this.mUserId = userId;
        this.mHasChallenge = hasChallenge;
        this.mChallenge = challenge;
        this.mWasSecureBefore = this.mUtils.isSecure(this.mUserId);
        Context context = getContext();
        if (context == null || UserManager.get(context).getUserInfo(this.mUserId).isPrimary()) {
            this.mUtils.setCredentialRequiredToDecrypt(credentialRequired);
        }
        this.mFinished = false;
        this.mResultData = null;
    }

    /* Access modifiers changed, original: protected */
    public void start() {
        if (this.mBlocking) {
            finish(saveAndVerifyInBackground());
        } else {
            new Task().execute(new Void[0]);
        }
    }

    /* Access modifiers changed, original: protected */
    public void finish(Intent resultData) {
        this.mFinished = true;
        this.mResultData = resultData;
        if (this.mListener != null) {
            this.mListener.onChosenLockSaveFinished(this.mWasSecureBefore, this.mResultData);
        }
    }

    public void setBlocking(boolean blocking) {
        this.mBlocking = blocking;
    }
}
