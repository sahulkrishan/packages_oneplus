package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.GuidedActionAutofillSupport.OnAutofillListener;
import android.support.v17.leanback.widget.ImeKeyMonitor.ImeKeyListener;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.autofill.AutofillValue;
import android.widget.EditText;
import android.widget.TextView;

public class GuidedActionEditText extends EditText implements ImeKeyMonitor, GuidedActionAutofillSupport {
    private OnAutofillListener mAutofillListener;
    private ImeKeyListener mKeyListener;
    private final Drawable mNoPaddingDrawable;
    private final Drawable mSavedBackground;

    static final class NoPaddingDrawable extends Drawable {
        NoPaddingDrawable() {
        }

        public boolean getPadding(Rect padding) {
            padding.set(0, 0, 0, 0);
            return true;
        }

        public void draw(Canvas canvas) {
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public int getOpacity() {
            return -2;
        }
    }

    public GuidedActionEditText(Context ctx) {
        this(ctx, null);
    }

    public GuidedActionEditText(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, 16842862);
    }

    public GuidedActionEditText(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        this.mSavedBackground = getBackground();
        this.mNoPaddingDrawable = new NoPaddingDrawable();
        setBackground(this.mNoPaddingDrawable);
    }

    public void setImeKeyListener(ImeKeyListener listener) {
        this.mKeyListener = listener;
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        boolean result = false;
        if (this.mKeyListener != null) {
            result = this.mKeyListener.onKeyPreIme(this, keyCode, event);
        }
        if (result) {
            return result;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName((isFocused() ? EditText.class : TextView.class).getName());
    }

    /* Access modifiers changed, original: protected */
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            setBackground(this.mSavedBackground);
        } else {
            setBackground(this.mNoPaddingDrawable);
        }
        if (!focused) {
            setFocusable(false);
        }
    }

    public int getAutofillType() {
        return 1;
    }

    public void setOnAutofillListener(OnAutofillListener autofillListener) {
        this.mAutofillListener = autofillListener;
    }

    public void autofill(AutofillValue values) {
        super.autofill(values);
        if (this.mAutofillListener != null) {
            this.mAutofillListener.onAutofill(this);
        }
    }

    public void setCustomSelectionActionModeCallback(Callback actionModeCallback) {
        super.setCustomSelectionActionModeCallback(TextViewCompat.wrapCustomSelectionActionModeCallback(this, actionModeCallback));
    }
}
