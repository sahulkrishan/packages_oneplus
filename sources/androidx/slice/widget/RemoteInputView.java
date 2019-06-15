package androidx.slice.widget;

import android.animation.Animator;
import android.app.PendingIntent.CanceledException;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import androidx.slice.SliceItem;
import androidx.slice.view.R;

@RequiresApi(21)
@RestrictTo({Scope.LIBRARY})
public class RemoteInputView extends LinearLayout implements OnClickListener, TextWatcher {
    private static final String TAG = "RemoteInput";
    public static final Object VIEW_TAG = new Object();
    private SliceItem mAction;
    private RemoteEditText mEditText;
    private ProgressBar mProgressBar;
    private RemoteInput mRemoteInput;
    private RemoteInput[] mRemoteInputs;
    private boolean mResetting;
    private int mRevealCx;
    private int mRevealCy;
    private int mRevealR;
    private ImageButton mSendButton;

    public static class RemoteEditText extends EditText {
        private final Drawable mBackground = getBackground();
        private RemoteInputView mRemoteInputView;
        boolean mShowImeOnInputConnection;

        public RemoteEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        private void defocusIfNeeded(boolean animate) {
            if (this.mRemoteInputView != null || isTemporarilyDetachedCompat()) {
                isTemporarilyDetachedCompat();
                return;
            }
            if (isFocusable() && isEnabled()) {
                setInnerFocusable(false);
                if (this.mRemoteInputView != null) {
                    this.mRemoteInputView.onDefocus();
                }
                this.mShowImeOnInputConnection = false;
            }
        }

        private boolean isTemporarilyDetachedCompat() {
            if (VERSION.SDK_INT >= 24) {
                return isTemporarilyDetached();
            }
            return false;
        }

        /* Access modifiers changed, original: protected */
        public void onVisibilityChanged(View changedView, int visibility) {
            super.onVisibilityChanged(changedView, visibility);
            if (!isShown()) {
                defocusIfNeeded(false);
            }
        }

        /* Access modifiers changed, original: protected */
        public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            if (!focused) {
                defocusIfNeeded(true);
            }
        }

