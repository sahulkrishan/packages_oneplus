package com.android.settings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ImeAwareEditText extends EditText {
    private boolean mHasPendingShowSoftInputRequest;
    final Runnable mRunShowSoftInputIfNecessary = new -$$Lambda$ImeAwareEditText$jSRw3KSZxc80AfkP8GTCtV5_bRY(this);

    public ImeAwareEditText(Context context) {
        super(context, null);
    }

    public ImeAwareEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImeAwareEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImeAwareEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        InputConnection ic = super.onCreateInputConnection(editorInfo);
        if (this.mHasPendingShowSoftInputRequest) {
            removeCallbacks(this.mRunShowSoftInputIfNecessary);
            post(this.mRunShowSoftInputIfNecessary);
        }
        return ic;
    }

    private void showSoftInputIfNecessary() {
        if (this.mHasPendingShowSoftInputRequest) {
            ((InputMethodManager) getContext().getSystemService(InputMethodManager.class)).showSoftInput(this, 0);
            this.mHasPendingShowSoftInputRequest = false;
        }
    }

    public void scheduleShowSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
        if (imm.isActive(this)) {
            this.mHasPendingShowSoftInputRequest = false;
            removeCallbacks(this.mRunShowSoftInputIfNecessary);
            imm.showSoftInput(this, 0);
            return;
        }
        this.mHasPendingShowSoftInputRequest = true;
    }
}
