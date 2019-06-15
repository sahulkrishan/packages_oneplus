package com.android.setupwizardlib.span;

import android.text.Spannable;

public class SpanHelper {
    public static void replaceSpan(Spannable spannable, Object oldSpan, Object newSpan) {
        int spanStart = spannable.getSpanStart(oldSpan);
        int spanEnd = spannable.getSpanEnd(oldSpan);
        spannable.removeSpan(oldSpan);
        spannable.setSpan(newSpan, spanStart, spanEnd, 0);
    }
}