        public void getFocusedRect(Rect r) {
            super.getFocusedRect(r);
            r.top = getScrollY();
            r.bottom = getScrollY() + (getBottom() - getTop());
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == 4) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (keyCode != 4) {
                return super.onKeyUp(keyCode, event);
            }
            defocusIfNeeded(true);
            return true;
        }

        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
            if (this.mShowImeOnInputConnection && inputConnection != null) {
                final InputMethodManager imm = (InputMethodManager) ContextCompat.getSystemService(getContext(), InputMethodManager.class);
                if (imm != null) {
                    post(new Runnable() {
                        public void run() {
                            imm.viewClicked(RemoteEditText.this);
                            imm.showSoftInput(RemoteEditText.this, 0);
                        }
                    });
                }
            }
            return inputConnection;
        }

        public void onCommitCompletion(CompletionInfo text) {
            clearComposingText();
            setText(text.getText());
            setSelection(getText().length());
        }

        /* Access modifiers changed, original: 0000 */
        public void setInnerFocusable(boolean focusable) {
            setFocusableInTouchMode(focusable);
            setFocusable(focusable);
            setCursorVisible(focusable);
            if (focusable) {
                requestFocus();
                setBackground(this.mBackground);
                return;
            }
            setBackground(null);
        }

        public void setCustomSelectionActionModeCallback(Callback actionModeCallback) {
            super.setCustomSelectionActionModeCallback(TextViewCompat.wrapCustomSelectionActionModeCallback(this, actionModeCallback));
        }
    }

    public RemoteInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* Access modifiers changed, original: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mProgressBar = (ProgressBar) findViewById(R.id.remote_input_progress);
        this.mSendButton = (ImageButton) findViewById(R.id.remote_input_send);
        this.mSendButton.setOnClickListener(this);
        this.mEditText = (RemoteEditText) getChildAt(0);
        this.mEditText.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean isSoftImeEvent = event == null && (actionId == 6 || actionId == 5 || actionId == 4);
                boolean isKeyboardEnterKey = event != null && RemoteInputView.isConfirmKey(event.getKeyCode()) && event.getAction() == 0;
                if (!isSoftImeEvent && !isKeyboardEnterKey) {
                    return false;
                }
                if (RemoteInputView.this.mEditText.length() > 0) {
                    RemoteInputView.this.sendRemoteInput();
                }
                return true;
            }
        });
        this.mEditText.addTextChangedListener(this);
        this.mEditText.setInnerFocusable(false);
        this.mEditText.mRemoteInputView = this;
    }

    private void sendRemoteInput() {
        Bundle results = new Bundle();
        results.putString(this.mRemoteInput.getResultKey(), this.mEditText.getText().toString());
        Intent fillInIntent = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(this.mRemoteInputs, fillInIntent, results);
        this.mEditText.setEnabled(false);
        this.mSendButton.setVisibility(4);
        this.mProgressBar.setVisibility(0);
        this.mEditText.mShowImeOnInputConnection = false;
        try {
            this.mAction.fireAction(getContext(), fillInIntent);
            reset();
        } catch (CanceledException e) {
            Log.i(TAG, "Unable to send remote input result", e);
            Toast.makeText(getContext(), "Failure sending pending intent for inline reply :(", 0).show();
            reset();
        }
    }

    public static RemoteInputView inflate(Context context, ViewGroup root) {
        RemoteInputView v = (RemoteInputView) LayoutInflater.from(context).inflate(R.layout.abc_slice_remote_input, root, false);
        v.setTag(VIEW_TAG);
        return v;
    }

    public void onClick(View v) {
        if (v == this.mSendButton) {
            sendRemoteInput();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    private void onDefocus() {
        setVisibility(4);
    }

    public void setAction(SliceItem action) {
        this.mAction = action;
    }

    public void setRemoteInput(RemoteInput[] remoteInputs, RemoteInput remoteInput) {
        this.mRemoteInputs = remoteInputs;
        this.mRemoteInput = remoteInput;
        this.mEditText.setHint(this.mRemoteInput.getLabel());
    }

    public void focusAnimated() {
        if (getVisibility() != 0) {
            Animator animator = ViewAnimationUtils.createCircularReveal(this, this.mRevealCx, this.mRevealCy, 0.0f, (float) this.mRevealR);
            animator.setDuration(200);
            animator.start();
        }
        focus();
    }

    private void focus() {
        setVisibility(0);
        this.mEditText.setInnerFocusable(true);
        this.mEditText.mShowImeOnInputConnection = true;
        this.mEditText.setSelection(this.mEditText.getText().length());
        this.mEditText.requestFocus();
        updateSendButton();
    }

    private void reset() {
        this.mResetting = true;
        this.mEditText.getText().clear();
        this.mEditText.setEnabled(true);
        this.mSendButton.setVisibility(0);
        this.mProgressBar.setVisibility(4);
        updateSendButton();
        onDefocus();
        this.mResetting = false;
    }

    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (this.mResetting && child == this.mEditText) {
            return false;
        }
        return super.onRequestSendAccessibilityEvent(child, event);
    }

    private void updateSendButton() {
        this.mSendButton.setEnabled(this.mEditText.getText().length() != 0);
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        updateSendButton();
    }

    @RestrictTo({Scope.LIBRARY})
    public void setRevealParameters(int cx, int cy, int r) {
        this.mRevealCx = cx;
        this.mRevealCy = cy;
        this.mRevealR = r;
    }

    public void dispatchStartTemporaryDetach() {
        super.dispatchStartTemporaryDetach();
        detachViewFromParent(this.mEditText);
    }

    public void dispatchFinishTemporaryDetach() {
        if (isAttachedToWindow()) {
            attachViewToParent(this.mEditText, 0, this.mEditText.getLayoutParams());
        } else {
            removeDetachedView(this.mEditText, false);
        }
        super.dispatchFinishTemporaryDetach();
    }

    @RestrictTo({Scope.LIBRARY})
    public static final boolean isConfirmKey(int keyCode) {
        if (keyCode == 23 || keyCode == 62 || keyCode == 66 || keyCode == 160) {
            return true;
        }
        return false;
    }
}
