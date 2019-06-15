package com.android.settings;

import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;

public class LinkifyUtils {
    private static final String PLACE_HOLDER_LINK_BEGIN = "LINK_BEGIN";
    private static final String PLACE_HOLDER_LINK_END = "LINK_END";

    public interface OnClickListener {
        void onClick();
    }

    private LinkifyUtils() {
    }

    public static boolean linkify(TextView textView, StringBuilder text, final OnClickListener listener) {
        int beginIndex = text.indexOf(PLACE_HOLDER_LINK_BEGIN);
        if (beginIndex == -1) {
            textView.setText(text);
            return false;
        }
        text.delete(beginIndex, PLACE_HOLDER_LINK_BEGIN.length() + beginIndex);
        int endIndex = text.indexOf(PLACE_HOLDER_LINK_END);
        if (endIndex == -1) {
            textView.setText(text);
            return false;
        }
        text.delete(endIndex, PLACE_HOLDER_LINK_END.length() + endIndex);
        textView.setText(text.toString(), BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        ((Spannable) textView.getText()).setSpan(new ClickableSpan() {
            public void onClick(View widget) {
                listener.onClick();
            }

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, beginIndex, endIndex, 33);
        return true;
    }
}
