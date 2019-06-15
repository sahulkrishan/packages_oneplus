package com.android.settings.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class AnnotationSpan extends URLSpan {
    private final OnClickListener mClickListener;

    public static class LinkInfo {
        public static final String DEFAULT_ANNOTATION = "link";
        private static final String TAG = "AnnotationSpan.LinkInfo";
        private final Boolean mActionable;
        private final String mAnnotation;
        private final OnClickListener mListener;

        public LinkInfo(String annotation, OnClickListener listener) {
            this.mAnnotation = annotation;
            this.mListener = listener;
            this.mActionable = Boolean.valueOf(true);
        }

        public LinkInfo(Context context, String annotation, Intent intent) {
            this.mAnnotation = annotation;
            boolean z = false;
            if (intent != null) {
                if (context.getPackageManager().resolveActivity(intent, 0) != null) {
                    z = true;
                }
                this.mActionable = Boolean.valueOf(z);
            } else {
                this.mActionable = Boolean.valueOf(false);
            }
            if (this.mActionable.booleanValue()) {
                this.mListener = new -$$Lambda$AnnotationSpan$LinkInfo$z7jQ60cPKy5FsRC4nTEr8I88qP0(intent);
            } else {
                this.mListener = null;
            }
        }

        static /* synthetic */ void lambda$new$0(Intent intent, View view) {
            try {
                view.startActivityForResult(intent, 0);
            } catch (ActivityNotFoundException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Activity was not found for intent, ");
                stringBuilder.append(intent);
                Log.w(str, stringBuilder.toString());
            }
        }

        public boolean isActionable() {
            return this.mActionable.booleanValue();
        }
    }

    private AnnotationSpan(OnClickListener lsn) {
        super((String) null);
        this.mClickListener = lsn;
    }

    public void onClick(View widget) {
        if (this.mClickListener != null) {
            this.mClickListener.onClick(widget);
        }
    }

    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }

    public static CharSequence linkify(CharSequence rawText, LinkInfo... linkInfos) {
        LinkInfo[] linkInfoArr = linkInfos;
        SpannableString msg = new SpannableString(rawText);
        int i = 0;
        Annotation[] spans = (Annotation[]) msg.getSpans(0, msg.length(), Annotation.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(msg);
        int length = spans.length;
        int i2 = 0;
        while (i2 < length) {
            Annotation annotation = spans[i2];
            String key = annotation.getValue();
            int start = msg.getSpanStart(annotation);
            int end = msg.getSpanEnd(annotation);
            AnnotationSpan link = null;
            int length2 = linkInfoArr.length;
            for (int i3 = i; i3 < length2; i3++) {
                LinkInfo linkInfo = linkInfoArr[i3];
                if (linkInfo.mAnnotation.equals(key)) {
                    link = new AnnotationSpan(linkInfo.mListener);
                    break;
                }
            }
            if (link != null) {
                builder.setSpan(link, start, end, msg.getSpanFlags(link));
            }
            i2++;
            i = 0;
        }
        return builder;
    }

    public static CharSequence linkifyRemoveFingerprintUrl(CharSequence rawText, LinkInfo... linkInfos) {
        LinkInfo[] linkInfoArr = linkInfos;
        SpannableString msg = new SpannableString(rawText);
        int i = 0;
        Annotation[] spans = (Annotation[]) msg.getSpans(0, msg.length(), Annotation.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(msg);
        int length = spans.length;
        int i2 = 0;
        while (i2 < length) {
            Annotation annotation = spans[i2];
            String key = annotation.getValue();
            int start = msg.getSpanStart(annotation);
            int end = msg.getSpanEnd(annotation);
            AnnotationSpan link = null;
            int length2 = linkInfoArr.length;
            int i3 = i;
            while (i3 < length2) {
                LinkInfo linkInfo = linkInfoArr[i3];
                if (linkInfo.mAnnotation.equals("url") && start >= 0 && start < end && end <= msg.length()) {
                    builder.delete(start, end);
                    Log.d("AnnotationSpan", "refresh summary");
                    return builder;
                } else if (linkInfo.mAnnotation.equals(key)) {
                    link = new AnnotationSpan(linkInfo.mListener);
                    break;
                } else {
                    i3++;
                    linkInfoArr = linkInfos;
                }
            }
            if (link != null) {
                builder.setSpan(link, start, end, msg.getSpanFlags(link));
            }
            i2++;
            linkInfoArr = linkInfos;
            i = 0;
        }
        return builder;
    }
}
