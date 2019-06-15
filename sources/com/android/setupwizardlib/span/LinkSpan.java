package com.android.setupwizardlib.span;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Typeface;
import android.os.Build.VERSION;
import android.support.annotation.Nullable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LinkSpan extends ClickableSpan {
    private static final String TAG = "LinkSpan";
    private static final Typeface TYPEFACE_MEDIUM = Typeface.create("sans-serif-medium", 0);
    private final String mId;

    @Deprecated
    public interface OnClickListener {
        void onClick(LinkSpan linkSpan);
    }

    public interface OnLinkClickListener {
        boolean onLinkClick(LinkSpan linkSpan);
    }

    public LinkSpan(String id) {
        this.mId = id;
    }

    public void onClick(View view) {
        if (!dispatchClick(view)) {
            Log.w(TAG, "Dropping click event. No listener attached.");
        } else if (VERSION.SDK_INT >= 19) {
            view.cancelPendingInputEvents();
        }
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text instanceof Spannable) {
                Selection.setSelection((Spannable) text, 0);
            }
        }
    }

    private boolean dispatchClick(View view) {
        boolean handled = false;
        if (view instanceof OnLinkClickListener) {
            handled = ((OnLinkClickListener) view).onLinkClick(this);
        }
        if (handled) {
            return handled;
        }
        OnClickListener listener = getLegacyListenerFromContext(view.getContext());
        if (listener == null) {
            return handled;
        }
        listener.onClick(this);
        return true;
    }

    @Nullable
    @Deprecated
    private OnClickListener getLegacyListenerFromContext(@Nullable Context context) {
        while (!(context instanceof OnClickListener)) {
            if (!(context instanceof ContextWrapper)) {
                return null;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return (OnClickListener) context;
    }

    public void updateDrawState(TextPaint drawState) {
        super.updateDrawState(drawState);
        drawState.setUnderlineText(false);
        drawState.setTypeface(TYPEFACE_MEDIUM);
    }

    public String getId() {
        return this.mId;
    }
}
