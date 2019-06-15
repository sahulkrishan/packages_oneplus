package com.android.setupwizardlib.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.android.setupwizardlib.span.LinkSpan;
import com.android.setupwizardlib.span.LinkSpan.OnLinkClickListener;
import com.android.setupwizardlib.span.SpanHelper;
import com.android.setupwizardlib.util.LinkAccessibilityHelper;
import com.android.setupwizardlib.view.TouchableMovementMethod.TouchableLinkMovementMethod;

public class RichTextView extends AppCompatTextView implements OnLinkClickListener {
    private static final String ANNOTATION_LINK = "link";
    private static final String ANNOTATION_TEXT_APPEARANCE = "textAppearance";
    private static final String TAG = "RichTextView";
    private LinkAccessibilityHelper mAccessibilityHelper;
    private OnLinkClickListener mOnLinkClickListener;

    public static CharSequence getRichText(Context context, CharSequence text) {
        if (!(text instanceof Spanned)) {
            return text;
        }
        SpannableString spannable = new SpannableString(text);
        int i = 0;
        Annotation[] spans = (Annotation[]) spannable.getSpans(0, spannable.length(), Annotation.class);
        int length = spans.length;
        while (i < length) {
            Annotation span = spans[i];
            String key = span.getKey();
            if (ANNOTATION_TEXT_APPEARANCE.equals(key)) {
                int style = context.getResources().getIdentifier(span.getValue(), "style", context.getPackageName());
                if (style == 0) {
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Cannot find resource: ");
                    stringBuilder.append(style);
                    Log.w(str, stringBuilder.toString());
                }
                SpanHelper.replaceSpan(spannable, span, new TextAppearanceSpan(context, style));
            } else if ("link".equals(key)) {
                SpanHelper.replaceSpan(spannable, span, new LinkSpan(span.getValue()));
            }
            i++;
        }
        return spannable;
    }

    public RichTextView(Context context) {
        super(context);
        init();
    }

    public RichTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.mAccessibilityHelper = new LinkAccessibilityHelper((TextView) this);
        ViewCompat.setAccessibilityDelegate(this, this.mAccessibilityHelper);
    }

    public void setText(CharSequence text, BufferType type) {
        text = getRichText(getContext(), text);
        super.setText(text, type);
        boolean hasLinks = hasLinks(text);
        if (hasLinks) {
            setMovementMethod(TouchableLinkMovementMethod.getInstance());
        } else {
            setMovementMethod(null);
        }
        setFocusable(hasLinks);
        if (VERSION.SDK_INT >= 25) {
            setRevealOnFocusHint(false);
            setFocusableInTouchMode(hasLinks);
        }
    }

    private boolean hasLinks(CharSequence text) {
        boolean z = false;
        if (!(text instanceof Spanned)) {
            return false;
        }
        if (((ClickableSpan[]) ((Spanned) text).getSpans(0, text.length(), ClickableSpan.class)).length > 0) {
            z = true;
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean superResult = super.onTouchEvent(event);
        MovementMethod movementMethod = getMovementMethod();
        if (movementMethod instanceof TouchableMovementMethod) {
            TouchableMovementMethod touchableMovementMethod = (TouchableMovementMethod) movementMethod;
            if (touchableMovementMethod.getLastTouchEvent() == event) {
                return touchableMovementMethod.isLastTouchEventHandled();
            }
        }
        return superResult;
    }

    /* Access modifiers changed, original: protected */
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mAccessibilityHelper == null || !this.mAccessibilityHelper.dispatchHoverEvent(event)) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }

    /* Access modifiers changed, original: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        if (VERSION.SDK_INT >= 17) {
            int[] state = getDrawableState();
            for (Drawable drawable : getCompoundDrawablesRelative()) {
                if (drawable != null && drawable.setState(state)) {
                    invalidateDrawable(drawable);
                }
            }
        }
    }

    public void setOnLinkClickListener(OnLinkClickListener listener) {
        this.mOnLinkClickListener = listener;
    }

    public OnLinkClickListener getOnLinkClickListener() {
        return this.mOnLinkClickListener;
    }

    public boolean onLinkClick(LinkSpan span) {
        if (this.mOnLinkClickListener != null) {
            return this.mOnLinkClickListener.onLinkClick(span);
        }
        return false;
    }
}
